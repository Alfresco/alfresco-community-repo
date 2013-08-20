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

package org.alfresco.repo.web.scripts.publishing;

import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.CAN_PUBLISH;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.CAN_PUBLISH_STATUS_UPDATES;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.CAN_UNPUBLISH;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.CHANNEL;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.CHANNEL_AUTH_STATUS;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.CHANNEL_ID;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.CHANNEL_IDS;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.CHANNEL_NODE_TYPE;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.CHANNEL_TYPE;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.COMMENT;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.CREATED_TIME;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.CREATOR;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.ICON;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.ID;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.MAX_STATUS_LENGTH;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.MESSAGE;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.NAME;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.NODE_REF;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.PUBLISHING_CHANNELS;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.PUBLISH_NODES;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.SCHEDULED_TIME;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.STATUS;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.STATUS_UPDATE;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.STATUS_UPDATE_CHANNELS;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.SUPPORTED_CONTENT_TYPES;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.SUPPORTED_MIME_TYPES;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.TITLE;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.UNPUBLISH_NODES;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.URL;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.URL_LENGTH;
import static org.alfresco.repo.web.scripts.publishing.PublishingWebScriptConstants.VERSION;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.publishing.AbstractChannelType;
import org.alfresco.repo.publishing.ChannelHelper;
import org.alfresco.repo.publishing.ChannelServiceImpl;
import org.alfresco.repo.publishing.PublishServiceImpl;
import org.alfresco.repo.publishing.PublishingTestHelper;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.person.TestPersonManager;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.repo.web.scripts.WebScriptUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.publishing.NodeSnapshot;
import org.alfresco.service.cmr.publishing.PublishingDetails;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingPackageEntry;
import org.alfresco.service.cmr.publishing.PublishingService;
import org.alfresco.service.cmr.publishing.Status;
import org.alfresco.service.cmr.publishing.StatusUpdate;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.GUID;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Function;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * @author Nick Smith
 * @since 4.0
 */
public class PublishingRestApiTest extends BaseWebScriptTest
{
    private static final String publishPdfType = "publishPdfForTest";
    private static final String publishAnyType = "publishAnyForTest";
    private static final String statusUpdateType = "statusUpdateForTest";
    private static final int maxStatusLength = 100;
    
    private static final String CHANNELS_URL = "api/publishing/channels";
    private static final String CHANNEL_URL = "api/publishing/channels/{0}";
    private static final String CHANNELS_NODE_URL = "api/publishing/{0}/{1}/{2}/channels";
    private static final String CHANNEL_TYPES_URL = "api/publishing/channel-types";
    private static final String PUBLISHING_QUEUE_URL = "api/publishing/queue";
    private static final String PUBLISHING_EVENTS_URL = "api/publishing/{0}/{1}/{2}/events";
    
    private static final String JSON = "application/json";
    
    private NodeService nodeService;
    private PermissionService permissionService;
    private ChannelService channelService;
    private PublishingService publishingService;
    private ChannelHelper channelHelper;
    private PublishingTestHelper testHelper;
    TestPersonManager personManager;
    private String userName = GUID.generate();
    

    public void testGetChannels() throws Exception
    {
        Channel publishAnyChannel = testHelper.createChannel(publishAnyType);
        Channel publishPdfChannel = testHelper.createChannel(publishPdfType);
        Channel statusUpdateChannel=testHelper. createChannel(statusUpdateType);
        
        // Call channels defined.
        Response response = sendRequest(new GetRequest(CHANNELS_URL), 200);
        JSONObject data = getJsonData(response);
        
        assertEquals(21, data.getInt(URL_LENGTH));
        JSONArray publishingChannels = data.getJSONArray(PUBLISHING_CHANNELS);
        JSONArray statusChannels = data.getJSONArray(STATUS_UPDATE_CHANNELS);
        
        checkChannels(publishingChannels, publishAnyChannel, publishPdfChannel);
        checkChannels(statusChannels, statusUpdateChannel);
        
        personManager.setUser(userName);
        response = sendRequest(new GetRequest(CHANNELS_URL), 200);
        data = getJsonData(response);
        publishingChannels = data.getJSONArray(PUBLISHING_CHANNELS);
        statusChannels = data.getJSONArray(STATUS_UPDATE_CHANNELS);
        
        checkChannels(publishingChannels, publishAnyChannel, publishPdfChannel);
        checkChannels(statusChannels, statusUpdateChannel);
    }

