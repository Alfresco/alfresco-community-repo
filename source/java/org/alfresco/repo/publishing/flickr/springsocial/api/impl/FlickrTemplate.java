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

import java.net.URI;
import java.util.List;

import org.alfresco.repo.publishing.flickr.springsocial.api.Flickr;
import org.alfresco.repo.publishing.flickr.springsocial.api.MediaOperations;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.social.oauth1.AbstractOAuth1ApiBinding;
import org.springframework.social.support.ClientHttpRequestFactorySelector;
import org.springframework.social.support.URIBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class FlickrTemplate extends AbstractOAuth1ApiBinding implements Flickr
{
    private final String REST_ENDPOINT = "http://api.flickr.com/services/rest/";
    private String consumerKey;

    private MediaOperations mediaOperations;

    public FlickrTemplate()
    {
        initialize();
    }

    public FlickrTemplate(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret)
    {
        super(consumerKey, consumerSecret, accessToken, accessTokenSecret);
        this.consumerKey = consumerKey;
        initialize();
    }

    private void initSubApis()
    {
        mediaOperations = new MediaTemplate(consumerKey, getRestTemplate(), isAuthorized());
    }

    @Override
    public void setRequestFactory(ClientHttpRequestFactory requestFactory)
    {
        // Wrap the request factory with a BufferingClientHttpRequestFactory so
        // that the error handler can do repeat reads on the response.getBody()
        super.setRequestFactory(ClientHttpRequestFactorySelector.bufferRequests(requestFactory));
    }

    public MediaOperations mediaOperations()
    {
        return mediaOperations;
    }

    @Override
    protected List<HttpMessageConverter<?>> getMessageConverters()
    {
        List<HttpMessageConverter<?>> messageConverters = super.getMessageConverters();
        messageConverters.add(new ByteArrayHttpMessageConverter());
        return messageConverters;
    }

    // private helpers
    private void initialize()
    {
        getRestTemplate().setErrorHandler(new FlickrErrorHandler());
        // Wrap the request factory with a BufferingClientHttpRequestFactory so
        // that the error handler can do repeat reads on the response.getBody()
        super.setRequestFactory(ClientHttpRequestFactorySelector.bufferRequests(getRestTemplate().getRequestFactory()));
        initSubApis();
    }

    @Override
    public boolean test()
    {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("api_key", consumerKey);
        params.add("format", "json");
        params.add("method", "flickr.test.login");
        params.add("nojsoncallback", "1");
        URI uri = URIBuilder.fromUri(REST_ENDPOINT).queryParams(params).build();
        getRestTemplate().getForObject(uri, String.class);
        return true;
    }
}
