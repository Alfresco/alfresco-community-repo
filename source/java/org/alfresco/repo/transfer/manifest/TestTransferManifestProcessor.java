/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.transfer.manifest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
