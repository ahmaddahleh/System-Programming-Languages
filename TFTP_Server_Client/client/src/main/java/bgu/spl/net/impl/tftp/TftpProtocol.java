package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessagingProtocol;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class TftpProtocol implements MessagingProtocol<byte[]> {
    private short opCode;
    private boolean shouldTerminate;
    private File fileToSend = null;
    private File fileToAceept = null;
    private List<byte[]> dataPacks = new ArrayList<>();
    private List<List<Byte>> filePartition = new ArrayList<>();
    short blockNUM = 1;
    private boolean inRRQMode = false;
    private boolean inDIRQMode = false;
    private boolean inWRQMode = false;
    private boolean shouldDisc = false;

    // this function writes to file a data
    private void writeFile(String fileName, byte[] content) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(content);
        } catch (IOException ignored) {}
    }

    // we use this to read data from a file
    private byte[] readFile(String fileName) {
        try {
            byte[] data = Files.readAllBytes(Paths.get(fileName));
            return data;
        } catch (IOException e) {
            return null;
        }
    }

    // used when calling "DIRQ" command
    public void convertAndPrintFileNames(List<byte[]> dataPackets) {
        List<Byte> ls = new ArrayList<>();
        for (byte[] packet : dataPackets) {
            for (int i = 6; i < packet.length; i++) {
                ls.add(packet[i]);
            }
        }
        StringBuilder fileNameBuilder = new StringBuilder();
        for (byte b : ls) {
            if (b != 0) {
                fileNameBuilder.append((char) b);
            } else {
                System.out.println(fileNameBuilder.toString());
                fileNameBuilder = new StringBuilder(); // Clear the StringBuilder for the next file name
            }
        }
        System.out.print(fileNameBuilder.toString());
    }
    @Override
    public byte[] process(byte[] msg) {
        opCode  = (short) ((msg[0] << 8) | (msg[1] & 0x00FF));
        if(opCode == 1){
            int startIndex = 2;
            int length = msg.length - startIndex;
            byte[] subArray = new byte[length - 1];
            // Copy bytes from startIndex to the end of the array to the new subArray without the byte 0
            System.arraycopy(msg, startIndex, subArray, 0, length - 1);
            // Convert the byte array to a string using the appropriate charset
            String fileName = new String(subArray, StandardCharsets.UTF_8);
            fileToAceept = new File(fileName);
            if(fileToAceept.exists()){
                System.out.println("file already exists!");
                return null;
            }
            else {
                try {
                    fileToAceept.createNewFile();
                } catch (IOException ignored) {}
                inRRQMode = true;
                return msg;
            }
        }
        else if (opCode == 2) {
            int startIndex = 2;
            int length = msg.length - startIndex;
            byte[] subArray = new byte[length - 1];
            // Copy bytes from startIndex to the end of the array to the new subArray without the byte 0
            System.arraycopy(msg, startIndex, subArray, 0, length - 1);
            // Convert the byte array to a string using the appropriate charset
            String fileName = new String(subArray, StandardCharsets.UTF_8);
            fileToSend = new File(fileName);
            if(!fileToSend.exists()){
                System.out.println("file doesn't exists!");
            }
            else {
                byte[] fileToRead = readFile(fileName);
                filePartition = divideByteArray(fileToRead, 512);
                inWRQMode = true;
                return msg;
            }
        } else if (opCode == 3) {
            short dataPackSize = (short) (((short) msg[2]) << 8 | (short) (msg[3]) & 0x00ff);
            short blockNum = (short) (((short) msg[4]) << 8 | (short) (msg[5]) & 0x00ff);
            dataPacks.add(msg);
            if (inRRQMode) {
                if (dataPackSize < 512) {
                    byte[] toUploadFile = concatByteArrays(dataPacks);
                    writeFile(fileToAceept.getName() , toUploadFile);
                    dataPacks.clear();
                    inRRQMode = false;
                    System.out.println("RRQ" + " " + fileToAceept + " " + "complete");
                    synchronized (this) {
                        this.notifyAll();
                    }
                }
            } else if (inDIRQMode) {
                if (dataPackSize < 512) {
                    convertAndPrintFileNames(dataPacks);
                    dataPacks.clear();
                    inDIRQMode = false;
                    synchronized (this) {
                        this.notifyAll();
                    }
                }
            }
            byte[] toret = createAckPacket(blockNum);
            return toret;
        }
        else if (opCode == 4) {
            short blockNum = (short) (((short) msg[2]) << 8 | (short) (msg[3]) & 0x00ff);
            System.out.println("ACK" + " " + blockNum);
            if(inWRQMode){
                if(!filePartition.isEmpty()){
                    List<Byte> list = filePartition.get(0);
                    filePartition.remove(0);
                    if(!list.isEmpty()) {
                        byte[] toret = createDataPacket(blockNUM, list);
                        blockNUM++;
                        return toret;
                    }
                    else{
                        byte[] arr = new byte[1];
                        arr[0] = 0;
                        byte[] toret = createDataPacket2(blockNUM, arr);
                        blockNUM = 1;
                        return toret;
                    }
                }
                else {
                    inWRQMode = false;
                    System.out.println("WRQ" + " " + fileToSend + " " + "complete");
                    synchronized (this) {
                        this.notifyAll();
                    }
                }
            }
            if(shouldDisc){
                shouldTerminate = true;
            }
        }
        else if (opCode == 5) {
            if(inRRQMode){
                inRRQMode = false;
                fileToAceept.delete();
            }
            if (inWRQMode) inWRQMode = false;
            if(inDIRQMode) inDIRQMode = false;
            if(shouldDisc) shouldDisc = false;
            short errorCode = (short) (((short) msg[2]) << 8 | (short) (msg[3]) & 0x00ff);
            int length = msg.length - 5;
            byte[] subArray = new byte[length];
            // Copy bytes from startIndex to the end of the array to the new subArray without the byte 0
            System.arraycopy(msg, 4, subArray, 0, length);
            // Convert the byte array to a string using the appropriate charset
            String errorMsg = new String(subArray, StandardCharsets.UTF_8);
            System.out.println("Error" + " " + errorCode + " " + errorMsg);
            synchronized (this) {
                this.notifyAll();
            }
        }
        else if (opCode == 6) {
            inDIRQMode = true;
            return msg;
        }
        else if (opCode == 7) {
            return msg;
        }
        else if (opCode == 8) {
            return msg;
        }
        else if (opCode == 9) {
            int length = msg.length - 2;
            byte[] subArray = new byte[length - 1];
            // Copy bytes from startIndex to the end of the array to the new subArray without the byte 0
            System.arraycopy(msg, 3, subArray, 0, length - 1);
            // Convert the byte array to a string using the appropriate charset
            String filename = new String(subArray, StandardCharsets.UTF_8);
            if(msg[2] == (byte) 0){
                System.out.println("BCAST" + " " + "deleted" + " " + filename);
            }
            else {
                System.out.println("BCAST" + " " + "added" + " " +  filename);
            }
        }
        else if (opCode == 10) {
            shouldDisc = true;
            return msg;
        }
        synchronized (this) {
            this.notifyAll();
        }
        return null;
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    public byte[] createAckPacket(short blockNumber) {
        // Calculate the size of the ACK packet
        int packetSize = 2 + 2; // Opcode (2 bytes) + Block Number (2 bytes)

        // Create the byte array for the ACK packet
        byte[] ackPacket = new byte[packetSize];

        // Set the opcode (4)
        ackPacket[0] = 0;
        ackPacket[1] = 4;

        // Set the block number (short, big-endian)
        ackPacket[2] = (byte) (blockNumber >> 8);
        ackPacket[3] = (byte) blockNumber;

        return ackPacket;
    }

    public static List<List<Byte>> divideByteArray(byte[] data, int chunkSize) {
        List<List<Byte>> chunks = new ArrayList<>();
        int offset = 0;

        while (offset < data.length) {
            int length = Math.min(chunkSize, data.length - offset);
            byte[] chunk = new byte[length];
            System.arraycopy(data, offset, chunk, 0, length);
            List<Byte> chunkList = byteArrayToList(chunk);
            chunks.add(chunkList);
            offset += length;
        }
        // Check if the size of the original data array is divisible by 512
        if (data.length % 512 == 0) {
            chunks.add(new ArrayList<>()); // Add an empty byte list
        }

        return chunks;
    }
    private static List<Byte> byteArrayToList(byte[] array) {
        List<Byte> list = new ArrayList<>();
        for (byte b : array) {
            list.add(b);
        }
        return list;
    }
    public byte[] createDataPacket(short blockNumber, List<Byte> data) {
        short packetSize = (short) data.size();
        // Assemble the data packet
        byte[] packet = new byte[6 + packetSize]; // Opcode (2 bytes) + Packet Size (2 bytes) + Block Number (2 bytes) + Data
        // Set the opcode (3)
        packet[0] = 0;
        packet[1] = 3;
        // Set the packet size (big-endian)
        packet[2] = (byte) (packetSize >> 8);
        packet[3] = (byte) packetSize;
        // Set the block number (big-endian)
        packet[4] = (byte) (blockNumber >> 8);
        packet[5] = (byte) blockNumber;
        // Copy the data bytes into the packet
        for (int i = 0; i < packetSize; i++) {
            packet[6 + i] = data.get(i);
        }
        return packet;
    }
    public byte[] createDataPacket2(short blockNumber, byte[] data) {
        short packetSize = (short) data.length;
        // Assemble the data packet
        byte[] packet = new byte[6 + packetSize]; // Opcode (2 bytes) + Packet Size (2 bytes) + Block Number (2 bytes) + Data
        // Set the opcode (3)
        packet[0] = 0;
        packet[1] = 3;
        // Set the packet size (big-endian)
        packet[2] = (byte) (packetSize >> 8);
        packet[3] = (byte) packetSize;
        // Set the block number (big-endian)
        packet[4] = (byte) (blockNumber >> 8);
        packet[5] = (byte) blockNumber;
        // Copy the data bytes into the packet
        System.arraycopy(data, 0, packet, 6, packetSize);

        return packet;
    }
    public byte[] concatByteArrays(List<byte[]> byteArrayList) {
        // Calculate the total length of the concatenated byte array
        int totalLength = 0;
        for (byte[] byteArray : byteArrayList) {
            totalLength += byteArray.length - 6; // Subtract 6 to start from index 6
        }

        // Create the concatenated byte array
        byte[] concatenatedArray = new byte[totalLength];
        int currentIndex = 0;

        // Concatenate each byte array starting from index 6
        for (byte[] byteArray : byteArrayList) {
            for (int i = 6; i < byteArray.length; i++) {
                concatenatedArray[currentIndex++] = byteArray[i];
            }
        }

        return concatenatedArray;
    }

    public byte[] arrayListToByteArray(LinkedList<Byte> arrayList) {
        byte[] byteArray = new byte[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i++) {
            byteArray[i] = arrayList.get(i);
        }
        return byteArray;
    }

}
