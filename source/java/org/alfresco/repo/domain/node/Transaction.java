package org.alfresco.repo.domain.node;

/**
 * Interface for <b>alf_transaction</b> objects.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public interface Transaction
{
    public Long getId();
    
    public String getChangeTxnId();
    
    public Long getCommitTimeMs();
}
