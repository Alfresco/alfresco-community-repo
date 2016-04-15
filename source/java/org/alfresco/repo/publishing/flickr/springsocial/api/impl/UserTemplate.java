package org.alfresco.repo.publishing.flickr.springsocial.api.impl;

import org.alfresco.repo.publishing.flickr.springsocial.api.UserOperations;

/**
 * 
 * @author Brian
 * @since 4.0
 */
class UserTemplate extends AbstractFlickrOperations implements UserOperations
{

    public UserTemplate(boolean isAuthorized)
    {
        super(isAuthorized);
    }

}
