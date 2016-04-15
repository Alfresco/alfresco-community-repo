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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.BlogIntegrationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.junit.experimental.categories.Category;

/**
 * @author Roy Wetherall
 */
@Category(BaseSpringTestsCategory.class)
public class BlogIntegrationServiceSystemTest extends BaseAlfrescoSpringTest implements BlogIntegrationModel
{
    /**
     * Wordpress details
     * 
     * http://paulhh.wordpress.com/xmlrpc.php
     * paulhh
     * 114eb1
     */
    
    /**
     * Typepad details
     * 
     * http://www.typepad.com/t/api
     * http://rwetherall.typepad.com/my_test_blog
     * 1340792
     * rwetherall
     * 
     */
    
    /** Blog Details **/
    private static final String BLOG = "wordpress";
    private static final String BLOG_URL = "http://paulhh.wordpress.com";
    private static final String BLOG_USER = "paulhh";
    private static final String BLOG_PWD = "114eb1";
    private static final String BLOG_ID = "0";
    
       
    private static final String BLOG_NAME = "Test blog details";
    private static final String BLOG_DESCRIPTION = "These are the details used to test the blog integration service";
    
    /** Blog entry */
    private static final String TITLE = "My Test Post @ " + new Date().toString();
    //private static final String TITLE = "";
    private static final String MODIFIED_TITLE = "My Test Post Modified @ " + new Date().toString();
    private static final String DESCRIPTION = "This is a description of my test post.";
    private static final String POST_CONTENT = "Hello and welcome to my test post.  This has been posted from the blog integration system test @ " + new Date().toString();
    private static final String MODIFIED_POST_CONTENT = "Hello and welcome to my MODIFIED test post.  This has been posted and MODIFIED from the blog integration system test @ " + new Date().toString();
    private static final boolean PUBLISH = true;
    
    private NodeService nodeService;    
    private ContentService contentService;
    private SearchService searchService;
    private BlogIntegrationService blogService;
    
    private NodeRef nodeRef;
    private NodeRef blogDetailsNodeRef;
    
    @Override
    protected void onSetUpInTransaction() 
        throws Exception 
    {
        super.onSetUpInTransaction();
        
        // Get references to the relevant services
        this.nodeService = (NodeService)this.applicationContext.getBean("nodeService");
        this.contentService = (ContentService)this.applicationContext.getBean("contentService");
        this.searchService = (SearchService)this.applicationContext.getBean("searchService");
        this.blogService = (BlogIntegrationService)this.applicationContext.getBean("blogIntegrationService");
        
        // Get a reference to the company home node
        ResultSet results1 = this.searchService.query(
                new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"), 
                SearchService.LANGUAGE_XPATH, 
                "app:company_home");
        NodeRef companyHome = results1.getNodeRefs().get(0);
        results1.close();
        
        // Create the blog details node
        this.blogDetailsNodeRef = this.nodeService.createNode(
                companyHome, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();
        this.nodeService.setProperty(this.blogDetailsNodeRef, ContentModel.PROP_NAME, "testFolder");
        Map<QName, Serializable> props2 = new HashMap<QName, Serializable>(5);
        props2.put(PROP_BLOG_IMPLEMENTATION, BLOG);
        props2.put(PROP_ID, BLOG_ID);
        props2.put(PROP_NAME, BLOG_NAME);
        props2.put(PROP_DESCRIPTION, BLOG_DESCRIPTION);
        props2.put(PROP_URL, BLOG_URL);
        props2.put(PROP_USER_NAME, BLOG_USER);
        props2.put(PROP_PASSWORD, BLOG_PWD);
        this.nodeService.addAspect(this.blogDetailsNodeRef, ASPECT_BLOG_DETAILS, props2);
                
        // Create the content node
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, "myBlogEntry.txt");        
        this.nodeRef = this.nodeService.createNode(
                this.blogDetailsNodeRef, 
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "myBlogEntry.txt"),
                ContentModel.TYPE_CONTENT, 
                props).getChildRef();
        
        // Add the titled aspect
        Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(2);
        titledProps.put(ContentModel.PROP_TITLE, TITLE);
        titledProps.put(ContentModel.PROP_DESCRIPTION, DESCRIPTION);
        this.nodeService.addAspect(this.nodeRef, ContentModel.ASPECT_TITLED, titledProps);
        
        // Add some content
        ContentWriter contentWriter = this.contentService.getWriter(this.nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        contentWriter.setEncoding("UTF-8");
        contentWriter.putContent(POST_CONTENT);
    }
    
