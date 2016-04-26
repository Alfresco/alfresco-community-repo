package org.alfresco.repo.transfer.manifest;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;

/**
 * Data value object - part of the transfer manifest
 * 
 * Represents a single node in the transfer manifest
 * 
 * @see org.alfresco.repo.transfer.manifest.TransferManifestDeletedNode
 * @see org.alfresco.repo.transfer.manifest.TransferManifestNormalNode
 *
 * @author Mark Rogers
 */
public interface TransferManifestNode
{
    public NodeRef getNodeRef();
    public void setNodeRef(NodeRef nodeRef);

    public void setUuid(String uuid);
    public String getUuid();
    
    public void setParentPath(Path parentPath);
    public Path getParentPath();
    
    public void setPrimaryParentAssoc(ChildAssociationRef primaryParent);
    public ChildAssociationRef getPrimaryParentAssoc();
}
