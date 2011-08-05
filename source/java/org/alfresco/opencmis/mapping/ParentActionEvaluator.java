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
package org.alfresco.opencmis.mapping;

import java.util.List;

import org.alfresco.opencmis.dictionary.CMISNodeInfo;

/**
 * Action Evaluator whose evaluation takes place on parent
 * 
 * @author florian.mueller
 */
public class ParentActionEvaluator extends AbstractActionEvaluator
{
    private AbstractActionEvaluator evaluator;

    /**
     * Construct
     * 
     * @param serviceRegistry
     * @param action
     */
    protected ParentActionEvaluator(AbstractActionEvaluator evaluator)
    {
        super(evaluator.getServiceRegistry(), evaluator.getAction());
        this.evaluator = evaluator;
    }

    public boolean isAllowed(CMISNodeInfo nodeInfo)
    {
        if (nodeInfo.isRootFolder())
        {
            return false;
        }

        List<CMISNodeInfo> parents = nodeInfo.getParents();
        if (!parents.isEmpty())
        {
            return evaluator.isAllowed(parents.get(0));
        }

        return false;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ParentActionEvaluator[evaluator=").append(evaluator).append("]");
        return builder.toString();
    }
}
