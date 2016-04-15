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
package org.alfresco.repo.blog;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.BlogIntegrationModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Blog details.  Contains the detail of a blog.
 * 
 * @author Roy Wetherall
 */
public class BlogDetails implements BlogIntegrationModel
{
    /** Node that has the blog details aspect applied */
    private NodeRef nodeRef;
    
    /** The blog implementation name (eg: wordpress, typepad, etc) */
    private String implementationName;
    
    /** The blog id */
    private String blogId;
    
    /** The blog URL */
    private String url;
    
    /** The user name */
    private String userName;
    
    /** The password */
    private String password;
    
    /** The display name of the blog */
    private String name;
    
    /** The description of the blog */
    private String description;
    
    /**
     * Create a BlogDetails object from a node that has the blogDetails aspect applied.
     * 
     * @param nodeService   the node service
     * @param nodeRef       the node reference
     * @return BlogDetails  the blog details 
     */
    public static BlogDetails createBlogDetails(NodeService nodeService, NodeRef nodeRef)
    {
        // Check for the blog details aspect
        if (nodeService.hasAspect(nodeRef, ASPECT_BLOG_DETAILS) == false)
        {
            throw new BlogIntegrationRuntimeException("Can not create blog details object since node does not have blogDetails aspect.");
        }
        
        // Get the blog details
        Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
        return new BlogDetails(
                (String)props.get(PROP_BLOG_IMPLEMENTATION),
                (String)props.get(PROP_ID),
                (String)props.get(PROP_URL),
                (String)props.get(PROP_USER_NAME),
                (String)props.get(PROP_PASSWORD),
                (String)props.get(PROP_NAME),
                (String)props.get(PROP_DESCRIPTION),
                nodeRef);        
    }
    
    /**
     * Constructor
     * 
     * @param implementationName    the implementation name
     * @param blogId                the blog id
     * @param url                   the blog URL
     * @param userName              the user name
     * @param password              the password
     * @param name                  the name
     * @param description           the description
     */
    public BlogDetails(String implementationName, String blogId, String url, String userName, String password, String name, String description)
    {
        this(implementationName, blogId, url, userName, password, name, description, null);
    }
    
    /**
     * Constructor 
     * 
     * @param implementationName    the implementation name
     * @param blogId                the blog id
     * @param url                   the blog URL
     * @param userName              the user name
     * @param password              the password
     * @param name                  the name
     * @param description           the description
     * @param nodeRef               the node reference
     */
    public BlogDetails(String implementationName, String blogId, String url, String userName, String password, String name, String description, NodeRef nodeRef)
    {
        this.implementationName = implementationName;
        this.blogId = blogId;
        this.url = url;
        this.userName = userName;
        this.password = password;
        this.name = name;
        this.description = description;
        this.nodeRef = nodeRef;
    }
    
    /**
     * Gets the node reference
     * 
     * @return NodeRef  the node reference
     */
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }
    
    /**
     * Get the implementation name
     * 
     * @return String   the implementation name
     */
    public String getImplementationName()
    {
        return this.implementationName;
    }
    
    /**
     * Get the blog id
     * 
     * @return String   the blog id
     */
    public String getBlogId()
    {
        return this.blogId;
    }
    
    /**
     * Get the blog URL
     * 
     * @return String   the blog URL
     */
    public String getUrl()
    {
        return this.url;
    }
    
    /**
     * Get the user name
     * 
     * @return String   the user name
     */
    public String getUserName()
    {
        return this.userName;
    }
    
    /**
     * Get the password
     * 
     * @return String   the password
     */
    public String getPassword()
    {
        return this.password;
    }
    
    /**
     * Get the name
     * 
     * @return String   the name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Get the description
     * 
     * @return String   the description
     */
    public String getDescription()
    {
        return description;
    }
}
