package org.alfresco.repo.publishing.linkedin.springsocial.connect;

import org.alfresco.repo.publishing.linkedin.springsocial.api.AlfrescoLinkedIn;
import org.springframework.social.connect.support.OAuth1ConnectionFactory;

/**
 * 
 * @author Brian
 * @since 4.0
 */
public class LinkedInConnectionFactory extends OAuth1ConnectionFactory<AlfrescoLinkedIn>{

    public LinkedInConnectionFactory(String consumerKey, String consumerSecret) {
        super("linkedin", new LinkedInServiceProvider(consumerKey, consumerSecret), new LinkedInAdapter());
    }

}
