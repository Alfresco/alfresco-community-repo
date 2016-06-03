package org.alfresco.repo.dictionary.types.period;

import java.util.Calendar;

/**
 * Support for calendar based "end of" periods with month and day offsets for fiscal year support
 * @author andyh
 *
 */
public abstract class AbstractEndOfCalendarPeriodProvider extends AbstractCalendarPeriodProvider
{
    private int startDayOfMonth = 1;
    
    private int startMonth = Calendar.JANUARY;

    /**
     * Get the start day of the month (as defined by Calendar)
     * @return - the start day of the month
     */
    public int getStartDayOfMonth()
    {
        return startDayOfMonth;
    }

    /**
     * Set the start day of the month (as defined by Calendar)
     * @param startDayOfMonth int
     */
    public void setStartDayOfMonth(int startDayOfMonth)
    {
        this.startDayOfMonth = startDayOfMonth;
    }

    /**
     * Get the start month (as defined by Calendar)
     * @return - the start month
     */
    public int getStartMonth()
    {
        return startMonth;
    }

    /**
     * Set the start month (as defined by Calendar)
     * @param startMonth int
     */
    public void setStartMonth(int startMonth)
    {
        this.startMonth = startMonth;
    }
}
