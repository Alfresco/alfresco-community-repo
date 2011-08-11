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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.EmptyPagingResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.node.getchildren.GetChildrenAuditableCannedQuery;
import org.alfresco.repo.node.getchildren.GetChildrenAuditableCannedQueryFactory;
import org.alfresco.repo.query.NodeBackedEntity;
import org.alfresco.repo.site.SiteServiceImpl;
import org.alfresco.service.cmr.discussion.DiscussionService;
import org.alfresco.service.cmr.discussion.PostInfo;
import org.alfresco.service.cmr.discussion.TopicInfo;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.cmr.wiki.WikiPageInfo;
import org.alfresco.service.cmr.wiki.WikiService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
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
    public static final String DISCUSSION_COMPONENT = "discussion";
   
    // TODO Correct CQ
    protected static final String CANNED_QUERY_GET_CHILDREN = "wikiGetChildrenCannedQueryFactory";
    
    /**
     * The logger
     */
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(DiscussionServiceImpl.class);
    
    private NodeService nodeService;
    private SiteService siteService;
    private ContentService contentService;
    private TaggingService taggingService;
    private FileFolderService fileFolderService;
    private TransactionService transactionService;
    private NamedObjectRegistry<CannedQueryFactory<? extends Object>> cannedQueryRegistry;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    public void setTaggingService(TaggingService taggingService)
    {
        this.taggingService = taggingService;
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
    protected NodeRef getSiteWikiContainer(final String siteShortName, boolean create)
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
       if(title == null)
       {
          // Try the first child
          PostInfo primaryPost = getPrimaryPost(topic);
          if(primaryPost != null)
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
       
       // Now do the discussion ones
       post.setTitle((String)props.get(ContentModel.PROP_TITLE));
       
       // Finally, do the content
       String contents = preLoadedContents;
       if(contents == null)
       {
          ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
          if(reader != null)
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
       NodeRef container = getSiteWikiContainer(siteShortName, false);
       if(container == null)
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
       if(topic != null)
       {
          return buildTopic(topic, parentNodeRef, topicName);
       }
       return null;
    }

    @Override
    public PostInfo getPost(TopicInfo topic, String postName) 
    {
       // Sanity check what we were given
       if(topic.getNodeRef() == null)
       {
          throw new IllegalArgumentException("Can't get posts for a topic that was never persisted!");
       }
       
       // Fetch
       NodeRef post = nodeService.getChildByName(topic.getNodeRef(), ContentModel.ASSOC_CONTAINS, postName);
       if(post != null)
       {
          return buildPost(post, topic, postName, null);
       }
       return null;
    }

    
    @Override
    public TopicInfo createTopic(String siteShortName, String title) 
    {
       // Grab the location to store in
       NodeRef container = getSiteWikiContainer(siteShortName, true);
       
       // Add by Parent NodeRef
       return createTopic(container, title);
    }

    @Override
    public TopicInfo createTopic(NodeRef parentNodeRef, String title) 
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
       
       // Generate the wrapping object for it
       // Build it that way, so creator and created date come through
       return buildTopic(nodeRef, parentNodeRef, name);
    }

    @Override
    public PostInfo createPost(TopicInfo topic, String contents) 
    {
       // Sanity check what we were given
       if(topic.getNodeRef() == null)
       {
          throw new IllegalArgumentException("Can't create posts for a topic that was never persisted!");
       }
       
       // Decide on the name. If this is the first post in a topic,
       //  it should share the topic's name, otherwise needs a new one
       String name = generateName();
       if(getPrimaryPost(topic) == null)
       {
          name = topic.getSystemName();
       }
       
       // Get the properties for the node
       Map<QName, Serializable> props = new HashMap<QName, Serializable>();
       props.put(ContentModel.PROP_NAME,  name);
       props.put(ContentModel.PROP_TITLE, topic.getTitle());
       
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
    public TopicInfo updateTopic(TopicInfo topic) 
    {
       // Sanity check what we were given
       if(topic.getNodeRef() == null)
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
    public PostInfo updatePost(PostInfo post) {
       // Sanity check what we were given
       if(post.getNodeRef() == null)
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
       
       // All done
       return post;
    }

    
    @Override
    public void deleteTopic(TopicInfo topic) {
       if(topic.getNodeRef() == null)
       {
          throw new IllegalArgumentException("Can't delete a topic that was never persisted");
       }

       nodeService.deleteNode(topic.getNodeRef());
    }

    @Override
    public void deletePost(PostInfo post) {
       if(post.getNodeRef() == null)
       {
          throw new IllegalArgumentException("Can't delete a post that was never persisted");
       }

       nodeService.deleteNode(post.getNodeRef());
    }

    
    public PagingResults<WikiPageInfo> listWikiPages(String siteShortName, String username, 
          Date createdFrom, Date createdTo, Date modifiedFrom, Date modifiedTo, PagingRequest paging) 
    {
       NodeRef container = getSiteWikiContainer(siteShortName, false);
       if(container == null)
       {
          // No events
          return new EmptyPagingResults<WikiPageInfo>();
       }
       
       // Grab the factory
       GetChildrenAuditableCannedQueryFactory getChildrenCannedQueryFactory = (GetChildrenAuditableCannedQueryFactory)cannedQueryRegistry.getNamedObject(CANNED_QUERY_GET_CHILDREN);
       
       // Do the sorting, newest first by created date
       CannedQuerySortDetails sorting = getChildrenCannedQueryFactory.createDateDescendingCQSortDetails();
       
       // Run the canned query
       GetChildrenAuditableCannedQuery cq = (GetChildrenAuditableCannedQuery)getChildrenCannedQueryFactory.getCannedQuery(
             container, ContentModel.TYPE_CONTENT, username, createdFrom, createdTo, null,
             modifiedFrom, modifiedTo, sorting, paging);
       
       // Execute the canned query
       CannedQueryResults<NodeBackedEntity> results = cq.execute();
       
       // Convert to Link objects
       return wrap(results, container);
    }
    
    /**
     * Our class to wrap up paged results of NodeBackedEntities as
     *  WikiPageInfo instances
     */
    private PagingResults<WikiPageInfo> wrap(final PagingResults<NodeBackedEntity> results, final NodeRef container)
    {
       return new PagingResults<WikiPageInfo>()
       {
           @Override
           public String getQueryExecutionId()
           {
               return results.getQueryExecutionId();
           }
           @Override
           public List<WikiPageInfo> getPage()
           {
               List<WikiPageInfo> pages = new ArrayList<WikiPageInfo>();
               for(NodeBackedEntity node : results.getPage())
               {
                  NodeRef nodeRef = node.getNodeRef();
                  String name = node.getName();
                  //pages.add(buildPage(nodeRef, container, name, null));
               }
               return pages;
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

   @Override
   public PostInfo getPrimaryPost(TopicInfo topic) {
      // First up, see if there is a post with the same name as the topic
      // (That's the normal Share case)
      PostInfo post = getPost(topic, topic.getSystemName());
      if(post != null)
      {
         return null;
      }
      
      // Cater for the explorer case, we want the first child
      List<ChildAssociationRef> children = nodeService.getChildAssocs(topic.getNodeRef());
      if(children.size() == 0)
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
   public PagingResults<PostInfo> listPostReplies(PostInfo primaryPost,
         int levels, PagingRequest paging) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public PagingResults<PostInfo> listPostReplies(TopicInfo forum, int levels,
         PagingRequest paging) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public PagingResults<PostInfo> listPosts(NodeRef nodeRef,
         PagingRequest paging) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public PagingResults<PostInfo> listPosts(String siteShortName,
         PagingRequest paging) {
      // TODO Auto-generated method stub
      return null;
   }
}
