package org.alfresco.repo.usage;

import java.util.Set;

import org.alfresco.repo.domain.usage.UsageDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.usage.UsageService;

/**
 * The implementation of the UsageService for tracking usages.
 * 
 * @author janv
 * @since 2.9, 3.0
 */
public class UsageServiceImpl implements UsageService
{
    private UsageDAO usageDAO;
    
    public void setUsageDAO(UsageDAO usageDAO)
    {
        this.usageDAO = usageDAO;
    }
    
    public void insertDelta(NodeRef usageNodeRef, long deltaSize)
    {
        usageDAO.insertDelta(usageNodeRef, deltaSize);
    }
    
    public long getTotalDeltaSize(NodeRef usageNodeRef)
    {
        return usageDAO.getTotalDeltaSize(usageNodeRef, false);
    }
    
    public long getAndRemoveTotalDeltaSize(NodeRef usageNodeRef)
    {
        return usageDAO.getTotalDeltaSize(usageNodeRef, true);
    }

    public Set<NodeRef> getUsageDeltaNodes()
    {
        return usageDAO.getUsageDeltaNodes();
    }
    
    public int deleteDeltas(NodeRef usageNodeRef)
    {
        return usageDAO.deleteDeltas(usageNodeRef);
    }
}
