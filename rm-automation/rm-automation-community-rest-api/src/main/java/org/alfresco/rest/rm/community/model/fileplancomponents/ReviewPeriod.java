/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.rm.community.model.fileplancomponents;

/**
 * POJO for the review period
 *
 * @author Rodica Sutu
 * @since 2.6
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
