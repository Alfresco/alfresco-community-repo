package org.alfresco.repo.dictionary.types.period;

import java.util.Date;

import org.alfresco.service.namespace.QName;

/**
 * Immediate Period type "immediate"
 * 
 * @author Roy Wetherall
 */
public class Immediately extends AbstractPeriodProvider
{
    /**
     * 
     */
    public static final String PERIOD_TYPE = "immediately";

    /**
     * Default constructor
     */
    public Immediately()
    {

    }

    public String getDefaultExpression()
    {
        return null;
    }

    public ExpressionMutiplicity getExpressionMutiplicity()
    {
        return ExpressionMutiplicity.NONE;
    }

    public Date getNextDate(Date date, String expression)
    {
        Date result = null;
        if (date == null)
        {
            result = new Date();
        }
        else
        {
            result = date;
        }
        return result;
    }

    public String getPeriodType()
    {
        return PERIOD_TYPE;
    }

    public QName getExpressionDataType()
    {
        return null;
    }
}
