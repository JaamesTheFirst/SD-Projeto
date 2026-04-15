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

    public void inicioJogo(ArrayList<Player> jogadores, String palavra) {
        this.ronda = 1;
        this.palavra = palavra;
        this.jogadores = jogadores;
        this.jogadoresAtivos = jogadores.size(); 
        this.tentativasRestantes = 6;            
        this.letrasUsadas = new ArrayList<>();
        this.jogadoresVencedores = new ArrayList<>();

        this.palavraEscondida = new char[palavra.length()];
        for (int i = 0; i < palavra.length(); i++) {
            this.palavraEscondida[i] = '_';
        }
        
        String msgStart = Protocolo.getStart(getMascaraString(), tentativasRestantes, 20000);
        enviarParaTodos(msgStart);
    } 
    
    public synchronized void processarJogada(int jogadorID, String tentativa) {
   
        jogadasNestaronda++;
        boolean contribuiu = false;

        if (tentativa.length() == 1) {
            char letra = tentativa.charAt(0);
            if (!letrasUsadas.contains(letra)) {
                letrasUsadas.add(letra);
                for (int i = 0; i < palavra.length(); i++) {
                    if (palavra.charAt(i) == letra) {
                        palavraEscondida[i] = letra;
                        contribuiu = true;
                    }
                }
            }
        } 
        else {
            if (tentativa.equals(palavra)) {
                palavraEscondida = palavra.toCharArray();
                contribuiu = true;
                jogadoresVencedores.add(getPlayerByID(jogadorID)); 
            }
        }

        
        if (!contribuiu) {
            tentativasRestantes--;
        }

        if (jogadasNestaronda == jogadoresAtivos) {
            if (getMascaraString().equals(palavra)) {
                if(jogadoresVencedores.isEmpty()) {
                    jogadoresVencedores.addAll(jogadores);
                }
                String msgWin = Protocolo.getEndWin(jogadoresVencedores, palavra);
                enviarParaTodos(msgWin);

            } else if (tentativasRestantes <= 0) {
                String msgLose = Protocolo.getEndLose(palavra);
                enviarParaTodos(msgLose);

            } else {
                ronda++;
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
