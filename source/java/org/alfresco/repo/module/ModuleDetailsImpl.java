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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.alfresco.repo.module.tool.ModuleManagementToolException;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleInstallState;
import org.alfresco.util.VersionNumber;

/**
 * Module details implementation.
 * 
 * Loads details from the serialized properties file provided.
 * 
 * @author Roy Wetherall 
 */
public class ModuleDetailsImpl implements ModuleDetails
{
    /** Property names */
    protected static final String PROP_ID = "module.id";
    protected static final String PROP_TITLE = "module.title";
    protected static final String PROP_DESCRIPTION = "module.description";
    protected static final String PROP_VERSION = "module.version";
    protected static final String PROP_INSTALL_DATE = "module.installDate";
    protected static final String PROP_INSTALL_STATE = "module.installState";
    
    /** Properties object */
    protected Properties properties;
    
    /** 
     * Constructor
     * 
     * @param is    input stream, which will be closed
     */
    public ModuleDetailsImpl(InputStream is)
    {
        try
        {
            this.properties = new Properties();
            this.properties.load(is);            
        }
        catch (IOException exception)
        {
            throw new ModuleManagementToolException("Unable to load module details from property file.", exception);
        }
        finally
        {
            try { is.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }
    
    /**
     * Constructor
     *  
     * @param id                module id
     * @param versionNumber     version number
     * @param title             title   
     * @param description       description
     */
    public ModuleDetailsImpl(String id, VersionNumber versionNumber, String title, String description)
    {
        this.properties = new Properties();
        this.properties.setProperty(PROP_ID, id);
        this.properties.setProperty(PROP_VERSION, versionNumber.toString());
        this.properties.setProperty(PROP_TITLE, title);
        this.properties.setProperty(PROP_DESCRIPTION, description);
    }

    /**
     * @see org.alfresco.service.cmr.module.ModuleDetails#exists()
     */
    public boolean exists()
    {
        return (this.properties != null);
    }
    
    /**
     * @see org.alfresco.service.cmr.module.ModuleDetails#getId()
     */
    public String getId()
    {
        return this.properties.getProperty(PROP_ID);
    }
    
    /**
     * @see org.alfresco.service.cmr.module.ModuleDetails#getVersionNumber()
     */
    public VersionNumber getVersionNumber()
    {
        return new VersionNumber(this.properties.getProperty(PROP_VERSION));
    }
    
    /**
     * @see org.alfresco.service.cmr.module.ModuleDetails#getTitle()
     */
    public String getTitle()
    {
        return this.properties.getProperty(PROP_TITLE);
    }
    
    /**
     * @see org.alfresco.service.cmr.module.ModuleDetails#getDescription()
     */
    public String getDescription()
    {
        return this.properties.getProperty(PROP_DESCRIPTION);
    }
    
    /**
     * @see org.alfresco.service.cmr.module.ModuleDetails#getInstalledDate()
     */
    public String getInstalledDate()
    {
        return this.properties.getProperty(PROP_INSTALL_DATE);
    }
    
    /**
     * @see org.alfresco.service.cmr.module.ModuleDetails#getInstallState()
     */
    public ModuleInstallState getInstallState()
    {
        ModuleInstallState result = ModuleInstallState.INSTALLED;
        String value = this.properties.getProperty(PROP_INSTALL_STATE);
        if (value != null && value.length() != 0)
        {
            result = ModuleInstallState.valueOf(value);
        }
        return result;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getId();
    }
}
