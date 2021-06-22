package com.example.copyresult.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 日期处理
 * 
 * @author uisftech
 *
 */
public class DateUtils {

	public static String FORMAT0 = "yyyy-MM-dd HH:mm:ss:SSS";
	public static String FORMAT1 = "yyyy-MM-dd HH:mm:ss";
	public static String FORMAT2 = "yyyy-MM-dd";
	public static String FORMAT3 = "yyyyMMdd";
	public static String FORMAT4 = "HH:mm:ss";
	public static String FORMAT5 = "HHmmss";

	private static String module = DateUtils.class.getSimpleName();

	/** 锁对�? */
	private static final Object lockObj = new Object();

	/** 存放不同的日期模板格式的sdf的Map */
	private static Map<String, ThreadLocal<SimpleDateFormat>> sdfMap = new HashMap<String, ThreadLocal<SimpleDateFormat>>();

	/**
	 * 是用ThreadLocal<SimpleDateFormat>来获取SimpleDateFormat,这样每个线程只会有一个SimpleDateFormat
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String getDataStr(String pattern, Date date) {
		return getSdf(pattern).format(date);
	}

	public static Date parse(String dateStr, String pattern) throws ParseException {
		return getSdf(pattern).parse(dateStr);
	}

	public static String getTimeStr(String pattern, Date date) {
		return getSdf(pattern).format(date);
	}

	/**
	 * Return a string formatted as format
	 *
	 * @return String formatted for right now
	 */
	public static String nowDateString(String format) {
		return getDataStr(format, new Date());
	}

	/**
	 * 返回�?��ThreadLocal的sdf,每个线程只会new�?��sdf
	 * 
	 * @param pattern
	 * @return
	 */
	private static SimpleDateFormat getSdf(final String pattern) {
		ThreadLocal<SimpleDateFormat> tl = sdfMap.get(pattern);

		// 此处的双重判断和同步是为了防止sdfMap这个单例被多次put重复的sdf
		if (tl == null) {
			synchronized (lockObj) {
				tl = sdfMap.get(pattern);
				if (tl == null) {
					// 只有Map中还没有这个pattern的sdf才会生成新的sdf并放入map
					// System.out.println("put new sdf of pattern " + pattern + " to map");

					// 这里是关�?使用ThreadLocal<SimpleDateFormat>替代原来直接new SimpleDateFormat
					tl = new ThreadLocal<SimpleDateFormat>() {

						@Override
						protected SimpleDateFormat initialValue() {
							return new SimpleDateFormat(pattern);
						}
					};
					sdfMap.put(pattern, tl);
				}
			}
		}

		return tl.get();
	}

	/**
	 * java 获取 获取某年某月 所有日期（yyyy-mm-dd格式字符串）
	 * 
	 * @param year
	 * @param month
	 * @return
	 */
	public static List<String> getMonthFullDay(int year, int month) {
		SimpleDateFormat dateFormatYYYYMMDD = new SimpleDateFormat("yyyy-MM-dd");
		List<String> fullDayList = new ArrayList<>(32);
		// 获得当前日期对象
		Calendar cal = Calendar.getInstance();
		cal.clear();// 清除信息
		cal.set(Calendar.YEAR, year);
		// 1月从0开始
		cal.set(Calendar.MONTH, month - 1);
		// 当月1号
		cal.set(Calendar.DAY_OF_MONTH, 1);
		int count = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		for (int j = 1; j <= count; j++) {
			fullDayList.add(dateFormatYYYYMMDD.format(cal.getTime()));
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}
		return fullDayList;
	}
	
	/**
	 *  某年某月的最后一天
	 * @param year
	 * @param month
	 * @return
	 */
	public static String getLastDay(int year,int month){
		
		SimpleDateFormat dateFormatYYYYMMDD = new SimpleDateFormat("yyyy-MM-dd");
	    Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.YEAR, year);
	    cal.set(Calendar.MONTH, month);
	    cal.set(Calendar.DAY_OF_MONTH, 0); //关键！
	    return dateFormatYYYYMMDD.format(cal.getTime());
	}
}
