package org.alfresco.rest.model.fileplancomponents;

/**
 * POJO for file plan component review period
 *
 * @author Rodica Sutu
 * @since 1.0
 */
public class ReviewPeriod
{
    private String periodType;
    private String expression;

    public String getPeriodType()
    {
        return periodType;
    }

    public void setPeriodType(String periodType)
    {
        this.periodType = periodType;
    }

    public String getExpression()
    {
        return expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }
}
