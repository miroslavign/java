package name.abhijitsarkar.java.rxjava;

import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * @author Abhijit Sarkar
 */
@Slf4j
public class YahooFinanceService {
    private final YahooFinanceRepository repo = new YahooFinanceRepository();

    public double netAsset1(SimpleImmutableEntry<String, Long> stock1, SimpleImmutableEntry<String, Long> stock2) {
        DoubleAdder adder = new DoubleAdder();

        Observable.just(stock1.getKey(), stock2.getKey())
                .flatMap(this::buildObservable)
                /* Without blocking, method exits before onNext is called
                   With blocking, onNext is called as soon as any price comes back
                 */
                .toBlocking()
                /* Executes concurrently */
                .forEach(p -> {
                    log.info("[netAsset1] Calculating net asset on thread: {}.", Thread.currentThread().getName());

                    adder.add(p);
                });

        return adder.doubleValue();
    }

    public double netAsset2(SimpleImmutableEntry<String, Long> stock1, SimpleImmutableEntry<String, Long> stock2) {
        DoubleAdder adder = new DoubleAdder();

        Observable.combineLatest(buildObservable(stock1.getKey()), buildObservable(stock2.getKey()),
                /* Executes only when both prices come back */
                (p1, p2) -> {
                    log.info("[netAsset2] Calculating net asset on thread: {}.", Thread.currentThread().getName());
                    adder.add(p1 + p2);
                    /* Doesn't matter */
                    return true;
                })
                /* Without blocking, method exits before onNext is called
                   With blocking, onNext is called when there's a combined item emitted
                 */
                .toBlocking()
                .subscribe(bool -> log.info("[netAsset2] Subscribing on thread: {}.", Thread.currentThread().getName()));

        return adder.doubleValue();
    }

    private Observable<Double> buildObservable(String stock) {
        return Observable.just(stock)
                .subscribeOn(Schedulers.computation())
                .map(repo::getPrice);
    }
}