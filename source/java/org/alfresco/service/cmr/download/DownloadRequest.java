/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.service.cmr.download;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * DownloadRequest data transfer object.
 *
 * @author Alex Miller
 */
public class DownloadRequest
{
    private String owner;
    private boolean recursive;
    private List<AssociationRef> requestedNodes;

    public DownloadRequest(boolean recursive, List<AssociationRef> requestedNodes, String owner)
    {
        this.owner = owner;
        this.recursive = recursive;
        this.requestedNodes = requestedNodes;
    }

    public List<AssociationRef> getRequetedNodes()
    {
        return requestedNodes;
    }

    public NodeRef[] getRequetedNodeRefs()
    {
        List<NodeRef> requestedNodeRefs = new ArrayList<NodeRef>(requestedNodes.size());
        for (AssociationRef requestedNode : requestedNodes) 
        {
            requestedNodeRefs.add(requestedNode.getTargetRef());
        }
        return requestedNodeRefs.toArray(new NodeRef[requestedNodeRefs.size()]);
    }

    /**
     * @return String
     */
    public String getOwner()
    {
        return owner;
    }

}
