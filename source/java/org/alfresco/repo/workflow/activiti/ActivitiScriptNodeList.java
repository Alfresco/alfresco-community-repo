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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * List of {@link ActivitiScriptNode}s.
 *
 * @author Frederik Heremans
 * @since 3.4.e
 */
public class ActivitiScriptNodeList extends ArrayList<ActivitiScriptNode>
{
    private static final long serialVersionUID = 5177463364573735290L;

    public List<NodeRef> getNodeReferences() 
    {
        // Extract all node references
        List<NodeRef> nodeRefs = new ArrayList<NodeRef>();
        for (ActivitiScriptNode scriptNode : this)
        {
            nodeRefs.add(scriptNode.getNodeRef());
        }
        return nodeRefs;
    }
    
    @Override
    public int size() 
    {
        return super.size();
    }
}
