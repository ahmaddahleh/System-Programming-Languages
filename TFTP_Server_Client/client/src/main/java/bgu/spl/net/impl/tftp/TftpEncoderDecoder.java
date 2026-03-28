package bgu.spl.net.impl.tftp;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

public class TftpEncoderDecoder{
    private LinkedList<Byte> byteList = new LinkedList<Byte>();
    private short opCode;
    private short dataPackSize;

    public byte[] decodeNextByte(byte nextByte) {
        byteList.add(nextByte);
        if (byteList.size() >= 2) {
            opCode = (short) ((byteList.get(0) << 8) | (byteList.get(1) & 0x00FF));
            if (byteList.size() >= 4) {
                if (opCode == 4) {
                    return arrayListToByteArray(byteList);
                } else if (opCode == 3) {
                    dataPackSize = (short) (((short) byteList.get(2)) << 8 | (short) byteList.get(3) & 0x00ff);
                    if (byteList.size() == dataPackSize + 6) {
                        return arrayListToByteArray(byteList);
                    }
                } else if (nextByte == '\0' && byteList.size() > 4) {
                    return arrayListToByteArray(byteList);
                }
            }
        }
        return null;
    }

    public byte[] encode(String message) {
        LinkedList<Byte> toSend = new LinkedList<>();
        short opCode;
        int indexOfSpace = message.indexOf(' ');
        if( message.equals("DIRQ")){
            byte[] arr = new byte[2];
            opCode = 6;
            arr[0] = (byte) ((opCode >> 8));
            arr[1] = (byte) ( opCode & 0x00ff );
            return arr;
        }else if (message.equals("DISC")){
            byte[] arr = new byte[2];
            opCode = 10;
            arr[0] = (byte) ((opCode >> 8));
            arr[1] = (byte) (opCode & 0x00ff);
            return arr;
        }
        if(indexOfSpace < 0){
            System.out.println("invalid input");
            return null;
        }
        String opt = message.substring(0, indexOfSpace);
        if ("RRQ".equals(opt)) {
            opCode = 1;
            createPack(toSend, message.substring(indexOfSpace + 1).getBytes(StandardCharsets.UTF_8), opCode);
        } else if ("WRQ".equals(opt)) {
            opCode = 2;
            createPack(toSend, message.substring(indexOfSpace + 1).getBytes(StandardCharsets.UTF_8), opCode);
        }  else if ("LOGRQ".equals(opt)) {
            opCode = 7;
            createPack(toSend, message.substring(indexOfSpace + 1).getBytes(StandardCharsets.UTF_8), opCode);
        } else if ("DELRQ".equals(opt)) {
            opCode = 8;
            createPack(toSend, message.substring(indexOfSpace + 1).getBytes(StandardCharsets.UTF_8), opCode);
        }  else {
            System.out.println("invalid input");
            return null;
        }
        return arrayListToByteArray(toSend);
    }

    private static void createPack(LinkedList<Byte> byteList, byte[] userOrFileName, short value) {
        // Use bitwise operations to convert short to byte and store it in the list
        byteList.add((byte) (value >> 8));
        byteList.add((byte) (value & 0x00FF));
        // Add userOrFileName bytes to the list
        for (byte b : userOrFileName) {
            byteList.add(b);
        }
        // Add null terminator
        byteList.add((byte) 0);
    }


    // this function converts an arraylist to an array of bytes , used in decode next byte.
    public byte[] arrayListToByteArray(LinkedList<Byte> arrayList) {
        byte[] byteArray = new byte[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i++) {
            byteArray[i] = arrayList.get(i);
        }
        arrayList.clear();
        return byteArray;
    }

}
