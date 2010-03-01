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
import java.util.Map;

import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.Function;
import org.alfresco.repo.search.impl.querymodel.FunctionArgument;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;

/**
 * @author andyh
 *
 */
public class BaseFunctionArgument extends BaseDynamicArgument implements FunctionArgument
{

    private Function function;
    
    private Map<String, Argument> arguments;

    public BaseFunctionArgument(String name, Function function, Map<String, Argument> arguments)
    {
        super(name, false, false);
        this.function = function;
        this.arguments = arguments;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.Argument#getValue()
     */
    public Serializable getValue(FunctionEvaluationContext context)
    {
        return function.getValue(arguments, context);
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
        return arguments;
    }
    
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BaseFunctionArgument[");
        builder.append("Name=").append(getName()).append(", ");
        builder.append("Function="+getFunction()).append(", ");
        builder.append("Arguments="+getFunctionArguments());
        builder.append("]");
        return builder.toString();
    }

    public boolean isQueryable()
    {
        for(Argument arg : arguments.values())
        {
            if(!arg.isQueryable())
            {
                return false;
            }
        }
        return true;
    }

}
