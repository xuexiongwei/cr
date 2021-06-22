package com.example.copyresult.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/12/5.
 */
public class StringUtils {

	private static String uuid = null;

	public static boolean isEmpty(String str) {
		return str == null || str.equals("");
	}

	public static String uuid32() {

		if (null == uuid) {
			String uid = UUID.randomUUID().toString();
			uuid = uid.replace("-", "");
		}
		return uuid;
	}
}