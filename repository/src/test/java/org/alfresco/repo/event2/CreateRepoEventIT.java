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

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.event.v1.model.EventData;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.junit.Test;

/**
 * @author Iulian Aftene
 */
public class CreateRepoEventIT extends AbstractContextAwareRepoEvent
{

    @Test
    public void testCreateEvent()
    {
        // Create a node without content
        final String name = "TestFile-" + System.currentTimeMillis() + ".txt";
        PropertyMap propertyMap = new PropertyMap();
        propertyMap.put(ContentModel.PROP_TITLE, "test title");
        propertyMap.put(ContentModel.PROP_NAME, name);
        final NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT, propertyMap);

        final RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEvent(1);
        // Repo event attributes
        assertEquals("Repo event type", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());
        assertNotNull("Repo event ID is not available. ", resultRepoEvent.getId());
        assertNotNull(resultRepoEvent.getSource());
        assertEquals("Repo event source is not available. ",
            "/" + descriptorService.getCurrentRepositoryDescriptor().getId(),
            resultRepoEvent.getSource().toString());
        assertNotNull("Repo event creation time is not available. ", resultRepoEvent.getTime());
        assertEquals("Repo event datacontenttype", "application/json", resultRepoEvent.getDatacontenttype());
        assertNotNull(resultRepoEvent.getDataschema());
        assertEquals(EventJSONSchema.NODE_CREATED_V1.getSchema(), resultRepoEvent.getDataschema());

        final EventData<NodeResource> nodeResourceEventData = getEventData(resultRepoEvent);
        // EventData attributes
        assertNotNull("Event data group ID is not available. ", nodeResourceEventData.getEventGroupId());
        assertNull("resourceBefore property is not available", nodeResourceEventData.getResourceBefore());
        final NodeResource nodeResource = getNodeResource(resultRepoEvent);

        // NodeResource attributes
        assertEquals(nodeRef.getId(), nodeResource.getId());
        assertEquals(name, nodeResource.getName());
        assertEquals("cm:content", nodeResource.getNodeType());
        assertNotNull(nodeResource.getPrimaryHierarchy());
        assertNotNull("Default aspects were not added. ", nodeResource.getAspectNames());
        assertEquals("test title", getProperty(nodeResource, "cm:title"));
        assertNull("There is no content.", nodeResource.getContent());

        assertNotNull("Missing createdByUser property.", nodeResource.getCreatedByUser());
        assertEquals("Wrong node creator id.", "admin", nodeResource.getCreatedByUser().getId());
        assertEquals("Wrong node creator display name.", "Administrator",
            nodeResource.getCreatedByUser().getDisplayName());
        assertNotNull("Missing createdAt property.", nodeResource.getCreatedAt());

        assertNotNull("Missing modifiedByUser property.", nodeResource.getModifiedByUser());
        assertEquals("Wrong node modifier id.", "admin", nodeResource.getModifiedByUser().getId());
        assertEquals("Wrong node modifier display name.", "Administrator",
            nodeResource.getModifiedByUser().getDisplayName());
        assertNotNull("Missing modifiedAt property.", nodeResource.getModifiedAt());
    }

    @Test
    public void testCreateContentInFolderStructure()
    {
        final NodeRef grandParent = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef parent = createNode(ContentModel.TYPE_FOLDER, grandParent);
        createNode(ContentModel.TYPE_CONTENT, parent);

        final NodeResource resource = getNodeResource(3);

        List<String> primaryHierarchy = resource.getPrimaryHierarchy();
        assertNotNull(primaryHierarchy);
        assertEquals("Wrong hierarchy", 3, primaryHierarchy.size());
        assertEquals("Wrong parent", parent.getId(), primaryHierarchy.get(0));
    }

    @Test
    public void testCreateNodeWithId()
    {
        final String uuid = GUID.generate();
        PropertyMap properties = new PropertyMap();
        properties.put(ContentModel.PROP_NODE_UUID, uuid);

        // create a node with an explicit UUID
        createNode(ContentModel.TYPE_CONTENT, properties);

        final NodeResource resource = getNodeResource(1);

        assertEquals("Failed to create node with a chosen ID", uuid, resource.getId());
    }

    @Test
    public void testFolderNodeType()
    {
        createNode(ContentModel.TYPE_FOLDER);

        final NodeResource resource = getNodeResource(1);

        assertEquals("cm:content node type was not found", "cm:folder", resource.getNodeType());
        assertFalse("isFile flag should be FALSE for nodeType=cm:folder. ", resource.isFile());
        assertTrue("isFolder flag should be TRUE for nodeType=cm:folder. ", resource.isFolder());
    }

    @Test
    public void testFileNodeType()
    {
        createNode(ContentModel.TYPE_CONTENT);

        final NodeResource resource = getNodeResource(1);

        assertEquals("cm:content node type was not found", "cm:content", resource.getNodeType());
        assertTrue("isFile flag should be TRUE for nodeType=cm:content. ", resource.isFile());
        assertFalse("isFolder flag should be FALSE for nodeType=cm:content. ", resource.isFolder());
    }

    @Test
    public void testCteateMultipleNodesInTheSameTransaction()
    {
        retryingTransactionHelper.doInTransaction(() -> {
            for (int i = 0; i < 3; i++)
            {
                nodeService.createNode(
                    rootNodeRef,
                    ContentModel.ASSOC_CHILDREN,
                    QName.createQName(TEST_NAMESPACE, GUID.generate()),
                    ContentModel.TYPE_CONTENT);
            }
            return null;
        });

        checkNumOfEvents(3);

        RepoEventContainer repoEventsContainer = getRepoEventsContainer();
        final String eventGroupId1 =
            getEventData(repoEventsContainer.getEvent(1)).getEventGroupId();
        final String eventGroupId2 =
            getEventData(repoEventsContainer.getEvent(2)).getEventGroupId();
        final String eventGroupId3 =
            getEventData(repoEventsContainer.getEvent(3)).getEventGroupId();

        //All events in the transaction should have the same eventGroupId
        assertTrue(eventGroupId1.equals(eventGroupId2) && eventGroupId2.equals(eventGroupId3));
    }
}
