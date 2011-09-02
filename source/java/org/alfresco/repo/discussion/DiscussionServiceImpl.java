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
package org.alfresco.repo.discussion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.EmptyCannedQueryResults;
import org.alfresco.query.EmptyPagingResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.discussion.cannedqueries.GetDiscussionTopcisWithPostsCannedQuery;
import org.alfresco.repo.discussion.cannedqueries.GetDiscussionTopcisWithPostsCannedQueryFactory;
import org.alfresco.repo.discussion.cannedqueries.NodeWithChildrenEntity;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.node.getchildren.GetChildrenAuditableCannedQuery;
import org.alfresco.repo.node.getchildren.GetChildrenAuditableCannedQueryFactory;
import org.alfresco.repo.node.getchildren.GetChildrenWithTargetAssocsAuditableCannedQuery;
import org.alfresco.repo.node.getchildren.GetChildrenWithTargetAssocsAuditableCannedQueryFactory;
import org.alfresco.repo.query.NodeBackedEntity;
import org.alfresco.repo.query.NodeWithTargetsEntity;
import org.alfresco.repo.query.NodeWithTargetsEntity.TargetAndTypeId;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteServiceImpl;
import org.alfresco.service.cmr.discussion.DiscussionService;
import org.alfresco.service.cmr.discussion.PostInfo;
import org.alfresco.service.cmr.discussion.PostWithReplies;
import org.alfresco.service.cmr.discussion.TopicInfo;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;
import org.alfresco.util.registry.NamedObjectRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Nick Burch (based on existing webscript controllers in the REST API)
 * @since 4.0
 */
public class DiscussionServiceImpl implements DiscussionService
{
    public static final String DISCUSSION_COMPONENT = "discussions";
   
    protected static final String CANNED_QUERY_GET_CHILDREN = "discussionGetChildrenCannedQueryFactory";
    protected static final String CANNED_QUERY_GET_CHILDREN_TARGETS = "discussionGetChildrenWithTargetAssocsAuditableCannedQueryFactory";
    protected static final String CANNED_QUERY_GET_TOPICS_WITH_POSTS = "discussionGetDiscussionTopcisWithPostsCannedQueryFactory";
    protected static final int MAX_REPLIES_FETCH_SIZE = 1000; 
    
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(DiscussionServiceImpl.class);
    
    private NodeDAO nodeDAO;
    private NodeService nodeService;
    private SiteService siteService;
    private SearchService searchService;
    private ContentService contentService;
    private TaggingService taggingService;
    private NamespaceService namespaceService;
    private FileFolderService fileFolderService;
    private TransactionService transactionService;
    private NamedObjectRegistry<CannedQueryFactory<? extends Object>> cannedQueryRegistry;
    
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    public void setTaggingService(TaggingService taggingService)
    {
        this.taggingService = taggingService;
    }
    
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    /**
     * Set the registry of {@link CannedQueryFactory canned queries}
     */
    public void setCannedQueryRegistry(NamedObjectRegistry<CannedQueryFactory<? extends Object>> cannedQueryRegistry)
    {
        this.cannedQueryRegistry = cannedQueryRegistry;
    }
    
    /**
     * Fetches the Discussions Container on a site, creating as required if requested.
     */
    protected NodeRef getSiteDiscussionsContainer(final String siteShortName, boolean create)
    {
       return SiteServiceImpl.getSiteContainer(
             siteShortName, DISCUSSION_COMPONENT, create, 
             siteService, transactionService, taggingService);
    }
    
    private String generateName()
    {
       // Generate a unique name
       // (Should be unique, but will retry for a new one if not)
       String name = "post-" +(new Date()).getTime() + "_" + 
                     Math.round(Math.random()*10000);
       return name;
    }
    
