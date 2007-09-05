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
package org.alfresco.repo.admin;

import org.alfresco.service.cmr.dictionary.ModelDefinition;


/**
 * Repository-stored Model Definition
 * 
 *
 */
public class RepoModelDefinition
{
    private String repoName;
    private String repoVersion;
    private ModelDefinition model;
    
    private boolean loaded;
    
    RepoModelDefinition(String repoName, String repoVersion, ModelDefinition model, boolean loaded)
    {
        this.repoName = repoName;
        this.repoVersion = repoVersion;
        this.model = model;
        this.loaded = loaded;
    }

    
    public String getRepoName()
    {
        return repoName;
    }
    
    public String getRepoVersion()
    {
        return repoVersion;
    }

    public ModelDefinition getModel()
    {
        return model;
    }
    
    // JanV - temp
    public boolean isLoaded()
    {
        return loaded;
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("IsLoaded: " + (loaded ? "Y" : "N") + " , ");
        sb.append("RepoVersion: " + repoVersion + " , ");
        sb.append("RepoName: " + repoName + " , ");
        sb.append("ModelQName: " + (model == null ? "n/a" : model.getName()) + " , ");
        sb.append("Description: " + (model == null ? "n/a" : model.getDescription()) + " , ");
        sb.append("Author: " + (model == null ? "n/a" : model.getAuthor()) + " , ");
        sb.append("Published: " + (model == null ? "n/a" : model.getPublishedDate()) + " , ");
        sb.append("Version: " + (model == null ? "n/a" : model.getVersion()));
       
        return sb.toString();
    }
}
