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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.blog.cannedqueries.DraftsAndPublishedBlogPostsCannedQuery;
import org.alfresco.repo.blog.cannedqueries.DraftsAndPublishedBlogPostsCannedQueryFactory;
import org.alfresco.repo.blog.cannedqueries.GetBlogPostsCannedQuery;
import org.alfresco.repo.blog.cannedqueries.GetBlogPostsCannedQueryFactory;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.search.impl.lucene.LuceneUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.blog.BlogService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.registry.NamedObjectRegistry;

/**
 * @author Neil Mc Erlean (based on existing webscript controllers in the REST API)
 * @since 4.0
 */
public class BlogServiceImpl implements BlogService
{
    /**
     *  For backwards compatibility with pre-Swift, we are asking the query to give us an accurate total count of how many
     *  blog-post nodes there are. This may need to change in the future - certainly if the current 'brute force' query
     *  is replaced by a database query.
     */
    private static final int MAX_QUERY_ENTRY_COUNT = 10000;
    
    // Injected services
    private NamedObjectRegistry<CannedQueryFactory<BlogPostInfo>> cannedQueryRegistry;
    private GetBlogPostsCannedQueryFactory draftPostsCannedQueryFactory;
    private GetBlogPostsCannedQueryFactory publishedPostsCannedQueryFactory;
    private GetBlogPostsCannedQueryFactory publishedExternallyPostsCannedQueryFactory;
    
    private DraftsAndPublishedBlogPostsCannedQueryFactory draftsAndPublishedBlogPostsCannedQueryFactory;
    
    private ContentService contentService;
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    private PermissionService permissionService;
    private SearchService searchService;
    
    public void setCannedQueryRegistry(NamedObjectRegistry<CannedQueryFactory<BlogPostInfo>> cannedQueryRegistry)
    {
        this.cannedQueryRegistry = cannedQueryRegistry;
    }
    
    public void setDraftBlogPostsCannedQueryFactory(GetBlogPostsCannedQueryFactory cannedQueryFactory)
    {
        this.draftPostsCannedQueryFactory = cannedQueryFactory;
    }
    
    public void setPublishedBlogPostsCannedQueryFactory(GetBlogPostsCannedQueryFactory cannedQueryFactory)
    {
        this.publishedPostsCannedQueryFactory = cannedQueryFactory;
    }
    
    public void setPublishedExternallyBlogPostsCannedQueryFactory(GetBlogPostsCannedQueryFactory cannedQueryFactory)
    {
        this.publishedExternallyPostsCannedQueryFactory = cannedQueryFactory;
    }
    
    public void setDraftsAndPublishedBlogPostsCannedQueryFactory(DraftsAndPublishedBlogPostsCannedQueryFactory cannedQueryFactory)
    {
        this.draftsAndPublishedBlogPostsCannedQueryFactory = cannedQueryFactory;
    }
    
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    @Override
    public boolean isDraftBlogPost(NodeRef blogPostNode)
    {
        return nodeService.getProperty(blogPostNode, ContentModel.PROP_PUBLISHED) == null;
    }
    
    @Override
    public BlogPostInfo createBlogPost(NodeRef blogContainerNode, String blogTitle,
                                              String blogContent, boolean isDraft)
    {
        final String nodeName = getUniqueChildName(blogContainerNode, "post");
        
        // we simply create a new file inside the blog folder
        Map<QName, Serializable> nodeProps = new HashMap<QName, Serializable>();
        nodeProps.put(ContentModel.PROP_NAME, nodeName);
        nodeProps.put(ContentModel.PROP_TITLE, blogTitle);
        QName assocName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nodeName);
        ChildAssociationRef postNode = nodeService.createNode(blogContainerNode, ContentModel.ASSOC_CONTAINS, assocName,
                                                  ContentModel.TYPE_CONTENT, nodeProps);
        
        ContentWriter writer = contentService.getWriter(postNode.getChildRef(), ContentModel.PROP_CONTENT, true);
        
        // Blog posts are always HTML (based on the JavaScript this class replaces.)
        writer.setMimetype(MimetypeMap.MIMETYPE_HTML);
        writer.setEncoding("UTF-8");
        writer.putContent(blogContent);
        
