package com.springcloud.ribbonconsumer.action;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCollapser;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.springcloud.ribbonconsumer.service.RibbonConsumerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@RestController
public class ConsumerAction {

    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RibbonConsumerService ribbonConsumerService;

    @RequestMapping(value = "/send/entity/{what}", method = RequestMethod.GET)
    public String sendConsumerForEntity(@PathVariable("what") String what) throws InterruptedException, ExecutionException {
        Map map = new HashMap();
        map.put("what", what);
        HystrixRequestContext.initializeContext();
        long startTime = System.nanoTime();
//        String result = this.ribbonConsumerService.sendConsumerForEntity(map, 1L);
//        String afterCacheResult = this.ribbonConsumerService.sendConsumerForEntity(map, 1L);
//        this.log.info("cache的结果为:{},之后的结果为{},加入合并器后耗时:{}", result, afterCacheResult, System.nanoTime() - startTime);
//        return result;
        Future future = this.ribbonConsumerService.sendConsumerForEntity2(map, 1L);
        Future future2 = this.ribbonConsumerService.sendConsumerForEntity2(map, 1L);
        this.log.info("cache的结果为:{},之后的结果为{},加入合并器后耗时:{}", future.get(), future2.get(), System.nanoTime() - startTime);
        return (String) future.get();
    }

    @RequestMapping(value = "/send/entity/remove/{what}", method = RequestMethod.GET)
    public String sendConsumerForEntityRemoveCache(@PathVariable("what") String what) {
        Map map = new HashMap();
        map.put("what", what);
        HystrixRequestContext.initializeContext();
        return this.ribbonConsumerService.removeKey(map, 1L);
    }

    @RequestMapping(value = "/send/object/{what}", method = RequestMethod.GET)
    public String sendConsumerForObject(@PathVariable("what") String what) {
        Map map = new HashMap();
        map.put("what", what);
        String result = this.restTemplate.getForObject("http://HELLO-PROVIDER/send/{what}", String.class, map);
        return result;
    }

    @RequestMapping(value = "/get/entity/user")
    public String getUser() {
        Map map = new HashMap();
        map.put("name", "zxc");
        map.put("age", 2);
        HttpEntity<String> entity = this.restTemplate.postForEntity("http://HELLO-PROVIDER/user", map, String.class);
        return entity.getBody();
    }

}
