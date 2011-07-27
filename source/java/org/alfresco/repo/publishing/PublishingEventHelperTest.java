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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.alfresco.repo.publishing.PublishingModel.ASSOC_PUBLISHING_EVENT;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_CHANNEL;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_COMMENT;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_PAYLOAD;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_STATUS;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_TIME;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_TIME_ZONE;
import static org.alfresco.repo.publishing.PublishingModel.TYPE_PUBLISHING_EVENT;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingPackageEntry;
import org.alfresco.service.cmr.publishing.Status;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test/alfresco/test-web-publishing-context.xml"})
public class PublishingEventHelperTest
{
    @Resource(name="publishingEventHelper")
    PublishingEventHelper helper;
    
    @Resource(name="NodeService")
    NodeService nodeService;
    
    @Resource(name="ContentService")
    ContentService contentService;
    
    @Test
    public void testGetPublishingEventNode() throws Exception
    {
        NodeRef eventNode= helper.getPublishingEventNode(null);
        assertNull("If id is null event shoudl be null!", eventNode);
        
        eventNode= helper.getPublishingEventNode("foo");
        assertNull("If id is invalid event shoudl be null!", eventNode);
        
        String nonExistantId = "foo://bar/nonExistantId";
        eventNode= helper.getPublishingEventNode(nonExistantId);
        assertNull("If event node does not exist event shoudl be null!", eventNode);
        
        String nonPublishingEventId = "foo://bar/nonPublishingEventId";
        NodeRef nonPublishingEventNode = new NodeRef(nonPublishingEventId);
        when(nodeService.exists(nonPublishingEventNode)).thenReturn(true);
        
        eventNode= helper.getPublishingEventNode(nonPublishingEventId);
        assertNull("Event shoudl exist!", eventNode);
        
        String publishingEventId = "foo://bar/publishingEventId";
        NodeRef publishingEventNode = new NodeRef(publishingEventId);
        when(nodeService.exists(publishingEventNode)).thenReturn(true);
        when(nodeService.getType(publishingEventNode)).thenReturn(TYPE_PUBLISHING_EVENT);
        
        eventNode= helper.getPublishingEventNode(publishingEventId);
        assertNotNull("Event shoudl exist!", eventNode);
    }
    
    @Test
    public void testGetPublishingEvent() throws Exception
    {
        // Mock up ContentReader to do nothing. Not testing payload deserialization.
        ContentReader reader = mock(ContentReader.class);
        InputStream inputStream = mock(InputStream.class);
        when(reader.getContentInputStream()).thenReturn(inputStream);
        when(contentService.getReader(any(NodeRef.class), any(QName.class)))
            .thenReturn(reader);
        PublishingPackageSerializer serializer = mock(PublishingPackageSerializer.class);
        helper.setSerializer(serializer);
        
        PublishingEvent result = helper.getPublishingEvent((NodeRef)null);
        assertNull(result);
        
        String comment = "The comment";
        Status status = Status.COMPLETED;
        Date modified= new Date();
        Date created = new Date(modified.getTime()-3600000);
        String creatorName = "The creator";
        String modifierName = "The modifier";
        Calendar schedule = Calendar.getInstance();
        schedule.add(Calendar.MONTH, 6);
        Date scheduledTime = schedule.getTime();
        String scheduledTimeZone = schedule.getTimeZone().getID();
        
        // Mock up node properties.
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(PROP_PUBLISHING_EVENT_COMMENT, comment);
        props.put(PROP_PUBLISHING_EVENT_STATUS, status.name());
        props.put(PROP_PUBLISHING_EVENT_TIME, scheduledTime);
        props.put(PROP_PUBLISHING_EVENT_TIME_ZONE, scheduledTimeZone);
        props.put(ContentModel.PROP_CREATED, created);
        props.put(ContentModel.PROP_CREATOR, creatorName);
        props.put(ContentModel.PROP_MODIFIED, modified);
        props.put(ContentModel.PROP_MODIFIER, modifierName);
        
        NodeRef eventNode = new NodeRef("foo://bar/eventNode");
        when(nodeService.getProperties(eventNode)).thenReturn(props);
        
        result = helper.getPublishingEvent(eventNode);
        assertEquals(eventNode.toString(), result.getId());
        assertEquals(comment, result.getComment());
        assertEquals(status, result.getStatus());
        assertEquals(schedule, result.getScheduledTime());
        assertEquals(created, result.getCreatedTime());
        assertEquals(creatorName, result.getCreator());
        assertEquals(modified, result.getModifiedTime());
        assertEquals(modifierName, result.getModifier());
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testCreateNode() throws Exception
    {
        // Mock serializer since this behaviour is already tested in PublishingPackageSerializerTest.
        ContentWriter writer = mock(ContentWriter.class);
        when(contentService.getWriter(any(NodeRef.class), eq(PROP_PUBLISHING_EVENT_PAYLOAD), eq(true)))
            .thenReturn(writer);
        OutputStream outputStream = mock(OutputStream.class);
        when(writer.getContentOutputStream()).thenReturn(outputStream);
        PublishingPackageSerializer serializer = mock(PublishingPackageSerializer.class);
        helper.setSerializer(serializer);
        
        NodeRef queue = new NodeRef("foo://bar/queue");
        NodeRef event = new NodeRef("foo://bar/event");
        
        ChildAssociationRef childAssoc = new ChildAssociationRef(ASSOC_PUBLISHING_EVENT, queue, null, event);
        when(nodeService.createNode(any(NodeRef.class), any(QName.class), any(QName.class), any(QName.class), anyMap()))
            .thenReturn(childAssoc);
        
        Map<NodeRef, PublishingPackageEntry> entires = Collections.emptyMap();
        PublishingPackage pckg = new PublishingPackageImpl(entires);
        String channelName = "The channel";
        Calendar schedule = Calendar.getInstance();
        String comment = "The comment";
        
        NodeRef result = helper.createNode(queue, pckg, channelName, schedule, comment, null);
        assertEquals(event, result);
        
        ArgumentCaptor<Map> argument = ArgumentCaptor.forClass(Map.class);
        verify(nodeService)
            .createNode(eq(queue), eq(ASSOC_PUBLISHING_EVENT),
                    any(QName.class), eq(TYPE_PUBLISHING_EVENT),
                    argument.capture());
        Map<QName, Serializable> props = argument.getValue();
        
        assertNotNull(props.get(ContentModel.PROP_NAME));
        assertEquals(channelName, props.get(PROP_PUBLISHING_EVENT_CHANNEL));
        assertEquals(comment, props.get(PROP_PUBLISHING_EVENT_COMMENT));
        assertEquals(schedule.getTime(), props.get(PROP_PUBLISHING_EVENT_TIME));
        assertEquals(schedule.getTimeZone().getID(), props.get(PROP_PUBLISHING_EVENT_TIME_ZONE));
    }
}
