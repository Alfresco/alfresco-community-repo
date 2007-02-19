/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.service.cmr.repository.datatype;

import java.io.IOException;
import java.io.Serializable;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;

import org.alfresco.util.CachingDateFormat;

/**
 * This data type represents duration/interval/period as defined by the XMLSchema type
 * duration.
 * 
 * The lexical representation of duration is
 * PnYnMnDTnHnMnS.
 * 
 * P is a literal value that starts the expression
 * nY is an integer number of years followed by the literal Y
 * nM is an integer number of months followed by the literal M
 * nD is an integer number of days followed by the literal D
 * T is the literal that separates the date and time
 * nH is an integer number of hours followed by a literal H
 * nM is an integer number of minutes followed by a literal M
 * nS is a decimal number of seconds followed by a literal S
 * 
 * Any numbers and designator may be absent if the value is zero.
 * A minus sign may appear before the literal P to indicate a negative duration.
 * If no time items are present the literal T must not appear.
 * 
 * 
 * This implementation is immutable and thread safe.
 * 
 * There are two forms of duration common on database types.
 * The code contains warnings wheer these are relevant.
 *
 * @author andyh
 */
public class Duration implements Comparable, Serializable
{

   static final long serialVersionUID = 3274526442325176068L;    

   public static final String XML_DAY = "P1D";
   public static final String XML_WEEK = "P7D";
   public static final String XML_TWO_WEEKS = "P14D";
   public static final String XML_MONTH = "P1M";
   public static final String XML_QUARTER = "P3M";
   public static final String XML_SIX_MONTHS = "P6M";
   public static final String XML_YEAR = "P1Y";

   public static final Duration DAY = new Duration(XML_DAY);
   public static final Duration WEEK = new Duration(XML_WEEK);
   public static final Duration TWO_WEEKS = new Duration(XML_TWO_WEEKS);
   public static final Duration MONTH = new Duration(XML_MONTH);
   public static final Duration QUARTER = new Duration(XML_QUARTER);
   public static final Duration SIX_MONTHS = new Duration(XML_SIX_MONTHS);
   public static final Duration YEAR = new Duration(XML_YEAR);

   private static final String s_parse = "-PYMDTHmS";

   private boolean m_positive = true;
   private int m_years = 0;
   private int m_months = 0;
   private int m_days = 0;
   private int m_hours = 0;
   private int m_mins = 0;
   private int m_seconds = 0;
   private int m_nanos = 0;

   // Date duration arithmetic
   
   /**
    * Add a duration to a date and return the date plus the specified increment.
    * 
    * @param date - the initial date
    * @param duration - the duration to add on to the date (the duration may be negative)
    * @return the adjusted date.
    */
   public static Date add(Date date, Duration duration)
   {
      Calendar c = Calendar.getInstance();
      c.setTime(date);
      c.add(Calendar.YEAR, (duration.m_positive ? 1 : -1) * duration.m_years);
      c.add(Calendar.MONTH, (duration.m_positive ? 1 : -1) * duration.m_months);
      c.add(Calendar.DATE, (duration.m_positive ? 1 : -1) * duration.m_days);
      c.add(Calendar.HOUR_OF_DAY, (duration.m_positive ? 1 : -1) * duration.m_hours);
      c.add(Calendar.MINUTE, (duration.m_positive ? 1 : -1) * duration.m_mins);
      c.add(Calendar.SECOND, (duration.m_positive ? 1 : -1) * duration.m_seconds);
      c.add(Calendar.MILLISECOND, (duration.m_positive ? 1 : -1) * duration.m_nanos / 1000000);
      return c.getTime();
   }

   /**
    * Subtract a period for a given date
    * 
    * @param date - the intial date
    * @param duration - the diration to subtract
    * @return the adjusted date.
    */
   
   public static Date subtract(Date date, Duration duration)
   {
      return add(date, duration.unaryMinus());
   }

   

