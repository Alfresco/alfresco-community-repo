/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.web.scripts;

import org.alfresco.service.descriptor.Descriptor;
import org.springframework.extensions.webscripts.ServerModel;


/**
 * Script / Template Model representing Repository Server meta-data
 * 
 * @author davidc
 */
public class RepositoryServerModel implements ServerModel
{
    private Descriptor currentDescriptor;
    private Descriptor serverDescriptor;
    
    /**
     * Construct
     * 
     * @param serverDescriptor
     */
    /*package*/ RepositoryServerModel(Descriptor currentDescriptor, Descriptor serverDescriptor)
    {
        this.currentDescriptor = currentDescriptor;
        this.serverDescriptor = serverDescriptor;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getContainerName()
     */
    public String getContainerName()
    {
        return "Repository";
    }

    /*(non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getId()
     */
    public String getId()
    {
        return currentDescriptor.getId();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getName()
     */
    public String getName()
    {
        return currentDescriptor.getName();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getVersionMajor()
     */
    public String getVersionMajor()
    {
        return currentDescriptor.getVersionMajor();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getVersionMinor()
     */
    public String getVersionMinor()
    {
        return currentDescriptor.getVersionMinor();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getVersionRevision()
     */
    public String getVersionRevision()
    {
        return currentDescriptor.getVersionRevision();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getVersionLabel()
     */
    public String getVersionLabel()
    {
        return currentDescriptor.getVersionLabel();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getVersionBuild()
     */
    public String getVersionBuild()
    {
        return currentDescriptor.getVersionBuild();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getVersion()
     */
    public String getVersion()
    {
        return currentDescriptor.getVersion();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getEdition()
     */
    public String getEdition()
    {
        return serverDescriptor.getEdition();
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.ServerModel#getSchema()
     */
    public int getSchema()
    {
        return currentDescriptor.getSchema();
    }
    
}
