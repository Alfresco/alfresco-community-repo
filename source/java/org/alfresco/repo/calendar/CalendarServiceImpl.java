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
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Nick Burch (based on existing webscript controllers in the REST API)
 * @since 4.0
 */
public class CalendarServiceImpl implements CalendarService
{
    protected static final String CALENDAR_COMPONENT = "calendar";
   
    /**
     *  For backwards compatibility with pre-Swift, we are asking the query to give us an accurate total count of how many
     *  calendar nodes there are. This may need to change in the future - certainly if the current 'brute force' query
     *  is replaced by a database query.
     */
    private static final int MAX_QUERY_ENTRY_COUNT = 10000;
    
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(CalendarServiceImpl.class);
    
    private NodeService nodeService;
    private SiteService siteService;
    private TaggingService taggingService;
    private PermissionService permissionService;
    private TransactionService transactionService;
    
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
     * Fetches the Calendar Container on a site, creating as required if requested.
     */
    private NodeRef getSiteCalendarContainer(final String siteShortName, boolean create)
    {
       if(! siteService.hasContainer(siteShortName, CALENDAR_COMPONENT))
       {
          if(create)
          {
             if(transactionService.isReadOnly())
             {
                throw new AlfrescoRuntimeException(
                      "Unable to create the calendar container from a read only transaction"
                );
             }
             
             // Have the site container created
             if(logger.isDebugEnabled())
             {
                logger.debug("Creating " + CALENDAR_COMPONENT + " container in site " + siteShortName);
             }
             
             NodeRef container = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>() 
                {
                   public NodeRef doWork() throws Exception
                   {
                      // Create the site container
                      NodeRef container = siteService.createContainer(
                            siteShortName, CALENDAR_COMPONENT, null, null
                      );
   
                      // Done
                      return container;
                   }
                }, AuthenticationUtil.getSystemUserName()
             );
             
             if(logger.isDebugEnabled())
             {
                logger.debug("Created " + CALENDAR_COMPONENT + " as " + container + " for " + siteShortName);
             }
             
             // Container is setup and ready to use
             return container;
          }
          else
          {
             // No container for this site, and not allowed to create
             // Have the site container created
             if(logger.isDebugEnabled())
             {
                logger.debug("No " + CALENDAR_COMPONENT + " component in " + siteShortName + " and not creating");
             }
             return null;
          }
       }
       else
       {
          // Container is already there
          final NodeRef container = siteService.getContainer(siteShortName, CALENDAR_COMPONENT);
       
          // Ensure the calendar container has the tag scope aspect applied to it
          if(! taggingService.isTagScope(container))
          {
             if(logger.isDebugEnabled())
             {
                logger.debug("Attaching tag scope to " + CALENDAR_COMPONENT + " " + container.toString() + " for " + siteShortName);
             }
             AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
                public Void doWork() throws Exception
                {
                   transactionService.getRetryingTransactionHelper().doInTransaction(
                       new RetryingTransactionCallback<Void>() {
                           public Void execute() throws Throwable {
                              // Add the tag scope aspect
                              taggingService.addTagScope(container);
                              return null;
                           }
                       }, false, true
                   );
                   return null;
                }
             }, AuthenticationUtil.getSystemUserName());
          }
          
          // Container is appropriately setup and configured
          return container;
       }
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
          CalendarEntryImpl entry = new CalendarEntryImpl(event, entryName);
          entry.populate(nodeService.getProperties(event));
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
       
       // Generate a name
       String name = "123.ics"; // TODO
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
          entryImpl.recordStorageDetails(nodeRef, name);
       }
       else
       {
          entryImpl = new CalendarEntryImpl(nodeRef, name);
          entryImpl.populate(properties);
       }
       return entryImpl;
    }

    @Override
    public CalendarEntry updateCalendarEntry(CalendarEntry entry) {
       Map<QName,Serializable> properties = CalendarEntryImpl.toNodeProperties(entry);
       
       if(entry.getNodeRef() == null)
       {
          throw new IllegalArgumentException("Can't update a calendar entry that was never persisted, call create instead");
       }
       
       // Update the existing one
       nodeService.setProperties(entry.getNodeRef(), properties);
       
       // Nothing changed
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
}
