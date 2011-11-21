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
package org.alfresco.repo.links;

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
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.node.getchildren.GetChildrenAuditableCannedQuery;
import org.alfresco.repo.node.getchildren.GetChildrenAuditableCannedQueryFactory;
import org.alfresco.repo.query.NodeBackedEntity;
import org.alfresco.repo.search.impl.lucene.LuceneUtils;
import org.alfresco.repo.site.SiteServiceImpl;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.links.LinkInfo;
import org.alfresco.service.cmr.links.LinksService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
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
public class LinksServiceImpl implements LinksService
{
    public static final String LINKS_COMPONENT = "links";
   
    protected static final String CANNED_QUERY_GET_CHILDREN = "linksGetChildrenCannedQueryFactory";
    
    /**
     * The logger
     */
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(LinksServiceImpl.class);
    
    private NodeDAO nodeDAO;
    private NodeService nodeService;
    private SiteService siteService;
    private SearchService searchService;
    private ContentService contentService;
    private TaggingService taggingService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
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
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
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
     * Fetches the Links Container on a site, creating as required if requested.
     */
    protected NodeRef getSiteLinksContainer(final String siteShortName, boolean create)
    {
       return SiteServiceImpl.getSiteContainer(
             siteShortName, LINKS_COMPONENT, create, 
             siteService, transactionService, taggingService);
    }
    
    private LinkInfo buildLink(NodeRef nodeRef, NodeRef container, String name)
    {
       LinkInfoImpl link = new LinkInfoImpl(nodeRef, container, name);
       
       // Grab all the properties, we need the bulk of them anyway
       Map<QName,Serializable> props = nodeService.getProperties(nodeRef);
       
       // Start with the auditable properties
       link.setCreator((String)props.get(ContentModel.PROP_CREATOR));
       link.setCreatedAt((Date)props.get(ContentModel.PROP_CREATED));
       link.setModifiedAt((Date)props.get(ContentModel.PROP_MODIFIED));
       
       // Now the link ones
       link.setTitle((String)props.get(LinksModel.PROP_TITLE));
       link.setDescription((String)props.get(LinksModel.PROP_DESCRIPTION));
       link.setURL((String)props.get(LinksModel.PROP_URL));
       
       // Now the internal aspect
       if (nodeService.hasAspect(nodeRef, LinksModel.ASPECT_INTERNAL_LINK))
       {
          Boolean isInternal = DefaultTypeConverter.INSTANCE.convert(
                Boolean.class, props.get(LinksModel.PROP_IS_INTERNAL));
          link.setInternal(isInternal);
       }
       else
       {
          // Not internal
          link.setInternal(false);
       }
       
       // Finally tags
       link.setTags(taggingService.getTags(nodeRef));
       
       // All done
       return link;
    }
    
    
    @Override
    public LinkInfo getLink(String siteShortName, String linkName) 
    {
       NodeRef container = getSiteLinksContainer(siteShortName, false);
       if (container == null)
       {
          // No links
          return null;
       }
       
       NodeRef link = nodeService.getChildByName(container, ContentModel.ASSOC_CONTAINS, linkName);
       if (link != null)
       {
          return buildLink(link, container, linkName);
       }
       return null;
    }

    @Override
    public LinkInfo createLink(String siteShortName, String title,
          String description, String url, boolean internal) 
    {
       // Grab the location to store in
       NodeRef container = getSiteLinksContainer(siteShortName, true);
       
       // Get the properties for the node
       Map<QName, Serializable> props = new HashMap<QName, Serializable>();
       props.put(LinksModel.PROP_TITLE,       title);
       props.put(LinksModel.PROP_DESCRIPTION, description);
       props.put(LinksModel.PROP_URL,         url);
       
       if (internal)
       {
          props.put(LinksModel.PROP_IS_INTERNAL, "true");
       }
       
       // Generate a unique name
       // (Should be unique, but will retry for a new one if not)
       String name = "link-" + (new Date()).getTime() + "-" + 
                     Math.round(Math.random()*10000);
       props.put(ContentModel.PROP_NAME, name);
       
       // Build the node
       NodeRef nodeRef = nodeService.createNode(
             container,
             ContentModel.ASSOC_CONTAINS,
             QName.createQName(name),
             LinksModel.TYPE_LINK,
             props
       ).getChildRef();
       
       // Duplicate the url into the node as the content property
       ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
       writer.setEncoding("UTF-8");
       writer.putContent(url);
       
       // Generate the wrapping object for it
       // Build it that way, so creator and created date come through
       return buildLink(nodeRef, container, name);
    }