    private TopicInfo buildTopic(NodeRef nodeRef, NodeRef container, String name)
    {
       TopicInfoImpl topic = new TopicInfoImpl(nodeRef, container, name);
       
       // Grab all the properties, we need the bulk of them anyway
       Map<QName,Serializable> props = nodeService.getProperties(nodeRef);
       
       // Start with the auditable properties
       topic.setCreator((String)props.get(ContentModel.PROP_CREATOR));
       topic.setModifier((String)props.get(ContentModel.PROP_MODIFIER));
       topic.setCreatedAt((Date)props.get(ContentModel.PROP_CREATED));
       topic.setModifiedAt((Date)props.get(ContentModel.PROP_MODIFIED));
       
       // Now do the discussion ones
       // Title is special - in older cases from share it was set on the
       //  First Post but not on the Topic
       String title = (String)props.get(ContentModel.PROP_TITLE);
       if (title == null)
       {
          // Try the first child
          PostInfo primaryPost = getPrimaryPost(topic);
          if (primaryPost != null)
          {
             title = primaryPost.getTitle();
          }
       }
       topic.setTitle(title);
       
       // Finally tags
       topic.setTags(taggingService.getTags(nodeRef));
       
       // All done
       return topic;
    }
    
    private PostInfo buildPost(NodeRef nodeRef, TopicInfo topic, String name, String preLoadedContents)
    {
       PostInfoImpl post = new PostInfoImpl(nodeRef, name, topic);
       
       // Grab all the properties, we need the bulk of them anyway
       Map<QName,Serializable> props = nodeService.getProperties(nodeRef);
       
       // Start with the auditable properties
       post.setCreator((String)props.get(ContentModel.PROP_CREATOR));
       post.setModifier((String)props.get(ContentModel.PROP_MODIFIER));
       post.setCreatedAt((Date)props.get(ContentModel.PROP_CREATED));
       post.setModifiedAt((Date)props.get(ContentModel.PROP_MODIFIED));
       post.setUpdatedAt((Date)props.get(ContentModel.PROP_UPDATED));
       
       // Now do the discussion ones
       post.setTitle((String)props.get(ContentModel.PROP_TITLE));
       
       // Finally, do the content
       String contents = preLoadedContents;
       if (contents == null)
       {
          ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
          if (reader != null)
          {
             contents = reader.getContentString();
          }
       }
       post.setContents(contents);
       
       // All done
       return post;
    }
    
    
    @Override
    public TopicInfo getTopic(String siteShortName, String topicName) 
    {
       NodeRef container = getSiteDiscussionsContainer(siteShortName, false);
       if (container == null)
       {
          // No discussions
          return null;
       }
       
       // We can now fetch by parent nodeRef
       return getTopic(container, topicName);
    }
    
