/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.util;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import org.alfresco.error.AlfrescoRuntimeException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.extensions.surf.exception.PlatformRuntimeException;

/**
 * Provides <b>thread safe</b> means of obtaining a cached date formatter.
 * <p>
 * The cached string-date mappings are stored in a <tt>WeakHashMap</tt>.
 * 
 * @see java.text.DateFormat#setLenient(boolean)
 * 
 * @author Derek Hulley
 */
public class CachingDateFormat extends SimpleDateFormat
{
    private static final long serialVersionUID = 3258415049197565235L;

    /** <pre> yyyy-MM-dd'T'HH:mm:ss </pre> */
    public static final String FORMAT_FULL_GENERIC = "yyyy-MM-dd'T'HH:mm:ss";

    /** <pre> yyyy-MM-dd'T'HH:mm:ss </pre> */
    public static final String FORMAT_CMIS_SQL = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    
    public static final String FORMAT_SOLR = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

    public static final StringAndResolution[] LENIENT_FORMATS;
    
    
    static
    {
        ArrayList<StringAndResolution> list = new ArrayList<StringAndResolution> ();
        list.add( new StringAndResolution("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Calendar.MILLISECOND));
        list.add( new StringAndResolution("yyyy-MM-dd'T'HH:mm:ss.SSS",  Calendar.MILLISECOND));
        list.add( new StringAndResolution("yyyy-MM-dd'T'HH:mm:ssZ", Calendar.SECOND));
        list.add( new StringAndResolution("yyyy-MM-dd'T'HH:mm:ss", Calendar.SECOND));
        list.add( new StringAndResolution("yyyy-MM-dd'T'HH:mmZ", Calendar.MINUTE));
        list.add( new StringAndResolution("yyyy-MM-dd'T'HH:mm", Calendar.MINUTE));
        list.add( new StringAndResolution("yyyy-MM-dd'T'HHZ",  Calendar.HOUR_OF_DAY));
        list.add( new StringAndResolution("yyyy-MM-dd'T'HH",  Calendar.HOUR_OF_DAY));
        list.add( new StringAndResolution("yyyy-MM-dd'T'Z",  Calendar.DAY_OF_MONTH));
        list.add( new StringAndResolution("yyyy-MM-dd'T'",  Calendar.DAY_OF_MONTH));
        list.add( new StringAndResolution("yyyy-MM-ddZ", Calendar.DAY_OF_MONTH));
        list.add( new StringAndResolution("yyyy-MM-dd", Calendar.DAY_OF_MONTH));
        list.add( new StringAndResolution("yyyy-MMZ", Calendar.MONTH));
        list.add( new StringAndResolution("yyyy-MM", Calendar.MONTH));
        // year would duplicate :-) and eat stuff
        list.add( new StringAndResolution( "yyyy-MMM-dd'T'HH:mm:ss.SSSZ", Calendar.MILLISECOND));
        list.add( new StringAndResolution( "yyyy-MMM-dd'T'HH:mm:ss.SSS", Calendar.MILLISECOND));
        list.add( new StringAndResolution( "yyyy-MMM-dd'T'HH:mm:ssZ", Calendar.SECOND));
        list.add( new StringAndResolution( "yyyy-MMM-dd'T'HH:mm:ss", Calendar.SECOND));
        list.add( new StringAndResolution( "yyyy-MMM-dd'T'HH:mmZ", Calendar.MINUTE));
        list.add( new StringAndResolution( "yyyy-MMM-dd'T'HH:mm", Calendar.MINUTE));
        list.add( new StringAndResolution( "yyyy-MMM-dd'T'HHZ", Calendar.HOUR_OF_DAY));
        list.add( new StringAndResolution( "yyyy-MMM-dd'T'HH", Calendar.HOUR_OF_DAY));
        list.add( new StringAndResolution( "yyyy-MMM-dd'T'Z",Calendar.DAY_OF_MONTH));
        list.add( new StringAndResolution( "yyyy-MMM-dd'T'",Calendar.DAY_OF_MONTH));
        list.add( new StringAndResolution( "yyyy-MMM-ddZ", Calendar.DAY_OF_MONTH));
        list.add( new StringAndResolution( "yyyy-MMM-dd", Calendar.DAY_OF_MONTH));
        list.add( new StringAndResolution( "yyyy-MMMZ", Calendar.MONTH));
        list.add( new StringAndResolution( "yyyy-MMM", Calendar.MONTH));
        list.add( new StringAndResolution("yyyyZ", Calendar.YEAR));
        list.add( new StringAndResolution("yyyy", Calendar.YEAR));
       


        LENIENT_FORMATS = list.toArray(new StringAndResolution[]{});
    }
    
    /** <pre> yyyy-MM-dd </pre> */
    public static final String FORMAT_DATE_GENERIC = "yyyy-MM-dd";

    /** <pre> HH:mm:ss </pre> */
    public static final String FORMAT_TIME_GENERIC = "HH:mm:ss";

    private static ThreadLocal<SimpleDateFormat> s_localDateFormat = new ThreadLocal<SimpleDateFormat>();
    
    private static ThreadLocal<SimpleDateFormat> s_localDateOnlyFormat = new ThreadLocal<SimpleDateFormat>();

    private static ThreadLocal<SimpleDateFormat> s_localTimeOnlyFormat = new ThreadLocal<SimpleDateFormat>();
    
    private static ThreadLocal<SimpleDateFormat> s_localCmisSqlDatetime = new ThreadLocal<SimpleDateFormat>();
    
    private static ThreadLocal<SimpleDateFormat> s_localSolrDatetime = new ThreadLocal<SimpleDateFormat>();
    
    private static ThreadLocal<SimpleDateFormatAndResolution[]> s_lenientParsers = new ThreadLocal<SimpleDateFormatAndResolution[]>();

    transient private Map<String, Date> cacheDates = new WeakHashMap<String, Date>(89);

    private CachingDateFormat(String format)
    {
        super(format);
    }

    public String toString()
    {
        return this.toPattern();
    }

    /**
     * @param length
     *            the type of date format, e.g. {@link CachingDateFormat#LONG }
     * @param locale
     *            the <code>Locale</code> that will be used to determine the
     *            date pattern
     * 
     * @see #getDateFormat(String, boolean)
     * @see CachingDateFormat#SHORT
     * @see CachingDateFormat#MEDIUM
     * @see CachingDateFormat#LONG
     * @see CachingDateFormat#FULL
     */
    public static SimpleDateFormat getDateFormat(int length, Locale locale, boolean lenient)
    {
        SimpleDateFormat dateFormat = (SimpleDateFormat) CachingDateFormat.getDateInstance(length, locale);
        // extract the format string
        String pattern = dateFormat.toPattern();
        // we have a pattern to use
        return getDateFormat(pattern, lenient);
    }

    /**
     * @param dateLength
     *            the type of date format, e.g. {@link CachingDateFormat#LONG }
     * @param timeLength
     *            the type of time format, e.g. {@link CachingDateFormat#LONG }
     * @param locale
     *            the <code>Locale</code> that will be used to determine the
     *            date pattern
     * 
     * @see #getDateFormat(String, boolean)
     * @see CachingDateFormat#SHORT
     * @see CachingDateFormat#MEDIUM
     * @see CachingDateFormat#LONG
     * @see CachingDateFormat#FULL
     */
    public static SimpleDateFormat getDateTimeFormat(int dateLength, int timeLength, Locale locale, boolean lenient)
    {
        SimpleDateFormat dateFormat = (SimpleDateFormat) CachingDateFormat.getDateTimeInstance(dateLength, timeLength, locale);
        // extract the format string
        String pattern = dateFormat.toPattern();
        // we have a pattern to use
        return getDateFormat(pattern, lenient);
    }

    /**
     * @param pattern
     *            the conversion pattern to use
     * @param lenient
     *            true to allow the parser to extract the date in conceivable
     *            manner
     * @return Returns a conversion-cacheing formatter for the given pattern,
     *         but the instance itself is not cached
     */
    public static SimpleDateFormat getDateFormat(String pattern, boolean lenient)
    {
        // create an alfrescoDateFormat for cacheing purposes
        SimpleDateFormat dateFormat = new CachingDateFormat(pattern);
        // set leniency
        dateFormat.setLenient(lenient);
        // done
        return dateFormat;
    }

    /**
     * @return Returns a thread-safe formatter for the generic date/time format
     * 
     * @see #FORMAT_FULL_GENERIC
     */
    public static SimpleDateFormat getDateFormat()
    {
        if (s_localDateFormat.get() != null)
        {
            return s_localDateFormat.get();
        }

        CachingDateFormat formatter = new CachingDateFormat(FORMAT_FULL_GENERIC);
        // it must be strict
        formatter.setLenient(false);
        // put this into the threadlocal object
        s_localDateFormat.set(formatter);
        // done
        return s_localDateFormat.get();
    }
    
    /**
     * @return Returns a thread-safe formatter for the cmis sql datetime format
     */
    public static SimpleDateFormat getCmisSqlDatetimeFormat()
    {
        if (s_localCmisSqlDatetime.get() != null)
        {
            return s_localCmisSqlDatetime.get();
        }

        CachingDateFormat formatter = new CachingDateFormat(FORMAT_CMIS_SQL);
        // it must be strict
        formatter.setLenient(false);
        // put this into the threadlocal object
        s_localCmisSqlDatetime.set(formatter);
        // done
        return s_localCmisSqlDatetime.get();
    }
    
    /**
     * @return Returns a thread-safe formatter for the cmis sql datetime format
     */
    public static SimpleDateFormat getSolrDatetimeFormat()
    {
        if (s_localSolrDatetime.get() != null)
        {
            return s_localSolrDatetime.get();
        }

        CachingDateFormat formatter = new CachingDateFormat(FORMAT_SOLR);
        // it must be strict
        formatter.setLenient(false);
        // put this into the threadlocal object
        s_localSolrDatetime.set(formatter);
        // done
        return s_localSolrDatetime.get();
    }

    /**
     * @return Returns a thread-safe formatter for the generic date format
     * 
     * @see #FORMAT_DATE_GENERIC
     */
    public static SimpleDateFormat getDateOnlyFormat()
    {
        if (s_localDateOnlyFormat.get() != null)
        {
            return s_localDateOnlyFormat.get();
        }

        CachingDateFormat formatter = new CachingDateFormat(FORMAT_DATE_GENERIC);
        // it must be strict
        formatter.setLenient(false);
        // put this into the threadlocal object
        s_localDateOnlyFormat.set(formatter);
        // done
        return s_localDateOnlyFormat.get();
    }

    /**
     * @return Returns a thread-safe formatter for the generic time format
     * 
     * @see #FORMAT_TIME_GENERIC
     */
    public static SimpleDateFormat getTimeOnlyFormat()
    {
        if (s_localTimeOnlyFormat.get() != null)
        {
            return s_localTimeOnlyFormat.get();
        }

        CachingDateFormat formatter = new CachingDateFormat(FORMAT_TIME_GENERIC);
        // it must be strict
        formatter.setLenient(false);
        // put this into the threadlocal object
        s_localTimeOnlyFormat.set(formatter);
        // done
        return s_localTimeOnlyFormat.get();
    }

    /**
     * Parses and caches date strings.
     * 
     * @see java.text.DateFormat#parse(java.lang.String,
     *      java.text.ParsePosition)
     */
    public Date parse(String text, ParsePosition pos)
    {
        Date cached = cacheDates.get(text);
        if (cached == null)
        {
            Date date = super.parse(text, pos);
            if ((date != null) && (pos.getIndex() == text.length()))
            {
                cacheDates.put(text, date);
                Date clonedDate = (Date) date.clone();
                return clonedDate;
            }
            else
            {
                return date;
            }
        }
        else
        {
            pos.setIndex(text.length());
            Date clonedDate = (Date) cached.clone();
            return clonedDate;
        }
    }
    
    public static Pair<Date, Integer> lenientParse(String text, int minimumResolution) throws ParseException
    {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        try
        {
            Date parsed = fmt.parseDateTime(text).toDate();
            return new Pair<Date, Integer>(parsed, Calendar.MILLISECOND);    
        }
        catch(IllegalArgumentException e)
        {
           
        }
        
        SimpleDateFormatAndResolution[] formatters = getLenientFormatters();
        for(SimpleDateFormatAndResolution formatter : formatters)
        {
            if(formatter.resolution >= minimumResolution)
            {
                ParsePosition pp = new ParsePosition(0);
                Date parsed = formatter.simpleDateFormat.parse(text, pp);
                if ((pp.getIndex() < text.length()) || (parsed == null))
                {
                    continue;
                }
                return new Pair<Date, Integer>(parsed, formatter.resolution);
            }
        }
        
        throw new ParseException("Unknown date format", 0);
        
        
    }
    
    public static SimpleDateFormatAndResolution[] getLenientFormatters()
    {
        if (s_lenientParsers.get() != null)
        {
            return s_lenientParsers.get();
        }

        int i = 0;
        SimpleDateFormatAndResolution[] formatters = new SimpleDateFormatAndResolution[LENIENT_FORMATS.length];
        for(StringAndResolution format : LENIENT_FORMATS)
        {
            CachingDateFormat formatter = new CachingDateFormat(format.string);
            // it must be strict
            formatter.setLenient(false);
            formatters[i++] = new SimpleDateFormatAndResolution(formatter, format.resolution);
        }
       
        // put this into the threadlocal object
        s_lenientParsers.set(formatters);
        // done
        return s_lenientParsers.get();
    }
    
    public static class StringAndResolution
    {
        String string;
        int resolution;
        
        /**
         * @return the resolution
         */
        public int getResolution()
        {
            return resolution;
        }

        /**
         * @param resolution the resolution to set
         */
        public void setResolution(int resolution)
        {
            this.resolution = resolution;
        }

        StringAndResolution(String string, int resolution)
        {
            this.string = string;
            this.resolution = resolution;
        }
    }
    
    public static class SimpleDateFormatAndResolution
    {
        SimpleDateFormat simpleDateFormat;
        int resolution;
        
        SimpleDateFormatAndResolution(SimpleDateFormat simpleDateFormat, int resolution)
        {
            this.simpleDateFormat = simpleDateFormat;
            this.resolution = resolution;
        }

        /**
         * @return the simpleDateFormat
         */
        public SimpleDateFormat getSimpleDateFormat()
        {
            return simpleDateFormat;
        }

        /**
         * @return the resolution
         */
        public int getResolution()
        {
            return resolution;
        }
        
    }
}
