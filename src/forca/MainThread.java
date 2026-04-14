package forca;
import java.net.*;
import java.io.*;

public class MainThread extends MainThread {
    private Socket S;
    public MainThread ( Socket s) {
        super();
        S = s;
        start();
    }
    public void run () {
        try {
            ObjectOutputStream os = new ObjectOutputStream( S.getOutputStream());
            os.writeObject ("A data e hora do sistema: " + new java.util.Date());
            os.flush();
        }catch ( IOException e) {
            e.printStackTrace();
        }
    }
}