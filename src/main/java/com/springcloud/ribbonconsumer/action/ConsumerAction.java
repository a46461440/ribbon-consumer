package com.springcloud.ribbonconsumer.action;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.springcloud.ribbonconsumer.service.RibbonConsumerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ConsumerAction {

    private Log log = LogFactory.getLog(this.getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RibbonConsumerService ribbonConsumerService;

    @RequestMapping(value = "/send/entity/{what}", method = RequestMethod.GET)
    public String sendConsumerForEntity(@PathVariable("what") String what) {
        Map map = new HashMap();
        map.put("what", what);
        HystrixRequestContext.initializeContext();
        String result = this.ribbonConsumerService.sendConsumerForEntity(map, 1L);
        return result;
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
