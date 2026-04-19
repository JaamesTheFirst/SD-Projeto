import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
    private static final int PORT = 5432;
    private static final int MAX_PLAYERS = 4;
    private static final int LOBBY_TIMEOUT_MS = 20000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println();
            System.out.println("  =============================");
            System.out.println("   JOGO DA FORCA - Servidor");
            System.out.println("  =============================");
            System.out.println("  1. Iniciar servidor");
            System.out.println("  2. Sair");
            System.out.print("  Opção: ");
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1":
                    iniciarServidor();
                    break;
                case "2":
                    System.out.println("  A encerrar.");
                    scanner.close();
                    return;
                default:
                    System.out.println("  Opção inválida.");
            }
        }
    }

    private static void iniciarServidor() {
        ArrayList<Player> jogadores = new ArrayList<>();

        try (ServerSocket ss = new ServerSocket(PORT)) {
            System.out.println("\n  Servidor iniciado na porta " + PORT + ".");
            System.out.println("  À espera de jogadores (mín. 2, máx. " + MAX_PLAYERS + ")...\n");

            // ---- Fase de lobby ----
            try {
                while (jogadores.size() < MAX_PLAYERS) {
                    Socket s = ss.accept();
                    int id = jogadores.size() + 1;
                    MainThread t = new MainThread(s, id, id);
                    jogadores.add(new Player(id, t));
                    System.out.println("  Jogador " + id + " entrou.");
                    if (jogadores.size() == 1) {
                        System.out.println("  Primeiro jogador entrou. Lobby vai esperar " + (LOBBY_TIMEOUT_MS / 1000) + "s por mais jogadores...");
                        ss.setSoTimeout(LOBBY_TIMEOUT_MS);
                    }
                }
            } catch (SocketTimeoutException e) {
                System.out.println("  Tempo de lobby esgotado.");
            }

            if (jogadores.size() < 2) {
                System.out.println("  Jogadores insuficientes. A encerrar servidor.");
                for (Player p : jogadores) {
                    p.getThreadDeComunicacao().interrupt();
                    p.getThreadDeComunicacao().fecharSocket();
                }
                return;
            }

            System.out.println("\n  Jogo a começar com " + jogadores.size() + " jogadores!");

            // Rejeitar novas ligações com mensagem FULL
            ss.setSoTimeout(0);
            Thread rejectThread = new Thread() {
                public void run() {
                    try {
                        while (true) {
                            Socket atrasado = ss.accept();
                            PrintWriter pw = new PrintWriter(atrasado.getOutputStream(), true);
                            pw.println(Protocolo.getFull());
                            atrasado.close();
                        }
                    } catch (IOException e) {
                        // ServerSocket fechado, sair
                    }
                }
            };
            rejectThread.setDaemon(true);
            rejectThread.start();

            // ---- Iniciar jogo ----
            Game jogo = new Game();
            String palavraSorteada = Ler.sortearPalavra();
            jogo.inicioJogo(jogadores, palavraSorteada);
            System.out.println("  Palavra sorteada: " + palavraSorteada);

            // Libertar as threads dos jogadores
            for (Player p : jogadores) {
                p.getThreadDeComunicacao().setJogo(jogo);
            }

            // Esperar que todas as threads terminem
            for (Player p : jogadores) {
                try {
                    p.getThreadDeComunicacao().join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("\n  Jogo terminado.");

        } catch (IOException e) {
            System.out.println("  Erro no servidor: " + e.getMessage());
        }
    }
}
