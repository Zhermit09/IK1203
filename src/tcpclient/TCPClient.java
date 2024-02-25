package tcpclient;

import java.net.*;
import java.io.*;


public class TCPClient {

    private final boolean shutdown;
    private final Integer limit;
    private final Integer timeout;

    private Socket socket;
    private OutputStream outputS;
    private InputStream inputS;

    private final byte[] buffer;
    private final ByteArrayOutputStream output;

    public TCPClient(boolean shutdown, Integer timeout, Integer limit) throws IOException {
        this.timeout = timeout;
        this.shutdown = shutdown;
        this.limit = limit;

        buffer = new byte[1024];
        output = new ByteArrayOutputStream();
    }

    private void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        inputS = socket.getInputStream();
        outputS = socket.getOutputStream();

        if (timeout != null) {
            socket.setSoTimeout(timeout);
        }
    }

    public byte[] askServer(String host, int port, byte[] input) throws IOException {
        connect(host, port);
        outputS.write(input);

        if (shutdown) {
            socket.shutdownOutput();
        }

        try {
            int bytes;
            if (limit != null) {
                while ((bytes = inputS.read(buffer)) != -1) {
                    if (output.size() + bytes >= limit) {
                        output.write(buffer, 0, limit - output.size());
                        break;
                    }
                    output.write(buffer, 0, bytes);
                }
            } else {
                while ((bytes = inputS.read(buffer)) != -1) {
                    output.write(buffer, 0, bytes);
                }
            }
        } catch (SocketTimeoutException ex) {
            System.err.println(ex);
        }

        socket.close();
        return output.toByteArray();
    }

}
