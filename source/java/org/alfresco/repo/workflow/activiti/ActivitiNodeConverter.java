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

package org.alfresco.repo.workflow.activiti;

import java.util.Collection;
import java.util.List;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.workflow.AbstractWorkflowNodeConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
public class ActivitiNodeConverter extends AbstractWorkflowNodeConverter
{
    private final ServiceRegistry serviceRegistry;
    
    public ActivitiNodeConverter(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Object convertNode(NodeRef node)
    {
        return new ActivitiScriptNode(node, serviceRegistry);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public List<? extends Object> convertNodes(Collection<NodeRef> values)
    {
        ActivitiScriptNodeList results = new ActivitiScriptNodeList();
        for (NodeRef node : values)
        {
            results.add(new ActivitiScriptNode(node, serviceRegistry));
        }
        return results;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public NodeRef convertToNode(Object toConvert)
    {
        return ((ScriptNode)toConvert).getNodeRef();
    }
    
    /**
    * {@inheritDoc}
    */
    @Override
    public boolean isSupported(Object object)
    {
        if (object == null)
        {
            return false;
        }
        if (object instanceof ActivitiScriptNode)
        {
            return true;
        }
        if (object instanceof ActivitiScriptNodeList)
        {
            return true;
        }
        return false;
    }
}
