package org.alfresco.module.org_alfresco_module_rm.admin;

/**
 * Custom metadata exception.
 * 
 * @author Roy Wethearll
 * @since 2.1
 * @see org.alfresco.module.org_alfresco_module_rm.CustomMetadataException
 */
public abstract class CustomMetadataException extends Exception
{
    private static final long serialVersionUID = -6676112294794381360L;
    
    public CustomMetadataException(String msg)
    {
        super(msg);
    }
}
