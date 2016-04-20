
package org.alfresco.repo.virtual;

import org.alfresco.repo.virtual.config.NodeRefExpression;

public class VirtualizationConfigTestBootstrap
{
    private NodeRefExpression typeTemplatesPath;
    
    private NodeRefExpression systemTemplatesPath;
    
    private String systemTemplateType;

    private boolean virtualFoldersEnabled = true;

    private NodeRefExpression downloadAssocaiationsFolder;


    private String typeTemplatesQNameFilter;
    
    public void setTypeTemplatesQNameFilter(String typeTemplatesQNameFilterRegexp)
    {
        this.typeTemplatesQNameFilter = typeTemplatesQNameFilterRegexp;
    }
    
    public String getTypeTemplatesQNameFilter()
    {
        return this.typeTemplatesQNameFilter;
    }
    
    public void setTypeTemplatesPath(NodeRefExpression typeTemplatesPath)
    {
        this.typeTemplatesPath = typeTemplatesPath;
    }
    
    public NodeRefExpression getTypeTemplatesPath()
    {
        return this.typeTemplatesPath;
    }
    
    public NodeRefExpression getDownloadAssocaiationsFolder()
    {
        return this.downloadAssocaiationsFolder;
    }
    
    public void setDownloadAssocaiationsFolder(NodeRefExpression downloadAssocaiationsFolder)
    {
        this.downloadAssocaiationsFolder = downloadAssocaiationsFolder;
    }
    
    public void setVirtualFoldersEnabled(boolean virtualFoldersEnabled)
    {
        this.virtualFoldersEnabled = virtualFoldersEnabled;
    }

    public boolean areVirtualFoldersEnabled()
    {
        return this.virtualFoldersEnabled;
    }

    public void setSystemTemplatesPath(NodeRefExpression systemTemplatesPath)
    {
        this.systemTemplatesPath = systemTemplatesPath;
    }

    public NodeRefExpression getSystemTemplatesPath()
    {
        return this.systemTemplatesPath;
    }

    public void setSystemTemplateType(String systemTemplateType)
    {
        this.systemTemplateType = systemTemplateType;
    }

    public String getSystemTemplateType()
    {
        return this.systemTemplateType;
    }
}
