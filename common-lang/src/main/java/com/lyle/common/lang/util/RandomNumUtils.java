package com.lyle.common.lang.util;

import java.util.Random;

/**
 * 随机数生成工具类
 */
public class RandomNumUtils {

	/**
	 * 获取固定位数的随机数
	 *
	 * @param charCount
	 * @return
	 */
	public static String getRandNum(int charCount) {
		String charValue = "";
		for (int i = 0; i < charCount; i++) {
			char c = (char) (randomInt(0, 10) + '0');
			charValue += String.valueOf(c);
		}
		return charValue;
	}

	/**
	 * 随机数获取
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	public static int randomInt(int from, int to) {
		Random r = new Random();
		return from + r.nextInt(to - from);
	}

}
