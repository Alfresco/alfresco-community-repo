/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.alfresco.repo.publishing.flickr.springsocial.api.impl;

import java.net.URI;

import org.alfresco.repo.publishing.flickr.springsocial.api.MediaOperations;
import org.alfresco.repo.publishing.flickr.springsocial.api.PhotoMetadata;
import org.alfresco.repo.publishing.flickr.springsocial.api.impl.AbstractFlickrOperations;
import org.springframework.core.io.Resource;
import org.springframework.social.support.URIBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

class MediaTemplate extends AbstractFlickrOperations implements MediaOperations
{
    private final RestTemplate restTemplate;
    private String consumerKey;

    public MediaTemplate(String consumerKey, RestTemplate restTemplate, boolean isAuthorizedForUser)
    {
        super(isAuthorizedForUser);
        this.restTemplate = restTemplate;
        this.consumerKey = consumerKey;
    }

    public String postPhoto(Resource photo, PhotoMetadata metadata)
    {
        requireAuthorization();
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
        parts.set("api_key", consumerKey);
        if (metadata.getDescription() != null)
            parts.set("description", metadata.getDescription());
        parts.set("photo", photo);
        if (metadata.getTitle() != null)
            parts.set("title", metadata.getTitle());
        URI uri = URIBuilder.fromUri("http://api.flickr.com/services/upload/").build();
        String response = restTemplate.postForObject(uri, parts, String.class);
        return (String) response;
    }

    @Override
    public PhotoMetadata createPhotoMetadata()
    {
        return new PhotoMetadataImpl();
    }
}
