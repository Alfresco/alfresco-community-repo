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

package org.alfresco.module.org_alfresco_module_rm.patch.v32;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.VersionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * RM V3.2 Hold report update patch unit test
 * 
 * @author Ramona Popa
 * @since 3.2
 */
public class RMv32HoldReportUpdatePatchUnitTest
{
    @Mock
    private NodeService mockedNodeService;

    @Mock
    private ContentService mockedContentService;

    @Mock
    private VersionService mockedVersionService;

    @Mock
    private ContentWriter mockedContentWriter;

    @InjectMocks
    private RMv32HoldReportUpdatePatch patch;

    private NodeRef hold_report;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        hold_report = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "rmr_holdReport");
    }

    /**
     * Test report content is updated after the patch is executed
     */
    @Test
    public void testReportContentIsUpdatedAfterUpgrade()
    {
        when(mockedNodeService.exists(hold_report)).thenReturn(true);
        when(mockedNodeService.hasAspect(hold_report, ContentModel.ASPECT_VERSIONABLE)).thenReturn(false);
        when(mockedContentService.getWriter(hold_report, ContentModel.PROP_CONTENT, true)).thenReturn(mockedContentWriter);

        patch.applyInternal();
        verify(mockedNodeService, times(1)).addAspect(hold_report, ContentModel.ASPECT_VERSIONABLE, null);
        verify(mockedVersionService, times(1)).createVersion((NodeRef) anyObject(), anyMap());
        verify(mockedContentWriter, times(1)).putContent((InputStream) anyObject());
    }
}


