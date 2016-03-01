 
package org.alfresco.module.org_alfresco_module_rm.patch;

/**
 * Module Patch Interface
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public interface ModulePatch extends Comparable<ModulePatch>
{
    /**
     * @return  module patch id
     */
    String getId();
    
    /**
     * @return  module patch description
     */
    String getDescription();
    
    /**
     * @return  module id this patch applies to
     */
    String getModuleId();
    
    /**
     * @return smallest module schema number that this patch may be applied to
     */
    int getFixesFromSchema();

    /**
     * @return largest module schema number that this patch may be applied to
     */
    int getFixesToSchema();
    
    /**
     * @return module schema number that this patch attempts to bring the repo up to
     */
    int getTargetSchema();
    
    /**
     * Apply the module patch
     */
    void apply();

}
