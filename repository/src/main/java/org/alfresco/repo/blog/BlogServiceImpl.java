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

import static java.lang.Math.min;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.EmptyPagingResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.blog.cannedqueries.BlogEntity;
import org.alfresco.repo.blog.cannedqueries.DraftsAndPublishedBlogPostsCannedQuery;
import org.alfresco.repo.blog.cannedqueries.DraftsAndPublishedBlogPostsCannedQueryFactory;
import org.alfresco.repo.blog.cannedqueries.GetBlogPostsCannedQuery;
import org.alfresco.repo.blog.cannedqueries.GetBlogPostsCannedQueryFactory;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.search.LuceneUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.site.SiteServiceImpl;
import org.alfresco.service.cmr.blog.BlogPostInfo;
import org.alfresco.service.cmr.blog.BlogService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.registry.NamedObjectRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * @author Neil Mc Erlean (based on existing webscript controllers in the REST API)
 * @since 4.0
 */
public class BlogServiceImpl implements BlogService
{
    public static final String BLOG_COMPONENT = "blog";
    private static final int MIN_NUMBER_OF_PAGES_FOR_THE_USER_TO_LOOP_THROUGH = 10;
    
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(BlogServiceImpl.class);
    
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
    private SiteService siteService;
    private TransactionService transactionService;
    private PermissionService permissionService;
    private TaggingService taggingService;
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
    
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    public void setTaggingService(TaggingService taggingService)
    {
        this.taggingService = taggingService;
    }
    
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    /**
     * Fetches the Blogs Container on a site, creating as required if requested.
     */
    protected NodeRef getSiteBlogContainer(final String siteShortName, boolean create)
    {
       return SiteServiceImpl.getSiteContainer(
             siteShortName, BLOG_COMPONENT, create,
             siteService, transactionService, taggingService);
    }
    
    /**
     * Builds up a {@link BlogPostInfo} object for the given node
     */
    private BlogPostInfo buildBlogPost(NodeRef nodeRef, NodeRef parentNodeRef, String postName)
    {
       BlogPostInfoImpl post = new BlogPostInfoImpl(nodeRef, parentNodeRef, postName);
       
       // Grab all the properties, we need the bulk of them anyway
       Map<QName,Serializable> props = nodeService.getProperties(nodeRef);

       // Populate them
       post.setTitle((String)props.get(ContentModel.PROP_TITLE));
       // TODO Populate the rest
       
       // Finally set tags
       // TODO
       
       // All done
       return post;
    }
    
    @Override
    public boolean isDraftBlogPost(NodeRef blogPostNode)
    {
        return nodeService.getProperty(blogPostNode, ContentModel.PROP_PUBLISHED) == null;
    }
    
    
    @Override
    public BlogPostInfo createBlogPost(String siteShortName, String blogTitle,
         String blogContent, boolean isDraft) 
    {
      // Grab the location to stor ein
      NodeRef container = getSiteBlogContainer(siteShortName, true);
      
      // Add by Parent NodeRef
      return createBlogPost(container, blogTitle, blogContent, isDraft);
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
        ChildAssociationRef postNode = null;
        try
        {
            postNode = nodeService.createNode(
                    blogContainerNode, ContentModel.ASSOC_CONTAINS, assocName,
                    ContentModel.TYPE_CONTENT, nodeProps);
        }
        catch (DuplicateChildNodeNameException e)
        {
            // This will be rare, but it's not impossible.
            // We have to retry the operation.
            throw new ConcurrencyFailureException("Blog post name already used: " + nodeName);
        }
        
        ContentWriter writer = contentService.getWriter(postNode.getChildRef(), ContentModel.PROP_CONTENT, true);
        
        // Blog posts are always HTML (based on the JavaScript this class replaces.)
        writer.setMimetype(MimetypeMap.MIMETYPE_HTML);
        writer.setEncoding("UTF-8");
        writer.putContent(blogContent);
        
        if (isDraft)
        {
            // Comment from the old JavaScript:
            // disable permission inheritance. The result is that only the creator will have access to the draft
            NodeRef draft = postNode.getChildRef();
            permissionService.setInheritParentPermissions(draft, false);
            
            // MNT-12082: give permissions to the post creator. He should be able to comment in his post's
            // forumFolder and commentsFolder, where owner is System user
            String creator = (String) nodeService.getProperty(draft, ContentModel.PROP_CREATOR);
            permissionService.setPermission(draft, creator, permissionService.getAllPermission(), true);
        }
        else
        {
            setOrUpdateReleasedAndUpdatedDates(postNode.getChildRef());
        }
        
        BlogPostInfo post = new BlogPostInfoImpl(postNode.getChildRef(), blogContainerNode, nodeName);
        post.setTitle(blogTitle);
        return post;
    }
   
