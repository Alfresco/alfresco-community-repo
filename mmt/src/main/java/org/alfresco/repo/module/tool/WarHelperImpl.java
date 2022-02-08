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

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.module.ModuleDetailsImpl;
import org.alfresco.service.cmr.module.ModuleDependency;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.util.VersionNumber;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;


/**
 * Performs logic for the Module Management Tool.
 *
 * @author Gethin James
 */
public class WarHelperImpl implements WarHelper
{

    public static final String VERSION_PROPERTIES = "/WEB-INF/classes/alfresco/version.properties";
    public static final String MANIFEST_FILE = "/META-INF/MANIFEST.MF";
    
    //see http://docs.oracle.com/javase/6/docs/technotes/guides/jar/jar.html#Main%20Attributes
    public static final String MANIFEST_SPECIFICATION_TITLE = "Specification-Title";
    public static final String MANIFEST_SPECIFICATION_VERSION = "Specification-Version";
    public static final String MANIFEST_IMPLEMENTATION_TITLE = "Implementation-Title";

    public static final String MANIFEST_SHARE = "Alfresco Share";
    public static final String MANIFEST_COMMUNITY = "Community";
    protected static final String REGEX_NUMBER_OR_DOT = "[0-9\\.]*";
    
    private LogOutput log = null;
    
  
    public WarHelperImpl(LogOutput log)
    {
        super();
        this.log = log;
    }

    @Override
    public void checkCompatibleVersion(TFile war, ModuleDetails installingModuleDetails)
    {
        //Version check
        TFile propsFile = new TFile(war+VERSION_PROPERTIES);
        if (propsFile != null && propsFile.exists())
        {
            log.info("INFO: Checking the war version using "+VERSION_PROPERTIES);
            Properties warVers = loadProperties(propsFile);
            VersionNumber warVersion = new VersionNumber(warVers.getProperty("version.major")+"."+warVers.getProperty("version.minor")+"."+warVers.getProperty("version.revision"));
            checkVersions(warVersion, installingModuleDetails);
        }
        else 
        {
            log.info("INFO: Checking the war version using the manifest.");
        	checkCompatibleVersionUsingManifest(war,installingModuleDetails);
        }
    }
    
    /**
     * Checks if the module is compatible using the entry in the manifest. This is more accurate and works for both alfresco.war and share.war, however
     * valid manifest entries weren't added until 3.4.11, 4.1.1 and Community 4.2 
     * @param war TFile
     * @param installingModuleDetails ModuleDetails
     */
    protected void checkCompatibleVersionUsingManifest(TFile war, ModuleDetails installingModuleDetails)
    {
			String version = findManifestArtibute(war, MANIFEST_SPECIFICATION_VERSION);
	        if (version != null && version.length() > 0)
	        {	        	
	        	if (version.matches(REGEX_NUMBER_OR_DOT)) {
			        VersionNumber warVersion = new VersionNumber(version);
		            checkVersions(warVersion, installingModuleDetails);	        		
	        	}
	        	else 
	        	{
	        		//A non-numeric version number.  Currently our VersionNumber class doesn't support Strings in the version
	        		String edition = findManifestArtibute(war, MANIFEST_IMPLEMENTATION_TITLE);
	        		if (edition.endsWith(MANIFEST_COMMUNITY))
	        		{
	        			//If it's a community version, so don't worry about it
	                    log.info("WARNING: Community edition war detected, the version number is non-numeric so we will not validate it.");
	        		}
	        		else
	        		{
	        			throw new ModuleManagementToolException("Invalid version number specified: "+ version);  
	        		}
	        	}

	        }
	        else
	        {
                log.info("WARNING: No version information detected in war, therefore version validation is disabled, continuing anyway.  Is this war prior to 3.4.11, 4.1.1 and Community 4.2 ?");    	
	        }
    }

    /**
     * Finds a single attribute from a war manifest file.
     * @param war the war
     * @param attributeName key name of attribute
     * @return attribute value
     * @throws ModuleManagementToolException
     */
	protected String findManifestArtibute(TFile war, String attributeName) throws ModuleManagementToolException {

        Manifest manifest = findManifest(war);
        Attributes attribs = manifest.getMainAttributes();
        return attribs.getValue(attributeName);
	}