    public void testGetBlogIntegrationImplementations()
    {
        List<BlogIntegrationImplementation> list = this.blogService.getBlogIntegrationImplementations();
        assertNotNull(list);
        assertEquals(2, list.size());
        
        BlogIntegrationImplementation blog = this.blogService.getBlogIntegrationImplementation(BLOG);
        assertNotNull(blog);
        assertEquals(BLOG, blog.getName());
    }
    
    public void testGetBlogDetails()
    {
        List<BlogDetails> details = this.blogService.getBlogDetails(this.nodeRef);
        assertNotNull(details);
        assertEquals(1, details.size());
        assertEquals(BLOG_URL, details.get(0).getUrl());
        
        List<BlogDetails> details2 = this.blogService.getBlogDetails(this.blogDetailsNodeRef);
        assertNotNull(details2);
        assertEquals(1, details2.size());
        assertEquals(BLOG_URL, details2.get(0).getUrl());
    }
    
    public void testNewPost()
    {
        // Create the blog details
        BlogDetails blogDetails = BlogDetails.createBlogDetails(this.nodeService, this.blogDetailsNodeRef);
        
        // Do a quick check on the blog details
        assertEquals(this.blogDetailsNodeRef, blogDetails.getNodeRef());
        assertEquals(BLOG, blogDetails.getImplementationName());
        assertEquals(BLOG_ID, blogDetails.getBlogId());
        assertEquals(BLOG_NAME, blogDetails.getName());
        assertEquals(BLOG_DESCRIPTION, blogDetails.getDescription());
        assertEquals(BLOG_URL, blogDetails.getUrl());
        assertEquals(BLOG_USER, blogDetails.getUserName());
        assertEquals(BLOG_PWD, blogDetails.getPassword());
        
        // Post and publish the content contained on the node
        this.blogService.newPost(blogDetails, this.nodeRef, ContentModel.PROP_CONTENT, PUBLISH);
        
        // Check the details of the node after the post
        assertTrue(this.nodeService.hasAspect(this.nodeRef, ASPECT_BLOG_POST));
        assertNotNull(this.nodeService.getProperty(this.nodeRef, PROP_POST_ID));
        System.out.println("The newly create post has id " + this.nodeService.getProperty(this.nodeRef, PROP_POST_ID));
        List<AssociationRef> assocs = this.nodeService.getTargetAssocs(this.nodeRef, ASSOC_BLOG_DETAILS);
        assertEquals(1, assocs.size());
        NodeRef testRef = assocs.get(0).getTargetRef();
        assertEquals(blogDetailsNodeRef, testRef);
        
        // TODO check the other stuff
        
        
        // Check that im not allowed to create another new post with the same node
        try
        {
            this.blogService.newPost(blogDetails, this.nodeRef, ContentModel.PROP_CONTENT, PUBLISH);
        }
        catch (BlogIntegrationRuntimeException e)
        {
            // Expected
        }
        
        // Edit the title and content
        this.nodeService.setProperty(this.nodeRef, ContentModel.PROP_TITLE, MODIFIED_TITLE);        
    }
    
    public void testUpdatePost()
    {
        // Create the blog details
        BlogDetails blogDetails = BlogDetails.createBlogDetails(this.nodeService, this.blogDetailsNodeRef);
               
        // Post and publish the content contained on the node
        this.blogService.newPost(blogDetails, this.nodeRef, ContentModel.PROP_CONTENT, PUBLISH);
        
        // Edit the title and content of the node
        this.nodeService.setProperty(this.nodeRef, ContentModel.PROP_TITLE, MODIFIED_TITLE);
        ContentWriter contentWriter = this.contentService.getWriter(this.nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.putContent(MODIFIED_POST_CONTENT);
        
        // Update the post
        this.blogService.updatePost(this.nodeRef, ContentModel.PROP_CONTENT, PUBLISH);
        
        // Check the updated meta-data .... TODO
    }
    
    public void testDeletePost()
    {
        // Create the blog details
        BlogDetails blogDetails = BlogDetails.createBlogDetails(this.nodeService, this.blogDetailsNodeRef);
               
        // Post and publish the content contained on the node
        this.blogService.newPost(blogDetails, this.nodeRef, ContentModel.PROP_CONTENT, PUBLISH);
        
        // Delete the post
        this.blogService.deletePost(this.nodeRef);
        
        // Check the aspect has bee removed from the node
        assertFalse(this.nodeService.hasAspect(this.nodeRef, ASPECT_BLOG_POST));
    }
}
