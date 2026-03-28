package bgu.spl.net.impl.tftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ListeningThread implements Runnable{
    private TftpProtocol protocol;
    private TftpEncoderDecoder encDec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;

    public ListeningThread(TftpProtocol protoc, TftpEncoderDecoder encDec, Socket sock){
        this.protocol = protoc;
        this.encDec = encDec;
        this.sock = sock;
    }
    @Override
    public void run() {

        try {
            while (!protocol.shouldTerminate()) {
                int read;
                in = new BufferedInputStream(sock.getInputStream());
                out = new BufferedOutputStream(sock.getOutputStream());
                while (!protocol.shouldTerminate() && (read = in.read()) >= 0) {
                    byte[] nextMessage = encDec.decodeNextByte((byte) read);
                    if (nextMessage != null) {
                 byte[] response = protocol.process(nextMessage);
                        if (response != null) {
                            out.write(response);
                            out.flush();
                        }
                    }
                }
            }
            in.close();
            out.close();
            sock.close();
        }catch (IOException e) {
        }
    }
}