   /**
    * Constructor for Duration - a zero value duration
    */

   public Duration()
   {
      super();
   }

   /**
    * Construct a Duration from the XMLSchema definition
    */

   public Duration(String duration)
   {

      if (duration.equals("P"))
      {
         throw new RuntimeException("Invalid period: P");
      }

      if (!duration.startsWith("P") && !duration.startsWith("-P"))
      {
         throw new RuntimeException("Invalid period: must start with P or -P");
      }
      else
      {
         boolean dateMode = true;
         int last = -1;
         Double nval = null;
         StringReader reader = new StringReader(duration);
         StreamTokenizer tok = new StreamTokenizer(reader);
         tok.resetSyntax();
         tok.eolIsSignificant(true);
         tok.parseNumbers();
         tok.ordinaryChars('-', '-');
         tok.ordinaryChars('P', 'P');
         tok.ordinaryChars('Y', 'Y');
         tok.ordinaryChars('M', 'M');
         tok.ordinaryChars('D', 'D');
         tok.ordinaryChars('T', 'T');
         tok.ordinaryChars('H', 'H');
         tok.ordinaryChars('m', 'm');
         tok.ordinaryChars('S', 'S');

         int token;
         try
         {
            while ((token = tok.nextToken()) != StreamTokenizer.TT_EOF)
            {
               if (token == StreamTokenizer.TT_NUMBER)
               {
                  nval = new Double(tok.nval);
               }
               else if (token == StreamTokenizer.TT_EOF)
               {
                  throw new RuntimeException("Invalid EOF in Duration");
               }
               else if (token == StreamTokenizer.TT_EOL)
               {
                  throw new RuntimeException("Invalid EOL in Duration");
               }
               else if (token == StreamTokenizer.TT_WORD)
               {
                  throw new RuntimeException("Invalid text in Duration: " + tok.sval);
               }
               else
               {
                  if (tok.ttype == '-')
                  {
                     last = checkIndex(last, "-");
                     m_positive = false;
                  }
                  else if (tok.ttype == 'P')
                  {
                     last = checkIndex(last, "P");
                     // nothing
                  }
                  else if (tok.ttype == 'Y')
                  {
                     last = checkIndex(last, "Y");
                     if (nval != null)
                     {
                        m_years = nval.intValue();
                     }
                     else
                     {
                        throw new RuntimeException("IO Error parsing Duration: " + duration);
                     }
                     nval = null;
                  }
                  else if (tok.ttype == 'M')
                  {
                     if (dateMode)
                     {
                        last = checkIndex(last, "M");
                        if (nval != null)
                        {
                           m_months = nval.intValue();
                        }
                        else
                        {
                           throw new RuntimeException("IO Error parsing Duration: " + duration);
                        }
                        nval = null;
                     }
                     else
                     {
                        last = checkIndex(last, "m");
                        if (nval != null)
                        {
                           m_mins = nval.intValue();
                        }
                        else
                        {
                           throw new RuntimeException("IO Error parsing Duration: " + duration);
                        }
                        nval = null;
                     }
                  }
                  else if (tok.ttype == 'D')
                  {
                     last = checkIndex(last, "D");
                     if (nval != null)
                     {
                        m_days = nval.intValue();
                     }
                     else
                     {
                        throw new RuntimeException("IO Error parsing Duration: " + duration);
                     }
                     nval = null;
                  }
                  else if (tok.ttype == 'T')
                  {
                     last = checkIndex(last, "T");
                     dateMode = false;
                     nval = null;
                  }
                  else if (tok.ttype == 'H')
                  {
                     last = checkIndex(last, "H");
                     if (nval != null)
                     {
                        m_hours = nval.intValue();
                     }
                     else
                     {
                        throw new RuntimeException("IO Error parsing Duration: " + duration);
                     }
                     nval = null;
                  }
                  else if (tok.ttype == 'S')
                  {
                     last = checkIndex(last, "S");
                     if (nval != null)
                     {
                        m_seconds = nval.intValue();
                        m_nanos = (int) ((long) (nval.doubleValue() * 1000000000) % 1000000000);
                     }
                     else
                     {
                        throw new RuntimeException("IO Error parsing Duration: " + duration);
                     }
                     nval = null;
                  }
                  else
                  {
                     throw new RuntimeException("IO Error parsing Duration: " + duration);
                  }
               }
            }
         }
         catch (IOException e)
         {
            throw new RuntimeException("IO Error parsing Duration: " + duration);
         }
         catch (RuntimeException e)
         {
            throw new RuntimeException("IO Error parsing Duration: " + duration, e);
         }
      }
   }

