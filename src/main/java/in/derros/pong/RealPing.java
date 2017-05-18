package in.derros.pong;

/**
 * Created by derros on 5/16/17.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class RealPing {

    private static RealPing pingClient = null;
    private Socket clientSocket;
    private DataInputStream clientInputStream;
    private DataOutputStream clientOutputStream;

    private RealPing(String host, int port) {
        try {
            clientSocket = new Socket(host, port);
            clientInputStream = new DataInputStream(clientSocket.getInputStream());
            clientOutputStream = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.out.println("realping socket creation error");
            System.exit(130);
        }
    }

    public synchronized static RealPing getRealPing() {
        if (pingClient == null) {
            System.out.println("Client: Connecting to Server...");
            pingClient = new RealPing(Main.server_address, Main.server_port);
            (new Thread(() -> {
                while(pingClient.isConnected()) {
                    try {
                        Double d = pingClient.getCompetitorXCoord();
                        synchronized(Volatiles.newestPingCompetitorLocation) {
                            Volatiles.newestPongCompetitorLocation = d;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            })).start();

        }
        return pingClient;
    }

    public synchronized double getCompetitorXCoord() throws IOException {
        return clientInputStream.readDouble();
    }

    public synchronized void sendSelfXCoord(double x) throws IOException {
        clientOutputStream.writeDouble(x);
    }

    public synchronized boolean isConnected() {
        return this.clientSocket.isConnected();
    }

    public void finalize() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
