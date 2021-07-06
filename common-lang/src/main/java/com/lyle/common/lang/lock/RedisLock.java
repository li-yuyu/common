package com.lyle.common.lang.lock;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * @author Lyle
 * @date 2019-06-14
 */
public class RedisLock implements Lock {

    private RedisTemplate<String, String> redisTemplate;
    private String key;
    private String value;
    private long expire;
    private TimeUnit unit;
    private static final String UNLOCK_SCRIPT;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("if redis.call(\"get\",KEYS[1]) == ARGV[1] ");
        sb.append("then ");
        sb.append("    return redis.call(\"del\",KEYS[1]) ");
        sb.append("else ");
        sb.append("    return 0 ");
        sb.append("end ");
        UNLOCK_SCRIPT = sb.toString();
    }

    /**
     * @param redisTemplate
     * @param key           redis键
     * @param expire        redis键过期时间
     * @param unit
     */
    public RedisLock(RedisTemplate<String, String> redisTemplate, String key, long expire, TimeUnit unit) {
        super();
        this.redisTemplate = redisTemplate;
        this.key = key;
        this.value = UUID.randomUUID().toString();
        this.expire = expire;
        this.unit = unit;
    }

    public void lock() throws InterruptedException {
        while (!tryLock()) {
            Thread.sleep(50);
        }
    }

    public boolean tryLock() {
        if (redisTemplate.opsForValue().setIfAbsent(key, value, expire, unit)) {
            return true;
        }
        return false;
    }

    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        long nanosTimeout = unit.toNanos(timeout);
        if (nanosTimeout <= 0L) return false;
        final long deadline = System.nanoTime() + nanosTimeout;

        while (!tryLock()) {
            nanosTimeout = deadline - System.nanoTime();
            if (nanosTimeout <= 0L) return false;
            Thread.sleep(50);
        }
        return true;
    }

    public void unlock() {
        RedisScript<Long> redisScript = new DefaultRedisScript<Long>(UNLOCK_SCRIPT, Long.class);

        Long result = redisTemplate.execute(redisScript, Arrays.asList(key), value);

//        return result == 1;
    }

}
