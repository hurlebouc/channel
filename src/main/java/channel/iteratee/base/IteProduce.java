package channel.iteratee.base;

import channel.iteratee.Factory;
import channel.iteratee.Iteratee;

import java.util.function.Function;

public abstract class IteProduce <I, O, R> implements Iteratee<I, O, R> {
//    final O o;
//    final Iteratee<I, O, R> next;
//
//    public IteProduce(O o, Factory<Iteratee<I, O, R>> next) {
//        this.o = o;
//        this.next = next.make();
//    }

    public abstract O produce(); // on peut envisager de faire un efffet en produisant...
    public abstract Iteratee<I, O, R> next(); // mais j'ai quand même besoin de cette suspension pour les calculs récursifs


    @Override
    public <II> Iteratee<II, O, R> backConnect(Iteratee<II, I, R> ite) {
        IteProduce<I, O, R> self = this;
        return new IteProduce<II, O, R>(){
            @Override
            public O produce() {
                return self.produce();
            }

            @Override
            public Iteratee<II, O, R> next() {
                return self.next().backConnect(ite);
            }
        };
//        return new IteProduce<>(o, () -> next.backConnect(ite));
    }

    @Override
    public <RR> Iteratee<I, O, RR> bind(Function<R, Iteratee<I, O, RR>> f) {
        IteProduce<I, O, R> self = this;
        return new IteProduce<I, O, RR>(){
            @Override
            public O produce() {
                return self.produce();
            }

            @Override
            public Iteratee<I, O, RR> next() {
                return self.next().bind(f);
            }
        };
//        return new IteProduce<>(o, () -> next.bind(f));
    }
}
