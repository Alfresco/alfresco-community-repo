package org.alfresco.rest.model.fileplancomponents;

/**
 * POJO for the review period
 *
 * @author Rodica Sutu
 * @since 1.0
 */
public class ReviewPeriod
{
    private String periodType;
    private String expression;

    /**
     * @return the periodType
     */
    public String getPeriodType()
    {
        return this.periodType;
    }

    /**
     * @param periodType the periodType to set
     */
    public void setPeriodType(String periodType)
    {
        this.periodType = periodType;
    }

    /**
     * @return the expression
     */
    public String getExpression()
    {
        return this.expression;
    }

    /**
     * @param expression the expression to set
     */
    public void setExpression(String expression)
    {
        this.expression = expression;
    }
}
