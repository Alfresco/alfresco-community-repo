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
package org.alfresco.service.cmr.blog;

import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;

/**
 * The Blog Service handles the management (CRUD) of Alfresco blog data, namely the blog posts which are exposed in the Share UI under the "Blog" heading.
 * <p/>
 * Please note that this service is a work in progress and currently exists primarily to support the blogs REST API.
 * 
 * @author Neil Mc Erlean (based on existing webscript controllers in the REST API)
 * @since 4.0
 */
public interface BlogService
{
    /**
     * Creates a new blog post in the specified site
     * 
     * @param siteShortName
     *            the name of the site to add the post to.
     * @param blogTitle
     *            the title of the blog post.
     * @param blogContent
     *            text/html content of the blog post.
     * @param isDraft
     *            <tt>true</tt> if the blog post is a draft post, else <tt>false</tt>.
     * 
     * @return The {@link BlogPostInfo} of the newly created blog post.
     * 
     * @see SiteService#getContainer(String, String) to retrieve the blogContainerNode
     */
    BlogPostInfo createBlogPost(String siteShortName, String blogTitle, String blogContent, boolean isDraft);

    /**
     * Creates a new blog post within the specified container node.
     * 
     * @param blogContainerNode
     *            the container node for blog posts (under the site).
     * @param blogTitle
     *            the title of the blog post.
     * @param blogContent
     *            text/html content of the blog post.
     * @param isDraft
     *            <tt>true</tt> if the blog post is a draft post, else <tt>false</tt>.
     * 
     * @return The {@link BlogPostInfo} of the newly created blog post.
     * 
     * @see SiteService#getContainer(String, String) to retrieve the blogContainerNode
     */
    BlogPostInfo createBlogPost(NodeRef blogContainerNode, String blogTitle, String blogContent, boolean isDraft);

    /**
     * Updates an existing {@link BlogPostInfo} in the repository.
     * 
     * @return The updated {@link BlogPostInfo}
     */
    BlogPostInfo updateBlogPost(BlogPostInfo post);

    /**
     * Deletes an existing {@link BlogPostInfo} from the repository.
     */
    void deleteBlogPost(BlogPostInfo post);

    /**
     * For a given NodeRef corresponding to a {@link BlogPostInfo}, returns the object wrapping the Node.
     * 
     * For anything else, the response is null.
     */
    @NotAuditable
    BlogPostInfo getForNodeRef(NodeRef nodeRef);

    /**
     * Retrieves an existing {@link BlogPostInfo} from the repository, which is within a site
     */
    BlogPostInfo getBlogPost(String siteShortName, String postName);

    /**
     * Retrieves an existing {@link BlogPostInfo} from the repository, which is attached to the specified Node. The parent Node should normally be a Site Container
     */
    BlogPostInfo getBlogPost(NodeRef parentNodeRef, String postName);

    /**
     * Gets the draft blog posts created by the specified user.
     * 
     * @param siteShortName
     *            the name of the site to add the post to.
     * @param username
     *            to limit results to blogs with this cm:creator. <tt>null</tt> means all users.
     * @param pagingReq
     *            an object defining the paging parameters for the result set.
     * 
     * @return a {@link PagingResults} object containing some or all of the results (subject to paging).
     * 
     * @see SiteService#getContainer(String, String) to retrieve the blogContainerNode
     */
    PagingResults<BlogPostInfo> getDrafts(String siteShortName, String username, PagingRequest pagingReq);

    /**
     * Gets the draft blog posts created by the specified user.
     * 
     * @param blogContainerNode
     *            the container node for blog posts (under the site).
     * @param username
     *            to limit results to blogs with this cm:creator. <tt>null</tt> means all users.
     * @param pagingReq
     *            an object defining the paging parameters for the result set.
     * 
     * @return a {@link PagingResults} object containing some or all of the results (subject to paging).
     * 
     * @see SiteService#getContainer(String, String) to retrieve the blogContainerNode
     */
    PagingResults<BlogPostInfo> getDrafts(NodeRef blogContainerNode, String username, PagingRequest pagingReq);

    /**
     * Gets the (internally, Alfresco-) published blog posts.
     * 
     * @param siteShortName
     *            the name of the site to add the post to.
     * @param fromDate
     *            an inclusive date limit for the results (more recent than).
     * @param toDate
     *            an inclusive date limit for the results (before).
     * @param byUser
     *            if not <tt>null</tt> limits results to posts by the specified user. if <tt>null</tt> results will be by all users.
     * @param pagingReq
     *            an object defining the paging parameters for the result set.
     * 
     * @return a {@link PagingResults} object containing some or all of the results (subject to paging).
     * 
     * @see SiteService#getContainer(String, String) to retrieve the blogContainerNode
     */
    PagingResults<BlogPostInfo> getPublished(String siteShortName, Date fromDate, Date toDate, String byUser, PagingRequest pagingReq);

