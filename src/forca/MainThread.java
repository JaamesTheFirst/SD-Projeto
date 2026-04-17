import java.net.*;
import java.io.*;
import java.util.concurrent.CountDownLatch;

public class MainThread extends Thread {
    private Socket socket;
    private int idJogador;
    private int totalJogadores;
    private PrintWriter out;
    private BufferedReader in;
    private Game jogo;
    private final CountDownLatch jogoLatch = new CountDownLatch(1);
    private static final int ROUND_TIMEOUT_MS = 20000;

    public MainThread(Socket s, int id, int totalJogadores) {
        super();
        this.socket = s;
        this.idJogador = id;
        this.totalJogadores = totalJogadores;
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
        jogoLatch.countDown();
    }

    public void enviarMensagem(String msg) {
        if (out != null) out.println(msg);
    }

    public void fecharSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // ignorar
        }
    }

    @Override
    public void run() {
        try {
            enviarMensagem(Protocolo.getWelcome(idJogador, totalJogadores));

            // Esperar até o jogo começar
            jogoLatch.await();

            // Definir timeout por ronda
            socket.setSoTimeout(ROUND_TIMEOUT_MS);

            // Loop do jogo
            while (!jogo.isGameOver()) {
                try {
                    String mensagemCliente = in.readLine();
                    if (mensagemCliente == null) {
                        // Jogador desconectou-se
                        System.out.println("Jogador " + idJogador + " desconectou-se.");
                        if (!jogo.isGameOver()) {
                            jogo.jogadorDesconectado(idJogador);
                        }
                        break;
                    }

                    String[] partes = mensagemCliente.trim().split("\\s+");
                    if (partes.length >= 2 && partes[0].equals(Protocolo.guess)) {
                        String tentativa = partes[1].toUpperCase();
                        jogo.processarJogada(this.idJogador, tentativa);
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Jogador " + idJogador + " excedeu o tempo limite.");
                    if (!jogo.isGameOver()) {
                        jogo.processarJogada(this.idJogador, "");
                    }
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Jogador " + idJogador + " thread interrompida.");
        } catch (IOException e) {
            System.out.println("Jogador " + idJogador + " desconectou-se.");
            if (jogo != null && !jogo.isGameOver()) {
                jogo.jogadorDesconectado(idJogador);
            }
        } finally {
            fecharSocket();
        }
    }
}
