package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;


public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {
    //TODO: Implement here the TFTP encoder and decoder
    private LinkedList<Byte> byteList = new LinkedList<Byte>();
    private short opCode;
    private short dataPackSize;
    @Override
    public byte[] decodeNextByte(byte nextByte) {
        // TODO: implement this
        byteList.add(nextByte);
        if (byteList.size() >= 2) {
            opCode = (short) ((byteList.get(0) << 8) | (byteList.get(1) & 0x00FF));
            if (opCode == 6 || opCode == 10) {;
                return arrayListToByteArray(byteList);
            }
            if (byteList.size() >= 4) {
                if (opCode == 4) {
                    return arrayListToByteArray(byteList);
                } else if (opCode == 3) {
                    dataPackSize = (short) (((short) byteList.get(2)) << 8 | (short) byteList.get(3) & 0x00ff);
                    if (byteList.size() == dataPackSize + 6) {
                        return arrayListToByteArray(byteList);
                    }
                }else if ( nextByte == '\0'){
                    return arrayListToByteArray(byteList);
                }
            }
        }
        return null;
    }

    @Override
    public byte[] encode(byte[] message) {
        return message;
    }


    // this function converts an arraylist to an array of bytes , used in decode next byte.
    public static byte[] arrayListToByteArray(LinkedList<Byte> arrayList) {
        byte[] byteArray = new byte[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i++) {
            byteArray[i] = arrayList.get(i);
        }
        arrayList.clear();
        return byteArray;
    }
}