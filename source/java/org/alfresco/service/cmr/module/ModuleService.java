package org.alfresco.service.cmr.module;

import java.util.List;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.module.ModuleComponent;

/**
 * A service to control and provide information about the currently-installed modules.
 * 
 * @author Roy Wetherall
 * @author Derek Hulley
 * @since 2.0
 */
@AlfrescoPublicApi
public interface ModuleService
{
    /**
     * Gets the module details for a given module id.  If the module does not exist or is not installed
     * then null is returned.
     * 
     * @param moduleId  a module id
     * @return          the module details
     */
    ModuleDetails getModule(String moduleId);
    
    /**
     * Gets a list of all the modules currently installed.
     * 
     * @return  module details of the currently installed modules.
     */
    List<ModuleDetails> getAllModules();
    
    /**
     * Gets a list of the modules missing from the system.
     * 
     * @return  module details of the modules missing from the system.
     */
    List<ModuleDetails> getMissingModules();
    
    /**
     * Register a component of a module for execution.
     * 
     * @param component the module component.
     */
    void registerComponent(ModuleComponent component);
    
    /**
     * Start all the modules.  For transaction purposes, each module should be
     * regarded as a self-contained unit and started in its own transaction.
     * Where inter-module dependencies exist, these will be pulled into the
     * transaction.
     */
    void startModules();
    
    void shutdownModules();
}
