/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.util;

import org.alfresco.service.cmr.search.IntervalSet;
import org.springframework.extensions.surf.util.I18NUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Moved from Solr4QueryParser
 */
public class SearchDateConversion
{

    public static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");
    /**
     *
     * @param dateString
     * @return Pair<Date, Integer>
     */
    public static Pair<Date, Integer> parseDateString(String dateString)
    {
        try
        {
            Pair<Date, Integer> result = CachingDateFormat.lenientParse(dateString, Calendar.YEAR);
            return result;
        } catch (java.text.ParseException e)
        {
            SimpleDateFormat oldDf = CachingDateFormat.getDateFormat();
            try
            {
                Date date = oldDf.parse(dateString);
                return new Pair<Date, Integer>(date, Calendar.SECOND);
            } catch (java.text.ParseException ee)
            {
                if (dateString.equalsIgnoreCase("min"))
                {
                    Calendar cal = Calendar.getInstance(I18NUtil.getLocale());
                    cal.set(Calendar.YEAR, cal.getMinimum(Calendar.YEAR));
                    cal.set(Calendar.DAY_OF_YEAR, cal.getMinimum(Calendar.DAY_OF_YEAR));
                    cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
                    cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
                    cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
                    cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
                    return new Pair<Date, Integer>(cal.getTime(), Calendar.MILLISECOND);
                } else if (dateString.equalsIgnoreCase("now"))
                {
                    return new Pair<Date, Integer>(new Date(), Calendar.MILLISECOND);
                } else if (dateString.equalsIgnoreCase("today"))
                {
                    Calendar cal = Calendar.getInstance(I18NUtil.getLocale());
                    cal.setTime(new Date());
                    cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
                    cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
                    cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
                    cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
                    return new Pair<Date, Integer>(cal.getTime(), Calendar.DAY_OF_MONTH);
                } else if (dateString.equalsIgnoreCase("max"))
                {
                    Calendar cal = Calendar.getInstance(I18NUtil.getLocale());
                    cal.set(Calendar.YEAR, cal.getMaximum(Calendar.YEAR));
                    cal.set(Calendar.DAY_OF_YEAR, cal.getMaximum(Calendar.DAY_OF_YEAR));
                    cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
                    cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MINUTE));
                    cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
                    cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
                    return new Pair<Date, Integer>(cal.getTime(), Calendar.MILLISECOND);
                } else
                {
                    return null; // delegate to SOLR date parsing
                }
            }
        }
    }

    /**
     * @param dateAndResolution
     * @return String date
     */
    public static String getDateEnd(Pair<Date, Integer> dateAndResolution)
    {
        Calendar cal = Calendar.getInstance(I18NUtil.getLocale());
        cal.setTime(dateAndResolution.getFirst());
        switch (dateAndResolution.getSecond())
        {
            case Calendar.YEAR:
                cal.set(Calendar.MONTH, cal.getActualMaximum(Calendar.MONTH));
            case Calendar.MONTH:
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            case Calendar.DAY_OF_MONTH:
                cal.set(Calendar.HOUR_OF_DAY, cal.getActualMaximum(Calendar.HOUR_OF_DAY));
            case Calendar.HOUR_OF_DAY:
                cal.set(Calendar.MINUTE, cal.getActualMaximum(Calendar.MINUTE));
            case Calendar.MINUTE:
                cal.set(Calendar.SECOND, cal.getActualMaximum(Calendar.SECOND));
            case Calendar.SECOND:
                cal.set(Calendar.MILLISECOND, cal.getActualMaximum(Calendar.MILLISECOND));
            case Calendar.MILLISECOND:
            default:
        }
        SimpleDateFormat formatter = CachingDateFormat.getSolrDatetimeFormat();
        formatter.setTimeZone(UTC_TIMEZONE);
        return formatter.format(cal.getTime());
    }

    /**
     * @param dateAndResolution
     * @return String date
     */
    public static String getDateStart(Pair<Date, Integer> dateAndResolution)
    {
        Calendar cal = Calendar.getInstance(I18NUtil.getLocale());
        cal.setTime(dateAndResolution.getFirst());
        switch (dateAndResolution.getSecond())
        {
            case Calendar.YEAR:
                cal.set(Calendar.MONTH, cal.getActualMinimum(Calendar.MONTH));
            case Calendar.MONTH:
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
            case Calendar.DAY_OF_MONTH:
                cal.set(Calendar.HOUR_OF_DAY, cal.getActualMinimum(Calendar.HOUR_OF_DAY));
            case Calendar.HOUR_OF_DAY:
                cal.set(Calendar.MINUTE, cal.getActualMinimum(Calendar.MINUTE));
            case Calendar.MINUTE:
                cal.set(Calendar.SECOND, cal.getActualMinimum(Calendar.SECOND));
            case Calendar.SECOND:
                cal.set(Calendar.MILLISECOND, cal.getActualMinimum(Calendar.MILLISECOND));
            case Calendar.MILLISECOND:
            default:
        }
        SimpleDateFormat formatter = CachingDateFormat.getSolrDatetimeFormat();
        formatter.setTimeZone(UTC_TIMEZONE);
        return formatter.format(cal.getTime());
    }

    public static IntervalSet parseDateInterval(IntervalSet theSet, boolean isDate)
    {
        if (isDate)
        {
            Pair<Date, Integer> dateAndResolution1 = parseDateString(theSet.getStart());
            Pair<Date, Integer> dateAndResolution2 = parseDateString(theSet.getEnd());
            String start = dateAndResolution1 == null ? theSet.getStart()
                        : (theSet.isStartInclusive() ? getDateStart(dateAndResolution1) : getDateEnd(dateAndResolution1));
            String end = dateAndResolution2 == null ?  theSet.getEnd()
                        : (theSet.isEndInclusive() ? getDateEnd(dateAndResolution2) : getDateStart(dateAndResolution2));
            return new IntervalSet(start, end, theSet.getLabel(), theSet.isStartInclusive(), theSet.isEndInclusive());

        }
        return theSet;
    }
}
