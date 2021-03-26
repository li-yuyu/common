package com.lyle.common.lang.util;

/**
 * 断言工具类
 */
public class AssertUtils {

	/**
	 * 断言表达式的值为true，否则抛出传入的异常。
	 *
	 * @param expValue 断言表达式
	 * @param e        抛出的异常
	 */
	public static void assertTrue(boolean expValue, RuntimeException e) {

		if (!expValue) {

			throw e;
		}
	}

	/**
	 * 断言表达式的值为false，否则抛出传入的异常。
	 *
	 * @param expValue 断言表达式
	 * @param e        抛出的异常
	 */
	public static void assertFalse(boolean expValue, RuntimeException e) {

		if (expValue) {

			throw e;
		}
	}

	/**
	 * 断言对象为null，否则抛出传入的异常。
	 *
	 * @param expValue 断言对象
	 * @param e        抛出的异常
	 */
	public static void assertNull(Object object, RuntimeException e) {

		if (object != null) {

			throw e;
		}
	}

	/**
	 * 断言对象非null，否则抛出传入的异常。
	 *
	 * @param object 断言对象
	 * @param e      抛出的异常
	 */
	public static void assertNotNull(Object object, RuntimeException e) {

		if (null == object) {

			throw e;
		}
	}

	/**
	 * 断言字符串为空，否则抛出传入的异常。
	 *
	 * @param str
	 * @param e
	 */
	public static void assertEmpty(String str, RuntimeException e) {

		if (StringUtils.isNotEmpty(str)) {

			throw e;
		}
	}

	/**
	 * 断言字符串非空，否则抛出传入的异常。
	 *
	 * @param str
	 * @param e
	 */
	public static void assertNotEmpty(String str, RuntimeException e) {

		if (StringUtils.isEmpty(str)) {

			throw e;
		}
	}

	/**
	 * 断言两个对象相等，否则抛出传入的异常。
	 *
	 * @param str
	 * @param e
	 */
	public static void assertEquals(Object obj1, Object obj2, RuntimeException e) {

		if (obj1 == null) {

			assertNull(obj2, e);
			return;
		}

		if (!obj1.equals(obj2)) {

			throw e;
		}
	}

}
