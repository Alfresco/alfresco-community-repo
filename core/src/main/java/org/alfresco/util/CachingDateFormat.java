/*
 * Copyright (C) 2005-2018 Alfresco Software Limited.
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

import static java.util.Arrays.stream;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Provides <b>thread safe</b> means of obtaining a cached date formatter.
 * <p>
 * The cached string-date mappings are stored in a <tt>WeakHashMap</tt>.
 * 
 * @see java.text.DateFormat#setLenient(boolean)
 * 
 * @author Derek Hulley
 * @author Andrea Gazzarini
 */
public class CachingDateFormat extends SimpleDateFormat
{
    private static final long serialVersionUID = 3258415049197565235L;
    public static final String UTC = "UTC";

    public static final String FORMAT_FULL_GENERIC = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String FORMAT_CMIS_SQL = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String FORMAT_SOLR = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    public static final String UTC_WITHOUT_MSECS = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String FORMAT_DATE_GENERIC = "yyyy-MM-dd";
    public static final String FORMAT_TIME_GENERIC = "HH:mm:ss";

    public static final StringAndResolution[] LENIENT_FORMATS =
    {
        new StringAndResolution("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Calendar.MILLISECOND),
        new StringAndResolution("yyyy-MM-dd'T'HH:mm:ss.SSS",  Calendar.MILLISECOND),
        new StringAndResolution("yyyy-MM-dd'T'HH:mm:ssZ", Calendar.SECOND),
        new StringAndResolution("yyyy-MM-dd'T'HH:mm:ss", Calendar.SECOND),
        new StringAndResolution("yyyy-MM-dd'T'HH:mmZ", Calendar.MINUTE),
        new StringAndResolution("yyyy-MM-dd'T'HH:mm", Calendar.MINUTE),
        new StringAndResolution("yyyy-MM-dd'T'HHZ",  Calendar.HOUR_OF_DAY),
        new StringAndResolution("yyyy-MM-dd'T'HH",  Calendar.HOUR_OF_DAY),
        new StringAndResolution("yyyy-MM-dd'T'Z",  Calendar.DAY_OF_MONTH),
        new StringAndResolution("yyyy-MM-dd'T'",  Calendar.DAY_OF_MONTH),
        new StringAndResolution("yyyy-MM-ddZ", Calendar.DAY_OF_MONTH),
        new StringAndResolution("yyyy-MM-dd", Calendar.DAY_OF_MONTH),
        new StringAndResolution("yyyy-MMZ", Calendar.MONTH),
        new StringAndResolution("yyyy-MM", Calendar.MONTH),
        new StringAndResolution( "yyyy-MMM-dd'T'HH:mm:ss.SSSZ", Calendar.MILLISECOND),
        new StringAndResolution( "yyyy-MMM-dd'T'HH:mm:ss.SSS", Calendar.MILLISECOND),
        new StringAndResolution( "yyyy-MMM-dd'T'HH:mm:ssZ", Calendar.SECOND),
        new StringAndResolution( "yyyy-MMM-dd'T'HH:mm:ss", Calendar.SECOND),
        new StringAndResolution( "yyyy-MMM-dd'T'HH:mmZ", Calendar.MINUTE),
        new StringAndResolution( "yyyy-MMM-dd'T'HH:mm", Calendar.MINUTE),
        new StringAndResolution( "yyyy-MMM-dd'T'HHZ", Calendar.HOUR_OF_DAY),
        new StringAndResolution( "yyyy-MMM-dd'T'HH", Calendar.HOUR_OF_DAY),
        new StringAndResolution( "yyyy-MMM-dd'T'Z",Calendar.DAY_OF_MONTH),
        new StringAndResolution( "yyyy-MMM-dd'T'",Calendar.DAY_OF_MONTH),
        new StringAndResolution( "yyyy-MMM-ddZ", Calendar.DAY_OF_MONTH),
        new StringAndResolution( "yyyy-MMM-dd", Calendar.DAY_OF_MONTH),
        new StringAndResolution( "yyyy-MMMZ", Calendar.MONTH),
        new StringAndResolution( "yyyy-MMM", Calendar.MONTH),
        new StringAndResolution("yyyyZ", Calendar.YEAR),
        new StringAndResolution("yyyy", Calendar.YEAR)
    };

    static ThreadLocal<SimpleDateFormat> S_LOCAL_DATE_FORMAT = ThreadLocal.withInitial(() -> newDateFormat(FORMAT_FULL_GENERIC));
    
    static ThreadLocal<SimpleDateFormat> S_LOCAL_DATEONLY_FORMAT = ThreadLocal.withInitial(() -> newDateFormat(FORMAT_DATE_GENERIC));

    static ThreadLocal<SimpleDateFormat> S_LOCAL_TIMEONLY_FORMAT = ThreadLocal.withInitial(() -> newDateFormat(FORMAT_TIME_GENERIC));
    
    static ThreadLocal<SimpleDateFormat> S_LOCAL_CMIS_SQL_DATETIME = ThreadLocal.withInitial(() -> newDateFormat(FORMAT_CMIS_SQL));
    
    static ThreadLocal<SimpleDateFormat> S_LOCAL_SOLR_DATETIME = ThreadLocal.withInitial(()->
    {
        CachingDateFormat formatter = newDateFormatWithLocale(FORMAT_SOLR, Locale.ENGLISH);
        /*
            SEARCH-1263
            Apache Solr only supports the ISO 8601 date format:
            UTC and western locale are mandatory (only Arabic numerals (0123456789) are supported)
        */
        formatter.setTimeZone(TimeZone.getTimeZone(UTC));
        return formatter;
    });

    static ThreadLocal<SimpleDateFormat> S_UTC_DATETIME_WITHOUT_MSECS = ThreadLocal.withInitial(() ->
    {
        CachingDateFormat formatter = newDateFormatWithLocale(UTC_WITHOUT_MSECS, Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone(UTC));
        return formatter;
    });

    static ThreadLocal<SimpleDateFormatAndResolution[]> S_LENIENT_PARSERS =
            ThreadLocal.withInitial(() ->
                stream(LENIENT_FORMATS)
                    .map(format -> {
                        CachingDateFormat formatter = new CachingDateFormat(format.string);
                        formatter.setLenient(false);
                        return new SimpleDateFormatAndResolution(formatter, format.resolution); })
                    .toArray(SimpleDateFormatAndResolution[]::new));

    private Map<String, Date> cacheDates = new WeakHashMap<>(89);

    private CachingDateFormat(String pattern, Locale locale)
    {
        super(pattern, locale);
    }

    private CachingDateFormat(String pattern)
    {
        super(pattern);
    }

    @Override
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
     * Returns a thread-safe formatter for the generic date/time format.
     *
     * @see #FORMAT_FULL_GENERIC
     * @return a thread-safe formatter for the generic date/time format.
     */
    public static SimpleDateFormat getDateFormat()
    {
        return S_LOCAL_DATE_FORMAT.get();
    }
    
    /**
     * Returns a thread-safe formatter for the cmis sql datetime format.
     *
     * @see #FORMAT_CMIS_SQL
     * @return a thread-safe formatter for the cmis sql datetime format.
     */
    public static SimpleDateFormat getCmisSqlDatetimeFormat()
    {
        return S_LOCAL_CMIS_SQL_DATETIME.get();
    }
    
    /**
     * Returns a thread-safe formatter for the Solr ISO 8601 datetime format (without the msecs part).
     *
     * @see #UTC_WITHOUT_MSECS
     * @return Returns a thread-safe formatter for the Solr ISO 8601 datetime format (without the msecs part).
     */
    public static SimpleDateFormat getSolrDatetimeFormatWithoutMsecs()
    {
        return S_UTC_DATETIME_WITHOUT_MSECS.get();
    }

    /**
     * Returns a thread-safe formatter for the Solr ISO 8601 datetime format.
     *
     * @see #FORMAT_SOLR
     * @return a thread-safe formatter for the Solr ISO 8601 datetime format
     */
    public static SimpleDateFormat getSolrDatetimeFormat()
    {
        return S_LOCAL_SOLR_DATETIME.get();
    }

    /**
     * @return Returns a thread-safe formatter for the generic date format
     * 
     * @see #FORMAT_DATE_GENERIC
     */
    public static SimpleDateFormat getDateOnlyFormat()
    {
        return S_LOCAL_DATEONLY_FORMAT.get();
    }

    /**
     * Returns a thread-safe formatter for the generic time format.
     *
     * @see #FORMAT_TIME_GENERIC
     * @return a thread-safe formatter for the generic time format.
     */
    public static SimpleDateFormat getTimeOnlyFormat()
    {
        return S_LOCAL_TIMEONLY_FORMAT.get();
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
                return (Date) date.clone();
            }
            else
            {
                return date;
            }
        }
        else
        {
            pos.setIndex(text.length());
            return (Date) cached.clone();
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
           // Nothing to be done here
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
        return S_LENIENT_PARSERS.get();
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

    /**
     * Creates a new non-lenient {@link CachingDateFormat} instance.
     *
     * @param pattern the date / datetime pattern.
     * @return new non-lenient {@link CachingDateFormat} instance.
     */
    private static CachingDateFormat newDateFormat(String pattern)
    {
        CachingDateFormat formatter = new CachingDateFormat(pattern);
        formatter.setLenient(false);
        return formatter;
    }

    /**
     * Creates a new non-lenient localised {@link CachingDateFormat} instance.
     *
     * @param pattern the date / datetime pattern.
     * @param locale the locale.
     * @return new non-lenient {@link CachingDateFormat} instance.
     */
    private static CachingDateFormat newDateFormatWithLocale(String pattern, Locale locale)
    {
        CachingDateFormat formatter = new CachingDateFormat(pattern, locale);
        formatter.setLenient(false);
        return formatter;
    }
}