    @Override
    public LinkInfo updateLink(LinkInfo link) 
    {
       // Sanity check what we were given
       if (link.getNodeRef() == null)
       {
          throw new IllegalArgumentException("Can't update a link that was never persisted, call create instead");
       }
       
       // Change the properties
       NodeRef nodeRef = link.getNodeRef();
       nodeService.setProperty(nodeRef, LinksModel.PROP_TITLE,       link.getTitle());
       nodeService.setProperty(nodeRef, LinksModel.PROP_DESCRIPTION, link.getDescription());
       nodeService.setProperty(nodeRef, LinksModel.PROP_URL,         link.getURL());
       
       // Internal/External is "special"
       if (link.isInternal())
       {
          if (! nodeService.hasAspect(nodeRef, LinksModel.ASPECT_INTERNAL_LINK))
          {
             Map<QName, Serializable> props = new HashMap<QName, Serializable>();
             props.put(LinksModel.PROP_IS_INTERNAL, "true");
             nodeService.addAspect(nodeRef, LinksModel.ASPECT_INTERNAL_LINK, props);
          }
       }
       else
       {
          if (nodeService.hasAspect(nodeRef, LinksModel.ASPECT_INTERNAL_LINK))
          {
             nodeService.removeAspect(nodeRef, LinksModel.ASPECT_INTERNAL_LINK);
          }
       }
       
       // Duplicate the url into the node as the content property
       ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
       writer.setEncoding("UTF-8");
       writer.putContent(link.getURL());
       
       // Now do the tags
       taggingService.setTags(nodeRef, link.getTags());
       
       // All done
       return link;
    }

    @Override
    public void deleteLink(LinkInfo link) 
    {
       if (link.getNodeRef() == null)
       {
          throw new IllegalArgumentException("Can't delete a link entry that was never persisted");
       }

       nodeService.deleteNode(link.getNodeRef());
    }

    @Override
    public PagingResults<LinkInfo> listLinks(String siteShortName, PagingRequest paging) 
    {
       return listLinks(siteShortName, null, null, null, paging);
    }

    @Override
    public PagingResults<LinkInfo> listLinks(String siteShortName, String user,
          PagingRequest paging) 
    {
       return listLinks(siteShortName, user, null, null, paging);
    }

    @Override
    public PagingResults<LinkInfo> listLinks(String siteShortName, Date from,
          Date to, PagingRequest paging) 
    {
       return listLinks(siteShortName, null, from, to, paging);
    }
    
    private PagingResults<LinkInfo> listLinks(String siteShortName, String user,
          Date from, Date to, PagingRequest paging) 
    {
       NodeRef container = getSiteLinksContainer(siteShortName, false);
       if (container == null)
       {
          // No events
          return new EmptyPagingResults<LinkInfo>();
       }
       
       // Run the canned query
       GetChildrenAuditableCannedQueryFactory getChildrenCannedQueryFactory = 
                   (GetChildrenAuditableCannedQueryFactory)cannedQueryRegistry.getNamedObject(CANNED_QUERY_GET_CHILDREN);
       GetChildrenAuditableCannedQuery cq = (GetChildrenAuditableCannedQuery)getChildrenCannedQueryFactory.getCannedQuery(
             container, LinksModel.TYPE_LINK, user, from, to, null, null, null, 
             getChildrenCannedQueryFactory.createDateDescendingCQSortDetails(), paging);
       
       // Execute the canned query
       CannedQueryResults<NodeBackedEntity> results = cq.execute();
       
       // Convert to Link objects
       return wrap(results, container);
    }

