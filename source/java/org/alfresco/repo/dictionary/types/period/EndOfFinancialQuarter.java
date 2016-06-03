package org.alfresco.repo.dictionary.types.period;

/**
 * End of financial quarter
 * @author andyh
 *
 */
public class EndOfFinancialQuarter extends EndOfQuarter
{
    /**
     * 
     */
    public static final String PERIOD_TYPE = "fqend"; 
    
 
    public String getPeriodType()
    {
       return PERIOD_TYPE;
    }
}
