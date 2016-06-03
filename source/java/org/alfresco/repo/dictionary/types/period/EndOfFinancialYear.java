package org.alfresco.repo.dictionary.types.period;

/**
 * End of financial year
 * @author andyh
 *
 */
public class EndOfFinancialYear extends EndOfYear
{
    /**
     * 
     */
    public static final String PERIOD_TYPE = "fyend"; 
    
 
    public String getPeriodType()
    {
       return PERIOD_TYPE;
    }
}
