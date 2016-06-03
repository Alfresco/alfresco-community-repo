package org.alfresco.repo.replication;


/**
 * An interface for retrieving configurable replication parameters.
 */
public interface ReplicationParams
{

    /**
     * Lock replicated items in target repository
     * 
     * @return <code>true</code> lock replication items
     */
    public boolean getTransferReadOnly();
    
    /**
     * Is the Replication Service Enabled
     * 
     * @return <code>true</code> the replication service is enabled
     */
    public boolean isEnabled();
    
    /**
     * set whether the replication service is enabled
     */
    public void setEnabled(boolean enabled);
}