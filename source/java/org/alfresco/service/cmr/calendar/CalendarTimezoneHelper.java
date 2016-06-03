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

import java.util.HashMap;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides helper functions for when working with Timezones
 *  for Calendar events.
 * It provides support for generating iCal timezone information blocks,
 *  and building Java TimeZones based on iCal timezone information. 
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class CalendarTimezoneHelper 
{
   private static Log logger = LogFactory.getLog(CalendarTimezoneHelper.class);
   
   private static final String ICAL_SECTION_EVENT = "VEVENT";  
   private static final String ICAL_SECTION_TIMEZONE = "VTIMEZONE";  
   private static final String ICAL_SECTION_TZ_STANDARD = "STANDARD";  
   private static final String ICAL_SECTION_TZ_DAYLIGHT = "DAYLIGHT";  
   
//   public static String toICalTimeZone()
//   {
//      // TODO
//      return null;
//   }
 
   /**
    * Builds a Java TimeZone from the VTIMEZONE info in an 
    *  iCal file.
    * @return a Java TimeZone that matches the iCal one, or NULL if no TZ info present
    */
   public static SimpleTimeZone buildTimeZone(String ical)
   {
      return buildTimeZone( getICalParams(ical) );
   }
   
   /**
    * Internal version that takes the parameters from {@link #getICalParams(String)}
    *  and builds a TimeZone from it.
    * This is not public as it will be refactored when {@link #getICalParams(String)}
    *  is replaced.
    * Note - because it uses the icalParams, we can't handle cases where we're
    *  given historic TZ info (eg until 2004 it was that, now it's this)
    */
   protected static SimpleTimeZone buildTimeZone(Map<String, String> icalParams)
   {
      // Pull out the interesting TZ parts
      Map<String,String> tzCore = new HashMap<String, String>();
      Map<String,String> tzStandard = new HashMap<String, String>();
      Map<String,String> tzDaylight = new HashMap<String, String>();
      
      for (String key : icalParams.keySet())
      {
         if (key.startsWith("TZ-"))
         {
            String value = icalParams.get(key);
            
            // Assign
            key = key.substring(3);
            Map<String,String> dst = tzCore;
            
            if (key.startsWith(ICAL_SECTION_TZ_STANDARD))
            {
               dst = tzStandard;
               key = key.substring(ICAL_SECTION_TZ_STANDARD.length()+1);
            }
            else if (key.startsWith(ICAL_SECTION_TZ_DAYLIGHT))
            {
               dst = tzDaylight;
               key = key.substring(ICAL_SECTION_TZ_DAYLIGHT.length()+1);
            }
            
            dst.put(key, value);
         }
      }
      
      // Do we have any timezone info?
      if (tzStandard.isEmpty() && tzDaylight.isEmpty())
      {
         logger.warn("No Standard/Daylight info found for " + tzCore);
         return null;
      }
      
      // Grab the name of it
      String tzID = tzCore.get("TZID");
      if (tzID == null || tzID.isEmpty())
      {
         tzID = "(unknown)";
      }
      // De-escape commans
      tzID = tzID.replace("\\,", ",");
      
      // Does it have daylight savings?
      if (tzDaylight.isEmpty())
      {
         // Life is easy!
         int offset = getOffset(tzStandard.get("TZOFFSETTO"));
         return new SimpleTimeZone(offset, tzID);
      }
      
      // Get the offsets
      int stdOffset = getOffset(tzDaylight.get("TZOFFSETFROM"));
      int dstOffset = getOffset(tzDaylight.get("TZOFFSETTO"));
      
      // Turn the rules into SimpleTimeZone ones
      int[] stdRules = getRuleForSimpleTimeZone(tzStandard.get("RRULE"));
      int[] dstRules = getRuleForSimpleTimeZone(tzDaylight.get("RRULE"));
      
      // Build it up
      return new SimpleTimeZone(
            stdOffset, tzID,
            dstRules[0], dstRules[1], dstRules[2], // When DST starts
            1*60*60*1000, // TODO Pull out the exact change time from DTSTART
            stdRules[0], stdRules[1], stdRules[2], // When DST ends
            2*60*60*1000  // TODO Pull out the exact change time from DTSTART
      );
   }
   
   /**
    * Turns an iCal offset like "+1000" or "-0730" into an
    *  offset in milliseconds from UTC 
    */
   private static int getOffset(String tzOffset)
   {
     int sign = 1;
     
     // + or - from UTC?
     if (tzOffset.startsWith("+"))
     {
        sign = 1;
        tzOffset = tzOffset.substring(1);
     }
     else if (tzOffset.startsWith("-"))
     {
        sign = -1;
        tzOffset = tzOffset.substring(1);
     }
     
     int mins = Integer.parseInt( tzOffset.substring(tzOffset.length()-2));
     int hours = Integer.parseInt(tzOffset.substring(0, tzOffset.length()-2));
     
     int offset = ((hours*60) + mins) * 60 * 1000;
     offset = offset * sign;
     
     return offset;
   }
   
   /**
    * Turn an iCal repeating rule like
    *  "FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10" into a SimpleTimeZone rule
    *  like Month=March, StartDay=0, StartDayOfWeek=-Sunday
    * See the JavaDocs of {@link SimpleTimeZone} for how to express
    *  the different requirements in the required int formats
    */
   private static int[] getRuleForSimpleTimeZone(String rule)
   {
      // Turn the rule into chunks
      Map<String,String> params = new HashMap<String, String>();
      for (String p : rule.split(";"))
      {
         int splitAt = p.indexOf('=');
         if (splitAt == -1)
         {
            logger.info("Skipping invalid param " + p + " in recurrence rule " + rule);
         }
         else
         {
            params.put(p.substring(0,splitAt), p.substring(splitAt+1));
         }
      }
      
      // Java months are 1 less than normal
      int month = Integer.parseInt(params.get("BYMONTH")) - 1;
      
      // Should end with a day of the week
      String byDay = params.get("BYDAY");
      String dow = byDay.substring(byDay.length()-2);
      int dayOfWeek = CalendarRecurrenceHelper.d2cd.get(dow);
      
      // Where in the month does it come?
      int dayOfMonth = 0;
      if (byDay.startsWith("-1"))
      {
         // Last in month
         dayOfMonth = -1;
      }
      else if (byDay.startsWith("1"))
      {
         // First in month
         dayOfMonth = 1;
         dayOfWeek = 0 - dayOfWeek;
      }
      else
      {
         // Nth day in month
         dayOfMonth = 1 + (Integer.parseInt(byDay.substring(0,1)) - 1)*7; 
         dayOfWeek = 0 - dayOfWeek;
      }
      
      // All done
      return new int[] {month, dayOfMonth, dayOfWeek};
   }
   
  
   /**
    * Splits an iCal line into key and value by the first
    * unquoted colon.
    * @param icalLine String
    */
   protected static String[] icalLineKeyValue(String icalLine){
	   int delim = indexOfFirstUnquotedColon(icalLine);
	   if(delim == -1){
		   return new String[]{"",""};
	   }
	   String key = icalLine.substring(0,delim);
	   String value = icalLine.substring(delim+1);
       return new String[]{key,value};
   }
   
   /**
    * @param icalLine String
    * @return location of first non quote enclosed colon
    */
   private static int indexOfFirstUnquotedColon(String icalLine){
	   int colon = icalLine.indexOf(":");
	   int quote = icalLine.indexOf("\"");
	   
	   if(quote == -1){
    	   return colon;
       }else{
    	   if(colon<quote){
    		   return colon;
    	   }else{
    		   //next quote, skipping past a colon if exists
    		   int nextQuote = icalLine.indexOf("\"", quote+1);
    		   if(nextQuote==-1){
    			  //will only happen if the quotes are unbalanced
    			  return  -1;
    		   }else{
    			   return nextQuote + indexOfFirstUnquotedColon(icalLine.substring(nextQuote+1, icalLine.length())) + 1;
    		   }
    	   }
       }
   }
   
   /**
    * Turns an iCal event into event + timezone parameters.
    * This is very closely tied to the SPP / VTI implementation,
    *  and should be replaced with something more general.
    * Until then, it is deliberately not public.
    *  
    * @param icalText iCal text for the event, and the TZ (prefixed)
    */
   protected static Map<String, String> getICalParams(String icalText)
   {
       // Split the iCal file by lines
       String[] segregatedLines = icalText.split("\r\n");
       if (segregatedLines.length == 1 && icalText.indexOf('\n') > 0)
       {
          segregatedLines = icalText.split("\n");
       }
      
       // Perform a stack based parsing of it
       Map<String, String> result = new HashMap<String, String>();
       int attendeeNum = 0;
       Stack<String> stack = new Stack<String>();
       for (String line : segregatedLines)
       {
           String[] keyValue = icalLineKeyValue(line);
           if (keyValue.length >= 2)
           {
               if (keyValue[0].equals("BEGIN"))
               {
                   stack.push(keyValue[1]);
                   continue;
               }
               if (keyValue[0].equals("END"))
               {
                   stack.pop();
                   continue;
               }
               
               if (!stack.isEmpty() && stack.peek().equals(ICAL_SECTION_EVENT))
               {
                   if (keyValue[0].contains(";"))
                   {
                       // Capture the extra details as suffix keys, they're sometimes needed
                       int splitAt = keyValue[0].indexOf(';');
                       String mainKey = keyValue[0].substring(0, splitAt);
                       
                       if (splitAt < keyValue[0].length() - 2)
                       {
                          // Grab each ;k=v part and store as mainkey-k=v
                          String[] extras = keyValue[0].substring(splitAt+1).split(";");
                          for (String extra : extras)
                          {
                             splitAt = extra.indexOf('=');
                             if (splitAt > -1 && !result.containsKey(mainKey+"-"+extra.substring(0,splitAt-1)))
                             {
                                result.put(mainKey+"-"+extra.substring(0,splitAt-1), extra.substring(splitAt+1));
                             }
                          }
                       }

                       // Use the main key for the core value
                       keyValue[0] = mainKey;
                   }
                   if (keyValue[0].equals("ATTENDEE"))
                   {
                       keyValue[0] = keyValue[0] + attendeeNum;
                       attendeeNum++;
                   }
                   
                   if (!result.containsKey(keyValue[0]))
                   {
                       result.put(keyValue[0], keyValue[keyValue.length - 1]);
                   }                   
               }
               
               if (!stack.isEmpty() && stack.peek().equals(ICAL_SECTION_TIMEZONE) && !result.containsKey("TZ-" + keyValue[0]))
               {
                   // Store the top level timezone details with a TZ prefix
                   result.put("TZ-"+keyValue[0], keyValue[keyValue.length-1]);
               }
               if (stack.size() >= 2 && stack.get(stack.size()-2).equals(ICAL_SECTION_TIMEZONE) &&
                     (stack.peek().equals(ICAL_SECTION_TZ_STANDARD) || stack.peek().equals(ICAL_SECTION_TZ_DAYLIGHT)) && 
                        !result.containsKey("TZ-"+stack.peek()+"-"+keyValue[0]))
               {
                   // Store the timezone details with a TZ prefix + details type
                   result.put("TZ-"+stack.peek()+"-"+keyValue[0], keyValue[keyValue.length-1]);
               }
           }
       }
       return result;
   }
}
