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
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.alfresco.model.ContentModel.ASPECT_GEOGRAPHIC;
import static org.alfresco.model.ContentModel.ASPECT_TEMPORARY;
import static org.alfresco.model.ContentModel.PROP_CONTENT;
import static org.alfresco.model.ContentModel.PROP_LATITUDE;
import static org.alfresco.model.ContentModel.PROP_LONGITUDE;
import static org.alfresco.model.ContentModel.PROP_NAME;
import static org.alfresco.model.ContentModel.TYPE_CONTENT;
import static org.alfresco.repo.publishing.PublishingModel.ASSOC_LAST_PUBLISHING_EVENT;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.publishing.MutablePublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingService;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class PublishEventActionTest extends AbstractPublishingIntegrationTest
{
    private static final String channelName = "Channel1";
    private static final String contentNodeName = "TheName";
    private static final String content = "The quick brown fox jumped over the lazy dog";
    
    @Resource(name="publishingService")
    private PublishingService publishingService;
    
    @Resource(name="channelService")
    private ChannelService channelService;
    
    @Resource(name="contentService")
    private ContentService contentService;
    
    @Autowired
    private ChannelHelper channelHelper;

    @Autowired
    private PublishEventAction action;

    private NodeRef root;
    private NodeRef channel;
    private String eventId;

    @Test
    public void testPublishNodes() throws Exception
    {
        // Create content node with appropriate aspects added.
        NodeRef source = createContentNode(contentNodeName, content);
        
        double lattitude = 0.25;
        double longtitude = 0.75;
        addGeographicAspect(source, lattitude, longtitude);
        nodeService.addAspect(source, ASPECT_TEMPORARY, null);
        
        NodeRef publishEventNode = publishNode(source);
        
        // Check published node exists and is in correct place.
        NodeRef publishedNode = channelHelper.mapSourceToEnvironment(source, channel);
        assertNotNull(publishedNode);
        assertTrue(nodeService.exists(publishedNode));
        assertEquals(root, nodeService.getPrimaryParent(publishedNode).getParentRef());
        
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
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(publishedNode, ASSOC_LAST_PUBLISHING_EVENT, RegexQNamePattern.MATCH_ALL);
        assertEquals(1, assocs.size());
        assertEquals(publishEventNode, assocs.get(0).getChildRef());
    }

    public void testUpdatePublishedNode() throws Exception
    {
        // Create content node without aspects
        NodeRef source = createContentNode(contentNodeName, content);
        NodeRef publishEventNode = publishNode(source);

        // Check published node exists
        NodeRef publishedNode = channelHelper.mapSourceToEnvironment(source, channel);
        assertNotNull(publishedNode);
        assertTrue(nodeService.exists(publishedNode));

        // Verify properties set correctly.
        Map<QName, Serializable> publishedProps = nodeService.getProperties(publishedNode);
        assertNull(publishedProps.containsKey(PROP_LATITUDE));
        assertNull(publishedProps.containsKey(PROP_LONGITUDE));
        assertEquals(contentNodeName, publishedProps.get(PROP_NAME));
        assertEquals(content, readContent(source));
        
        Set<QName> aspects = nodeService.getAspects(source);
        assertFalse(aspects.contains(ASPECT_GEOGRAPHIC));
        
        // Check lastPublishingEvent association is created.
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(publishedNode, ASSOC_LAST_PUBLISHING_EVENT, RegexQNamePattern.MATCH_ALL);
        assertEquals(1, assocs.size());
        assertEquals(publishEventNode, assocs.get(0).getChildRef());

        // Modify source node
        double lattitude = 0.25;
        double longtitude = 0.75;
        addGeographicAspect(source, lattitude, longtitude);
        
        String newName = "NewName";
        nodeService.setProperty(source, PROP_NAME, newName);
        String newContent = "The new content";
        writeContent(source, newContent);
        
        // Update published node.
        publishEventNode = publishNode(source);
        
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
        assocs = nodeService.getChildAssocs(publishedNode, ASSOC_LAST_PUBLISHING_EVENT, RegexQNamePattern.MATCH_ALL);
        assertEquals(1, assocs.size());
        assertEquals(publishEventNode, assocs.get(0).getChildRef());

        // Remove aspect from source node.
        nodeService.removeAspect(source, ASPECT_GEOGRAPHIC);
        
        // Update publish node
        publishNode(source);
        
        aspects = nodeService.getAspects(source);
        assertFalse(aspects.contains(ASPECT_GEOGRAPHIC));
        
        publishedProps = nodeService.getProperties(publishedNode);
        assertFalse(publishedProps.containsKey(PROP_LATITUDE));
        assertFalse(publishedProps.containsKey(PROP_LONGITUDE));
    }
    
    private NodeRef publishNode(NodeRef source)
    {
        MutablePublishingPackage pckg = queue.createPublishingPackage();
        pckg.addNodesToPublish(source);
        scheduleEvent(pckg);
        
        assertNotNull(eventId);
        NodeRef eventNode = new NodeRef(eventId);
        assertTrue(nodeService.exists(eventNode));
        
        action.executeImpl(null, eventNode);
        return eventNode;
    }

    private void scheduleEvent(PublishingPackage publishPckg)
    {
        Calendar schedule = Calendar.getInstance();
        schedule.add(Calendar.YEAR, 1);
        this.eventId = queue.scheduleNewEvent(publishPckg, channelName, schedule, null);
    }

    private void addGeographicAspect(NodeRef source, double lattitude, double longtitude)
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(PROP_LATITUDE, lattitude);
        props.put(PROP_LONGITUDE, longtitude);
        nodeService.addAspect(source, ASPECT_GEOGRAPHIC, props);
    }

    private NodeRef createContentNode(String name, String theContent)
    {
        NodeRef source = fileFolderService.create(docLib, name, TYPE_CONTENT).getNodeRef();
        writeContent(source, theContent);
        return source;
    }

    /**
     * @param source
     * @param theContent
     */
    private void writeContent(NodeRef source, String theContent)
    {
        ContentWriter writer = contentService.getWriter(source, PROP_CONTENT, true);
        writer.setEncoding("UTF-8");
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.putContent(theContent);
    }
    
    private String readContent(NodeRef source)
    {
        ContentReader reader = contentService.getReader(source, PROP_CONTENT);
        return reader.getContentString();
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        ChannelType channelType = mockChannelType();
        channelService.register(channelType);
        channelService.createChannel(siteId, channelTypeId, channelName, null);
        
        this.channel = channelHelper.getChannelNodeForEnvironment(environment.getNodeRef(), channelName);
        this.root = channelHelper.getChannelRootNode(channel);
        assertNotNull(root);
    }

    @Override
    public void tearDown()
    {
        if(eventId !=null)
        {
            publishingService.cancelPublishingEvent(eventId);
        }
        super.tearDown();
    }
}
