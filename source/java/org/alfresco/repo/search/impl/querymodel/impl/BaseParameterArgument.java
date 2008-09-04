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
        super(name);
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
