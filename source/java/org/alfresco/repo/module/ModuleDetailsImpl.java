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
package org.alfresco.repo.module;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleInstallState;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.VersionNumber;

/**
 * Module details implementation.
 * 
 * Loads details from the serialized properties file provided.
 * 
 * @author Roy Wetherall 
 */
/**
 * @author Derek Hulley
 */
public class ModuleDetailsImpl implements ModuleDetails
{
    private String id;
    private List<String> aliases;
    private VersionNumber version;
    private String title;
    private String description;
    private VersionNumber repoVersionMin;
    private VersionNumber repoVersionMax;
    private Date installDate;
    private ModuleInstallState installState;
    
    /**
     * Private constructor to set default values.
     */
    private ModuleDetailsImpl()
    {
        aliases = new ArrayList<String>(0);
        repoVersionMin = VersionNumber.VERSION_ZERO;
        repoVersionMax = VersionNumber.VERSION_BIG;
        this.installState = ModuleInstallState.UNKNOWN;
    }
    
    /**
     * Creates the instance from a set of properties.  All the property values are trimmed
     * and empty string values are removed from the set.  In other words, zero length or
     * whitespace strings are not supported.
     * 
     * @param properties        the set of properties
     */
    public ModuleDetailsImpl(Properties properties)
    {
        // Set defaults
        this();
        // Copy the properties so they don't get modified
        Properties trimmedProperties = new Properties();
        // Trim all the property values
        for (Map.Entry entry : properties.entrySet())
        {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (value == null)
            {
                // Don't copy nulls over
                continue;
            }
            String trimmedValue = value.trim();
            if (trimmedValue.length() == 0)
            {
                // Don't copy empty strings over
                continue;
            }
            // It is a real value
            trimmedProperties.setProperty(key, trimmedValue);
        }
        
        // Check that the required properties are present
        List<String> missingProperties = new ArrayList<String>(1);
        // ID
        id = trimmedProperties.getProperty(PROP_ID);
        if (id == null)
        {
            missingProperties.add(PROP_ID);
        }
        // ALIASES
        String aliasesStr = trimmedProperties.getProperty(PROP_ALIASES);
        if (aliasesStr != null)
        {
            StringTokenizer st = new StringTokenizer(aliasesStr, ",");
            while (st.hasMoreTokens())
            {
                String alias = st.nextToken().trim();
                if (alias.length() == 0)
                {
                    continue;
                }
                aliases.add(alias);
            }
        }
        // VERSION
        if (trimmedProperties.getProperty(PROP_VERSION) == null)
        {
            missingProperties.add(PROP_VERSION);
        }
        else
        {
            version = new VersionNumber(trimmedProperties.getProperty(PROP_VERSION));
        }
        // TITLE
        title = trimmedProperties.getProperty(PROP_TITLE);
        if (title == null) { missingProperties.add(PROP_TITLE); }
        // DESCRIPTION
        description = trimmedProperties.getProperty(PROP_DESCRIPTION);
        if (description == null) { missingProperties.add(PROP_DESCRIPTION); }
        // REPO MIN
        if (trimmedProperties.getProperty(PROP_REPO_VERSION_MIN) != null)
        {
            repoVersionMin = new VersionNumber(trimmedProperties.getProperty(PROP_REPO_VERSION_MIN));
        }
        // REPO MAX
        if (trimmedProperties.getProperty(PROP_REPO_VERSION_MAX) != null)
        {
            repoVersionMax = new VersionNumber(trimmedProperties.getProperty(PROP_REPO_VERSION_MAX));
        }
        // INSTALL DATE
        if (trimmedProperties.getProperty(PROP_INSTALL_DATE) != null)
        {
            String installDateStr = trimmedProperties.getProperty(PROP_INSTALL_DATE);
            try
            {
                installDate = ISO8601DateFormat.parse(installDateStr);
            }
            catch (Throwable e)
            {
                throw new AlfrescoRuntimeException("Unable to parse install date: " + installDateStr, e);
            }
        }
        // INSTALL STATE
        if (trimmedProperties.getProperty(PROP_INSTALL_STATE) != null)
        {
            String installStateStr = trimmedProperties.getProperty(PROP_INSTALL_STATE);
            try
            {
                installState = ModuleInstallState.valueOf(installStateStr);
            }
            catch (Throwable e)
            {
                throw new AlfrescoRuntimeException("Unable to parse install state: " + installStateStr, e);
            }
        }
        // Check
        if (missingProperties.size() > 0)
        {
            throw new AlfrescoRuntimeException("The following module properties need to be defined: " + missingProperties);
        }
        if (repoVersionMax.compareTo(repoVersionMin) < 0)
        {
            throw new AlfrescoRuntimeException("The max repo version must be greater than the min repo version:\n" +
                    "   ID:               " + id + "\n" +
                    "   Min repo version: " + repoVersionMin + "\n" +
                    "   Max repo version: " + repoVersionMax);
        }
        if (id.matches(INVALID_ID_REGEX))
        {
            throw new AlfrescoRuntimeException(
                    "The module ID '" + id + "' is invalid.  It may consist of valid characters, numbers, '.', '_' and '-'");
        }
    }
    
