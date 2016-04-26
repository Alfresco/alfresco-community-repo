package org.alfresco.repo.dictionary.types.period;

import java.text.ParseException;
import java.util.Date;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;
import org.quartz.CronExpression;

/**
 * Cron based periods
 * 
 * @author andyh
 */
public class Cron extends AbstractPeriodProvider
{
    /**
     * Period type
     */
    public static final String PERIOD_TYPE = "cron";

    public String getDefaultExpression()
    {
        return "59 59 23 * * ?";
    }

    public ExpressionMutiplicity getExpressionMutiplicity()
    {
        return ExpressionMutiplicity.MANDATORY;
    }

    public Date getNextDate(Date date, String expression)
    {
        CronExpression ce;
        try
        {
            ce = new CronExpression(expression);
        }
        catch (ParseException e)
        {
            throw new AlfrescoRuntimeException("Invalid cron expression: " + expression);
        }
        return ce.getNextValidTimeAfter(date);
    }

    public String getPeriodType()
    {
        return PERIOD_TYPE;
    }

    public QName getExpressionDataType()
    {
        return DataTypeDefinition.TEXT;
    }

}
