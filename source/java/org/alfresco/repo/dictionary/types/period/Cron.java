/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
