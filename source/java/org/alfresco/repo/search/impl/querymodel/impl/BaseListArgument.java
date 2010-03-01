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
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.ListArgument;

/**
 * @author andyh
 *
 */
public class BaseListArgument extends BaseStaticArgument implements ListArgument
{
    private List<Argument> arguments;

    /**
     * @param name
     */
    public BaseListArgument(String name, List<Argument> arguments)
    {
        super(name, false, false);
        this.arguments = arguments;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.ListArgument#getArguments()
     */
    public List<Argument> getArguments()
    {
        return arguments;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.Argument#getValue()
     */
    public Serializable getValue(FunctionEvaluationContext context)
    {
        ArrayList<Serializable> answer = new ArrayList<Serializable>(arguments.size());
        for(Argument argument : arguments)
        {
            Serializable value = argument.getValue(context);
            answer.add(value);
        }
        return answer;
        
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BaseListArgument[");
        builder.append("name=").append(getName()).append(", ");
        builder.append("values=").append(getArguments());
        builder.append("]");
        return builder.toString();
    }
    
    public boolean isQueryable()
    {
        for(Argument arg : arguments)
        {
            if(!arg.isQueryable())
            {
                return false;
            }
        }
        return true;
    }
}
