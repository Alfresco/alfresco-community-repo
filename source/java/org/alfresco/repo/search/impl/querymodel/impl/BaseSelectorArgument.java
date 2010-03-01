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
import org.alfresco.repo.search.impl.querymodel.SelectorArgument;

/**
 * @author andyh
 *
 */
public class BaseSelectorArgument  extends BaseStaticArgument implements SelectorArgument
{

    private String selector;

    /**
     * @param name
     */
    public BaseSelectorArgument(String name, String selector)
    {
        super(name, true, false);
        this.selector = selector;

    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.SelectorArgument#getSelector()
     */
    public String getSelector()
    {
        return selector;
    }

   

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.Argument#getValue()
     */
    public Serializable getValue(FunctionEvaluationContext context)
    {
        return getSelector();
    }

    
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BaseSelectorArgument[");
        builder.append("name=").append(getName()).append(", ");
        builder.append("selector=").append(getSelector());
        builder.append("]");
        return builder.toString();
    }
}