   /**
    * Simple index to check identifiers appear in order
    */

   private int checkIndex(int last, String search)
   {
      if ((search == null) || (search.length() == 0))
      {
         throw new RuntimeException("Null or zero length serach");
      }
      int index = s_parse.indexOf(search);
      if (index > last)
      {
         return index;
      }
      else
      {
         throw new RuntimeException("Illegal position for identifier " + search);
      }
   }

   /**
    * Create a duration given a date. The duration is between the two dates provided.
    * 
    * Sadly, it works out the duration by incrementing the lower calendar until it matches
    * the higher. 
    */

   public Duration(Date date)
   {
      this(date, new Date());
   }

   /**
    * Create a duration betweeen two dates expressed as strings. 
    * Uses the standard XML date form.
    * 
    * @param start - the date at the start of the period
    * @param end - the date at the end of the period
    */
   
   public Duration(String start, String end)
   {
      this(parseDate(start), parseDate(end));
   }

   
   /**
    * Helper method to parse eaets from strings
    * @param stringDate
    * @return
    */
   private static Date parseDate(String stringDate)
   {
      DateFormat df = CachingDateFormat.getDateFormat();
      df.setLenient(true);
      Date date;

      ParsePosition pp = new ParsePosition(0);
      date = df.parse(stringDate, pp);
      if ((pp.getIndex() < stringDate.length()) || (date == null))
      {
         date = new Date();
      }
      return date;

   }

   /**
    * Construct a preiod between the two given dates
    * 
    * @param start_in
    * @param end_in
    */
   public Duration(Date start_in, Date end_in)
   {
      boolean positive = true;
      Date start;
      Date end;
      if (start_in.before(end_in))
      {
         start = start_in;
         end = end_in;
         positive = true;
      }
      else
      {
         start = end_in;
         end = start_in;
         positive = false;
      }
      Calendar cstart = Calendar.getInstance();
      cstart.setTime(start);
      Calendar cend = Calendar.getInstance();
      cend.setTime(end);

      int millis = cend.get(Calendar.MILLISECOND) - cstart.get(Calendar.MILLISECOND);
      if (millis < 0)
      {
         millis += cstart.getActualMaximum(Calendar.MILLISECOND)+1;
      }
      cstart.add(Calendar.MILLISECOND, millis);

      int seconds = cend.get(Calendar.SECOND) - cstart.get(Calendar.SECOND);
      if (seconds < 0)
      {
         seconds += cstart.getActualMaximum(Calendar.SECOND)+1;
      }
      cstart.add(Calendar.SECOND, seconds);

      int minutes = cend.get(Calendar.MINUTE) - cstart.get(Calendar.MINUTE);
      if (minutes < 0)
      {
         minutes += cstart.getActualMaximum(Calendar.MINUTE)+1;
      }
      cstart.add(Calendar.MINUTE, minutes);

      int hours = cend.get(Calendar.HOUR_OF_DAY) - cstart.get(Calendar.HOUR_OF_DAY);
      if (hours < 0)
      {
         hours += cstart.getActualMaximum(Calendar.HOUR_OF_DAY)+1;
      }
      cstart.add(Calendar.HOUR_OF_DAY, hours);

      int days = cend.get(Calendar.DAY_OF_MONTH) - cstart.get(Calendar.DAY_OF_MONTH);
      if (days < 0)
      {
         days += cstart.getActualMaximum(Calendar.DAY_OF_MONTH)+1;
      }
      cstart.add(Calendar.DAY_OF_MONTH, days);

      int months = cend.get(Calendar.MONTH) - cstart.get(Calendar.MONTH);
      if (months < 0)
      {
         months += cstart.getActualMaximum(Calendar.MONTH)+1;
      }
      cstart.add(Calendar.MONTH, months);

      int years = cend.get(Calendar.YEAR) - cstart.get(Calendar.YEAR);
      //cstart.add(Calendar.YEAR, years);

      m_positive = positive;
      m_years = years;
      m_months = months;
      m_days = days;
      m_hours = hours;
      m_mins = minutes;
      m_seconds = seconds;
      m_nanos = millis * 1000000;

   }

