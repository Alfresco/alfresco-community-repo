package org.alfresco.module.org_alfresco_module_rm.util;

/**
 * Alfresco Transaction Support delegation bean.
 * 
 * @author Roy Wetherall
 * @since 2.3
 * @see org.alfresco.repo.transaction.AlfrescoTransactionSupport
 */
public class AlfrescoTransactionSupport
{
    /**
     * @see org.alfresco.repo.transaction.AlfrescoTransactionSupport#bindResource(Object, Object)
     */
    public void bindResource(Object key, Object resource)
    {
        org.alfresco.repo.transaction.AlfrescoTransactionSupport.bindResource(key, resource);
    }
    
    /**
     * @see org.alfresco.repo.transaction.AlfrescoTransactionSupport#unbindResource(Object)
     */
    public void unbindResource(Object key)
    {
        org.alfresco.repo.transaction.AlfrescoTransactionSupport.unbindResource(key);
    }

    /**
     * @see org.alfresco.repo.transaction.AlfrescoTransactionSupport#getResource(Object)
     * @since 2.4.a
     */
    public Object getResource(Object key)
    {
        return org.alfresco.repo.transaction.AlfrescoTransactionSupport.getResource(key);
    }
}
