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
package org.alfresco.repo.blog;

import java.util.Date;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteService;

/**
 * The Blog Service handles the management (CRUD) of Alfresco blog data, namely the blog posts which are
 * exposed in the Share UI under the "Blog" heading. The {@link BlogIntegrationService}, a separate service, is
 * concerned with the integration of Alfresco blog content with external Blog-hosting sites.
 * <p/>
 * Please note that this service is a work in progress and currently exists primarily to support the blogs REST API.
 * 
 * @author Neil Mc Erlean (based on existing webscript controllers in the REST API)
 * @since 4.0
 */
public interface BlogService
{
    /**
     * Creates a new blog post within the specified container node.
     * 
     * @param blogContainerNode the container node for blog posts (under the site).
     * @param blogTitle         the title of the blog post.
     * @param blogContent       text/html content of the blog post.
     * @param isDraft           <tt>true</tt> if the blog post is a draft post, else <tt>false</tt>.
     * 
     * @return The {@link ChildAssociationRef} of the newly created blog post.
     * 
     * @see SiteService#getContainer(String, String) to retrieve the blogContainerNode
     */
    ChildAssociationRef createBlogPost(NodeRef blogContainerNode, String blogTitle,
                                       String blogContent, boolean isDraft);
    
    /**
     * Gets the draft blog posts created by the specified user.
     * 
     * @param blogContainerNode the container node for blog posts (under the site).
     * @param username          to limit results to blogs with this cm:creator. <tt>null</tt> means all users.
     * @param pagingReq         an object defining the paging parameters for the result set.
     * 
     * @return a {@link PagingResults} object containing some or all of the results (subject to paging).
     * 
     * @see SiteService#getContainer(String, String) to retrieve the blogContainerNode
     */
    PagingResults<BlogPostInfo> getDrafts(NodeRef blogContainerNode, String username, PagingRequest pagingReq);
    
    /**
     * Gets the (internally, Alfresco-) published blog posts.
     * 
     * @param blogContainerNode the container node for blog posts (under the site).
     * @param fromDate          an inclusive date limit for the results (more recent than).
     * @param toDate            an inclusive date limit for the results (before).
     * @param byUser            if not <tt>null</tt> limits results to posts by the specified user.
     *                          if <tt>null</tt> results will be by all users.
     * @param pagingReq         an object defining the paging parameters for the result set.
     * 
     * @return a {@link PagingResults} object containing some or all of the results (subject to paging).
     * 
     * @see SiteService#getContainer(String, String) to retrieve the blogContainerNode
    */
    PagingResults<BlogPostInfo> getPublished(NodeRef blogContainerNode, Date fromDate, Date toDate, String byUser, PagingRequest pagingReq);
    
    /**
     * Gets blog posts published externally (i.e. to an external blog hosting site).
     * 
     * @param blogContainerNode the container node for blog posts (under the site).
     * @param pagingReq         an object defining the paging parameters for the result set.
     * 
     * @return a {@link PagingResults} object containing some or all of the results (subject to paging).
     * 
     * @see SiteService#getContainer(String, String) to retrieve the blogContainerNode
     */
    PagingResults<BlogPostInfo> getPublishedExternally(NodeRef blogContainerNode, PagingRequest pagingReq);
    
    /**
     * Gets draft blog posts by the currently authenticated user along with all published posts.
     * 
     * @param blogContainerNode the container node for blog posts (under the site).
     * @param fromDate          an inclusive date limit for the results (more recent than).
     * @param toDate            an inclusive date limit for the results (before).
     * @param tag               if specified, only returns posts tagged with this tag.
     * @param pagingReq     an object defining the paging parameters for the result set.
     * 
     * @return a {@link PagingResults} object containing some or all of the results (subject to paging).
     * 
     * @see SiteService#getContainer(String, String) to retrieve the blogContainerNode
     * 
     * @deprecated This method is a domain-specific query used by the Blog REST API and is not considered suitable for general use.
     */
    PagingResults<BlogPostInfo> getMyDraftsAndAllPublished(NodeRef blogContainerNode, Date fromDate, Date toDate,
                                                      String tag, PagingRequest pagingReq);
    
    /**
     * Returns true if the specified blog-post node is a 'draft' blog post.
     * 
     * @param blogPostNode a NodeRef representing a blog-post.
     * @return <tt>true</tt> if it is a draft post, else <tt>false</tt>.
     */
    boolean isDraftBlogPost(NodeRef blogPostNode);
}
