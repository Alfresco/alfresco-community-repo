package org.alfresco.repo.blog.wordpress;

import org.alfresco.repo.blog.BlogDetails;
import org.alfresco.repo.blog.DefaultBlogIntegrationImplementation;

/**
 * @author Roy Wetherall
 */
public class WordPressIntegration extends DefaultBlogIntegrationImplementation
{
    private static String ENDPOINT = "xmlrpc.php";
    
    /**
     * @see DefaultBlogIntegrationImplementation#getEndpointURL(BlogDetails)
     */
    @Override
    protected String getEndpointURL(BlogDetails blogDetails)
    {
        String endpoint = checkForProtocol(blogDetails.getUrl());
        return checkForTrainlingSlash(endpoint) + ENDPOINT;
    }

}