    public void testGetChannelsForNode() throws Exception
    {
        NodeRef textNode = testHelper.createContentNode("plainContent", "Some plain text", MimetypeMap.MIMETYPE_TEXT_PLAIN);
        permissionService.setPermission(textNode, userName, PermissionService.READ, true);
        
        File pdfFile = AbstractContentTransformerTest.loadQuickTestFile("pdf");
        assertNotNull("Failed to load required test file.", pdfFile);
        NodeRef xmlNode = testHelper.createContentNode("xmlContent", pdfFile, MimetypeMap.MIMETYPE_PDF);
        StoreRef store = textNode.getStoreRef();
        
        String plainTextNodeUrl = MessageFormat.format(CHANNELS_NODE_URL, store.getProtocol(), store.getIdentifier(), textNode.getId() );
        
        Channel publishAnyChannel = testHelper.createChannel(publishAnyType);
        Channel publishPdfChannel = testHelper.createChannel(publishPdfType);
        Channel statusUpdateChannel= testHelper.createChannel(statusUpdateType);
        
        // Call with channels defined.
        Response response = sendRequest(new GetRequest(plainTextNodeUrl), 200);
        JSONObject data = getJsonData(response);
        
        //TODO Fix hard coding.
        assertEquals(21, data.getInt(URL_LENGTH));
        JSONArray publishingChannels = data.getJSONArray(PUBLISHING_CHANNELS);
        JSONArray statusChannels = data.getJSONArray(STATUS_UPDATE_CHANNELS);
        
        checkChannels(publishingChannels, publishAnyChannel);
        checkChannels(statusChannels, statusUpdateChannel);
        
        String xmlNodeUrl = MessageFormat.format(CHANNELS_NODE_URL, store.getProtocol(), store.getIdentifier(), xmlNode.getId() );
        response = sendRequest(new GetRequest(xmlNodeUrl), 200);
        data = getJsonData(response);

        assertEquals(21, data.getInt(URL_LENGTH));
        publishingChannels = data.getJSONArray(PUBLISHING_CHANNELS);
        statusChannels = data.getJSONArray(STATUS_UPDATE_CHANNELS);
        
        checkChannels(publishingChannels, publishAnyChannel, publishPdfChannel);
        checkChannels(statusChannels, statusUpdateChannel);
        
        personManager.setUser(userName);
        response = sendRequest(new GetRequest(plainTextNodeUrl), 200);
        data = getJsonData(response);
        publishingChannels = data.getJSONArray(PUBLISHING_CHANNELS);
        statusChannels = data.getJSONArray(STATUS_UPDATE_CHANNELS);
        
        assertEquals(0, publishingChannels.length());
        assertEquals(0, statusChannels.length());

        testHelper.allowChannelAccess(userName, publishAnyChannel.getId());
        testHelper.allowChannelAccess(userName, publishPdfChannel.getId());
        testHelper.allowChannelAccess(userName, statusUpdateChannel.getId());

        personManager.setUser(userName);
        response = sendRequest(new GetRequest(plainTextNodeUrl), 200);
        data = getJsonData(response);
        publishingChannels = data.getJSONArray(PUBLISHING_CHANNELS);
        statusChannels = data.getJSONArray(STATUS_UPDATE_CHANNELS);
        
        checkChannels(publishingChannels, publishAnyChannel);
        checkChannels(statusChannels, statusUpdateChannel);
    }
    
    public void testChannelPut() throws Exception
    {
        Channel channel1 = testHelper.createChannel(publishAnyType);
        Channel channel2 = testHelper.createChannel(publishAnyType);
        
        String name1 = channel1.getName();
        String name2 = channel2.getName();
        
        String newName = name1 + "Foo";
        JSONObject json = new JSONObject();
        json.put(NAME, newName);
        
        String jsonStr = json.toString();
        
        String channel1Url = MessageFormat.format(CHANNEL_URL, URLEncoder.encode(channel1.getId()));
        // Post JSON content.
        sendRequest(new PutRequest(channel1Url, jsonStr, JSON), 200);
        
        Channel renamedCH1 = channelService.getChannelById(channel1.getId());
        assertEquals("Channel1 was not renamed correctly!", newName, renamedCH1.getName());
        
        Channel renamedCH2 = channelService.getChannelById(channel2.getId());
        assertEquals("Channel2 name should not have changed!", name2, renamedCH2.getName());
    }
    
