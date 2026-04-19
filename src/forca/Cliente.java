import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Cliente {

    private static int meuId = -1;
    private static boolean jogoTerminado = false;

    private static synchronized boolean isJogoTerminado() {
        return jogoTerminado;
    }

    private static synchronized void setJogoTerminado(boolean valor) {
        jogoTerminado = valor;
    }

    private static synchronized int getMeuId() {
        return meuId;
    }

    private static synchronized void setMeuId(int valor) {
        meuId = valor;
    }

    private static void mostrarPainel(String mascara, int tentativasRestantes, String rondaInfo, String letrasUsadas) {
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

    private static void mostrarFim(boolean ganhou, String palavra, String vencedoresInfo) {
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
        System.out.println("  Pressione ENTER para voltar ao menu principal.");
    }

    private static void processarMensagemServidor(String msg) {
        String t = msg.trim();
        if (t.isEmpty()) {
            return;
        }

        if (t.startsWith(Protocolo.welcome + " ")) {
            String[] p = t.split("\\s+");
            if (p.length >= 3) {
                setMeuId(Integer.parseInt(p[1]));
                System.out.println("\n  Ligado! És o jogador " + p[1] + " (" + p[2] + " jogadores ligados).");
                System.out.println("  À espera que o jogo comece...");
            } else {
                System.out.println("\n[SERVIDOR]: " + t);
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
                System.out.println("\n[SERVIDOR]: " + t);
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
                System.out.println("\n[SERVIDOR]: " + t);
            }
            return;
        }

        if (t.startsWith(Protocolo.end_win + " ")) {
            setJogoTerminado(true);
            String rest = t.substring((Protocolo.end_win + " ").length()).trim();
            String[] parts = rest.split("\\s+", 2);
            String vencedorIds = parts[0];
            String palavra = parts.length > 1 ? parts[1] : "";

            boolean euGanhei = false;
            for (String wid : vencedorIds.split(",")) {
                try {
                    if (Integer.parseInt(wid.trim()) == getMeuId()) {
                        euGanhei = true;
                        break;
                    }
                } catch (NumberFormatException ignored) {}
            }

            if (euGanhei) {
                mostrarFim(true, palavra, vencedorIds);
            } else {
                ForcaVisual.limparEcra();
                System.out.println();
                System.out.println("  O jogo terminou — jogador(es) " + vencedorIds + " ganharam!");
                System.out.println("  A palavra era: " + palavra);
                System.out.println();
            }
            return;
        }

        if (t.startsWith(Protocolo.end_lose + " ")) {
            setJogoTerminado(true);
            String palavra = t.substring((Protocolo.end_lose + " ").length()).trim();
            mostrarFim(false, palavra, null);
            return;
        }

        if (t.equals(Protocolo.full)) {
            setJogoTerminado(true);
            System.out.println("\n  O servidor está cheio. Tente novamente mais tarde.");
            return;
        }

        System.out.println("\n[SERVIDOR]: " + t);
    }

    private static class OuvinteThread extends Thread {
        private BufferedReader in;

        public OuvinteThread(BufferedReader in) {
            super();
            setDaemon(true);
            this.in = in;
        }

        public void run() {
            try {
                String linha;
                while ((linha = in.readLine()) != null) {
                    processarMensagemServidor(linha);
                }
            } catch (IOException e) {
                if (!isJogoTerminado()) {
                    System.out.println("\n  Ligação ao servidor perdida.");
                }
            }
            setJogoTerminado(true);
        }
    }

    private static void jogar(Scanner teclado) {
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", 5432);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            setJogoTerminado(false);
            setMeuId(-1);

            System.out.println("  Ligado ao servidor. À espera do jogo...");

            OuvinteThread ouvinte = new OuvinteThread(in);
            ouvinte.start();

            while (!isJogoTerminado()) {
                if (!teclado.hasNextLine()) break;
                String input = teclado.nextLine().trim();
                
                if (isJogoTerminado()) {
                    break;
                }
                
                if (input.equalsIgnoreCase("sair")) {
                    break;
                }
                if (!input.isEmpty()) {
                    out.println(Protocolo.getGuess(input));
                }
            }

        } catch (ConnectException e) {
            System.out.println("\n  Não foi possível ligar ao servidor. Verifique se o servidor está ativo.");
        } catch (IOException e) {
            System.out.println("\n  Erro de ligação: " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                try { 
                    socket.close(); 
                } catch (IOException ignored) {}
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
