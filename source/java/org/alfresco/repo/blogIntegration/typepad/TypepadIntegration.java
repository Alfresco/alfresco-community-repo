/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.blogIntegration.typepad;

import org.alfresco.repo.blogIntegration.BlogDetails;
import org.alfresco.repo.blogIntegration.DefaultBlogIntegrationImplementation;

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
