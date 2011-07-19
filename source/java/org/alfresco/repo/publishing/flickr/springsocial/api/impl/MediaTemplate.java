/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.publishing.flickr.springsocial.api.impl;

import org.alfresco.repo.publishing.flickr.springsocial.api.MediaOperations;
import org.alfresco.repo.publishing.flickr.springsocial.api.PhotoMetadata;
import org.springframework.core.io.Resource;
import org.springframework.social.support.URIBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

class MediaTemplate extends AbstractFlickrOperations implements MediaOperations
{
    private final RestTemplate restTemplate;

    public MediaTemplate(String consumerKey, RestTemplate restTemplate, boolean isAuthorizedForUser)
    {
        super(isAuthorizedForUser);
        this.restTemplate = restTemplate;
    }

    public String postPhoto(Resource photo, PhotoMetadata metadata)
    {
        requireAuthorization();
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
        if (metadata.getDescription() != null)
            parts.set("description", metadata.getDescription());
        parts.set("photo", photo);
        if (metadata.getTitle() != null)
            parts.set("title", metadata.getTitle());
        URIBuilder uriBuilder = URIBuilder.fromUri("http://api.flickr.com/services/upload/");
        if (metadata.getDescription() != null)
        {
            uriBuilder.queryParam("description", metadata.getDescription());
        }
        if (metadata.getTitle() != null)
        {
            uriBuilder.queryParam("title", metadata.getTitle());
        }
        String response = restTemplate.postForObject(uriBuilder.build(), parts, String.class);
        return (String) response;
    }

    @Override
    public PhotoMetadata createPhotoMetadata()
    {
        return new PhotoMetadataImpl();
    }
}
