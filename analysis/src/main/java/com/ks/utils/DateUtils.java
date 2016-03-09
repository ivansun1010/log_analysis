package com.ks.utils;

import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

	public static Logger LOG = Logger.getLogger(DateUtils.class);

	public static final String C_DATE_PATTON_DEFAULT = "yyyy-MM-dd";


	private static String[] FORMATS = new String[]{
			"yyyy-MM-dd'T'HH:mm:ss.SSS",
			"yyyy.MM.dd G 'at' HH:mm:ss z",
			"yyyyy.MMMMMM.dd GGG hh:mm aaa",
			"EEE, d MMM yyyy HH:mm:ss Z",
			"yyMMddHHmmssZ"
	};

	private Date parseDate(String value) {
		for ( int i = 0; i < FORMATS.length; i ++ ) {
			SimpleDateFormat format = new SimpleDateFormat(FORMATS[i]);
			Date temp;
			try {
				temp = format.parse(value);
				if ( temp != null ) {
					return temp;
				}
			}
			catch ( ParseException e ) {
			}
		}

		LOG.error("Cloud not parse timestamp for log");
		return null;
	}

	public long durationTime(String sTime) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd H:mm:ss.SSS");
		try {
			Date startDate = simpleDateFormat.parse("19700101 "+sTime);
			Date endDate = simpleDateFormat.parse("19700101 0:00:00.000");
			return startDate.getTime()-endDate.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return -1;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
