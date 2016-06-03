/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.publishing.linkedin.springsocial.connect;

import org.alfresco.repo.publishing.linkedin.springsocial.api.AlfrescoLinkedIn;
import org.alfresco.repo.publishing.linkedin.springsocial.api.impl.AlfrescoLinkedInTemplate;
import org.springframework.social.oauth1.AbstractOAuth1ServiceProvider;
import org.springframework.social.oauth1.OAuth1Template;

/**
 * 
 * @author Brian
 * @since 4.0
 */
public class LinkedInServiceProvider  extends AbstractOAuth1ServiceProvider<AlfrescoLinkedIn> {

    public LinkedInServiceProvider(String consumerKey, String consumerSecret) {
        super(consumerKey, consumerSecret, new OAuth1Template(consumerKey, consumerSecret,
            "https://api.linkedin.com/uas/oauth/requestToken",
            "https://www.linkedin.com/uas/oauth/authorize",
            "https://www.linkedin.com/uas/oauth/authenticate",          
            "https://api.linkedin.com/uas/oauth/accessToken"));
    }

    public AlfrescoLinkedIn getApi(String accessToken, String secret) {
        return new AlfrescoLinkedInTemplate(getConsumerKey(), getConsumerSecret(), accessToken, secret);
    }
    

}
