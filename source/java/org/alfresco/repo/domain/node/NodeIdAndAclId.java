package org.alfresco.repo.domain.node;

/**
 * Simple interface for beans carrying basic Node ID and related ACL ID data
 * 
 * @author andyh
 */
public interface NodeIdAndAclId
{
    public abstract Long getId();

    public abstract Long getAclId();
}
