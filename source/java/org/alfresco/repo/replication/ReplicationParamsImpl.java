package org.alfresco.repo.replication;


/**
 * Configurable system parameters.
 */
public class ReplicationParamsImpl implements ReplicationParams
{
    /** Lock replication items? */
    private boolean readOnly = true;
    private boolean isEnabled = true;
    
    public ReplicationParamsImpl()
    {
    }

    /**
     * Sets whether to lock replicated items
     * 
     * @param readOnly <code>true</code> lock replicated items in target repository
     */
    public void setTransferReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.replication.ReplicationParams#getTransferReadOnly()
     */
    public boolean getTransferReadOnly()
    {
        return this.readOnly;
    }

    @Override
    public boolean isEnabled()
    {
        return isEnabled;
    }
    
    public void setEnabled(boolean isEnabled)
    {
        this.isEnabled = isEnabled;
    }
}
