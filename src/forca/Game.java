import java.util.*;

public class Game {
    private int ronda;
    private int jogadasNestaronda;
    private int jogadoresAtivos;
    private int tentativasRestantes;
    private String palavra;
    private char[] palavraEscondida;
    private ArrayList<Character> letrasUsadas;
    private ArrayList<Player> jogadoresVencedores;
    private ArrayList<Player> jogadores;
    private volatile boolean gameOver = false;

    public boolean isGameOver() {
        return gameOver;
    }

    public void inicioJogo(ArrayList<Player> jogadores, String palavra) {
        this.ronda = 1;
        this.palavra = palavra.toUpperCase();
        this.jogadores = jogadores;
        this.jogadoresAtivos = jogadores.size();
        this.tentativasRestantes = 6;
        this.letrasUsadas = new ArrayList<>();
        this.jogadoresVencedores = new ArrayList<>();

        this.palavraEscondida = new char[this.palavra.length()];
        for (int i = 0; i < this.palavra.length(); i++) {
            this.palavraEscondida[i] = '_';
        }

        String msgStart = Protocolo.getStart(getMascaraString(), tentativasRestantes, 20000);
        enviarParaTodos(msgStart);
    }

    public synchronized void processarJogada(int jogadorID, String tentativa) {
        if (gameOver) {
            notifyAll();
            return;
        }

        jogadasNestaronda++;
        boolean contribuiu = false;

        if (tentativa.length() == 1) {
            char letra = Character.toUpperCase(tentativa.charAt(0));
            if (!letrasUsadas.contains(letra)) {
                letrasUsadas.add(letra);
                for (int i = 0; i < palavra.length(); i++) {
                    if (Character.toUpperCase(palavra.charAt(i)) == letra) {
                        palavraEscondida[i] = Character.toUpperCase(palavra.charAt(i));
                        contribuiu = true;
                    }
                }
            }
        } else if (tentativa.length() > 1) {
            if (tentativa.equalsIgnoreCase(palavra)) {
                palavraEscondida = palavra.toUpperCase().toCharArray();
                contribuiu = true;
                jogadoresVencedores.add(getPlayerByID(jogadorID));
            }
        }
        // tentativa vazia (timeout) → contribuiu fica false

        if (!contribuiu) {
            tentativasRestantes--;
        }

        if (jogadasNestaronda >= jogadoresAtivos) {
            if (getMascaraString().equalsIgnoreCase(palavra)) {
                if (jogadoresVencedores.isEmpty()) {
                    jogadoresVencedores.addAll(jogadores);
                }
                String msgWin = Protocolo.getEndWin(jogadoresVencedores, palavra);
                enviarParaTodos(msgWin);
                gameOver = true;

            } else if (tentativasRestantes <= 0) {
                String msgLose = Protocolo.getEndLose(palavra);
                enviarParaTodos(msgLose);
                gameOver = true;

            } else {
                ronda++;
                jogadoresVencedores.clear();
                String msgRound = Protocolo.getRound(ronda, getMascaraString(), tentativasRestantes, letrasUsadas);
                enviarParaTodos(msgRound);
            }

            jogadasNestaronda = 0;
            notifyAll();

        } else {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void jogadorDesconectado(int jogadorID) {
        if (gameOver) return;
        jogadoresAtivos--;
        System.out.println("Jogador " + jogadorID + " removido. Jogadores ativos: " + jogadoresAtivos);

        if (jogadoresAtivos < 1) {
            gameOver = true;
            notifyAll();
            return;
        }

        // Se já temos todas as jogadas esperadas, resolver a ronda
        if (jogadasNestaronda >= jogadoresAtivos) {
            if (getMascaraString().equalsIgnoreCase(palavra)) {
                if (jogadoresVencedores.isEmpty()) {
                    jogadoresVencedores.addAll(jogadores);
                }
                String msgWin = Protocolo.getEndWin(jogadoresVencedores, palavra);
                enviarParaTodos(msgWin);
                gameOver = true;
            } else if (tentativasRestantes <= 0) {
                String msgLose = Protocolo.getEndLose(palavra);
                enviarParaTodos(msgLose);
                gameOver = true;
            } else {
                ronda++;
                jogadoresVencedores.clear();
                String msgRound = Protocolo.getRound(ronda, getMascaraString(), tentativasRestantes, letrasUsadas);
                enviarParaTodos(msgRound);
            }
            jogadasNestaronda = 0;
            notifyAll();
        }
    }

    private String getMascaraString() {
        return new String(palavraEscondida);
    }

    private Player getPlayerByID(int id) {
        for (Player p : jogadores) {
            if (p.getID() == id) return p;
        }
        return null;
    }

    private void enviarParaTodos(String mensagem) {
        for (Player p : jogadores) {
            p.getThreadDeComunicacao().enviarMensagem(mensagem);
        }
    }
}