    public void testPublishingQueuePost() throws Exception
    {
        // Create some content.
        NodeRef textNode = testHelper.createContentNode("plainContent", "Some plain text", MimetypeMap.MIMETYPE_TEXT_PLAIN);
        try
        {
            checkPublishingQueuePost(textNode);
        }
        finally
        {
            // Clean up events
            List<PublishingEvent> events = publishingService.getPublishEventsForNode(textNode);
            List<String> ids = CollectionUtils.transform(events, new Function<PublishingEvent, String>()
            {
                public String apply(PublishingEvent value)
                {
                    return value.getId();
                }
            });
            testHelper.addEvents(ids);
        }
    }

    @SuppressWarnings("unchecked")
    private void checkPublishingQueuePost(NodeRef textNode) throws Exception
    {
        // Create publish and status update channels.
        Channel publishChannel = testHelper.createChannel(publishAnyType);
        Channel statusChannel = testHelper.createChannel(statusUpdateType);

        // Post empty content.
        sendRequest(new PostRequest(PUBLISHING_QUEUE_URL, "", JSON), 400);
        
        String comment = "The comment";
        String statusMessage = "The status message";

        JSONObject json = buildScheduleEventJson(textNode, publishChannel, comment, statusMessage, true, statusChannel);
        
        String jsonStr = json.toString();

        // Post JSON content.
        sendRequest(new PostRequest(PUBLISHING_QUEUE_URL, jsonStr, JSON), 200);

        List<PublishingEvent> publishedEvents = publishingService.getPublishEventsForNode(textNode);
        
        assertEquals(1, publishedEvents.size());
        
        PublishingEvent event = publishedEvents.get(0);
        assertEquals(publishChannel.getId(), event.getChannelId());
        assertEquals(comment, event.getComment());
        assertEquals(Status.SCHEDULED, event.getStatus());
        
        // Check Package
        PublishingPackage pckg = event.getPackage();
        Set<NodeRef> toPublish = pckg.getNodesToPublish();
        assertEquals(1, toPublish.size());
        assertTrue(toPublish.contains(textNode));
        assertTrue(pckg.getNodesToUnpublish().isEmpty());
        
        // Check StatusUpdate
        StatusUpdate statusUpdate = event.getStatusUpdate();
        assertEquals(statusMessage, statusUpdate.getMessage());
        assertEquals(textNode, statusUpdate.getNodeToLinkTo());
        Set<String> channelIds = statusUpdate.getChannelIds();
        assertEquals(1, channelIds.size());
        assertTrue(channelIds.contains(statusChannel.getId()));
        
        // Wait for Publishing Event to execute asynchronously
        Thread.sleep(5000);
        
        AbstractChannelType publishAnyChannelType = (AbstractChannelType) channelService.getChannelType(publishAnyType);
        AbstractChannelType statusUpdateChannelType = (AbstractChannelType) channelService.getChannelType(statusUpdateType);
        
        NodeRef mappedTextNode = channelHelper.mapSourceToEnvironment(textNode, publishChannel.getNodeRef());
        
        // Check publish is called.
        verify(publishAnyChannelType)
            .publish(eq(mappedTextNode), anyMap());

        // Check updateStatus is called correctly.
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(statusUpdateChannelType)
        .sendStatusUpdate(any(Channel.class), captor.capture());
        String actualStatusMessage = captor.getValue();
        assertTrue(actualStatusMessage.startsWith(statusMessage));
        
        verify(statusUpdateChannelType, never()).publish(any(NodeRef.class), anyMap());
        verify(publishAnyChannelType, never()).sendStatusUpdate(any(Channel.class), anyString());
        
        JSONObject status = json.optJSONObject(STATUS_UPDATE);
        status.remove(NODE_REF);
        jsonStr = json.toString();
        
        // Post JSON without NodeRef in status.
        sendRequest(new PostRequest(PUBLISHING_QUEUE_URL, jsonStr, JSON), 200);
        
        json.remove(STATUS_UPDATE);
        jsonStr = json.toString();
        
        // Post JSON without Status Update.
        sendRequest(new PostRequest(PUBLISHING_QUEUE_URL, jsonStr, JSON), 200);

        // Wait for Publishing Events to execute asynchronously
        Thread.sleep(5000);

        // Check published node still exists.
        assertTrue(nodeService.exists(mappedTextNode));

        // Check unpublish hasn't been called yet.
        verify(publishAnyChannelType, never())
            .unpublish(eq(mappedTextNode), anyMap());

        // Create unpublish Json
        json = buildScheduleEventJson(textNode, publishChannel, comment, null, false);
        jsonStr = json.toString();
        
        // Post Unpublish JSON.
        sendRequest(new PostRequest(PUBLISHING_QUEUE_URL, jsonStr, JSON), 200);

        // Wait for unpublishing Event to execute asynchronously
        Thread.sleep(5000);

        // Check published node has been deleted.
        assertFalse(nodeService.exists(mappedTextNode));
        NodeRef remappedTextNode = channelHelper.mapSourceToEnvironment(textNode, publishChannel.getNodeRef());
        assertNull(remappedTextNode);
        
        // Check unpublish was called
        verify(publishAnyChannelType)
            .unpublish(eq(mappedTextNode), anyMap());
        
        // Check can get unpublish event.
        String protocol = textNode.getStoreRef().getProtocol();
        String storeId = textNode.getStoreRef().getIdentifier();
        String nodeId = textNode.getId();
        String textNodeUrl = MessageFormat.format(PUBLISHING_EVENTS_URL, protocol, storeId, nodeId);

        // Get events on textNode1 before any events created.
        Response response = sendRequest(new GetRequest(textNodeUrl), 200);
        JSONArray data = getDataArray(response);

        List<PublishingEvent> unpublishEvents = publishingService.getUnpublishEventsForNode(textNode);
        assertEquals(1, unpublishEvents.size());
        PublishingEvent unpublishedEvent = unpublishEvents.get(0);
        
        checkContainsEvent(data, unpublishedEvent.getId());
    }

