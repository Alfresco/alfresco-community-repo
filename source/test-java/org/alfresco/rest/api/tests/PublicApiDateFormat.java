package org.alfresco.rest.api.tests;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class PublicApiDateFormat
{
	private static DateFormat dateFormat;
	
	private PublicApiDateFormat()
	{
	}

	// The date format to use when checking dates coming back from the rest api
	static
	{
		dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        TimeZone gmt = TimeZone.getTimeZone("UTC");
        dateFormat.setTimeZone(gmt);        
	}
	
	public static DateFormat getDateFormat()
	{
		return dateFormat;
	}
}
