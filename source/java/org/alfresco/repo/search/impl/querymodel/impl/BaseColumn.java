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

import java.util.Map;

import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.Function;

/**
 * @author andyh
 *
 */
public class BaseColumn implements Column
{
    private String alias;
    
    private Function function;
    
    private Map<String, Argument> functionArguments;
    
    public BaseColumn(Function function, Map<String, Argument> functionArguments, String alias)
    {
        this.function = function;
        this.functionArguments = functionArguments;
        this.alias = alias;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.Column#getAlias()
     */
    public String getAlias()
    {
       return alias;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.FunctionInvokation#getFunction()
     */
    public Function getFunction()
    {
        return function;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.FunctionInvokation#getFunctionArguments()
     */
    public Map<String, Argument> getFunctionArguments()
    {
        return functionArguments;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BaseColumn[");
        builder.append("Alias=").append(getAlias()).append(", ");
        builder.append("Function=").append(getFunction()).append(", ");
        builder.append("FunctionArguments=").append(getFunctionArguments());
        builder.append("]");
        return builder.toString();
    }

    public boolean isOrderable()
    {
        for(Argument arg : functionArguments.values())
        {
            if(!arg.isOrderable())
            {
                return false;
            }
        }
       return true;
    }

    public boolean isQueryable()
    {
        for(Argument arg : functionArguments.values())
        {
            if(!arg.isQueryable())
            {
                return false;
            }
        }
        return true;
    }
    
}
