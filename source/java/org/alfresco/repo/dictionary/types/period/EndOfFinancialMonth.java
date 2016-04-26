package org.alfresco.repo.dictionary.types.period;

/**
 * End of financial month
 * @author andyh
 *
 */
public class EndOfFinancialMonth extends EndOfMonth
{
    /**
     * 
     */
    public static final String PERIOD_TYPE = "fmend"; 
    
 
    public String getPeriodType()
    {
       return PERIOD_TYPE;
    }

}
