package org.alfresco.repo.publishing.flickr.springsocial.api.impl;

import org.alfresco.repo.publishing.flickr.springsocial.api.FlickrException;
import org.alfresco.repo.publishing.flickr.springsocial.api.FlickrHelper;
import org.alfresco.repo.publishing.flickr.springsocial.api.MediaOperations;
import org.alfresco.repo.publishing.flickr.springsocial.api.PhotoInfo;
import org.alfresco.repo.publishing.flickr.springsocial.api.impl.xml.FlickrError;
import org.alfresco.repo.publishing.flickr.springsocial.api.impl.xml.FlickrPayload;
import org.alfresco.repo.publishing.flickr.springsocial.api.impl.xml.FlickrResponse;
import org.alfresco.repo.publishing.flickr.springsocial.api.impl.xml.Photo;
import org.alfresco.repo.publishing.flickr.springsocial.api.impl.xml.PhotoId;
import org.springframework.core.io.Resource;
import org.springframework.social.support.URIBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * @author Brian
 * @since 4.0
 */
class MediaTemplate extends AbstractFlickrOperations implements MediaOperations
{
    private final RestTemplate restTemplate;
    private FlickrHelper helper;

    public MediaTemplate(FlickrHelper helper, RestTemplate restTemplate, boolean isAuthorizedForUser)
    {
        super(isAuthorizedForUser);
        this.restTemplate = restTemplate;
        this.helper = helper;
    }

    public String postPhoto(Resource photo, String title, String description, String... tags)
    {
        String id = null;
        requireAuthorization();
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
        URIBuilder uriBuilder = URIBuilder.fromUri(helper.getUploadEndpoint());

        parts.set("photo", photo);
        if (description != null)
        {
            uriBuilder.queryParam("description", description);
            parts.set("description", description);
        }
        if (title != null)
        {
            uriBuilder.queryParam("title", title);
            parts.set("title", title);
        }
        if (tags.length > 0)
        {
            StringBuilder tagBuilder = new StringBuilder();
            for (String tag : tags)
            {
                tagBuilder.append(tag).append(' ');
            }
            String tagsString = tagBuilder.toString();
            uriBuilder.queryParam("tags", tagsString);
            parts.set("tags", tagsString);
        }
        helper.addStandardParams(uriBuilder);
        FlickrResponse response = restTemplate.postForObject(uriBuilder.build(), parts, FlickrResponse.class);
        FlickrPayload payload = response.payload;
        checkError(payload);
        if (PhotoId.class.isAssignableFrom(payload.getClass()))
        {
            id = ((PhotoId)payload).id;
        }
        return id;
    }

    public PhotoInfo getPhoto(String id)
    {
        Photo result = null;
        requireAuthorization();
        URIBuilder uriBuilder = URIBuilder.fromUri(helper.getRestEndpoint());
        helper.addStandardParams(uriBuilder);
        uriBuilder.queryParam("method", "flickr.photos.getInfo");
        uriBuilder.queryParam("photo_id", id);
        FlickrResponse response = restTemplate.getForObject(uriBuilder.build(), FlickrResponse.class);
        FlickrPayload payload = response.payload;
        checkError(payload);
        if (Photo.class.isAssignableFrom(payload.getClass()))
        {
            result = (Photo)payload;
        }
        return result;
    }

    public void deletePhoto(String id)
    {
        requireAuthorization();
        MultiValueMap<String, String> parts = new LinkedMultiValueMap<String, String>();
        helper.addStandardParams(parts);
        parts.add("method", "flickr.photos.delete");
        parts.add("photo_id", id);
        FlickrResponse response = restTemplate.postForObject(helper.getRestEndpoint(), parts, FlickrResponse.class);
        FlickrPayload payload = response.payload;
        checkError(payload);
    }
    
    private void checkError(FlickrPayload payload) throws FlickrException
    {
        if (payload != null && FlickrError.class.isAssignableFrom(payload.getClass()))
        {
            FlickrError error = (FlickrError) payload;
            throw new FlickrException(error.code, error.msg);
        }
    }
}
