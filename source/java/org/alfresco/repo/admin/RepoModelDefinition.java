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
        sb.append("Description: " + (model == null ? "n/a" : model.getDescription(null)) + " , ");
        sb.append("Author: " + (model == null ? "n/a" : model.getAuthor()) + " , ");
        sb.append("Published: " + (model == null ? "n/a" : model.getPublishedDate()) + " , ");
        sb.append("Version: " + (model == null ? "n/a" : model.getVersion()));
       
        return sb.toString();
    }
}
