package org.alfresco.repo.dictionary.types.period;

import java.util.Calendar;

/**
 * Quarters
 * @author andyh
 *
 */
public class Quarters extends AbstractCalendarPeriodProvider
{
    /**
     * 
     */
    public static final String PERIOD_TYPE = "quarter"; 

    public String getPeriodType()
    {
        return PERIOD_TYPE;
    }

    @Override
    public void add(Calendar calendar, int value)
    {
        calendar.add(Calendar.MONTH, value*3);
    }
}
