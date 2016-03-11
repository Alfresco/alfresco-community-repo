package org.alfresco.module.org_alfresco_module_rm;

/**
 * @deprecated as of 2.1 see {@link org.alfresco.module.org_alfresco_module_rm.admin.CustomMetadataException}
 */
public abstract class CustomMetadataException extends Exception
{
    private static final long serialVersionUID = -6676112294794381360L;
    
    public CustomMetadataException(String msg)
    {
        super(msg);
    }
}
