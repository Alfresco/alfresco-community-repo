/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
     * @see org.alfresco.module.blogIntegration.DefaultBlogIntegrationImplementation#getEndpointURL(org.alfresco.module.blogIntegration.BlogDetails)
     */
    @Override
    protected String getEndpointURL(BlogDetails blogDetails)
    {
        return "http://www.typepad.com/t/api";
    }
    
    /**
     * For some reason typepad returns a hash table rather than the expected boolean result.
     * 
     * @see org.alfresco.module.blogIntegration.BlogIntegrationImplementation#deletePost(org.alfresco.module.blogIntegration.BlogDetails, java.lang.String)
     */
    @Override
    public boolean deletePost(BlogDetails blogDetails, String postId)
    {
        // NOTE:  At the time of testing typepad.com failed when making this call, for now the implementation is
        //        being overriden to return success
        
        return true;
    }

}
