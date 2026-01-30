/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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

import static org.alfresco.model.ContentModel.PROP_DESCRIPTION;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Type;
import org.alfresco.repo.event.v1.model.ContentInfo;
import org.alfresco.repo.event.v1.model.EventData;
import org.alfresco.repo.event.v1.model.EventType;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.service.cmr.dictionary.CustomModelDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;

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
        assertNotNull("Content should not have been null.", resourceBefore.getContent());

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
        assertNull(resourceBefore.getPrimaryAssocQName());
    }

    @Test
    public void testUpdateNodeResourceContent_NullBefore()
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
        assertNotNull("Content should not have been null.", resourceBefore.getContent());
        content = resourceBefore.getContent();
        assertNull(content.getMimeType());
        assertNull(content.getEncoding());
        assertNull(content.getSizeInBytes());
        assertNotNull(resourceBefore.getModifiedAt());

        // Apart from the 'content' and 'modifiedAt' properties the rest should not be set
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
        assertNull(resourceBefore.getPrimaryAssocQName());
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
        assertNotNull("Content should not have been null.", resourceBefore.getContent());

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
        assertNull(resourceBefore.getPrimaryAssocQName());
    }

    @Test
    public void testUpdateContentWithLocalizedProperties()
    {
        final String description = "cm:description";
        final NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT);
        NodeResource resource = getNodeResource(1);

        assertNull(getProperty(resource, description));
        assertNull(resource.getLocalizedProperties());
        assertNull(getEventData(1).getResourceBefore());

        retryingTransactionHelper.doInTransaction(() -> {
            final MLText localizedDescription = new MLText(germanLocale, "german description");
            localizedDescription.addValue(defaultLocale, "default description");
            localizedDescription.addValue(japaneseLocale, "japanese description");

            nodeService.setProperty(nodeRef, PROP_DESCRIPTION, localizedDescription);
            return null;
        });

        resource = getNodeResource(2);
        NodeResource resourceBefore = getNodeResourceBefore(2);

        assertEquals("default description", getProperty(resource, description));
        assertEquals("default description", getLocalizedProperty(resource, description, defaultLocale));
        assertEquals("german description", getLocalizedProperty(resource, description, germanLocale));
        assertEquals("japanese description", getLocalizedProperty(resource, description, japaneseLocale));
        assertNull(getLocalizedProperty(resourceBefore, description, defaultLocale));
        assertNull(getLocalizedProperty(resourceBefore, description, germanLocale));
        assertNull(getLocalizedProperty(resourceBefore, description, japaneseLocale));

        retryingTransactionHelper.doInTransaction(() -> {
            final MLText localizedDescription = new MLText(frenchLocale, "french description added");
            localizedDescription.addValue(defaultLocale, "default description modified");
            localizedDescription.addValue(japaneseLocale, "japanese description");

            nodeService.setProperty(nodeRef, PROP_DESCRIPTION, localizedDescription);
            return null;
        });

        resource = getNodeResource(3);
        resourceBefore = getNodeResourceBefore(3);

        assertEquals("default description modified", getProperty(resource, description));
        assertEquals("default description modified", getLocalizedProperty(resource, description, defaultLocale));
        assertEquals("french description added", getLocalizedProperty(resource, description, frenchLocale));
        assertEquals("japanese description", getLocalizedProperty(resource, description, japaneseLocale));
        assertFalse(containsLocalizedProperty(resource, description, germanLocale));
        assertEquals("default description", getLocalizedProperty(resourceBefore, description, defaultLocale));
        assertEquals("german description", getLocalizedProperty(resourceBefore, description, germanLocale));
        assertNull(getLocalizedProperty(resourceBefore, description, frenchLocale));
        assertFalse(containsLocalizedProperty(resourceBefore, description, japaneseLocale));
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
        NodeResource resourceBefore = getNodeResourceBefore(2);
        title = getProperty(resource, "cm:title");
        assertEquals("test title", title);
        assertEquals("test title", getLocalizedProperty(resource, "cm:title", defaultLocale));
        assertNull(getLocalizedProperty(resourceBefore, "cm:title", defaultLocale));

        // update content cm:title property again with "new test title" value
        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, "new test title");
            return null;
        });

        resource = getNodeResource(3);
        title = getProperty(resource, "cm:title");
        assertEquals("new test title", title);
        assertEquals("new test title", getLocalizedProperty(resource, "cm:title", defaultLocale));

        resourceBefore = getNodeResourceBefore(3);
        title = getProperty(resourceBefore, "cm:title");
        assertEquals("Wrong old property.", "test title", title);
        assertEquals("test title", getLocalizedProperty(resourceBefore, "cm:title", defaultLocale));
        assertNotNull(resourceBefore.getModifiedAt());
    }

    @Test
    public void testUpdateContentTitleFromNull()
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

        NodeResource resourceBefore = getNodeResourceBefore(2);
        Map<String, Serializable> expectedResourceBeforeProperties = new HashMap<>();
        expectedResourceBeforeProperties.put("cm:title", null);
        assertEquals(expectedResourceBeforeProperties, resourceBefore.getProperties());

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
            nodeService.setProperty(nodeRef, PROP_DESCRIPTION, "test description");
            return null;
        });

        resource = getNodeResource(2);
        desc = getProperty(resource, "cm:description");
        assertEquals("test description", desc);

        NodeResource resourceBefore = getNodeResourceBefore(2);
        assertNotNull(resourceBefore.getProperties());

        Map<String, Serializable> expectedResourceBeforeProperties = new HashMap<>();
        expectedResourceBeforeProperties.put("cm:description", null);
        assertEquals(expectedResourceBeforeProperties, resourceBefore.getProperties());
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
        // Check all aspects
        Set<String> expectedAspects = new HashSet<>(originalAspects);
        expectedAspects.add("cm:versionable");
        assertEquals(expectedAspects, resource.getAspectNames());
        // Check properties
        assertFalse(resource.getProperties().isEmpty());

        // Check resourceBefore
        NodeResource resourceBefore = getNodeResourceBefore(2);
        assertNotNull(resourceBefore.getAspectNames());
        assertEquals(originalAspects, resourceBefore.getAspectNames());
        assertNotNull(resourceBefore.getProperties());

        Map<String, Serializable> expectedResourceBeforeProperties = new HashMap<>();
        expectedResourceBeforeProperties.put("cm:autoVersion", null);
        expectedResourceBeforeProperties.put("cm:initialVersion", null);
        expectedResourceBeforeProperties.put("cm:versionType", null);
        expectedResourceBeforeProperties.put("cm:autoVersionOnUpdateProps", null);
        expectedResourceBeforeProperties.put("cm:versionLabel", null);
        assertEquals(expectedResourceBeforeProperties, resourceBefore.getProperties());
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
    public void testRemoveAspectPropertiesTest()
    {
        final NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT);
        NodeResource resource = getNodeResource(1);
        final Set<String> originalAspects = resource.getAspectNames();
        assertNotNull(originalAspects);

        // Add cm:geographic aspect with properties
        retryingTransactionHelper.doInTransaction(() -> {
            Map<QName, Serializable> aspectProperties = new HashMap<>();
            aspectProperties.put(ContentModel.PROP_LATITUDE, "12.345678");
            aspectProperties.put(ContentModel.PROP_LONGITUDE, "12.345678");
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_GEOGRAPHIC, aspectProperties);
            return null;
        });
        resource = getNodeResource(2);
        Set<String> aspectsBeforeRemove = resource.getAspectNames();
        assertNotNull(aspectsBeforeRemove);
        assertTrue(aspectsBeforeRemove.contains("cm:geographic"));

        // Remove cm:geographic aspect - this automatically removes the properties from the node
        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.removeAspect(nodeRef, ContentModel.ASPECT_GEOGRAPHIC);
            return null;
        });

        resource = getNodeResource(3);
        assertEquals(originalAspects, resource.getAspectNames());

        NodeResource resourceBefore = getNodeResourceBefore(3);
        assertNotNull(resourceBefore.getAspectNames());
        assertEquals(aspectsBeforeRemove, resourceBefore.getAspectNames());
        // Resource before should contain cm:latitude and cm:longitude properties
        assertNotNull(resourceBefore.getProperties());
        assertTrue(resourceBefore.getProperties().containsKey("cm:latitude"));
        assertTrue(resourceBefore.getProperties().containsKey("cm:longitude"));
        // Resource after should NOT contain cm:latitude and cm:longitude properties
        assertNotNull(resource.getProperties());
        assertFalse(resource.getProperties().containsKey("cm:latitude"));
        assertFalse(resource.getProperties().containsKey("cm:longitude"));
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

            nodeService.setProperty(node1, PROP_DESCRIPTION, "test description");
            return null;
        });
        // Create and update node are done in the same transaction so one event is expected
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
        assertEquals("Incorrect node type was found", "cm:content", resourceBefore.getNodeType());
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
        assertNull(resourceBefore.getPrimaryAssocQName());
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
        CustomModelDefinition modelDefinition = retryingTransactionHelper.doInTransaction(() -> customModelService.createCustomModel(model, true));

        assertNotNull(modelDefinition);
        assertEquals(modelName, modelDefinition.getName().getLocalName());
        assertEquals(modelDescription, modelDefinition.getDescription());

        // List all of the model's types
        Collection<TypeDefinition> types = modelDefinition.getTypeDefinitions();
        assertEquals(1, types.size());

        // we should have only 2 events, node.Created and node.Updated
        checkNumOfEvents(2);

        // node.Created event should be generated for the model
        RepoEvent<EventData<NodeResource>> resultRepoEvent = getFilteredEvent(EventType.NODE_CREATED, 0);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());
        NodeResource nodeResource = getNodeResource(resultRepoEvent);
        assertEquals("Incorrect node type was found", "cm:dictionaryModel", nodeResource.getNodeType());

        initTestNamespacePrefixMapping();
        final NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT);
        // old node's type
        assertEquals(ContentModel.TYPE_CONTENT, nodeService.getType(nodeRef));

        // node.Created event should be generated
        resultRepoEvent = getRepoEvent(3);
        nodeResource = getNodeResource(resultRepoEvent);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());
        assertEquals("cm:content node type was not found", "cm:content", nodeResource.getNodeType());

        QName typeQName = QName.createQName("{" + namespacePair.getFirst() + "}" + typeName);
        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.setType(nodeRef, typeQName);

            // new node's type
            assertEquals(typeQName, nodeService.getType(nodeRef));
            return null;
        });

        // we should have 4 events, node.Created for the model, node.Updated for the parent, node.Created for the node and node.Updated
        checkNumOfEvents(4);

        resultRepoEvent = getRepoEvent(4);
        assertEquals("Wrong repo event type.", EventType.NODE_UPDATED.getType(), resultRepoEvent.getType());
        nodeResource = getNodeResource(resultRepoEvent);
        assertEquals("Incorrect node type was found", namespacePair.getSecond() + QName.NAMESPACE_PREFIX + typeName, nodeResource.getNodeType());

        NodeResource resourceBefore = getNodeResourceBefore(4);
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
        assertNull(resourceBefore.getPrimaryAssocQName());
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
        assertNull(resourceBefore.getPrimaryAssocQName());
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

        RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEvent(1);
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
        assertNotNull(resourceBefore.getProperties());
        assertNull(resourceBefore.getAspectNames());
        assertNotNull(resourceBefore.getPrimaryHierarchy());
        assertNull("Content should have been null.", resource.getContent());
        assertNull("Content should have been null.", resourceBefore.getContent());

        assertNotNull(resource.getModifiedAt());
        assertNotNull(resource.getModifiedByUser());
        assertNotNull(resource.getAspectNames());
        assertNull(resource.getContent());
        assertFalse(resource.getProperties().isEmpty());
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

        final String moveFolderParentBeforeMove = getNodeResourceBefore(4).getPrimaryHierarchy().get(0);
        final String moveFolderParentAfterMove = getNodeResource(4).getPrimaryHierarchy().get(0);

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
        final String grandParentParentAfterMove = getNodeResource(6).getPrimaryHierarchy().get(0);
        assertEquals("Wrong node parent.", root2ID, grandParentParentAfterMove);

        final String grandParentID = getNodeResource(3).getId();
        final String parentIDOfTheParentFolder = getNodeResource(4).getPrimaryHierarchy().get(0);
        assertEquals("Wrong node parent.", grandParentID, parentIDOfTheParentFolder);

        final String parentID = getNodeResource(4).getId();
        final String contentParentID = getNodeResource(5).getPrimaryHierarchy().get(0);
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
        final String moveFileParentAfterMove = getNodeResource(5).getPrimaryHierarchy().get(0);

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
        final String moveFileParentAfterMove = getNodeResource(4).getPrimaryHierarchy().get(0);

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
        final String moveFileParentAfterMove = getNodeResource(3).getPrimaryHierarchy().get(0);

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

    /**
     * Test that verifies update event is generated when only cm:modifiedAt property is changed
     * In a transaction cm:userName property is added and removed, resulting in no net change to properties
     * but cm:modifiedAt is updated
     */
    @Test
    public void testAddAndRemovePropertyInTheSameTransaction()
    {
        final NodeRef nodeRef = createNode(ContentModel.TYPE_CONTENT);

        checkNumOfEvents(1);

        NodeResource resource = getNodeResource(1);
        // Check properties
        assertTrue(resource.getProperties().isEmpty());

        // Add and remove cm:userName property
        retryingTransactionHelper.doInTransaction(() -> {
            Map<QName, Serializable> properties = Map.of(ContentModel.PROP_USERNAME, "user1");
            nodeService.addProperties(nodeRef, properties);
            nodeService.removeProperty(nodeRef, ContentModel.PROP_USERNAME);
            return null;
        });

        // No change to properties expected
        resource = getNodeResource(2);
        assertTrue(resource.getProperties().isEmpty());

        // There should be one update event as modifiedAt is updated
        List<RepoEvent<EventData<NodeResource>>> nodeUpdatedEvents = getFilteredEvents(EventType.NODE_UPDATED);
        assertEquals(1, nodeUpdatedEvents.size());
    }
}
