package com.springcloud.ribbonconsumer.action;

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

    @Autowired
    private RestTemplate restTemplate;

    private Log log = LogFactory.getLog(this.getClass());

    @RequestMapping(value = "/send/entity/{what}", method = RequestMethod.GET)
    public String sendConsumerForEntity(@PathVariable("what") String what) {
        Map map = new HashMap();
        map.put("what", what);
        ResponseEntity<String> entity = this.restTemplate.getForEntity("http://HELLO-PROVIDER/send/{what}", String.class, map);
        HttpStatus status = entity.getStatusCode();
        int statusValue = entity.getStatusCodeValue();
        HttpHeaders headers = entity.getHeaders();
        this.log.info(statusValue + status.toString());
        for (Object o : headers.toSingleValueMap().entrySet()) {
            if (o instanceof Map.Entry)
                this.log.info(((Map.Entry) o).getKey() + ":" + ((Map.Entry) o).getValue());
        }
        return entity.getBody();
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
