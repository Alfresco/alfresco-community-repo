package org.alfresco.repo.dictionary.types.period;

import java.util.Calendar;


/**
 * Day based periods
 * @author andyh
 *
 */
public class Days extends AbstractCalendarPeriodProvider
{
    /**
     * 
     */
    public static final String PERIOD_TYPE = "day"; 

    public String getPeriodType()
    {
        return PERIOD_TYPE;
    }

    @Override
    public void add(Calendar calendar, int value)
    {
        calendar.add(Calendar.DAY_OF_YEAR, value);
    }

}
