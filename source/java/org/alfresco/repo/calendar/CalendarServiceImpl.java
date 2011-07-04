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
import java.util.Date;
import java.util.HashMap;
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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.calendar.CalendarEntry;
import org.alfresco.service.cmr.calendar.CalendarService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.registry.NamedObjectRegistry;

/**
 * @author Nick Burch (based on existing webscript controllers in the REST API)
 * @since 4.0
 */
public class CalendarServiceImpl implements CalendarService
{
    private static final String CALENDAR_COMPONENT = "calendar";
   
    /**
     *  For backwards compatibility with pre-Swift, we are asking the query to give us an accurate total count of how many
     *  blog-post nodes there are. This may need to change in the future - certainly if the current 'brute force' query
     *  is replaced by a database query.
     */
    private static final int MAX_QUERY_ENTRY_COUNT = 10000;
    
    private NodeService nodeService;
    private SiteService siteService;
    private TaggingService taggingService;
    private PermissionService permissionService;
    
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
    
    /**
     * Fetches the Calendar Container on a site, creating as required.
     */
    private NodeRef getSiteCalendarContainer(SiteInfo site)
    {
       if(! siteService.hasContainer(site.getShortName(), CALENDAR_COMPONENT))
       {
          // TODO RunAs + Transaction
          siteService.createContainer(
                site.getShortName(), CALENDAR_COMPONENT, null, null
          );
       }
       
       NodeRef container = siteService.getContainer(site.getShortName(), CALENDAR_COMPONENT);
       if(! taggingService.isTagScope(container))
       {
          // TODO RunAs + Transaction
          taggingService.addTagScope(container);
       }
       
       return container;
    }

    @Override
    public CalendarEntry getCalendarEntry(SiteInfo site, String name) 
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String createCalendarEntry(SiteInfo site, CalendarEntry entry) 
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void updateCalendarEntry(SiteInfo site, CalendarEntry entry) 
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void deleteCalendarEntry(SiteInfo site, CalendarEntry entry) 
    {
      // TODO Auto-generated method stub
      
    }
}
