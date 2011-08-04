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

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;

/**
 * Action Evaluator whose evaluation takes place on parent
 * 
 * @author florian.mueller
 */
public class ParentActionEvaluator extends AbstractActionEvaluator
{
    private AbstractActionEvaluator evaluator;
    private CMISConnector cmisConnector;

    /**
     * Construct
     * 
     * @param serviceRegistry
     * @param action
     */
    protected ParentActionEvaluator(CMISConnector cmisConnector, AbstractActionEvaluator evaluator)
    {
        super(evaluator.getServiceRegistry(), evaluator.getAction());
        this.evaluator = evaluator;
        this.cmisConnector = cmisConnector;
    }

    public boolean isAllowed(CMISNodeInfo nodeInfo)
    {
        if (nodeInfo.isRootFolder())
        {
            return false;
        }

        ChildAssociationRef car = getServiceRegistry().getNodeService().getPrimaryParent(nodeInfo.getNodeRef());
        if ((car != null) && (car.getParentRef() != null))
        {
            return evaluator.isAllowed(cmisConnector.createNodeInfo(car.getParentRef()));
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