    @Override
    public BlogPostInfo updateBlogPost(BlogPostInfo post) {
       if (post.getNodeRef() == null)
       {
          throw new IllegalArgumentException("Can't update a post that was never persisted, call create instead");
       }
       
       // TODO Implement, once BlogPostInfo is finished
       throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void deleteBlogPost(BlogPostInfo post) {
       if (post.getNodeRef() == null)
       {
          throw new IllegalArgumentException("Can't delete a post that was never persisted");
       }
       
       nodeService.deleteNode(post.getNodeRef());
    }

    
    @Override
    public BlogPostInfo getForNodeRef(NodeRef nodeRef) 
    {
       QName type = nodeService.getType(nodeRef);

       // Note - there isn't a special blog type!
       //  The nodes are just created as cm:Content 
       if (type.equals(ContentModel.TYPE_CONTENT))
       {
          ChildAssociationRef ref = nodeService.getPrimaryParent(nodeRef);
          String postName = ref.getQName().getLocalName();
          NodeRef container = ref.getParentRef();
          return buildBlogPost(nodeRef, container, postName);
       }
       else
       {
          logger.debug("Invalid type " + type + " found");
          return null;
       }
    }

    @Override
    public BlogPostInfo getBlogPost(String siteShortName, String postName) 
    {
       NodeRef container = getSiteBlogContainer(siteShortName, false);
       if (container == null)
       {
          // No blog posts yet
          return null;
       }
       
       // We can now fetch by parent nodeRef
       return getBlogPost(container, postName);
    }

    @Override
    public BlogPostInfo getBlogPost(NodeRef parentNodeRef, String postName) 
    {
       NodeRef postNode;
       try
       {
          postNode = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, postName);
       }
       catch(AccessDeniedException e)
       {
          // You can't see that blog post
          // For compatibility with the old webscripts, rather than
          //  reporting permission denied, pretend it isn't there
          postNode = null;
       }

       // If we found a node, wrap it as a BlogPostInfo
       if (postNode != null)
       {
          return buildBlogPost(postNode, parentNodeRef, postName);
       }
       return null;
    }

   @Override
    public PagingResults<BlogPostInfo> getDrafts(String siteShortName,
          String username, PagingRequest pagingReq) 
    {
       NodeRef container = getSiteBlogContainer(siteShortName, false);
       if (container == null)
       {
          // No blog posts yet
          return new EmptyPagingResults<BlogPostInfo>();
       }
       
       // We can now fetch by parent nodeRef
       return getDrafts(container, username, pagingReq);
    }

    @Override
    public PagingResults<BlogPostInfo> getDrafts(NodeRef blogContainerNode, String username, PagingRequest pagingReq)
    {
        ParameterCheck.mandatory("blogContainerNode", blogContainerNode);
        ParameterCheck.mandatory("pagingReq", pagingReq);
        
        // get canned query
        GetBlogPostsCannedQuery cq = (GetBlogPostsCannedQuery)draftPostsCannedQueryFactory.getGetDraftsCannedQuery(blogContainerNode, username, pagingReq);
            
        // execute canned query
        CannedQueryResults<BlogEntity> results = cq.execute();
        return wrap(results, blogContainerNode);
    }

    @Override
    public PagingResults<BlogPostInfo> getPublished(String siteShortName,
         Date fromDate, Date toDate, String byUser, PagingRequest pagingReq) 
    {
       NodeRef container = getSiteBlogContainer(siteShortName, false);
       if (container == null)
       {
          // No blog posts yet
          return new EmptyPagingResults<BlogPostInfo>();
       }
       
       // We can now fetch by parent nodeRef
       return getPublished(container, fromDate, toDate, byUser, pagingReq);
    }