    public void testPublishingEventsGet() throws Exception
    {
        Channel publishChannel = testHelper.createChannel(publishAnyType);
        NodeRef textNode1 = testHelper.createContentNode("plain1.txt", "This is some plain text", MimetypeMap.MIMETYPE_TEXT_PLAIN);
        NodeRef textNode2 = testHelper.createContentNode("plain2.txt", "This is some more plain text", MimetypeMap.MIMETYPE_TEXT_PLAIN);
        
        String protocol = textNode1.getStoreRef().getProtocol();
        String storeId = textNode1.getStoreRef().getIdentifier();
        String nodeId1 = textNode1.getId();
        String textNode1Url = MessageFormat.format(PUBLISHING_EVENTS_URL, protocol, storeId, nodeId1);

        // Get events on textNode1 before any events created.
        Response response = sendRequest(new GetRequest(textNode1Url), 200);
        JSONArray data = getDataArray(response);
        assertEquals(0, data.length());
        
        // Create publishing event for textNode1.
        String comment = "This is a comment";
        PublishingDetails details = publishingService.createPublishingDetails()
            .setPublishChannelId(publishChannel.getId())
            .addNodesToPublish(textNode1)
            .setComment(comment);
        
        String event1Id = testHelper.scheduleEvent1Year(details);
        
        // Query for events on textNode1.
        response = sendRequest(new GetRequest(textNode1Url), 200);
        data = getDataArray(response);
        checkContainsEvents(data, event1Id);
        
        // Query for events on textNode2.
        String nodeId2 = textNode2.getId();
        String textNode2Url = MessageFormat.format(PUBLISHING_EVENTS_URL, protocol, storeId, nodeId2);
        response = sendRequest(new GetRequest(textNode2Url), 200);
        data = getDataArray(response);
        assertEquals(0, data.length());
        
        // Change to non-Admin user.
        personManager.setUser(userName);
        // Query for events on textNode1.
        response = sendRequest(new GetRequest(textNode1Url), 200);
        data = getDataArray(response);
        checkContainsEvents(data, event1Id);
        
        // Query for events on textNode2.
        response = sendRequest(new GetRequest(textNode2Url), 200);
        data = getDataArray(response);
        assertEquals(0, data.length());
        
    }
    
