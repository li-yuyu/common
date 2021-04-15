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
public class RedisLock {

	private RedisTemplate<String, String> redisTemplate;
	private String key;
	private String value;
	private long timeout;
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
	 * @param key           redis中的键值
	 * @param timeout       超时时间
	 * @param unit
	 */
	public RedisLock(RedisTemplate<String, String> redisTemplate, String key, long timeout, TimeUnit unit) {
		super();
		this.redisTemplate = redisTemplate;
		this.key = key;
		this.value = UUID.randomUUID().toString();
		this.timeout = timeout;
		this.unit = unit;
	}

	/**
	 * 尝试获取锁
	 * 
	 * @return
	 */
	public boolean tryLock() {
		if (redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit)) {
			return true;
		}
		return false;
	}

	/**
	 * 尝试获取锁，获取不到时自旋等待
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public boolean tryLockAndSpinWait() throws InterruptedException {
		while (!tryLock()) {
			Thread.sleep(200);
		}
		return true;
	}

	/**
	 * 解锁
	 * 
	 * @return
	 */
	public boolean unlock() {
		RedisScript<Long> redisScript = new DefaultRedisScript<Long>(UNLOCK_SCRIPT, Long.class);

		Long result = redisTemplate.execute(redisScript, Arrays.asList(key), value);

		return result == 1;
	}

}
