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
import java.util.Date;

import org.alfresco.repo.module.ModuleDetailsImpl;
import org.alfresco.service.cmr.module.ModuleInstallState;

import de.schlichtherle.io.File;
import de.schlichtherle.io.FileInputStream;
import de.schlichtherle.io.FileOutputStream;

/**
 * Module details helper used by the module mangement tool
 * 
 * @author Roy Wetherall
 */
public class ModuleDetailsHelper extends ModuleDetailsImpl
{    
    /**
     * Constructor
     * 
     * @param is    input stream
     */
    public ModuleDetailsHelper(InputStream is)
    {
        super(is);
    }

    /**
     * Creates a module details helper object based on a file location.
     * 
     * @param location  file location
     * @return           module details helper object
     */
    public static ModuleDetailsHelper create(String location)
    {
        ModuleDetailsHelper result = null;
        try
        {
            File file = new File(location, ModuleManagementTool.defaultDetector);
            if (file.exists() == true)
            {
                result = new ModuleDetailsHelper(new FileInputStream(file));
            }
        }
        catch (IOException exception)
        {
            throw new ModuleManagementToolException("Unable to load module details from property file.", exception);
        }
        return result;
    }
    
    /**
     * Creates a module details helper object based on a war location and the module id
     * 
     * @param warLocation   the war location
     * @param moduleId      the module id
     * @return              the module details helper
     */
    public static ModuleDetailsHelper create(String warLocation, String moduleId)
    {
        return ModuleDetailsHelper.create(ModuleDetailsHelper.getFileLocation(warLocation, moduleId));
    }
    
    /**
     * Gets the file location
     * 
     * @param warLocation   the war location
     * @param moduleId      the module id
     * @return              the file location
     */
    private static String getFileLocation(String warLocation, String moduleId)
    {
        return warLocation + ModuleManagementTool.MODULE_DIR + "/" + moduleId + "/" + "module.properties";
    }
    
    /**
     * Saves the module detailsin to the war in the correct location based on the module id
     * 
     * @param warLocation   the war location
     * @param moduleId      the module id
     */
    public void save(String warLocation, String moduleId)
    {
        try
        {
            File file = new File(getFileLocation(warLocation, moduleId), ModuleManagementTool.defaultDetector);
            if (file.exists() == false)
            {
                file.createNewFile();               
            }  
            
            OutputStream os = new FileOutputStream(file);
            try
            {
                Date now = new Date();
                this.properties.setProperty(PROP_INSTALL_DATE, now.toString());
                this.properties.store(os, null);
            }
            finally
            {
                os.close();
            }
        }
        catch (IOException exception)
        {
            throw new ModuleManagementToolException("Unable to save module details into WAR file.", exception);
        }
    }
    
    /**
     * Set the install state
     * 
     * @param installState  the install state
     */
    public void setInstallState(ModuleInstallState installState)
    {
        this.properties.setProperty(PROP_INSTALL_STATE, installState.toString());
    }

}
