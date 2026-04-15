import java.util.*;
public class Protocolo {
    public static final String welcome = "WELCOME";
    public static final String start = "START";
    public static final String guess = "GUESS";
    public static final String round = "ROUND";
    public static final String state = "STATE";
    public static final String end_win = "END WIN";
    public static final String end_lose = "END LOSE";
    public static final String full = "FULL";

    public static String getWelcome(int id, int playersTotais) {
        return welcome + " " + id + " " + playersTotais;
    }

    public static String getStart(String palavraEscondida, int ronda, long timeout) {
        return start + " " + palavraEscondida + " " + ronda + " " + timeout;
    }

    public static String getGuess(String guessdoJogador) {
        return guess + " " + guessdoJogador;
    }

    public static String getRound(int ronda, String palavraEscondida, int tentativasRestantes, ArrayList<Character> letrasUsadas) {
        return round + " " + ronda + " " + palavraEscondida + " " + tentativasRestantes + " " + letrasUsadas.toString();
    }

    public static String getState(String palavraEscondida, int tentativasRestantes, ArrayList<Character> letrasUsadas) {
        return state + " " + palavraEscondida + " " + tentativasRestantes + " " + letrasUsadas.toString();
    }

    public static String getEndWin(ArrayList<Player> jogadoresVencedores, String palavra) {
        return end_win + " " + palavra;
    }

    public static String getEndLose(String palavra) {
        return end_lose + " " + palavra;
    }

    public static String getFull() {
        return full;
    }

}
