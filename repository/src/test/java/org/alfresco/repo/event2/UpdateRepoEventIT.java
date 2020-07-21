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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Type;
import org.alfresco.repo.event.v1.model.ContentInfo;
import org.alfresco.repo.event.v1.model.EventData;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.service.cmr.dictionary.CustomModelDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.junit.Test;

/**
 * @author Iulian Aftene
 * @author Jamal Kaabi-Mofard
 */
public class UpdateRepoEventIT extends AbstractContextAwareRepoEvent
{
    @Test
    public void testUpdateNodeResourceContent()
    {
        ContentService contentService = (ContentService) applicationContext.getBean(
            "contentService");

        final NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT);

        RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEvent(1);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(),
            resultRepoEvent.getType());

        NodeResource resource = getNodeResource(resultRepoEvent);
        assertNull("Content should have been null.", resource.getContent());

        retryingTransactionHelper.doInTransaction(() -> {
            ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.TYPE_CONTENT,
                true);
            writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
            writer.setEncoding("UTF-8");
            writer.putContent("test content.");
            return null;
        });

        checkNumOfEvents(2);
        
        resultRepoEvent = getRepoEvent(2);
        assertEquals("Wrong repo event type.", EventType.NODE_UPDATED.getType(),
            resultRepoEvent.getType());

        resource = getNodeResource(resultRepoEvent);
        ContentInfo content = resource.getContent();
        assertNotNull(content);
        assertEquals(MimetypeMap.MIMETYPE_PDF, content.getMimeType());
        assertEquals("UTF-8", content.getEncoding());
        assertTrue(content.getSizeInBytes() > 0);

        NodeResource resourceBefore = getNodeResourceBefore(resultRepoEvent);
        assertNull("Content should have been null.", resourceBefore.getContent());

        // Update the content again
        retryingTransactionHelper.doInTransaction(() -> {
            ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.TYPE_CONTENT,
                true);
            writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
            writer.setEncoding("UTF-8");
            writer.putContent("A quick brown fox jumps over the lazy dog.");
            return null;
        });

        resource = getNodeResource(3);
        content = resource.getContent();
        assertNotNull(content);
        assertEquals(MimetypeMap.MIMETYPE_PDF, content.getMimeType());
        assertEquals("UTF-8", content.getEncoding());
        assertTrue(content.getSizeInBytes() > 0);

        resourceBefore = getNodeResourceBefore(3);
        assertNotNull("Content should not have been null.", resourceBefore.getContent());
        content = resourceBefore.getContent();
        assertNotNull(content);
        assertEquals(MimetypeMap.MIMETYPE_PDF, content.getMimeType());
        assertEquals("UTF-8", content.getEncoding());
        assertTrue(content.getSizeInBytes() > 0);
        assertNotNull(resourceBefore.getModifiedAt());

        // Apart from the 'content' and 'modifiedAt' properties the rest should be not be not set
        // for the resourceBefore object
        assertNull(resourceBefore.getId());
        assertNull(resourceBefore.getName());
        assertNull(resourceBefore.getNodeType());
        assertNull(resourceBefore.isFile());
        assertNull(resourceBefore.isFolder());
        assertNull(resourceBefore.getModifiedByUser());
        assertNull(resourceBefore.getCreatedAt());
        assertNull(resourceBefore.getCreatedByUser());
        assertNull(resourceBefore.getProperties());
        assertNull(resourceBefore.getAspectNames());
        assertNull(resourceBefore.getPrimaryHierarchy());
    }

    @Test
    public void testUpdateNodeResourceContentSameContentSize()
    {
        ContentService contentService = (ContentService) applicationContext.getBean("contentService");

        final NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT);

        RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEvent(1);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());

        NodeResource resource = getNodeResource(resultRepoEvent);
        assertNull("Content should have been null.", resource.getContent());

        retryingTransactionHelper.doInTransaction(() -> {
            ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.TYPE_CONTENT, true);
            writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
            writer.setEncoding("UTF-8");
            writer.putContent("test content a");
            return null;
        });

        checkNumOfEvents(2);
        
        resultRepoEvent = getRepoEvent(2);
        assertEquals("Wrong repo event type.", EventType.NODE_UPDATED.getType(), resultRepoEvent.getType());

        resource = getNodeResource(resultRepoEvent);
        ContentInfo content = resource.getContent();
        assertNotNull(content);
        assertEquals(MimetypeMap.MIMETYPE_PDF, content.getMimeType());
        assertEquals("UTF-8", content.getEncoding());
        assertEquals(14, (long) content.getSizeInBytes());

        NodeResource resourceBefore = getNodeResourceBefore(resultRepoEvent);
        assertNull("Content should have been null.", resourceBefore.getContent());

        // Update the content again - different content but same size
        retryingTransactionHelper.doInTransaction(() -> {
            ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.TYPE_CONTENT, true);
            writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
            writer.setEncoding("UTF-8");
            writer.putContent("test content b");
            return null;
        });

        resource = getNodeResource(3);
        content = resource.getContent();
        assertNotNull(content);
        assertEquals(MimetypeMap.MIMETYPE_PDF, content.getMimeType());
        assertEquals("UTF-8", content.getEncoding());
        assertEquals(14, (long) content.getSizeInBytes());

        resourceBefore = getNodeResourceBefore(3);
        assertNotNull("Content should not have been null.", resourceBefore.getContent());
        content = resourceBefore.getContent();
        assertNotNull(content);
        assertEquals(MimetypeMap.MIMETYPE_PDF, content.getMimeType());
        assertEquals("UTF-8", content.getEncoding());
        assertEquals(14, (long) content.getSizeInBytes());
        assertNotNull(resourceBefore.getModifiedAt());

        // Apart from the 'content' and 'modifiedAt' properties the rest should be not be not set
        // for the resourceBefore object
        assertNull(resourceBefore.getId());
        assertNull(resourceBefore.getName());
        assertNull(resourceBefore.getNodeType());
        assertNull(resourceBefore.isFile());
        assertNull(resourceBefore.isFolder());
        assertNull(resourceBefore.getModifiedByUser());
        assertNull(resourceBefore.getCreatedAt());
        assertNull(resourceBefore.getCreatedByUser());
        assertNull(resourceBefore.getProperties());
        assertNull(resourceBefore.getAspectNames());
        assertNull(resourceBefore.getPrimaryHierarchy());
    }

    @Test
    public void testUpdateContentTitle()
    {
        final NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT);
        NodeResource resource = getNodeResource(1);

        assertNotNull(resource.getProperties());
        String title = getProperty(resource, "cm:title");
        assertNull("Title should have been null.", title);

        // update content cm:title property with "test title" value
        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, "test title");
            return null;
        });

        resource = getNodeResource(2);
        title = getProperty(resource, "cm:title");
        assertEquals("test title", title);

        // update content cm:title property again with "new test title" value
        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, "new test title");
            return null;
        });

        resource = getNodeResource(3);
        title = getProperty(resource, "cm:title");
        assertEquals("new test title", title);

        NodeResource resourceBefore = getNodeResourceBefore(3);
        title = getProperty(resourceBefore, "cm:title");
        assertEquals("Wrong old property.", "test title", title);
        assertNotNull(resourceBefore.getModifiedAt());
    }

    @Test
    public void testUpdateContentDescription()
    {
        final NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT);

        NodeResource resource = getNodeResource(1);
        String desc = getProperty(resource, "cm:description");
        assertNull("Description should have been null.", desc);

        // update content cm:description property with "test_description" value
        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, "test description");
            return null;
        });

        resource = getNodeResource(2);
        desc = getProperty(resource, "cm:description");
        assertEquals("test description", desc);

        NodeResource resourceBefore = getNodeResourceBefore(2);
        assertNull(resourceBefore.getProperties());
    }

    @Test
    public void testUpdateContentName()
    {
        final NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT);

        NodeResource resource = getNodeResource(1);
        String oldName = resource.getName();
        assertEquals(nodeRef.getId(), oldName);

        // update cm:name property with "test_new_name" value
        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, "test_new_name");
            return null;
        });

        resource = getNodeResource(2);
        assertEquals("test_new_name", resource.getName());

        NodeResource resourceBefore = getNodeResourceBefore(2);
        assertEquals(oldName, resourceBefore.getName());
        assertNotNull(resourceBefore.getModifiedAt());
        // Apart from the 'name' and 'modifiedAt' properties the rest should be not be not set
        // for the resourceBefore object
        assertNull(resourceBefore.getId());
        assertNull(resourceBefore.getContent());
        assertNull(resourceBefore.getNodeType());
        assertNull(resourceBefore.isFile());
        assertNull(resourceBefore.isFolder());
        assertNull(resourceBefore.getModifiedByUser());
        assertNull(resourceBefore.getCreatedAt());
        assertNull(resourceBefore.getCreatedByUser());
        assertNull(resourceBefore.getProperties());
        assertNull(resourceBefore.getAspectNames());
        assertNull(resourceBefore.getPrimaryHierarchy());
    }

    @Test
    public void testAddAspectToContent()
    {
        final NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT);

        NodeResource resource = getNodeResource(1);
        final Set<String> originalAspects = resource.getAspectNames();
        assertNotNull(originalAspects);
        assertFalse(originalAspects.contains("cm:versionable"));
        // Check properties
        assertTrue(resource.getProperties().isEmpty());

        // Add cm:versionable aspect with default value
        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, null);
            return null;
        });

        resource = getNodeResource(2);
        assertNotNull(resource.getAspectNames());
        assertTrue(resource.getAspectNames().contains("cm:versionable"));
        //Check all aspects
        Set<String> expectedAspects = new HashSet<>(originalAspects);
        expectedAspects.add("cm:versionable");
        assertEquals(expectedAspects, resource.getAspectNames());
        // Check properties
        assertFalse(resource.getProperties().isEmpty());

        //Check resourceBefore
        NodeResource resourceBefore = getNodeResourceBefore(2);
        assertNotNull(resourceBefore.getAspectNames());
        assertEquals(originalAspects, resourceBefore.getAspectNames());
        assertNull(resourceBefore.getProperties());
    }

    @Test
    public void testRemoveAspectFromContentTest()
    {
        final NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT);
        NodeResource resource = getNodeResource(1);
        final Set<String> originalAspects = resource.getAspectNames();
        assertNotNull(originalAspects);

        // Add cm:geographic aspect with default value
        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_GEOGRAPHIC, null);
            return null;
        });
        resource = getNodeResource(2);
        Set<String> aspectsBeforeRemove = resource.getAspectNames();
        assertNotNull(aspectsBeforeRemove);
        assertTrue(aspectsBeforeRemove.contains("cm:geographic"));

        // Remove cm:geographic aspect
        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.removeAspect(nodeRef, ContentModel.ASPECT_GEOGRAPHIC);
            return null;
        });

        resource = getNodeResource(3);
        assertEquals(originalAspects, resource.getAspectNames());

        NodeResource resourceBefore = getNodeResourceBefore(3);
        assertNotNull(resourceBefore.getAspectNames());
        assertEquals(aspectsBeforeRemove, resourceBefore.getAspectNames());
    }

    @Test
    public void testCreateAndUpdateInTheSameTransaction()
    {
        retryingTransactionHelper.doInTransaction(() -> {

            NodeRef node1 = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(TEST_NAMESPACE, GUID.generate()),
                ContentModel.TYPE_CONTENT).getChildRef();

            nodeService.setProperty(node1, ContentModel.PROP_DESCRIPTION, "test description");
            return null;
        });
        //Create and update node are done in the same transaction so one event is expected
        // to be generated
        checkNumOfEvents(1);
    }

    @Test
    public void testUpdateNodeType()
    {
        final NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT);
        // old node's type
        assertEquals("Created node does not have the correct type", ContentModel.TYPE_CONTENT, nodeService.getType(nodeRef));

        // node.Created event should be generated
        RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEvent(1);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());
        NodeResource nodeResource = getNodeResource(resultRepoEvent);
        assertEquals("cm:content node type was not found", "cm:content", nodeResource.getNodeType());

        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.setType(nodeRef, ContentModel.TYPE_FOLDER);

            // new node's type
            assertEquals("Wrong node type", ContentModel.TYPE_FOLDER, nodeService.getType(nodeRef));
            return null;
        });

        // we should have 2 events, node.Created and node.Updated
        checkNumOfEvents(2);

        resultRepoEvent = getRepoEvent(2);
        assertEquals("Wrong repo event type.", EventType.NODE_UPDATED.getType(), resultRepoEvent.getType());
        nodeResource = getNodeResource(resultRepoEvent);
        assertEquals("Incorrect node type was found", "cm:folder", nodeResource.getNodeType());

        NodeResource resourceBefore = getNodeResourceBefore(2);
        assertEquals("Incorrect node type was found","cm:content", resourceBefore.getNodeType());
        // assertNotNull(resourceBefore.getModifiedAt());  uncomment this when the issue will be fixed
        assertNull(resourceBefore.getId());
        assertNull(resourceBefore.getContent());
        assertNull(resourceBefore.isFile());
        assertNull(resourceBefore.isFolder());
        assertNull(resourceBefore.getModifiedByUser());
        assertNull(resourceBefore.getCreatedAt());
        assertNull(resourceBefore.getCreatedByUser());
        assertNull(resourceBefore.getProperties());
        assertNull(resourceBefore.getAspectNames());
        assertNull(resourceBefore.getPrimaryHierarchy());
    }

    @Test
    public void testUpdateNodeTypeWithCustomType()
    {
        String modelName = "testModel" + System.currentTimeMillis();
        String modelDescription = "testModel description";
        Pair<String, String> namespacePair = getNamespacePair();

        M2Model model = M2Model.createModel(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + modelName);
        model.createNamespace(namespacePair.getFirst(), namespacePair.getSecond());
        model.setDescription(modelDescription);

        String typeName = "testType";
        M2Type m2Type = model.createType(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + typeName);
        m2Type.setTitle("Test type title");

        // Create active model
        CustomModelDefinition modelDefinition =
                retryingTransactionHelper.doInTransaction(() -> customModelService.createCustomModel(model, true));

        assertNotNull(modelDefinition);
        assertEquals(modelName, modelDefinition.getName().getLocalName());
        assertEquals(modelDescription, modelDefinition.getDescription());

        // List all of the model's types
        Collection<TypeDefinition> types = modelDefinition.getTypeDefinitions();
        assertEquals(1, types.size());

        // node.Created event should be generated for the model
        RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEvent(1);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());
        NodeResource nodeResource = getNodeResource(resultRepoEvent);
        assertEquals("Incorrect node type was found", "cm:dictionaryModel", nodeResource.getNodeType());

        final NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT);
        // old node's type
        assertEquals(ContentModel.TYPE_CONTENT, nodeService.getType(nodeRef));

        // node.Created event should be generated
        resultRepoEvent = getRepoEvent(2);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());
        nodeResource = getNodeResource(resultRepoEvent);
        assertEquals("cm:content node type was not found", "cm:content", nodeResource.getNodeType());

        QName typeQName = QName.createQName("{" + namespacePair.getFirst()+ "}" + typeName);
        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.setType(nodeRef, typeQName);

            // new node's type
            assertEquals(typeQName, nodeService.getType(nodeRef));
            return null;
        });

        // we should have 3 events, node.Created for the model, node.Created for the node and node.Updated
        checkNumOfEvents(3);

        resultRepoEvent = getRepoEvent(3);
        assertEquals("Wrong repo event type.", EventType.NODE_UPDATED.getType(), resultRepoEvent.getType());
        nodeResource = getNodeResource(resultRepoEvent);
        assertEquals("Incorrect node type was found", namespacePair.getSecond() + QName.NAMESPACE_PREFIX + typeName, nodeResource.getNodeType());

        NodeResource resourceBefore = getNodeResourceBefore(3);
        assertEquals("Incorrect node type was found", "cm:content", resourceBefore.getNodeType());
        assertNull(resourceBefore.getId());
        assertNull(resourceBefore.getContent());
        assertNull(resourceBefore.isFile());
        assertNull(resourceBefore.isFolder());
        assertNull(resourceBefore.getModifiedByUser());
        assertNull(resourceBefore.getCreatedAt());
        assertNull(resourceBefore.getCreatedByUser());
        assertNull(resourceBefore.getProperties());
        assertNull(resourceBefore.getAspectNames());
        assertNull(resourceBefore.getPrimaryHierarchy());

    }

    @Test
    public void testUpdateTwiceNodeTypeInTheSameTransaction()
    {
        final NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT);

        // node.Created event should be generated
        RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEvent(1);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());
        NodeResource nodeResource = getNodeResource(resultRepoEvent);
        assertEquals("Incorrect node type was found", "cm:content", nodeResource.getNodeType());

        // old type
        assertEquals(ContentModel.TYPE_CONTENT, nodeService.getType(nodeRef));

        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.setType(nodeRef, ContentModel.TYPE_FOLDER);
            nodeService.setType(nodeRef, ContentModel.TYPE_CONTENT);

            // new type
            assertEquals("Wrong node type", ContentModel.TYPE_CONTENT, nodeService.getType(nodeRef));
            return null;
        });

        // we should have only 2 events, node.Created and node.Updated
        checkNumOfEvents(2);

        resultRepoEvent = getRepoEvent(2);
        assertEquals("Wrong repo event type.", EventType.NODE_UPDATED.getType(), resultRepoEvent.getType());
        nodeResource = getNodeResource(resultRepoEvent);
        assertEquals("Incorrect node type was found", "cm:content", nodeResource.getNodeType());

        NodeResource resourceBefore = getNodeResourceBefore(2);
        assertEquals("Incorrect node type was found", "cm:folder", resourceBefore.getNodeType());
        // assertNotNull(resourceBefore.getModifiedAt()); uncomment this when the issue will be fixed
        assertNull(resourceBefore.getId());
        assertNull(resourceBefore.getContent());
        assertNull(resourceBefore.isFile());
        assertNull(resourceBefore.isFolder());
        assertNull(resourceBefore.getModifiedByUser());
        assertNull(resourceBefore.getCreatedAt());
        assertNull(resourceBefore.getCreatedByUser());
        assertNull(resourceBefore.getProperties());
        assertNull(resourceBefore.getAspectNames());
        assertNull(resourceBefore.getPrimaryHierarchy());
    }

    @Test
    public void testCreateAndUpdateNodeTypeInTheSameTransaction()
    {
        retryingTransactionHelper.doInTransaction(() -> {
            final NodeRef nodeRef = nodeService.createNode(
                    rootNodeRef,
                    ContentModel.ASSOC_CHILDREN,
                    QName.createQName(TEST_NAMESPACE, GUID.generate()),
                    ContentModel.TYPE_CONTENT).getChildRef();

            // old type
            assertEquals(ContentModel.TYPE_CONTENT, nodeService.getType(nodeRef));

            nodeService.setType(nodeRef, ContentModel.TYPE_FOLDER);

            // new type
            assertEquals(ContentModel.TYPE_FOLDER, nodeService.getType(nodeRef));
            return null;
        });

        // we should have only 1 event, node.Created
        checkNumOfEvents(1);

        RepoEvent<EventData<NodeResource>>  resultRepoEvent = getRepoEvent(1);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());
        NodeResource nodeResource = getNodeResource(resultRepoEvent);
        assertEquals("Incorrect node type was found", "cm:folder", nodeResource.getNodeType());
    }

    private Pair<String, String> getNamespacePair()
    {
        long timeMillis = System.currentTimeMillis();
        String uri = "http://www.alfresco.org/model/testcmmservicenamespace" + timeMillis + "/1.0";
        String prefix = "testcmmservice" + timeMillis;

        return new Pair<>(uri, prefix);
    }

    @Test
    public void testMoveFile()
    {
        final NodeRef folder1 = createNode(ContentModel.TYPE_FOLDER);
        final String folder1ID = getNodeResource(1).getId();
        final NodeRef folder2 = createNode(ContentModel.TYPE_FOLDER);
        final String folder2ID = getNodeResource(2).getId();
        final NodeRef moveFile = createNode(ContentModel.TYPE_CONTENT, folder1);

        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.moveNode(
                moveFile,
                folder2,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(TEST_NAMESPACE));
            return null;
        });

        checkNumOfEvents(4);
        
        NodeResource resourceBefore = getNodeResourceBefore(4);
        NodeResource resource = getNodeResource(4);

        final String moveFileParentBeforeMove = resourceBefore.getPrimaryHierarchy().get(0);
        final String moveFileParentAfterMove = resource.getPrimaryHierarchy().get(0);

        assertEquals("Wrong node parent.", folder1ID, moveFileParentBeforeMove);
        assertEquals("Wrong node parent.", folder2ID, moveFileParentAfterMove);
        assertEquals("Wrong repo event type.", EventType.NODE_UPDATED.getType(),
            getRepoEvent(4).getType());

        assertNull(resourceBefore.getId());
        assertNull(resourceBefore.getName());
        assertNull(resourceBefore.getNodeType());
        assertNull(resourceBefore.isFile());
        assertNull(resourceBefore.isFolder());
        assertNull(resourceBefore.getModifiedByUser());
        assertNull(resourceBefore.getCreatedAt());
        assertNull(resourceBefore.getCreatedByUser());
        assertNull(resourceBefore.getProperties());
        assertNull(resourceBefore.getAspectNames());
        assertNotNull(resourceBefore.getPrimaryHierarchy());
        assertNull("Content should have been null.", resource.getContent());
        assertNull("Content should have been null.", resourceBefore.getContent());

        assertNotNull(resource.getModifiedAt());
        assertNotNull(resource.getModifiedByUser());
        assertNotNull(resource.getAspectNames());
        assertNull(resource.getContent());
        assertTrue(resource.getProperties().isEmpty());
    }

    @Test
    public void testMoveFolder()
    {
        final NodeRef grandParent = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef parent = createNode(ContentModel.TYPE_FOLDER, grandParent);
        final NodeRef moveFolder = createNode(ContentModel.TYPE_FOLDER, parent);

        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.moveNode(
                moveFolder,
                grandParent,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(TEST_NAMESPACE));
            return null;
        });

        checkNumOfEvents(4);

        final String grandParentID = getNodeResource(1).getId();
        final String parentID = getNodeResource(2).getId();

        final String moveFolderParentBeforeMove =
            getNodeResourceBefore(4).getPrimaryHierarchy().get(0);
        final String moveFolderParentAfterMove =
            getNodeResource(4).getPrimaryHierarchy().get(0);

        assertEquals("Wrong node parent.", parentID, moveFolderParentBeforeMove);
        assertEquals("Wrong node parent.", grandParentID, moveFolderParentAfterMove);
        assertEquals("Wrong repo event type.", EventType.NODE_UPDATED.getType(),
            getRepoEventWithoutWait(4).getType());
    }

    @Test
    public void testMoveFolderStructure()
    {
        final NodeRef root1 = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef root2 = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef grandParent = createNode(ContentModel.TYPE_FOLDER, root1);
        final NodeRef parent = createNode(ContentModel.TYPE_FOLDER, grandParent);
        createNode(ContentModel.TYPE_CONTENT, parent);

        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.moveNode(
                grandParent,
                root2,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(TEST_NAMESPACE));
            return null;
        });

        checkNumOfEvents(6);

        final String root2ID = getNodeResource(2).getId();
        final String grandParentParentAfterMove =
            getNodeResource(6).getPrimaryHierarchy().get(0);
        assertEquals("Wrong node parent.", root2ID, grandParentParentAfterMove);

        final String grandParentID = getNodeResource(3).getId();
        final String parentIDOfTheParentFolder =
            getNodeResource(4).getPrimaryHierarchy().get(0);
        assertEquals("Wrong node parent.", grandParentID, parentIDOfTheParentFolder);

        final String parentID = getNodeResource(4).getId();
        final String contentParentID =
            getNodeResource(5).getPrimaryHierarchy().get(0);
        assertEquals("Wrong node parent.", parentID, contentParentID);
    }

    @Test
    public void testMoveNodeWithAspects()
    {
        final NodeRef folder1 = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef folder2 = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef moveFile = createNode(ContentModel.TYPE_CONTENT, folder1);

        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.addAspect(moveFile, ContentModel.ASPECT_VERSIONABLE, null);
            return null;
        });

        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.moveNode(
                moveFile,
                folder2,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(TEST_NAMESPACE));
            return null;
        });

        NodeResource resource = getNodeResource(5);
        assertNotNull(resource.getAspectNames());
        assertTrue("Wrong aspect.", resource.getAspectNames().contains("cm:versionable"));

        final String folder2ID = getNodeResource(2).getId();
        final String moveFileParentAfterMove =
            getNodeResource(5).getPrimaryHierarchy().get(0);

        assertEquals("Wrong node parent.", folder2ID, moveFileParentAfterMove);
    }

    @Test
    public void testMoveNodeWithProperties()
    {
        final NodeRef folder1 = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef folder2 = createNode(ContentModel.TYPE_FOLDER);
        final NodeRef moveFile = createNode(ContentModel.TYPE_CONTENT, folder1);

        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.setProperty(moveFile, ContentModel.PROP_NAME, "test_new_name");

            nodeService.moveNode(
                moveFile,
                folder2,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(TEST_NAMESPACE));
            return null;
        });

        NodeResource resource = getNodeResource(4);
        assertEquals("test_new_name", resource.getName());

        final String folder2ID = getNodeResource(2).getId();
        final String moveFileParentAfterMove =
            getNodeResource(4).getPrimaryHierarchy().get(0);

        assertEquals("Wrong node parent.", folder2ID, moveFileParentAfterMove);
    }

    @Test
    public void testCreateAndMoveFileInTheSameTransaction()
    {
        retryingTransactionHelper.doInTransaction(() -> {

            NodeRef folder1 = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(TEST_NAMESPACE),
                ContentModel.TYPE_FOLDER).getChildRef();

            NodeRef folder2 = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(TEST_NAMESPACE),
                ContentModel.TYPE_FOLDER).getChildRef();

            NodeRef fileToMove = nodeService.createNode(
                folder1,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(TEST_NAMESPACE),
                ContentModel.TYPE_CONTENT).getChildRef();

            nodeService.moveNode(
                fileToMove,
                folder2,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(TEST_NAMESPACE));

            assertEquals(folder2, nodeService.getPrimaryParent(fileToMove).getParentRef());

            return null;
        });

        checkNumOfEvents(3);

        final String folder2ID = getNodeResource(2).getId();
        final String moveFileParentAfterMove =
            getNodeResource(3).getPrimaryHierarchy().get(0);

        assertEquals("Wrong node parent.", folder2ID, moveFileParentAfterMove);
    }

    @Test
    public void testAddAspectRemoveAspectFromContentSameTransactionTest()
    {
        final NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT);
        NodeResource resource = getNodeResource(1);
        final Set<String> originalAspects = resource.getAspectNames();
        assertNotNull(originalAspects);


        retryingTransactionHelper.doInTransaction(() -> {
            // Add cm:geographic aspect with default value
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_GEOGRAPHIC, null);

            // Remove cm:geographic aspect
            nodeService.removeAspect(nodeRef, ContentModel.ASPECT_GEOGRAPHIC);
            return null;
        });

        checkNumOfEvents(1);
    }

    @Test
    public void testAddAspectRemoveAspectAddAspectFromContentSameTransactionTest()
    {
        final NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT);
        NodeResource resource = getNodeResource(1);
        final Set<String> originalAspects = resource.getAspectNames();
        assertNotNull(originalAspects);
        
        retryingTransactionHelper.doInTransaction(() -> {
            // Add cm:geographic aspect with default value
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_GEOGRAPHIC, null);

            // Remove cm:geographic aspect
            nodeService.removeAspect(nodeRef, ContentModel.ASPECT_GEOGRAPHIC);

            // Add cm:geographic aspect with default value
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_GEOGRAPHIC, null);

            return null;
        });

        checkNumOfEvents(2);

        resource = getNodeResource(2);
        Set<String> aspectsAfter = resource.getAspectNames();
        assertNotNull(aspectsAfter);
        assertEquals(2, aspectsAfter.size());
        assertTrue(aspectsAfter.contains("cm:auditable"));
        assertTrue(aspectsAfter.contains("cm:auditable"));

        NodeResource resourceBefore = getNodeResourceBefore(2);
        Set<String> aspectsBefore = resourceBefore.getAspectNames();
        assertNotNull(aspectsBefore);
        assertEquals(1, aspectsBefore.size());
        assertTrue(aspectsBefore.contains("cm:auditable"));
    }
}