    public void testChannelTypesGet() throws Exception
    {
        Response response = sendRequest(new GetRequest(CHANNEL_TYPES_URL), 200);
        JSONArray data = getDataArray(response);
        checkChannelTypes(data, channelService.getChannelTypes());
    }
    
    private void checkChannelTypes(JSONArray data, List<ChannelType> channelTypes) throws Exception
    {
        assertEquals(channelTypes.size(), data.length());
        for (ChannelType type : channelTypes)
        {
            checkContainsChannelType(data, type);
        }
    }

    private void checkContainsChannelType(JSONArray data, ChannelType type) throws Exception
    {
        String typeId = type.getId();
        for (int i = 0; i < data.length(); i++)
        {
            JSONObject json = data.optJSONObject(i);
            if (typeId.equals(json.optString(ID)))
            {
                checkChannelType(json, type);
                return;
            }
        }
        fail("Failed to find Channel Type: " + typeId);
    }

    private void checkContainsEvents(JSONArray data, String... eventIds) throws Exception
    {
        assertEquals(eventIds.length, data.length());
        for (String eventId : eventIds)
        {
            checkContainsEvent(data, eventId);
        }
    }

    private void checkContainsEvent(JSONArray data, String eventId) throws Exception
    {
        for (int i = 0; i < data.length(); i++)
        {
            JSONObject json = data.optJSONObject(i);
            if (eventId.equals(json.optString(ID)))
            {
                PublishingEvent event = publishingService.getPublishingEvent(eventId);
                checkJsonEvent(event, json);
                return;
            }
        }
        fail("Failed to find Publishing Event: " + eventId);
    }

    private void checkJsonEvent(PublishingEvent event, JSONObject json) throws Exception
    {
        String url = "api/publishing/events/" + URLEncoder.encode(event.getId());
        assertEquals(url, json.getString(URL));
        
        assertEquals(event.getStatus().name(), json.getString(STATUS));
        
        assertEquals(event.getComment(), json.optString(COMMENT));
        checkCalendar(event.getScheduledTime(), json.optJSONObject(SCHEDULED_TIME));
        assertEquals(event.getCreator(), json.getString(CREATOR));
        checkDate(event.getCreatedTime(), json.getJSONObject(CREATED_TIME));
        
        PublishingPackage pckg = event.getPackage();
        checkContainsNodes(pckg, json.getJSONArray(PUBLISH_NODES), true);
        checkContainsNodes(pckg, json.getJSONArray(UNPUBLISH_NODES), false);
        
        Channel channel = channelService.getChannelById(event.getChannelId());
        checkChannel(json.getJSONObject(CHANNEL), channel);
    }

    private void checkContainsNodes(PublishingPackage pckg, JSONArray json, boolean isPublish) throws JSONException
    {
        Collection<NodeRef> nodes = isPublish ? pckg.getNodesToPublish() : pckg.getNodesToUnpublish();
        checkContainsNodes(nodes, pckg.getEntryMap(), json);
    }

    private void checkContainsNodes(Collection<NodeRef> nodes, Map<NodeRef, PublishingPackageEntry> entryMap, JSONArray json) throws JSONException
    {
        assertEquals(nodes.size(), json.length());
        for (NodeRef node : nodes)
        {
            checkContainsNode(entryMap.get(node), json);
        }
    }

    private void checkContainsNode(PublishingPackageEntry entry, JSONArray jsonArray) throws JSONException
    {
        String nodeId = entry.getNodeRef().toString();
        for (int i = 0; i < jsonArray.length(); i++)
        {
            JSONObject json = jsonArray.getJSONObject(i);
            if (nodeId.equals(json.getString(NODE_REF)))
            {
                checkNode(entry, json);
                return;
            }
        }
        fail("NodeRef was not found!");
    }

    private void checkNode(PublishingPackageEntry entry, JSONObject json) throws JSONException
    {
        NodeSnapshot snapshot = entry.getSnapshot();
        String version = snapshot.getVersion();
        if (version != null && version.isEmpty() == false)
        {
            assertEquals(version, json.getString(VERSION));
        }
        String name = (String) snapshot.getProperties().get(ContentModel.PROP_NAME);
        if (name != null && name.isEmpty() == false)
        {
            assertEquals(name, json.getString(NAME));
        }
    }

