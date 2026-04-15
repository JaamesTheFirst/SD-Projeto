import java.net.*;
import java.io.*;

public class MainThread extends Thread {
    private Socket socket;
    private int idJogador;
    private PrintWriter out;
    private BufferedReader in;
    private Game jogo; 
    
    public MainThread (Socket s, int id) {
        super();
        this.socket = s;
        this.idJogador = id;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    } 
    
    public void setJogo(Game jogo) {
        this.jogo = jogo;
    }

    public void enviarMensagem(String msg) {
        if (out != null) out.println(msg);
    }

    @Override
    public void run() {
        try {
            enviarMensagem(Protocolo.getWelcome(idJogador, 4));

            String mensagemCliente;
            while ((mensagemCliente = in.readLine()) != null) {
                
                String[] partes = mensagemCliente.trim().split("\\s+");
                
                if (partes.length >= 2) {
                    
                    if (partes[0].equals(Protocolo.guess) && jogo != null) {
                        String tentativa = partes[1].toUpperCase(); 
                        
                        jogo.processarJogada(this.idJogador, tentativa); 
                    }
                } else {
                    System.out.println("Comando inválido recebido do jogador " + idJogador);
                }
            }
        } catch (IOException e) {
            System.out.println("Jogador " + idJogador + " desconectou-se.");
        }
    }
}
