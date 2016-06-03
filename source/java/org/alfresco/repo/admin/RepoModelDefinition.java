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
