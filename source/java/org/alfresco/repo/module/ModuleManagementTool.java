/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.module;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.VersionNumber;
import org.apache.log4j.Logger;

import de.schlichtherle.io.DefaultRaesZipDetector;
import de.schlichtherle.io.File;
import de.schlichtherle.io.FileInputStream;
import de.schlichtherle.io.FileOutputStream;
import de.schlichtherle.io.ZipControllerException;
import de.schlichtherle.io.ZipDetector;
import de.schlichtherle.io.ZipWarningException;

/**
 * @author Roy Wetherall
 */
public class ModuleManagementTool
{
    public static Logger logger = Logger.getLogger("org.alfresco.repo.extension.ModuleManagementTool");
    
    private static final String DEFAULT_FILE_MAPPING_PROPERTIES = "org/alfresco/repo/module/default-file-mapping.properties";
    private static final String MODULE_DIR = "/WEB-INF/classes/alfresco/module";
    
    private static final String DELIMITER = ":";
    
    private static final String PROP_ID = "module.id";
    private static final String PROP_TITLE = "module.title";
    private static final String PROP_DESCRIPTION = "module.description";
    private static final String PROP_VERSION = "module.version";
    
    private static final String MOD_ADD_FILE = "add";
    private static final String MOD_UPDATE_FILE = "update";
    private static final String MOD_MK_DIR = "mkdir";
    
    private static final String OP_INSTALL = "install";
    
    private ZipDetector defaultDetector;
    
    private Properties fileMappingProperties;
    
    private boolean verbose = false;
    
    public ModuleManagementTool()
    {
        // Create the default zip detector
        this.defaultDetector = new DefaultRaesZipDetector("amp|war");
        
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
    
    public boolean isVerbose()
    {
        return verbose;
    }
    
    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }
    
