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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.alfresco.repo.module.ModuleDetailsImpl;
import org.alfresco.service.cmr.module.ModuleDetails;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import de.schlichtherle.truezip.file.TFileOutputStream;

/**
 * Module details helper used by the module mangement tool
 * 
 * @author Roy Wetherall
 * @author Derek Hulley
 */
public class ModuleDetailsHelper
{
    /**
     * Factory method to create module details from a stream of a properties file
     * @param is                the properties input stream, which will be closed during the call
     * @return                  Returns the initialized module details
     */
    public static ModuleDetails createModuleDetailsFromPropertiesStream(InputStream is) throws IOException
    {
        return createModuleDetailsFromPropertiesStream(is, null);
    }

    /**
     * Factory method to create module details from a stream of a properties file
     * @param is                the properties input stream, which will be closed during the call
     * @param log               logger
     * @return                  Returns the initialized module details
     */
    public static ModuleDetails createModuleDetailsFromPropertiesStream(InputStream is, LogOutput log) throws IOException
    {
        try
        {
            Properties properties = new Properties();
            properties.load(is);
            return new ModuleDetailsImpl(properties, log);
        }
        finally
        {
            try { is.close(); } catch (Throwable e) {}
        }
    }

    /**
     * Creates a module details helper object based on a file location.
     * 
     * @param location  file location
     * @return          Returns the module details or null if the location points to nothing
     * @throws IOException 
     */
    public static ModuleDetails createModuleDetailsFromPropertyLocation(String location) throws IOException
    {
        return createModuleDetailsFromPropertyLocation(location, null);
    }

    /**
     * Creates a module details helper object based on a file location.
     * 
     * @param location  file location
     * @param log       logger
     * @return          Returns the module details or null if the location points to nothing
     * @throws IOException 
     */
    public static ModuleDetails createModuleDetailsFromPropertyLocation(String location, LogOutput log) throws IOException
    {
        ModuleDetails result = null;
        TFileInputStream is;
        try
        {
            is = new TFileInputStream(location);
        }
        catch (FileNotFoundException error)
        {
            error.printStackTrace(System.out);
            throw new ModuleManagementToolException("Unable to load module details from property file. File Not Found, " + error.getMessage(), error);
            
        }

        try
        {
            result = createModuleDetailsFromPropertiesStream(is, log);
        }
        catch (IOException exception)
        {
            throw new ModuleManagementToolException(
                        "Unable to load module details from property file." + exception.getMessage(), exception);
        }
        finally
        {
            is.close(); // ALWAYS close the stream!
        }

        return result;
    }
    
    /**
     * Creates a module details instance based on a war location and the module id
     * 
     * @param warLocation   the war location
     * @param moduleId      the module id
     * @return              Returns the module details for the given module ID as it occurs in the WAR, or <tt>null</tt>
     *                      if there are no module details available.
     * @throws IOException 
     */
    public static ModuleDetails createModuleDetailsFromWarAndId(String warLocation, String moduleId) throws IOException
    {
        String modulePropertiesFileLocation = ModuleDetailsHelper.getModulePropertiesFileLocation(warLocation, moduleId);
        return ModuleDetailsHelper.createModuleDetailsFromPropertyLocation(modulePropertiesFileLocation);
    }
    
    /**
     * @param warLocation   the location of the WAR file
     * @param moduleId      the module ID within the WAR
     * @return              Returns a file handle to the module properties file within the given WAR.
     *                      The file may or may not exist.
     */
    public static TFile getModuleDetailsFileFromWarAndId(String warLocation, String moduleId)
    {
        String location = ModuleDetailsHelper.getModulePropertiesFileLocation(warLocation, moduleId);
        TFile file = new TFile(location);
        return file;
    }
    
    /**
     * Gets the file location
     * 
     * @param warLocation   the war location
     * @param moduleId      the module id
     * @return              the file location
     */
    public static String getModulePropertiesFileLocation(String warLocation, String moduleId)
    {
        return warLocation + getModulePropertiesFilePathInWar(moduleId);
    }
    
    /**
     * @param moduleId      the module ID
     * @return              Returns the path of the module file within a WAR
     */
    public static String getModulePropertiesFilePathInWar(String moduleId)
    {
        return WarHelper.MODULE_NAMESPACE_DIR + "/" + moduleId + WarHelper.MODULE_CONFIG_IN_WAR;
    }
    
    /**
     * Saves the module details to the war in the correct location based on the module id
     * 
     * @param warLocation   the war location
     * @param moduleDetails      the module id
     */
    public static void saveModuleDetails(String warLocation, ModuleDetails moduleDetails)
    {
        // Ensure that it is a valid set of properties
        String moduleId = moduleDetails.getId();
        try
        {
            String modulePropertiesFileLocation = getModulePropertiesFileLocation(warLocation, moduleId);
            TFile file = new TFile(modulePropertiesFileLocation);
            if (file.exists() == false)
            {
                file.createNewFile();
            }  
            
            // Get all the module properties
            Properties moduleProperties = moduleDetails.getProperties();
            OutputStream os = new TFileOutputStream(file);
            try
            {
                moduleProperties.store(os, null);
            }
            finally
            {
                os.close();
            }
        }
        catch (IOException exception)
        {
            throw new ModuleManagementToolException(
                    "Unable to save module details into WAR file: \n" +
                    "   Module: " + moduleDetails.getId() + "\n" +
                    "   Properties: " + moduleDetails.getProperties(),
                    exception);
        }
    }
}