    /**
     * Finds a single attribute from a war manifest file.
     * @param war the war
     * @return Manifest
     * @throws ModuleManagementToolException
     */
    @Override
    public Manifest findManifest(TFile war) throws ModuleManagementToolException {

        InputStream is = null;

        try
        {
            is = new TFileInputStream(war+MANIFEST_FILE);
            Manifest manifest = new Manifest(is);
            return manifest;
        }
        catch (IOException e)
        {
            throw new ModuleManagementToolException("Unabled to read a manifest for the war file: "+ war);
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (Throwable e ) {}
            }
        }
    }

	/**
	 * Compares the version information with the module details to see if their valid.  If they are invalid then it throws an exception.
	 * @param warVersion VersionNumber
	 * @param installingModuleDetails ModuleDetails
	 * @throws ModuleManagementToolException
	 */
	private void checkVersions(VersionNumber warVersion, ModuleDetails installingModuleDetails) throws ModuleManagementToolException
	{
		if(warVersion.compareTo(installingModuleDetails.getRepoVersionMin())==-1) {
		    throw new ModuleManagementToolException("The module ("+installingModuleDetails.getTitle()+") must be installed on a war version equal to or greater than "
		                +installingModuleDetails.getRepoVersionMin()+". This war is version: "+warVersion+".");
		}
		if(warVersion.compareTo(installingModuleDetails.getRepoVersionMax())==1) {
		    throw new ModuleManagementToolException("The module ("+installingModuleDetails.getTitle()+") cannot be installed on a war version greater than "
		                +installingModuleDetails.getRepoVersionMax()+". This war is version: "+warVersion+".");
		}
	}

    @Override
    public void checkCompatibleEdition(TFile war, ModuleDetails installingModuleDetails)
    {

        List<String> installableEditions = installingModuleDetails.getEditions();

        if (installableEditions != null && installableEditions.size() > 0) {
            
            TFile propsFile = new TFile(war+VERSION_PROPERTIES);
            if (propsFile != null && propsFile.exists())
            {
                Properties warVers = loadProperties(propsFile);
                String warEdition = warVers.getProperty("version.edition");
                
                for (String edition : installableEditions)
                {
                    if (warEdition.equalsIgnoreCase(edition))
                    {
                        return;  //successful match.
                    }
                }
                throw new ModuleManagementToolException("The module ("+installingModuleDetails.getTitle()
                            +") can only be installed in one of the following editions"+installableEditions);
            } else {
            	checkCompatibleEditionUsingManifest(war,installingModuleDetails);
            }
        }
    }

    /**
     * Checks to see if the module that is being installed is compatible with the war, (using the entry in the manifest).
     * This is more accurate and works for both alfresco.war and share.war, however
     * valid manifest entries weren't added until 3.4.11, 4.1.1 and Community 4.2 
     * @param war TFile
     * @param installingModuleDetails ModuleDetails
     */
    public void checkCompatibleEditionUsingManifest(TFile war, ModuleDetails installingModuleDetails)
    {
        List<String> installableEditions = installingModuleDetails.getEditions();

        if (installableEditions != null && installableEditions.size() > 0) {
            
    		String warEdition = findManifestArtibute(war, MANIFEST_IMPLEMENTATION_TITLE);
    		if (warEdition != null && warEdition.length() > 0)
    		{
    			warEdition = warEdition.toLowerCase();
                for (String edition : installableEditions)
                {
                    if (warEdition.endsWith(edition.toLowerCase()))
                    {
                        return;  //successful match.
                    }
                }
                throw new ModuleManagementToolException("The module ("+installingModuleDetails.getTitle()
                            +") can only be installed in one of the following editions"+installableEditions);
            } else {
                log.info("WARNING: No edition information detected in war, edition validation is disabled, continuing anyway. Is this war prior to 3.4.11, 4.1.1 and Community 4.2 ?");
            }
        }	
    }
    
    @Override
    public void checkModuleDependencies(TFile war, ModuleDetails installingModuleDetails)
    {
        // Check that the target war has the necessary dependencies for this install
        List<ModuleDependency> installingModuleDependencies = installingModuleDetails.getDependencies();
        List<ModuleDependency> missingDependencies = new ArrayList<ModuleDependency>(0);
        for (ModuleDependency dependency : installingModuleDependencies)
        {
            String dependencyId = dependency.getDependencyId();
            ModuleDetails dependencyModuleDetails = getModuleDetails(war, dependencyId);
            // Check the dependency.  The API specifies that a null returns false, so no null check is required
            if (!dependency.isValidDependency(dependencyModuleDetails))
            {
                missingDependencies.add(dependency);
                continue;
            }
        }
        if (missingDependencies.size() > 0)
        {
            throw new ModuleManagementToolException("The following modules must first be installed: " + missingDependencies);
        }
    }
    
    @Override
    public ModuleDetails getModuleDetailsOrAlias(TFile war, ModuleDetails installingModuleDetails)
    {
        ModuleDetails installedModuleDetails = getModuleDetails(war, installingModuleDetails.getId());
        if (installedModuleDetails == null)
        {
            // It might be there as one of the aliases
            List<String> installingAliases = installingModuleDetails.getAliases();
            for (String installingAlias : installingAliases)
            {
                ModuleDetails installedAliasModuleDetails = getModuleDetails(war, installingAlias);
                if (installedAliasModuleDetails == null)
                {
                    // There is nothing by that alias
                    continue;
                }
                // We found an alias and will treat it as the same module
                installedModuleDetails = installedAliasModuleDetails;
                //outputMessage("Module '" + installingAlias + "' is installed and is an alias of '" + installingModuleDetails + "'", false);
                break;
            }
        }
        return installedModuleDetails;
    }


    @Override
    public boolean isShareWar(TFile warFile)
    {
        if (!warFile.exists())
        {
            throw new ModuleManagementToolException("The war file '" + warFile + "' does not exist.");     
        }
        
        String title = findManifestArtibute(warFile, MANIFEST_SPECIFICATION_TITLE);
        if (MANIFEST_SHARE.equals(title)) return true;  //It is share
        
        return false; //default
    }


    /**
     * Lists all the currently installed modules in the WAR
     *
     * @param war the war
     * @throws ModuleManagementToolException
     */
    @Override
    public List<ModuleDetails> listModules(TFile war)
    {
        List<ModuleDetails> moduleDetails = new ArrayList<>();
        boolean moduleFound = false;

        TFile moduleDir = new TFile(war, WarHelper.MODULE_NAMESPACE_DIR);
        if (moduleDir.exists() == false)
        {
            return moduleDetails; //empty
        }

        java.io.File[] dirs = moduleDir.listFiles();
        if (dirs != null && dirs.length != 0)
        {
            for (java.io.File dir : dirs)
            {
                if (dir.isDirectory() == true)
                {
                    TFile moduleProperties = new TFile(dir.getPath() + WarHelper.MODULE_CONFIG_IN_WAR);
                    if (moduleProperties.exists() == true)
                    {
                        InputStream is = null;
                        try
                        {
                            moduleFound = true;
                            is = new TFileInputStream(moduleProperties);
                            moduleDetails.add(ModuleDetailsHelper.createModuleDetailsFromPropertiesStream(is));
                        }
                        catch (AlfrescoRuntimeException exception)
                        {
                            throw new ModuleManagementToolException("Unable to open module properties file '" + moduleProperties.getPath() + "' " + exception.getMessage(), exception);
                        }
                        catch (IOException exception)
                        {
                            throw new ModuleManagementToolException("Unable to open module properties file '" + moduleProperties.getPath() + "'", exception);
                        }
                        finally
                        {
                            if (is != null)
                            {
                                try { is.close(); } catch (Throwable e ) {}
                            }
                        }
                    }
                }
            }
        }

        return moduleDetails;
    }

    /**
     * Backs up a given file or directory.
     *
     * @param file   the file to backup
     * @return the absolute path to the backup file.
     */
    @Override
    public String backup(TFile file) throws IOException
    {

        String backupLocation = file.getAbsolutePath()+"-" + System.currentTimeMillis() + ".bak";

        if (file.isArchive())
        {
            log.info("Backing up file...");
            TFile source = new TFile(file.getAbsolutePath(), TArchiveDetector.NULL);
            TFile backup = new TFile(backupLocation, TArchiveDetector.NULL);
            source.cp_rp(backup);   //Just copy the file
        }
        else
        {
            log.info("Backing up DIRECTORY...");
            TFile backup = new TFile(backupLocation);
            file.cp_rp(backup);   //Copy the directory
        }
        log.info("The back up is at '" + backupLocation + "'");

        return backupLocation;
    }

    /**
     * Gets the module details for the specified module from the war.
     * @param war   a valid war file or exploded directory from a war
     * @param moduleId String
     * @return ModuleDetails
     */
    protected ModuleDetails getModuleDetails(TFile war, String moduleId)
    {
        ModuleDetails moduleDets = null;
        TFile theFile = getModuleDetailsFile(war, moduleId);
        if (theFile != null && theFile.exists())
        {
            moduleDets =  new ModuleDetailsImpl(loadProperties(theFile));
        }
        return moduleDets;
    }
    
    /**
     * Reads a .properites file from the war and returns it as a Properties object
     * @param propertiesFile Path to the properties file (including .properties)
     * @return Properties object or null
     */
    private Properties loadProperties(TFile propertiesFile)
    {
        Properties result = null;
        InputStream is = null;
        try
        {
            if (propertiesFile.exists())
            {
                is = new TFileInputStream(propertiesFile);
                result = new Properties();
                result.load(is);
            }
        }
        catch (IOException exception)
        {
            throw new ModuleManagementToolException("Unable to load properties from the war file; "+propertiesFile.getPath(), exception);
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (Throwable e ) {}
            }
        }
        return result;
       
    }
    
    private TFile getModuleDetailsFile(TFile war, String moduleId)
    {
        return new TFile(war.getAbsolutePath()+MODULE_NAMESPACE_DIR+ "/" + moduleId+MODULE_CONFIG_IN_WAR);
    }
    

}
