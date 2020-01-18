package channel.iteratee.base;

import channel.iteratee.Iteratee;

import java.util.function.Function;

public abstract class IteEnd<I, O, R> implements Iteratee<I, O, R> {

    public abstract R end();

    @Override
    public <II> Iteratee<II, O, R> backConnect(Iteratee<II, I, R> ite) {
        IteEnd<I, O, R> self = this;
        return new IteEnd<II, O, R>(){
            @Override
            public R end() {
                return self.end();
            }
        };
    }

    @Override
    public <RR> Iteratee<I, O, RR> bind(Function<R, Iteratee<I, O, RR>> f) {
        return f.apply(end());
    }
}
