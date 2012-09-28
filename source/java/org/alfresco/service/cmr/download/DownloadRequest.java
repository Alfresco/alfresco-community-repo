/*
 * Copyright 2005-2012 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
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
     * @return
     */
    public String getOwner()
    {
        return owner;
    }

}
