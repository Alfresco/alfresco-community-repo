/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.module.tool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.alfresco.service.cmr.module.ModuleInstallState;
import org.alfresco.util.GUID;
import org.apache.log4j.Logger;
import org.springframework.util.FileCopyUtils;

import de.schlichtherle.io.DefaultRaesZipDetector;
import de.schlichtherle.io.File;
import de.schlichtherle.io.FileInputStream;
import de.schlichtherle.io.ZipControllerException;
import de.schlichtherle.io.ZipDetector;
import de.schlichtherle.io.ZipWarningException;

/**
 * Module management tool.
 * 
 * Manages the modules installed in a war file.  Allows modules to be installed, updated, enabled, disabled and
 * uninstalled.  Information about the module installed is also available. 
 * 
 * @author Roy Wetherall
 */
public class ModuleManagementTool
{
    /** Logger */
    public static Logger logger = Logger.getLogger("org.alfresco.repo.extension.ModuleManagementTool");
    
    /** Location of the default mapping properties file */
    private static final String DEFAULT_FILE_MAPPING_PROPERTIES = "org/alfresco/repo/module/tool/default-file-mapping.properties";
    
    /** Standard directories found in the alfresco war */
    public static final String MODULE_DIR = "/WEB-INF/classes/alfresco/module";
    public static final String BACKUP_DIR = MODULE_DIR + "/backup";
    
    /** Operations and options supperted via the command line interface to this class */
    private static final String OP_INSTALL = "install";
    private static final String OP_LIST = "list";
    private static final String OPTION_VERBOSE = "-verbose";
    private static final String OPTION_FORCE = "-force";
    private static final String OPTION_PREVIEW = "-preview";
    private static final String OPTION_NOBACKUP = "-nobackup";
    private static final String OPTION_DIRECTORY = "-directory";
    
    /** Default zip detector */
    public static ZipDetector defaultDetector = new DefaultRaesZipDetector("amp|war");
    
    /** File mapping properties */
    private Properties fileMappingProperties;
    
    /** Indicates the current verbose setting */
    private boolean verbose = false;
    
