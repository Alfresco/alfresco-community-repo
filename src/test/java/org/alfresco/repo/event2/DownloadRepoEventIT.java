/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.event2;

import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.event.v1.model.EventData;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Adina Ababei
 */
public class DownloadRepoEventIT extends AbstractContextAwareRepoEvent
{
    private ContentService contentService;

    @Before
    public void setup()
    {
        contentService = (ContentService) applicationContext.getBean("contentService");
    }

    @Test
    public void testDownload()
    {
        final NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT);

        // node.Created event should be generated
        RepoEvent<NodeResource> resultRepoEvent = getRepoEvent(1);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());

        retryingTransactionHelper.doInTransaction(() -> {
            ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.TYPE_CONTENT,
                    true);
            writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
            writer.setEncoding("UTF-8");
            writer.putContent("test content");
            return null;
        });

        // node.Updated event should be generated
        resultRepoEvent = getRepoEvent(2);
        assertEquals("Wrong repo event type.", EventType.NODE_UPDATED.getType(), resultRepoEvent.getType());

        retryingTransactionHelper.doInTransaction(() -> {
            CMISNodeInfo cmisNodeInfo = cmisConnector.createNodeInfo(nodeRef);
            cmisConnector.getContentStream(cmisNodeInfo, null, null, null);
            return null;
        });

        // we should have 3 events: node.Created, node.Updated, node.Downloaded
        checkNumOfEvents(3);

        // node.Downloaded event should be generated
        RepoEvent<NodeResource> downloadedRepoEvent = getRepoEvent(3);
        assertEquals("Wrong repo event type.", EventType.NODE_DOWNLOADED.getType(), downloadedRepoEvent.getType());
        assertEquals(EventData.JSON_SCHEMA, downloadedRepoEvent.getDataschema());
        assertNotNull("The event should not have null id", downloadedRepoEvent.getId());
        assertNotNull("The event should not have null time", downloadedRepoEvent.getTime());

        NodeResource nodeResource = downloadedRepoEvent.getData().getResource();
        assertNotNull("Resource ID is null", nodeResource.getId());
        assertNotNull("Default aspects were not added. ", nodeResource.getAspectNames());
        assertNotNull("Missing createdByUser property.", nodeResource.getCreatedByUser());
        assertNotNull("Missing createdAt property.", nodeResource.getCreatedAt());
        assertNotNull("Missing modifiedByUser property.", nodeResource.getModifiedByUser());
        assertNotNull("Missing modifiedAt property.", nodeResource.getModifiedAt());
        assertNotNull("Missing node resource properties", nodeResource.getProperties());
        assertTrue("Incorrect value for isFile field", nodeResource.isFile());
        assertFalse("Incorrect value for isFolder files", nodeResource.isFolder());
        assertNull("ResourceBefore is not null", downloadedRepoEvent.getData().getResourceBefore());
    }

    @Test
    public void testDownloadTwiceInTheSameTransaction()
    {
        final NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT);

        // node.Created event should be generated
        RepoEvent<NodeResource> resultRepoEvent = getRepoEvent(1);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());

        retryingTransactionHelper.doInTransaction(() -> {
            ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.TYPE_CONTENT,
                    true);
            writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
            writer.setEncoding("UTF-8");
            writer.putContent("test content");
            return null;
        });

        // node.Updated event should be generated
        resultRepoEvent = getRepoEvent(2);
        assertEquals("Wrong repo event type.", EventType.NODE_UPDATED.getType(), resultRepoEvent.getType());

        retryingTransactionHelper.doInTransaction(() -> {
            CMISNodeInfo cmisNodeInfo = cmisConnector.createNodeInfo(nodeRef);
            cmisConnector.getContentStream(cmisNodeInfo, null, null, null);
            cmisConnector.getContentStream(cmisNodeInfo, null, null, null);
            return null;
        });

        // we should have 3 events: node.Created, node.Updated, node.Downloaded
        checkNumOfEvents(3);

        RepoEvent<NodeResource> downloadedRepoEvent = getRepoEvent(3);
        assertEquals("Wrong repo event type.", EventType.NODE_DOWNLOADED.getType(), downloadedRepoEvent.getType());
        assertEquals("Downloaded event does not have the correct id",
                    getNodeResource(resultRepoEvent).getId(),
                    getNodeResource(downloadedRepoEvent).getId());
        assertNull("ResourceBefore field is not null", downloadedRepoEvent.getData().getResourceBefore());
    }

    @Test
    public void testDownloadEventTwiceInDifferentTransactions()
    {
        final NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT);

        // node.Created event should be generated
        RepoEvent<NodeResource> resultRepoEvent = getRepoEvent(1);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());

        retryingTransactionHelper.doInTransaction(() -> {
            ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.TYPE_CONTENT,
                    true);
            writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
            writer.setEncoding("UTF-8");
            writer.putContent("test content");
            return null;
        });

        // node.Updated event should be generated
        resultRepoEvent = getRepoEvent(2);
        assertEquals("Wrong repo event type.", EventType.NODE_UPDATED.getType(), resultRepoEvent.getType());

        retryingTransactionHelper.doInTransaction(() -> {
            CMISNodeInfo cmisNodeInfo = cmisConnector.createNodeInfo(nodeRef);
            cmisConnector.getContentStream(cmisNodeInfo, null, null, null);
            return null;
        });

        RepoEvent<NodeResource> downloadedRepoEvent = getRepoEvent(3);
        assertEquals("Wrong repo event type.", EventType.NODE_DOWNLOADED.getType(), downloadedRepoEvent.getType());
        assertEquals("Downloaded event does not have the correct id",
                    getNodeResource(resultRepoEvent).getId(),
                    getNodeResource(downloadedRepoEvent).getId());
        assertNull("ResourceBefore field is not null", downloadedRepoEvent.getData().getResourceBefore());

        retryingTransactionHelper.doInTransaction(() -> {
            CMISNodeInfo cmisNodeInfo = cmisConnector.createNodeInfo(nodeRef);
            cmisConnector.getContentStream(cmisNodeInfo, null, null, null);
            return null;
        });

        // we should have 4 events: node.Created, node.Updated, node.Downloaded, node.Downloaded
        checkNumOfEvents(4);

        downloadedRepoEvent = getRepoEvent(4);
        assertEquals("Wrong repo event type.", EventType.NODE_DOWNLOADED.getType(), downloadedRepoEvent.getType());
        assertEquals("Downloaded event does not have the correct id",
                    getNodeResource(resultRepoEvent).getId(),
                    getNodeResource(downloadedRepoEvent).getId());
        assertNull("ResourceBefore field is not null", downloadedRepoEvent.getData().getResourceBefore());
    }
}
