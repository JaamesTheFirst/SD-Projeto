import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Cliente {
    public static void main (String args []){
        try {
            Socket socket = new Socket("127.0.0.1", 5432); 
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner teclado = new Scanner(System.in);

            System.out.println("Ligado ao Servidor! À espera do jogo...");
            
            Thread ouvinte = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        System.out.println("\n[SERVIDOR]: " + msg);
                        System.out.print("Escreva a sua tentativa: "); 
                    }
                } catch (IOException e) {
                    System.out.println("O servidor foi desligado.");
                }
            });
            ouvinte.start();
            
            while (true) {
                String input = teclado.nextLine();
                if (input.equalsIgnoreCase("sair")) {
                    break;
                }
                
                out.println(Protocolo.getGuess(input));
            }

            socket.close();
            teclado.close();

        } catch (IOException e){ 
            System.out.println(e.getMessage());
        } 
    }
}
