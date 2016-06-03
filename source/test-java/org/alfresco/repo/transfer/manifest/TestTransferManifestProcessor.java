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
