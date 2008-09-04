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

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.Join;
import org.alfresco.repo.search.impl.querymodel.JoinType;
import org.alfresco.repo.search.impl.querymodel.Selector;
import org.alfresco.repo.search.impl.querymodel.Source;

/**
 * @author andyh
 */
public class BaseJoin implements Join
{
    private Constraint joinConstraint;

    private JoinType joinType;

    private Source left;

    private Source right;

    public BaseJoin(Source left, Source right, JoinType joinType, Constraint joinConstraint)
    {
        this.left = left;
        this.right = right;
        this.joinType = joinType;
        this.joinConstraint = joinConstraint;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Join#getJoinCondition()
     */
    public Constraint getJoinCondition()
    {
        return joinConstraint;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Join#getJoinType()
     */
    public JoinType getJoinType()
    {
        return joinType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Join#getLeft()
     */
    public Source getLeft()
    {
        return left;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Join#getRight()
     */
    public Source getRight()
    {
        return right;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BaseJoin[");
        builder.append("Left=" + getLeft()).append(", ");
        builder.append("Right=" + getRight()).append(", ");
        builder.append("JoinType=" + getJoinType()).append(", ");
        builder.append("Condition=" + getJoinCondition());
        builder.append("]");
        return builder.toString();
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.Source#getSelectorNames()
     */
    public Map<String, Selector> getSelectors()
    {
        HashMap<String, Selector> answer = new HashMap<String, Selector>();
        Map<String, Selector> leftSelectors = left.getSelectors();
        for(String selectorName : leftSelectors.keySet())
        {
            Selector selector = leftSelectors.get(selectorName);
            if(answer.put(selectorName, selector) != null)
            {
                throw new DuplicateSelectorNameException("There is a duplicate selector name for "+selectorName);
            }
        }
        Map<String, Selector> rightSelectors = right.getSelectors();
        for(String selectorName : rightSelectors.keySet())
        {
            Selector selector = rightSelectors.get(selectorName);
            if(answer.put(selectorName, selector) != null)
            {
                throw new DuplicateSelectorNameException("There is a duplicate selector name for "+selectorName);
            }
        }
        return answer;
    }
}