    /**
     * Constructor
     */
    public ModuleManagementTool()
    {
        // Load the default file mapping properties
        this.fileMappingProperties = new Properties();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_FILE_MAPPING_PROPERTIES);
        try
        {
            this.fileMappingProperties.load(is);
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
     * 
     * @param directory
     * @param warFileLocation
     */
    public void installModules(String directory, String warFileLocation)
    {
        installModules(directory, warFileLocation, false, false, true);
    }
    
    /**
     * 
     * @param directoryLocation
     * @param warFileLocation
     * @param preview
     * @param forceInstall
     * @param backupWAR
     */
    public void installModules(String directoryLocation, String warFileLocation, boolean preview, boolean forceInstall, boolean backupWAR)
    {
        java.io.File dir = new java.io.File(directoryLocation);
        if (dir.exists() == true)
        {
            installModules(dir, warFileLocation, preview, forceInstall,backupWAR);
        }
        else
        {
            throw new ModuleManagementToolException("Invalid directory '" + directoryLocation + "'");
        }
    }
    
    /**
     * 
     * @param dir
     * @param warFileLocation
     * @param preview
     * @param forceInstall
     * @param backupWAR
     */
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
     * @see ModuleManagementTool.installModule(String, String, boolean, boolean, boolean)
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
     * @param forceInstall      indicates whether the installed files will be replaces reguarless of the currently installed 
     *                          version of the AMP.  Generally used during development of the AMP.
     * @param backupWAR         indicates whether we should backup the war we are modifying or not
     */
    public void installModule(String ampFileLocation, String warFileLocation, boolean preview, boolean forceInstall, boolean backupWAR)
    {
        try
        {   
            outputMessage("Installing AMP '" + ampFileLocation + "' into WAR '" + warFileLocation + "'");
            
            if (preview == false)
            {
                // Make sure the module and backup directory exisits in the WAR file
                File moduleDir = new File(warFileLocation + MODULE_DIR, defaultDetector);
                if (moduleDir.exists() == false)
                {
                    moduleDir.mkdir();
                }
                File backUpDir = new File(warFileLocation + BACKUP_DIR, defaultDetector);
                if (backUpDir.exists() == false)
                {
                    backUpDir.mkdir();
                }
                
                // Make a backup of the war we are oging to modify
                if (backupWAR == true)
                {
                    java.io.File warFile = new java.io.File(warFileLocation);
                    if (warFile.exists() == false)
                    {
                        throw new ModuleManagementToolException("The war file '" + warFileLocation + "' does not exist.");
                    }
                    String backupLocation = warFileLocation + "-" + System.currentTimeMillis() + ".bak";
                    java.io.File backup = new java.io.File(backupLocation);
                    FileCopyUtils.copy(warFile, backup);
                          
                    outputMessage("WAR has been backed up to '" + backupLocation + "'");
                }
            }
            
            // Get the details of the installing module
            ModuleDetailsHelper installingModuleDetails = ModuleDetailsHelper.create(ampFileLocation + "/module.properties");
            if (installingModuleDetails.exists() == false)
            {
                throw new ModuleManagementToolException("No module.properties file has been found in the installing .amp file '" + ampFileLocation + "'");
            }
            
            // Get the detail of the installed module
            ModuleDetailsHelper installedModuleDetails = ModuleDetailsHelper.create(warFileLocation, installingModuleDetails.getId());
            if (installedModuleDetails != null)
            {            
                int compareValue = installedModuleDetails.getVersionNumber().compareTo(installingModuleDetails.getVersionNumber());
                if (forceInstall == true || compareValue == -1)
                {
                    if (forceInstall == true)
                    {
                        // Warn of forced install
                        outputMessage("WARNING: The installation of this module is being forced.  All files will be removed and replaced reguarless of exiting versions present.");
                    }
                    
                    // Trying to update the extension, old files need to cleaned before we proceed
                    outputMessage("Clearing out files relating to version '" + installedModuleDetails.getVersionNumber().toString() + "' of module '" + installedModuleDetails.getId() + "'");
                    cleanWAR(warFileLocation, installedModuleDetails.getId(), preview);
                }
                else if (compareValue == 0)
                {
                    // Trying to install the same extension version again
                    outputMessage("WARNING: This version of this module is already installed in the WAR");
                    throw new ModuleManagementToolException("This version of this module is alreay installed.  Use the 'force' parameter if you want to overwrite the current installation.");                    
                }
                else if (compareValue == 1)
                {
                    // Trying to install an earlier version of the extension
                    outputMessage("WARNING: A later version of this module is already installed in the WAR");
                    throw new ModuleManagementToolException("An earlier version of this module is already installed.  You must first unistall the current version before installing this version of the module.");
                }
                
            }
            
            // TODO check for any additional file mapping propeties supplied in the AEP file
            
            // Copy the files from the AEP file into the WAR file
            outputMessage("Adding files relating to version '" + installingModuleDetails.getVersionNumber().toString() + "' of module '" + installingModuleDetails.getId() + "'");
            InstalledFiles installedFiles = new InstalledFiles(warFileLocation, installingModuleDetails.getId());
            for (Map.Entry<Object, Object> entry : this.fileMappingProperties.entrySet())
            {
                // Run throught the files one by one figuring out what we are going to do during the copy
                copyToWar(ampFileLocation, warFileLocation, (String)entry.getKey(), (String)entry.getValue(), installedFiles, preview);
                
                if (preview == false)
                {
                    // Get a reference to the source folder (if it isn't present dont do anything
                    File source = new File(ampFileLocation + "/" + entry.getKey(), defaultDetector);
                    if (source != null && source.list() != null)
                    {
                        // Get a reference to the destination folder
                        File destination = new File(warFileLocation + "/" + entry.getValue(), defaultDetector);
                        if (destination == null)
                        {
                            throw new ModuleManagementToolException("The destination folder '" + entry.getValue() + "' as specified in mapping properties does not exist in the war");
                        }
                        // Do the bulk copy since this is quicker than copying file's one by one
                        destination.copyAllFrom(source);             
                    }
                }
            }   
            
            if (preview == false)
            {
                // Save the installed file list
                installedFiles.save();
           
                // Update the installed module details
                installingModuleDetails.setInstallState(ModuleInstallState.INSTALLED);
                installingModuleDetails.save(warFileLocation, installingModuleDetails.getId());

                // Update the zip file's
                File.update(); 
            }               
        }
        catch (ZipWarningException ignore) 
        {
            // Only instances of the class ZipWarningException exist in the chain of
            // exceptions. We choose to ignore this.
        }
        catch (ZipControllerException exception) 
        {
            // At least one exception occured which is not just a ZipWarningException.
            // This is a severe situation that needs to be handled.
            throw new ModuleManagementToolException("A Zip error was encountered during deployment of the AEP into the WAR", exception);
        }
        catch (IOException exception)
        {
            throw new ModuleManagementToolException("An IO error was encountered during deployment of the AEP into the WAR", exception);
        }       
    }
    
    /**
     * Cleans the WAR file of all files relating to the currently installed version of the the AMP.
     * 
     * @param warFileLocatio    the war file location
     * @param moduleId          the module id
     * @param preview           indicates whether this is a preview installation
     */
    private void cleanWAR(String warFileLocation, String moduleId, boolean preview)
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
                File modified = new File(warFileLocation + update.getKey(), defaultDetector);
                File backup = new File(warFileLocation + update.getValue(), defaultDetector);
                modified.copyFrom(backup);
                backup.delete();
            }
            
