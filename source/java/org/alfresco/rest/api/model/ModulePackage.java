package org.alfresco.rest.api.model;

import org.alfresco.repo.module.ModuleVersionNumber;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleInstallState;
import org.alfresco.util.VersionNumber;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.util.Date;

/**
 * POJO representation of Module Details for Serialization as JSON.
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
    {
    }

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
            //Something went wrong with the definition of the Module.
            //These are just POJO properties, I am unable to represent
            //the data so will return null
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