    private void checkCalendar(Calendar calendar, JSONObject json) throws JSONException
    {
        checkDate(calendar.getTime(), json);
        String timeZone = calendar.getTimeZone().getID();
        assertEquals(timeZone, json.getString(WebScriptUtil.TIME_ZONE));
    }

    private void checkDate(Date date, JSONObject json) throws JSONException
    {
        assertEquals(WebScriptUtil.ISO8601, json.getString(WebScriptUtil.FORMAT));
        String dateStr = json.getString(WebScriptUtil.DATE_TIME);
        Date actualDate = ISO8601DateFormat.parse(dateStr);
        assertEquals(date, actualDate);
    }

    private JSONObject buildScheduleEventJson(NodeRef node, Channel publishChannel,
            String comment, String statusMessage,
            boolean publish, Channel... statusChannels) throws JSONException
    {
        JSONObject json = new JSONObject();
        json.put(CHANNEL_ID, publishChannel.getId());
        json.put(COMMENT, comment);
        Collection<String> publishNodes = Collections.singleton(node.toString());
        if (publish)
        {
            json.put(PUBLISH_NODES, publishNodes);
        }
        else
        {
            json.put(UNPUBLISH_NODES, publishNodes);
        }
        if (statusMessage != null)
        {
            json.put(STATUS_UPDATE, buildStatusUpdate(statusMessage, node, statusChannels));
        }
        return json;
    }

    private JSONObject buildStatusUpdate(String message, NodeRef textNode, Channel... theChannels) throws JSONException
    {
        Function<Channel, String> transformer = new Function<Channel, String>()
        {
            public String apply(Channel channel)
            {
                return channel.getId();
            }
        };
        List<String> ids = CollectionUtils.transform(transformer, theChannels);
            
        JSONObject statusUpdate = new JSONObject();
        statusUpdate.put(MESSAGE, message);
        statusUpdate.put(NODE_REF, textNode.toString());
        statusUpdate.put(CHANNEL_IDS, ids);
        return statusUpdate;
    }

    private void checkChannels(JSONArray json, Channel... theChannels)throws Exception
    {
        for (Channel channel : theChannels)
        {
            checkContainsChannel(json, channel);
        }
    }
    
    private void checkContainsChannel(JSONArray json, Channel channel) throws Exception
    {
        for (int i = 0; i < json.length(); i++)
        {
            JSONObject jsonChannel = json.getJSONObject(i);
            String name = jsonChannel.getString(NAME);
            if (channel.getName().equals(name))
            {
                checkChannel(jsonChannel, channel);
                return;
            }
        }
        fail("Json did not contain channel: " + channel.getName());
    }

    private void checkChannel(JSONObject jsonChannel, Channel channel) throws Exception
    {
        NodeRef node = channel.getNodeRef();
        StoreRef storeRef = node.getStoreRef();
        check(ID, jsonChannel, channel.getId());
        String expUrl = "api/publishing/channels/"
            + storeRef.getProtocol() + "/"
            + storeRef.getIdentifier() + "/"
            + node.getId();
        check(URL, jsonChannel, expUrl);
        check(TITLE, jsonChannel, channel.getName());
        check(CAN_PUBLISH, jsonChannel, channel.canPublish());
        check(CAN_UNPUBLISH, jsonChannel, channel.canUnpublish());
        check(CAN_PUBLISH_STATUS_UPDATES, jsonChannel, channel.canPublishStatusUpdates());
        check(CHANNEL_AUTH_STATUS, jsonChannel, channel.isAuthorised());
        
        JSONObject jsonType = jsonChannel.getJSONObject(CHANNEL_TYPE);
        assertNotNull("The channel type is null!", jsonType);
        checkChannelType(jsonType, channel.getChannelType());
    }

