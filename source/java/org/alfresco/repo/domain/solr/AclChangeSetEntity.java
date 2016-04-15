package org.alfresco.repo.domain.solr;

import org.alfresco.repo.solr.AclChangeSet;

/**
 * Interface for SOLR changeset objects.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class AclChangeSetEntity implements AclChangeSet
{
    private Long id;
    private Long commitTimeMs;
    private int aclCount;

    @Override
    public String toString()
    {
        return "AclChangeSetEntity " +
        		"[id=" + id +
        		", commitTimeMs=" + commitTimeMs +
        		", aclCount=" + aclCount +
        		"]";
    }
    
    @Override
    public Long getId()
    {
        return id;
    }
    public void setId(Long id)
    {
        this.id = id;
    }
    @Override
    public Long getCommitTimeMs()
    {
        return commitTimeMs;
    }
    public void setCommitTimeMs(Long commitTimeMs)
    {
        this.commitTimeMs = commitTimeMs;
    }
    @Override
    public int getAclCount()
    {
        return aclCount;
    }
    public void setAclCount(int aclCount)
    {
        this.aclCount = aclCount;
    }
}
