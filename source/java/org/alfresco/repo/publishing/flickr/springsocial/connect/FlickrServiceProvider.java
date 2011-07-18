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
package org.alfresco.repo.publishing.flickr.springsocial.connect;

import org.alfresco.repo.publishing.flickr.springsocial.api.Flickr;
import org.alfresco.repo.publishing.flickr.springsocial.api.impl.FlickrTemplate;
import org.springframework.social.oauth1.AbstractOAuth1ServiceProvider;
import org.springframework.social.oauth1.OAuth1Template;

public class FlickrServiceProvider extends AbstractOAuth1ServiceProvider<Flickr>
{

    public FlickrServiceProvider(String consumerKey, String consumerSecret)
    {
        super(consumerKey, consumerSecret, new OAuth1Template(consumerKey, consumerSecret,
                "http://www.flickr.com/services/oauth/request_token", "http://www.flickr.com/services/oauth/authorize",
                "http://www.flickr.com/services/oauth/access_token"));
    }

    public Flickr getApi(String accessToken, String secret)
    {
        return new FlickrTemplate(getConsumerKey(), getConsumerSecret(), accessToken, secret);
    }

}