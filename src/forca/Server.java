import java.net.*;

import forca.MainThread;

import java.io.*;

public class Server {
    private ServerSocket ss;
    private Socket s;
    private MainThread c;
    public Server() {
        try {
            ss = new ServerSocket(5432);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                s = ss.accept();
                c = new MainThread(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        Server jogo = new Server();
    }
}
