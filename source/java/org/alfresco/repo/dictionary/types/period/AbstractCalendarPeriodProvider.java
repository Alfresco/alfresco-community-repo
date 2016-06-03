package org.alfresco.repo.dictionary.types.period;

import java.util.Calendar;
import java.util.Date;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Support for calendar based periods
 * @author andyh
 *
 */
public abstract class AbstractCalendarPeriodProvider extends AbstractPeriodProvider
{
    /** Logger */
    private static Log logger = LogFactory.getLog(AbstractCalendarPeriodProvider.class);
    
    public String getDefaultExpression()
    {
        return "1";
    }

    public ExpressionMutiplicity getExpressionMutiplicity()
    {
        return ExpressionMutiplicity.OPTIONAL;
    }

    public Date getNextDate(Date date, String expression)
    {
        int value = 1;
        try
        {
            value = Integer.parseInt(expression);
        }
        catch (NumberFormatException nfe)
        {
            // default to 1 and log warning
            value = 1;
            
            if (logger.isWarnEnabled())
                logger.warn("\"" + expression + "\" is not a valid period expression!");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        add(calendar, value);
        Date next = calendar.getTime();
        return next;
    }
    
    /**
     * Implementation add
     * @param calendar Calendar
     * @param value int
     */
    public abstract void add(Calendar calendar, int value);

    public QName getExpressionDataType()
    {
        return DataTypeDefinition.INT;
    }
}