    @Override
    public TopicInfo getTopic(NodeRef parentNodeRef, String topicName) 
    {
       NodeRef topic = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, topicName);
       if (topic != null)
       {
          return buildTopic(topic, parentNodeRef, topicName);
       }
       return null;
    }

    @Override
    public PostInfo getPost(TopicInfo topic, String postName) 
    {
       // Sanity check what we were given
       if (topic.getNodeRef() == null)
       {
          throw new IllegalArgumentException("Can't get posts for a topic that was never persisted!");
       }
       
       // Fetch
       NodeRef post = nodeService.getChildByName(topic.getNodeRef(), ContentModel.ASSOC_CONTAINS, postName);
       if (post != null)
       {
          return buildPost(post, topic, postName, null);
       }
       return null;
    }

    
    @Override
    public TopicInfo createTopic(final String siteShortName, final String title) 
    {
       // Grab the location to store in
       NodeRef container = getSiteDiscussionsContainer(siteShortName, true);
       
       // Add by Parent NodeRef
       return createTopic(container, title);
    }

    @Override
    public TopicInfo createTopic(final NodeRef parentNodeRef, final String title) 
    {
       // Build the name
       String name = generateName();
       
       // Get the properties for the node
       Map<QName, Serializable> props = new HashMap<QName, Serializable>();
       props.put(ContentModel.PROP_NAME,  name);
       props.put(ContentModel.PROP_TITLE, title);
       
       // Build the node
       NodeRef nodeRef = nodeService.createNode(
             parentNodeRef,
             ContentModel.ASSOC_CONTAINS,
             QName.createQName(name),
             ForumModel.TYPE_TOPIC,
             props
       ).getChildRef();
       
       // We require that the parent container of the topic
       //  is always a tag scope. This should always be the case
       //  for site containers, but perhaps not others
       if (! taggingService.isTagScope(parentNodeRef))
       {
          AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
             public Void doWork() throws Exception
             {
                // Add the tag scope aspect
                taggingService.addTagScope(parentNodeRef);
                return null;
             }
          }, AuthenticationUtil.getSystemUserName());
       }
       
       // Generate the wrapping object for it
       // Build it that way, so creator and created date come through
       return buildTopic(nodeRef, parentNodeRef, name);
    }

    @Override
    public PostInfo createPost(TopicInfo topic, String contents) 
    {
       // Sanity check what we were given
       if (topic.getNodeRef() == null)
       {
          throw new IllegalArgumentException("Can't create posts for a topic that was never persisted!");
       }
       
       // Are we going to be the primary post?
       boolean isPrimary = false;
       if (getPrimaryPost(topic) == null)
       {
          isPrimary = true;
       }
       
       // Decide on the name. If this is the first post in a topic,
       //  it should share the topic's name, otherwise needs a new one
       String name = generateName();
       if (isPrimary)
       {
          name = topic.getSystemName();
       }
       
       // Create the properties for the node
       Map<QName, Serializable> props = new HashMap<QName, Serializable>();
       props.put(ContentModel.PROP_NAME,  name);
       
       // TODO Remove this shortly, when the webscripts have been
       //  fixed up to avoid their current broken-ness
       props.put(ContentModel.PROP_PUBLISHED, new Date());
       
       // Do we want a title? By default, primary posts share a title
       //  with the topic, but replies are title-free
       if (isPrimary)
       {
          props.put(ContentModel.PROP_TITLE, topic.getTitle());
       }
       
       // Build the node
       NodeRef nodeRef = nodeService.createNode(
             topic.getNodeRef(),
             ContentModel.ASSOC_CONTAINS,
             QName.createQName(name),
             ForumModel.TYPE_POST,
             props
       ).getChildRef();
       
       // Store the content
       ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
       writer.setEncoding("UTF-8");
       writer.putContent(contents);
             
       // Generate the wrapping object for it
       // Build it that way, so creator and created date come through
       return buildPost(nodeRef, topic, name, contents);
    }
    
    @Override
    public PostInfo createReply(PostInfo parentPost, String contents)
    {
       // Sanity check what we were given
       if (parentPost.getNodeRef() == null)
       {
          throw new IllegalArgumentException("Can't reply to a post that was never persisted");
       }
       if (parentPost.getTopic() == null)
       {
          throw new IllegalArgumentException("Can't reply to a post with no attached topic");
       }
       
       // Have the post created
       PostInfo reply = createPost(parentPost.getTopic(), contents);
       
       // Now make it a reply
       nodeService.createAssociation(
             reply.getNodeRef(), parentPost.getNodeRef(), ContentModel.ASSOC_REFERENCES);
       
       // All done
       return reply;
    }

    
    @Override
    public TopicInfo updateTopic(TopicInfo topic) 
    {
       // Sanity check what we were given
       if (topic.getNodeRef() == null)
       {
          throw new IllegalArgumentException("Can't update a topic that was never persisted, call create instead");
       }
       
       // Update the properties
       NodeRef nodeRef = topic.getNodeRef();
       nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, topic.getTitle());
       
       // Now do the tags
       taggingService.setTags(nodeRef, topic.getTags());
       
       // All done
       return topic;
    }

    @Override
    public PostInfo updatePost(PostInfo post) 
    {
       // Sanity check what we were given
       if (post.getNodeRef() == null)
       {
          throw new IllegalArgumentException("Can't update a post that was never persisted, call create instead");
       }
       
       // Update the properties
       NodeRef nodeRef = post.getNodeRef();
       nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, post.getTitle());
       
       // Change the content
       ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
       writer.setEncoding("UTF-8");
       writer.putContent(post.getContents());
       
       // Mark it as having been updated
       Date updatedAt = new Date();
       nodeService.setProperty(nodeRef, ContentModel.PROP_UPDATED, updatedAt);
       if (post instanceof PostInfoImpl)
       {
          ((PostInfoImpl)post).setUpdatedAt(updatedAt);
          ((PostInfoImpl)post).setModifiedAt(updatedAt);
       }
       else
       {
          // Re-create to get the updated date
          post = buildPost(nodeRef, post.getTopic(), post.getSystemName(), post.getContents()); 
       }
       
       // All done
       return post;
    }

    
    @Override
    public void deleteTopic(TopicInfo topic) 
    {
       if (topic.getNodeRef() == null)
       {
          throw new IllegalArgumentException("Can't delete a topic that was never persisted");
       }

       nodeService.deleteNode(topic.getNodeRef());
    }

    @Override
    public void deletePost(PostInfo post) 
    {
       if (post.getNodeRef() == null)
       {
          throw new IllegalArgumentException("Can't delete a post that was never persisted");
       }

       nodeService.deleteNode(post.getNodeRef());
    }
    
    @Override
    public Pair<TopicInfo,PostInfo> getForNodeRef(NodeRef nodeRef)
    {
       QName type = nodeService.getType(nodeRef);
       TopicInfo topic = null;
       PostInfo post = null;
       
       if (type.equals(ForumModel.TYPE_TOPIC))
       {
          ChildAssociationRef ref = nodeService.getPrimaryParent(nodeRef);
          String topicName = ref.getQName().getLocalName();
          topic = getTopic(ref.getParentRef(), topicName);
       }
       else if (type.equals(ForumModel.TYPE_POST))
       {
          ChildAssociationRef toTopic = nodeService.getPrimaryParent(nodeRef);
          String postName = toTopic.getQName().getLocalName();
          NodeRef topicNodeRef = toTopic.getParentRef();
          
          ChildAssociationRef toParent = nodeService.getPrimaryParent(topicNodeRef);
          String topicName = toParent.getQName().getLocalName();
          topic = getTopic(toParent.getParentRef(), topicName);
          post = getPost(topic, postName);
       }
       else
       {
          logger.debug("Invalid type " + type + " found");
          return null;
       }
       
       // Return what we found
       return new Pair<TopicInfo, PostInfo>(topic, post);
    }

    @Override
    public PostInfo getPrimaryPost(TopicInfo topic) 
    {
       // First up, see if there is a post with the same name as the topic
       // (That's the normal Share case)
       PostInfo post = getPost(topic, topic.getSystemName());
       if (post != null)
       {
          return post;
       }

       // Cater for the explorer case, we want the first child
       List<ChildAssociationRef> children = nodeService.getChildAssocs(topic.getNodeRef());
       if (children.size() == 0)
       {
          // No child posts yet
          return null;
       }

       // We want the first one in the list
       NodeRef postNodeRef = children.get(0).getChildRef();
       String postName = children.get(0).getQName().getLocalName();
       return buildPost(postNodeRef, topic, postName, null);
    }
    
    @Override
    public PostInfo getMostRecentPost(TopicInfo topic)
    {
       // Do a listing at the Node level, ordered by created date
       //  to get the most recent nodes
       PagingRequest paging = new PagingRequest(0, 1);
       CannedQueryResults<NodeBackedEntity> results = 
           listEntries(topic.getNodeRef(), ForumModel.TYPE_POST, null, null, null, false, paging);
          
       // Bail if the topic lacks posts
       if (results.getPage().size() == 0)
       {
          // No posts in the topic
          return null;
       }
       
       // Grab the newest node
       NodeBackedEntity node = results.getPage().get(0);
       
       // Wrap and return
       return buildPost(node.getNodeRef(), topic, node.getName(), null);
    }

   
   @Override
   public PagingResults<TopicInfo> listTopics(String siteShortName,
         boolean sortAscending, PagingRequest paging) 
   {
      NodeRef container = getSiteDiscussionsContainer(siteShortName, false);
      if (container == null)
      {
         // No topics
         return new EmptyPagingResults<TopicInfo>();
      }
      
      // We can now fetch by parent nodeRef
      return listTopics(container, sortAscending, paging);
   }

   @Override
   public PagingResults<TopicInfo> listTopics(NodeRef nodeRef,
         boolean sortAscending, PagingRequest paging) 
   {
      // Do the listing, oldest first
      CannedQueryResults<NodeBackedEntity> nodes = 
         listEntries(nodeRef, ForumModel.TYPE_TOPIC, null, null, null, sortAscending, paging);
      
      // Wrap and return
      return wrap(nodes, nodeRef);
   }
   
   @Override
   public PagingResults<TopicInfo> listTopics(String siteShortName,
         String username, boolean sortAscending, PagingRequest paging) 
   {
      NodeRef container = getSiteDiscussionsContainer(siteShortName, false);
      if (container == null)
      {
         // No topics
         return new EmptyPagingResults<TopicInfo>();
      }
      
      // We can now fetch by parent nodeRef
      return listTopics(container, username, sortAscending, paging);
   }

   @Override
   public PagingResults<TopicInfo> listTopics(NodeRef nodeRef,
         String username, boolean sortAscending, PagingRequest paging) 
   {
      // Do the listing, oldest first
      CannedQueryResults<NodeBackedEntity> nodes = 
         listEntries(nodeRef, ForumModel.TYPE_TOPIC, username, null, null, sortAscending, paging);
      
      // Wrap and return
      return wrap(nodes, nodeRef);
   }
   
   @Override
   public PagingResults<TopicInfo> listTopics(String siteShortName,
         Date from, Date to, boolean sortAscending, PagingRequest paging) 
   {
      NodeRef container = getSiteDiscussionsContainer(siteShortName, false);
      if (container == null)
      {
         // No topics
         return new EmptyPagingResults<TopicInfo>();
      }
      
      // We can now fetch by parent nodeRef
      return listTopics(container, from, to, sortAscending, paging);
   }

   @Override
   public PagingResults<TopicInfo> listTopics(NodeRef nodeRef,
         Date from, Date to, boolean sortAscending, PagingRequest paging) 
   {
      // Do the listing, with the sort order as requested
      CannedQueryResults<NodeBackedEntity> nodes = 
         listEntries(nodeRef, ForumModel.TYPE_TOPIC, null, from, to, sortAscending, paging);
      
      // Wrap and return
      return wrap(nodes, nodeRef);
   }

   
   @Override
   public PagingResults<Pair<TopicInfo, Integer>> listHotTopics(
         String siteShortName, Date since, PagingRequest paging) 
   {
      NodeRef container = getSiteDiscussionsContainer(siteShortName, false);
      if (container == null)
      {
         // No topics
         return new EmptyPagingResults<Pair<TopicInfo,Integer>>();
      }
      
      // We can now fetch by parent nodeRef
      return listHotTopics(container, since, paging);
   }
   
   @Override
   public PagingResults<Pair<TopicInfo, Integer>> listHotTopics(
         NodeRef nodeRef, Date since, PagingRequest paging) 
   {
      // Do the query
      GetDiscussionTopcisWithPostsCannedQueryFactory getCQFactory = 
                  (GetDiscussionTopcisWithPostsCannedQueryFactory)cannedQueryRegistry.getNamedObject(CANNED_QUERY_GET_TOPICS_WITH_POSTS);
      GetDiscussionTopcisWithPostsCannedQuery cq = (GetDiscussionTopcisWithPostsCannedQuery)getCQFactory.getCannedQuery(
            nodeRef, null, since, true, null, paging);
      
      // Execute the canned query
      CannedQueryResults<NodeWithChildrenEntity> results = cq.execute();
      
      // Wrap and return
      return wrapWithCount(results, nodeRef);
   }

   @Override
   public PagingResults<TopicInfo> findTopics(String siteShortName,
         String username, String tag, boolean sortAscending, PagingRequest paging) 
   {
      NodeRef container = getSiteDiscussionsContainer(siteShortName, false);
      if (container == null)
      {
         // No topics
         return new EmptyPagingResults<TopicInfo>();
      }
      
      // We can now search by parent nodeRef
      return findTopics(container, username, tag, sortAscending, paging);
   }

   @Override
   public PagingResults<TopicInfo> findTopics(NodeRef nodeRef,
         String username, String tag, boolean sortAscending, PagingRequest paging) 
   {
      // Build the query
      StringBuilder luceneQuery = new StringBuilder();
      luceneQuery.append(" +TYPE:\"" + ForumModel.TYPE_TOPIC + "\"");
      luceneQuery.append(" +PATH:\"" + nodeService.getPath(nodeRef).toPrefixString(namespaceService) + "/*\"");

      if (username != null)
      {
         luceneQuery.append(" +@cm\\:creator:\"" + username + "\"");
      }
      if (tag != null)
      {
         luceneQuery.append(" +PATH:\"/cm:taggable/cm:" + ISO9075.encode(tag) + "/member\"" );
      }
      
      String sortOn = "@{http://www.alfresco.org/model/content/1.0}created";

      // Query
      SearchParameters sp = new SearchParameters();
      sp.addStore(nodeRef.getStoreRef());
      sp.setLanguage(SearchService.LANGUAGE_LUCENE);
      sp.setQuery(luceneQuery.toString());
      sp.addSort(sortOn, sortAscending);
      if (paging.getMaxItems() > 0)
      {
          sp.setLimit(paging.getMaxItems());
          sp.setLimitBy(LimitBy.FINAL_SIZE);
      }
      if (paging.getSkipCount() > 0)
      {
          sp.setSkipCount(paging.getSkipCount());
      }
      
      
      // Build the results
      PagingResults<TopicInfo> pagedResults = new EmptyPagingResults<TopicInfo>();
      ResultSet results = null;
      
      try 
      {
         results = searchService.query(sp);
         pagedResults = wrap(results, nodeRef);
      }
      finally
      {
         if (results != null)
         {
            results.close();
         }
      }
      
      return pagedResults;
   }

   
   @Override
   public PagingResults<PostInfo> listPosts(TopicInfo topic, PagingRequest paging)
   {
      // Do the listing, oldest first
      CannedQueryResults<NodeBackedEntity> nodes = 
         listEntries(topic.getNodeRef(), ForumModel.TYPE_POST, null, null, null, true, paging);
      
      // Wrap and return
      return wrap(nodes, topic);
   }


   @Override
   public PostWithReplies listPostReplies(TopicInfo topic, int levels)
   {
       PostInfo primaryPost = getPrimaryPost(topic);
       if (primaryPost == null)
       {
          return null;
       }
       return listPostReplies(primaryPost, levels);
   }
   
   @Override
   public PostWithReplies listPostReplies(PostInfo primaryPost, int levels)
   {
      // Grab the factory
      GetChildrenWithTargetAssocsAuditableCannedQueryFactory cqFactory = 
                  (GetChildrenWithTargetAssocsAuditableCannedQueryFactory)cannedQueryRegistry.getNamedObject(CANNED_QUERY_GET_CHILDREN_TARGETS);
      
      // Sort by date
      CannedQuerySortDetails sorting = cqFactory.createDateAscendingCQSortDetails();
      
      // Run the canned query
      GetChildrenWithTargetAssocsAuditableCannedQuery cq = (GetChildrenWithTargetAssocsAuditableCannedQuery)cqFactory.getCannedQuery(
            primaryPost.getTopic().getNodeRef(), ForumModel.TYPE_POST,
            ContentModel.ASSOC_REFERENCES, sorting, new PagingRequest(MAX_REPLIES_FETCH_SIZE));
      
      // Execute the canned query
      CannedQueryResults<NodeWithTargetsEntity> results = cq.execute();
      
      // Prepare to invert
      Map<Long,NodeRef> idToNodeRef = new HashMap<Long, NodeRef>();
      for(NodeWithTargetsEntity e : results.getPage())
      {
         idToNodeRef.put(e.getId(), e.getNodeRef());
      }
      
      Map<NodeRef,List<NodeWithTargetsEntity>> idToReplies = new HashMap<NodeRef, List<NodeWithTargetsEntity>>();
      for (NodeWithTargetsEntity e : results.getPage())
      {
         for (TargetAndTypeId idP : e.getTargetIds())
         {
            Long id = idP.getTargetId();
            NodeRef nodeRef = idToNodeRef.get(id);
            if (nodeRef == null)
            {
               // References a node outside of this topic
               continue;
            }
            if (id.equals(e.getId()))
            {
               // Self reference
               continue;
            }
            if (! idToReplies.containsKey(nodeRef))
            {
               idToReplies.put(nodeRef, new ArrayList<NodeWithTargetsEntity>());
            }
            idToReplies.get(nodeRef).add(e);
         }
      }
      
      // Grab the list of NodeRefs to pre-load, and pre-load them
      List<NodeRef> preLoad = new ArrayList<NodeRef>();
      calculateRepliesPreLoad(primaryPost.getNodeRef(), preLoad, idToReplies, levels);
      nodeDAO.cacheNodes(preLoad);
      
      // Wrap
      return wrap(primaryPost, idToReplies, levels);
   }
   
   private void calculateRepliesPreLoad(NodeRef nodeRef, List<NodeRef> preLoad, 
         Map<NodeRef,List<NodeWithTargetsEntity>> idToReplies, int levels)
   {
      preLoad.add(nodeRef);
      if (levels > 0)
      {
         List<NodeWithTargetsEntity> replies = idToReplies.get(nodeRef);
         if (replies != null && replies.size() > 0)
         {
            for (NodeWithTargetsEntity entity : replies)
            {
               calculateRepliesPreLoad(entity.getNodeRef(), preLoad, idToReplies, levels-1);
            }
         }
      }
   }
   
   private PostWithReplies wrap(PostInfo post, Map<NodeRef,List<NodeWithTargetsEntity>> idToReplies, int levels)
   {
      List<PostWithReplies> replies = new ArrayList<PostWithReplies>();
      if (levels > 0)
      {
         List<NodeWithTargetsEntity> replyEntities = idToReplies.get(post.getNodeRef());
         if (replyEntities != null && replyEntities.size() > 0)
         {
            for (NodeWithTargetsEntity entity : replyEntities)
            {
               PostInfo replyPost = buildPost(entity.getNodeRef(), post.getTopic(), entity.getName(), null);
               replies.add(wrap(replyPost, idToReplies, levels-1));
            }
         }
      }
      return new PostWithReplies(post, replies);
   }


   /**
    * Finds nodes in the specified parent container, with the given
    *  type, optionally filtered by creator
    */
   private CannedQueryResults<NodeBackedEntity> listEntries(NodeRef parent, 
         QName nodeType, String creatorUsername, Date from, Date to, 
         boolean oldestFirst, PagingRequest paging) 
   {
      // The Canned Query system doesn't allow for zero sized pages
      // If they asked for that (bits of share sometimes do), bail out now
      if (paging != null && paging.getMaxItems() == 0)
      {
         return new EmptyCannedQueryResults<NodeBackedEntity>(null);
      }
      
      // Grab the factory
      GetChildrenAuditableCannedQueryFactory getChildrenCannedQueryFactory = (GetChildrenAuditableCannedQueryFactory)cannedQueryRegistry.getNamedObject(CANNED_QUERY_GET_CHILDREN);
      
      // Do the sorting, newest first or last by created date (as requested)
      CannedQuerySortDetails sorting = null;
      if (oldestFirst)
      {
         sorting = getChildrenCannedQueryFactory.createDateAscendingCQSortDetails();
      }
      else
      {
         sorting = getChildrenCannedQueryFactory.createDateDescendingCQSortDetails();
      }
      
      // Run the canned query
      GetChildrenAuditableCannedQuery cq = (GetChildrenAuditableCannedQuery)getChildrenCannedQueryFactory.getCannedQuery(
            parent, nodeType, creatorUsername, from, to, null,
            null, null, sorting, paging);
      
      // Execute the canned query
      CannedQueryResults<NodeBackedEntity> results = cq.execute();
      
      // Return for wrapping
      return results;
   }
   
   /**
    * Our class to wrap up search results as {@link TopicInfo} instances
    */
   private PagingResults<TopicInfo> wrap(final ResultSet finalLuceneResults, final NodeRef container)
   {
      final List<TopicInfo> topics = new ArrayList<TopicInfo>();
      for (ResultSetRow row : finalLuceneResults)
      {
         TopicInfo topic = buildTopic(
               row.getNodeRef(), container, row.getQName().getLocalName());
         topics.add(topic);
      }
      
      // Wrap
      return new PagingResults<TopicInfo>() 
      {
         @Override
         public boolean hasMoreItems() 
         {
            return finalLuceneResults.hasMore();
         }

         @Override
         public Pair<Integer, Integer> getTotalResultCount() 
         {
            int skipCount = 0;
            int itemsRemainingAfterThisPage = 0;
            try
            {
               skipCount = finalLuceneResults.getStart();
               itemsRemainingAfterThisPage = finalLuceneResults.length();
            }
            catch(UnsupportedOperationException e) {}
            
            final int totalItemsInUnpagedResultSet = skipCount + itemsRemainingAfterThisPage;
            return new Pair<Integer, Integer>(totalItemsInUnpagedResultSet, totalItemsInUnpagedResultSet);
         }

         @Override
         public List<TopicInfo> getPage() 
         {
            return topics;
         }

         @Override
         public String getQueryExecutionId() 
         {
            return null;
         }
     };
   }
   
   /**
    * Our class to wrap up paged results of NodeBackedEntities as
    *  {@link TopicInfo} instances
    */
   private PagingResults<TopicInfo> wrap(final PagingResults<NodeBackedEntity> results, final NodeRef container)
   {
      // Pre-load the nodes before we create them
      List<Long> ids = new ArrayList<Long>();
      for (NodeBackedEntity node : results.getPage())
      {
         ids.add(node.getId());
      }
      nodeDAO.cacheNodesById(ids);
      
      // Wrap
      return new PagingResults<TopicInfo>()
      {
          @Override
          public String getQueryExecutionId()
          {
              return results.getQueryExecutionId();
          }
          
          @Override
          public List<TopicInfo> getPage()
          {
              List<TopicInfo> topics = new ArrayList<TopicInfo>();
              for (NodeBackedEntity node : results.getPage())
              {
                 NodeRef nodeRef = node.getNodeRef();
                 String name = node.getName();
                 topics.add(buildTopic(nodeRef, container, name));
              }
              return topics;
          }
          
          @Override
          public boolean hasMoreItems()
          {
              return results.hasMoreItems();
          }
          
          @Override
          public Pair<Integer, Integer> getTotalResultCount()
          {
              return results.getTotalResultCount();
          }
      };
   }
   
   /**
    * Our class to wrap up paged results of NodeBackedEntities as
    *  {@link PostInfo} instances
    */
   private PagingResults<PostInfo> wrap(final PagingResults<NodeBackedEntity> results, final TopicInfo topic)
   {
      // Pre-load the nodes before we create them
      List<Long> ids = new ArrayList<Long>();
      for (NodeBackedEntity node : results.getPage())
      {
         ids.add(node.getId());
      }
      nodeDAO.cacheNodesById(ids);
      
      // Wrap
      return new PagingResults<PostInfo>()
      {
          @Override
          public String getQueryExecutionId()
          {
              return results.getQueryExecutionId();
          }
          
          @Override
          public List<PostInfo> getPage()
          {
              List<PostInfo> posts = new ArrayList<PostInfo>();
              for (NodeBackedEntity node : results.getPage())
              {
                 NodeRef nodeRef = node.getNodeRef();
                 String name = node.getName();
                 posts.add(buildPost(nodeRef, topic, name, null));
              }
              return posts;
          }
          
          @Override
          public boolean hasMoreItems()
          {
              return results.hasMoreItems();
          }
          
          @Override
          public Pair<Integer, Integer> getTotalResultCount()
          {
              return results.getTotalResultCount();
          }
      };
   }
   
   /**
    * Our class to wrap up paged results of NodeWithChildrenEntity as
    *  {@link TopicInfo} instances
    */
   private PagingResults<Pair<TopicInfo,Integer>> wrapWithCount(final PagingResults<NodeWithChildrenEntity> results, final NodeRef container)
   {
      // Pre-load the nodes before we create them
      List<Long> ids = new ArrayList<Long>();
      for (NodeBackedEntity node : results.getPage())
      {
         ids.add(node.getId());
      }
      nodeDAO.cacheNodesById(ids);
      
      // Wrap
      return new PagingResults<Pair<TopicInfo,Integer>>()
      {
          @Override
          public String getQueryExecutionId()
          {
              return results.getQueryExecutionId();
          }
          
          @Override
          public List<Pair<TopicInfo,Integer>> getPage()
          {
              List<Pair<TopicInfo,Integer>> topics = new ArrayList<Pair<TopicInfo,Integer>>();
              for (NodeWithChildrenEntity node : results.getPage())
              {
                 NodeRef nodeRef = node.getNodeRef();
                 String name = node.getName();
                 int count = node.getChildren().size();
                 
                 TopicInfo topic = buildTopic(nodeRef, container, name);
                 topics.add(new Pair<TopicInfo,Integer>(topic, count));
              }
              return topics;
          }
          
          @Override
          public boolean hasMoreItems()
          {
              return results.hasMoreItems();
          }
          
          @Override
          public Pair<Integer, Integer> getTotalResultCount()
          {
              return results.getTotalResultCount();
          }
      };
   }
}
