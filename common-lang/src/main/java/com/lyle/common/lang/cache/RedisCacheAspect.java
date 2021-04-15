package com.lyle.common.lang.cache;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.lyle.common.lang.lock.RedisLock;

/**
 * @author Lyle
 * @since 2021年2月23日 上午11:37:19
 */
@Aspect
@Component
public class RedisCacheAspect {

	public static final String LOCK_SUFFIX = ":mutexLock";

	@Autowired
	protected RedisTemplate<String, Object> objectRedisTemplate;
	@Autowired
	protected RedisTemplate<String, String> stringRedisTemplate;

	@Pointcut("@annotation(com.liyuyu.art.common.redis.Cacheable)")
	public void cacheablePointcut() {
	}

	@Around("cacheablePointcut()")
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		Cacheable annotation = findAnnotation(pjp);

		BoundValueOperations<String, Object> ops = objectRedisTemplate.boundValueOps(annotation.key());

		if (ops.getExpire() > TimeUnit.SECONDS.convert(annotation.preloadTime(), annotation.timeUnit())) {
			return ops.get();
		}

		RedisLock redisLock = new RedisLock(stringRedisTemplate, annotation.key() + LOCK_SUFFIX, 1, TimeUnit.MINUTES);
		if (!redisLock.tryLock()) {
			// 未获取到锁的线程继续返回缓存中内容
			return ops.get();
		}

		// 获取到锁的线程，执行方法并刷新缓存
		Object result = pjp.proceed();
		ops.set(result, annotation.expireTime(), annotation.timeUnit());

		redisLock.unlock();

		return result;
	}

	private Cacheable findAnnotation(ProceedingJoinPoint pjp) throws NoSuchMethodException, SecurityException {
		MethodSignature ms = (MethodSignature) pjp.getSignature();
		Class<?> classTarget = pjp.getTarget().getClass();
		Method method = classTarget.getMethod(ms.getName(), ms.getParameterTypes());

		return method.getAnnotation(Cacheable.class);
	}

}
