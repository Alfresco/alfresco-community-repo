package org.alfresco.repo.publishing.linkedin.springsocial.api;

public interface Share
{
    public String getComment();

    public void setComment(String comment);

    public ShareVisibility getVisibility();

    public void setVisibility(ShareVisibility visibility);

}
