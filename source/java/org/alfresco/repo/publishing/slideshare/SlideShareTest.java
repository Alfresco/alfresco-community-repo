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

package org.alfresco.repo.publishing.slideshare;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.publishing.Environment;
import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.repo.publishing.PublishingQueueImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.junit.Assert;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @author Brian
 * 
 */
public class SlideShareTest extends BaseSpringTest
{
    protected ServiceRegistry serviceRegistry;
    protected SiteService siteService;
    protected FileFolderService fileFolderService;
    protected NodeService nodeService;
    protected String siteId;
    protected PublishingQueueImpl queue;
    protected Environment environment;
    protected NodeRef docLib;
    protected Map<String, String> testFiles = new TreeMap<String, String>();
    protected Map<NodeRef, String> testNodeMap = new HashMap<NodeRef, String>();

    private ChannelService channelService;

    private RetryingTransactionHelper transactionHelper;

    public void onSetUp() throws Exception
    {
        serviceRegistry = (ServiceRegistry) getApplicationContext().getBean("ServiceRegistry");
        channelService = (ChannelService) getApplicationContext().getBean("channelService");
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        siteService = serviceRegistry.getSiteService();
        fileFolderService = serviceRegistry.getFileFolderService();
        nodeService = serviceRegistry.getNodeService();
        transactionHelper = serviceRegistry.getRetryingTransactionHelper();

        siteId = GUID.generate();
        siteService.createSite("test", siteId, "Site created by publishing test", "Site created by publishing test",
                SiteVisibility.PUBLIC);
        docLib = siteService.createContainer(siteId, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);

        testFiles.put("test/alfresco/TestPresentation.pptx", MimetypeMap.MIMETYPE_OPENXML_PRESENTATION);
        testFiles.put("test/alfresco/TestPresentation2.ppt", MimetypeMap.MIMETYPE_PPT);
        testFiles.put("test/alfresco/TestPresentation3.odp", MimetypeMap.MIMETYPE_OPENDOCUMENT_PRESENTATION);
        testFiles.put("test/alfresco/TestPresentation4.pdf", MimetypeMap.MIMETYPE_PDF);
    }

    public void onTearDown()
    {
        siteService.deleteSite(siteId);
    }

    public void testBlank()
    {

    }

    // Note that this test isn't normally run, as it requires valid YouTube
    // credentials.
    // To run it, remove the initial 'x' from the method name and set the
    // appropriate YouTube credentials where the
    // text "YOUR_USER_NAME" and "YOUR_PASSWORD" appear.
    public void xtestSlideSharePublishAndUnpublishActions() throws Exception
    {
        final List<NodeRef> nodes = transactionHelper.doInTransaction(new RetryingTransactionCallback<List<NodeRef>>()
        {
            public List<NodeRef> execute() throws Throwable
            {
                List<NodeRef> createdTestNodes = new ArrayList<NodeRef>();
                Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                // props.put(PublishingModel.PROP_CHANNEL_USERNAME,
                // "YOUR_USER_NAME");
                // props.put(PublishingModel.PROP_CHANNEL_PASSWORD,
                // "YOUR_PASSWORD");
                props.put(PublishingModel.PROP_CHANNEL_USERNAME, "YOUR_USER_NAME");
                props.put(PublishingModel.PROP_CHANNEL_PASSWORD, "YOUR_PASSWORD");
                Channel channel = channelService.createChannel(SlideShareChannelType.ID, GUID.generate(), props);

                NodeRef channelNode = channel.getNodeRef();

                for (Map.Entry<String, String> testFileInfoEntry : testFiles.entrySet())
                {
                    NodeRef nodeRef = createTestNode(channelNode, testFileInfoEntry.getKey(), testFileInfoEntry
                            .getValue());
                    createdTestNodes.add(nodeRef);
                    testNodeMap.put(nodeRef, testFileInfoEntry.getKey());
                }
                return createdTestNodes;
            }

            private NodeRef createTestNode(NodeRef parent, String fileLocation, String mimeType)
                    throws ContentIOException, IOException
            {
                Resource file = new ClassPathResource(fileLocation);
                Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                props.put(ContentModel.PROP_NAME, "Presentation " + GUID.generate());
                NodeRef node = nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()),
                        ContentModel.TYPE_CONTENT, props).getChildRef();
                ContentService contentService = serviceRegistry.getContentService();
                ContentWriter writer = contentService.getWriter(node, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(mimeType);
                writer.putContent(file.getFile());
                return node;
            }
        });

        transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                for (NodeRef node : nodes)
                {
                    ActionService actionService = serviceRegistry.getActionService();
                    Action publishAction = actionService.createAction(SlideSharePublishAction.NAME);
                    actionService.executeAction(publishAction, node);
                    Map<QName, Serializable> props = nodeService.getProperties(node);
                    Assert.assertTrue(nodeService.hasAspect(node, SlideSharePublishingModel.ASPECT_ASSET));
                    Assert.assertNotNull(props.get(PublishingModel.PROP_ASSET_ID));
                    Assert.assertNotNull(props.get(PublishingModel.PROP_ASSET_URL));

                    System.out.println("Test file: " + testNodeMap.get(node));
                    System.out.println("SlideShare id: " + props.get(PublishingModel.PROP_ASSET_ID));
                    System.out.println("SlideShare URL: " + props.get(PublishingModel.PROP_ASSET_URL));
                }

                // Action unpublishAction =
                // actionService.createAction(SlideShareUnpublishAction.NAME);
                // actionService.executeAction(unpublishAction, node);
                // props = nodeService.getProperties(node);
                // Assert.assertFalse(nodeService.hasAspect(node,
                // SlideSharePublishingModel.ASPECT_ASSET));
                // Assert.assertNull(props.get(SlideSharePublishingModel.PROP_ASSET_ID));
                // Assert.assertNull(props.get(SlideSharePublishingModel.PROP_ASSET_URL));
                return null;
            }
        });

        transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                for (NodeRef node : nodes)
                {
                    nodeService.deleteNode(node);
                }
                return null;
            }
        });

    }

}