   /**
    * Construct a duration from months seconds and nanos
    * Checks sign and fixes up seconds and nano.
    * Treats year-month abd day-sec as separate chunks
    */

   public Duration(boolean positive_in, long months_in, long seconds_in, long nanos_in)
   {

      boolean positive = positive_in;
      long months = months_in;
      long seconds = seconds_in + nanos_in / 1000000000;
      long nanos = nanos_in % 1000000000;

      // Fix up seconds and nanos to be of the same sign

      if ((seconds > 0) && (nanos < 0))
      {
         seconds -= 1;
         nanos += 1000000000;
      }
      else if ((seconds < 0) && (nanos > 0))
      {
         seconds += 1;
         nanos -= 1000000000;
      }

      // seconds and nanos now the same sign - sum to test overall sign    

      if ((months < 0) && (seconds + nanos < 0))
      {
         // switch sign
         positive = !positive;
         months = -months;
         seconds = -seconds;
         nanos = -nanos;
      }
      else if ((months == 0) && (seconds + nanos < 0))
      {
         // switch sign
         positive = !positive;
         months = -months;
         seconds = -seconds;
         nanos = -nanos;
      }
      else if ((months > 0) && (seconds + nanos < 0))
      {
         throw new RuntimeException("Can not convert to period - incompatible signs for year_to_momth and day_to_second elements");
      }
      else if ((months < 0) && (seconds + nanos > 0))
      {
         throw new RuntimeException("Can not convert to period - incompatible signs for year_to_momth and day_to_second elements");
      }
      else
      {
         // All  +ve
      }

      m_positive = positive;
      m_years = (int) (months / 12);
      m_months = (int) (months % 12);

      m_days = (int) (seconds / (3600 * 24));
      seconds -= m_days * 3600 * 24;
      m_hours = (int) (seconds / 3600);
      seconds -= m_hours * 3600;
      m_mins = (int) (seconds / 60);
      seconds -= m_mins * 60;
      m_seconds = (int) seconds;
      m_nanos = (int) nanos;

   }

   
   // Duration arithmetic
   
   /**
    * Add two durations together
    */

   public Duration add(Duration add)
   {

      long months = (this.m_positive ? 1 : -1) * this.getTotalMonths() + (add.m_positive ? 1 : -1) * add.getTotalMonths();
      long seconds = (this.m_positive ? 1 : -1) * this.getTotalSeconds() + (add.m_positive ? 1 : -1) * add.getTotalSeconds();
      long nanos = (this.m_positive ? 1 : -1) * this.getTotalNanos() + (add.m_positive ? 1 : -1) * add.getTotalNanos();

      Duration result = new Duration(true, months, seconds, nanos);
      return result;
   }

   /**
    * Subtract one duration from another
    */

