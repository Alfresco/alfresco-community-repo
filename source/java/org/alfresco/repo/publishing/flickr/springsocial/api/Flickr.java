package org.alfresco.repo.publishing.flickr.springsocial.api;

import org.springframework.social.ApiBinding;

/**
 * 
 * @author Brian
 * @since 4.0
 */
public interface Flickr extends ApiBinding
{
    boolean test();
    
    /**
     * API for performing operations on albums, photos, and videos.
     */
    MediaOperations mediaOperations();
}
