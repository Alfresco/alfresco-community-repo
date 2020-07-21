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
public class DeleteRepoEventIT extends AbstractContextAwareRepoEvent
{
    @Test
    public void testDeleteContent()
    {
        PropertyMap propertyMap = new PropertyMap();
        propertyMap.put(ContentModel.PROP_TITLE, "test title");
        NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT, propertyMap);

        NodeResource createdResource = getNodeResource(1);

        assertNotNull("Resource ID is null", createdResource.getId());
        assertNotNull("Default aspects were not added. ", createdResource.getAspectNames());
        assertNotNull("Missing createdByUser property.", createdResource.getCreatedByUser());
        assertNotNull("Missing createdAt property.", createdResource.getCreatedAt());
        assertNotNull("Missing modifiedByUser property.", createdResource.getModifiedByUser());
        assertNotNull("Missing modifiedAt property.", createdResource.getModifiedAt());
        assertNotNull("Missing node resource properties", createdResource.getProperties());

        deleteNode(nodeRef);
        final RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEvent(2);

        assertEquals("Repo event type:", EventType.NODE_DELETED.getType(), resultRepoEvent.getType());
        assertEquals(createdResource.getId(), getNodeResource(resultRepoEvent).getId());

        // There should be no resourceBefore
        EventData<NodeResource> eventData = getEventData(resultRepoEvent);
        assertNull("There should be no 'resourceBefore' object for the Deleted event type.",
            eventData.getResourceBefore());
    }

    @Test
    public void testDeleteFolderWithContent()
    {
        NodeRef grandParent = createNode(ContentModel.TYPE_FOLDER);
        NodeRef parent = createNode(ContentModel.TYPE_FOLDER, grandParent);
        createNode(ContentModel.TYPE_CONTENT, parent);
        createNode(ContentModel.TYPE_CONTENT, parent);

        // 4 Created Events
        checkNumOfEvents(4);

        deleteNode(grandParent);
        // 4 Deleted events + 4 created events
        checkNumOfEvents(8);
    }

    @Test
    public void testCreateDeleteNodeInTheSameTransaction()
    {
        retryingTransactionHelper.doInTransaction(() -> {

            NodeRef nodeRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(TEST_NAMESPACE, GUID.generate()),
                ContentModel.TYPE_CONTENT).getChildRef();

            nodeService.deleteNode(nodeRef);
            return null;
        });
        //Create and delete node are done in the same transaction so no events are expected
        // to be generated
        checkNumOfEvents(0);
    }
}
