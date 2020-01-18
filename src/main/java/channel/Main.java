package channel;


/**
 * pas correct : loupe le premier lorsque write arrive avant le read
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        Channel<Byte> channel = new Channel<>();
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
//                channel.open(); pas une bonne solution car blocage si on a read avant le open()
                for (int i = 0; i < 1000000; i++) {
                    channel.write((byte) i);
                }
                channel.close();
            }
        });
        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(true){
                        Byte read = channel.read();
                        //System.out.println(read);
                    }
                } catch (EndOfStream e){
                    System.out.println("Fin !");
                }
            }
        });

        thread1.start();
        //Thread.sleep(1000); // on fait le write avant le read et du coup on loup le premier
        thread2.start();
    }
}
