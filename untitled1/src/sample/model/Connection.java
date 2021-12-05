package sample.model;

import java.io.IOException;
import java.net.Socket;

// Handles the connection to flightGear.

public class Connection {
    private String ip;
    private int port;
    Socket socket = null;

    public Connection() {
        this.ip = "localhost";
        this.port = 5400;
    }

    public boolean Connect() {
        try {
            this.socket = new Socket(this.ip, this.port);
            System.out.println("Connection established!");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to establish connection");
            return false;
        }
    }
}
