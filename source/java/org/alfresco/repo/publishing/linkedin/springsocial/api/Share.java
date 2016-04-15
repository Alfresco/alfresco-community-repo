package org.alfresco.repo.publishing.linkedin.springsocial.api;

/**
 * 
 * @author Brian
 * @since 4.0
 */
public interface Share
{
    public String getComment();

    public void setComment(String comment);

    public ShareVisibility getVisibility();

    public void setVisibility(ShareVisibility visibility);

}