            outputMessage("Recovering file '" + update.getKey() + "' from backup '" + update.getValue() + "'", true);
        }
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
        File removeFile = new File(warLocation + filePath, defaultDetector);
        if (removeFile.exists() == true)
        {
            outputMessage("Removing file '" + filePath + "' from war", true);
            if (preview == false)
            {
                removeFile.delete();
            }
        }
        else
        {
            outputMessage("The file '" + filePath + "' was expected for removal but was not present in the war", true);
        }
    }

    /**
     * Copies a file from the AMP location to the correct location in the WAR, interating on directories where appropraite.
     * 
     * @param ampFileLocation   the AMP file location
     * @param warFileLocation   the WAR file location
     * @param sourceDir         the directory in the AMP to copy from
     * @param destinationDir    the directory in the WAR to copy to
     * @param installedFiles    a list of the currently installed files
     * @param preview           indicates whether this is a preview install or not
     * @throws IOException      throws any IOExpceptions thar are raised
     */
    private void copyToWar(String ampFileLocation, String warFileLocation, String sourceDir, String destinationDir, InstalledFiles installedFiles, boolean preview)
        throws IOException
    {
        String sourceLocation = ampFileLocation + sourceDir;               
        File ampConfig = new File(sourceLocation, defaultDetector);
        
        java.io.File[] files = ampConfig.listFiles();  
        if (files != null)
        {
            for (java.io.File sourceChild : files)
            {
                String destinationFileLocation = warFileLocation + destinationDir + "/" + sourceChild.getName();
                File destinationChild = new File(destinationFileLocation, defaultDetector);
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
                        // Backup file about to be updated
                        backupLocation = BACKUP_DIR + "/" + GUID.generate() + ".bin";
                        if (preview == false)
                        {
                            File backupFile = new File(warFileLocation + backupLocation, defaultDetector);
                            backupFile.copyFrom(destinationChild);
                        }
                    }
                    
                    if (createFile == true)
                    {
                        installedFiles.addAdd(destinationDir + "/" + sourceChild.getName());
                        this.outputMessage("File '" + destinationDir + "/" + sourceChild.getName() + "' added to war from amp", true);
                    }
                    else
                    {
                        installedFiles.addUpdate(destinationDir + "/" + sourceChild.getName(), backupLocation);
                        this.outputMessage("WARNING: The file '" + destinationDir + "/" + sourceChild.getName() + "' is being updated by this module and has been backed-up to '" + backupLocation + "'", true);
                    }
                }
                else
                {
                    boolean mkdir = false;
                    if (destinationChild.exists() == false)
                    {
                        mkdir = true;
                    }
                    
                    copyToWar(ampFileLocation, warFileLocation, sourceDir + "/" + sourceChild.getName(), 
                                                                destinationDir + "/" + sourceChild.getName(), installedFiles, preview);
                    if (mkdir == true)
                    {
                        installedFiles.addMkdir(destinationDir + "/" + sourceChild.getName());
                        this.outputMessage("Directory '" + destinationDir + "/" + sourceChild.getName() + "' added to war", true);
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
     * @throws  UnsupportedOperationException
     */
    public void uninstallModule(String moduleId, String warLocation)
    {
        throw new UnsupportedOperationException("Uninstall module is not currently supported");
    }
    
    /**
     * Lists all the currently installed modules in the WAR
     * 
     * @param warLocation   the war location
     */
    public void listModules(String warLocation)
    {
        ModuleDetailsHelper moduleDetails = null;
        boolean previous = this.verbose;
        this.verbose = true;
        try
        {
            File moduleDir = new File(warLocation + MODULE_DIR, defaultDetector);
            if (moduleDir.exists() == false)
            {
                outputMessage("No modules are installed in this WAR file");
            }
            
            java.io.File[] dirs = moduleDir.listFiles();
            if (dirs != null && dirs.length != 0)
            {
                for (java.io.File dir : dirs)
                {
                    if (dir.isDirectory() == true)
                    {
                        File moduleProperties = new File(dir.getPath() + "/module.properties", defaultDetector);
                        if (moduleProperties.exists() == true)
                        {
                            try
                            {
                                moduleDetails = new ModuleDetailsHelper(new FileInputStream(moduleProperties));
                            }
                            catch (FileNotFoundException exception)
                            {
                                throw new ModuleManagementToolException("Unable to open module properties file '" + moduleProperties.getPath() + "'");
                            }
                            
                            outputMessage("Module '" + moduleDetails.getId() + "' installed in '" + warLocation + "'");
                            outputMessage("Title: " + moduleDetails.getTitle(), true);
                            outputMessage("Version: " + moduleDetails.getVersionNumber(), true);
                            outputMessage("Install Date: " + moduleDetails.getInstalledDate(), true);                
                            outputMessage("Desription: " + moduleDetails.getDescription(), true); 
                        }
                    }
                }
            }
            else
            {
                outputMessage("No modules are installed in this WAR file");
            }
        }
        finally
        {
            this.verbose = previous;
        }
    }
    
    /**
     * Outputs a message the console (in verbose mode) and the logger.
     * 
     * @param message   the message to output
     */
    private void outputMessage(String message)
    {
        outputMessage(message, false);
    }
    
    /**
     * Outputs a message the console (in verbose mode) and the logger.
     * 
     * @param message   the message to output
     * @prarm indent    indicates that the message should be formated with an indent
     */
    private void outputMessage(String message, boolean indent)
    {
        if (indent == true)
        {
            message = "   - " + message;
        }
        if (this.verbose == true)
        {
            System.out.println(message);
        }
        if (logger.isDebugEnabled() == true)
        {
            logger.debug(message);
        }
    }
    
    /**
     * Main
     * 
     * @param args  command line interface arguments 
     */
    public static void main(String[] args)
    {
        if (args.length >= 1)
        {
            ModuleManagementTool manager = new ModuleManagementTool();
            
            String operation = args[0];
            if (operation.equals(OP_INSTALL) == true && args.length >= 3)
            {            
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
            else if (OP_LIST.equals(operation) == true && args.length == 2)
            {
                // List the installed modules
                String warFileLocation = args[1];
                manager.listModules(warFileLocation);                
            }
            else
            {
                outputUsage();
            }
        }
        else
        {
            outputUsage();
        }
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
    }    
}
