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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.FunctionalConstraint;
import org.alfresco.repo.search.impl.querymodel.Join;
import org.alfresco.repo.search.impl.querymodel.JoinType;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.repo.search.impl.querymodel.Selector;
import org.alfresco.repo.search.impl.querymodel.Source;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Equals;

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

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Source#getSelectorNames()
     */
    public Map<String, Selector> getSelectors()
    {
        HashMap<String, Selector> answer = new HashMap<String, Selector>();
        Map<String, Selector> leftSelectors = left.getSelectors();
        for (String selectorName : leftSelectors.keySet())
        {
            Selector selector = leftSelectors.get(selectorName);
            if (answer.put(selectorName, selector) != null)
            {
                throw new DuplicateSelectorNameException("There is a duplicate selector name for " + selectorName);
            }
        }
        Map<String, Selector> rightSelectors = right.getSelectors();
        for (String selectorName : rightSelectors.keySet())
        {
            Selector selector = rightSelectors.get(selectorName);
            if (answer.put(selectorName, selector) != null)
            {
                throw new DuplicateSelectorNameException("There is a duplicate selector name for " + selectorName);
            }
        }
        return answer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Source#getSelector(java.lang.String)
     */
    public Selector getSelector(String name)
    {
        Map<String, Selector> answer = getSelectors();
        return answer.get(name);
    }

    public List<Set<String>> getSelectorGroups(FunctionEvaluationContext functionContext)
    {
        List<Set<String>> answer = new ArrayList<Set<String>>();

        List<Set<String>> left = getLeft().getSelectorGroups(functionContext);
        List<Set<String>> right = getRight().getSelectorGroups(functionContext);

        FunctionalConstraint joinCondition = (FunctionalConstraint) getJoinCondition();
        if (!joinCondition.getFunction().getName().equals(Equals.NAME))
        {
            throw new UnsupportedOperationException("Only equi-joins are supported");
        }

        Argument lhs = joinCondition.getFunctionArguments().get(Equals.ARG_LHS);
        Argument rhs = joinCondition.getFunctionArguments().get(Equals.ARG_RHS);

        String lhsSelector = null;
        String rhsSelector = null;

        if (lhs instanceof PropertyArgument)
        {
            PropertyArgument propertyArgument = (PropertyArgument) lhs;
            String name = propertyArgument.getPropertyName();
            if (functionContext.isObjectId(name))
            {
                lhsSelector = propertyArgument.getSelector();
            }
        }

        if (rhs instanceof PropertyArgument)
        {
            PropertyArgument propertyArgument = (PropertyArgument) rhs;
            String name = propertyArgument.getPropertyName();
            if (functionContext.isObjectId(name))
            {
                rhsSelector = propertyArgument.getSelector();
            }
        }

        if ((getJoinType() == JoinType.INNER) && (lhsSelector != null) && (rhsSelector != null))
        {

            TOADD: for (Set<String> toAddTo : left)
            {
                if (toAddTo.contains(lhsSelector))
                {
                    TOMOVE: for (Set<String> toMove : right)
                    {
                        if (toMove.contains(rhsSelector))
                        {
                            toAddTo.addAll(toMove);
                            toMove.clear();
                            break TOMOVE;
                        }
                    }
                    break TOADD;
                }
                if (toAddTo.contains(rhsSelector))
                {
                    TOMOVE: for (Set<String> toMove : right)
                    {
                        if (toMove.contains(lhsSelector))
                        {
                            toAddTo.addAll(toMove);
                            toMove.clear();
                            break TOMOVE;
                        }
                    }
                    break TOADD;
                }
            }
        }

        // remove any empty sets

        for (Set<String> group : left)
        {
            if (group.size() > 0)
            {
                answer.add(group);
            }
        }
        for (Set<String> group : right)
        {
            if (group.size() > 0)
            {
                answer.add(group);
            }
        }

        return answer;
    }
}
