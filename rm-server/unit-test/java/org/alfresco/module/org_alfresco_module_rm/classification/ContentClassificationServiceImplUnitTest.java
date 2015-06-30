/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.classification;

import static com.google.common.collect.Sets.newHashSet;
import static org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock.generateNodeRef;
import static org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock.generateText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.model.QuickShareModel;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.InvalidNode;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.LevelIdNotFound;
import org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.MockAuthenticationUtilHelper;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

/**
 * Unit tests for {@link ContentClassificationServiceImpl}.
 *
 * @author tpage
 */
public class ContentClassificationServiceImplUnitTest implements ClassifiedContentModel
{
    private static final String CLASSIFICATION_LEVEL_ID = "classificationLevelId";
    private static final ClassificationLevel CLASSIFICATION_LEVEL = new ClassificationLevel(CLASSIFICATION_LEVEL_ID, generateText());

    @InjectMocks ContentClassificationServiceImpl contentClassificationServiceImpl;
    @Mock ClassificationLevelManager mockLevelManager;
    @Mock ClassificationReasonManager mockReasonManager;
    @Mock NodeService mockNodeService;
    @Mock DictionaryService mockDictionaryService;
    @Mock SecurityClearanceService mockSecurityClearanceService;
    @Mock AuthenticationUtil mockAuthenticationUtil;
    @Captor ArgumentCaptor<Map<QName, Serializable>> propertiesCaptor;

