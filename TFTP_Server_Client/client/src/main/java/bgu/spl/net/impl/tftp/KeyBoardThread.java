package bgu.spl.net.impl.tftp;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

public class KeyBoardThread implements Runnable{
    private TftpProtocol protocol;
    private TftpEncoderDecoder encDec;
    private final Socket sock;
    private BufferedOutputStream out;

    public KeyBoardThread(TftpProtocol protoc, TftpEncoderDecoder encDec, Socket sock) {
        this.protocol = protoc;
        this.encDec = encDec;
        this.sock = sock;
    }
    @Override
    public void run() {
        try {
            out = new BufferedOutputStream(sock.getOutputStream());
            Scanner scanner = new Scanner(System.in);
            while (!protocol.shouldTerminate()) {
                String input = scanner.nextLine();
                // here we handle the input then :
                byte[] encoded = encDec.encode(input);
                if (encoded != null) {
                    byte[] processed = protocol.process(encoded);
                    if( processed != null ) {
                        out.write(processed);
                        out.flush();
                        try {
                            synchronized (protocol) {
                                protocol.wait();
                            }
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
            out.close();
            sock.close();
        } catch (IOException e) {}
    }
}