   public Duration subtract(Duration sub)
   {
      long months = (this.m_positive ? 1 : -1) * this.getTotalMonths() - (sub.m_positive ? 1 : -1) * sub.getTotalMonths();
      long seconds = (this.m_positive ? 1 : -1) * this.getTotalSeconds() - (sub.m_positive ? 1 : -1) * sub.getTotalSeconds();
      long nanos = (this.m_positive ? 1 : -1) * this.getTotalNanos() - (sub.m_positive ? 1 : -1) * sub.getTotalNanos();
      Duration result = new Duration(true, months, seconds, nanos);
      return result;
   }

   /**
    * Negate the duration
    */

   public Duration unaryMinus()
   {
      Duration result = new Duration(!this.m_positive, this.getTotalMonths(), this.getTotalSeconds(), this.getTotalNanos());
      return result;
   }

   /**
    * Divide the duration - if year-month drops the day-second part of the duration
    */

   public Duration divide(int d)
   {
      if (isYearToMonth())
      {
         long months = getTotalMonths();
         months /= d;
         Duration result = new Duration(m_positive, months, 0, 0);
         return result;
      }
      else
      {
         long seconds = getTotalSeconds();
         long nanos = (seconds * (1000000000 / d)) % 1000000000;
         nanos += getTotalNanos() / d;
         seconds /= d;
         Duration result = new Duration(m_positive, 0, seconds, nanos);
         return result;
      }
   }

   /**
    * Helper method to get the total number of months - year-month
    */

   private long getTotalMonths()
   {
      return m_years * 12 + m_months;
   }

   /**
    * Helper method to get the total number of seconds
    */

   private long getTotalSeconds()
   {
      return m_seconds + m_mins * 60 + m_hours * 3600 + m_days * 3600 * 24;
   }

   /**
    * Helper method to get the total number of nanos (does not include seconds_
    */

   private long getTotalNanos()
   {
      return m_nanos;
   }

   /**
    * Check if is year-month
    */

   public boolean isYearToMonth()
   {
      return (m_years != 0) || (m_months != 0);
   }

   /**
    * Check if is day-sec
    */

   public boolean isDayToSec()
   {
      return ((m_years == 0) && (m_months == 0));
   }

   /**
    * Check if it includes time
    */

   public boolean hasTime()
   {
      return (m_hours != 0) || (m_mins != 0) || (m_seconds != 0) || (m_nanos != 0);
   }

   /**
    * Extract the year to month part
    */

   public Duration getYearToMonth()
   {
      Duration result = new Duration(m_positive, getTotalMonths(), 0, 0);
      return result;
   }

   /**
    * Extract the day to sec part.
    */

   public Duration getDayToYear()
   {
      Duration result = new Duration(m_positive, 0, getTotalSeconds(), getTotalNanos());
      return result;
   }

   /**
    * Compare two durations
    */

   public int compareTo(Object o)
   {
      if (!(o instanceof Duration))
      {
         throw new RuntimeException("Can not compare Duration and " + o.getClass().getName());
      }

      Duration d = (Duration) o;
      if (this.m_positive != d.m_positive)
      {
         return (m_positive ? 1 : -1);
      }

      if (this.getTotalMonths() != d.getTotalMonths())
      {
         return (m_positive ? 1 : -1) * ((int) (this.getTotalMonths() - d.getTotalMonths()));
      }
      else if (this.getTotalSeconds() != d.getTotalSeconds())
      {
         return (m_positive ? 1 : -1) * ((int) (this.getTotalSeconds() - d.getTotalSeconds()));
      }
      else if (this.getTotalNanos() != d.getTotalNanos())
      {
         return (m_positive ? 1 : -1) * ((int) (this.getTotalNanos() - d.getTotalNanos()));
      }
      else
      {
         return 0;
      }
   }

   /**
    * @see java.lang.Object#equals(Object)
    */

   public boolean equals(Object o)
   {
      if (this == o)
         return true;
      if (!(o instanceof Duration))
         return false;
      Duration d = (Duration) o;
      return (this.m_positive == d.m_positive) && (this.getTotalMonths() == d.getTotalMonths()) && (this.getTotalSeconds() == d.getTotalSeconds()) && (this.getTotalNanos() == d.getTotalNanos());

   }

