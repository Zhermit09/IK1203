import tcpclient.TCPClient;

import java.net.*;
import java.io.*;

public class HTTPAsk {
    static String hostname;
    static Integer port;
    static String input;
    static boolean shutdown;
    static Integer limit;
    static Integer timeout;


    private static void reset() {
        hostname = null;
        port = null;
        input = null;
        shutdown = false;
        limit = null;
        timeout = null;
    }

    private static void usage() {
        System.err.println("Usage: HTTPAsk port");
        System.exit(1);
    }

    private static int parseArg(String[] args) {
        if (args.length != 1) {
            usage();
        }
        int temp = -1;
        try {
            temp = Integer.parseInt(args[0]);
        } catch (Exception e) {
            usage();
        }
        return temp;
    }


    private static void badRequestResponse(OutputStream outputS) throws IOException {
        outputS.write("""
                HTTP/1.1 400 Bad Request
                Content-Type: text/plain
                Content-Length: 37
                                    
                400 Bad Request: Invalid Query Syntax
                """.getBytes());
    }

    private static void notFoundResponse(OutputStream outputS) throws IOException {
        outputS.write("""
                HTTP/1.1 404 Not Found
                Content-Type: text/plain
                Content-Length: 43
                                    
                404 Not Found: Query Could Not Be Completed
                """.getBytes());
    }

    private static void httpResponse(OutputStream outputS) throws IOException {

        try {
            TCPClient tcpClient = new TCPClient(shutdown, timeout, limit);
            byte[] outputBytes = tcpClient.askServer(hostname, port, input == null ? new byte[0] : (input + "\n").getBytes());

            outputS.write(String.format("""
                    HTTP/1.1 200 OK
                    Content-Type: text/plain
                    Content-Length: %d
                                        
                    """, outputBytes.length).getBytes());
            outputS.write(outputBytes);

        } catch (Exception e) {
            notFoundResponse(outputS);
        }
    }

    private static void handleQuery(String query, OutputStream outputS) throws IOException {

        String[] reqLine = query.split("\\s+");

        if (reqLine.length != 3 || !reqLine[0].equals("GET") || !reqLine[2].equals("HTTP/1.1")) {
            badRequestResponse(outputS);
            return;
        }

        String[] subQuery = reqLine[1].split("\\?");
        if (subQuery.length != 2 || !subQuery[0].equals("/ask")) {
            notFoundResponse(outputS);
            return;
        }
        String[] param = subQuery[1].split("&");
        try {
            for (String str : param) {
                String[] keyVal = str.split("=");
                String key = keyVal[0];
                String val = keyVal[1];

                switch (key) {
                    case "hostname":
                        hostname = val;
                        break;
                    case "port":
                        port = Integer.parseInt(val);
                        break;
                    case "string":
                        input = val;
                        break;
                    case "shutdown":
                        shutdown = Boolean.parseBoolean(val);
                        break;
                    case "limit":
                        limit = Integer.parseInt(val);
                        break;
                    case "timeout":
                        timeout = Integer.parseInt(val);
                        break;
                    default:
                        badRequestResponse(outputS);
                        return;
                }
            }

        } catch (Exception e) {
            badRequestResponse(outputS);
        }
    }

    private static void httpListen(Socket socket) throws IOException {
        InputStream inputS = socket.getInputStream();
        OutputStream outputS = socket.getOutputStream();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        int bytes;
        while ((bytes = inputS.read(buffer)) != -1) {
            output.write(buffer, 0, bytes);
            if (output.toString().contains("\r\n\r\n")) {
                break;
            }
        }

        try {
            handleQuery(output.toString().split("\r\n", 2)[0], outputS);
            httpResponse(outputS);
        } catch (Exception e) {
            badRequestResponse(outputS);
        }
    }

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(parseArg(args));

        while (true) {
            Socket socket = serverSocket.accept();
            httpListen(socket);
            socket.close();
            reset();
        }
    }
}