    @Before public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        MockAuthenticationUtilHelper.setup(mockAuthenticationUtil);
        contentClassificationServiceImpl.setAuthenticationUtil(mockAuthenticationUtil);
    }

    /** Classify a piece of content with a couple of reasons and check the NodeService is called correctly. */
    @Test public void classifyContent_success()
    {
        // Create a level and two reasons.
        ClassificationLevel level = new ClassificationLevel("levelId1", "displayLabelKey");
        ClassificationReason reason1 = new ClassificationReason("reasonId1", "displayLabelKey1");
        ClassificationReason reason2 = new ClassificationReason("reasonId2", "displayLabelKey2");
        // Set up the managers to return these objects when the ids are provided.
        when(mockLevelManager.findLevelById("levelId1")).thenReturn(level);
        when(mockReasonManager.findReasonById("reasonId1")).thenReturn(reason1);
        when(mockReasonManager.findReasonById("reasonId2")).thenReturn(reason2);
        // Create a content node.
        NodeRef content = new NodeRef("fake://content/");
        when(mockDictionaryService.isSubClass(mockNodeService.getType(content), ContentModel.TYPE_CONTENT)).thenReturn(true);
        when(mockNodeService.hasAspect(content, ClassifiedContentModel.ASPECT_CLASSIFIED)).thenReturn(false);
        when(mockSecurityClearanceService.isCurrentUserClearedForClassification("levelId1")).thenReturn(true);

        // Call the method under test
        contentClassificationServiceImpl.classifyContent("levelId1", "classifiedBy", "classificationAgency",
                Sets.newHashSet("reasonId1", "reasonId2"), content);

        verify(mockNodeService).addAspect(eq(content), eq(ClassifiedContentModel.ASPECT_CLASSIFIED),
                    propertiesCaptor.capture());
        // Check the properties that were received.
        Map<QName, Serializable> properties = propertiesCaptor.getValue();
        HashSet<QName> expectedPropertyKeys = Sets.newHashSet(ClassifiedContentModel.PROP_INITIAL_CLASSIFICATION,
                    ClassifiedContentModel.PROP_CURRENT_CLASSIFICATION,
                    ClassifiedContentModel.PROP_CLASSIFICATION_AGENCY,
                    ClassifiedContentModel.PROP_CLASSIFIED_BY,
                    ClassifiedContentModel.PROP_CLASSIFICATION_REASONS);
        assertEquals("Aspect created with unexpected set of keys.", expectedPropertyKeys, properties.keySet());
        assertEquals("Unexpected initial classification.", level.getId(), properties.get(ClassifiedContentModel.PROP_INITIAL_CLASSIFICATION));
        assertEquals("Unexpected current classification.", level.getId(), properties.get(ClassifiedContentModel.PROP_CURRENT_CLASSIFICATION));
        assertEquals("Unexpected classifiedBy.", "classifiedBy", properties.get(ClassifiedContentModel.PROP_CLASSIFIED_BY));
        assertEquals("Unexpected agency.", "classificationAgency", properties.get(ClassifiedContentModel.PROP_CLASSIFICATION_AGENCY));
        Set<String> expectedReasonIds = Sets.newHashSet("reasonId1", "reasonId2");
        assertEquals("Unexpected set of reasons.", expectedReasonIds, properties.get(ClassifiedContentModel.PROP_CLASSIFICATION_REASONS));
    }

    /** Classify a folder using the <code>classifyContent</code> method and check that an exception is raised. */
    @Test(expected = InvalidNode.class)
    public void classifyContent_notContent()
    {
        // Create a folder node.
        NodeRef notAPieceOfContent = new NodeRef("not://a/piece/of/content/");
        when(mockNodeService.getType(notAPieceOfContent)).thenReturn(ContentModel.TYPE_FOLDER);

        // Call the method under test.
        contentClassificationServiceImpl.classifyContent("levelId1", "classifiedBy", "classificationAgency",
                    Sets.newHashSet("reasonId1", "reasonId2"), notAPieceOfContent);
    }

    /** Classify a piece of content that has already been classified. */
    @Test(expected = UnsupportedOperationException.class)
    public void classifyContent_alreadyClassified()
    {
        // Create a classified piece of content.
        NodeRef classifiedContent = new NodeRef("classified://content/");
        when(mockDictionaryService.isSubClass(mockNodeService.getType(classifiedContent), ContentModel.TYPE_CONTENT)).thenReturn(true);
        when(mockNodeService.hasAspect(classifiedContent, ClassifiedContentModel.ASPECT_CLASSIFIED)).thenReturn(true);

        // Call the method under test.
        contentClassificationServiceImpl.classifyContent("levelId1", "classifiedBy", "classificationAgency",
                    Sets.newHashSet("reasonId1", "reasonId2"), classifiedContent);
    }

    /** Classify a piece of content that has already been shared */
    @Test(expected = IllegalStateException.class)
    public void classifySharedContent()
    {
        NodeRef sharedContent = generateNodeRef(mockNodeService);
        when(mockDictionaryService.isSubClass(mockNodeService.getType(sharedContent), ContentModel.TYPE_CONTENT)).thenReturn(true);
        when(mockNodeService.hasAspect(sharedContent, QuickShareModel.ASPECT_QSHARE)).thenReturn(true);

        // Call the method under test.
        contentClassificationServiceImpl.classifyContent(generateText(), generateText(), generateText(),
                newHashSet(generateText(), generateText()), sharedContent);
    }

    /**
     * Check that a user can't classify content with a level that either (a) doesn't exist, or (b) they don't have
     * clearance for.  (Both cases are covered by the same flow through the code).
     */
    @Test(expected = LevelIdNotFound.class)
    public void classifyContent_notFound()
    {
        // Create a classified piece of content.
        NodeRef classifiedContent = new NodeRef("classified://content/");
        when(mockDictionaryService.isSubClass(mockNodeService.getType(classifiedContent), ContentModel.TYPE_CONTENT)).thenReturn(true);
        when(mockNodeService.hasAspect(classifiedContent, ClassifiedContentModel.ASPECT_CLASSIFIED)).thenReturn(false);
        when(mockSecurityClearanceService.isCurrentUserClearedForClassification("levelId1")).thenReturn(false);

        // Call the method under test.
        contentClassificationServiceImpl.classifyContent("levelId1", "classifiedBy","classificationAgency",
                    Sets.newHashSet("reasonId1", "reasonId2"), classifiedContent);
    }

    /**
     * Given that a node does not have the classify aspect applied
     * When I ask for the nodes classification
     * Then 'Unclassified' is returned
     */
    @Test
    public void getCurrentClassificationWithoutAspectApplied()
    {
        NodeRef nodeRef = generateNodeRef(mockNodeService);
        when(mockNodeService.hasAspect(nodeRef, ClassifiedContentModel.ASPECT_CLASSIFIED))
            .thenReturn(false);

        ClassificationLevel classificationLevel = contentClassificationServiceImpl.getCurrentClassification(nodeRef);

        assertEquals(ClassificationLevelManager.UNCLASSIFIED, classificationLevel);
        verify(mockNodeService).hasAspect(nodeRef, ClassifiedContentModel.ASPECT_CLASSIFIED);
        verifyNoMoreInteractions(mockNodeService);
    }

    /**
     * Given that a node is classified
     * When I ask for the node classification
     * Then I get the correct classificationlevel
     */
    @Test
    public void getCurrentClassification()
    {
        NodeRef nodeRef = generateNodeRef(mockNodeService);
        when(mockNodeService.hasAspect(nodeRef, ClassifiedContentModel.ASPECT_CLASSIFIED))
            .thenReturn(true);
        when(mockNodeService.getProperty(nodeRef, ClassifiedContentModel.PROP_CURRENT_CLASSIFICATION))
            .thenReturn(CLASSIFICATION_LEVEL_ID);
        when(mockLevelManager.findLevelById(CLASSIFICATION_LEVEL_ID))
            .thenReturn(CLASSIFICATION_LEVEL);

        ClassificationLevel classificationLevel = contentClassificationServiceImpl.getCurrentClassification(nodeRef);

        assertEquals(CLASSIFICATION_LEVEL, classificationLevel);
        verify(mockNodeService).hasAspect(nodeRef, ClassifiedContentModel.ASPECT_CLASSIFIED);
        verify(mockNodeService).getProperty(nodeRef, ClassifiedContentModel.PROP_CURRENT_CLASSIFICATION);
        verify(mockLevelManager).findLevelById(CLASSIFICATION_LEVEL_ID);
        verifyNoMoreInteractions(mockNodeService, mockLevelManager);
    }

    /**
     * Given that the node is classified
     * And the user has no security clearance
     * When I ask if the current user has clearance
     * Then false
     */
    @Test public void userWithNoClearanceIsntClearedOnClassifiedNode()
    {
        // assign test classification to node
        NodeRef nodeRef = generateNodeRef(mockNodeService);
        when(mockNodeService.hasAspect(nodeRef, ASPECT_CLASSIFIED)).thenReturn(true);
        String classificationLevelId = generateText();
        when(mockNodeService.getProperty(nodeRef, PROP_CURRENT_CLASSIFICATION)).thenReturn(classificationLevelId);
        ClassificationLevel classificationLevel = new ClassificationLevel(classificationLevelId, generateText());
        when(mockLevelManager.findLevelById(classificationLevelId)).thenReturn(classificationLevel);

        // create user with no clearance
        SecurityClearance clearance = new SecurityClearance(mock(PersonInfo.class), ClearanceLevelManager.NO_CLEARANCE);
        when(mockSecurityClearanceService.getUserSecurityClearance()).thenReturn(clearance);

        assertFalse(contentClassificationServiceImpl.hasClearance(nodeRef));
    }

    /**
     * Given that the node is classified
     * And the user has clearance greater or equal to the the classification
     * When I ask if the user has clearance
     * Then true
     */
    @Test public void classifiedNodeUserClearanceAtLeast()
    {
        // init classification levels
        ClassificationLevel topSecret = new ClassificationLevel("TopSecret", generateText());
        String secretId = "Secret";
        ClassificationLevel secret = new ClassificationLevel(secretId, generateText());
        ClassificationLevel confidential = new ClassificationLevel("Confidential", generateText());
        List<ClassificationLevel> classificationLevels = Arrays.asList(topSecret, secret, confidential, ClassificationLevelManager.UNCLASSIFIED);
        when(mockLevelManager.getClassificationLevels()).thenReturn(ImmutableList.copyOf(classificationLevels));

        // set nodes classification
        NodeRef nodeRef = generateNodeRef(mockNodeService);
        when(mockNodeService.hasAspect(nodeRef, ASPECT_CLASSIFIED)).thenReturn(true);
        when(mockNodeService.getProperty(nodeRef, PROP_CURRENT_CLASSIFICATION)).thenReturn(secretId);
        when(mockLevelManager.findLevelById(secretId)).thenReturn(secret);

        // set users security clearance
        when(mockSecurityClearanceService.isCurrentUserClearedForClassification("Secret")).thenReturn(true);

        assertTrue(contentClassificationServiceImpl.hasClearance(nodeRef));
    }

    /**
     * Given that the node is classified
     * And the user has clearance less than the classification
     * When I ask if the user has clearance
     * Then false
     */
    @Test public void classifiedNodeUserClearanceLess()
    {
        // init classification levels
        ClassificationLevel topSecret = new ClassificationLevel("TopSecret", generateText());
        String secretId = "Secret";
        ClassificationLevel secret = new ClassificationLevel(secretId, generateText());
        ClassificationLevel confidential = new ClassificationLevel("Confidential", generateText());
        List<ClassificationLevel> classificationLevels = Arrays.asList(topSecret, secret, confidential, ClassificationLevelManager.UNCLASSIFIED);
        when(mockLevelManager.getClassificationLevels()).thenReturn(ImmutableList.copyOf(classificationLevels));

        // set nodes classification
        NodeRef nodeRef = generateNodeRef(mockNodeService);
        when(mockNodeService.hasAspect(nodeRef, ASPECT_CLASSIFIED)).thenReturn(true);
        when(mockNodeService.getProperty(nodeRef, PROP_CURRENT_CLASSIFICATION)).thenReturn(secretId);
        when(mockLevelManager.findLevelById(secretId)).thenReturn(secret);

        // set users security clearance
        when(mockSecurityClearanceService.isCurrentUserClearedForClassification("Secret")).thenReturn(false);

        assertFalse(contentClassificationServiceImpl.hasClearance(nodeRef));
    }

    /**
     * Given that I classify a node with a level not equal to "Unclassified"
     * When I ask if the node is classified
     * Then return true
     */
    @Test public void contentClassified_levelNotUnclassified()
    {
        NodeRef nodeRef = generateNodeRef(mockNodeService);

        when(mockNodeService.getProperty(nodeRef, PROP_CURRENT_CLASSIFICATION)).thenReturn("level1");
        when(mockNodeService.hasAspect(nodeRef, ASPECT_CLASSIFIED)).thenReturn(true);

        assertTrue(contentClassificationServiceImpl.isClassified(nodeRef));
    }

    /**
     * Given that I classify a node with level "Unclassified"
     * When I ask if the node is classified
     * Then return false
     */
    @Test public void contentClassified_levelUnclassified()
    {
        NodeRef nodeRef = generateNodeRef(mockNodeService);

        when(mockNodeService.getProperty(nodeRef, PROP_CURRENT_CLASSIFICATION)).thenReturn(ClassificationLevelManager.UNCLASSIFIED_ID);
        when(mockNodeService.hasAspect(nodeRef, ASPECT_CLASSIFIED)).thenReturn(true);

        assertFalse(contentClassificationServiceImpl.isClassified(nodeRef));
    }

    /**
     * Given that a node is not classified
     * When I ask if the node is classified
     * Then return false
     */
    @Test public void contentNotClassified()
    {
        NodeRef nodeRef = generateNodeRef(mockNodeService);

        when(mockNodeService.hasAspect(nodeRef, ASPECT_CLASSIFIED)).thenReturn(false);

        assertFalse(contentClassificationServiceImpl.isClassified(nodeRef));
    }
}
