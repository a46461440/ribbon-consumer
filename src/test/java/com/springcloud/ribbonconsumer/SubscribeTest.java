package com.springcloud.ribbonconsumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import rx.Observable;
import rx.Subscriber;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class SubscribeTest {

    private Log log = LogFactory.getLog(this.getClass());

    private volatile AtomicInteger version = new AtomicInteger(0);

    /**
     * 测试RxJava发布订阅
     */
    @Test
    public void testSubscribe() throws ExecutionException, InterruptedException, TimeoutException {
        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("observable subscribe version:" + version.getAndIncrement());
//                subscriber.onCompleted();
            }
        });
        Observable<String> observable2 = Observable.just("no");
        Subscriber<String> subscriberOne = new Subscriber<String>() {
            @Override
            public void onCompleted() {
                log.info("completed in " + version.get());
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onNext(String s) {
                log.info("subscriber get message:" + s);
            }
        };
        observable.subscribe(subscriberOne);
        observable2.subscribe(subscriberOne);
        Future future = observable.toBlocking().toFuture();
        log.info(future.get(2, TimeUnit.SECONDS));
    }

}
