package com.netmarch.monitorcenter.service.snmp;

import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Calendar;

public class TimeScope {
	public static final String SCOPE_1HOUR="1h";
	public static final String SCOPE_24HOUR="24h";
	public static final String SCOPE_TODAY="today";
	public static final String SCOPE_YESTERDAY = "yesterday";
	public static final String SCOPE_7DAY = "7d";
	public static final String SCOPE_WEEK= "week";
	public static final String SCOPE_LASTWEEK = "lastweek";
	public static final String SCOPE_MONTH = "month";
	public static final String SCOPE_LASTMONTH = "lastmonth";
	public static final String SCOPE_YEAR = "year";

	public static Pair<LocalDateTime,LocalDateTime> compute(String rule)
	{
		LocalDateTime date1 = LocalDateTime.now(),date0;
		if(rule == null)
		{
			return null;
		}

		int p = rule.indexOf("*");


		String unit = rule.substring(0, p);
		int field;
		if (unit.endsWith("m")) {
			field = Calendar.MONTH;
		} else if (unit.endsWith("d")) {
			field = Calendar.DAY_OF_YEAR;
		} else if (unit.endsWith("y")) {
			field = Calendar.YEAR;
		} else {
			throw new InternalException("无效的时间单位，支持日和月(d、m、y)");
		}
		//fieldCount = Integer.parseInt(unit.substring(0, unit.length() - 1));

		return null;
	}

	public static Pair<Timestamp, Timestamp> computeDate(String timeScope) {
		Timestamp date1, date0;
		if(timeScope == null) {
			return null;
		}
		if(timeScope.equals(SCOPE_1HOUR)) {
			date1 = new Timestamp(System.currentTimeMillis());
			date0 = new Timestamp(date1.getTime() - 3600*1000);
		} else {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.MILLISECOND, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MINUTE, 0);

			if(timeScope.equals(SCOPE_24HOUR)) {
				date1 = new Timestamp(System.currentTimeMillis());
				date0 = new Timestamp(date1.getTime() - 24*3600*1000);
			} else if(timeScope.equals(SCOPE_YESTERDAY)) {
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.add(Calendar.DAY_OF_MONTH, -1);
				date0 = new Timestamp(c.getTimeInMillis());
				c.add(Calendar.DAY_OF_MONTH, 1);
				date1 = new Timestamp(c.getTimeInMillis());
			} else if(timeScope.equals(SCOPE_7DAY)) {
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.add(Calendar.DAY_OF_MONTH, -6);
				date0 = new Timestamp(c.getTimeInMillis());
				c.add(Calendar.DAY_OF_MONTH, 7);
				date1 = new Timestamp(c.getTimeInMillis());
			} else if(timeScope.equals(SCOPE_WEEK)) {
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.DAY_OF_WEEK, 1);
				date0 = new Timestamp(c.getTimeInMillis());
				c.add(Calendar.WEEK_OF_YEAR, 1);
				date1 = new Timestamp(c.getTimeInMillis());
			} else if(timeScope.equals(SCOPE_LASTWEEK)) {
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.DAY_OF_WEEK, 1);
				c.add(Calendar.WEEK_OF_YEAR, -1);
				date0 = new Timestamp(c.getTimeInMillis());
				c.add(Calendar.WEEK_OF_YEAR, 1);
				date1 = new Timestamp(c.getTimeInMillis());
			} else if(timeScope.equals(SCOPE_MONTH)) {
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.DAY_OF_MONTH, 1);
				date0 = new Timestamp(c.getTimeInMillis());
				c.add(Calendar.MONTH, 1);
				date1 = new Timestamp(c.getTimeInMillis());
			} else if(timeScope.equals(SCOPE_LASTMONTH)) {
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.DAY_OF_MONTH, 1);
				c.add(Calendar.MONTH, -1);
				date0 = new Timestamp(c.getTimeInMillis());
				c.add(Calendar.MONTH, 1);
				date1 = new Timestamp(c.getTimeInMillis());
			} else if(timeScope.equals(SCOPE_YEAR)) {
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.DAY_OF_MONTH, 1);
				c.set(Calendar.MONTH, 0);
				date0 = new Timestamp(c.getTimeInMillis());
				c.add(Calendar.YEAR, 1);
				date1 = new Timestamp(c.getTimeInMillis());
			} else {//if(timeScope.equals(SCOPE_TODAY)) {
				c.set(Calendar.HOUR_OF_DAY, 0);
				date0 = new Timestamp(c.getTimeInMillis());
				c.add(Calendar.DAY_OF_MONTH, 1);
				date1 = new Timestamp(c.getTimeInMillis());
			} 
		}
		return new Pair<Timestamp,Timestamp>(date0, date1);
	}

	public static void main(String[] args) {
		System.out.println(computeDate(TimeScope.SCOPE_1HOUR));
		System.out.println(computeDate(TimeScope.SCOPE_24HOUR));
		System.out.println(computeDate(TimeScope.SCOPE_7DAY));
		System.out.println(computeDate(TimeScope.SCOPE_LASTMONTH));
		System.out.println(computeDate(TimeScope.SCOPE_LASTWEEK));
		System.out.println(computeDate(TimeScope.SCOPE_MONTH));
		System.out.println(computeDate(TimeScope.SCOPE_TODAY));
		System.out.println(computeDate(TimeScope.SCOPE_WEEK));
		System.out.println(computeDate(TimeScope.SCOPE_YESTERDAY));
	}
}
