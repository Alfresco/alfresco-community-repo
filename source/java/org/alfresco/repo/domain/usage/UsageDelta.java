package org.alfresco.repo.domain.usage;


/**
 * Interface for persistent <b>usage delta</b> objects.
 *
 */
public interface UsageDelta
{
    public Long getNodeId();
    
    public Long getDeltaSize();
}