package org.alfresco.filesys.repo;

import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Extended {@link org.alfresco.jlan.server.filesys.DiskInterface disk interface} to
 * allow access to some of the internal configuration properties.
 * 
 * @author Derek Hulley
 */
public interface ContentDiskInterface extends DiskInterface
{
    /**
     * Get the name of the shared path within the server.  The share name is
     * equivalent in browse path to the {@link #getContextRootNodeRef() context root}.
     * 
     * @return Returns the share name
     */
    public String getShareName();
    
    /**
     * Get a reference to the node that all CIFS paths are relative to
     *    
     * @return Returns a node acting as the CIFS root
     */
    public NodeRef getContextRootNodeRef();
}
