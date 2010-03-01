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
package org.alfresco.repo.search.impl.querymodel.impl;

import java.io.Serializable;

import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.ParameterArgument;

/**
 * @author andyh
 *
 */
public class BaseParameterArgument extends BaseStaticArgument implements ParameterArgument
{
    private String parameterName;
    
    /**
     * @param name
     */
    public BaseParameterArgument(String name, String parameterName)
    {
        super(name, true, false);
        this.parameterName = parameterName;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.ParameterArgument#getParameterName()
     */
    public String getParameterName()
    {
        return parameterName;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.Argument#getValue()
     */
    public Serializable getValue(FunctionEvaluationContext context)
    {
        throw new UnsupportedOperationException();
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BaseParameterArgument[");
        builder.append("name=").append(getName()).append(", ");
        builder.append("parameterName=").append(getParameterName());
        builder.append("]");
        return builder.toString();
    }

}