        if (isDraft)
        {
            // Comment from the old JavaScript:
            // disable permission inheritance. The result is that only the creator will have access to the draft
            permissionService.setInheritParentPermissions(postNode.getChildRef(), false);
        }
        else
        {
            setOrUpdateReleasedAndUpdatedDates(postNode.getChildRef());
        }
        
        return new BlogPostInfo(postNode.getChildRef(), nodeName);
    }
    
    @Override
    public PagingResults<BlogPostInfo> getDrafts(NodeRef blogContainerNode, String username, PagingRequest pagingReq)
    {
        ParameterCheck.mandatory("blogContainerNode", blogContainerNode);
        ParameterCheck.mandatory("pagingReq", pagingReq);
        
        // get canned query
        pagingReq.setRequestTotalCountMax(MAX_QUERY_ENTRY_COUNT);
        GetBlogPostsCannedQuery cq = (GetBlogPostsCannedQuery)draftPostsCannedQueryFactory.getGetDraftsCannedQuery(blogContainerNode, username, pagingReq);
            
        // execute canned query
        CannedQueryResults<BlogPostInfo> results = cq.execute();
        return results;
    }
    @Override
    public PagingResults<BlogPostInfo> getPublishedExternally(NodeRef blogContainerNode, PagingRequest pagingReq)
    {
        ParameterCheck.mandatory("blogContainerNode", blogContainerNode);
        ParameterCheck.mandatory("pagingReq", pagingReq);
        
        // get canned query
        pagingReq.setRequestTotalCountMax(MAX_QUERY_ENTRY_COUNT);
        GetBlogPostsCannedQuery cq = (GetBlogPostsCannedQuery)publishedExternallyPostsCannedQueryFactory.getGetPublishedExternallyCannedQuery(blogContainerNode, pagingReq);
            
        // execute canned query
        CannedQueryResults<BlogPostInfo> results = cq.execute();
        return results;
    }
    
    @Override
    public PagingResults<BlogPostInfo> getPublished(NodeRef blogContainerNode, Date fromDate, Date toDate, String byUser, PagingRequest pagingReq)
    {
        ParameterCheck.mandatory("blogContainerNode", blogContainerNode);
        ParameterCheck.mandatory("pagingReq", pagingReq);
        
        // get canned query
        pagingReq.setRequestTotalCountMax(MAX_QUERY_ENTRY_COUNT);
        GetBlogPostsCannedQuery cq = (GetBlogPostsCannedQuery)publishedPostsCannedQueryFactory.getGetPublishedCannedQuery(blogContainerNode, fromDate, toDate, byUser, pagingReq);
            
        // execute canned query
        CannedQueryResults<BlogPostInfo> results = cq.execute();
        return results;
    }
    
    /**
     * @deprecated
     */
    @Override
    public PagingResults<BlogPostInfo> getMyDraftsAndAllPublished(NodeRef blogContainerNode, Date createdFrom, Date createdTo, PagingRequest pagingReq)
    {
        ParameterCheck.mandatory("blogContainerNode", blogContainerNode);
        ParameterCheck.mandatory("pagingReq", pagingReq);
        
        // get canned query
        pagingReq.setRequestTotalCountMax(MAX_QUERY_ENTRY_COUNT);
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        DraftsAndPublishedBlogPostsCannedQuery cq = (DraftsAndPublishedBlogPostsCannedQuery)draftsAndPublishedBlogPostsCannedQueryFactory.getCannedQuery(blogContainerNode, createdFrom, createdTo, currentUser, pagingReq);
            
        // execute canned query
        CannedQueryResults<BlogPostInfo> results = cq.execute();
        return results;
    }
    
    private String getUniqueChildName(NodeRef parentNode, String prefix)
    {
        return prefix + "-" + System.currentTimeMillis();
    }

    
    /**
     * This method is taken from the previous JavaScript webscript controllers.
     */
    private void setOrUpdateReleasedAndUpdatedDates(NodeRef blogPostNode)
    {
        // make sure the syndication aspect has been added
        if (!nodeService.hasAspect(blogPostNode, ContentModel.ASPECT_SYNDICATION))
        {
            nodeService.addAspect(blogPostNode, ContentModel.ASPECT_SYNDICATION, null);
        }

        // (re-)enable permission inheritance which got disable for draft posts
        // only set if was previously draft - as only the owner/admin can do
        // this
        if (!permissionService.getInheritParentPermissions(blogPostNode))
        {
            permissionService.setInheritParentPermissions(blogPostNode, true);
        }

        // check whether the published date has been set
        if (nodeService.getProperty(blogPostNode, ContentModel.PROP_PUBLISHED) == null)
        {
            nodeService.setProperty(blogPostNode, ContentModel.PROP_PUBLISHED, new Date());
        }
        else
        {
            // set/update the updated date
            nodeService.setProperty(blogPostNode, ContentModel.PROP_UPDATED, new Date());
        }
    }
    
    @Override
    public PagingResults<BlogPostInfo> findBlogPosts(NodeRef blogContainerNode, RangedDateProperty dateRange, String tag, PagingRequest pagingReq)
    {
        StringBuilder luceneQuery = new StringBuilder();
        luceneQuery.append("+TYPE:\"").append(ContentModel.TYPE_CONTENT).append("\" ")
                   .append("+PARENT:\"").append(blogContainerNode.toString()).append("\" ");
        if (tag != null && !tag.trim().isEmpty())
        {
            luceneQuery.append("+PATH:\"/cm:taggable/cm:").append(ISO9075.encode(tag)).append("/member\"");
        }
        if (dateRange != null)
        {
            luceneQuery.append(createDateRangeQuery(dateRange.getFromDate(), dateRange.getToDate(), dateRange.getDateProperty()));
        }
        
        SearchParameters sp = new SearchParameters();
        sp.addStore(blogContainerNode.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(luceneQuery.toString());
        sp.addSort(ContentModel.PROP_PUBLISHED.toString(), false);
        
        sp.setMaxItems(pagingReq.getMaxItems());
        sp.setSkipCount(pagingReq.getSkipCount());
        
        ResultSet luceneResults = null;
        PagingResults<BlogPostInfo> results = null;
        try
        {
            luceneResults = searchService.query(sp);
            final ResultSet finalLuceneResults = luceneResults;
            
            results = new PagingResults<BlogPostInfo>()
            {
                
                @Override
                public List<BlogPostInfo> getPage()
                {
                    List<NodeRef> nodeRefs = finalLuceneResults.getNodeRefs();
                    List<BlogPostInfo> blogPostInfos = new ArrayList<BlogPostInfo>(nodeRefs.size());
                    for (NodeRef nodeRef : nodeRefs)
                    {
                        blogPostInfos.add(new BlogPostInfo(nodeRef, (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)));
                    }
                    return blogPostInfos;
                }
                
                @Override
                public String getQueryExecutionId()
                {
                    return null;
                }
                
                @Override
                public Pair<Integer, Integer> getTotalResultCount()
                {
                    int skipCount = finalLuceneResults.getStart();
                    int itemsRemainingAfterThisPage = finalLuceneResults.length();
                    final int totalItemsInUnpagedResultSet = skipCount + itemsRemainingAfterThisPage;
                    return new Pair<Integer, Integer>(totalItemsInUnpagedResultSet, totalItemsInUnpagedResultSet);
                }
                
                @Override
                public boolean hasMoreItems()
                {
                    return finalLuceneResults.hasMore();
                }
            };
        }
        finally
        {
            if (luceneResults != null) luceneResults.close();
        }
        
        
        return results;
    }

    /**
     * This method creates a Lucene query fragment which constrains the specified dateProperty to a range
     * given by the fromDate and toDate parameters.
     * 
     * @param fromDate     the start of the date range (defaults to 1970-01-01 00:00:00 if null).
     * @param toDate       the end of the date range (defaults to 3000-12-31 00:00:00 if null).
     * @param dateProperty the Alfresco property value to check against the range (must be a valid Date or DateTime property).
     * 
     * @return the Lucene query fragment.
     * 
     * @throws NullPointerException if dateProperty is null or if the dateProperty is not recognised by the system.
     * @throws IllegalArgumentException if dateProperty refers to a property that is not of type {@link DataTypeDefinition#DATE} or {@link DataTypeDefinition#DATETIME}.
     */
    private String createDateRangeQuery(Date fromDate, Date toDate, QName dateProperty)
    {
       return LuceneUtils.createDateRangeQuery(fromDate, toDate, dateProperty, dictionaryService, namespaceService);
    }
}