   /**
    * @see java.lang.Object#hashCode()
    */

   public int hashCode()
   {
      int hash = 17;
      hash = 37 * hash + (m_positive ? 1 : -1);
      hash = 37 * hash + (int) getTotalMonths();
      hash = 37 * hash + (int) getTotalSeconds();
      hash = 37 * hash + (int) getTotalNanos();
      return hash;
   }

   /**
    * Produce the XML Schema string
    * 
    * @see java.lang.Object#toString()
    */

   public String toString()
   {
      StringBuffer buffer = new StringBuffer(128);
      if (!m_positive)
      {
         buffer.append("-");
      }
      buffer.append("P");
      // Always include years as just P on its own is invalid
      buffer.append(m_years).append("Y");

      if (m_months != 0)
      {
         buffer.append(m_months).append("M");
      }
      if (m_days != 0)
      {
         buffer.append(m_days).append("D");
      }
      if (hasTime())
      {
         buffer.append("T");
         if (m_hours != 0)
         {
            buffer.append(m_hours).append("H");
         }
         if (m_mins != 0)
         {
            buffer.append(m_mins).append("M");
         }
         if ((m_seconds != 0) || (m_nanos != 0))
         {
            BigDecimal a = new BigDecimal(m_seconds);
            BigDecimal b = new BigDecimal(m_nanos);
            a = a.add(b.divide(new BigDecimal(1000000000), 9, BigDecimal.ROUND_HALF_EVEN));
            NumberFormat nf = NumberFormat.getInstance();
            buffer.append(nf.format(a));
            buffer.append("S");
         }

      }

      return buffer.toString();
   }

   /**
    * Format in human readable form
    * 
    * TODO: I18n
    */

