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
