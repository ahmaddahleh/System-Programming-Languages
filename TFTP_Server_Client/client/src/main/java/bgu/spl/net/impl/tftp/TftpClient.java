package bgu.spl.net.impl.tftp;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class TftpClient {
    //TODO: implement the main logic of the client, when using a thread per client the main logic goes here
    public static void main(String[] args) {
        try (Socket sock = new Socket(args[0], Integer.parseInt(args[1]))) {
            TftpProtocol proc=new TftpProtocol();
            TftpEncoderDecoder enc=new TftpEncoderDecoder();
            Thread Listener = new Thread(new ListeningThread(proc,enc, sock));
            Listener.start();
            KeyBoardThread write =new KeyBoardThread(proc,enc,sock);
            write.run();
        } catch (IOException ignored) {}
    }
}
