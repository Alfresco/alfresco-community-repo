package org.alfresco.module.org_alfresco_module_rm.model.security;

/**
 * Protected property implementation
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class ProtectedProperty extends ProtectedModelArtifact
{
    /** always allow new indicator */
    private boolean allwaysAllowNew = false;
    
    /**
     * @param allwaysAllowNew   true if always allow new, false otherwise
     */
    public void setAllwaysAllowNew(boolean allwaysAllowNew)
    {
        this.allwaysAllowNew = allwaysAllowNew;
    }
    
    /**
     * @return  true if always allow new, false otherwise
     */
    public boolean isAllwaysAllowNew()
    {
        return allwaysAllowNew;
    }
}
