/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.module.tool;

import org.alfresco.service.cmr.module.ModuleDetails;

import de.schlichtherle.truezip.file.TFile;

import java.io.IOException;
import java.util.List;
import java.util.jar.Manifest;

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
     * @param installingModuleDetails ModuleDetails
     * @return ModuleDetails
     */
    public ModuleDetails getModuleDetailsOrAlias(TFile war, ModuleDetails installingModuleDetails);
    
    /**
     * Checks the dependencies of this module
     * @param war TFile
     * @param installingModuleDetails ModuleDetails
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
     * @param installingModuleDetails ModuleDetails
     */
    public void checkCompatibleEdition(TFile war, ModuleDetails installingModuleDetails);

    /**
     * Indicates if the war file specified is a "Share" war.  The default is FALSE
     * Returns true if the Share war manifest states its a share war.
     * @since 3.4.11,4.1.1,Community 4.2
     * 
     * @param war TFile
     * @return boolean - true if it is a share war
     */
	public boolean isShareWar(TFile war);

    /**
     * Lists all the currently installed modules in the WAR
     * @since 5.1
     * @param war the war
     * @return an unordered list of module details.
     * @throws ModuleManagementToolException
     */
    List<ModuleDetails> listModules(TFile war);

    /**
     * Backs up a given file or directory.
     *
     * @since 5.1
     * @param file   the file to backup
     * @return the absolute path to the backup file.
     */
    public String backup(TFile file) throws IOException;

    /**
     * Finds a war manifest file.
     * @since 5.1
     * @param war the war
     * @return Manifest
     * @throws ModuleManagementToolException
     */
    public Manifest findManifest(TFile war) throws ModuleManagementToolException;

}