    @Override
    public PagingResults<LinkInfo> findLinks(String siteShortName, String user,
          Date from, Date to, String tag, PagingRequest paging) 
    {
       NodeRef container = getSiteLinksContainer(siteShortName, false);
       if (container == null)
       {
          // No links
          return new EmptyPagingResults<LinkInfo>();
       }
       
       // Build the query
       StringBuilder luceneQuery = new StringBuilder();
       luceneQuery.append(" +TYPE:\"" + LinksModel.TYPE_LINK + "\"");
       luceneQuery.append(" +PATH:\"" + nodeService.getPath(container).toPrefixString(namespaceService) + "/*\"");

       if (user != null)
       {
          luceneQuery.append(" +@cm\\:creator:\"" + user + "\"");
       }
       if (from != null && to != null)
       {
          luceneQuery.append(LuceneUtils.createDateRangeQuery(
                from, to, ContentModel.PROP_CREATED, dictionaryService, namespaceService));
       }
       if (tag != null)
       {
          luceneQuery.append(" +PATH:\"/cm:taggable/cm:" + ISO9075.encode(tag) + "/member\"");
       }
       
       String sortOn = "@{http://www.alfresco.org/model/content/1.0}created";

       // Query
       SearchParameters sp = new SearchParameters();
       sp.addStore(container.getStoreRef());
       sp.setLanguage(SearchService.LANGUAGE_LUCENE);
       sp.setQuery(luceneQuery.toString());
       sp.addSort(sortOn, false);
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
       PagingResults<LinkInfo> pagedResults = new EmptyPagingResults<LinkInfo>();
       ResultSet results = null;
       
       try 
       {
          results = searchService.query(sp);
          pagedResults = wrap(results, container);
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
    
    private PagingResults<LinkInfo> wrap(final ResultSet finalLuceneResults, final NodeRef container)
    {
       final List<LinkInfo> links = new ArrayList<LinkInfo>();
       for (ResultSetRow row : finalLuceneResults)
       {
          LinkInfo link = buildLink(
                row.getNodeRef(), container, row.getQName().getLocalName());
          links.add(link);
       }
       
       // Wrap
       return new PagingResults<LinkInfo>() 
       {
          @Override
          public boolean hasMoreItems() 
          {
             try
             {
                return finalLuceneResults.hasMore();
             }
             catch(UnsupportedOperationException e)
             {
                // Not all lucene results support paging
                return false;
             }
          }

          @Override
          public Pair<Integer, Integer> getTotalResultCount() 
          {
             int skipCount = 0;
             int itemsRemainingAfterThisPage = 0;
             try
             {
                skipCount = finalLuceneResults.getStart();
             }
             catch(UnsupportedOperationException e) {}
             try
             {
                itemsRemainingAfterThisPage = finalLuceneResults.length();
             }
             catch(UnsupportedOperationException e) {}
             
             final int totalItemsInUnpagedResultSet = skipCount + itemsRemainingAfterThisPage;
             return new Pair<Integer, Integer>(totalItemsInUnpagedResultSet, totalItemsInUnpagedResultSet);
          }

          @Override
          public List<LinkInfo> getPage() 
          {
             return links;
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
     *  LinkInfo instances
     */
    private PagingResults<LinkInfo> wrap(final PagingResults<NodeBackedEntity> results, final NodeRef container)
    {
       // Pre-load the nodes before we create them
       List<Long> ids = new ArrayList<Long>();
       for (NodeBackedEntity node : results.getPage())
       {
          ids.add(node.getId());
       }
       nodeDAO.cacheNodesById(ids);
       
       // Wrap
       return new PagingResults<LinkInfo>()
       {
           @Override
           public String getQueryExecutionId()
           {
               return results.getQueryExecutionId();
           }
           
           @Override
           public List<LinkInfo> getPage()
           {
               List<LinkInfo> links = new ArrayList<LinkInfo>();
               for (NodeBackedEntity node : results.getPage())
               {
                  NodeRef nodeRef = node.getNodeRef();
                  String name = node.getName();
                  links.add(buildLink(nodeRef, container, name));
               }
               return links;
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
