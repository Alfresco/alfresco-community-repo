package org.alfresco.repo.publishing.flickr.springsocial.api;

import org.springframework.social.support.URIBuilder;
import org.springframework.util.MultiValueMap;

/**
 * 
 * @author Brian
 * @since 4.0
 */
public interface FlickrHelper
{
    String getRestEndpoint();

    String getUploadEndpoint();
    
    void addStandardParams(URIBuilder uriBuilder);
    
    void addStandardParams(MultiValueMap<String, String> params);
}