    /**
     * @param id                module id
     * @param versionNumber     version number
     * @param title             title   
     * @param description       description
     */
    public ModuleDetailsImpl(String id, VersionNumber versionNumber, String title, String description)
    {
        // Set defaults
        this();
        
        this.id = id;
        this.version = versionNumber;
        this.title = title;
        this.description = description;
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        // Mandatory properties
        properties.setProperty(PROP_ID, id);
        properties.setProperty(PROP_VERSION, version.toString());
        properties.setProperty(PROP_TITLE, title);
        properties.setProperty(PROP_DESCRIPTION, description);
        // Optional properites
        if (repoVersionMin != null)
        {
            properties.setProperty(PROP_REPO_VERSION_MIN, repoVersionMin.toString());
        }
        if (repoVersionMax != null)
        {
            properties.setProperty(PROP_REPO_VERSION_MAX, repoVersionMax.toString());
        }
        if (installDate != null)
        {
            String installDateStr = ISO8601DateFormat.format(installDate);
            properties.setProperty(PROP_INSTALL_DATE, installDateStr);
        }
        if (installState != null)
        {
            String installStateStr = installState.toString();
            properties.setProperty(PROP_INSTALL_STATE, installStateStr);
        }
        if (aliases.size() > 0)
        {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (String oldId : aliases)
            {
                if (!first)
                {
                    sb.append(", ");
                }
                sb.append(oldId);
                first = false;
            }
            properties.setProperty(PROP_ALIASES, sb.toString());
        }
        // Done
        return properties;
    }
    
    @Override
    public String toString()
    {
        return "ModuleDetails[" + getProperties() + "]";
    }

    public String getId()
    {
        return id;
    }
    
    public List<String> getAliases()
    {
        return aliases;
    }

    public VersionNumber getVersion()
    {
        return version;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public String getDescription()
    {
        return description;
    }

    public VersionNumber getRepoVersionMin()
    {
        return repoVersionMin;
    }

    public void setRepoVersionMin(VersionNumber repoVersionMin)
    {
        this.repoVersionMin = repoVersionMin;
    }

    public VersionNumber getRepoVersionMax()
    {
        return repoVersionMax;
    }

    public void setRepoVersionMax(VersionNumber repoVersionMax)
    {
        this.repoVersionMax = repoVersionMax;
    }

    public Date getInstallDate()
    {
        return installDate;
    }
    
    public void setInstallDate(Date installDate)
    {
        this.installDate = installDate;
    }
    
    public ModuleInstallState getInstallState()
    {
        return installState;
    }
    
    public void setInstallState(ModuleInstallState installState)
    {
        this.installState = installState;
    }
}
