package org.alfresco.repo.publishing.flickr.springsocial.api.impl;

import org.springframework.social.MissingAuthorizationException;

/**
 * 
 * @author Brian
 * @since 4.0
 */
class AbstractFlickrOperations
{

    private final boolean isAuthorized;

    public AbstractFlickrOperations(boolean isAuthorized)
    {
        this.isAuthorized = isAuthorized;
    }

    protected void requireAuthorization()
    {
        if (!isAuthorized)
        {
            throw new MissingAuthorizationException();
        }
    }

}
