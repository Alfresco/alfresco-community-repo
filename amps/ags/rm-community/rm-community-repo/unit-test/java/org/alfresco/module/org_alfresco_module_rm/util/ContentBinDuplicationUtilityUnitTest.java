/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.util;

import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASPECT_ARCHIVED;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.query.RecordsManagementQueryDAO;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for the ContentBinDuplicationUtility
 * @author Ross Gale
 * @since 2.7.2
 */
public class ContentBinDuplicationUtilityUnitTest
{
    private final static NodeRef NODE_REF = new NodeRef("some://test/noderef");
    private final static NodeRef NODE_REF2 = new NodeRef("some://test/anothernoderef");
    private final static String CONTENT_URL = "someContentUrl";

    @Mock
    private ContentService mockContentService;

    @Mock
    private BehaviourFilter mockBehaviourFilter;

    @Mock
    private ContentReader mockContentReader;

    @Mock
    private ContentWriter mockContentWriter;
    @Mock
    private NodeService mockNodeService;

    @Mock
    private RecordsManagementQueryDAO recordsManagementQueryDAO;

    @InjectMocks
    private ContentBinDuplicationUtility contentBinDuplicationUtility;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        contentBinDuplicationUtility.setNodeService(mockNodeService);
    }

    /**
     * Tests that the requests are made to disable and re-enable the audit and versioning and to update the content bin
     */
    @Test
    public void testContentUrlIsUpdated()
    {
        when(mockContentService.getReader(NODE_REF, ContentModel.PROP_CONTENT)).thenReturn(mockContentReader);
        when(mockContentService.getWriter(NODE_REF, ContentModel.PROP_CONTENT, true)).thenReturn(mockContentWriter);
        contentBinDuplicationUtility.duplicate(NODE_REF);
        verify(mockContentWriter, times(1)).putContent(mockContentReader);
        checkBehaviours(1);
    }

    /**
     * Test content duplication doesn't happen when node has no content
     */
    @Test
    public void testDuplicationDoesntHappenWithNoContent()
    {
        when(mockContentService.getReader(NODE_REF, ContentModel.PROP_CONTENT)).thenReturn(null);
        contentBinDuplicationUtility.duplicate(NODE_REF);
        verify(mockContentWriter, times(0)).putContent(mockContentReader);
        checkBehaviours(1);
    }

    /**
     * This is testing the fix for RM-6788 where archived content couldn't be declared as a record
     * This was caused by attempting to copy the bin file and updating the content url of the
     * archived piece of content which failed as this is a protected property. This is done if
     * the node is/has a copy but the same duplication already happens during archive.
     */
    @Test
    public void testBinFileNotDuplicatedForArchivedContent()
    {
        NodeRef nodeRef = new NodeRef("some://test/noderef");
        when(mockNodeService.hasAspect(nodeRef, ASPECT_ARCHIVED)).thenReturn(true);
        contentBinDuplicationUtility.duplicate(nodeRef);
        verify(mockContentReader, times(0)).getReader();
        checkBehaviours(0);
    }
    /**
     * Test hasAtLeastOneOtherReference returns true when node has another reference to it
     */
    @Test
    public void testHasAtLeastOneOtherReference()
    {
        Set<NodeRef> multipleReferences = new HashSet<>();
        Collections.addAll(multipleReferences, NODE_REF, NODE_REF2);

        when(mockContentService.getReader(NODE_REF, ContentModel.PROP_CONTENT)).thenReturn(mockContentReader);
        when(mockContentService.getReader(NODE_REF, ContentModel.PROP_CONTENT).getContentUrl()).thenReturn(CONTENT_URL);
        when(recordsManagementQueryDAO.getNodeRefsWhichReferenceContentUrl(CONTENT_URL)).thenReturn(multipleReferences);

        assertTrue(contentBinDuplicationUtility.hasAtLeastOneOtherReference(NODE_REF));
    }

    /**
     * Test hasAtLeastOneOtherReference returns false when node has no other reference to it other than its own content ref
     */
    @Test
    public void testHasNoOtherReference()
    {
        Set<NodeRef> singleReference = Collections.singleton(NODE_REF);

        when(mockContentService.getReader(NODE_REF, ContentModel.PROP_CONTENT)).thenReturn(mockContentReader);
        when(mockContentService.getReader(NODE_REF, ContentModel.PROP_CONTENT).getContentUrl()).thenReturn(CONTENT_URL);
        when(recordsManagementQueryDAO.getNodeRefsWhichReferenceContentUrl(CONTENT_URL)).thenReturn(singleReference);

        assertFalse(contentBinDuplicationUtility.hasAtLeastOneOtherReference(NODE_REF));
    }

    /**
     * Test hasAtLeastOneOtherReference returns false when node has no references to it at all
     */
    @Test
    public void testHasNoReferences()
    {
        Set<NodeRef> noReferences = Collections.<NodeRef> emptySet();

        when(mockContentService.getReader(NODE_REF, ContentModel.PROP_CONTENT)).thenReturn(mockContentReader);
        when(mockContentService.getReader(NODE_REF, ContentModel.PROP_CONTENT).getContentUrl()).thenReturn(CONTENT_URL);
        when(recordsManagementQueryDAO.getNodeRefsWhichReferenceContentUrl(CONTENT_URL)).thenReturn(noReferences);

        assertFalse(contentBinDuplicationUtility.hasAtLeastOneOtherReference(NODE_REF));
    }

    /**
     * Check that the behaviours are disabled and re-enabled the correct number of times
     * @param times the times the behaviours should be called
     */
    private void checkBehaviours(int times)
    {
        verify(mockBehaviourFilter, times(times)).disableBehaviour();
        verify(mockBehaviourFilter, times(times)).enableBehaviour();
    }
}
