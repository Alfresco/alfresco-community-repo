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
package org.alfresco.cmis.mapping;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Action Evaluator which evaluates on whether node is root or not
 * 
 * @author davidc
 */
public class RootActionEvaluator extends AbstractActionEvaluator<NodeRef>
{
    private AbstractActionEvaluator<NodeRef> evaluator;
    private boolean allow;

    /**
     * Construct
     * 
     * @param serviceRegistry
     * @param action
     */
    protected RootActionEvaluator(AbstractActionEvaluator<NodeRef> evaluator, boolean allow)
    {
        super(evaluator.getServiceRegistry(), evaluator.getAction());
        this.evaluator = evaluator;
        this.allow = allow;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISActionEvaluator#isAllowed(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isAllowed(NodeRef nodeRef)
    {
        if (nodeRef.equals(getServiceRegistry().getCMISService().getDefaultRootNodeRef()))
        {
            return allow;
        }
        return evaluator.isAllowed(nodeRef);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("RootActionEvaluator[evaluator=").append(evaluator).append(",allow=").append(allow).append("]");
        return builder.toString();
    }
}