    @Override
    public PagingResults<BlogPostInfo> getPublished(NodeRef blogContainerNode, Date fromDate, Date toDate, String byUser, PagingRequest pagingReq)
    {
        ParameterCheck.mandatory("blogContainerNode", blogContainerNode);
        ParameterCheck.mandatory("pagingReq", pagingReq);
        
        // get canned query
        GetBlogPostsCannedQuery cq = (GetBlogPostsCannedQuery)publishedPostsCannedQueryFactory.getGetPublishedCannedQuery(blogContainerNode, fromDate, toDate, byUser, pagingReq);
            
        // execute canned query
        CannedQueryResults<BlogEntity> results = cq.execute();
        return wrap(results, blogContainerNode);
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
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        DraftsAndPublishedBlogPostsCannedQuery cq = (DraftsAndPublishedBlogPostsCannedQuery)draftsAndPublishedBlogPostsCannedQueryFactory.getCannedQuery(blogContainerNode, createdFrom, createdTo, currentUser, pagingReq);
            
        // execute canned query
        CannedQueryResults<BlogEntity> results = cq.execute();
        return wrap(results, blogContainerNode);
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
            // MNT-12082 
            String creator = (String) nodeService.getProperty(blogPostNode, ContentModel.PROP_CREATOR);
            permissionService.deletePermission(blogPostNode, creator, permissionService.getAllPermission());
            
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
    public PagingResults<BlogPostInfo> findBlogPosts(String siteShortName,
         RangedDateProperty dateRange, String tag, PagingRequest pagingReq) 
    {
        NodeRef container = getSiteBlogContainer(siteShortName, false);
        if (container == null)
        {
           // No blog posts yet
           return new EmptyPagingResults<BlogPostInfo>();
        }
        
        // We can now fetch by parent nodeRef
        return findBlogPosts(container, dateRange, tag, pagingReq);
    }

    @Override
    public PagingResults<BlogPostInfo> findBlogPosts(
          final NodeRef blogContainerNode, final RangedDateProperty dateRange, 
          final String tag, final PagingRequest pagingReq)
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

        sp.setMaxItems(pagingReq.getMaxItems() * MIN_NUMBER_OF_PAGES_FOR_THE_USER_TO_LOOP_THROUGH); 
        sp.setSkipCount(pagingReq.getSkipCount());
        ResultSet luceneResults = null;
        PagingResults<BlogPostInfo> results = null;
        try
        {
            luceneResults = searchService.query(sp);
            final ResultSet finalLuceneResults = luceneResults;
            
            final List<NodeRef> nodeRefs = finalLuceneResults.getNodeRefs().subList(0, min(pagingReq.getMaxItems(), finalLuceneResults.length()));
            
            results = new PagingResults<BlogPostInfo>()
            {
                
                @Override
                public List<BlogPostInfo> getPage()
                {
                    List<BlogPostInfo> blogPostInfos = new ArrayList<BlogPostInfo>(nodeRefs.size());
                    for (NodeRef nodeRef : nodeRefs)
                    {
                        String postName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME); 
                        blogPostInfos.add(new BlogPostInfoImpl(nodeRef, blogContainerNode, postName));
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
                    long totalResultCount = finalLuceneResults.getNumberFound();
                    /*if (finalLuceneResults.hasMore()){
                        totalResultCount++;
                    }*/ 
                    return new Pair<Integer, Integer>((int)totalResultCount, (int)totalResultCount);
                }
                
                @Override
                public boolean hasMoreItems()
                {
                    return finalLuceneResults.length() > pagingReq.getMaxItems();
                }
            };
        }
        finally
        {
            if (luceneResults != null) luceneResults.close();
        }
        
        
        return results;
    }
    
    private PagingResults<BlogPostInfo> wrap(final CannedQueryResults<BlogEntity> results, final NodeRef containerNodeRef)
    {
       // TODO Pre-load all the nodes via the NodeDAO cache
       
       // Wrap
       return new PagingResults<BlogPostInfo>() {
          @Override
          public String getQueryExecutionId() {
             return results.getQueryExecutionId();
          }

          @Override
          public Pair<Integer, Integer> getTotalResultCount() {
             return results.getTotalResultCount();
          }

          @Override
          public boolean hasMoreItems() {
             return results.hasMoreItems();
          }

          @Override
          public List<BlogPostInfo> getPage() {
             List<BlogEntity> entities = results.getPage();
             List<BlogPostInfo> posts = new ArrayList<BlogPostInfo>(entities.size());
             
             for (BlogEntity entity : entities)
             {
                posts.add(new BlogPostInfoImpl(
                      entity.getNodeRef(), containerNodeRef, entity.getName()
                ));
             }
             return posts;
          }
       };
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
