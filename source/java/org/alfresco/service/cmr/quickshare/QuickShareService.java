package org.alfresco.service.cmr.quickshare;

import java.util.Map;

import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * The QuickShare service.
 * 
 * Responsible for creating, updating and retrieving Quick share metadata,
 *
 * @author Alex Miller, janv
 * @since Cloud/4.2
 */
public interface QuickShareService
{
    /**
     * Share content identified by nodeRef.
     * 
     * @param nodeRef The NodeRef of the content to share
     * @return QuickDTO with details of the share
     */
    public QuickShareDTO shareContent(NodeRef nodeRef) throws QuickShareDisabledException, InvalidNodeRefException;

    /**
     * Get QuickShare related metadata for the given node.
     *  
     * @param nodeRef NodeRef
     * @return Map
     */
    public Map<String, Object> getMetaData(NodeRef nodeRef) throws QuickShareDisabledException, InvalidNodeRefException;

    /**
     * Get QuickShare related metadata for the given shareId.
     *
     * @param shareId String
     * @return Map
     */
    public Map<String, Object> getMetaData(String shareId) throws QuickShareDisabledException, InvalidSharedIdException;

    /**
     * Get the tenant domain and node reference for the the given share id.
     * 
     * @param sharedId String
     * @return Pair
     */
    public Pair<String, NodeRef> getTenantNodeRefFromSharedId(String sharedId) throws QuickShareDisabledException, InvalidSharedIdException;

    /**
     * Unshare the content identified by sharedId
     *  
     * @param sharedId The shared id of the content to unshare.
     */
    public void unshareContent(String sharedId) throws QuickShareDisabledException, InvalidSharedIdException;

    /**
     * Determine if the current user has permission to read the shared content.
     */
    public boolean canRead(String sharedId);
}
