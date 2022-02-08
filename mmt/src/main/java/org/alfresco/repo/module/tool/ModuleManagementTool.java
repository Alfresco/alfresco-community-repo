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

import de.schlichtherle.truezip.file.*;
import de.schlichtherle.truezip.fs.FsSyncException;
import de.schlichtherle.truezip.fs.archive.zip.JarDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.module.ModuleVersionNumber;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleInstallState;
import org.safehaus.uuid.UUIDGenerator;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;

/**
 * Module management tool.
 * <p>
 * Manages the modules installed in a war file.  Allows modules to be installed, updated, enabled, disabled and
 * uninstalled.  Information about the module installed is also available. 
 * 
 * @since 2.0
 * 
 * @author Roy Wetherall
 * @author Derek Hulley
 */
public class ModuleManagementTool implements LogOutput
{
    /** Location of the default mapping properties file */
    private static final String DEFAULT_FILE_MAPPING_PROPERTIES = "org/alfresco/repo/module/tool/default-file-mapping.properties";
    /** Location of the AMP-specific mappings file */
    private static final String FILE_MAPPING_PROPERTIES = "file-mapping.properties";
    /**
     * The property to add to a custom {@link #FILE_MAPPING_PROPERTIES file-mapping.properties} to inherit the default values.
     * The default is <code>true</code>.
     */
    private static final String PROP_INHERIT_DEFAULT = "include.default";
    
    /** Standard directories found in the alfresco war */
    public static final String BACKUP_DIR = WarHelper.MODULE_NAMESPACE_DIR+ "/backup";
    
    /** Operations and options supperted via the command line interface to this class */
    private static final String OP_INSTALL = "install";
    private static final String OP_UNINSTALL = "uninstall";    
    private static final String OP_LIST = "list";
    private static final String OPTION_VERBOSE = "-verbose";
    private static final String OPTION_FORCE = "-force";
    private static final String OPTION_PREVIEW = "-preview";
    private static final String OPTION_NOBACKUP = "-nobackup";
    private static final String OPTION_DIRECTORY = "-directory";
    private static final String OPTION_PURGE = "-purge";
    private static final String OPTION_HELP = "-help";
    
    private static final int ERROR_EXIT_CODE = 1;
    private static final int SUCCESS_EXIT_CODE = 0;
    
    /** File mapping properties */
    private Properties defaultFileMappingProperties;
    
    /** Indicates the current verbose setting */
    private boolean verbose = false;
    
    WarHelper warHelper = new WarHelperImpl(this);
    
