package com.springcloud.ribbonconsumer;

import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class RedisTest {

    private Jedis jedis;

    @Before
    public void setRedis() {
        JedisPool jedisPool = new JedisPool("192.168.2.234", 6379);
        if (jedisPool != null)
            this.jedis = jedisPool.getResource();
    }

    /**
     * 测试分布式锁
     * @throws InterruptedException
     */
    @Test
    public void jedisSetExpire() throws InterruptedException {
        //其中NX为不存在则创建 存在则返回null
        //PX为过期时间
        String result = this.jedis.set("have", "true", "NX", "PX", 3000);
        String result2 = this.jedis.set("have", "true", "NX", "PX", 3000);
        System.out.println(result);
        System.out.println(result2);
        System.out.println(this.jedis.get("have"));
        Thread.sleep(4000);
        System.out.println(this.jedis.get("have"));
    }

    @Test
    public void testReturnThread() {
        this.getCall();
    }

    private Callable getCall() {
        return new Callable() {
            @Override
            public Object call() throws Exception {
                System.out.print("ok");
                return null;
            }
        };
    }

}
