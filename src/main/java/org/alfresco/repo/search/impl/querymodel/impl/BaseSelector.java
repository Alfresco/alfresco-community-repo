/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.search.impl.querymodel.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.Selector;
import org.alfresco.service.namespace.QName;

/**
 * @author andyh
 */
public class BaseSelector implements Selector
{
    private QName type;

    private String alias;

    public BaseSelector(QName type, String alias)
    {
        this.type = type;
        this.alias = alias;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Selector#getAlias()
     */
    public String getAlias()
    {
        return alias;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Selector#getType()
     */
    public QName getType()
    {
        return type;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BaseSelector[");
        builder.append("alias=").append(getAlias()).append(", ");
        builder.append("type=").append(getType());
        builder.append("]");
        return builder.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Source#getSelectorNames()
     */
    public Map<String, Selector> getSelectors()
    {
        HashMap<String, Selector> answer = new HashMap<String, Selector>();
        answer.put(getAlias(), this);
        return answer;
    }

    public Selector getSelector(String name)
    {
        if (getAlias().equals(name))
        {
            return this;
        }
        else
        {
            return null;
        }
    }

    public List<Set<String>> getSelectorGroups(FunctionEvaluationContext functionContext)
    {
        HashSet<String> set = new HashSet<String>();
        set.add(getAlias());
        List<Set<String>> answer = new ArrayList<Set<String>>();
        answer.add(set);
        return answer;
    }
}
