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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileOutputStream;
import de.schlichtherle.truezip.file.TFileReader;

/**
 * Details of the files installed during a module installation into a WAR
 * 
 * @author Roy Wetherall
 */
public class InstalledFiles
{
    /** Modification types */
    private static final String MOD_ADD_FILE = "add";
    private static final String MOD_UPDATE_FILE = "update";
    private static final String MOD_MK_DIR = "mkdir";
    
    /** Delimieter used in the file */
    private static final String DELIMITER = "|";
    
    /** War location **/
    private String warLocation;
    
    /** Module id **/
    private String moduleId;
    
    /** Lists containing the modifications made */
    private List<String> adds = new ArrayList<String>();
    private Map<String, String> updates = new HashMap<String, String>();
    private List<String> mkdirs = new ArrayList<String>();
    
    /**
     * Constructor
     * 
     * @param warLocation   the war location
     * @param moduleId      the module id
     */
    public InstalledFiles(String warLocation, String moduleId)
    {
        this.warLocation = warLocation;
        this.moduleId = moduleId;
    }
    
    /**
     * Loads the exisiting information about the installed files from the WAR
     */
    public void load()
    {
        TFile file = new TFile(getFileLocation());
        if (file.exists() == true)
        {
            try
            {
                BufferedReader reader = new BufferedReader(new TFileReader(file));
                try
                {
                    String line = reader.readLine();
                    while (line != null)
                    {
                        String[] modification = line.split("\\" + DELIMITER);
                        String mod = modification[0];
                        String location = modification[1];
                        if (mod.equals(MOD_ADD_FILE) == true)
                        {
                            this.adds.add(location);
                        }
                        else if (mod.equals(MOD_MK_DIR) == true)
                        {
                            this.mkdirs.add(location);
                        }
                        else if (mod.equals(MOD_UPDATE_FILE) == true)
                        {
                            this.updates.put(location, modification[2]);
                        }
                        line = reader.readLine();
                    }
                }
                finally
                {
                    reader.close();
                }
            }
            catch(FileNotFoundException exception)
            {
                throw new ModuleManagementToolException("The module file install file '" + getFileLocation() + "' does not exist", exception);
            }
            catch(IOException exception)
            {
                throw new ModuleManagementToolException("Error whilst reading file '" + getFileLocation(), exception);
            }
        }
        else 
        {
            throw new ModuleManagementToolException("Invalid module.  The installation file does not exist for module: "+moduleId);
        }
    }
    
    /**
     * Saves the current modification details into the WAR
     */
    public void save()
    {
        try
        {
            TFile file = new TFile(getFileLocation());
            if (file.exists() == false)
            {
                file.createNewFile();               
            } 
            TFileOutputStream os = new TFileOutputStream(file);
            try
            {
                for (String add : this.adds)
                {
                    String output = MOD_ADD_FILE + DELIMITER + add + "\n";
                    os.write(output.getBytes());
                }
                for (Map.Entry<String, String> update : this.updates.entrySet())
                {
                    String output = MOD_UPDATE_FILE + DELIMITER + update.getKey() + DELIMITER + update.getValue() + "\n";
                    os.write(output.getBytes());
                }
                for (String mkdir : this.mkdirs)
                {
                    String output = MOD_MK_DIR + DELIMITER + mkdir + "\n";
                    os.write(output.getBytes());
                }
            }
            finally
            {
                os.close();
            }
        }
        catch(IOException exception)
        {
            throw new ModuleManagementToolException("Error whilst saving modifications file.", exception);
        }
    }
    
    /**
     * Returns the location of the modifications file based on the module id
     * 
     * @return  the file location
     */
    public String getFileLocation()
    {
        return this.warLocation + getFilePathInWar();
    }
    
    /**
     * @return      Returns the path of the install file within the WAR
     */
    public String getFilePathInWar()
    {
        return WarHelper.MODULE_NAMESPACE_DIR + "/" + this.moduleId + "/modifications.install";
    }
    
    /**
     * Get all the added files
     * 
     * @return list of files added to war
     */
    public List<String> getAdds()
    {
        return adds;
    }
    
    /** 
     * Get all the updated files, key is the file that has been updated and the value is the 
     * location of the backup made before modification took place.
     * 
     * @return  map of file locaiton and backup
     */
    public Map<String, String> getUpdates()
    {
        return updates;
    }
    
    /**
     * Gets a list of the dirs added during install
     * 
     * @return  list of directories added
     */
    public List<String> getMkdirs()
    {
        return mkdirs;
    }
    
    /**
     * Add a file addition
     * 
     * @param location  the file added
     */
    public void addAdd(String location)
    {
        this.adds.add(location);
    }
    
    /**
     * Add a file update
     * 
     * @param location  the file updated
     * @param backup    the backup location
     */
    public void addUpdate(String location, String backup)
    {
        this.updates.put(location, backup);
    }
    
    /**
     * Add a directory 
     * 
     * @param location  the directory location
     */
    public void addMkdir(String location)
    {
        this.mkdirs.add(location);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("InstalledFiles [warLocation=").append(this.warLocation).append(", moduleId=")
                    .append(this.moduleId).append(", adds=").append(this.adds).append(", updates=")
                    .append(this.updates).append(", mkdirs=").append(this.mkdirs).append("]");
        return builder.toString();
    }
}
