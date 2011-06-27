/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.publishing;

import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_STATUS;
import static org.alfresco.repo.publishing.PublishingModel.TYPE_PUBLISHING_EVENT;

import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.publishing.PublishingEvent.Status;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * This {@link ActionExecuter} checks the status of the publishing event
 * dependencies and sets the pub:publishingEventStatus property accordingly.
 * 
 * @author Nick Smith
 * @since 4.0
 * 
 */
public class CheckPublishingDependenciesAction extends ActionExecuterAbstractBase
{
    private NodeService nodeService;
    
    /**
    * {@inheritDoc}
    */
    @Override
    protected void executeImpl(Action action, NodeRef node)
    {
        QName nodeType = nodeService.getType(node);
        if(TYPE_PUBLISHING_EVENT.equals(nodeType))
        {
            nodeService.setProperty(node, PROP_PUBLISHING_EVENT_STATUS, Status.IN_PROGRESS);
        }
    }

    /**
    * {@inheritDoc}
    */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        //NOOP
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
}
