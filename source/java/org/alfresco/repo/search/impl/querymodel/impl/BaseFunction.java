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

import java.util.LinkedHashSet;

import org.alfresco.repo.search.impl.querymodel.ArgumentDefinition;
import org.alfresco.repo.search.impl.querymodel.Function;
import org.alfresco.service.namespace.QName;

/**
 * @author andyh
 */
public abstract class BaseFunction implements Function
{

    private String name;

    private QName returnType;

    private LinkedHashSet<ArgumentDefinition> argumentDefinitions;

    public BaseFunction(String name, QName returnType, LinkedHashSet<ArgumentDefinition> argumentDefinitions)
    {
        this.name = name;
        this.returnType = returnType;
        this.argumentDefinitions = argumentDefinitions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Function#getArgumentDefinitions()
     */
    public LinkedHashSet<ArgumentDefinition> getArgumentDefinitions()
    {
        return argumentDefinitions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Function#getName()
     */
    public String getName()
    {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Function#getReturnType()
     */
    public QName getReturnType()
    {
        return returnType;
    }
    
    
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BaseFunction[");
        builder.append("Name="+getName()).append(", ");
        builder.append("Return type="+getReturnType()).append(", ");
        builder.append("ArgumentDefinitions="+getArgumentDefinitions());
        builder.append("]");
        return builder.toString();
    }
}
