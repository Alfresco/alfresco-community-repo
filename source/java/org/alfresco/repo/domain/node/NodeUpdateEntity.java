package org.alfresco.repo.domain.node;

/**
 * Bean to convey <b>alf_node</b> update data.  It uses the basic node data, but adds
 * information to identify the properties that need updating.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class NodeUpdateEntity extends NodeEntity
{
    private static final long serialVersionUID = 1L;
    private boolean updateTypeQNameId;
    private boolean updateLocaleId;
    private boolean updateAclId;
    private boolean updateTransaction;
    private boolean updateAuditableProperties;
    
    /**
     * Required default constructor
     */
    public NodeUpdateEntity()
    {
    }
    
    /**
     * Determine if this update represents anything new at all
     */
    public boolean isUpdateAnything()
    {
        return updateAuditableProperties || updateTransaction
               || updateLocaleId || updateAclId || updateTypeQNameId;
    }

    public boolean isUpdateTypeQNameId()
    {
        return updateTypeQNameId;
    }

    public void setUpdateTypeQNameId(boolean updateTypeQNameId)
    {
        this.updateTypeQNameId = updateTypeQNameId;
    }

    public boolean isUpdateLocaleId()
    {
        return updateLocaleId;
    }

    public void setUpdateLocaleId(boolean updateLocaleId)
    {
        this.updateLocaleId = updateLocaleId;
    }

    public boolean isUpdateAclId()
    {
        return updateAclId;
    }

    public void setUpdateAclId(boolean updateAclId)
    {
        this.updateAclId = updateAclId;
    }

    public boolean isUpdateTransaction()
    {
        return updateTransaction;
    }

    public void setUpdateTransaction(boolean updateTransaction)
    {
        this.updateTransaction = updateTransaction;
    }

    public boolean isUpdateAuditableProperties()
    {
        return updateAuditableProperties;
    }

    public void setUpdateAuditableProperties(boolean updateAuditableProperties)
    {
        this.updateAuditableProperties = updateAuditableProperties;
    }
}
