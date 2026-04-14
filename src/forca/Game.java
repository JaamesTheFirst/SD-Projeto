import java.net.*;
import java.io.*;
import java.util.*;

public class Game {
    private int ronda;
    private String palavra;
    private char[] palavraEscondida;
    private ArrayList<Character> letrasUsadas;
    private ArrayList<Player> jogadoresVencedores;
    private ArrayList<Player> jogadores;

    public void inicioJogo(ArrayList<Player> jogadores, String palavra) {
        this.ronda = 0;
        this.palavra = palavra;
        this.jogadores = jogadores;
        this.letrasUsadas = new ArrayList<>();
        this.jogadoresVencedores = new ArrayList<>();

        this.palavraEscondida = new char[palavra.length()];
        for (int i = 0; i < palavra.length(); i++) {
            this.palavraEscondida[i] = '_';
        }
        
        


    } 

    private String getMascaraString() {
            return new String(palavraEscondida);
        }

}