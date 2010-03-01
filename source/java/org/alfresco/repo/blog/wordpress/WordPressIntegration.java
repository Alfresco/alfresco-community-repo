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
     * @see org.alfresco.module.blogIntegration.DefaultBlogIntegrationImplementation#getEndpointURL(org.alfresco.module.blogIntegration.BlogDetails)
     */
    @Override
    protected String getEndpointURL(BlogDetails blogDetails)
    {
        String endpoint = checkForProtocol(blogDetails.getUrl());
        return checkForTrainlingSlash(endpoint) + ENDPOINT;
    }

}
