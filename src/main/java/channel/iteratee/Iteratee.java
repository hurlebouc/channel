package channel.iteratee;

import channel.iteratee.base.IteConsume;
import channel.iteratee.base.IteEnd;
import channel.iteratee.base.IteProduce;

import java.util.Iterator;
import java.util.function.Function;

public interface Iteratee <I, O, R>{


    <II> Iteratee<II, O, R> backConnect(Iteratee<II, I, R> ite);
    <RR> Iteratee<I, O, RR> bind(Function<R, Iteratee<I, O, RR>> f);

    default <OO> Iteratee<I, OO, R> connect(Iteratee<O, OO, R> ite){
        return ite.backConnect(this);
    }

    default Iteratee<I, O, R> repeatUntil(Function<R, Boolean> condition){
        return this.bind((R r) -> condition.apply(r) ? new IteEnd<I, O, R>() {
            @Override
            public R end() {
                return r;
            }
        } : this.repeatUntil(condition));
    }

    default <RR> Iteratee<I, O, RR> andThen(Iteratee<I, O, RR> ite){
        return this.bind((r) -> ite);
    }

    public static <R> R run(Iteratee<Object, ?, R> ite){
        while (!(ite instanceof IteEnd)){
            if (ite instanceof IteProduce){
                IteProduce<Object, ?, R> iteProd = (IteProduce<Object, ?, R>) ite;
                ite = iteProd.next();
                continue;
            }
            if (ite instanceof IteConsume){
                IteConsume<Object, ?, R> iteCons = (IteConsume<Object, ?, R>) ite;
                ite = iteCons.consume(new Object());
                continue;
            }
        }
        IteEnd<Object, ?, R> iteEnd = (IteEnd<Object, ?, R>) ite;
        return iteEnd.end();
    }

    public static <I, O> Iteratee<I, O, Object> yield(Factory<O> o) {
        return new IteProduce<I, O, Object>() {
            @Override
            public O produce() {
                return o.make();
            }

            @Override
            public Iteratee<I, O, Object> next() {
                return new IteEnd<I, O, Object>() {
                    @Override
                    public Object end() {
                        return new Object();
                    }
                };
            }
        };
    }

    public static <I, O> Iteratee<I, O, Object> yield(Class<I> iClass, Factory<O> o){
        return yield(o);
    }

    public static <I, O, R> Iteratee<I, O, R> end (Factory<R> r) {
        return new IteEnd<I, O, R>() {
            @Override
            public R end() {
                return r.make();
            }
        };
    }

    public static <I, O, R> Iteratee<I, O, R> end (Class<I> iClass, Class<O> oClass, Factory<R> r) {
        return end(r);
    }

    public static <I, O> Iteratee<I, O, I> get() {
        return new IteConsume<I, O, I>() {
            @Override
            public Iteratee<I, O, I> consume(I i) {
                return new IteEnd<I, O, I>() {
                    @Override
                    public I end() {
                        return i;
                    }
                };
            }
        };
    }

    public static <I, O> Iteratee<I, O, I> get(Class<I> iClass, Class<O> oClass) {
        return get();
    }

    public static <I, O, R> Iteratee<I, O, R> iterate(Iteratee<I, O, R> iteInit, Function<R, Iteratee<I, O, R>> op, Function<R, Boolean> comp){
        return iteInit.bind(r -> comp.apply(r) ? end(() -> r) : iterate(op.apply(r), op, comp));
    }


    public static <I, O, R> Iteratee<I, O, R> iterate(Class<I> iClass, Class<O> oClass, Class<R> rClass, Iteratee<I, O, R> iteInit, Function<R, Iteratee<I, O, R>> op, Function<R, Boolean> comp){
        return iterate(iteInit, op, comp);
    }

    default public Iteratee<I, O, R> repeatBindUntil(Function<R, Iteratee<I, O, R>> op, Function<R, Boolean> comp){
        return this.bind(r -> comp.apply(r) ? end(() -> r) : op.apply(r).repeatBindUntil(op, comp));
    }

    static <O> Iteratee<Byte, O, Couple<String, Byte>> getAndCumulate(Couple<String, Byte> couple){
        Class<O> trc = null;
        return get(Byte.class, trc).bind((a) -> end(() -> new Couple(couple.a + (char) a.byteValue(), a)));
    }

    public static <O> Iteratee<Byte, O, String> readLine() {
        Iteratee<Byte, O, Couple<String, Byte>> init = end(() -> new Couple<>("", (byte) 0));
        Class<O> o = null;
        Class<Couple<String, Byte>> coupleType = null;
        return iterate(Byte.class,  o, coupleType, init, Iteratee::getAndCumulate, couple -> couple.b == 10).bind(couple -> end(() -> couple.a));
    }

    public static <I, O> Iteratee<I, O, Boolean> fromIterable(Iterable<O> iterable){
        Iterator<O> iterator = iterable.iterator();
        Class<I> iClass = null;
        return yield(iClass, () -> iterator.next()).andThen(end(() -> iterator.hasNext())).repeatUntil(b -> b);
    }
}
