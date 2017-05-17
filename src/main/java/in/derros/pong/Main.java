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

    private static void sendPing(String addr, int port) {
        try {
            Socket sock = new Socket(addr, port);
            String s = "ping";
            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
            InputStreamReader in = new InputStreamReader(sock.getInputStream());
            BufferedReader buff = new BufferedReader (in);
            System.out.println("Writing message...");
            out.writeBytes(s);
            String response = buff.readLine();
            System.out.println("Response:");
            System.out.println(response);

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
            DataOutputStream out = new DataOutputStream(send.getOutputStream());
            out.writeBytes("pong");
        } catch (IOException ioException) {
            System.out.println("returnPong(): Server socket cannot be established. Printing stack trace...");
            ioException.printStackTrace();
            System.out.println("returnPong(): Aborting...");
            System.exit(130);
        }
    }

    // entry point
    public static void main(String[] args) {
        for (String arg : args) {
            System.out.println(arg);
        }
        if(args[0].equals("--ping")) {
            String hostAddress = args[1];
            int hostPort = Integer.parseInt(args[2]);
            sendPing(hostAddress, hostPort);
        } else if (args[0].equals("--pong")) {
            int port = Integer.parseInt(args[1]);
            returnPong(port);
        } else if (args[0].equals("--real-ping")) {
            // goes real!!
        } else if (args[0].equals("--real-pong")) {

        } else if (args[0].equals("--test")) {
            Table.startShow();
        } else {
            // print usage
            System.out.println("Usage:\n--ping <server address> <server port>\n--pong <local port>");
        }
    }
}
