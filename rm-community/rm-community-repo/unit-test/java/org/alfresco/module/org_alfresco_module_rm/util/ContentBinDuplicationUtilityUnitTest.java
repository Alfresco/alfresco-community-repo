/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.alfresco.model.ContentModel;
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

    @InjectMocks
    private ContentBinDuplicationUtility contentBinDuplicationUtility;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Tests that the requests are made to disable and re-enable the audit and versioning and to update the content bin
     */
    @Test
    public void testContentUrlIsUpdated()
    {
        NodeRef nodeRef = new NodeRef("some://test/noderef");
        when(mockContentService.getReader(nodeRef, ContentModel.PROP_CONTENT)).thenReturn(mockContentReader);
        when(mockContentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true)).thenReturn(mockContentWriter);
        contentBinDuplicationUtility.duplicate(nodeRef);
        verify(mockContentWriter, times(1)).putContent(mockContentReader);
        checkBehaviours(1);
    }

    /**
     * Test content duplication doesn't happen when node has no content
     */
    @Test
    public void testDuplicationDoesntHappenWithNoContent()
    {
        NodeRef nodeRef = new NodeRef("some://test/noderef");
        when(mockContentService.getReader(nodeRef, ContentModel.PROP_CONTENT)).thenReturn(null);
        contentBinDuplicationUtility.duplicate(nodeRef);
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
     * Check that the behaviours are disabled and re-enabled the correct number of times
     * @param times the times the behaviours should be called
     */
    private void checkBehaviours(int times)
    {
        verify(mockBehaviourFilter, times(times)).disableBehaviour();
        verify(mockBehaviourFilter, times(times)).enableBehaviour();
    }
}
