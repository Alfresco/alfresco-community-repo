package org.alfresco.repo.publishing.flickr.springsocial.connect;

import org.alfresco.repo.publishing.flickr.springsocial.api.Flickr;
import org.alfresco.repo.publishing.flickr.springsocial.connect.FlickrAdapter;
import org.springframework.social.connect.support.OAuth1ConnectionFactory;

/**
 * 
 * @author Brian
 * @since 4.0
 */
public class FlickrConnectionFactory extends OAuth1ConnectionFactory<Flickr>
{

    public FlickrConnectionFactory(String consumerKey, String consumerSecret)
    {
        super("flickr", new FlickrServiceProvider(consumerKey, consumerSecret), new FlickrAdapter());
    }

}
