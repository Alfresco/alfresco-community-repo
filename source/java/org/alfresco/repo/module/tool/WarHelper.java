package org.alfresco.repo.module.tool;

import java.io.File;

import org.alfresco.service.cmr.module.ModuleDetails;

/**
 * Performs various actions on a war file or exploded war directory
 *
 * @author Gethin James
 */
public interface WarHelper
{
    public static final String MODULE_NAMESPACE_DIR = "/WEB-INF/classes/alfresco/module";
    public static final String MODULE_CONFIG_IN_WAR = "/module.properties";
    
    /**
     * Gets the module details or an available alias
     * @param war   a valid war file or exploded directory from a war
     * @param installingModuleDetails
     * @return ModuleDetails
     */
    public ModuleDetails getModuleDetailsOrAlias(File war, ModuleDetails installingModuleDetails);
    
    /**
     * Checks the dependencies of this module
     * @param war
     * @param installingModuleDetails
     */
    public void checkModuleDependencies(File war, ModuleDetails installingModuleDetails);
     
    /**
     * Checks to see if the module is compatible with the version of Alfresco.
     * 
     * @param war   a valid war file or exploded directory from a war
     */
    public void checkCompatibleVersion(File war, ModuleDetails installingModuleDetails);
 
    /**
     * This checks to see if the module that is being installed is compatible with the war.
     * If not module edition is specfied then it will just return.  However, if an edition is specified and it doesn't match
     * then an error is thrown.
     * @param war   a valid war file or exploded directory from a war
     * @param installingModuleDetails
     */
    public void checkCompatibleEdition(File war, ModuleDetails installingModuleDetails);
    
}
