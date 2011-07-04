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
public class CalendarEntryImpl implements CalendarEntry
{
    private NodeRef nodeRef;
    private NodeRef parentNodeRef;
    private Map<QName,Serializable> properties;
    
    /**
     * Creates an empty Calendar Entry, to be stored in the
     *  given parent
     */
    public CalendarEntryImpl(NodeRef parentNodeRef)
    {
       this.parentNodeRef = parentNodeRef;
       this.properties = new HashMap<QName, Serializable>();
    }
    
    /**
     * Wraps an existing Calendar Entry node
     */
    public CalendarEntryImpl(NodeRef nodeRef, NodeRef parentNodeRef, Map<QName,Serializable> properties)
    {
       this.nodeRef = nodeRef;
       this.parentNodeRef = parentNodeRef;
       this.properties = properties;
    }
    
    protected void recordStorageDetails(NodeRef nodeRef)
    {
       this.nodeRef = nodeRef;
    }

    /**
     * Returns the NodeRef of the parent where this calendar entry
     *  lives, or will live
     */
    public NodeRef getParentNodeRef() 
    {
       return parentNodeRef;
    }

    /**
     * Returns the current set of properties
     */
    public Map<QName,Serializable> getProperties()
    {
       return properties;
    }
    
    @Override
    public NodeRef getNodeRef() 
    {
       return nodeRef;
    }
    
    @Override
    public String getSystemName() {
       // TODO Auto-generated method stub
       return null;
    }

    @Override
    public String getTitle() {
       // TODO Auto-generated method stub
       return null;
    }

    @Override
    public void setTitle(String title) {
       // TODO Auto-generated method stub

    }
    @Override
    public String getDescription() {
       // TODO Auto-generated method stub
       return null;
    }

    @Override
    public void setDescription(String description) {
       // TODO Auto-generated method stub
    }

    @Override
    public Date getStart() {
       // TODO Auto-generated method stub
       return null;
    }

    @Override
    public void setStart(Date start) {
       // TODO Auto-generated method stub
       
    }
    
    @Override
    public Date getEnd() {
       // TODO Auto-generated method stub
       return null;
    }

    @Override
    public void setEnd(Date end) {
       // TODO Auto-generated method stub
       
    }
    
    @Override
    public String getLocation() {
       // TODO Auto-generated method stub
       return null;
    }
    
    @Override
    public void setLocation(String location) {
       // TODO Auto-generated method stub

    }

    @Override
    public List<String> getTags() {
       // TODO Auto-generated method stub
       return null;
    }
}
