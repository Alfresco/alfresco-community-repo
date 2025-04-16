/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.rest.api.model;

import java.util.Date;

import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleInstallState;

/**
 * POJO representation of Module Details for Serialization as JSON.
 * 
 * @author Gethin James.
 */
public class ModulePackage
{
    private String id;
    private String title;
    private String description;
    private String version;
    private Date installDate;
    private ModuleInstallState installState;
    private String versionMin;
    private String versionMax;

    public ModulePackage()
    {}

    private ModulePackage(ModuleDetails moduleDetails)
    {
        this.id = moduleDetails.getId();
        this.title = moduleDetails.getTitle();
        this.description = moduleDetails.getDescription();
        this.version = moduleDetails.getModuleVersionNumber().toString();
        this.installDate = moduleDetails.getInstallDate();
        this.installState = moduleDetails.getInstallState();
        this.versionMin = moduleDetails.getRepoVersionMin().toString();
        this.versionMax = moduleDetails.getRepoVersionMax().toString();
    }

    public static ModulePackage fromModuleDetails(ModuleDetails moduleDetails)
    {
        try
        {
            return new ModulePackage(moduleDetails);
        }
        catch (NullPointerException npe)
        {
            // Something went wrong with the definition of the Module.
            // These are just POJO properties, I am unable to represent
            // the data so will return null
            return null;
        }
    }

    public String getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription()
    {
        return description;
    }

    public String getVersion()
    {
        return version;
    }

    public Date getInstallDate()
    {
        return installDate;
    }

    public ModuleInstallState getInstallState()
    {
        return installState;
    }

    public String getVersionMin()
    {
        return versionMin;
    }

    public String getVersionMax()
    {
        return versionMax;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("ModulePackage{");
        sb.append("id='").append(id).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", version=").append(version);
        sb.append(", installDate=").append(installDate);
        sb.append(", installState=").append(installState);
        sb.append(", versionMin='").append(versionMin).append('\'');
        sb.append(", versionMax='").append(versionMax).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
