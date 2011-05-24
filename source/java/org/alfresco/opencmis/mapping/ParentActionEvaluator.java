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

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Action Evaluator whose evaluation takes place on parent
 * 
 * @author davidc
 */
public class ParentActionEvaluator extends AbstractActionEvaluator<NodeRef>
{
    private AbstractActionEvaluator<NodeRef> evaluator;

    /**
     * Construct
     * 
     * @param serviceRegistry
     * @param action
     */
    protected ParentActionEvaluator(AbstractActionEvaluator<NodeRef> evaluator)
    {
        super(evaluator.getServiceRegistry(), evaluator.getAction());
        this.evaluator = evaluator;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISActionEvaluator#isAllowed(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isAllowed(NodeRef nodeRef)
    {
        if (nodeRef.equals(getServiceRegistry().getCMISService().getDefaultRootNodeRef()))
        {
            return false;
        }

        ChildAssociationRef car = getServiceRegistry().getNodeService().getPrimaryParent(nodeRef);
        if ((car != null) && (car.getParentRef() != null))
        {
            return evaluator.isAllowed(car.getParentRef());
        }
        
        return false;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ParentActionEvaluator[evaluator=").append(evaluator).append("]");
        return builder.toString();
    }
}

