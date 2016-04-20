package org.alfresco.repo.dictionary.types.period;

import java.util.Calendar;

/**
 * Years
 * @author andyh
 *
 */
public class Years extends AbstractCalendarPeriodProvider
{
    /**
     * 
     */
    public static final String PERIOD_TYPE = "year"; 

    public String getPeriodType()
    {
        return PERIOD_TYPE;
    }

    @Override
    public void add(Calendar calendar, int value)
    {
        calendar.add(Calendar.YEAR, value);
    }
}
