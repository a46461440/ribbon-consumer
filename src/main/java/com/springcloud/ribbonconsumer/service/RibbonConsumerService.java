package com.springcloud.ribbonconsumer.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.ObservableExecutionMode;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheKey;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheRemove;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheResult;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.netflix.ribbon.proxy.annotation.Hystrix;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rx.Observable;
import rx.Subscriber;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * 为测试不符合MVC规范
 */
@Service
public class RibbonConsumerService {

    private Log log = LogFactory.getLog(this.getClass());

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 同步的方式 execute
     *
     * 默认抛出错误都会进入fallback，除了HystrixBadRequestException以外
     * ignoreExceptions会将大括号里面的class值都设置为直接抛出异常而不会进入fallback逻辑
     *
     * groupKey、commandKey和threadPoolKey是对该方法使用线程池的定位
     * 最精确的是threadPoolKey，通常我们可以设置threadPoolKey
     *
     */
//    @CacheResult(cacheKeyMethod = "getCacheKey")
    @CacheResult
    @HystrixCommand(fallbackMethod = "sendConsumerErrorMethod", ignoreExceptions = {RuntimeException.class, Exception.class}
        ,groupKey = "group_key", commandKey = "commandKey", threadPoolKey = "threadPoolKey")
    public String sendConsumerForEntity(Map map, @CacheKey long id) {
//        throw new HystrixBadRequestException("直接抛出，不进入fallback逻辑");
//        throw new RuntimeException("配置了ignoreException，不进入fallback逻辑");
        return this.normalMethod(map);
    }

//    @CacheRemove(commandKey = "commandKey", cacheKeyMethod = "getCacheKey")
    @CacheRemove(commandKey = "commandKey")
    public String removeKey(Map map, @CacheKey long id) {
        return "OK";
    }

    private String getCacheKey(Map map) {
        return "cacheKey";
    }

    /**
     * 异步的方式 queue
     * @param map
     * @return
     */
    @HystrixCommand(fallbackMethod = "sendConsumerErrorMethod")
    public Future<String> sendConsumerForEntity2(Map map) {
        return new AsyncResult<String>() {
            @Override
            public String invoke() {
                return normalMethod(map);
            }
        };
    }

    /**
     * 通过发布订阅的模式 HotObserver
     * @param map
     * @return
     */
    @HystrixCommand(fallbackMethod = "sendConsumerErrorMethod", observableExecutionMode = ObservableExecutionMode.EAGER)
    public Observable<String> sendConsumerForEntity3(Map map) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        String result = normalMethod(map);
                        subscriber.onNext(result);
                        subscriber.onCompleted();
                    }
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * 通过发布订阅的模式 ColdObserver
     * @param map
     * @return
     */
    @HystrixCommand(fallbackMethod = "sendConsumerErrorMethod", observableExecutionMode = ObservableExecutionMode.LAZY)
    public Observable<String> sendConsumerForEntity4(Map map) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    if (!subscriber.isUnsubscribed()) {
                        String result = normalMethod(map);
                        subscriber.onNext(result);
                        subscriber.onCompleted();
                    }
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private String normalMethod(Map map) {
        ResponseEntity<String> entity = this.restTemplate.getForEntity("http://HELLO-PROVIDER/send/{what}", String.class, map);
        HttpStatus status = entity.getStatusCode();
        int statusValue = entity.getStatusCodeValue();
        HttpHeaders headers = entity.getHeaders();
        this.log.info(statusValue + status.toString());
        for (Object o : headers.toSingleValueMap().entrySet()) {
            if (o instanceof Map.Entry)
                this.log.info(((Map.Entry) o).getKey() + ":" + ((Map.Entry) o).getValue());
        }
        this.log.info("normal方法执行");
        return entity.getBody();
    }

    /**
     * 嵌套降级
     * @param map
     * @return
     */
    @HystrixCommand(fallbackMethod = "connotDoAnything")
    public String sendConsumerErrorMethod(Map map, Throwable e) {
        this.log.info(e);
        this.log.info(map);
        return "降级一次";
    }

    public String connotDoAnything(Map map) {
        return "降级两次";
    }

}
