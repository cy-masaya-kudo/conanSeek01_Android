package com.gency.commons.time;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * <h3>時間関連のユーティリティクラス</h3>
 */
public class GencyTimeUtil {

	/**
	 * 与えられたロングミリ秒に指定日数を加算
	 *
	 * @param targetTimeMills ロングミリ秒
	 * @param addDays 追加したい日数
	 * @return 指定日数を加算したロングミリ秒
	 */
	public static long getLongTimeMillsAddedDays(long targetTimeMills, int addDays) {
		long addDaysL = (long) addDays;
		long aDay = 60 * 1000 * 60 * 24;
		return targetTimeMills + (addDaysL * aDay);
	}

	/**
	 * 与えられたロングミリ秒を日付形式文字列に変換
	 *
	 * @param millis ロングミリ秒
	 * @return 変換後の日付形式文字列
	 */
	public static String formatTimeMillis(long millis) {
		Date date = new Date(millis);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}

	/**
	 * 与えられたロングミリ秒を日付形式文字列に変換
	 *
	 * @param millis ロングミリ秒
	 * @return 変換後の日付形式文字列
	 */
	public static String formatTimeMills(String millis) {
		long millisL = 0;
		String strDate = null;
		try {
			millisL = Long.parseLong(millis);
			strDate = formatTimeMillis(millisL);
		} catch (Exception ex) {
			strDate = millis;
		}
		return strDate;
	}

}
