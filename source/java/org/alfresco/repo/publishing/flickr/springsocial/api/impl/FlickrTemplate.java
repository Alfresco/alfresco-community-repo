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

import javax.xml.transform.Source;

import org.alfresco.repo.publishing.JaxbHttpMessageConverter;
import org.alfresco.repo.publishing.flickr.springsocial.api.Flickr;
import org.alfresco.repo.publishing.flickr.springsocial.api.FlickrHelper;
import org.alfresco.repo.publishing.flickr.springsocial.api.MediaOperations;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.social.oauth1.AbstractOAuth1ApiBinding;
import org.springframework.social.support.ClientHttpRequestFactorySelector;
import org.springframework.social.support.URIBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class FlickrTemplate extends AbstractOAuth1ApiBinding implements Flickr, FlickrHelper
{
    private static final String DEFAULT_ENDPOINT = "http://api.flickr.com/services/";
//    private static final String DEFAULT_ENDPOINT = "https://secure.flickr.com/services/";
    
    private static String endpoint = DEFAULT_ENDPOINT;
    
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
        mediaOperations = new MediaTemplate(this, getRestTemplate(), isAuthorized());
    }

    @Override
    public void setRequestFactory(ClientHttpRequestFactory requestFactory)
    {
        // Wrap the request factory with a BufferingClientHttpRequestFactory so
        // that the error handler can do repeat reads on the response.getBody()
        super.setRequestFactory(ClientHttpRequestFactorySelector.bufferRequests(requestFactory));
    }

    public static void setEndpoint(String endpoint)
    {
        FlickrTemplate.endpoint = endpoint;
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
        messageConverters.add(new SourceHttpMessageConverter<Source>());
        messageConverters.add(new JaxbHttpMessageConverter("org.alfresco.repo.publishing.flickr.springsocial.api.impl.xml"));
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
        params.add("method", "flickr.test.login");
        addStandardParams(params);
        URI uri = URIBuilder.fromUri(getRestEndpoint()).queryParams(params).build();
        getRestTemplate().getForObject(uri, String.class);
        return true;
    }

    @Override
    public void addStandardParams(URIBuilder uriBuilder)
    {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        addStandardParams(params);
        uriBuilder.queryParams(params);
    }

    @Override
    public void addStandardParams(MultiValueMap<String, String> params)
    {
        params.set("api_key", consumerKey);
    }

    @Override
    public String getRestEndpoint()
    {
        return endpoint + "rest/";
    }

    @Override
    public String getUploadEndpoint()
    {
        return endpoint + "upload/";
    }
}
