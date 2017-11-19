package hku.cs.smp.guardian.common;

public class Counter {
    private int c;

    public Counter() {
        c = 0;
    }

    public synchronized void add() {
        c++;
        notifyAll();
    }

    public synchronized void done() {
        c--;
        notifyAll();
    }

    public synchronized void reset() {
        c = 0;
        notifyAll();
    }

    public synchronized void check() throws InterruptedException {
        while (true) {
            if (c != 0) {
                wait();
            } else break;
        }
    }

}
