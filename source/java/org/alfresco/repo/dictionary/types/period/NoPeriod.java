package org.alfresco.repo.dictionary.types.period;

import java.util.Date;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * No period Period type "none"
 * 
 * @author andyh
 */
public class NoPeriod extends AbstractPeriodProvider
{
    /**
     * 
     */
    public static final String PERIOD_TYPE = "none";

    /**
     * Default constructor
     */
    public NoPeriod()
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
        return null;
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
