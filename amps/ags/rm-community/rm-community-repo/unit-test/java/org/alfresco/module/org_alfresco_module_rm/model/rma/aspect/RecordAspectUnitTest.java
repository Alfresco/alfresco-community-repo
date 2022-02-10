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
package org.alfresco.module.org_alfresco_module_rm.model.rma.aspect;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASPECT_RECORD;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService;
import org.alfresco.module.org_alfresco_module_rm.util.ContentBinDuplicationUtility;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for the {@link RecordAspect}.
 *
 * @author Claudia Agache
 */
@RunWith(MockitoJUnitRunner.class)
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
    private ExtendedSecurityService mockExtendedSecurityService;
    @Mock
    private ContentBinDuplicationUtility mockContentBinDuplicationUtility;

    @Before
    public void setUp()
    {
        recordAspect.setNodeService(mockNodeService);
    }

    /** Check that the bin is duplicated before adding the aspect if the file has a copy. */
    @Test
    public void testDuplicateBinBeforeAddingAspectForFileWithCopy()
    {
        when(mockNodeService.getSourceAssocs(NODE_REF, ContentModel.ASSOC_ORIGINAL)).thenReturn(asList(SOURCE_ASSOC_REF));

        recordAspect.beforeAddAspect(NODE_REF, ASPECT_RECORD);

        verify(mockContentBinDuplicationUtility, times(1)).duplicate(NODE_REF);
    }

    /** Check that the bin is duplicated before adding the aspect if the file is a copy. */
    @Test
    public void testDuplicateBinBeforeAddingAspectForCopy()
    {
        when(mockNodeService.getTargetAssocs(NODE_REF, ContentModel.ASSOC_ORIGINAL)).thenReturn(asList(TARGET_ASSOC_REF));

        recordAspect.beforeAddAspect(NODE_REF, ASPECT_RECORD);

        verify(mockContentBinDuplicationUtility, times(1)).duplicate(NODE_REF);
    }

    /** Check that the bin is not duplicated before adding the aspect if the node has no copies. */
    @Test
    public void testNotDuplicateBinForFileWithNoCopies()
    {
        when(mockNodeService.getSourceAssocs(NODE_REF, ContentModel.ASSOC_ORIGINAL)).thenReturn(emptyList());
        when(mockNodeService.getTargetAssocs(NODE_REF, ContentModel.ASSOC_ORIGINAL)).thenReturn(emptyList());

        recordAspect.beforeAddAspect(NODE_REF, ASPECT_RECORD);

        verify(mockContentBinDuplicationUtility, times(0)).duplicate(NODE_REF);
    }

    /** Check that the bin is duplicated when copying a record. */
    @Test
    public void testDuplicateBinWhenCopyingRecord()
    {
        when(mockNodeService.exists(COPY_REF)).thenReturn(true);
        when(mockNodeService.hasAspect(COPY_REF, ASPECT_RECORD)).thenReturn(true);

        recordAspect.onCopyComplete(null, NODE_REF, COPY_REF, true, null);

        verify(mockExtendedSecurityService, times(1)).remove(COPY_REF);
        verify(mockContentBinDuplicationUtility, times(1)).duplicate(COPY_REF);
    }
}
