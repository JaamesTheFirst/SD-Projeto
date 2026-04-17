import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Cliente {

    private static final Object PRINT_LOCK = new Object();
    private static volatile int meuId = -1;
    private static volatile boolean jogoTerminado = false;

    private static void printSeguro(String s) {
        synchronized (PRINT_LOCK) {
            System.out.print(s);
            System.out.flush();
        }
    }

    private static void printlnSeguro(String s) {
        synchronized (PRINT_LOCK) {
            System.out.println(s);
        }
    }

    private static void mostrarPainel(String mascara, int tentativasRestantes, String rondaInfo, String letrasUsadas) {
        synchronized (PRINT_LOCK) {
            ForcaVisual.limparEcra();
            System.out.println();
            System.out.println("  ===  J O G O   D A   F O R C A  ===");
            if (rondaInfo != null && !rondaInfo.isEmpty()) {
                System.out.println("  " + rondaInfo);
            }
            System.out.print(ForcaVisual.desenhar(tentativasRestantes));
            System.out.println("  Tentativas restantes: " + tentativasRestantes);
            System.out.println("  Palavra:  " + ForcaVisual.mascaraLegivel(mascara));
            if (letrasUsadas != null && !letrasUsadas.isBlank()) {
                System.out.println("  Letras já usadas: " + letrasUsadas.trim());
            }
            System.out.println();
            System.out.print("  Escreva a sua tentativa (letra ou palavra): ");
            System.out.flush();
        }
    }

    private static void mostrarFim(boolean ganhou, String palavra, String vencedoresInfo) {
        synchronized (PRINT_LOCK) {
            ForcaVisual.limparEcra();
            System.out.println();
            if (ganhou) {
                System.out.println("  *** Parabéns! Vitória! ***");
                if (vencedoresInfo != null && !vencedoresInfo.isEmpty()) {
                    System.out.println("  Vencedor(es): jogador(es) " + vencedoresInfo);
                }
            } else {
                System.out.println("  *** Fim de jogo — esgotaram-se as tentativas. ***");
                System.out.print(ForcaVisual.desenhar(0));
            }
            System.out.println("  A palavra era: " + palavra);
            System.out.println();
        }
    }

    private static void processarMensagemServidor(String msg) {
        String t = msg.trim();
        if (t.isEmpty()) {
            return;
        }

        if (t.startsWith(Protocolo.welcome + " ")) {
            String[] p = t.split("\\s+");
            if (p.length >= 3) {
                meuId = Integer.parseInt(p[1]);
                printlnSeguro("\n  Ligado! És o jogador " + p[1] + " (" + p[2] + " jogadores ligados).");
                printlnSeguro("  À espera que o jogo comece...");
            } else {
                printlnSeguro("\n[SERVIDOR]: " + t);
            }
            return;
        }

        if (t.startsWith(Protocolo.start + " ")) {
            String[] p = t.split("\\s+");
            if (p.length >= 4) {
                String mascara = p[1];
                int tentativas = Integer.parseInt(p[2]);
                mostrarPainel(mascara, tentativas, "Ronda 1", "");
            } else {
                printlnSeguro("\n[SERVIDOR]: " + t);
            }
            return;
        }

        if (t.startsWith(Protocolo.round + " ")) {
            String[] p = t.split(" ", 5);
            if (p.length >= 4) {
                String nrRonda = p[1];
                String mascara = p[2];
                int tentativas = Integer.parseInt(p[3]);
                String letras = p.length == 5 ? p[4] : "";
                mostrarPainel(mascara, tentativas, "Ronda " + nrRonda, letras);
            } else {
                printlnSeguro("\n[SERVIDOR]: " + t);
            }
            return;
        }

        if (t.startsWith(Protocolo.end_win + " ")) {
            jogoTerminado = true;
            String rest = t.substring((Protocolo.end_win + " ").length()).trim();
            String[] parts = rest.split("\\s+", 2);
            String vencedorIds = parts[0];
            String palavra = parts.length > 1 ? parts[1] : "";

            boolean euGanhei = false;
            for (String wid : vencedorIds.split(",")) {
                try {
                    if (Integer.parseInt(wid.trim()) == meuId) {
                        euGanhei = true;
                        break;
                    }
                } catch (NumberFormatException ignored) {}
            }

            if (euGanhei) {
                mostrarFim(true, palavra, vencedorIds);
            } else {
                synchronized (PRINT_LOCK) {
                    ForcaVisual.limparEcra();
                    System.out.println();
                    System.out.println("  O jogo terminou — jogador(es) " + vencedorIds + " ganharam!");
                    System.out.println("  A palavra era: " + palavra);
                    System.out.println();
                }
            }
            return;
        }

        if (t.startsWith(Protocolo.end_lose + " ")) {
            jogoTerminado = true;
            String palavra = t.substring((Protocolo.end_lose + " ").length()).trim();
            mostrarFim(false, palavra, null);
            return;
        }

        if (t.equals(Protocolo.full)) {
            jogoTerminado = true;
            printlnSeguro("\n  O servidor está cheio. Tente novamente mais tarde.");
            return;
        }

        printlnSeguro("\n[SERVIDOR]: " + t);
    }

    private static void jogar(Scanner teclado) {
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", 5432);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            final Socket socketRef = socket;

            jogoTerminado = false;
            meuId = -1;

            printlnSeguro("  Ligado ao servidor. À espera do jogo...");

            Thread ouvinte = new Thread(() -> {
                try {
                    String linha;
                    while ((linha = in.readLine()) != null) {
                        processarMensagemServidor(linha);
                    }
                } catch (IOException e) {
                    if (!jogoTerminado) {
                        printlnSeguro("\n  Ligação ao servidor perdida.");
                    }
                }
                jogoTerminado = true;
            });
            ouvinte.setDaemon(true);
            ouvinte.start();

            while (!jogoTerminado) {
                if (!teclado.hasNextLine()) break;
                String input = teclado.nextLine().trim();
                if (input.equalsIgnoreCase("sair")) {
                    break;
                }
                if (!input.isEmpty() && !jogoTerminado) {
                    out.println(Protocolo.getGuess(input));
                }
            }

        } catch (ConnectException e) {
            printlnSeguro("\n  Não foi possível ligar ao servidor. Verifique se o servidor está ativo.");
        } catch (IOException e) {
            printlnSeguro("\n  Erro de ligação: " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                try { socket.close(); } catch (IOException ignored) {}
            }
        }
    }

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);

        while (true) {
            System.out.println();
            System.out.println("  =============================");
            System.out.println("   JOGO DA FORCA - Cliente");
            System.out.println("  =============================");
            System.out.println("  1. Entrar no jogo");
            System.out.println("  2. Sair");
            System.out.print("  Opção: ");
            String opcao = teclado.nextLine().trim();

            switch (opcao) {
                case "1":
                    jogar(teclado);
                    break;
                case "2":
                    System.out.println("  Até à próxima!");
                    teclado.close();
                    return;
                default:
                    System.out.println("  Opção inválida.");
            }
        }
    }
}
