import java.io.*;
import java.net.*;

public class Cliente {
    private Socket s;
    public Cliente(){
        int aposta = 0;
        try {
            s = new Socket ("127.0.0.1", 5432);
            ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream is = new ObjectInputStream(s.getInputStream() );
            System.out.println("Qual a sua aposta?");
            aposta = Ler.umInt();
            os.writeObject(aposta);
            os.flush();
            s.close();
        }catch (IOException e){ 
            System.out.println(e.getMessage());
        }
   } //construtor
    public static void main (String args []){
        Cliente c = new Cliente();
    }
}
