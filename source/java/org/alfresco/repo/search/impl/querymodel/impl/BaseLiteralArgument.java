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
import org.alfresco.repo.search.impl.querymodel.LiteralArgument;
import org.alfresco.service.namespace.QName;

/**
 * @author andyh
 *
 */
public class BaseLiteralArgument extends BaseStaticArgument implements LiteralArgument
{
    private QName type;
    
    private Serializable value;
    
    public BaseLiteralArgument(String name, QName type, Serializable value)
    {
        super(name, true, false);
        this.type = type;
        this.value = value;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.LiteralArgument#getValue()
     */
    public Serializable getValue(FunctionEvaluationContext context)
    {
        return value;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.LiteralArgument#getType()
     */
    public QName getType()
    {
        return type;
    }
    
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BaseLiteralArgument[");
        builder.append("name=").append(getName()).append(", ");
        builder.append("type=").append(getType()).append(", ");
        builder.append("value=").append(getValue(null)).append(", ");
        builder.append("]");
        return builder.toString();
    }
}
