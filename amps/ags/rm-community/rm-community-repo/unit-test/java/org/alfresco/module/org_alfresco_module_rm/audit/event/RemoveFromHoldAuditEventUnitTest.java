/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.audit.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.GUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for {@link RemoveFromHoldAuditEvent}.
 *
 * @author Chris Shields
 * @since 3.3
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoveFromHoldAuditEventUnitTest extends BaseUnitTest
{
    @InjectMocks
    private RemoveFromHoldAuditEvent removeFromHoldAuditEvent;

    @Mock
    private NodeService mockedNodeService;

    private NodeRef holdNodeRef;
    private NodeRef contentNodeRef;

    /**
     * Set up the mocks.
     */
    @Before
    public void setUp()
    {
        holdNodeRef = generateNodeRef();
        String holdName = "Hold " + GUID.generate();

        contentNodeRef = generateNodeRef();
        String contentName = "Content " + GUID.generate();

        lenient().when(mockedNodeService.getProperty(holdNodeRef, PROP_NAME)).thenReturn(holdName);
        lenient().when(mockedNodeService.getProperty(contentNodeRef, PROP_NAME)).thenReturn(contentName);
    }

    /**
     * Check that the remove from hold event calls an audit event.
     */
    @Test
    public void testRemoveFromHoldCausesAuditEvent()
    {
        removeFromHoldAuditEvent.onRemoveFromHold(holdNodeRef, contentNodeRef);
        verify(mockedRecordsManagementAuditService, times(1)).auditEvent(eq(contentNodeRef), eq(null), any(Map.class), eq(null), eq(true));
    }

}