    public void installModule(String ampFileLocation, String warFileLocation)
    {
        try
        {
            // Load the extension properties
            File installingPropertiesFile = new File(ampFileLocation + "/module.properties", this.defaultDetector);
            if (installingPropertiesFile.exists() == false)
            {
                throw new ModuleManagementToolException("Extension properties are not present in the AMP.  Check that a valid module.properties file is present.");
            }
            Properties installingProperties = new Properties();
            installingProperties.load(new FileInputStream(installingPropertiesFile));

            // Get the intalling extension version
            String installingVersionString = installingProperties.getProperty(PROP_VERSION);
            if (installingVersionString == null || installingVersionString.length() == 0)
            {
                throw new ModuleManagementToolException("The version number has not been specified in the module properties found in the AMP.");                
            }
            VersionNumber installingVersion = new VersionNumber(installingVersionString);
            
            // Get the installed directory
            File installDir = getInstalledDir(warFileLocation);
            
            // Look for a previously installed version of this extension
            File installedExtensionPropertiesFile = new File(installDir.getPath() + "/" + getModuleDetailsFileName(installingProperties.getProperty(PROP_ID)), this.defaultDetector);
            if (installedExtensionPropertiesFile.exists() == true)
            {
                Properties installedExtensionProperties = new Properties();
                InputStream is = new FileInputStream(installedExtensionPropertiesFile);
                installedExtensionProperties.load(is);
                
                // Get the installed version
                VersionNumber installedVersion = new VersionNumber(installedExtensionProperties.getProperty(PROP_VERSION));
                int compareValue = installedVersion.compareTo(installingVersion);
                if (compareValue == -1)
                {
                    // Trying to update the extension, old files need to cleaned before we proceed
                    cleanWAR(warFileLocation, installedExtensionProperties);
                }
                else if (compareValue == 0)
                {
                    // Trying to install the same extension version again
                    verboseMessage("WARNING: This version of this module is already installed in the WAR");
                    throw new ModuleManagementToolException("This version of this module is alreay installed.  Use the 'force' parameter if you want to overwrite the current installation.");                    
                }
                else if (compareValue == 1)
                {
                    // Trying to install an earlier version of the extension
                    verboseMessage("WARNING: A later version of this module is already installed in the WAR");
                    throw new ModuleManagementToolException("An earlier version of this module is already installed.  You must first unistall the current version before installing this version of the module.");
                }
                
            }
            
            // TODO check for any additional file mapping propeties supplied in the AEP file
            
            // Copy the files from the AEP file into the WAR file
            Map<String, String> modifications = new HashMap<String, String>(50);
            for (Map.Entry<Object, Object> entry : this.fileMappingProperties.entrySet())
            {
                modifications.putAll(copyToWar(ampFileLocation, warFileLocation, (String)entry.getKey(), (String)entry.getValue()));
            }    
            
            // Copy the properties file into the war
            if (installedExtensionPropertiesFile.exists() == false)
            {
                installedExtensionPropertiesFile.createNewFile();               
            }            
            InputStream is = new FileInputStream(installingPropertiesFile);
            try
            {
                installedExtensionPropertiesFile.catFrom(is);
            }
            finally
            {
                is.close();
            }
            
            // Create and add the modifications file to the war
            writeModificationToFile(installDir.getPath() + "/" + getModuleModificationFileName(installingProperties.getProperty(PROP_ID)), modifications);
        
            // Update the zip file's
            File.update(); 
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
    
    private void cleanWAR(String warFileLocation, Properties installedExtensionProperties)
    {
        // Get the currently installed modifications
        Map<String, String> modifications = readModificationsFromFile(warFileLocation + "/" + getModuleModificationFileName(installedExtensionProperties.getProperty(PROP_ID)));
        
        for (Map.Entry<String, String> modification : modifications.entrySet())
        {
            String modType = modification.getValue();
            if (MOD_ADD_FILE.equals(modType) == true)
            {
                // Remove file 
            }
            else if (MOD_UPDATE_FILE.equals(modType) == true)
            {
                // Remove file
                // Replace with back-up
            }
            else if (MOD_MK_DIR.equals(modType) == true)
            {
                // Add to list of dir's to remove at the end
            }
        }
    }

    private Map<String, String> copyToWar(String aepFileLocation, String warFileLocation, String sourceDir, String destinationDir)
        throws IOException
    {
        Map<String, String> result = new HashMap<String, String>(10);
        
        String sourceLocation = aepFileLocation + sourceDir;               
        File aepConfig = new File(sourceLocation, this.defaultDetector);
        
        for (java.io.File sourceChild : aepConfig.listFiles())
        {
            String destinationFileLocation = warFileLocation + destinationDir + "/" + sourceChild.getName();
            File destinationChild = new File(destinationFileLocation, this.defaultDetector);
            if (sourceChild.isFile() == true)
            {
                boolean createFile = false;
                if (destinationChild.exists() == false)
                {
                    destinationChild.createNewFile();
                    createFile = true;
                }
                FileInputStream fis = new FileInputStream(sourceChild);
                try
                {
                    destinationChild.catFrom(fis);
                }
                finally
                {
                    fis.close();
                }
                
                if (createFile == true)
                {
                    result.put(destinationDir + "/" + sourceChild.getName(), MOD_ADD_FILE);
                    this.verboseMessage("File added: " + destinationDir + "/" + sourceChild.getName());
                }
                else
                {
                    result.put(destinationDir + "/" + sourceChild.getName(), MOD_UPDATE_FILE);
                    this.verboseMessage("File updated:" + destinationDir + "/" + sourceChild.getName());
                }
            }
            else
            {
                boolean mkdir = false;
                if (destinationChild.exists() == false)
                {
                    destinationChild.mkdir();
                    mkdir = true;
                }
                
                Map<String, String> subResult = copyToWar(aepFileLocation, warFileLocation, sourceDir + "/" + sourceChild.getName(), 
                                                            destinationDir + "/" + sourceChild.getName());
                result.putAll(subResult);
                
                if (mkdir == true)
                {
                    result.put(destinationDir + "/" + sourceChild.getName(), MOD_MK_DIR);
                    this.verboseMessage("Directory added: " + destinationDir + "/" + sourceChild.getName());
                }
            }
        }
        
        return result;
    }
    
    private File getInstalledDir(String warFileLocation)
    {
        // Check for the installed directory in the WAR file
        File installedDir = new File(warFileLocation + MODULE_DIR, this.defaultDetector);
        if (installedDir.exists() == false)
        {
            installedDir.mkdir();
        }
        return installedDir;
    }
    
    public void disableModule(String moduleId, String warLocation)
    {
        System.out.println("Currently unsupported ...");
    }
    
    public void enableModule(String moduleId, String warLocation)
    {
        System.out.println("Currently unsupported ...");
    }
    
    public void uninstallModule(String moduleId, String warLocation)
    {
        System.out.println("Currently unsupported ...");
    }
    
    public void listModules(String warLocation)
    {
        System.out.println("Currently unsupported ...");
    }
    
    private void verboseMessage(String message)
    {
        if (this.verbose == true)
        {
            System.out.println(message);
        }
    }
    
    private void writeModificationToFile(String fileLocation, Map<String, String> modifications)
        throws IOException
    {
        File file = new File(fileLocation, this.defaultDetector);
        if (file.exists() == false)
        {
            file.createNewFile();               
        } 
        FileOutputStream os = new FileOutputStream(file);
        try
        {
            for (Map.Entry<String, String> mod : modifications.entrySet())
            {
                String output = mod.getValue() + DELIMITER + mod.getKey() + "\n";
                os.write(output.getBytes());
            }
        }
        finally
        {
            os.close();
        }
    }
    
    private Map<String, String> readModificationsFromFile(String fileLocation)    
    {
        Map<String, String> modifications = new HashMap<String, String>(50);
        
        File file = new File(fileLocation, this.defaultDetector);        
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            try
            {
                String line = reader.readLine();
                while (line != null)
                {
                    line = reader.readLine();
                    String[] modification = line.split(DELIMITER);
                    modifications.put(modification[1], modification[0]);
                }
            }
            finally
            {
                reader.close();
            }
        }
        catch(FileNotFoundException exception)
        {
            throw new ModuleManagementToolException("The module file install file '" + fileLocation + "' does not exist");
        }
        catch(IOException exception)
        {
            throw new ModuleManagementToolException("Error whilst reading file '" + fileLocation);
        }
        
        return modifications;
    }
    
    private String getModuleDetailsFileName(String moduleId)
    {
        return "module-" + moduleId + ".install";
    }
    
    private String getModuleModificationFileName(String moduleId)
    {
        return "module-" + moduleId + "-modifications.install";
    }
    
    /**
     * @param args 
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
                        
                manager.installModule(aepFileLocation, warFileLocation);
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
    
    private static void outputUsage()
    {
        System.out.println("output useage ...");
    }

}
