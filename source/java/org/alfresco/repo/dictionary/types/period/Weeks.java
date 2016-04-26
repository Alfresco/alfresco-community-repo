package org.alfresco.repo.dictionary.types.period;

import java.util.Calendar;
/**
 * Weeks
 * @author andyh
 *
 */
public class Weeks extends AbstractCalendarPeriodProvider
{
    /**
     * 
     */
    public static final String PERIOD_TYPE = "week"; 

    public String getPeriodType()
    {
        return PERIOD_TYPE;
    }

    @Override
    public void add(Calendar calendar, int value)
    {
        calendar.add(Calendar.WEEK_OF_YEAR, value);
    }
}
