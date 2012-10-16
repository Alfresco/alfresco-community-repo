package org.alfresco.repo.module.tool;

import org.alfresco.service.cmr.module.ModuleDetails;

import de.schlichtherle.truezip.file.TFile;

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
    public ModuleDetails getModuleDetailsOrAlias(TFile war, ModuleDetails installingModuleDetails);
    
    /**
     * Checks the dependencies of this module
     * @param war
     * @param installingModuleDetails
     */
    public void checkModuleDependencies(TFile war, ModuleDetails installingModuleDetails);
     
    /**
     * Checks to see if the module is compatible with the version of Alfresco.
     * 
     * @param war   a valid war file or exploded directory from a war
     */
    public void checkCompatibleVersion(TFile war, ModuleDetails installingModuleDetails);
 
    /**
     * This checks to see if the module that is being installed is compatible with the war.
     * If not module edition is specfied then it will just return.  However, if an edition is specified and it doesn't match
     * then an error is thrown.
     * @param war   a valid war file or exploded directory from a war
     * @param installingModuleDetails
     */
    public void checkCompatibleEdition(TFile war, ModuleDetails installingModuleDetails);

    /**
     * Indicates if the war file specified is a "Share" war.  The default is FALSE
     * Returns true if the Share war manifest states its a share war.
     * @since 3.4.11,4.1.1,Community 4.2
     * 
     * @param war
     * @return boolean - true if it is a share war
     */
	public boolean isShareWar(TFile war);
    
}
