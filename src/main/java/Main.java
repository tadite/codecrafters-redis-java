import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {

    private static final byte[] PONG = "PONG".getBytes(StandardCharsets.UTF_8);
    private static final byte[] LINE_END = "\r\n".getBytes(StandardCharsets.UTF_8);

    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");

        //  Uncomment this block to pass the first stage
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = 6379;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            System.out.println("Wait for connection from client.");
            clientSocket = serverSocket.accept();
            System.out.println("Client connected.");
            handle(clientSocket);
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }

    private static void handle(Socket socket) throws IOException {
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();
        while (true) {
            int c = is.read();
            if (c == '*') {
                int argCount = readNumber(is);
                System.out.println("*" + argCount);
                byte[][] args = new byte[argCount][];
                for (int argIndex = 0; argIndex < argCount; argIndex++) {
                    c = is.read();
                    if (c != '$') {
                        throw new ProtocolParseException(String.format("Expected $ but found %c", c));
                    }
                    int argBytesSize = readNumber(is);
                    byte[] arg = new byte[argBytesSize];
                    for (int argByteIdx = 0; argByteIdx < argBytesSize; argByteIdx++) {
                        arg[argByteIdx] = (byte) is.read();
                    }
                    readLineEnd(is);
                    args[argIndex] = arg;
                }

                handleCommand(args, os);

                System.out.println();
                os.flush();
            }
        }
    }

    private static void handleCommand(byte[][] args, OutputStream os) throws IOException {
        String name = new String(args[0]);
        if ("PING".equals(name)) {
            if (args.length == 1) {
                response(os, PONG);
            } else if (args.length == 2) {
                response(os, args[1]);
            }
        } else {
            responseError(os);
        }
    }

    private static void responseError(OutputStream os) throws IOException {
        os.write("-Error message\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private static void response(OutputStream os, byte[] msg) throws IOException {
        os.write('+');
        os.write(msg);
        os.write(LINE_END);
    }

    private static void readLineEnd(InputStream is) throws IOException {
        int c1 = is.read();
        int c2 = is.read();
        if (c1 != '\r' || c2 != '\n') {
            throw new ProtocolParseException(String.format("Expected line end but found %c%c", c1, c2));
        }
    }

    private static int readNumber(InputStream is) throws IOException {
        int c;
        int argCount = 0;
        while ((c = is.read()) != '\r') {
            int number = c - '0';
            argCount = argCount * 10 + number;
        }
        is.read(); // skip \n
        return argCount;
    }

    static class ProtocolParseException extends RuntimeException {

        public ProtocolParseException(String message) {
            super(message);
        }
    }
}
