 
package org.alfresco.module.org_alfresco_module_rm.model.security;

import org.alfresco.repo.security.permissions.AccessDeniedException;

/**
 * Model access denied exception implementation
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class ModelAccessDeniedException extends AccessDeniedException
{
    private static final long serialVersionUID = 6796435040345714366L;

    public ModelAccessDeniedException(String msg)
    {
        super(msg);
    }
    
    public ModelAccessDeniedException(String msg, Throwable cause)
    {
        super(msg, cause);        
    }        
}
