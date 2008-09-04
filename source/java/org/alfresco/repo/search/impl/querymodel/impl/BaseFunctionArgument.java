/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.search.impl.querymodel.impl;

import java.io.Serializable;
import java.util.List;

import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.Function;
import org.alfresco.repo.search.impl.querymodel.FunctionArgument;

/**
 * @author andyh
 *
 */
public class BaseFunctionArgument extends BaseDynamicArgument implements FunctionArgument
{

    private Function function;
    
    private List<Argument> arguments;

    public BaseFunctionArgument(String name, Function function, List<Argument> arguments)
    {
        super(name);
        this.function = function;
        this.arguments = arguments;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.Argument#getValue()
     */
    public Serializable getValue()
    {
        throw new UnsupportedOperationException();
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
    public List<Argument> getFunctionArguments()
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

}
