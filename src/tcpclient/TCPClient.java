package tcpclient;

import java.net.*;
import java.io.*;


public class TCPClient {

    private Socket socket;
    private OutputStream outputS;
    private InputStream inputS;
    private final ByteArrayOutputStream buffer;

    public TCPClient() {
        socket = null;
        buffer = new ByteArrayOutputStream();
    }

    private void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        inputS = socket.getInputStream();
        outputS = socket.getOutputStream();
        System.out.println("Connected");
    }

    public byte[] askServer(String host, int port, byte[] input) throws IOException {
        connect(host, port);
        outputS.write(input);

        int temp;
        while ((temp = inputS.read()) != -1) {
            buffer.write(temp);
        }

        return buffer.toByteArray();
    }

    public byte[] askServer(String host, int port) throws IOException {
        connect(host, port);

        int temp;
        while ((temp = inputS.read()) != -1) {
            buffer.write(temp);
        }

        return buffer.toByteArray();
    }

}
