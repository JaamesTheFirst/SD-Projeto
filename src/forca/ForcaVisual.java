/**
 * Desenho ASCII da forca no terminal. {@code tentativasRestantes} vai de 6 (nenhum erro) a 0 (fim).
 */
public final class ForcaVisual {
    private static final int MAX_TENTATIVAS = 6;

    private ForcaVisual() {}

    public static String desenhar(int tentativasRestantes) {
        int t = Math.max(0, Math.min(MAX_TENTATIVAS, tentativasRestantes));
        int erros = MAX_TENTATIVAS - t;

        String head = erros >= 1 ? "  O  " : "     ";
        String tronco = erros >= 2 ? "  |  " : "     ";

        String bracos;
        if (erros < 3) {
            bracos = "     ";
        } else if (erros == 3) {
            bracos = " /|  ";
        } else {
            bracos = " /|\\ ";
        }

        String pernas;
        if (erros < 5) {
            pernas = "     ";
        } else if (erros == 5) {
            pernas = " /   ";
        } else {
            pernas = " / \\ ";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("    +---+\n");
        sb.append("    |   |\n");
        sb.append("    ").append(head).append("\n");
        sb.append("    ").append(tronco).append("\n");
        sb.append("    ").append(bracos).append("\n");
        sb.append("    ").append(pernas).append("\n");
        sb.append("        |\n");
        sb.append("  =========\n");
        return sb.toString();
    }

    /** Formata a palavra com espaços: {@code A_B_C} */
    public static String mascaraLegivel(String mascara) {
        if (mascara == null || mascara.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mascara.length(); i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(mascara.charAt(i));
        }
        return sb.toString();
    }

    public static boolean usarLimparEcra() {
        String v = System.getenv("FORCA_CLEAR");
        if (v == null) {
            return System.console() != null;
        }
        return !"0".equals(v) && !"false".equalsIgnoreCase(v);
    }

    public static void limparEcra() {
        if (usarLimparEcra()) {
            System.out.print("\033[2J\033[H");
            System.out.flush();
        }
    }
}