    /**
     * Constructor
     */
    public ModuleManagementTool()
    {
        TConfig config = TConfig.get();
        config.setArchiveDetector(new TArchiveDetector("war|amp", new JarDriver(IOPoolLocator.SINGLETON)));

        // Load the default file mapping properties
        this.defaultFileMappingProperties = new Properties();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_FILE_MAPPING_PROPERTIES);
        try
        {
            this.defaultFileMappingProperties.load(is);
        }
        catch (IOException exception)
        {
            throw new ModuleManagementToolException("Unable to load default extension file mapping properties.", exception);
        }
    }
    
    /** 
     * Indicates whether the management tool is currently in verbose reporting mode.
     * 
     * @return  true if verbose, false otherwise
     */
    public boolean isVerbose()
    {
        return verbose;
    }
    
    /**
     * Sets the verbose setting for the mangement tool
     * 
     * @param verbose   true if verbose, false otherwise
     */
    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }

    /**
     * Installs all modules within a folder into the given WAR file.
     * @throws IOException 
     * 
     * @see #installModule(String, String, boolean, boolean, boolean)
     */
    public void installModules(String directory, String warFileLocation) throws IOException
    {
        installModules(directory, warFileLocation, false, false, true);
    }
    
    public void installModules(String directoryLocation, String warFileLocation, boolean preview, boolean forceInstall, boolean backupWAR) throws IOException
    {
        java.io.File dir = new java.io.File(directoryLocation);
        if (dir.exists() == true)
        {
            if (backupWAR) {
                backupWar(new TFile(warFileLocation),true);
                backupWAR = false; //Set it to false so a backup doesn't occur again.
            }
            installModules(dir, warFileLocation, preview, forceInstall,backupWAR);
        }
        else
        {
            throw new ModuleManagementToolException("Invalid directory '" + directoryLocation + "'");
        }
    }
    
    private void installModules(java.io.File dir, String warFileLocation, boolean preview, boolean forceInstall, boolean backupWAR)
    {
        java.io.File[] children =  dir.listFiles();
        if (children != null)
        {
            for (java.io.File child : children)
            {
                if (child.isFile() == true && child.getName().toLowerCase().endsWith(".amp") == true)
                {
                    installModule(child.getPath(), warFileLocation, preview, forceInstall, backupWAR);
                }
                else
                {
                    installModules(child, warFileLocation, preview, forceInstall, backupWAR);
                }
            }
        }
    }
    
    /**
     * Installs a given AMP file into a given WAR file.  
     * 
     * @see ModuleManagementTool#installModule(String, String, boolean, boolean, boolean)
     * 
     * @param ampFileLocation   the location of the AMP file to be installed
     * @param warFileLocation   the location of the WAR file into which the AMP file is to be installed
     */
    public void installModule(String ampFileLocation, String warFileLocation)
    {
        installModule(ampFileLocation, warFileLocation, false, false, true);
    }
    
    /**
     * Installs a given AMP file into a given WAR file.
     * 
     * @param ampFileLocation   the location of the AMP file to be installed
     * @param warFileLocation   the location of the WAR file into which the AMP file is to be installed.
     * @param preview           indicates whether this should be a preview install.  This means that the process of 
     *                          installation will be followed and reported, but the WAR file will not be modified.
     * @param forceInstall      indicates whether the installed files will be replaced regardless of the currently installed 
     *                          version of the AMP.  Generally used during development of the AMP.
     * @param backupWAR         indicates whether we should backup the war we are modifying or not 
     */
    public void installModule(String ampFileLocation, String warFileLocation, boolean preview, boolean forceInstall, boolean backupWAR)
    {
        TFile warFile = new TFile(warFileLocation);
        try
        {   
            outputVerboseMessage("Installing AMP '" + ampFileLocation + "' into WAR '" + warFileLocation + "'");
            if (!warFile.exists())
            {
                throw new ModuleManagementToolException("The war file '" + warFile + "' does not exist.");     
            }
            if (preview == false)
            {
                // Make sure the module and backup directory exisits in the WAR file
                TFile moduleDir = new TFile(warFileLocation + WarHelper.MODULE_NAMESPACE_DIR);
                if (moduleDir.exists() == false)
                {
                    moduleDir.mkdir();
                }
                backupWar(warFile, backupWAR);
            }
            
            // Get the details of the installing module
            String propertiesLocation = ampFileLocation + "/module.properties";
            ModuleDetails installingModuleDetails = ModuleDetailsHelper.createModuleDetailsFromPropertyLocation(propertiesLocation, this);
            if (installingModuleDetails == null)
            {
                throw new ModuleManagementToolException("No module.properties file has been found in the installing .amp file '" + ampFileLocation + "'");
            }
            String installingId = installingModuleDetails.getId();
            ModuleVersionNumber installingVersion = installingModuleDetails.getModuleVersionNumber();
            
            //A series of checks
            warHelper.checkCompatibleVersion(warFile, installingModuleDetails);
            warHelper.checkCompatibleEdition(warFile, installingModuleDetails);
            warHelper.checkModuleDependencies(warFile, installingModuleDetails);
            
            // Try to find an installed module by the ID
            ModuleDetails installedModuleDetails = warHelper.getModuleDetailsOrAlias(warFile, installingModuleDetails);
            
            //Check module directory exists
            TFile moduleInstallDirectory = new TFile(warFileLocation + WarHelper.MODULE_NAMESPACE_DIR+ "/" + installingId);
            if (preview == false  && moduleInstallDirectory.exists() == false)
            {
            	moduleInstallDirectory.mkdir();
            }       
            
            uninstallIfNecessary(warFileLocation, installedModuleDetails, preview, forceInstall, installingVersion);
            
            outputVerboseMessage("Adding files relating to version '" + installingVersion + "' of module '" + installingId + "'");
            InstalledFiles installedFiles = new InstalledFiles(warFileLocation, installingId);
            
            Properties directoryChanges = calculateChanges(ampFileLocation, warFileLocation, preview, forceInstall, installedFiles);   
            
            if (preview == false)
            {
                //Now actually do the changes
                if (directoryChanges != null && directoryChanges.size() > 0) 
                {
                    for (Entry<Object, Object> entry : directoryChanges.entrySet())
                    {
                        TFile source = new TFile((String) entry.getKey());
                        TFile destination = new TFile((String) entry.getValue());
                        source.cp_rp(destination);
                    }
                }
                
                // Save the installed file list
                installedFiles.save();
           
                // Update the installed module details
                installingModuleDetails.setInstallState(ModuleInstallState.INSTALLED);
                installingModuleDetails.setInstallDate(new Date());
                ModuleDetailsHelper.saveModuleDetails(warFileLocation, installingModuleDetails);

                // Set the modified date
                if (warFile.exists())
                {
                    warFile.setLastModified(System.currentTimeMillis());
                }
            }               
        }
        catch (AlfrescoRuntimeException exception)
        {
            throw new ModuleManagementToolException("An error was encountered during deployment of the AMP into the WAR: " + exception.getMessage(), exception);
        }
        catch (IOException exception)
        {
            throw new ModuleManagementToolException("An IO error was encountered during deployment of the AMP into the WAR", exception);
        }
        finally
        {
            try
            {
                TVFS.umount(warFile);
            }
            catch (FsSyncException e)
            {
                throw new ModuleManagementToolException(
                            "Error when attempting to unmount WAR file: "+warFile.getPath(),
                            e);
            }
        }
    }

    private void uninstallIfNecessary(String warFileLocation, ModuleDetails installedModuleDetails, boolean preview,
                boolean forceInstall, ModuleVersionNumber installingVersion) throws IOException
    {
        // Now clean up the old instance
        if (installedModuleDetails != null)
        {
            String installedId = installedModuleDetails.getId();
            ModuleVersionNumber installedVersion = installedModuleDetails.getModuleVersionNumber();
            
            int compareValue = installedVersion.compareTo(installingVersion);
            if (compareValue > 0)
            {
                // Trying to install an earlier version of the extension
                outputVerboseMessage("WARNING: A later version of this module is already installed in the WAR. Installation skipped.  "+
                "You could force the installation by passing the -force option.",false);
                return;
            }

            if (forceInstall == true)
            {
                // Warn of forced install
                outputVerboseMessage("WARNING: The installation of this module is being forced.  All files will be removed and replaced regardless of existing versions present.",false);
            }
            
            if (compareValue == 0)
            {
                // Trying to install the same extension version again
                outputVerboseMessage("WARNING: This version of this module is already installed in the WAR..upgrading.",false);
            }
            
            if (forceInstall == true || compareValue <= 0)
            {
                
                // Trying to update the extension, old files need to cleaned before we proceed
                outputVerboseMessage("Clearing out files relating to version '" + installedVersion + "' of module '" + installedId + "'",false);
                uninstallModule(installedId, warFileLocation, preview, true);
            } 
        }
    }

    /**
     */
    private Properties calculateChanges(String ampFileLocation, String warFileLocation, boolean preview,
                boolean forceInstall, InstalledFiles installedFiles) throws IOException
    {
        Properties dirChanges = new Properties();
        
        // Check if a custom mapping file has been defined
        Properties fileMappingProperties = null;
        Properties customFileMappingProperties = getCustomFileMappings(ampFileLocation);
        if (customFileMappingProperties == null)
        {
            fileMappingProperties = defaultFileMappingProperties;
        }
        else
        {
            fileMappingProperties = new Properties();
            // A custom mapping file was present.  Check if it must inherit the default mappings.
            String inheritDefaultStr = customFileMappingProperties.getProperty(PROP_INHERIT_DEFAULT, "true");
            if (inheritDefaultStr.equalsIgnoreCase("true"))
            {
                fileMappingProperties.putAll(defaultFileMappingProperties);
            }
            fileMappingProperties.putAll(customFileMappingProperties);
            fileMappingProperties.remove(PROP_INHERIT_DEFAULT);
        }
        
        // Copy the files from the AMP file into the WAR file
        for (Map.Entry<Object, Object> entry : fileMappingProperties.entrySet())
        {
            // The file mappings are expected to start with "/"
            String mappingSource = (String) entry.getKey();
            if (mappingSource.length() == 0 || !mappingSource.startsWith("/"))
            {
                throw new AlfrescoRuntimeException("File mapping sources must start with '/', but was: " + mappingSource);
            }
            String mappingTarget = (String) entry.getValue();
            if (mappingTarget.length() == 0 || !mappingTarget.startsWith("/"))
            {
                throw new AlfrescoRuntimeException("File mapping targets must start with '/' but was '" + mappingTarget + "'");
            }
            
            mappingSource = mappingSource.trim(); //trim whitespace
            mappingTarget = mappingTarget.trim(); //trim whitespace
            
            // Run through the files one by one figuring out what we are going to do during the copy
            calculateCopyToWar(ampFileLocation, warFileLocation, mappingSource, mappingTarget, installedFiles, preview, forceInstall);
            
            // Get a reference to the source folder (if it isn't present don't do anything)
            TFile source = new TFile(ampFileLocation + "/" + mappingSource);
            if (source != null && source.list() != null)
            {
                // Add to the list of directory changes so we can implement the changes later.
                String sourceDir = ampFileLocation + mappingSource;
                String destinationDir = warFileLocation + mappingTarget;
                dirChanges.put(sourceDir, destinationDir);
            }
            
        }
        
        return dirChanges;
    }

    /**
     * Backsup a given WAR file.
     *
     * @see ModuleManagementTool#installModule(String, String, boolean, boolean, boolean)
     *
     * @param warFile   the location of the AMP file to be installed
     * @param backupWAR true if you want it to perform the backup
     */
    private void backupWar(TFile warFile, boolean backupWAR) throws IOException
    {

        // Make a backup of the war we are going to modify
        if (backupWAR == true)
        {
            warHelper.backup(warFile);
        }
    }
    
    /**
     * @return Returns the custom file mapping properties or null if they weren't overwritten
     */
    private Properties getCustomFileMappings(String ampFileLocation)
    {
        TFile file = new TFile(ampFileLocation + "/" + FILE_MAPPING_PROPERTIES);
        if (!file.exists())
        {
            // Nothing there
            return null;
        }
        Properties mappingProperties = new Properties();
        InputStream is = null;
        try
        {
            is = new BufferedInputStream(new TFileInputStream(file));
            mappingProperties.load(is);
        }
        catch (IOException exception)
        {
            throw new ModuleManagementToolException("Unable to load default extension file mapping properties.", exception);
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (Throwable e ) {}
            }
        }
        return mappingProperties;
    }
    
    /**
     * Cleans the WAR file of all files relating to the currently installed version of the the Module.
     * 
     * @param warFileLocation    the war file location
     * @param moduleId          the module id
     * @param preview           indicates whether this is a preview installation
     * @param purge             Fully delete all files (including those marked "PRESERVED")
     * @throws IOException 
     */
    public void uninstallModule(String moduleId,String warFileLocation, boolean preview, boolean purge) throws IOException
    {
        InstalledFiles installedFiles = new InstalledFiles(warFileLocation, moduleId);
        installedFiles.load();
                
        for (String add : installedFiles.getAdds())
        {
            // Remove file
            removeFile(warFileLocation, add, preview);
        }
        for (String mkdir : installedFiles.getMkdirs())
        {
            // Remove folder
            removeFile(warFileLocation, mkdir, preview);
        }
        for (Map.Entry<String, String> update : installedFiles.getUpdates().entrySet())
        {
            if (preview == false)
            {
                // Recover updated file and delete backups
               TFile modified = new TFile(warFileLocation + update.getKey());
               TFile backup = new TFile(warFileLocation + update.getValue());
               backup.cp_rp(modified);
               backup.rm();
            }
            
            outputVerboseMessage("Recovering file '" + update.getKey() + "' from backup '" + update.getValue() + "'", true);
        }
        // Now remove the installed files list
        String installedFilesPathInWar = installedFiles.getFilePathInWar();
        removeFile(warFileLocation, installedFilesPathInWar, preview);
        // Remove the module properties
        String modulePropertiesFileLocationInWar = ModuleDetailsHelper.getModulePropertiesFilePathInWar(moduleId);
        removeFile(warFileLocation, modulePropertiesFileLocationInWar, preview);
    }
    
    /**
     * Removes a file from the given location in the war file.
     * 
     * @param warLocation   the war file location
     * @param filePath      the path to the file that is to be deleted
     * @param preview       indicates whether this is a preview install
     */
    private void removeFile(String warLocation, String filePath, boolean preview)
    {
        TFile removeFile = new TFile(warLocation + filePath);
        if (removeFile.exists() == true)
        {
            outputVerboseMessage("Removing file '" + filePath + "' from war", true);
            if (preview == false)
            {
                removeFile.delete();
            }
        }
        else
        {
            outputVerboseMessage("The file '" + filePath + "' was expected for removal but was not present in the war", true);
        }
    }
    
    /**
     * Copies a file from the AMP location to the correct location in the WAR, interating on directories where appropraite.
     * 
     * @param ampFileLocation   the AMP file location
     * @param warFileLocation   the WAR file location
     * @param sourceDir         the directory in the AMP to copy from.  It must start with "/".
     * @param destinationDir    the directory in the WAR to copy to.  It must start with "/".
     * @param installedFiles    a list of the currently installed files
     * @param preview           indicates whether this is a preview install or not
     * @param forceInstall      indicates whether the installed files will be replaces regardless of the currently installed 
     *                          version of the AMP.
     * @throws IOException      throws any IOExpceptions thar are raised
     */
    private void calculateCopyToWar(String ampFileLocation, String warFileLocation, String sourceDir, String destinationDir, InstalledFiles installedFiles, boolean preview, boolean forceInstall)
        throws IOException
    {
        if (sourceDir.length() == 0 || !sourceDir.startsWith("/"))
        {
            throw new IllegalArgumentException("sourceDir must start with '/'");
        }
        if (destinationDir.length() == 0 || !destinationDir.startsWith("/"))
        {
            throw new IllegalArgumentException("destinationDir must start with '/'");
        }
        
        // Handle source and destination if they are just the root '/'
        if (sourceDir.equals("/"))
        {
            sourceDir = "";
        }
        if (destinationDir.equals("/"))
        {
            destinationDir = "";
        }
        
        String sourceLocation = ampFileLocation + sourceDir;               
        TFile ampConfig = new TFile(sourceLocation);
        
        java.io.File[] files = ampConfig.listFiles();  
        if (files != null)
        {
            for (java.io.File sourceChild : files)
            {
                String destinationFileLocation = warFileLocation + destinationDir + "/" + sourceChild.getName();
                TFile destinationChild = new TFile(destinationFileLocation);
                if (sourceChild.isFile() == true)
                {
                    String backupLocation = null;
                    boolean createFile = false;
                    if (destinationChild.exists() == false)
                    {
                        createFile = true;
                    }
                    else
                    {
                        if (forceInstall)
                        {
                            // Backup file about to be updated
                            backupLocation = BACKUP_DIR + "/" + generateGuid() + ".bin";
                            if (preview == false)
                            {
                                //Create the directory if it doesn't exist.
                                TFile backupLocationDirectory = new TFile(warFileLocation+ BACKUP_DIR);
                                if (!backupLocationDirectory.exists())
                                {
                                    backupLocationDirectory.mkdir();
                                }
                                
                                //Backup the file
                                TFile backupFile = new TFile(warFileLocation + backupLocation);
                                destinationChild.cp_rp(backupFile);
                            }
                        } else {
                            //Not a forced install, there is an existing file in the war, lets rollback the transaction, 
                            //throw an error and explain the problem.
//                            File.
//                            ZipController zipController = ZipController.getInstance(warFile);
//                            zipController.reset();
                            throw new ModuleManagementToolException("ERROR: The amp will overwrite an existing file in the war '" + destinationDir + "/" + sourceChild.getName() + "'. Execution halted.  By specifying -force , you can force installation of AMP regardless of the current war state.");
                        }
                    }
                    
                    if (createFile == true)
                    {
                        installedFiles.addAdd(destinationDir + "/" + sourceChild.getName());
                        this.outputVerboseMessage("File '" + destinationDir + "/" + sourceChild.getName() + "' added to war from amp", true);
                    }
                    else
                    {
                        installedFiles.addUpdate(destinationDir + "/" + sourceChild.getName(), backupLocation);
                        this.outputMessage("WARNING: The file '" + destinationDir + "/" + sourceChild.getName() + "' is being overwritten by this module. The original has been backed-up to '" + backupLocation + "'", true);
                    }
                }
                else
                {
                    boolean mkdir = false;
                    if (destinationChild.exists() == false)
                    {
                        mkdir = true;
                    }
                    
                    calculateCopyToWar(ampFileLocation, warFileLocation, sourceDir + "/" + sourceChild.getName(), 
                                                                destinationDir + "/" + sourceChild.getName(), installedFiles, preview, forceInstall);
                    if (mkdir == true)
                    {
                        installedFiles.addMkdir(destinationDir + "/" + sourceChild.getName());
                        this.outputVerboseMessage("Directory '" + destinationDir + "/" + sourceChild.getName() + "' added to war", true);
                    }
                }
            }
        }
    }
    
    /**
     * @throws  UnsupportedOperationException
     */
    public void disableModule(String moduleId, String warLocation)
    {
        throw new UnsupportedOperationException("Disable module is not currently supported");
    }
    
    /**
     * @throws  UnsupportedOperationException
     */
    public void enableModule(String moduleId, String warLocation)
    {
        throw new UnsupportedOperationException("Enable module is not currently supported");
    }
    
    /**
     * Lists all the currently installed modules in the WAR
     * 
     * @param warLocation   the war location
     * @throws ModuleManagementToolException
     */
    public void listModules(String warLocation)
    {
        boolean previous = this.verbose;
        this.verbose = true;

        try
        {
            List<ModuleDetails> modulesFound =  warHelper.listModules(new TFile(warLocation));

            if (modulesFound.size() < 1)
            {
                outputVerboseMessage("No modules are installed in this WAR file");
            }

            for (Iterator<ModuleDetails> iterator = modulesFound.iterator(); iterator.hasNext(); ) {
                ModuleDetails moduleDetails =  iterator.next();
                outputVerboseMessage("Module '" + moduleDetails.getId() + "' installed in '" + warLocation + "'");
                outputVerboseMessage("   Title:        " + moduleDetails.getTitle(), true);
                outputVerboseMessage("   Version:      " + moduleDetails.getModuleVersionNumber(), true);
                outputVerboseMessage("   Install Date: " + moduleDetails.getInstallDate(), true);
                outputVerboseMessage("   Description:   " + moduleDetails.getDescription(), true);
            }

        }
        finally
        {
            this.verbose = previous;
        }
    }
    
    /**
     * Outputs a message the console (in verbose mode).
     * 
     * @param message   the message to output
     */
    private void outputVerboseMessage(String message)
    {
        outputMessage(message, false, false, false);
    }
    
    /**
     * Outputs a message the console (in verbose mode).
     * 
     * @param message   the message to output
     */
    private void outputErrorMessage(String message)
    {
        outputMessage(message, false, true, false);
    }
    
    /**
     * Outputs a message the console (in verbose mode).
     * 
     * @param message   the message to output
     * @param indent    indicates that the message should be formated with an indent
     */
    private void outputVerboseMessage(String message, boolean indent)
    {
        outputMessage(message, indent, false, false);
    }
    
    /**
     * Outputs a message to the console regardless of the verbose setting.
     * 
     * @param message   the message to output
     * @param indent    indicates that the message should be formated with an indent
     */
    private void outputMessage(String message, boolean indent)
    {
    	outputMessage(message, indent, false, true);
    }
    
    /**
     * Outputs a message the console. Errors are always output, but others are only output in verbose mode.
     * 
     * @param message   the message to output
     * @param indent    indicates that the message should be formated with an indent
     * @param error     indicates that the message is an error.
     * @param stdout	indicates that the message should output to the console regardless of verbose setting
     */
    private void outputMessage(String message, boolean indent, boolean error, boolean stdout)
    {
        if (indent == true)
        {
            message = "   - " + message;
        }
        if (error)
        {
            System.err.println(message);
        }
        else if (this.verbose == true || stdout == true)
        {
            System.out.println(message);
        }
    }
    
    /**
     * Main
     * 
     * @param args  command line interface arguments 
     */
    public static void main(String[] args)
    {
        if (args.length <= 1)
        {
            outputUsage();
            System.exit(ERROR_EXIT_CODE);
        }
        ModuleManagementTool manager = new ModuleManagementTool();
        
        String operation = args[0];
        try
        {
        	if (operation.equals(OPTION_HELP) == true)
        	{
        		outputUsage();
        		System.exit(SUCCESS_EXIT_CODE);
        	} 
        	else if (operation.equals(OP_INSTALL) == true)
            {
            	if (args.length < 3) 
            	{
            		throw new UsageException(OP_INSTALL + " requires at least 3 arguments.");
            	}
                String aepFileLocation = args[1];
                String warFileLocation = args[2];
                boolean forceInstall = false;
                boolean previewInstall = false;
                boolean backup = true;
                boolean directory = false;
                
                if (args.length > 3)
                {
                    for (int i = 3; i < args.length; i++)
                    {
                        String option = args[i];
                        if (OPTION_VERBOSE.equals(option) == true)
                        {
                            manager.setVerbose(true);
                        }
                        else if (OPTION_FORCE.equals(option) == true)
                        {
                            forceInstall = true;
                        }
                        else if (OPTION_PREVIEW.equals(option) == true)
                        {
                            previewInstall = true;
                            manager.setVerbose(true);
                        }
                        else if (OPTION_NOBACKUP.equals(option) == true)
                        {
                            backup = false;
                        }
                        else if (OPTION_DIRECTORY.equals(option) == true)
                        {
                            directory = true;
                        }
                    }
                }
               
                try
                {
                    if (directory == false)
                    {
                        // Install the module
                        manager.installModule(aepFileLocation, warFileLocation, previewInstall, forceInstall, backup);
                    }
                    else
                    {
                        // Install the modules from the directory
                        manager.installModules(aepFileLocation, warFileLocation, previewInstall, forceInstall, backup);
                    }
                }
                catch (IOException error)
                {
                    throw new ModuleManagementToolException(error.getMessage());    
                }
                System.exit(SUCCESS_EXIT_CODE);

            }
            else if (OP_LIST.equals(operation) == true)
            {
            	if (args.length != 2) 
            	{
            		throw new UsageException(OP_LIST + " requires 2 arguments.");
            	}
                // List the installed modules
                String warFileLocation = args[1];
                manager.listModules(warFileLocation);                
                System.exit(SUCCESS_EXIT_CODE);
            }
            else if (OP_UNINSTALL.equals(operation) == true)
            {
            	if (args.length < 3) 
            	{
            		throw new UsageException(OP_UNINSTALL + " requires at least 3 arguments.");
            	}
                String moduleId = args[1];
                String warLocation = args[2];
                boolean purge = false;
                boolean preview = false;
                
                if (args.length >= 4) 
                {
                    for (int i = 3; i < args.length; i++)
                    {
                        String option = args[i];
                        if (OPTION_PURGE.equals(option) == true) 
                        {
                            purge = true;
                        }
                        if (OPTION_PREVIEW.equals(option) == true)
                        {
                            preview = true;
                            manager.setVerbose(true);
                        }
                    }
                }
                manager.setVerbose(true);
                manager.uninstallModule(moduleId, warLocation,preview, purge);
                System.exit(SUCCESS_EXIT_CODE);
            }
            else
            {
                throw new UsageException("Unknown operation " + operation + ".");
            }
        
        }
        catch (UsageException e) 
        {
        	manager.outputErrorMessage("Usage error: " + e.getMessage());
        	outputUsage();
        	System.exit(ERROR_EXIT_CODE);
        }
        catch (ModuleManagementToolException e)
        {
            // These are user-friendly
            manager.outputErrorMessage(e.getMessage());
            System.exit(ERROR_EXIT_CODE);
        }
        catch (IOException error)
        {
            manager.outputErrorMessage(error.getMessage());
            System.exit(ERROR_EXIT_CODE);
        }
    }

    /**
     * Generates a GUID, avoiding undesired imports.
     */
    private static String generateGuid()
    {
        return UUIDGenerator.getInstance().generateTimeBasedUUID().toString();
    }

    /**
     * Outputs the module management tool usage
     */
    private static void outputUsage()
    {
        System.out.println("Module managment tool available commands:");
        System.out.println("-----------------------------------------------------------\n");        
        System.out.println("install: Installs a AMP file(s) into an Alfresco WAR file, updates if an older version is already installed.");
        System.out.println("usage:   install <AMPFileLocation> <WARFileLocation> [options]");
        System.out.println("valid options: ");
        System.out.println("   -verbose   : enable verbose output");
        System.out.println("   -directory : indicates that the amp file location specified is a directory.");
        System.out.println("                All amp files found in the directory and its sub directories are installed.");
        System.out.println("   -force     : forces installation of AMP regardless of currently installed module version");
        System.out.println("   -preview   : previews installation of AMP without modifying WAR file");
        System.out.println("   -nobackup  : indicates that no backup should be made of the WAR\n");
        System.out.println("-----------------------------------------------------------\n");
        System.out.println("list:  Lists all the modules currently installed in an Alfresco WAR file.");
        System.out.println("usage: list <WARFileLocation>\n");
        System.out.println("-----------------------------------------------------------\n");
        System.out.println("uninstall:  Uninstalls a module from the Alfresco WAR file.");
        System.out.println("usage: uninstall <ModuleId> <WARFileLocation>\n");
        System.out.println("-----------------------------------------------------------\n");    
    }

    @Override
    public void info(Object message)
    {
        outputVerboseMessage(String.valueOf(message));
    }    

    private static class UsageException extends Exception 
    {
		private static final long serialVersionUID = 1L;
		
		public UsageException(String message) {
			super(message);
		}
    }
 
}
