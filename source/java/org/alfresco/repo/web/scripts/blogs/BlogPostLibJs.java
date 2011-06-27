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
package org.alfresco.repo.web.scripts.blogs;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.BlogIntegrationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This class is a port of a previous JavaScript library.
 * 
 * @author Neil Mc Erlean (based on previous JavaScript)
 * @since 4.0
 */
public class BlogPostLibJs
{
    //FIXME It will be refactored when the other services are ported from JavaScript to Java.
    
    /**
     * Checks whether a blog configuration is available
     * This should at some point also check whether the configuration is enabled.
     * 
     * @param node the node that should be checked. Will check all parents if
     *        the node itself doesn't contain a configuration.
     * @return {boolean} whether a configuration could be found.
     */
    public static boolean hasExternalBlogConfiguration(NodeRef node, ServiceRegistry services)
    {
        if (node == null)
        {
            return false;
        }
        else if (services.getNodeService().hasAspect(node, BlogIntegrationModel.ASPECT_BLOG_DETAILS))
        {
            return true;
        }
        else
        {
            return hasExternalBlogConfiguration(services.getNodeService().getPrimaryParent(node).getParentRef(), services);
        }
    }
    
    public static Map<String, Object> getBlogPostData(NodeRef node, ServiceRegistry services)
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("node", node);
        String creator = (String)services.getNodeService().getProperty(node, ContentModel.PROP_CREATOR);
        data.put("author", services.getPersonService().getPerson(creator));
        
        data.put("commentCount", CommentsLibJs.getCommentsCount(node, services));
       
        // is the post published
        Serializable published = services.getNodeService().getProperty(node, ContentModel.PROP_PUBLISHED);
        boolean isPublished = published != null;
        if (isPublished)
        {
            data.put("releasedDate", published);
        }
       
        // draft
        data.put("isDraft", !isPublished);
       
        // set the isUpdated flag
        Date updatedDate = (Date) services.getNodeService().getProperty(node, ContentModel.PROP_UPDATED);
        boolean isUpdated = updatedDate != null;
        data.put("isUpdated", isUpdated);
        if (isUpdated)
        {
           data.put("updatedDate", updatedDate);
        }
       
        // fetch standard created/modified dates
        data.put("createdDate", services.getNodeService().getProperty(node, ContentModel.PROP_CREATED));
        data.put("modifiedDate", services.getNodeService().getProperty(node, ContentModel.PROP_MODIFIED));
       
        // does the external post require an update?
        Date lastUpdate = (Date) services.getNodeService().getProperty(node, BlogIntegrationModel.PROP_LAST_UPDATE);
        if (isPublished && lastUpdate != null)
        {
            // we either use the release or updated date
            Date modifiedDate = (Date) data.get("releasedDate");
            
            if (isUpdated)
            {
                modifiedDate = (Date) data.get("updatedDate");
            }
            data.put("outOfDate", modifiedDate.getTime() - lastUpdate.getTime() > 5000L);
        }
        else
        {
            data.put("outOfDate", false);
        }
        
        data.put("tags", services.getTaggingService().getTags(node));
       
       return data;
    }
}
