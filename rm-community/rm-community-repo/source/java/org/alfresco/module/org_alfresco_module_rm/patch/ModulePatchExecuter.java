package org.alfresco.module.org_alfresco_module_rm.patch;

/**
 * Module patch service interface
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public interface ModulePatchExecuter
{
    /**
     * Register module patch with the module patch executer
     * 
     * @param modulePatch   module patch
     */
    void register(ModulePatch modulePatch); 
    
    /**
     * Init the schema version number
     */
    void initSchemaVersion();
}
