package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.ConnectionsImpl;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {
    private static final String currentDirectory = "Files";
    static private ConcurrentHashMap<String , Boolean> loggedInClients = new ConcurrentHashMap<>();
    static private ConcurrentHashMap<Integer , Boolean> loggedInIDS= new ConcurrentHashMap<>();
    private boolean shouldTerminate = false;
    private ConnectionsImpl<byte[]> byteConnections;
    private int connectionId;
    private String userName;
    private boolean isLoggedIn;
    private List<byte[]> dataPacks = new ArrayList<>();
    private short opCode;
    private String toWriteFile = "";
    private boolean errorFound = false ;
    private String errorMsg = "test";
    private short errorCode = 100;
    private List<List<Byte>> filePartition = new ArrayList<>();
    private List<byte[]> fileNamesPartition = new ArrayList<>();
    private short blockNUM;

    // these are helper functions to perform action on the "Files" Folder.
    private void writeFile(String folderPath, String fileName, byte[] content) {
        try {
            File file = new File(folderPath, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                outputStream.write(content);
            }
        } catch (IOException e) {}
    }

    private byte[] readFile(String folderPath, String fileName) {
        try {
            byte[] data = Files.readAllBytes(Paths.get(folderPath, fileName));
            return data;
        } catch (IOException e) {
            return null;
        }
    }


    private void removeFile(String folderPath, String fileName) {
        File fileToRemove = new File(folderPath, fileName);
        if (fileToRemove.exists()) {
            fileToRemove.delete();
        }
    }
    public static byte[] getFileNamesFromFolder(String folderPath) {
        // Create a File object for the folder
        File folder = new File(folderPath);

        // Get the list of files in the folder
        File[] files = folder.listFiles();
        System.out.println(Arrays.toString(files));
        // Determine the number of files
        int numFiles; // Define a variable to store the number of files
        if (files != null) {  // Check if 'files' is not null
            numFiles = files.length; // If not null, count the number of files
        } else {
            numFiles = 0; // If null, there are no files, so set the number of files to 0
        }

        //Create a list to store the bytes of file names
        List<Byte> bytesList = new ArrayList<>();

        // Iterate through the files and store their names as bytes
        for (int i = 0; i < numFiles; i++) {
            File file = files[i];
            String fileName = file.getName();
            byte[] fileNameBytes = fileName.getBytes();
            for (byte b : fileNameBytes) {
                bytesList.add(b);
            }
            // Add null terminator (zero byte) after each file name except for the last one
            bytesList.add((byte) 0);
        }

        // Convert the list of bytes to byte array
        byte[] fileNamesBytes = new byte[bytesList.size()];
        for (int i = 0; i < bytesList.size(); i++) {
            fileNamesBytes[i] = bytesList.get(i);
        }
        return fileNamesBytes;
    }


    @Override
    public void start(int connectionId, ConnectionsImpl<byte[]> connections) {
        // TODO implement this
        this.shouldTerminate = false;
        this.byteConnections =  connections;
        this.connectionId = connectionId;
        this.userName = "";
        this.isLoggedIn = false;
        this.dataPacks = new ArrayList<>();
    }

    @Override
    public void process(byte[] message) {
        // TODO implement this
        opCode  = (short) ((message[0] << 8) | (message[1] & 0x00FF));
        /**
         * WE SHOULD CHECK IF THE USER IS ONLINE :
         */
        if(!isLoggedIn){
            errorFound = true;
            if(errorCode > 6) {
                errorCode = 6;
                errorMsg = "User not logged in!";
            }
        }
        if(opCode == 1) {
            System.out.println(Arrays.toString(message));
            int startIndex = 2;
            int length = message.length - startIndex;
            byte[] subArray = new byte[length - 1];
            // Copy bytes from startIndex to the end of the array to the new subArray without the byte 0
            System.arraycopy(message, startIndex, subArray, 0, length - 1);
            // Convert the byte array to a string using the appropriate charset
            String fileName = new String(subArray, StandardCharsets.UTF_8);
            File file = new File(currentDirectory,fileName);
            if (file.exists() && isLoggedIn) {
                // here we should return the file as data packets...
                byte[] fileToRead = readFile(currentDirectory, fileName);
                if (fileToRead.length > 512) {
                    filePartition = divideByteArray(fileToRead, 512);
                    blockNUM = 1;
                    byte[] toret = createDataPacket(blockNUM, filePartition.remove(0));
                    byteConnections.send(connectionId, toret);
                    blockNUM++;
                } else {
                    short blockNum = 1;
                    byte[] toSend = createDataPacket2(blockNum, fileToRead);
                    byteConnections.send(connectionId, toSend);
                }
            } else if (!file.exists()) {
                errorFound = true;
                System.out.println(errorCode);
                if (errorCode > 1) {
                    errorCode = 1;
                    errorMsg = "RRQ of non-existing file!";
                }
            }
        }
        else if(opCode == 2 ){
            int startIndex = 2;
            int length = message.length - startIndex;
            byte[] subArray = new byte[length - 1];
            // Copy bytes from startIndex to the end of the array to the new subArray with out the byte 0
            System.arraycopy(message, startIndex, subArray, 0, length - 1);
            // Convert the byte array to a string using the appropriate charset
            String fileName = new String(subArray, StandardCharsets.UTF_8);
            File file = new File(currentDirectory,fileName);
            if(!file.exists() && isLoggedIn){
                short blocknum = 0;
                byte[] toSend = createAckPacket(blocknum);
                byteConnections.send(connectionId , toSend);
                toWriteFile = fileName;
            }
            else {
                errorFound = true;
                errorCode = 5;
                errorMsg = "File already exists!";
            }
        } else if (opCode == 3 && isLoggedIn) {
            short dataPackSize = (short) (((short) message[2]) << 8 | (short) (message[3]) & 0x00ff);
            short blockNum = (short) (((short) message[4]) << 8 | (short) (message[5]) & 0x00ff);
            dataPacks.add(message);
            if(dataPackSize < 512){
                byte[] toUploadFile = concatByteArrays(dataPacks);
                writeFile(currentDirectory , toWriteFile , toUploadFile);
                byte[] BCastArr = createBcastPacket(1 , toWriteFile);
                for(Integer connId : loggedInIDS.keySet()){
                    byteConnections.send(connId , BCastArr);
                }
                dataPacks.clear();
                toWriteFile = "";
            }
            byte[] toret = createAckPacket(blockNum);
            byteConnections.send(connectionId , toret);
        } else if (opCode == 4 && isLoggedIn) {
            if(!filePartition.isEmpty()){
                List<Byte> list = filePartition.get(0);
                filePartition.remove(0);
                if(!list.isEmpty()) {
                    byte[] toret = createDataPacket(blockNUM, list);
                    byteConnections.send(connectionId, toret);
                }
                else{
                    byte[] arr = new byte[1];
                    arr[0] = 0;
                    byte[] toret = createDataPacket2(blockNUM, arr);
                    byteConnections.send(connectionId, toret);
                }
            } else if ((!fileNamesPartition.isEmpty())) {
                byte[]arr = fileNamesPartition.get(0);
               fileNamesPartition.remove(0);
                    byte[] toret = createDataPacket2(blockNUM, arr);
                    byteConnections.send(connectionId, toret);

            }
            blockNUM++;
            if(fileNamesPartition.isEmpty() && filePartition.isEmpty()) {
                blockNUM = 0;
            }
        }
        else if (opCode == 6 && isLoggedIn) {
            byte[] filesNames = getFileNamesFromFolder(currentDirectory);
            if(filesNames.length > 512){
                fileNamesPartition = divideIntoChunks(filesNames);
                byte[] toret = createDataPacket2(blockNUM , fileNamesPartition.remove(0));
                byteConnections.send(connectionId , toret);
                blockNUM++;
            }
            else{
                short blockNum = 1;
                byte[] toret = createDataPacket2(blockNum , filesNames);
                byteConnections.send(connectionId , toret);
            }
        } else if (opCode ==  7) {
            int startIndex = 2;
            int length = message.length - startIndex;
            byte[] subArray = new byte[length - 1];
            // Copy bytes from startIndex to the end of the array to the new subArray
            System.arraycopy(message, startIndex, subArray, 0, length - 1);
            // Convert the byte array to a string using the appropriate charset
            userName = new String(subArray, StandardCharsets.UTF_8);
            if(!loggedInClients.containsKey(userName) && !isLoggedIn) {
                errorFound = false;
                loggedInClients.put(userName , true);
                loggedInIDS.put(connectionId , true);
                isLoggedIn = true;
                short blocknum = 0;
                byte[] toret = createAckPacket(blocknum);
                byteConnections.send(connectionId , toret);
            }
            else {
                errorFound = true;
                if(errorCode > 7) {
                    errorCode = 7;
                    errorMsg = "client already LoggedIn !";
                }
            }
        } else if (opCode == 8 ) {
            int startIndex = 2;
            int length = message.length - startIndex;
            byte[] subArray = new byte[length - 1];
            // Copy bytes from startIndex to the end of the array to the new subArray
            System.arraycopy(message, startIndex, subArray, 0, length - 1);
            // Convert the byte array to a string using the appropriate charset
            String fileName = new String(subArray, StandardCharsets.UTF_8);
            File file = new File(currentDirectory,fileName);
            // we check if the Files folder contains filename and act accordingly
            if (file.exists() && isLoggedIn) {
                removeFile(currentDirectory, fileName);
                short blocknum = 0;
                byte[] toret = createAckPacket(blocknum);
                byteConnections.send(connectionId, toret);
                byte[] BCastArr = createBcastPacket(0, fileName);
                for(Integer connId : loggedInIDS.keySet()){
                    byteConnections.send(connId , BCastArr);
                }
            } else if (!file.exists()) {
                errorFound = true;
                if (errorCode > 1) {
                    errorCode = 1;
                    errorMsg = "DELRQ of non-existing file!";
                }
            }
        }
        else if (opCode == 10 && isLoggedIn) {
            short blockNum = 0;
            byte[] toret = createAckPacket(blockNum);
            byteConnections.send(connectionId, toret);
            byteConnections.disconnect(connectionId);
            loggedInClients.remove(userName);
            loggedInIDS.remove(connectionId);
            shouldTerminate = true;
        }
        if(errorFound) {
            byte[] toret = createErrorPacket(errorCode, errorMsg);
            byteConnections.send(connectionId, toret);
            errorFound = false;
            errorCode = 100;
            errorMsg = "";
        }
    }

    @Override
    public boolean shouldTerminate() {
        // TODO implement this
       return shouldTerminate;
    }

    // Helper Functions :
    public byte[] createErrorPacket(short errorCode, String errorMessage) {
        // Convert error message to UTF-8 bytes
        byte[] errorMessageBytes = errorMessage.getBytes(StandardCharsets.UTF_8);

        // Calculate the size of the error packet
        int packetSize = 2 + 2 + errorMessageBytes.length + 1; // Opcode (2 bytes) + ErrorCode (2 bytes) + ErrorMsg + zero terminator

        // Create the byte array for the error packet
        byte[] errorPacket = new byte[packetSize];

        // Set the opcode (5)
        errorPacket[0] = 0;
        errorPacket[1] = 5;

        // Set the error code (short, big-endian)
        errorPacket[2] = (byte) (errorCode >> 8);
        errorPacket[3] = (byte) errorCode;

        // Set the error message
        System.arraycopy(errorMessageBytes, 0, errorPacket, 4, errorMessageBytes.length);

        // Set the zero terminator
        errorPacket[packetSize - 1] = 0;

        return errorPacket;
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

    public byte[] createBcastPacket(int action, String filename) {
        // Convert filename to UTF-8 bytes
        byte[] filenameBytes = filename.getBytes(StandardCharsets.UTF_8);

        // Calculate the size of the BCAST packet
        int packetSize = 2 + 1 + filenameBytes.length + 1; // Opcode (2 bytes) + Deleted/Added (1 byte) + Filename + Zero terminator

        // Create the byte array for the BCAST packet
        byte[] bcastPacket = new byte[packetSize];

        // Set the opcode (9)
        bcastPacket[0] = 0;
        bcastPacket[1] = 9;

        // Set Deleted/Added flag
        bcastPacket[2] = (byte) action;

        // Set the filename
        System.arraycopy(filenameBytes, 0, bcastPacket, 3, filenameBytes.length);

        // Set the zero terminator
        bcastPacket[packetSize - 1] = 0;

        return bcastPacket;
    }

    public byte[] createDataPacket(short blockNumber, List<Byte> data) {
        // Calculate packet size
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

    public byte[] createDataPacket2(short blockNumber, byte[] data) {
        // Calculate packet size
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

    public static List<byte[]> divideIntoChunks(byte[] fileNamesBytes) {
        List<byte[]> chunks = new ArrayList<>();
        int start = 0;
        int end = 0;
        int currentSize = 0;

        for (int i = 0; i < fileNamesBytes.length; i++) {
            if (fileNamesBytes[i] == 0) {
                currentSize++;
            }

            if (currentSize == 512) {
                end = i + 1;
                byte[] chunk = new byte[end - start];
                System.arraycopy(fileNamesBytes, start, chunk, 0, end - start);
                chunks.add(chunk);
                start = end;
                currentSize = 0;
            }
        }

        // Add the remaining bytes as the last chunk
        if (start < fileNamesBytes.length) {
            byte[] chunk = new byte[fileNamesBytes.length - start];
            System.arraycopy(fileNamesBytes, start, chunk, 0, fileNamesBytes.length - start);
            chunks.add(chunk);
        }

        return chunks;
    }
}
