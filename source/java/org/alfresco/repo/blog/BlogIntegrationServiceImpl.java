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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.BlogIntegrationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Blog integration service implementation
 * 
 * @author Roy Wetherall
 */
public class BlogIntegrationServiceImpl implements BlogIntegrationService, BlogIntegrationModel
{
    /** Node service */
    private NodeService nodeService;
    
    /** Content service */
    private ContentService contentService;
    
    /** Registered blog integration implemenatations */
    private Map<String, BlogIntegrationImplementation> implementations = new HashMap<String, BlogIntegrationImplementation>(5);
    
    /** Supported mimetypes */
    public static List<String> supportedMimetypes = new ArrayList<String>(5);
    
    /** Static initialisation of supported mimetypes */
    static
    {
        supportedMimetypes.add(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        supportedMimetypes.add(MimetypeMap.MIMETYPE_HTML);
    }
    
    /**
     * Set the node service
     * 
     * @param nodeService   the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the content service
     * 
     * @param contentService    the content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * @see org.alfresco.repo.blog.BlogIntegrationService#register(org.alfresco.repo.blog.BlogIntegrationImplementation)
     */
    public void register(BlogIntegrationImplementation implementation)
    {
        if (this.implementations.containsKey(implementation.getName()) == true)
        {
            throw new BlogIntegrationRuntimeException("A blog implementation with name '" + implementation.getName() + "' has already been registered.");
        }
        this.implementations.put(implementation.getName(), implementation);
    }
    
    /**
     * @see org.alfresco.repo.blog.BlogIntegrationService#getBlogIntegrationImplementation(java.lang.String)
     */
    public BlogIntegrationImplementation getBlogIntegrationImplementation(String implementationName)
    {
        return this.implementations.get(implementationName);
    }

    /**
     * @see org.alfresco.repo.blog.BlogIntegrationService#getBlogIntegrationImplementations()
     */
    public List<BlogIntegrationImplementation> getBlogIntegrationImplementations()
    {
        return new ArrayList<BlogIntegrationImplementation>(this.implementations.values());
    }

    /**
     * @see org.alfresco.repo.blog.BlogIntegrationService#getBlogDetails(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<BlogDetails> getBlogDetails(NodeRef nodeRef)
    {
        List<BlogDetails> result = new ArrayList<BlogDetails>(5);
        
        // First check the node itself
        if (this.nodeService.hasAspect(nodeRef, ASPECT_BLOG_DETAILS) == true)
        {
            result.add(BlogDetails.createBlogDetails(this.nodeService, nodeRef));
        }
        
        // Now walk up the parent hiearchy adding details as they are found
        getBlogDetailsImpl(nodeRef, result);        
        return result;
    }
    
    /**
     * Helper method that recurses up the primary parent hierarchy checking for 
     * blog details
     * 
     * @param nodeRef       the node reference
     * @param blogDetails   list of blog details
     */
    private void getBlogDetailsImpl(NodeRef nodeRef, List<BlogDetails> blogDetails)
    {
        // Check the parent assoc
        ChildAssociationRef parentAssoc = this.nodeService.getPrimaryParent(nodeRef);
        if (parentAssoc != null)
        {
            // Check for the blog details
            NodeRef parent = parentAssoc.getParentRef();
            if (parent != null)
            {
                if (this.nodeService.hasAspect(parent, ASPECT_BLOG_DETAILS) == true)
                {
                    blogDetails.add(BlogDetails.createBlogDetails(this.nodeService, parent));
                }
                
                // Recurse
                getBlogDetailsImpl(parent, blogDetails);
            }
        }        
    }
    
    /**
     * @see org.alfresco.repo.blog.BlogIntegrationService#newPost(BlogDetails, NodeRef, QName, boolean)
     */
    public void newPost(BlogDetails blogDetails, NodeRef nodeRef, QName contentProperty, boolean publish)
    {
        // Get the blog implementation
        BlogIntegrationImplementation implementation = getImplementation(blogDetails.getImplementationName());
        
        // Check that this node has not already been posted to a blog
        if (this.nodeService.hasAspect(nodeRef, ASPECT_BLOG_POST) == true)
        {
            throw new BlogIntegrationRuntimeException("Can not create new blog post since this conten has already been posted to a blog.");
        }
        
        // Get the posts body
        ContentReader contentReader = this.contentService.getReader(nodeRef, contentProperty);
        if (contentReader == null)
        {
            throw new BlogIntegrationRuntimeException("No content found for new blog entry.");
        }
        
        // Check the mimetype
        String body = null;
        if (supportedMimetypes.contains(contentReader.getMimetype()) == true)
        {
            // Get the content
            body = contentReader.getContentString();
        }
        else
        {
            throw new BlogIntegrationRuntimeException("The content mimetype '" + contentReader.getMimetype() + "' is not supported.");
        }
        
        // Get the posts title
        String title = (String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
        if (title == null || title.length() == 0)
        {
           if (body.length() > 23)
           {
              // Get the title from the first 22 character plus ' ...'
              title = body.substring(0, 23) + " ...";
           }
           else
           {
              title = body;
           }
        }
        
        // Post the new blog entry
        String postId = implementation.newPost(blogDetails, title, body, true);
        
        // Get the blog details node if the is one
        NodeRef blogDetailsNodeRef = blogDetails.getNodeRef();
        if (blogDetailsNodeRef != null)
        {
            // Now get the details of the newly created post
            Map<String, Object> details = implementation.getPost(blogDetails, postId);
            String link = (String)details.get("link");            
            
            // Add the details of the new post to the node
            Map<QName, Serializable> props = new HashMap<QName, Serializable>(5);
            props.put(PROP_POST_ID, postId);
            if (link != null)
            {
                props.put(PROP_LINK, link);
            }
            Date now = new Date();
            props.put(PROP_POSTED, now);
            props.put(PROP_LAST_UPDATE, now);
            props.put(PROP_PUBLISHED, Boolean.valueOf(publish));
            this.nodeService.addAspect(nodeRef, ASPECT_BLOG_POST, props);
            
            // Associate to the blog details
            this.nodeService.createAssociation(nodeRef, blogDetailsNodeRef, ASSOC_BLOG_DETAILS);
        }
    }
    
    /**
     * Gets the blog implementation based on its name
     * 
     * @param implementationName                the implementation name
     * @return BlogIntegrationImplementation    the blog integration
     */
    private BlogIntegrationImplementation getImplementation(String implementationName)
    {
        if (this.implementations.containsKey(implementationName) == false)
        {
            throw new BlogIntegrationRuntimeException("There is no blog implementation present for '" + implementationName + "'");
        }
        return this.implementations.get(implementationName);
    }
    
    /**
     * @see org.alfresco.repo.blog.BlogIntegrationService#updatePost(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, boolean)
     */
    public void updatePost(NodeRef nodeRef, QName contentProperty, boolean publish)
    {
        // Get the blog details and post id
        BlogDetails blogDetails = null;
        String postId = null;
        if (this.nodeService.hasAspect(nodeRef, ASPECT_BLOG_POST) == true)
        {
            List<AssociationRef> assocs = this.nodeService.getTargetAssocs(nodeRef, ASSOC_BLOG_DETAILS);
            if (assocs.size() == 0)
            {
                throw new BlogIntegrationRuntimeException("Can not resolve blog details for update because blogDetails association is not populated.");
            }
            else
            {
                blogDetails = BlogDetails.createBlogDetails(this.nodeService, assocs.get(0).getTargetRef());
                postId = (String)this.nodeService.getProperty(nodeRef, PROP_POST_ID);
            }
        }
        else
        {
            throw new BlogIntegrationRuntimeException("Can not update blog post as this node has not been previously posted to a blog.");
        }        
        
        // Get the blog implementation
        BlogIntegrationImplementation implementation = getImplementation(blogDetails.getImplementationName());        
        
        // Get the posts title
        String title = (String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
        if (title == null || title.length() == 0)
        {
            throw new BlogIntegrationRuntimeException("No title available for update blog post.  Set the title property and re-try.");
        }
        
        // Get the posts body
        ContentReader contentReader = this.contentService.getReader(nodeRef, contentProperty);
        if (contentReader == null)
        {
            throw new BlogIntegrationRuntimeException("No content found for update blog entry.");
        }
        
        // Check the mimetype
        String body = null;
        if (supportedMimetypes.contains(contentReader.getMimetype()) == true)
        {
            // Get the content
            body = contentReader.getContentString();
        }
        else
        {
            throw new BlogIntegrationRuntimeException("The content mimetype '" + contentReader.getMimetype() + "' is not supported.");
        }
        
        // Update the blog post
        boolean result = implementation.updatePost(blogDetails, postId, title, body, publish);
        
        // Check the return result
        if (result == false)
        {
            throw new BlogIntegrationRuntimeException("The update of the post unexpectedly failed.  Check your blog for more information.");
        }
        
        // Now get the details of the newly created post
        Map<String, Object> details = implementation.getPost(blogDetails, postId);
        String link = (String)details.get("link");
        
        // Update the post details accordingly
        Map<QName, Serializable> props = this.nodeService.getProperties(nodeRef);
        Date now = new Date();
        props.put(PROP_LAST_UPDATE, now);
        props.put(PROP_PUBLISHED, Boolean.valueOf(publish));
        props.put(PROP_LINK, link);
        this.nodeService.setProperties(nodeRef, props);
    }

    /**
     * @see org.alfresco.repo.blog.BlogIntegrationService#deletePost(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void deletePost(NodeRef nodeRef)
    {
        // Get the blog details and post id
        BlogDetails blogDetails = null;
        String postId = null;
        if (this.nodeService.hasAspect(nodeRef, ASPECT_BLOG_POST) == true)
        {
            List<AssociationRef> assocs = this.nodeService.getTargetAssocs(nodeRef, ASSOC_BLOG_DETAILS);
            if (assocs.size() == 0)
            {
                throw new BlogIntegrationRuntimeException("Can not resolve blog details for delete because blogDetails association is not populated.");
            }
            else
            {
                blogDetails = BlogDetails.createBlogDetails(this.nodeService, assocs.get(0).getTargetRef());
                postId = (String)this.nodeService.getProperty(nodeRef, PROP_POST_ID);
            }
        }
        else
        {
            throw new BlogIntegrationRuntimeException("Can not delete blog post as this node has not been previously posted to a blog.");
        }        
        
        // Get the blog implementation
        BlogIntegrationImplementation implementation = getImplementation(blogDetails.getImplementationName());
        
        // Delete the post
        boolean result = implementation.deletePost(blogDetails, postId);
        
        // Check the return result
        if (result == false)
        {
            throw new BlogIntegrationRuntimeException("Deleting the post unexpectedly failed.  Check your blog for more information.");
        }
        
        // Remove the postDetails aspect from the node
        this.nodeService.removeAspect(nodeRef, ASPECT_BLOG_POST);
    }
}