   public String formattedString()
   {
      StringBuffer buffer = new StringBuffer(128);
      if (!m_positive)
      {
         buffer.append("-");
      }
      if (m_years != 0)
      {
         if (buffer.length() > 0)
            buffer.append(" ");
         buffer.append(m_years);
         buffer.append((m_years == 1) ? " Year" : " Years");

      }
      if (m_months != 0)
      {
         if (buffer.length() > 0)
            buffer.append(" ");
         buffer.append(m_months);
         buffer.append((m_months == 1) ? " Month" : " Months");
      }
      if (m_days != 0)
      {
         if (buffer.length() > 0)
            buffer.append(" ");
         buffer.append(m_days);
         buffer.append((m_days == 1) ? " Day" : " Days");
      }
      if (hasTime())
      {
         if (m_hours != 0)
         {
            if (buffer.length() > 0)
               buffer.append(" ");
            buffer.append(m_hours);
            buffer.append((m_hours == 1) ? " Hour" : " Hours");
         }
         if (m_mins != 0)
         {
            if (buffer.length() > 0)
               buffer.append(" ");
            buffer.append(m_mins);
            buffer.append((m_mins == 1) ? " Minute" : " Minutes");
         }
         if ((m_seconds != 0) || (m_nanos != 0))
         {
            if (buffer.length() > 0)
               buffer.append(" ");
            BigDecimal a = new BigDecimal(m_seconds);
            BigDecimal b = new BigDecimal(m_nanos);
            a = a.add(b.divide(new BigDecimal(1000000000), 9, BigDecimal.ROUND_HALF_EVEN));
            NumberFormat nf = NumberFormat.getInstance();
            String formatted = nf.format(a);
            buffer.append(formatted);
            buffer.append(formatted.equals("1") ? " Second" : " Seconds");
         }

      }

      return buffer.toString();
   }

   
   /**
    * TODO: Tests that should be moved into a unit test 
    * 
    * @param args
    */
   public static void main(String[] args)
   {
      Duration diff = new Duration("2002-04-02T01:01:01", "2003-03-01T00:00:00");
      System.out.println("Diff  " + diff);

      try
      {
         Duration test = new Duration("P");
         System.out.println("Just P" + test);
      }
      catch (RuntimeException e)
      {
         e.printStackTrace();
      }

      try
      {
         Duration test2 = new Duration("P Jones");
         System.out.println("P Jones" + test2);
      }
      catch (RuntimeException e)
      {
         e.printStackTrace();
      }

      try
      {
         Duration test2 = new Duration("P12Y Jones");
         System.out.println("P Jones" + test2);
      }
      catch (RuntimeException e)
      {
         e.printStackTrace();
      }

      try
      {
         Duration test = new Duration("PPPPPPPPPPPPPP");
         System.out.println("Just many P" + test);
      }
      catch (RuntimeException e)
      {
         e.printStackTrace();
      }

      try
      {
         Duration test = new Duration("PY");
         System.out.println("PY" + test);
      }
      catch (RuntimeException e)
      {
         e.printStackTrace();
      }

      try
      {
         Duration test = new Duration("PM");
         System.out.println("PM" + test);
      }
      catch (RuntimeException e)
      {
         e.printStackTrace();
      }

      try
      {
         Duration test = new Duration("PP");
         System.out.println("PP" + test);
      }
      catch (RuntimeException e)
      {
         e.printStackTrace();
      }

      Date now = new Date();
      Calendar c = Calendar.getInstance();
      c.setTime(now);
      c.add(Calendar.YEAR, -1);
      c.add(Calendar.MONTH, +2);
      c.add(Calendar.DAY_OF_MONTH, -3);
      c.add(Calendar.HOUR_OF_DAY, +4);
      c.add(Calendar.MINUTE, -5);
      c.add(Calendar.SECOND, +6);
      c.add(Calendar.MILLISECOND, -7);

      diff = new Duration(c.getTime(), now);
      System.out.println("V:  " + diff);

      Duration diff2 = new Duration(now, c.getTime());
      System.out.println("V:  " + diff2);

      Duration a1 = new Duration("P2Y6M");
      Duration a2 = new Duration("P1DT2H3M1.5S");

      Duration d = new Duration("P2Y6M5DT12H35M30.100S");
      System.out.println("V:  " + d);
      System.out.println("F:  " + d.formattedString());
      System.out.println(" D: " + d.divide(2));
      System.out.println(" +: " + d.add(a1));
      System.out.println(" +: " + d.add(a1.add(a2)));
      d = new Duration("P1DT2H3M1.5S");
      System.out.println("V:  " + d);
      System.out.println("F:  " + d.formattedString());
      System.out.println(" D: " + d.divide(2));
      System.out.println(" +: " + d.add(a1));
      System.out.println(" +: " + d.add(a1.add(a2)));
      d = new Duration("PT1.5S");
      System.out.println("V:  " + d);
      System.out.println("F:  " + d.formattedString());
      System.out.println(" D: " + d.divide(2));
      System.out.println(" +: " + d.add(a1));
      System.out.println(" +: " + d.add(a1.add(a2)));
      d = new Duration("P20M");
      System.out.println("V:  " + d);
      System.out.println("F:  " + d.formattedString());
      System.out.println(" D: " + d.divide(2));
      System.out.println(" +: " + d.add(a1));
      System.out.println(" +: " + d.add(a1.add(a2)));
      d = new Duration("P0Y20M0D");
      System.out.println("V:  " + d);
      System.out.println("F:  " + d.formattedString());
      System.out.println(" D: " + d.divide(2));
      System.out.println(" +: " + d.add(a1));
      System.out.println(" +: " + d.add(a1.add(a2)));
      d = new Duration("-P60D");
      System.out.println("V:  " + d);
      System.out.println("F:  " + d.formattedString());
      System.out.println(" D: " + d.divide(10));
      System.out.println(" +: " + d.add(a2));
      //System.out.println(" +: " + d.add(a1));

   }
}
