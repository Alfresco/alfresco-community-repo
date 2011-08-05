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

import static org.alfresco.model.ContentModel.ASPECT_GEOGRAPHIC;
import static org.alfresco.model.ContentModel.ASPECT_TEMPORARY;
import static org.alfresco.model.ContentModel.PROP_CONTENT;
import static org.alfresco.model.ContentModel.PROP_LATITUDE;
import static org.alfresco.model.ContentModel.PROP_LONGITUDE;
import static org.alfresco.model.ContentModel.PROP_NAME;
import static org.alfresco.model.ContentModel.TYPE_CONTENT;
import static org.alfresco.repo.publishing.PublishingModel.ASSOC_LAST_PUBLISHING_EVENT;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.publishing.MutablePublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingService;
import org.alfresco.service.cmr.publishing.Status;
import org.alfresco.service.cmr.publishing.StatusUpdate;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class PublishEventActionTest extends AbstractPublishingIntegrationTest
{
    private static final String contentNodeName = "TheName";
    private static final String content = "The quick brown fox jumped over the lazy dog";
    
    @Resource(name="publishingService")
    private PublishingService publishingService;
    
    @Resource(name="contentService")
    private ContentService contentService;
    
    @Autowired
    private ChannelHelper channelHelper;

    @Autowired
    private PublishEventAction action;

    private Channel channel;
    private NodeRef channelNode;
    private ChannelType channelType;
    
    @Test
    public void testPublishNodes() throws Exception
    {
        // Create content node with appropriate aspects added.
        NodeRef source = testHelper.createContentNode(contentNodeName, content, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        
        double lattitude = 0.25;
        double longtitude = 0.75;
        addGeographicAspect(source, lattitude, longtitude);
        nodeService.addAspect(source, ASPECT_TEMPORARY, null);
        
        NodeRef publishEventNode = publishNode(source);
        
        // Check published node exists and is in correct place.
        NodeRef publishedNode = channelHelper.mapSourceToEnvironment(source, channelNode);
        assertNotNull(publishedNode);
        assertTrue(nodeService.exists(publishedNode));
        assertEquals(channelNode, nodeService.getPrimaryParent(publishedNode).getParentRef());
        
        // Check published node type and aspects
        assertEquals(TYPE_CONTENT, nodeService.getType(publishedNode));
        Set<QName> sourceAspects = nodeService.getAspects(source);
        Set<QName> publishedAspects = nodeService.getAspects(publishedNode);
        assertTrue(publishedAspects.containsAll(sourceAspects));
        assertTrue(publishedAspects.contains(ASPECT_GEOGRAPHIC));
        assertTrue(publishedAspects.contains(ASPECT_TEMPORARY));

        // Check published node properties
        Map<QName, Serializable> publishedProps = nodeService.getProperties(publishedNode);
        assertEquals(lattitude, publishedProps.get(PROP_LATITUDE));
        assertEquals(longtitude, publishedProps.get(PROP_LONGITUDE));
        assertEquals(contentNodeName, publishedProps.get(PROP_NAME));
        assertEquals(content, readContent(source));
        
        // Check lastPublishingEvent association is created.
        List<AssociationRef> assocs = nodeService.getTargetAssocs(publishedNode, ASSOC_LAST_PUBLISHING_EVENT);
        assertEquals(1, assocs.size());
        assertEquals(publishEventNode, assocs.get(0).getTargetRef());
    }

    public void testUpdatePublishedNode() throws Exception
    {
        // Create content node without aspects
        NodeRef source = testHelper.createContentNode(contentNodeName, content, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        NodeRef publishEventNode = publishNode(source);
        
        // Check published node exists
        NodeRef publishedNode = channelHelper.mapSourceToEnvironment(source, channelNode);
        assertNotNull(publishedNode);
        assertTrue(nodeService.exists(publishedNode));

        // Verify properties set correctly.
        Map<QName, Serializable> publishedProps = nodeService.getProperties(publishedNode);
        assertFalse(publishedProps.containsKey(PROP_LATITUDE));
        assertFalse(publishedProps.containsKey(PROP_LONGITUDE));
        assertEquals(contentNodeName, publishedProps.get(PROP_NAME));
        assertEquals(content, readContent(source));
        
        Set<QName> aspects = nodeService.getAspects(source);
        assertFalse(aspects.contains(ASPECT_GEOGRAPHIC));
        
        // Check lastPublishingEvent association is created.
        List<AssociationRef> assocs = nodeService.getTargetAssocs(publishedNode, ASSOC_LAST_PUBLISHING_EVENT);
        assertEquals(1, assocs.size());
        assertEquals(publishEventNode, assocs.get(0).getTargetRef());

        // Modify source node
        double lattitude = 0.25;
        double longtitude = 0.75;
        addGeographicAspect(source, lattitude, longtitude);
        
        String newName = "NewName";
        nodeService.setProperty(source, PROP_NAME, newName);
        String newContent = "The new content";
        testHelper.writeContent(source, newContent, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        
        // Update published node.
        publishEventNode = publishNode(source);
        NodeRef newPublishNode = channelHelper.mapSourceToEnvironment(source, channelNode);
        assertEquals(publishedNode, newPublishNode);
        
        // Published node shoudl still exist.
        assertNotNull(publishedNode);
        assertTrue(nodeService.exists(publishedNode));

        // Check aspects modified
        aspects = nodeService.getAspects(publishedNode);
        assertTrue(aspects.contains(ASPECT_GEOGRAPHIC));
        assertTrue(aspects.containsAll(nodeService.getAspects(source)));
        
        // Check properties modified
        publishedProps = nodeService.getProperties(publishedNode);
        assertEquals(lattitude, publishedProps.get(PROP_LATITUDE));
        assertEquals(longtitude, publishedProps.get(PROP_LONGITUDE));
        assertEquals(newName, publishedProps.get(PROP_NAME));
        assertEquals(newContent, readContent(source));

        // Check lastPublishingEvent association has changed.
        assocs = nodeService.getTargetAssocs(publishedNode, ASSOC_LAST_PUBLISHING_EVENT);
        assertEquals(1, assocs.size());
        assertEquals(publishEventNode, assocs.get(0).getTargetRef());

        // Remove aspect from source node.
        nodeService.removeAspect(source, ASPECT_GEOGRAPHIC);
        
        // Update publish node
        publishNode(source);
        newPublishNode = channelHelper.mapSourceToEnvironment(source, channelNode);
        assertEquals(publishedNode, newPublishNode);

        aspects = nodeService.getAspects(source);
        assertFalse(aspects.contains(ASPECT_GEOGRAPHIC));
        
        publishedProps = nodeService.getProperties(publishedNode);
        assertFalse(publishedProps.containsKey(PROP_LATITUDE));
        assertFalse(publishedProps.containsKey(PROP_LONGITUDE));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testChannelTypePublishIsCalledOnPublish() throws Exception
    {
        // Create content node with appropriate aspects added.
        NodeRef source = testHelper.createContentNode(contentNodeName, content, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        
        // Enable publishing on ChannelType.
        when(channelType.canPublish()).thenReturn(true);
        
        publishNode(source);
        NodeRef publishedNode = channelHelper.mapSourceToEnvironment(source, channelNode);
        
        // Check publish was called
        verify(channelType, times(1)).publish(eq(publishedNode), anyMap());
    }
    
    @SuppressWarnings("unchecked")
    public void testChannelTypePublishIsCalledOnUpdate() throws Exception
    {
        // Create content node with appropriate aspects added.
        NodeRef source = testHelper.createContentNode(contentNodeName, content, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        
        // Publish source node but dont' call ChannelType.publish().
        publishNode(source);
        NodeRef publishedNode = channelHelper.mapSourceToEnvironment(source, channelNode);

        // Check publish was not called.
        verify(channelType, never()).publish(eq(publishedNode), anyMap());

        // Enable publishing on ChannelType.
        when(channelType.canPublish()).thenReturn(true);

        // Update publish node
        publishNode(source);
        
        // Check publish was called on update
        verify(channelType, times(1)).publish(eq(publishedNode), anyMap());
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSupportedContentTypes() throws Exception
    {
        // Create content node with appropriate aspects added.
        NodeRef source = testHelper.createContentNode(contentNodeName, content, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        
        // Enable publishing on ChannelType.
        when(channelType.canPublish()).thenReturn(true);

        // Set supported type to cm:folder
        Set<QName> contentTypes = Collections.singleton(ContentModel.TYPE_FOLDER);
        when(channelType.getSupportedContentTypes()).thenReturn(contentTypes);

        // Publish source node but don't call ChannelType.publish().
        publishNode(source);
        NodeRef publishedNode = channelHelper.mapSourceToEnvironment(source, channelNode);
        
        verify(channelType, never()).publish(eq(publishedNode), anyMap());
        
        // Change supported type to cm:content
        contentTypes = Collections.singleton(ContentModel.TYPE_CONTENT);
        when(channelType.getSupportedContentTypes()).thenReturn(contentTypes);
        
        // Publish source node
        publishNode(source);
        
        verify(channelType, times(1)).publish(eq(publishedNode), anyMap());
        
        // Change supported type to cm:cmobject
        contentTypes = Collections.singleton(ContentModel.TYPE_CMOBJECT);
        when(channelType.getSupportedContentTypes()).thenReturn(contentTypes);
        
        // Publish source node
        publishNode(source);
        
        verify(channelType, times(2)).publish(eq(publishedNode), anyMap());
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testSupportedMimeTypes() throws Exception
    {
        // Create content node with appropriate aspects added.
        NodeRef source = testHelper.createContentNode(contentNodeName, content, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        
        // Enable publishing on ChannelType.
        when(channelType.canPublish()).thenReturn(true);

        // Set supported type to XML
        Set<String> mimeTypes = Collections.singleton(MimetypeMap.MIMETYPE_XML);
        when(channelType.getSupportedMimeTypes()).thenReturn(mimeTypes);

        // Publish source node but don't call ChannelType.publish().
        publishNode(source);
        NodeRef publishedNode = channelHelper.mapSourceToEnvironment(source, channelNode);
        
        verify(channelType, never()).publish(eq(publishedNode), anyMap());
        
        // Change supported type to plain text.
        mimeTypes = Collections.singleton(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        when(channelType.getSupportedMimeTypes()).thenReturn(mimeTypes);
        
        // Publish source node
        publishNode(source);
        
        verify(channelType, times(1)).publish(eq(publishedNode), anyMap());
    }
    
    @SuppressWarnings("unchecked")
    public void testStatusUpdate() throws Exception
    {
        NodeRef source = testHelper.createContentNode(contentNodeName, content, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        
        // Create Status Update
        String message = "Here is the message ";
        StatusUpdate status = publishingService.getPublishingQueue().createStatusUpdate(message, source, channel.getId());
        
        String url = "http://test/url";
        when(channelType.getNodeUrl(any(NodeRef.class))).thenReturn(url);
        when(channelType.canPublishStatusUpdates()).thenReturn(true);
        
        publishNode(source, status);
        
        String expMessage = message + " " + url;
        verify(channelType, times(1)).updateStatus(any(Channel.class), eq(expMessage), anyMap());
    }

    private NodeRef publishNode(NodeRef source)
    {
        return publishNode(source, null);
    }
    
    private NodeRef publishNode(NodeRef source, StatusUpdate statusUpdate)
    {
        MutablePublishingPackage pckg = publishingService.getPublishingQueue().createPublishingPackage();
        pckg.addNodesToPublish(source);
        String eventId = testHelper.scheduleEvent1Year(pckg, channel.getId(), null, statusUpdate);
        
        assertNotNull(eventId);
        NodeRef eventNode = new NodeRef(eventId);
        assertTrue(nodeService.exists(eventNode));
        Serializable eventStatus = nodeService.getProperty(eventNode, PublishingModel.PROP_PUBLISHING_EVENT_STATUS);
        assertEquals(Status.SCHEDULED.name(), eventStatus);

        action.executeImpl(null, eventNode);
        
        // Check Status has changed to COMPLETE
        eventStatus = nodeService.getProperty(eventNode, PublishingModel.PROP_PUBLISHING_EVENT_STATUS);
        assertEquals(Status.COMPLETED.name(), eventStatus);

        return eventNode;
    }

    private void addGeographicAspect(NodeRef source, double lattitude, double longtitude)
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(PROP_LATITUDE, lattitude);
        props.put(PROP_LONGITUDE, longtitude);
        serviceRegistry.getNodeService().addAspect(source, ASPECT_GEOGRAPHIC, props);
    }

    private String readContent(NodeRef source)
    {
        ContentReader reader = contentService.getReader(source, PROP_CONTENT);
        return reader.getContentString();
    }

    @Override
    public void onSetUp() throws Exception
    {
        super.onSetUp();
        this.publishingService = (PublishingService) getApplicationContext().getBean("publishingService");
        this.contentService = (ContentService) getApplicationContext().getBean("ContentService");
        this.channelHelper = (ChannelHelper) getApplicationContext().getBean("channelHelper");
        this.action = (PublishEventAction) getApplicationContext().getBean("pub_publishEvent");

        this.channelType = testHelper.mockChannelType(channelTypeId);
        this.channel = testHelper.createChannel(channelTypeId);
        this.channelNode = new NodeRef(channel.getId());
    }

}
