package org.alfresco.repo.publishing.flickr.springsocial.api.impl;

import org.alfresco.repo.publishing.flickr.springsocial.api.PhotoMetadata;

public class PhotoMetadataImpl implements PhotoMetadata
{
    private String title;
    private String description;

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
