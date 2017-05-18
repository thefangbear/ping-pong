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

    public synchronized static RealPong createPongServer() {
        if (pongServer == null) {
            pongServer = new RealPong(Main.this_port);
            try {
                System.out.println("Server: Waiting for connection...");
                pongServer.createAndAccept();
            } catch (IOException e) {
                System.out.println("error accepting connection");
                e.printStackTrace();
                System.exit(130);
            }
            // now let's run a thread
            (new Thread(() -> {
                while(pongServer.isConnected()) {
                    try {
                        Double d = pongServer.getCompetitorXCoord();
                        synchronized(Volatiles.newestPongCompetitorLocation) {
                            Volatiles.newestPongCompetitorLocation = d;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            })).start();
            return pongServer;
        } else {
            return pongServer;
        }

    }

    public synchronized boolean isConnected() {
        return this.thisSocket.isConnected();
    }

    private void createAndAccept() throws IOException {
        thisSocket = serverSocket.accept();
        clientInputStream = new DataInputStream(thisSocket.getInputStream());
        serverOutputStream = new DataOutputStream(thisSocket.getOutputStream());
    }

    public synchronized double getCompetitorXCoord() throws IOException {
        return this.clientInputStream.readDouble();
    }

    public synchronized void sendSelfXCoord(double x) throws IOException {
        this.serverOutputStream.writeDouble(x);
    }

    public void finalize() {
        try {
            thisSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
