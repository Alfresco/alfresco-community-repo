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
package org.alfresco.module.org_alfresco_module_rm.model.rma.aspect;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASPECT_ARCHIVED;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASPECT_RECORD;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Unit tests for the {@link RecordAspect}.
 *
 * @author Claudia Agache
 */
public class RecordAspectUnitTest
{
    private static final NodeRef NODE_REF = new NodeRef("node://Ref/");
    private static final NodeRef COPY_REF = new NodeRef("node://Copy/");
    private static final AssociationRef SOURCE_ASSOC_REF = new AssociationRef(COPY_REF, ContentModel.ASSOC_ORIGINAL,
            NODE_REF);
    private static final AssociationRef TARGET_ASSOC_REF = new AssociationRef(NODE_REF, ContentModel.ASSOC_ORIGINAL,
            COPY_REF);

    @InjectMocks
    private RecordAspect recordAspect;
    @Mock
    private NodeService mockNodeService;
    @Mock
    private BehaviourFilter mockBehaviorFilter;
    @Mock
    private ContentService mockContentService;
    @Mock
    private ContentReader mockContentReader;
    @Mock
    private ContentWriter mockContentWriter;
    @Mock
    private ExtendedSecurityService mockExtendedSecurityService;

    @Before
    public void setUp()
    {
        initMocks(this);
    }

    /** Check that the bin is duplicated before adding the aspect if the file has a copy. */
    @Test
    public void testDuplicateBinBeforeAddingAspectForFileWithCopy()
    {
        when(mockNodeService.getSourceAssocs(NODE_REF, ContentModel.ASSOC_ORIGINAL)).thenReturn(asList(SOURCE_ASSOC_REF));
        when(mockContentService.getReader(NODE_REF, ContentModel.PROP_CONTENT)).thenReturn(mockContentReader);
        when(mockContentService.getWriter(NODE_REF, ContentModel.PROP_CONTENT, true)).thenReturn(mockContentWriter);

        recordAspect.beforeAddAspect(NODE_REF, ASPECT_RECORD);

         verifyBeforeAddAspectMethodsInvocations(1);
    }

    /** Check that the bin is duplicated before adding the aspect if the file is a copy. */
    @Test
    public void testDuplicateBinBeforeAddingAspectForCopy()
    {
        when(mockNodeService.getTargetAssocs(NODE_REF, ContentModel.ASSOC_ORIGINAL)).thenReturn(asList(TARGET_ASSOC_REF));
        when(mockContentService.getReader(NODE_REF, ContentModel.PROP_CONTENT)).thenReturn(mockContentReader);
        when(mockContentService.getWriter(NODE_REF, ContentModel.PROP_CONTENT, true)).thenReturn(mockContentWriter);

        recordAspect.beforeAddAspect(NODE_REF, ASPECT_RECORD);

         verifyBeforeAddAspectMethodsInvocations(1);
    }

    /** Check that no content bin is created if the file does not have content. */
    @Test
    public void testBeforeAddAspectOnFileWithNoContent()
    {
        when(mockNodeService.getTargetAssocs(NODE_REF, ContentModel.ASSOC_ORIGINAL)).thenReturn(asList(TARGET_ASSOC_REF));
        when(mockContentService.getReader(NODE_REF, ContentModel.PROP_CONTENT)).thenReturn(null);

        recordAspect.beforeAddAspect(NODE_REF, ASPECT_RECORD);

        verify(mockBehaviorFilter, times(1)).disableBehaviour(eq(ContentModel.ASPECT_AUDITABLE));
        verify(mockBehaviorFilter, times(1)).disableBehaviour(eq(ContentModel.ASPECT_VERSIONABLE));
        verify(mockContentService, times(1)).getReader(NODE_REF, ContentModel.PROP_CONTENT);
        verify(mockContentService, never()).getWriter(NODE_REF, ContentModel.PROP_CONTENT, true);
        verify(mockBehaviorFilter, times(1)).enableBehaviour(eq(ContentModel.ASPECT_AUDITABLE));
        verify(mockBehaviorFilter, times(1)).enableBehaviour(eq(ContentModel.ASPECT_VERSIONABLE));
    }

    /** Check that the bin is not duplicated before adding the aspect if the node has no copies. */
    @Test
    public void testNotDuplicateBinForFileWithNoCopies()
    {
        when(mockNodeService.getSourceAssocs(NODE_REF, ContentModel.ASSOC_ORIGINAL)).thenReturn(emptyList());
        when(mockNodeService.getTargetAssocs(NODE_REF, ContentModel.ASSOC_ORIGINAL)).thenReturn(emptyList());

        recordAspect.beforeAddAspect(NODE_REF, ASPECT_RECORD);

        verifyBeforeAddAspectMethodsInvocations(0);
    }

    /** Check that the bin is duplicated when copying a record. */
    @Test
    public void testDuplicateBinWhenCopyingRecord()
    {
        when(mockNodeService.exists(COPY_REF)).thenReturn(true);
        when(mockNodeService.hasAspect(COPY_REF, ASPECT_RECORD)).thenReturn(true);
        when(mockContentService.getReader(COPY_REF, ContentModel.PROP_CONTENT)).thenReturn(mockContentReader);
        when(mockContentService.getWriter(COPY_REF, ContentModel.PROP_CONTENT, true)).thenReturn(mockContentWriter);

        recordAspect.onCopyComplete(null, NODE_REF, COPY_REF, true, null);

        verify(mockExtendedSecurityService, times(1)).remove(COPY_REF);
        verify(mockContentService, times(1)).getReader(COPY_REF, ContentModel.PROP_CONTENT);
        verify(mockContentService, times(1)).getWriter(COPY_REF, ContentModel.PROP_CONTENT, true);
        verify(mockContentWriter, times(1)).putContent(mockContentReader);
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
        when(mockNodeService.getTargetAssocs(NODE_REF, ContentModel.ASSOC_ORIGINAL)).thenReturn(asList(TARGET_ASSOC_REF));
        when(mockContentService.getReader(NODE_REF, ContentModel.PROP_CONTENT)).thenReturn(null);
        when(mockNodeService.hasAspect(NODE_REF, ASPECT_ARCHIVED)).thenReturn(true);
        recordAspect.beforeAddAspect(NODE_REF, ASPECT_RECORD);

        verifyBeforeAddAspectMethodsInvocations(0);
    }

    /**
     * Helper to verify beforeAddAspect methods invocations
     *
     * @param wantedNumberOfInvocations wanted number of invocations for each method
     */
    private void verifyBeforeAddAspectMethodsInvocations(int wantedNumberOfInvocations)
    {
        verify(mockBehaviorFilter, times(wantedNumberOfInvocations)).disableBehaviour(eq(ContentModel.ASPECT_AUDITABLE));
        verify(mockBehaviorFilter, times(wantedNumberOfInvocations)).disableBehaviour(eq(ContentModel.ASPECT_VERSIONABLE));
        verify(mockContentService, times(wantedNumberOfInvocations)).getReader(NODE_REF, ContentModel.PROP_CONTENT);
        verify(mockContentService, times(wantedNumberOfInvocations)).getWriter(NODE_REF, ContentModel.PROP_CONTENT, true);
        verify(mockContentWriter, times(wantedNumberOfInvocations)).putContent(mockContentReader);
        verify(mockBehaviorFilter, times(wantedNumberOfInvocations)).enableBehaviour(eq(ContentModel.ASPECT_AUDITABLE));
        verify(mockBehaviorFilter, times(wantedNumberOfInvocations)).enableBehaviour(eq(ContentModel.ASPECT_VERSIONABLE));
    }
}
