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
package org.alfresco.repo.transfer.manifest;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Test implementation of TransferManifestProcessor.
 * 
 * Simply gives access to the data through header and nodes properties.
 *
 * @author Mark Rogers
 */
public class TestTransferManifestProcessor implements TransferManifestProcessor
{
    private TransferManifestHeader header;
    private Map<NodeRef, TransferManifestNode> nodes = new HashMap<NodeRef, TransferManifestNode>();

    public void endTransferManifest()
    {
    }

    public void processTransferManifestNode(TransferManifestNormalNode node)
    {
        nodes.put(node.getNodeRef(), node);
    }
    
    public void processTransferManifestNode(TransferManifestDeletedNode node)
    {
        nodes.put(node.getNodeRef(), node);
    }

    public void processTransferManifiestHeader(TransferManifestHeader header)
    {
        this.header = header;
    }

    public void startTransferManifest()
    {
    }

    void setHeader(TransferManifestHeader header)
    {
        this.header = header;
    }

    TransferManifestHeader getHeader()
    {
        return header;
    }

    void setNodes( Map<NodeRef, TransferManifestNode> nodes)
    {
        this.nodes = nodes;
    }

    Map<NodeRef, TransferManifestNode>  getNodes()
    {
        return nodes;
    }

}
