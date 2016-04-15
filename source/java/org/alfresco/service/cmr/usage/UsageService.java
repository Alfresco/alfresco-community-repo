package org.alfresco.service.cmr.usage;

import java.util.Set;

import org.alfresco.service.NotAuditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The public API by which applications can create usage delta entries.
 * 
 * @author janv
 * @since 2.9, 3.0
 */
public interface UsageService
{
    /**
     * Add a usage delta entry.
     */
    @NotAuditable
    public void insertDelta(NodeRef usageNodeRef, long deltaSize);
    
    /**
     * Get sum of usage delta sizes.
     */
    @NotAuditable
    public long getTotalDeltaSize(NodeRef usageNodeRef);
    
    /**
     * Get sum of usage delta sizes and remove affected deltas.
     */
    @NotAuditable
    public long getAndRemoveTotalDeltaSize(NodeRef usageNodeRef);
    
    /**
     * Get distinct set of usage delta nodes
     */
    @NotAuditable
    public Set<NodeRef> getUsageDeltaNodes();
    
    /**
     * Delete the usage delta nodes
     */
    @NotAuditable
    public int deleteDeltas(NodeRef usageNodeRef);
}
