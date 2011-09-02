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

package org.alfresco.repo.publishing;

import static org.alfresco.model.ContentModel.TYPE_CONTENT;
import static org.alfresco.repo.publishing.PublishingModel.PROP_AUTHORISATION_COMPLETE;
import static org.alfresco.repo.publishing.PublishingModel.TYPE_DELIVERY_CHANNEL;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.publishing.PublishingDetails;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingQueue;
import org.alfresco.service.cmr.publishing.PublishingService;
import org.alfresco.service.cmr.publishing.Status;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;

/**
 * @author Nick Smith
 * @since 4.0
 */
public class PublishingTestHelper
{
    private final ChannelService channelService;
    private final PublishingService publishingService;
    private final SiteService siteService;
    private final FileFolderService fileFolderService;
    private final PermissionService permissionService;
    
    private final NodeRef docLib;
    private final String siteId = GUID.generate();
    
    private final List<Channel> channels = new LinkedList<Channel>();
    private final List<String> events = new LinkedList<String>();
    
    public PublishingTestHelper(ChannelService channelService,
            PublishingService publishingService,
            SiteService siteService,
            FileFolderService fileFolderService,
            PermissionService permissionService)
    {
        this.channelService = channelService;
        this.publishingService = publishingService;
        this.siteService = siteService;
        this.fileFolderService = fileFolderService;
        this.permissionService = permissionService;
        
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        siteService.createSite("test", siteId,
                "Test site created by ChannelServiceImplIntegratedTest",
                "Test site created by ChannelServiceImplIntegratedTest",
                SiteVisibility.PUBLIC);
        this.docLib = siteService.createContainer(siteId, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);
    }
    

    public void tearDown() throws Exception
    {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        try
        {
            cancelAllEvents();
        }
        finally
        {
            try
            {
                deleteAllChannels();
            }
            finally
            {
                deleteSite();
            }
        }
    }

    private void deleteSite()
    {
        try
        {
            siteService.deleteSite(siteId);
        }
        catch (Throwable t)
        {
            //NOOP
        }
    }

    private void deleteAllChannels()
    {
        for (Channel channel : channels)
        {
            try
            {
                channelService.deleteChannel(channel);
            }
            catch (Throwable t)
            {
                //NOOP
            }
        }
    }

    private void cancelAllEvents()
    {
        for (String event : events)
        {
            try
            {
                publishingService.cancelPublishingEvent(event);
            }
            catch (Throwable t)
            {
                //NOOP
            }
        }
    }

    public ChannelType mockChannelType(String channelTypeId)
    {
        ChannelType channelType = channelService.getChannelType(channelTypeId);
        if (channelType != null)
        {
            reset(channelType);
            when(channelType.getId()).thenReturn(channelTypeId);
        }
        else
        {
            channelType = mock(ChannelType.class);
            when(channelType.getId()).thenReturn(channelTypeId);
            channelService.register(channelType);
        }
        when(channelType.getChannelNodeType()).thenReturn(TYPE_DELIVERY_CHANNEL);
        return channelType;
    }
    
    public Channel createChannel(String typeId)
    {
        String channelName = GUID.generate();
        return createChannel(typeId, channelName);
    }

    public Channel createChannel(String typeId, String channelName)
    {
        return createChannel(typeId, channelName, true);
    }
    
    public Channel createChannel(String typeId, String channelName, boolean isAuthorised)
    {
        Channel channel = channelService.createChannel(typeId, channelName, null);
        channels.add(channel);
        Map<QName, Serializable> properties = Collections.singletonMap(PROP_AUTHORISATION_COMPLETE, (Serializable)isAuthorised);
        channelService.updateChannel(channel, properties);
        return channel;
    }
    
    public void allowChannelAccess(String username, String channelId)
    {
        setChannelPermission(username, channelId, PermissionService.ADD_CHILDREN);
    }
    
    public void setChannelPermission(final String username, String channelId, final String permission)
    {
        final NodeRef channel = new NodeRef(channelId);
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                permissionService.setPermission(channel, username, permission, true);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    public String scheduleEvent1Year(PublishingDetails details)
    {
        Calendar schedule = Calendar.getInstance();
        schedule.add(Calendar.YEAR, 1);
        details.setSchedule(schedule);
        return scheduleEvent(details);
    }
    
    public String scheduleEvent(PublishingDetails details)
    {
        PublishingQueue queue = publishingService.getPublishingQueue();
        String eventId = queue.scheduleNewEvent(details);
        events.add(eventId);
        return eventId;
    }
    
    public void addEvent(String eventId)
    {
        events.add(eventId);
    }
    
    public void addEvents(Collection<String> eventIds)
    {
        events.addAll(eventIds);
    }
    
    public NodeRef createContentNode(String name)
    {
        return fileFolderService.create(docLib, name, TYPE_CONTENT).getNodeRef();
    }
    
    public NodeRef createContentNode(String name, File theContent, String mimetype)
    {
        NodeRef source = createContentNode(name);
        writeContent(source, theContent, mimetype);
        return source;
    }

    public NodeRef createContentNode(String name, String theContent, String mimetype)
    {
        NodeRef source = fileFolderService.create(docLib, name, TYPE_CONTENT).getNodeRef();
        writeContent(source, theContent, mimetype);
        return source;
    }
    
    public void writeContent(NodeRef source, String theContent, String mimetype)
    {
        ContentWriter writer = fileFolderService.getWriter(source);
        writer.setEncoding("UTF-8");
        writer.putContent(theContent);
        writer.setMimetype(mimetype);
    }
    
    public void writeContent(NodeRef source, File theContent, String mimetype)
    {
        ContentWriter writer = fileFolderService.getWriter(source);
        writer.setMimetype(mimetype);
        writer.setEncoding("UTF-8");
        writer.putContent(theContent);
    }


    public PublishingEvent publishAndWait(final PublishingDetails details, RetryingTransactionHelper transactionHelper) throws InterruptedException
    {
        RetryingTransactionCallback<String> startWorkflowCallback = new RetryingTransactionCallback<String>()
        {
            public String execute() throws Throwable
            {
                return scheduleEvent(details);
            }
        };
        String eventId = transactionHelper.doInTransaction(startWorkflowCallback);

        int i = 0;
        while (i < 100)
        {
            Thread.sleep(200);
            PublishingEvent event = publishingService.getPublishingEvent(eventId);
            Status status = event.getStatus();
            if (Status.IN_PROGRESS != status && Status.SCHEDULED != status)
            {
                return event;
            }
            i++;
        }
        throw new IllegalStateException("The publishing event did not complete after 20s!");
    }
}
