/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual;

import org.alfresco.repo.virtual.config.NodeRefExpression;

public class VirtualizationConfigTestBootstrap
{
    private NodeRefExpression typeTemplatesPath;
    
    private NodeRefExpression systemTemplatesPath;
    
    private String systemTemplateType;

    private boolean virtualFoldersEnabled = true;

    private NodeRefExpression downloadAssocaiationsFolder;


    private String typeTemplatesQNameFilterRegexp;
    
    public void setTypeTemplatesQNameFilterRegexp(String typeTemplatesQNameFilterRegexp)
    {
        this.typeTemplatesQNameFilterRegexp = typeTemplatesQNameFilterRegexp;
    }
    
    public String getTypeTemplatesQNameFilterRegexp()
    {
        return this.typeTemplatesQNameFilterRegexp;
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
