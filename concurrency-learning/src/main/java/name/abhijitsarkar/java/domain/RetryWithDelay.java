package name.abhijitsarkar.java.domain;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;
import rx.functions.Func1;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Abhijit Sarkar
 */
@Slf4j
public class RetryWithDelay implements
        Func1<Observable<? extends Throwable>, Observable<?>> {

    private final int maxRetries;
    private final long retryDelaySeconds;
    private final RetryDelayStrategy retryDelayStrategy;

    private int retryCount;

    /* Use Builder on constructor to avoid field retryCount being included. */
    @Builder
    private RetryWithDelay(int maxRetries, long retryDelaySeconds, RetryDelayStrategy retryDelayStrategy) {
        this.maxRetries = maxRetries;
        this.retryDelaySeconds = retryDelaySeconds;
        this.retryDelayStrategy = retryDelayStrategy;
    }

    @Override
    public Observable<?> call(Observable<? extends Throwable> attempts) {
        return attempts
                .concatMap(new Func1<Throwable, Observable<?>>() {
                    @Override
                    public Observable<?> call(Throwable throwable) {
                        if (++retryCount <= maxRetries) {
                            /* When this Observable calls onNext, the original
                             * Observable will be retried (i.e. resubscribed).
                             */
                            long delaySeconds = delaySeconds();

                            log.debug("Retrying...attempt #{} in {} second(s).", retryCount, delaySeconds);
                            return Observable.timer(delaySeconds, SECONDS);
                        }

                        /* Max retries hit. Just pass the error along. */
                        log.warn("Exhausted all retries: {}.", maxRetries);
                        return Observable.error(throwable);
                    }
                });
    }

    private long delaySeconds() {
        requireNonNull(retryDelayStrategy, "RetryDelayStrategy must not be null.");

        switch (retryDelayStrategy) {
            case CONSTANT_DELAY:
                return retryDelaySeconds;
            case RETRY_COUNT:
                return retryCount;
            case CONSTANT_DELAY_TIMES_RETRY_COUNT:
                return retryDelaySeconds * retryCount;
            case CONSTANT_DELAY_RAISED_TO_RETRY_COUNT:
                return (long) Math.pow(retryDelaySeconds, retryCount);
            default:
                return 0;
        }
    }
}
