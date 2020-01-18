package channel.iteratee;

import channel.iteratee.base.IteConsume;
import channel.iteratee.base.IteEnd;
import channel.iteratee.base.IteProduce;

import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    static AtomicInteger entier = new AtomicInteger(0);

    public static void main(String[] args) {


        Iteratee<Object, Integer, Integer> generator = new IteProduce<Object, Integer, Integer>() {
            @Override
            public Integer produce() {
                return entier.incrementAndGet();
            }

            @Override
            public Iteratee<Object, Integer, Integer> next() {
                return new IteEnd<Object, Integer, Integer>() {
                    @Override
                    public Integer end() {
                        return entier.get();
                    }
                };
            }
        }.repeatUntil((i) -> i > 1000000);

        Iteratee<Object, Integer, Integer> generator2 = Iteratee.yield(() -> entier.incrementAndGet()).andThen(Iteratee.end(() -> entier.get())).repeatUntil((n) -> n > 1000000);

        Iteratee<Integer, Void, Integer> reader = new IteConsume<Integer, Void, Integer>() {
            @Override
            public Iteratee<Integer, Void, Integer> consume(Integer i){
                //System.out.println(i);
                return new IteEnd<Integer, Void, Integer>() {
                    @Override
                    public Integer end() {
                        return 0;
                    }
                };
            }
        }.repeatUntil((n) -> false);

        Iteratee<Object, Void, Integer> connect = generator2.connect(reader);

        System.out.println(Iteratee.run(connect));
    }
}
