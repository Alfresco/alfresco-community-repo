/*
 * Copyright 2010 the original author or authors.
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
import java.util.List;

import org.alfresco.repo.publishing.flickr.springsocial.api.Flickr;
import org.alfresco.repo.publishing.flickr.springsocial.api.MediaOperations;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.social.NotAuthorizedException;
import org.springframework.social.oauth1.AbstractOAuth1ApiBinding;
import org.springframework.social.support.ClientHttpRequestFactorySelector;
import org.springframework.social.support.URIBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * <p>This is the central class for interacting with Facebook.</p>
 * <p>
 * There are some operations, such as searching, that do not require OAuth
 * authentication. In those cases, you may use a {@link FlickrTemplate} that is
 * created through the default constructor and without any OAuth details.
 * Attempts to perform secured operations through such an instance, however,
 * will result in {@link NotAuthorizedException} being thrown.
 * </p>
 * @author Craig Walls
 */
public class FlickrTemplate extends AbstractOAuth1ApiBinding implements Flickr {
    private final String REST_ENDPOINT = "http://api.flickr.com/services/rest/";
    private String consumerKey;
    
	private MediaOperations mediaOperations;

	/**
	 * Create a new instance of FacebookTemplate.
	 * This constructor creates a new FacebookTemplate able to perform unauthenticated operations against Facebook's Graph API.
	 * Some operations do not require OAuth authentication. 
	 * For example, retrieving a specified user's profile or feed does not require authentication (although the data returned will be limited to what is publicly available). 
	 * A FacebookTemplate created with this constructor will support those operations.
	 * Those operations requiring authentication will throw {@link NotAuthorizedException}.
	 */
	public FlickrTemplate() {
		initialize();		
	}

	/**
	 * Create a new instance of FacebookTemplate.
	 * This constructor creates the FacebookTemplate using a given access token.
	 * @param accessToken An access token given by Facebook after a successful OAuth 2 authentication (or through Facebook's JS library).
	 */
	public FlickrTemplate(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
		super(consumerKey, consumerSecret, accessToken, accessTokenSecret);
		this.consumerKey = consumerKey;
		initialize();
	}

	private void initSubApis() {
		mediaOperations = new MediaTemplate(consumerKey, getRestTemplate(), isAuthorized());
	}
	
	@Override
	public void setRequestFactory(ClientHttpRequestFactory requestFactory) {
		// Wrap the request factory with a BufferingClientHttpRequestFactory so that the error handler can do repeat reads on the response.getBody()
		super.setRequestFactory(ClientHttpRequestFactorySelector.bufferRequests(requestFactory));
	}

	public MediaOperations mediaOperations() {
		return mediaOperations;
	}
	
	@Override
	protected List<HttpMessageConverter<?>> getMessageConverters() {
		List<HttpMessageConverter<?>> messageConverters = super.getMessageConverters();
		messageConverters.add(new ByteArrayHttpMessageConverter());
		return messageConverters;
	}
	
	// private helpers
	private void initialize() {
		getRestTemplate().setErrorHandler(new FlickrErrorHandler());
		// Wrap the request factory with a BufferingClientHttpRequestFactory so that the error handler can do repeat reads on the response.getBody()
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
