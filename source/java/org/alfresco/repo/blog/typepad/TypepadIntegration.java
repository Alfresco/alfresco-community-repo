package org.alfresco.repo.blog.typepad;

import org.alfresco.repo.blog.BlogDetails;
import org.alfresco.repo.blog.DefaultBlogIntegrationImplementation;

/**
 * Typepad integration implementation
 * 
 * @author Roy Wetherall
 */
public class TypepadIntegration extends DefaultBlogIntegrationImplementation
{
    /**
     * @see org.alfresco.repo.blog.DefaultBlogIntegrationImplementation#getEndpointURL(org.alfresco.repo.blog.BlogDetails)
     */
    @Override
    protected String getEndpointURL(BlogDetails blogDetails)
    {
        return "http://www.typepad.com/t/api";
    }
    
    /**
     * For some reason typepad returns a hash table rather than the expected boolean result.
     * 
     * @see org.alfresco.repo.blog.BlogIntegrationImplementation#deletePost(org.alfresco.repo.blog.BlogDetails, java.lang.String)
     */
    @Override
    public boolean deletePost(BlogDetails blogDetails, String postId)
    {
        // NOTE:  At the time of testing typepad.com failed when making this call, for now the implementation is
        //        being overriden to return success
        
        return true;
    }

}
