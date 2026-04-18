public class Player {
    private int ID;
    private int tentativasRestantes;
    private MainThread threadDeComunicacao;

    public Player(int ID, MainThread thread) {
        this.ID = ID;
        this.threadDeComunicacao = thread;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
    public int getID() {
        return ID;
    }

    public void setTentativasRestantes(int tentativasRestantes) {
        this.tentativasRestantes = tentativasRestantes;
    }

    public int getTentativasRestantes() {
        return tentativasRestantes;
    }

    public void reduzirTentativas() {
        this.tentativasRestantes--;
    }

    public MainThread getThreadDeComunicacao() {
        return threadDeComunicacao;
    }
}
