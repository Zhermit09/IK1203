package tcpclient;

import java.net.*;
import java.io.*;


public class TCPClient {

    private final boolean shutdown;
    private final Integer limit;
    private final Socket socket;
    private OutputStream outputS;
    private InputStream inputS;
    private final ByteArrayOutputStream buffer;

    public TCPClient(boolean shutdown, Integer timeout, Integer limit) throws IOException {

        socket = new Socket();

        if (timeout != null) {
            socket.setSoTimeout(timeout);
        }

        socket.setReceiveBufferSize(1);

        this.shutdown = shutdown;
        this.limit = limit;
        buffer = new ByteArrayOutputStream();
    }

    private void connect(String host, int port) throws IOException {
        InetAddress addr = InetAddress.getByName(host);
        InetSocketAddress endP = new InetSocketAddress(addr, port);

        socket.connect(endP);
        inputS = socket.getInputStream();
        outputS = socket.getOutputStream();
    }

    public byte[] askServer(String host, int port, byte[] input) throws IOException {
        connect(host, port);
        outputS.write(input);

        if (shutdown) {
            socket.shutdownOutput();
        }

        int temp;
        try {
            if(limit != null) {
                while ((temp = inputS.read()) != -1 && buffer.size() < limit) {
                    buffer.write(temp);
                }
            }else {
                while ((temp = inputS.read()) != -1) {
                    buffer.write(temp);
                }
            }
        } catch (SocketTimeoutException ex) {
            System.err.println(ex);
        }

        socket.close();
        return buffer.toByteArray();
    }

}
