package in.derros.pong;

/**
 * Created by derros on 5/16/17.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RealPong {
    private static RealPong pongServer = null;
    private ServerSocket serverSocket;
    private Socket thisSocket;
    private DataInputStream clientInputStream;
    private DataOutputStream serverOutputStream;

    private RealPong(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ioException) {
            System.out.println("RealPong(): socket creation error");
        }
    }

    public static RealPong createPongServer() {
        if (pongServer == null) {
            pongServer = new RealPong(Main.this_port);
            try {
                pongServer.createAndAccept();
            } catch (IOException e) {
                System.out.println("error accepting connection");
                e.printStackTrace();
                System.exit(130);
            }
            return pongServer;
        } else {
            return pongServer;
        }

    }

    private void createAndAccept() throws IOException {
        thisSocket = serverSocket.accept();
        clientInputStream = new DataInputStream(thisSocket.getInputStream());
        serverOutputStream = new DataOutputStream(thisSocket.getOutputStream());
    }

    public double getCompetitorXCoord() throws IOException {
        return this.clientInputStream.readDouble();
    }

    public void sendSelfXCoord(double x) throws IOException {
        this.serverOutputStream.writeDouble(x);
    }
}
