package org.alfresco.repo.dictionary.types.period;

import java.util.Date;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.datatype.Duration;
import org.alfresco.service.namespace.QName;

/**
 * XMLDuration
 * @author andyh
 *
 */
public class XMLDuration extends AbstractPeriodProvider
{
    /**
     * Period type
     */
    public static final String PERIOD_TYPE = "duration"; 

    public String getDefaultExpression()
    {
        return "P1D";
    }

    public ExpressionMutiplicity getExpressionMutiplicity()
    {
        return ExpressionMutiplicity.MANDATORY;
    }

    public Date getNextDate(Date date, String expression)
    {
       Duration d = new Duration(expression);
       return Duration.add(date, d);
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
