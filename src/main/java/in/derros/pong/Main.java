package in.derros.pong;

import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketAddress;

/**
 * Created by derros on 5/16/17.
 */
public class Main {

    public static String server_address;
    public static int server_port;
    public static int this_port;
    public static int operating_mode; // 1 = server, 0 = client
    private static void sendPing(String addr, int port) {
        try {
            Socket sock = new Socket(addr, port);
            String s = "ping";
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out.write("ping");
            out.flush();
            //System.out.println(in.readLine());
        } catch (IOException ioException) {
            System.out.println("sendPing(): Connect error. Printing stack trace...");
            ioException.printStackTrace();
            System.out.println("sendPing(): Aborting...");
            System.exit(130);
        }

    }

    private static void returnPong(int port) {
        try {
            ServerSocket serverSock = new ServerSocket(port);
            System.out.println("returnPong(): Listening on port: " + port);
            Socket send = serverSock.accept();
            System.out.println("accepted socket");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(send.getOutputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(send.getInputStream()));
            System.out.println("created both writer and reader.");
            System.out.println(br.readLine());
            bw.write("pong");
            bw.flush();
        } catch (IOException ioException) {
            System.out.println("returnPong(): Server socket cannot be established. Printing stack trace...");
            ioException.printStackTrace();
            System.out.println("returnPong(): Aborting...");
            System.exit(130);
        }
    }

    // entry point
    public static void main(String[] args) {
        try {
            if (args[0].equals("--ping")) {
                String hostAddress = args[1];
                int hostPort = Integer.parseInt(args[2]);
                sendPing(hostAddress, hostPort);
            } else if (args[0].equals("--pong")) {
                int port = Integer.parseInt(args[1]);
                returnPong(port);
            } else if (args[0].equals("--real-ping")) {
                // goes real!!
                Main.operating_mode = 0;
                Main.server_port = Integer.parseInt(args[2]);
                Main.server_address = args[1];
                RealPing.getRealPing();
                Table.startShow();
            } else if (args[0].equals("--real-pong")) {
                Main.operating_mode = 1;
                Main.this_port = Integer.parseInt(args[1]);
                RealPong.createPongServer();
                Table.startShow();
            } else if (args[0].equals("--test")) {
                System.out.println("haha... if anything?!");
            } else {
                // print usage
                System.out.println("Usage:\n--ping <server address> <server port>\n--pong <local port>\n" +
                        "-real-ping <server address> <server port>\n--real-pong <local port>\n--test\n");
            }
        } catch (ArrayIndexOutOfBoundsException aiobe) {
            // print usage
            System.out.println("Usage:\n--ping <server address> <server port>\n--pong <local port>\n" +
                    "-real-ping <server address> <server port>\n--real-pong <local port>\n--test\n");
        }
    }
}
