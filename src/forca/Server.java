import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) {
        ArrayList<Player> jogadores = new ArrayList<>();
        
        try (ServerSocket ss = new ServerSocket(5432)) {
            System.out.println("Servidor iniciado. À espera de dois ou mais jogadores ...");
            try {
                while (jogadores.size() < 4) {
                    Socket s = ss.accept();
                    int id = jogadores.size() + 1;
                    MainThread t = new MainThread(s, id);
                    jogadores.add(new Player(id, t));
                    System.out.println("Jogador " + id + " entrou.");
                    if(jogadores.size() == 2) {
                    System.out.println("Agora que dois jogadores entraram, o lobby vai esperar mais 20 segundos por mais jogadores...");
                    ss.setSoTimeout(20000);
            }
                }
            } catch (SocketTimeoutException e) {
                System.out.println("Tempo de lobby esgotado (20s).");
            }

            if (jogadores.size() >= 2) {
                System.out.println("O Jogo vai começar com " + jogadores.size() + " jogadores!");
                
                Game jogo = new Game();
                for (Player p : jogadores) {
                    p.getThreadDeComunicacao().setJogo(jogo); 
                }

                String palavraSorteada = Ler.sortearPalavra();
                jogo.inicioJogo(jogadores, palavraSorteada);
                
            } else {
                System.out.println("Jogadores insuficientes. Tente novamente.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
