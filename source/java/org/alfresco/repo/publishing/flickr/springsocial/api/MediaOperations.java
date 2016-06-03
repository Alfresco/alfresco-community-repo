package org.alfresco.repo.publishing.flickr.springsocial.api;

import org.springframework.core.io.Resource;

/**
 * 
 * @author Brian
 * @since 4.0
 */
public interface MediaOperations
{

    String postPhoto(Resource photo, String title, String description, String... tags) throws FlickrException;

    PhotoInfo getPhoto(String id) throws FlickrException;
    
    void deletePhoto(String id) throws FlickrException;
}
