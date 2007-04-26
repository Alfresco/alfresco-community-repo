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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.alfresco.repo.module.ModuleDetailsImpl;
import org.alfresco.service.cmr.module.ModuleDetails;

import de.schlichtherle.io.File;
import de.schlichtherle.io.FileInputStream;
import de.schlichtherle.io.FileOutputStream;

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
        try
        {
            Properties properties = new Properties();
            properties.load(is);
            return new ModuleDetailsImpl(properties);
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
     */
    public static ModuleDetails createModuleDetailsFromPropertyLocation(String location)
    {
        ModuleDetails result = null;
        try
        {
            File file = new File(location, ModuleManagementTool.DETECTOR_AMP_AND_WAR);
            if (file.exists())
            {
                InputStream is = new FileInputStream(file);
                result = createModuleDetailsFromPropertiesStream(is);
            }
        }
        catch (IOException exception)
        {
            throw new ModuleManagementToolException("Unable to load module details from property file.", exception);
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
     */
    public static ModuleDetails createModuleDetailsFromWarAndId(String warLocation, String moduleId)
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
    public static File getModuleDetailsFileFromWarAndId(String warLocation, String moduleId)
    {
        String location = ModuleDetailsHelper.getModulePropertiesFileLocation(warLocation, moduleId);
        File file = new File(location, ModuleManagementTool.DETECTOR_AMP_AND_WAR);
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
        return ModuleManagementTool.MODULE_DIR + "/" + moduleId + "/" + "module.properties";
    }
    
    /**
     * Saves the module details to the war in the correct location based on the module id
     * 
     * @param warLocation   the war location
     * @param moduleId      the module id
     */
    public static void saveModuleDetails(String warLocation, ModuleDetails moduleDetails)
    {
        // Ensure that it is a valid set of properties
        String moduleId = moduleDetails.getId();
        try
        {
            String modulePropertiesFileLocation = getModulePropertiesFileLocation(warLocation, moduleId);
            File file = new File(modulePropertiesFileLocation, ModuleManagementTool.DETECTOR_AMP_AND_WAR);
            if (file.exists() == false)
            {
                file.createNewFile();
            }  
            
            // Get all the module properties
            Properties moduleProperties = moduleDetails.getProperties();
            OutputStream os = new FileOutputStream(file);
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