    private void checkChannelType(JSONObject jsonType, ChannelType channelType) throws Exception
    {
        check(ID, jsonType, channelType.getId());
        check(TITLE, jsonType, channelType.getId());
        
        String expUrl = "api/publishing/channel-types/"+URLEncoder.encode(channelType.getId());
        check(URL, jsonType, expUrl);
        check(CHANNEL_NODE_TYPE, jsonType, channelType.getChannelNodeType().toString());
        
        List<String> contentTypes = CollectionUtils.toListOfStrings(channelType.getSupportedContentTypes());
        checkStrings(jsonType.getJSONArray(SUPPORTED_CONTENT_TYPES), contentTypes);
        checkStrings(jsonType.getJSONArray(SUPPORTED_MIME_TYPES), channelType.getSupportedMimeTypes());
        
        check(CAN_PUBLISH, jsonType, channelType.canPublish());
        check(CAN_PUBLISH_STATUS_UPDATES, jsonType, channelType.canPublishStatusUpdates());
        check(CAN_UNPUBLISH, jsonType, channelType.canUnpublish());
        check(MAX_STATUS_LENGTH, jsonType, channelType.getMaximumStatusLength());

        //TODO Implement Icon URL
        check(ICON, jsonType, expUrl + "/icon");
    }

    private void check(String key, JSONObject json, Object exp)
    {
        assertEquals("Comparing "+key, exp, json.opt(key));
    }
    
    private void checkStrings(JSONArray json, Collection<String> strings) throws Exception
    {
        assertEquals(strings.size(), json.length());
        for (String string : strings)
        {
            checkContains(json, string);
        }
    }

    private void checkContains(JSONArray json, String string) throws Exception
    {
        for (int i = 0; i < json.length(); i++)
        {
            if (string.equals(json.getString(i)))
            {
                return;
            }
        }
        fail("Did not contain " + string);
    }

    private JSONObject getJsonData(Response response) throws Exception
    {
        JSONObject json = getJson(response);
        JSONObject data = json.getJSONObject("data");
        assertNotNull("Data was null!", data);
        return data;
    }

    public JSONArray getDataArray(Response response) throws Exception
    {
        JSONObject json = getJson(response);
        JSONArray data = json.getJSONArray("data");
        assertNotNull("Data was null!", data);
        return data;
    }

    private JSONObject getJson(Response response) throws UnsupportedEncodingException, JSONException
    {
        String jsonStr = response.getContentAsString();
        assertNotNull("The JSON is null!", jsonStr);
        return new JSONObject(jsonStr);
    }
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ApplicationContext ctx = getServer().getApplicationContext();
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        SiteService siteService = serviceRegistry.getSiteService();
        FileFolderService fileFolderService = serviceRegistry.getFileFolderService();
        this.nodeService = serviceRegistry.getNodeService();
        this.permissionService= serviceRegistry.getPermissionService();
        this.channelService = (ChannelService) ctx.getBean(ChannelServiceImpl.NAME);
        this.publishingService= (PublishingService) ctx.getBean(PublishServiceImpl.NAME);
        this.channelHelper = (ChannelHelper) ctx.getBean(ChannelHelper.NAME);
        this.testHelper = new PublishingTestHelper(channelService, publishingService, siteService, fileFolderService, permissionService);
        
        MutableAuthenticationService authenticationService = serviceRegistry.getAuthenticationService();
        PersonService personService = serviceRegistry.getPersonService();
        this.personManager = new TestPersonManager(authenticationService, personService, nodeService);

        personManager.createPerson(userName);
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        ChannelType publishAny = testHelper.mockChannelType(publishAnyType);
        when(publishAny.canPublish()).thenReturn(true);
        when(publishAny.canUnpublish()).thenReturn(true);
        
        ChannelType publishPdf= testHelper.mockChannelType(publishPdfType);
        when(publishPdf.canPublish()).thenReturn(true);
        when(publishPdf.getSupportedMimeTypes()).thenReturn(Collections.singleton(MimetypeMap.MIMETYPE_PDF));
        
        ChannelType statusUpdate= testHelper.mockChannelType(statusUpdateType);
        when(statusUpdate.canPublishStatusUpdates()).thenReturn(true);
        when(statusUpdate.getMaximumStatusLength()).thenReturn(maxStatusLength);
    }
    
    @Override
    public void tearDown() throws Exception
    {
        try 
        {
            testHelper.tearDown();
        }
        finally
        {
            super.tearDown();
        }
    }
}
