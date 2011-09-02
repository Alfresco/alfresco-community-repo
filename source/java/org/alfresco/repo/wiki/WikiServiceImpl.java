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
package org.alfresco.repo.wiki;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.EmptyPagingResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.node.getchildren.GetChildrenAuditableCannedQuery;
import org.alfresco.repo.node.getchildren.GetChildrenAuditableCannedQueryFactory;
import org.alfresco.repo.query.NodeBackedEntity;
import org.alfresco.repo.site.SiteServiceImpl;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
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
public class WikiServiceImpl implements WikiService
{
    public static final String WIKI_COMPONENT = "wiki";
   
    protected static final String CANNED_QUERY_GET_CHILDREN = "wikiGetChildrenCannedQueryFactory";
    
    /**
     * The logger
     */
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(WikiServiceImpl.class);
    
    private NodeDAO nodeDAO;
    private NodeService nodeService;
    private SiteService siteService;
    private ContentService contentService;
    private TaggingService taggingService;
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
     * Fetches the Wiki Container on a site, creating as required if requested.
     */
    protected NodeRef getSiteWikiContainer(final String siteShortName, boolean create)
    {
       return SiteServiceImpl.getSiteContainer(
             siteShortName, WIKI_COMPONENT, create, 
             siteService, transactionService, taggingService);
    }
    
    /**
     * Turns a Title into a Page Name.
     */
    private static String buildName(String title)
    {
       // The name is based on the title, but with underscores
       String name = title.replace(' ', '_');
       return name;
    }
    
    private WikiPageInfo buildPage(NodeRef nodeRef, NodeRef container, String name, String preLoadedContents)
    {
       WikiPageInfoImpl page = new WikiPageInfoImpl(nodeRef, container, name);
       
       // Grab all the properties, we need the bulk of them anyway
       Map<QName,Serializable> props = nodeService.getProperties(nodeRef);
       
       // Start with the auditable properties
       page.setCreator((String)props.get(ContentModel.PROP_CREATOR));
       page.setModifier((String)props.get(ContentModel.PROP_MODIFIER));
       page.setCreatedAt((Date)props.get(ContentModel.PROP_CREATED));
       page.setModifiedAt((Date)props.get(ContentModel.PROP_MODIFIED));
       
       // Now the wiki ones
       page.setTitle((String)props.get(ContentModel.PROP_TITLE));
       
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
       page.setContents(contents);
       
       // Finally tags
       page.setTags(taggingService.getTags(nodeRef));
       
       // All done
       return page;
    }
    
    
    @Override
    public WikiPageInfo getWikiPage(String siteShortName, String pageName) 
    {
       NodeRef container = getSiteWikiContainer(siteShortName, false);
       if(container == null)
       {
          // No links
          return null;
       }
       
       NodeRef link = nodeService.getChildByName(container, ContentModel.ASSOC_CONTAINS, pageName);
       if(link != null)
       {
          return buildPage(link, container, pageName, null);
       }
       return null;
    }

    @Override
    public WikiPageInfo createWikiPage(String siteShortName, String title,
          String content) 
    {
       // Grab the location to store in
       NodeRef container = getSiteWikiContainer(siteShortName, true);
       
       // Build the name
       String name = buildName(title);
       
       // Get the properties for the node
       Map<QName, Serializable> props = new HashMap<QName, Serializable>();
       props.put(ContentModel.PROP_NAME,  name);
       props.put(ContentModel.PROP_TITLE, title);
       
       // Build the node
       NodeRef nodeRef = nodeService.createNode(
             container,
             ContentModel.ASSOC_CONTAINS,
             QName.createQName(name),
             ContentModel.TYPE_CONTENT,
             props
       ).getChildRef();
       
       // Store the content
       ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
       writer.setEncoding("UTF-8");
       writer.putContent(content);
             
       // Generate the wrapping object for it
       // Build it that way, so creator and created date come through
       return buildPage(nodeRef, container, name, content);
    }

    @Override
    public WikiPageInfo updateWikiPage(WikiPageInfo page) 
    {
       // Sanity check what we were given
       if(page.getNodeRef() == null)
       {
          throw new IllegalArgumentException("Can't update a page that was never persisted, call create instead");
       }
       
       NodeRef nodeRef = page.getNodeRef();
       String nodeName = buildName(page.getTitle());
       
       // Handle the rename case
       boolean renamed = false;
       if(! nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE).equals(page.getTitle()))
       {
          try
          {
             fileFolderService.rename(nodeRef, nodeName);
             renamed = true;
          }
          catch(FileNotFoundException e)
          {
             throw new AlfrescoRuntimeException("Invalid node state - wiki page no longer found");
          }
          nodeService.setProperty(nodeRef, ContentModel.PROP_NAME,  nodeName);
          nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, page.getTitle());
       }
       
       // Change the content
       ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
       writer.setEncoding("UTF-8");
       writer.putContent(page.getContents());
       
       // Now do the tags
       taggingService.setTags(nodeRef, page.getTags());
       
       // If we re-named, re-create the object
       if(renamed)
       {
          page = buildPage(nodeRef, page.getContainerNodeRef(), nodeName, page.getContents());
       }
       
       // All done
       return page;
    }

    @Override
    public void deleteWikiPage(WikiPageInfo page) 
    {
       if(page.getNodeRef() == null)
       {
          throw new IllegalArgumentException("Can't delete a wiki page that was never persisted");
       }

       nodeService.deleteNode(page.getNodeRef());
    }

    @Override
    public PagingResults<WikiPageInfo> listWikiPages(String siteShortName, PagingRequest paging) 
    {
       return listWikiPages(siteShortName, null, paging);
    }

    @Override
    public PagingResults<WikiPageInfo> listWikiPages(String siteShortName, String user,
          PagingRequest paging) 
    {
       return listWikiPages(siteShortName, user, null, null, null, null, paging);
    }

    @Override
    public PagingResults<WikiPageInfo> listWikiPagesByCreated(String siteShortName, 
          Date from, Date to, PagingRequest paging) 
    {
       return listWikiPages(siteShortName, null, from, to, null, null, paging);
    }

    @Override
    public PagingResults<WikiPageInfo> listWikiPagesByModified(String siteShortName, 
          Date from, Date to, PagingRequest paging) 
    {
       return listWikiPages(siteShortName, null, null, null, from, to, paging);
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
       // Pre-load the nodes before we create them
       List<Long> ids = new ArrayList<Long>();
       for(NodeBackedEntity node : results.getPage())
       {
          ids.add(node.getId());
       }
       nodeDAO.cacheNodesById(ids);
       
       // Wrap
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
                  pages.add(buildPage(nodeRef, container, name, null));
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
}
