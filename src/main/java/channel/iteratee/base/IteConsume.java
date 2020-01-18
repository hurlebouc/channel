package channel.iteratee.base;

import channel.iteratee.Couple;
import channel.iteratee.Iteratee;

import java.util.function.Function;

public abstract class IteConsume<I, O, R> implements Iteratee<I, O, R> {

    public abstract Iteratee<I, O, R> consume(I i);

    @Override
    public <II> Iteratee<II, O, R> backConnect(Iteratee<II, I, R> ite) {
        IteConsume<I, O, R> self = this;
        if (ite instanceof IteProduce){
            Iteratee<I, O, R> aval = self;
            Iteratee<II, I, R> amont = ite;
            while((aval instanceof IteConsume) && (amont instanceof IteProduce)){
                IteProduce<II, I, R> amontProd = (IteProduce<II, I, R>) amont;
                IteConsume<I, O, R> avalCons = (IteConsume<I, O, R>) aval;
                aval = avalCons.consume(amontProd.produce());
                amont = amontProd.next();
            }
            return aval.backConnect(amont);
//            return self.consume(iteProd.produce()).backConnect(iteProd.next());
        }
        if (ite instanceof IteConsume){
            IteConsume<II, I, R> iteCons = (IteConsume<II, I, R>) ite;
            return new IteConsume<II, O, R>(){
                @Override
                public Iteratee<II, O, R> consume(II ii) {
                    return self.backConnect(iteCons.consume(ii));
                }
            };
        }
        if (ite instanceof IteEnd){
            IteEnd<II, I, R> iteEnd = (IteEnd<II, I, R>) ite;
            return new IteEnd<II, O, R>(){
                @Override
                public R end() {
                    return iteEnd.end();
                }
            };
        }
        return null; // impossible
    }

    @Override
    public <RR> Iteratee<I, O, RR> bind(Function<R, Iteratee<I, O, RR>> f) {
        IteConsume<I, O, R> self = this;
        return new IteConsume<I, O, RR>(){
            @Override
            public Iteratee<I, O, RR> consume(I i) {
                return self.consume(i).bind(f);
            }
        };
    }
}