    /**
     * Gets the (internally, Alfresco-) published blog posts.
     * 
     * @param blogContainerNode
     *            the container node for blog posts (under the site).
     * @param fromDate
     *            an inclusive date limit for the results (more recent than).
     * @param toDate
     *            an inclusive date limit for the results (before).
     * @param byUser
     *            if not <tt>null</tt> limits results to posts by the specified user. if <tt>null</tt> results will be by all users.
     * @param pagingReq
     *            an object defining the paging parameters for the result set.
     * 
     * @return a {@link PagingResults} object containing some or all of the results (subject to paging).
     * 
     * @see SiteService#getContainer(String, String) to retrieve the blogContainerNode
     */
    PagingResults<BlogPostInfo> getPublished(NodeRef blogContainerNode, Date fromDate, Date toDate, String byUser, PagingRequest pagingReq);

    /**
     * Gets draft blog posts by the currently authenticated user along with all published posts.
     * 
     * @param blogContainerNode
     *            the container node for blog posts (under the site).
     * @param fromDate
     *            an inclusive date limit for the results (more recent than).
     * @param toDate
     *            an inclusive date limit for the results (before).
     * @param pagingReq
     *            an object defining the paging parameters for the result set.
     * 
     * @return a {@link PagingResults} object containing some or all of the results (subject to paging).
     * 
     * @see SiteService#getContainer(String, String) to retrieve the blogContainerNode
     * 
     * @deprecated This method is a domain-specific query used by the Blog REST API and is not considered suitable for general use.
     */
    PagingResults<BlogPostInfo> getMyDraftsAndAllPublished(NodeRef blogContainerNode, Date fromDate, Date toDate, PagingRequest pagingReq);

    /**
     * Finds blog posts by the specified user tagged with the given tag string. This method allows date ranges to be applied to any valid {@link DataTypeDefinition#DATE} or {@link DataTypeDefinition#DATETIME} property. Examples include {@link ContentModel#PROP_CREATED} or {@link ContentModel#PROP_PUBLISHED}.
     * 
     * @param siteShortName
     *            the name of the site to add the post to.
     * @param dateRange
     *            a {@link RangedDateProperty} parameter object. Can be null.
     * @param tag
     *            tag string.
     * @param pagingReq
     *            an object defining the paging parameters for the result set.
     * 
     * @return a {@link PagingResults} object containing some or all of the results (subject to paging).
     * 
     * @see SiteService#getContainer(String, String) to retrieve the blogContainerNode
     */
    PagingResults<BlogPostInfo> findBlogPosts(String siteShortName, RangedDateProperty dateRange, String tag, PagingRequest pagingReq);

    /**
     * Finds blog posts by the specified user tagged with the given tag string. This method allows date ranges to be applied to any valid {@link DataTypeDefinition#DATE} or {@link DataTypeDefinition#DATETIME} property. Examples include {@link ContentModel#PROP_CREATED} or {@link ContentModel#PROP_PUBLISHED}.
     * 
     * @param blogContainerNode
     *            the container node for blog posts (under the site).
     * @param dateRange
     *            a {@link RangedDateProperty} parameter object. Can be null.
     * @param tag
     *            tag string.
     * @param pagingReq
     *            an object defining the paging parameters for the result set.
     * 
     * @return a {@link PagingResults} object containing some or all of the results (subject to paging).
     * 
     * @see SiteService#getContainer(String, String) to retrieve the blogContainerNode
     */
    PagingResults<BlogPostInfo> findBlogPosts(NodeRef blogContainerNode, RangedDateProperty dateRange, String tag, PagingRequest pagingReq);

    /**
     * Returns true if the specified blog-post node is a 'draft' blog post.
     * 
     * @param blogPostNode
     *            a NodeRef representing a blog-post.
     * @return <tt>true</tt> if it is a draft post, else <tt>false</tt>.
     * 
     * @deprecated Add this to the BlogPostInfo shortly
     */
    boolean isDraftBlogPost(NodeRef blogPostNode);

    /**
     * A simple data object for expressing a date range search parameter.
     */
    public class RangedDateProperty
    {
        private final Date fromDate;
        private final Date toDate;
        private final QName dateProperty;

        /**
         * Constructs a ConstrainedDateProperty object.
         * 
         * @param fromDate
         *            the start date for the range (can be null for unbounded lower)
         * @param toDate
         *            the end date for the range (can be null for unbounded upper)
         * @param dateProperty
         *            the Alfresco node property which is to be checked against the range. (must be a valid date or datetime property)
         */
        public RangedDateProperty(Date fromDate, Date toDate, QName dateProperty)
        {
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.dateProperty = dateProperty;
        }

        public Date getFromDate()
        {
            return fromDate;
        }

        public Date getToDate()
        {
            return toDate;
        }

        public QName getDateProperty()
        {
            return dateProperty;
        }
    }
}
