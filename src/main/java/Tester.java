import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Tester {

    public static void main(String[] args) throws IOException, InterruptedException {
        while (true) {
            try {
                System.out.println("Opened socket");
                Socket socket = new Socket("localhost", 6379);
                OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                socket.getOutputStream().write(
                        "*1\r\n$4\r\nPING\r\n".getBytes(StandardCharsets.UTF_8)
                );
                socket.getOutputStream().flush();
//                String input = new Scanner(System.in).nextLine();
//                writer.write(input);
//                writer.flush();

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (Exception e) {
                System.out.println(e);
                Thread.sleep(1000);
            }
        }

    }
}
