package org.alfresco.service.cmr.calendar;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CalendarTimezoneHelperTest {

	/**
	 * Ensure that iCal lines are split into key and value
	 * by the first unquoted colon
	 */
	@Test
	public void icalLineKeyValue(){
		
		//blank line
		String icalLine = "";
		String[] keyVal = CalendarTimezoneHelper.icalLineKeyValue(icalLine);
		assertEquals("", keyVal[0]);
		assertEquals("", keyVal[1]);
		
		//single unquoted
		icalLine = "a:b";
		keyVal = CalendarTimezoneHelper.icalLineKeyValue(icalLine);
		assertEquals("a", keyVal[0]);
		assertEquals("b", keyVal[1]);
		
		//multiple unquoted colons
		icalLine = "a:bcd:ds";
		keyVal = CalendarTimezoneHelper.icalLineKeyValue(icalLine);
		assertEquals("a", keyVal[0]);
		assertEquals("bcd:ds", keyVal[1]);
		
		//deliminating colon preceded by quoted colon
		icalLine = "a\":bcdA\":ds";
		keyVal = CalendarTimezoneHelper.icalLineKeyValue(icalLine);
		assertEquals("a\":bcdA\"", keyVal[0]);
		assertEquals("ds", keyVal[1]);
		
		//deliminating colon preceded and followed by quoted colons
		icalLine = "a\":bcdA\":ds\"hello\"";
		keyVal = CalendarTimezoneHelper.icalLineKeyValue(icalLine);
		assertEquals("a\":bcdA\"", keyVal[0]);
		assertEquals("ds\"hello\"", keyVal[1]);
		
		//unbalanced quotes
		icalLine = "a\":bcdA:ds";
		keyVal = CalendarTimezoneHelper.icalLineKeyValue(icalLine);
		assertEquals("", keyVal[0]);
		assertEquals("", keyVal[1]);
	}
	
}
