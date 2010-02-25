package org.alfresco.repo.transfer.manifest;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;

/**
 * A record of a deleted node in the transfer manifest
 * 
 * The path and node ref refers to the state prior to the node's deletion.
 *
 * @author Mark Rogers
 */
public class TransferManifestDeletedNode implements TransferManifestNode
{
    private NodeRef nodeRef;   
    private ChildAssociationRef primaryParentAssoc;
    private String uuid;
    private Path parentPath; 

    public void setNodeRef(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }

    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    public String getUuid()
    {
        return uuid;
    }
    
    public void setParentPath(Path parentPath)
    {
        this.parentPath = parentPath;
    }

    public Path getParentPath()
    {
        return parentPath;
    }

    public void setPrimaryParentAssoc(ChildAssociationRef parentAssoc)
    {
        this.primaryParentAssoc = parentAssoc;
    }

    public ChildAssociationRef getPrimaryParentAssoc()
    {
        return primaryParentAssoc;
    }


}
