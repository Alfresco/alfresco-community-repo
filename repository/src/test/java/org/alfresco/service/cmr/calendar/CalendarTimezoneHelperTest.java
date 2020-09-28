/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
