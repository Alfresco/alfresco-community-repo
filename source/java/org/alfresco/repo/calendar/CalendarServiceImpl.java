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
package org.alfresco.repo.calendar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.EmptyPagingResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.calendar.cannedqueries.GetCalendarEntriesCannedQuery;
import org.alfresco.repo.calendar.cannedqueries.GetCalendarEntriesCannedQueryFactory;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQueryFactory;
import org.alfresco.repo.site.SiteServiceImpl;
import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.tagging.TaggingService;
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
public class CalendarServiceImpl implements CalendarService
{
    public static final String CALENDAR_COMPONENT = "calendar";
   
    /**
     *  For backwards compatibility with pre-Swift, we are asking the query to give us an accurate total count of how many
     *  calendar nodes there are. This may need to change in the future - certainly if the current 'brute force' query
     *  is replaced by a database query.
     */
    private static final int MAX_QUERY_ENTRY_COUNT = 10000;

    protected static final String CANNED_QUERY_GET_CHILDREN = "calendarGetChildrenCannedQueryFactory";
    protected static final String CANNED_QUERY_GET_ENTRIES = "calendarGetCalendarEntriesCannedQueryFactory";
    
    /**
     * The logger
     */
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(CalendarServiceImpl.class);
    
    private NodeService nodeService;
    private SiteService siteService;
    private TaggingService taggingService;
    private PermissionService permissionService;
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
    
    public void setTaggingService(TaggingService taggingService)
    {
        this.taggingService = taggingService;
    }
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
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
     * Fetches the Calendar Container on a site, creating as required if requested.
     */
    protected NodeRef getSiteCalendarContainer(final String siteShortName, boolean create)
    {
       return SiteServiceImpl.getSiteContainer(
             siteShortName, CALENDAR_COMPONENT, create, 
             siteService, transactionService, taggingService);
    }

    @Override
    public CalendarEntry getCalendarEntry(String siteShortName, String entryName) 
    {
       NodeRef container = getSiteCalendarContainer(siteShortName, false);
       if(container == null)
       {
          // No events
          return null;
       }
       
       NodeRef event = nodeService.getChildByName(container, ContentModel.ASSOC_CONTAINS, entryName);
       if(event != null)
       {
          CalendarEntryImpl entry = new CalendarEntryImpl(event, container, entryName);
          entry.populate(nodeService.getProperties(event));
          entry.setTags(taggingService.getTags(event));
          return entry;
       }
       return null;
    }

    @Override
    public CalendarEntry createCalendarEntry(String siteShortName, CalendarEntry entry) 
    {
       if(entry.getNodeRef() != null)
       {
          throw new IllegalArgumentException("Can't call create for a calendar entry that was previously persisted");
       }
       
       // Grab the location to store in
       NodeRef container = getSiteCalendarContainer(siteShortName, true);
       
       // Turn the entry into properties
       Map<QName,Serializable> properties = CalendarEntryImpl.toNodeProperties(entry);
       
       // Generate a unique name
       // (Should be unique, but will retry for a new one if not)
       String name = (new Date()).getTime() + "-" + 
                     Math.round(Math.random()*10000) + ".ics";
       properties.put(ContentModel.PROP_NAME, name);
       
       // Add the entry
       NodeRef nodeRef = nodeService.createNode(
             container,
             ContentModel.ASSOC_CONTAINS,
             QName.createQName(name),
             CalendarModel.TYPE_EVENT,
             properties
       ).getChildRef();
       
       // Record it's details
       CalendarEntryImpl entryImpl;
       if(entry instanceof CalendarEntryImpl)
       {
          entryImpl = (CalendarEntryImpl)entry;
          entryImpl.recordStorageDetails(nodeRef, container, name);
       }
       else
       {
          entryImpl = new CalendarEntryImpl(nodeRef, container, name);
          entryImpl.populate(properties);
          entryImpl.setTags(entry.getTags());
       }
       
       // Tag it
       taggingService.setTags(nodeRef, entry.getTags());
             
       // All done
       return entryImpl;
    }

    @Override
    public CalendarEntry updateCalendarEntry(CalendarEntry entry) {
       // Sanity check what we were given
       if(entry.getNodeRef() == null)
       {
          throw new IllegalArgumentException("Can't update a calendar entry that was never persisted, call create instead");
       }
       
       // Get the Calendar properties
       Map<QName,Serializable> properties = CalendarEntryImpl.toNodeProperties(entry);
       
       // Merge in the non calendar ones
       for(Map.Entry<QName,Serializable> prop : nodeService.getProperties(entry.getNodeRef()).entrySet())
       {
          if(! prop.getKey().getNamespaceURI().equals(CalendarModel.CALENDAR_MODEL_URL))
          {
             properties.put(prop.getKey(), prop.getValue());
          }
       }
       
       // Save the new properties
       nodeService.setProperties(entry.getNodeRef(), properties);
       
       // Update the tags
       taggingService.setTags(entry.getNodeRef(), entry.getTags());
       
       // Nothing was changed on the entry itself
       return entry;
    }

