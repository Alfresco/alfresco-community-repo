package org.alfresco.repo.publishing.linkedin.springsocial.api;

import org.springframework.social.linkedin.api.LinkedIn;

/**
 * 
 * @author Brian
 * @since 4.0
 */
public interface AlfrescoLinkedIn extends LinkedIn
{
    void postNetworkUpdate(String update);
    
    void shareComment(String comment);
}
