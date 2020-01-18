package channel;

public class Channel <A> {
    private boolean closed = false;
    private A channel;

    public synchronized void write(A b){
        this.channel = b;
        notify();
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized A read() throws EndOfStream {
        notify();
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (closed){
            throw new EndOfStream();
        }
        return channel;
    }

    public synchronized void close() {
        closed = true;
        notify();
    }

    public synchronized void open() {
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
