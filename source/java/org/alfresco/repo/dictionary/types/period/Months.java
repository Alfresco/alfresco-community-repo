package org.alfresco.repo.dictionary.types.period;

import java.util.Calendar;

/**
 * Months
 * @author andyh
 *
 */
public class Months extends  AbstractCalendarPeriodProvider
{
    /**
     * 
     */
    public static final String PERIOD_TYPE = "month"; 

    public String getPeriodType()
    {
        return PERIOD_TYPE;
    }

    @Override
    public void add(Calendar calendar, int value)
    {
        calendar.add(Calendar.MONTH, value);
    }

}
