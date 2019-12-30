

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {
	public static final String yyyy_MM_dd = "yyyy-MM-dd";
	public static final String yyyyMMddHHmm = "yyyyMMddHHmm";
	public static final String ddMMyyHHmm = "ddMMyyHHmm";
	public static final String MMddyyyyhhmmssaa = "MM/dd/yyyy hh:mm:ss aa";
	public static final String yyyyMMddHHmmss = "yyyyMMdd HH:mm:ss";
	public static final String yyyyMMdd = "yyyyMMdd";
	public static final String yyyyMM = "yyyyMM";
	public static final String yyyy_MM_ddHHmmss = "yyyy-MM-dd HH:mm:ss";
	public static final String yyyyMMdd_HHmm = "yyyyMMdd_HHmm";
	
	private static CxLogger logger = CxLogManager.getLogger(DateUtil.class);
	
	public static Date asDate(LocalDate localDate) {
		return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
	}

	public static Date StringToDate(String strDate, String dateFormat) {
		Date convertedDate = null;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
		LocalDate localdate = LocalDate.parse(strDate,formatter);
		convertedDate = asDate(localdate);

		return convertedDate;
	}
	
	/**
	 * get the hours between the startDate and endDate
	 * @param startDate
	 * @param endDate
	 * @param format
	 * @return
	 * @throws ParseException 
	 */
	public static double getHoursBetweenDate(String startDate, String endDate, String format) throws ParseException {
		double hours = 0;
		SimpleDateFormat ft = new SimpleDateFormat(format);
		Date date1 = ft.parse(startDate);
		Date date2 = ft.parse(endDate);
		hours = date2.getTime() - date1.getTime();
		hours = hours * 1.0 / 1000 / 60 / 60;

		return hours;
	}
	
	/**
	 * get the minutes between the startDate and endDate
	 * @param startDate
	 * @param endDate
	 * @param format
	 * @return
	 * @throws ParseException 
	 */
	public static long getMinutesBetweenDate(String startDate, String endDate, String format) {
		long hours = 0;
		try {
			SimpleDateFormat ft = new SimpleDateFormat(format);
			Date date1 = ft.parse(startDate);
			Date date2 = ft.parse(endDate);
			hours = date2.getTime() - date1.getTime();
			hours = hours / 1000 / 60 ;
		} catch (ParseException e) {
			//e.printStackTrace();
			logger.error("[DateUtil] getMinutesBetweenDate throws ParseException, startDate=" + startDate + ", endDate=" + endDate);
		}
		return hours;
	}
	
	public static String convertDateToString(Date date, String format) {
		SimpleDateFormat f = new SimpleDateFormat(format);
		return f.format(date);
	}
	
	public static String convertDateToString(Date date,String format,Locale locale) {
		SimpleDateFormat f=new SimpleDateFormat(format,locale);
		return f.format(date);    
	}

	public static Date convertDateTimeForTimezone(Date date, String format, TimeZone fromTimeZone, TimeZone toTimeZone) {
		SimpleDateFormat toSdf = new SimpleDateFormat(format);
		toSdf.setTimeZone(toTimeZone);
		String dateStr = toSdf.format(date);

		SimpleDateFormat fromSdf = new SimpleDateFormat(format);
		fromSdf.setTimeZone(fromTimeZone);
		try {
			return fromSdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;

	}

	public static Date changeDate(Date date, int yearAdd, int monthAdd, int dateAdd, int hourAdd, int minuteAdd, int secondAdd, int milSecAdd) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		if (yearAdd != 0) {
			c.add(Calendar.YEAR, yearAdd);
		}
		if (monthAdd != 0) {
			c.add(Calendar.MONTH, monthAdd);
		}
		if (dateAdd != 0) {
			c.add(Calendar.DATE, dateAdd);
		}
		if (hourAdd != 0) {
			c.add(Calendar.HOUR, hourAdd);
		}
		if (minuteAdd != 0) {
			c.add(Calendar.MINUTE, minuteAdd);
		}
		if (secondAdd != 0) {
			c.add(Calendar.SECOND, secondAdd);
		}
		if (milSecAdd != 0) {
			c.add(Calendar.MILLISECOND, milSecAdd);
		}

		return c.getTime();
	}
	
	public static Date getFirstDateLastMonth() {
		Date currentDate = new Date();
		Date dateLastMonth = DateUtil.changeDate(currentDate, 0, -1, 0, 0, 0, 0, 0);
		Calendar c = Calendar.getInstance();
		c.setTime(dateLastMonth);
		c.set(Calendar.DATE, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		Date d = c.getTime();
		return d;
	}

	public static Date getLastTwoWeeks(Date currentDate){

		Date dateLastWeekThu = DateUtil.changeDate(currentDate, 0, 0, -14, 0, 0, 0, 0);
		Calendar c = Calendar.getInstance();
		c.setTime(dateLastWeekThu);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		Date d = c.getTime();
		return d;
	}
	
}
