package com.sharshar.taskservice.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Re-usuable code
 * Created by lsharshar on 3/16/2018.
 */
class GenUtils {
	private GenUtils() {}
	private static Logger logger = LogManager.getLogger();
	public static Date parseDate(String date, SimpleDateFormat sdf) {
		Date dateVal = null;
		try {
			dateVal = sdf.parse(date);
		} catch (Exception ex) {
			logger.error("Invalid date: " + date + " for format " + sdf.toPattern(), ex);
			return null;
		}
		return dateVal;
	}
}
