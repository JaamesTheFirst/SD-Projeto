public class Player {
    private int ID;
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

    public MainThread getThreadDeComunicacao() {
        return threadDeComunicacao;
    }
}
