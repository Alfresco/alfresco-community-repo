/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

import static org.alfresco.model.ContentModel.TYPE_CONTENT;
import static org.alfresco.repo.publishing.PublishingModel.TYPE_DELIVERY_CHANNEL;
import static org.alfresco.repo.web.scripts.publishing.PublishingJsonParser.CHANNEL_NAME;
import static org.alfresco.repo.web.scripts.publishing.PublishingJsonParser.CHANNEL_NAMES;
import static org.alfresco.repo.web.scripts.publishing.PublishingJsonParser.COMMENT;
import static org.alfresco.repo.web.scripts.publishing.PublishingJsonParser.MESSAGE;
import static org.alfresco.repo.web.scripts.publishing.PublishingJsonParser.NODE_REF;
import static org.alfresco.repo.web.scripts.publishing.PublishingJsonParser.PUBLISH_NODES;
import static org.alfresco.repo.web.scripts.publishing.PublishingJsonParser.STATUS_UPDATE;
import static org.alfresco.repo.web.scripts.publishing.PublishingModelBuilder.CAN_PUBLISH;
import static org.alfresco.repo.web.scripts.publishing.PublishingModelBuilder.CAN_PUBLISH_STATUS_UPDATES;
import static org.alfresco.repo.web.scripts.publishing.PublishingModelBuilder.CAN_UNPUBLISH;
import static org.alfresco.repo.web.scripts.publishing.PublishingModelBuilder.CHANNEL_NODE_TYPE;
import static org.alfresco.repo.web.scripts.publishing.PublishingModelBuilder.CHANNEL_TYPE;
import static org.alfresco.repo.web.scripts.publishing.PublishingModelBuilder.ICON;
import static org.alfresco.repo.web.scripts.publishing.PublishingModelBuilder.ID;
import static org.alfresco.repo.web.scripts.publishing.PublishingModelBuilder.MAX_STATUS_LENGTH;
import static org.alfresco.repo.web.scripts.publishing.PublishingModelBuilder.NAME;
import static org.alfresco.repo.web.scripts.publishing.PublishingModelBuilder.SUPPORTED_CONTENT_TYPES;
import static org.alfresco.repo.web.scripts.publishing.PublishingModelBuilder.SUPPORTED_MIME_TYPES;
import static org.alfresco.repo.web.scripts.publishing.PublishingModelBuilder.TITLE;
import static org.alfresco.repo.web.scripts.publishing.PublishingModelBuilder.URL;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.publishing.ChannelHelper;
import org.alfresco.repo.publishing.ChannelServiceImpl;
import org.alfresco.repo.publishing.PublishingObjectFactory;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.publishing.Environment;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingEvent.Status;
import org.alfresco.service.cmr.publishing.PublishingEventFilter;
import org.alfresco.service.cmr.publishing.PublishingPackage;
import org.alfresco.service.cmr.publishing.StatusUpdate;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;
import org.alfresco.util.collections.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class PublishingRestApiTest extends BaseWebScriptTest
{
    private static final String environmentName = "live";
    private static final String publishPdfType = "publishPdfForTest";
    private static final String publishAnyType = "publishAnyForTest";
    private static final String statusUpdateType = "statusUpdateForTest";
    private static final int maxStatusLength = 100;
    
    private static final String CHANNELS_SITE_URL = "api/publishing/site/{0}/channels";
    private static final String CHANNELS_NODE_URL = "api/publishing/{0}/{1}/{2}/channels";
    private static final String PUBLISHING_QUEUE_URL = "api/publishing/{0}/queue";

    private static final String JSON = "application/json";
    
    private SiteService siteService;
    private FileFolderService fileFolderService;
    private ChannelService channelService;
    private ChannelHelper channelHelper;
    
    private Environment environment;
    private NodeRef docLib;
    private String siteId;

    public void testGetChannelsForNode() throws Exception
    {
        NodeRef textNode = createContentNode("plainContent", "Some plain text", MimetypeMap.MIMETYPE_TEXT_PLAIN);
        
        File pdfFile = AbstractContentTransformerTest.loadQuickTestFile("pdf");
        assertNotNull("Failed to load required test file.", pdfFile);
        NodeRef xmlNode = createContentNode("xmlContent", pdfFile, MimetypeMap.MIMETYPE_PDF);
        StoreRef store = textNode.getStoreRef();
        
        assertEquals(MimetypeMap.MIMETYPE_PDF, fileFolderService.getReader(xmlNode).getMimetype());
        String plainTextNodeUrl = MessageFormat.format(CHANNELS_NODE_URL, store.getProtocol(), store.getIdentifier(), textNode.getId() );
        Response response = sendRequest(new GetRequest(plainTextNodeUrl), 200);
        
        // Call with no channels defined on site.
        response = sendRequest(new GetRequest(plainTextNodeUrl), 200);
        JSONObject data = getJsonData(response);
        
        //TODO Fix hard coding.
        assertEquals(20, data.getInt(ChannelsGet.URL_LENGTH));
        JSONArray publishingChannels = data.getJSONArray(ChannelsGet.PUBLISHING_CHANNELS);
        assertEquals(0, publishingChannels.length());
        JSONArray statusChannels = data.getJSONArray(ChannelsGet.STATUS_UPDATE_CHANNELS);
        assertEquals(0, statusChannels.length());
        
        Channel publishAnyChannel = createChannel(publishAnyType);
        Channel publishPdfChannel = createChannel(publishPdfType);
        Channel statusUpdateChannel= createChannel(statusUpdateType);
        
        // Call with channels defined.
        response = sendRequest(new GetRequest(plainTextNodeUrl), 200);
        data = getJsonData(response);
        
        //TODO Fix hard coding.
        assertEquals(20, data.getInt(ChannelsGet.URL_LENGTH));
        publishingChannels = data.getJSONArray(ChannelsGet.PUBLISHING_CHANNELS);
        statusChannels = data.getJSONArray(ChannelsGet.STATUS_UPDATE_CHANNELS);
        
        checkChannels(publishingChannels, publishAnyChannel);
        checkChannels(statusChannels, statusUpdateChannel);
        
        String xmlNodeUrl = MessageFormat.format(CHANNELS_NODE_URL, store.getProtocol(), store.getIdentifier(), xmlNode.getId() );
        response = sendRequest(new GetRequest(xmlNodeUrl), 200);
        data = getJsonData(response);

        //TODO Fix hard coding.
        assertEquals(20, data.getInt(ChannelsGet.URL_LENGTH));
        publishingChannels = data.getJSONArray(ChannelsGet.PUBLISHING_CHANNELS);
        statusChannels = data.getJSONArray(ChannelsGet.STATUS_UPDATE_CHANNELS);
        
        checkChannels(publishingChannels, publishAnyChannel, publishPdfChannel);
        checkChannels(statusChannels, statusUpdateChannel);
    }
    
    public void testGetChannelsForSite() throws Exception
    {
        String badSiteUrl = MessageFormat.format(CHANNELS_SITE_URL, "foo");
        Response response = sendRequest(new GetRequest(badSiteUrl), 500);
        
        // Call with no channels defined on site.
        String siteUrl = MessageFormat.format(CHANNELS_SITE_URL, siteId);
        response = sendRequest(new GetRequest(siteUrl), 200);
        JSONObject data = getJsonData(response);
        
        //TODO Fix hard coding.
        assertEquals(20, data.getInt(ChannelsGet.URL_LENGTH));
        JSONArray publishingChannels = data.getJSONArray(ChannelsGet.PUBLISHING_CHANNELS);
        assertEquals(0, publishingChannels.length());
        JSONArray statusChannels = data.getJSONArray(ChannelsGet.STATUS_UPDATE_CHANNELS);
        assertEquals(0, statusChannels.length());
        
        Channel publishAnyChannel = createChannel(publishAnyType);
        Channel publishPdfChannel = createChannel(publishPdfType);
        Channel statusUpdateChannel= createChannel(statusUpdateType);
        
        // Call channels defined.
        response = sendRequest(new GetRequest(siteUrl), 200);
        data = getJsonData(response);
        
        //TODO Fix hard coding.
        assertEquals(20, data.getInt(ChannelsGet.URL_LENGTH));
        publishingChannels = data.getJSONArray(ChannelsGet.PUBLISHING_CHANNELS);
        statusChannels = data.getJSONArray(ChannelsGet.STATUS_UPDATE_CHANNELS);
        
        checkChannels(publishingChannels, publishAnyChannel, publishPdfChannel);
        checkChannels(statusChannels, statusUpdateChannel);
    }


    @SuppressWarnings("unchecked")
    public void testPublishingQueuePost() throws Exception
    {
        // Create publish and status update channels.
        Channel publishChannel = channelService.createChannel(siteId, publishAnyType, GUID.generate(), null);
        Channel statusChannel = channelService.createChannel(siteId, statusUpdateType, GUID.generate(), null);

        // Create some content.
        NodeRef textNode = createContentNode("plainContent", "Some plain text", MimetypeMap.MIMETYPE_TEXT_PLAIN);

        String pubQueueUrl = MessageFormat.format(PUBLISHING_QUEUE_URL, siteId);
        
        // Post empty content.
        sendRequest(new PostRequest(pubQueueUrl, "", JSON), 500);
        
        String comment = "The comment";
        String statusMessage = "The status message";

        JSONObject json = buildJson(textNode, publishChannel, comment, statusMessage, statusChannel);
        
        String jsonStr = json.toString();

        // Post JSON content.
        sendRequest(new PostRequest(pubQueueUrl, jsonStr, JSON), 200);

        PublishingEventFilter filter = environment.createPublishingEventFilter();
        List<PublishingEvent> events = environment.getPublishingEvents(filter);
        assertEquals(1, events.size());
        PublishingEvent event = events.get(0);
        assertEquals(publishChannel.getName(), event.getChannelName());
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
        Set<String> channelNames = statusUpdate.getChannelNames();
        assertEquals(1, channelNames.size());
        assertTrue(channelNames.contains(statusChannel.getName()));
        
        // Wait for Publishing Event to execute asynchronously
        Thread.sleep(3000);
        
        ChannelType publishAnyChannelType = channelService.getChannelType(publishAnyType);
        ChannelType statusUpdateChannelType = channelService.getChannelType(statusUpdateType);
        
        NodeRef environmentNode = new NodeRef(environment.getId());
        NodeRef mappedTextNode = channelHelper.mapSourceToEnvironment(textNode, environmentNode, publishChannel.getName());
        
        // Check publish is called.
        verify(publishAnyChannelType)
            .publish(eq(mappedTextNode), anyMap());

        // Check updateStatus is called correctly.
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(statusUpdateChannelType)
        .updateStatus(captor.capture(), anyMap());
        String actualStatusMessage = captor.getValue();
        assertTrue(actualStatusMessage.startsWith(statusMessage));
        
        verify(statusUpdateChannelType, never()).publish(any(NodeRef.class), anyMap());
        verify(publishAnyChannelType, never()).updateStatus(anyString(), anyMap());
        
        JSONObject status = json.optJSONObject(STATUS_UPDATE);
        status.remove(NODE_REF);
        jsonStr = json.toString();
        
        // Post JSON without NodeRef in status.
        sendRequest(new PostRequest(pubQueueUrl, jsonStr, JSON), 200);
        
        json.remove(STATUS_UPDATE);
        jsonStr = json.toString();
        
        // Post JSON without Status Update.
        sendRequest(new PostRequest(pubQueueUrl, jsonStr, JSON), 200);
    }

    private JSONObject buildJson(NodeRef node, Channel publishChannel,
            String comment, String statusMessage,
            Channel... statusChannels) throws JSONException
    {
        JSONObject json = new JSONObject();
        json.put(CHANNEL_NAME, publishChannel.getName());
        json.put(COMMENT, comment);
        Collection<String> publishNodes = Collections.singleton(node.toString());
        json.put(PUBLISH_NODES, publishNodes);
        json.put(STATUS_UPDATE, buildStatusUpdate(statusMessage, node, statusChannels));
        return json;
    }

    private JSONObject buildStatusUpdate(String message, NodeRef textNode, Channel... channels) throws JSONException
    {
        ArrayList<String> channelNames = new ArrayList<String>(channels.length);
        for (Channel channel : channels)
        {
            channelNames.add(channel.getName());
        }
        JSONObject statusUpdate = new JSONObject();
        statusUpdate.put(MESSAGE, message);
        statusUpdate.put(NODE_REF, textNode.toString());
        statusUpdate.put(CHANNEL_NAMES, channelNames);
        return statusUpdate;
    }

    private void checkChannels(JSONArray json, Channel... channels)throws Exception
    {
        assertEquals(channels.length, json.length());
        for (Channel channel : channels)
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
            if(channel.getName().equals(name))
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
        String expUrl = "api/publishing/channels/"
            + storeRef.getProtocol() + "/"
            + storeRef.getIdentifier() + "/"
            + node.getId();
        check(URL, jsonChannel, expUrl);
        check(TITLE, jsonChannel, channel.getName());
        JSONObject jsonType = jsonChannel.getJSONObject(CHANNEL_TYPE);
        assertNotNull("The channel type is null!", jsonType);
        checkChannelType(jsonType, channel.getChannelType());
    }

    private void checkChannelType(JSONObject jsonType, ChannelType channelType) throws Exception
    {
        check(ID, jsonType, channelType.getId());
        check(TITLE, jsonType, channelType.getId());
        
        String expUrl = "api/publishing/channelTypes/"+URLEncoder.encode(channelType.getId());
        check(URL, jsonType, expUrl);
        check(CHANNEL_NODE_TYPE, jsonType, channelType.getChannelNodeType().toString());
        
        List<String> contentTypes = CollectionUtils.toListOfStrings(channelType.getSupportedContentTypes());
        checkStrings(jsonType.getJSONArray(SUPPORTED_CONTENT_TYPES), contentTypes);
        checkStrings(jsonType.getJSONArray(SUPPORTED_MIME_TYPES), channelType.getSupportedMimetypes());
        
        check(CAN_PUBLISH, jsonType, channelType.canPublish());
        check(CAN_PUBLISH_STATUS_UPDATES, jsonType, channelType.canPublishStatusUpdates());
        check(CAN_UNPUBLISH, jsonType, channelType.canUnpublish());
        check(MAX_STATUS_LENGTH, jsonType, channelType.getMaximumStatusLength());

        //TODO Implement Icon URL
        check(ICON, jsonType, "");
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
            if(string.equals(json.getString(i)))
            {
                return;
            }
        }
        fail("Did not contain " + string);
    }

    private Channel createChannel(String typeId)
    {
        return channelService.createChannel(siteId, typeId, GUID.generate(), null);
    }

    private JSONObject getJsonData(Response response) throws Exception
    {
        String jsonStr = response.getContentAsString();
        assertNotNull("The JSON is null!", jsonStr);
        JSONObject json = new JSONObject(jsonStr);
        JSONObject data = json.getJSONObject("data");
        assertNotNull("Data was null!", data);
        return data;
    }

    private NodeRef createContentNode(String name, File theContent, String mimetype)
    {
        NodeRef source = fileFolderService.create(docLib, name, TYPE_CONTENT).getNodeRef();
        writeContent(source, theContent, mimetype);
        return source;
    }

    private NodeRef createContentNode(String name, String theContent, String mimetype)
    {
        NodeRef source = fileFolderService.create(docLib, name, TYPE_CONTENT).getNodeRef();
        writeContent(source, theContent, mimetype);
        return source;
    }
    
    private void writeContent(NodeRef source, String theContent, String mimetype)
    {
        ContentWriter writer = fileFolderService.getWriter(source);
        writer.setEncoding("UTF-8");
        writer.putContent(theContent);
        writer.setMimetype(mimetype);
    }
    
    private void writeContent(NodeRef source, File theContent, String mimetype)
    {
        ContentWriter writer = fileFolderService.getWriter(source);
        writer.setMimetype(mimetype);
        writer.setEncoding("UTF-8");
        writer.putContent(theContent);
    }

    private ChannelType mockChannelType(String channelTypeId)
    {
        ChannelType channelType = channelService.getChannelType(channelTypeId);
        if(channelType != null)
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

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ApplicationContext ctx = getServer().getApplicationContext();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        this.siteService = serviceRegistry.getSiteService();
        this.fileFolderService = serviceRegistry.getFileFolderService();
        this.channelService = (ChannelService) ctx.getBean(ChannelServiceImpl.NAME);
        this.channelHelper = (ChannelHelper) ctx.getBean(ChannelHelper.NAME);
        PublishingObjectFactory factory = (PublishingObjectFactory) ctx.getBean(PublishingObjectFactory.NAME);

        ChannelType publishAny = mockChannelType(publishAnyType);
        when(publishAny.canPublish()).thenReturn(true);
        
        ChannelType publishPdf= mockChannelType(publishPdfType);
        when(publishPdf.canPublish()).thenReturn(true);
        when(publishPdf.getSupportedMimetypes()).thenReturn(Collections.singleton(MimetypeMap.MIMETYPE_PDF));
        
        ChannelType statusUpdate= mockChannelType(statusUpdateType);
        when(statusUpdate.canPublishStatusUpdates()).thenReturn(true);
        when(statusUpdate.getMaximumStatusLength()).thenReturn(maxStatusLength);
        
        this.siteId = GUID.generate();
        siteService.createSite("test", siteId,
                "Test site created by ChannelServiceImplIntegratedTest",
                "Test site created by ChannelServiceImplIntegratedTest",
                SiteVisibility.PUBLIC);
        this.docLib = siteService.createContainer(siteId, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);

        this.environment = factory.createEnvironmentObject(siteId, environmentName);
    }
    
    @Override
    public void tearDown() throws Exception
    {
        try
        {
            siteService.deleteSite(siteId);
        }
        catch(Throwable t)
        {
            //NOOP
        }
        super.tearDown();
    }
}
