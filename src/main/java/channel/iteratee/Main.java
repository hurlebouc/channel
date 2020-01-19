package channel.iteratee;

import java.util.concurrent.atomic.AtomicInteger;

import static channel.iteratee.Iteratee.*;

public class Main {

    static AtomicInteger entier = new AtomicInteger(0);

    public static void main(String[] args) {

        Iteratee<Object, Integer, Integer> generator = yield(() -> entier.incrementAndGet()).andThen(end(() -> entier.get())).repeatUntil((n) -> n > 1000000);

        Iteratee<Integer, Void, Integer> reader = get(Integer.class, Void.class).repeatUntil((Integer i) -> false);

        Iteratee<Object, Void, Integer> connect = generator.connect(reader);

        System.out.println(Iteratee.run(connect));
    }
}