    @Override
    public void deleteCalendarEntry(CalendarEntry entry) {
       if(entry.getNodeRef() == null)
       {
          throw new IllegalArgumentException("Can't delete a calendar entry that was never persisted");
       }

       nodeService.deleteNode(entry.getNodeRef());
    }

    @Override
    public PagingResults<CalendarEntry> listCalendarEntries(
          String siteShortName, PagingRequest paging) 
    {
       NodeRef container = getSiteCalendarContainer(siteShortName, false);
       if(container == null)
       {
          // No events
          return new EmptyPagingResults<CalendarEntry>();
       }
       
       // Build our sorting, by date
       List<Pair<QName,Boolean>> sort = new ArrayList<Pair<QName, Boolean>>();
       sort.add(new Pair<QName, Boolean>(CalendarModel.PROP_FROM_DATE, true)); 
       sort.add(new Pair<QName, Boolean>(CalendarModel.PROP_TO_DATE, true));
       
       // We only want calendar entries
       Set<QName> types = new HashSet<QName>();
       types.add(CalendarModel.TYPE_EVENT);
       
       // Run the canned query
       GetChildrenCannedQueryFactory getChildrenCannedQueryFactory = (GetChildrenCannedQueryFactory)cannedQueryRegistry.getNamedObject(CANNED_QUERY_GET_CHILDREN);
       GetChildrenCannedQuery cq = (GetChildrenCannedQuery)getChildrenCannedQueryFactory.getCannedQuery(
             container, null, types, null, sort, paging);
       
       // Execute the canned query
       CannedQueryResults<NodeRef> results = cq.execute();
       return wrap(results, container);
    }

    @Override
    public PagingResults<CalendarEntry> listCalendarEntries(
          String[] siteShortNames, PagingRequest paging) 
    {
       // If we only have the one site, use the list above 
       if(siteShortNames != null && siteShortNames.length == 1)
       {
          return listCalendarEntries(siteShortNames[0], paging);
       }

       // Use the date one with no dates
       return listCalendarEntries(siteShortNames, null, null, paging);
    }
    
    @Override
    public PagingResults<CalendarEntry> listCalendarEntries(
          String[] siteShortNames, Date from, Date to, PagingRequest paging) 
    {
       // Get the containers
       List<NodeRef> containersL = new ArrayList<NodeRef>();
       for(String siteShortName : siteShortNames)
       {
          // Grab the container for this site
          NodeRef container = getSiteCalendarContainer(siteShortName, false);
          if(container != null)
          {
             containersL.add(container);
          }
       }
       NodeRef[] containers = containersL.toArray(new NodeRef[containersL.size()]);
       
       // Check we have some sites to look for
       if(containers.length == 0)
       {
          // No sites, so no events
          return new EmptyPagingResults<CalendarEntry>();
       }
       
       // Run the canned query
       GetCalendarEntriesCannedQueryFactory cqFactory = (GetCalendarEntriesCannedQueryFactory)cannedQueryRegistry.getNamedObject(CANNED_QUERY_GET_ENTRIES);
       GetCalendarEntriesCannedQuery cq = (GetCalendarEntriesCannedQuery)cqFactory.getCannedQuery(
             containers, from, to, paging
       );
       
       // Execute the canned query
       return cq.execute();
    }
    
    /**
     * Our class to wrap up paged results of NodeRefs as
     *  CalendarEntry instances
     */
    private PagingResults<CalendarEntry> wrap(final PagingResults<NodeRef> results, final NodeRef container)
    {
       return new PagingResults<CalendarEntry>()
       {
           @Override
           public String getQueryExecutionId()
           {
               return results.getQueryExecutionId();
           }
           @Override
           public List<CalendarEntry> getPage()
           {
               List<CalendarEntry> entries = new ArrayList<CalendarEntry>();
               for(NodeRef nodeRef : results.getPage())
               {
                  String entryName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                  
                  CalendarEntryImpl entry = new CalendarEntryImpl(nodeRef, container, entryName);
                  entry.populate(nodeService.getProperties(nodeRef));
                  entry.setTags(taggingService.getTags(nodeRef));
                  entries.add(entry);
               }
               return entries;
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
