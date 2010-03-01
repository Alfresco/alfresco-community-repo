/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.service.cmr.repository;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation for the period data type "d:period" A period is specified by the period type and an optional
 * expression. The string value is periodType|expression Examples are: none day - one day day|3 - 3 days week - one week
 * week|1 - one week week|2 - two weeks month year monthend quarterend The period type specifies a period
 * implementation. This is registered with this class and is used to when adding the period to a date, handles any
 * processing of the expression, reports if the expression is not required, optional or mandatory.
 * 
 * @author andyh
 */
@SuppressWarnings("unchecked")
public class Period implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -7978001474638355909L;

    // Providers do not serialize
    transient private static ConcurrentHashMap<String, PeriodProvider> providers = new ConcurrentHashMap<String, PeriodProvider>();

    /**
     * Register a provider
     * @param periodProvider
     */
    public static void registerProvider(PeriodProvider periodProvider)
    {
        providers.put(periodProvider.getPeriodType(), periodProvider);
    }

    /**
     * Find a provider
     * @param periodType
     * @return the provider
     * @throws IllegalStateException of there is no implementation
     */
    public static PeriodProvider getProvider(String periodType)
    {
        PeriodProvider provider = providers.get(periodType);
        if (provider == null)
        {
            throw new IllegalStateException("No period provider for period type " + periodType);
        }
        return provider;
    }

    /**
     * Get the set of registered providers
     * @return - the set of registered providers
     */
    public static Set<String> getProviderNames()
    {
        return providers.keySet();
    }

    private String periodType;

    private String expression;

    /**
     * Create a period without an accompanying expression.
     * 
     * @param period
     */
    public Period(String period)
    {
        String[] parts = period.split("\\|", 2);
        periodType = parts[0];
        if (parts.length == 2)
        {
            expression = parts[1];
        }
    }

    /**
     * @return the periodType
     */
    public String getPeriodType()
    {
        return periodType;
    }

    /**
     * @return the expression
     */
    public String getExpression()
    {
        return expression;
    }

    /**
     * Calculate the next date for this period given the a start date.
     * 
     * @param date
     * @return the next date.
     */
    public Date getNextDate(Date date)
    {
        if (periodType == null || periodType.length() == 0)
        {
            return null;
        }
        
        PeriodProvider provider = getProvider(periodType);
        return provider.getNextDate(date, expression != null ? expression : provider.getDefaultExpression());
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expression == null) ? 0 : expression.hashCode());
        result = prime * result + ((periodType == null) ? 0 : periodType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Period other = (Period) obj;
        if (expression == null)
        {
            if (other.expression != null)
                return false;
        }
        else if (!expression.equals(other.expression))
            return false;
        if (periodType == null)
        {
            if (other.periodType != null)
                return false;
        }
        else if (!periodType.equals(other.periodType))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(periodType);
        if (expression != null)
        {
            builder.append("|");
            builder.append(expression);
        }
        return builder.toString();
    }

}
