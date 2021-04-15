package com.lyle.common.lang.cache;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author Lyle
 * @since 2021年2月23日 上午11:37:25 
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cacheable {

	String key();

	/**
	 * 缓存有效时间
	 *
	 * @return long
	 */
	long expireTime();

	/**
	 * 缓存主动在失效前强制刷新缓存的时间 建议是： preloadTime = expireTime * 0.2
	 *
	 * @return long
	 */
	long preloadTime();

	/**
	 * 时间单位 {@link TimeUnit}
	 *
	 * @return TimeUnit
	 */
	TimeUnit timeUnit();

	String desc() default "";
}
