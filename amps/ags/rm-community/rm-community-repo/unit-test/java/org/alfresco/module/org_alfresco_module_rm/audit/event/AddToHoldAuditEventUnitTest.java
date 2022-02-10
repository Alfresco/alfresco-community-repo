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

package org.alfresco.module.org_alfresco_module_rm.audit.event;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.GUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AddToHoldAuditEvent}.
 *
 * @author Sara Aspery
 * @since 3.3
 */
public class AddToHoldAuditEventUnitTest extends BaseUnitTest
{
    @InjectMocks
    private AddToHoldAuditEvent addToHoldAuditEvent;

    @Mock
    private NodeService mockedNodeService;

    private NodeRef holdNodeRef;
    private NodeRef contentNodeRef;

    /** Set up the mocks. */
    @Before
    public void setUp()
    {
        holdNodeRef = generateNodeRef();
        String holdName = "Hold " + GUID.generate();

        contentNodeRef = generateNodeRef();
        String contentName = "Content " + GUID.generate();

        when(mockedNodeService.getProperty(holdNodeRef, PROP_NAME)).thenReturn(holdName);
        when(mockedNodeService.getProperty(contentNodeRef, PROP_NAME)).thenReturn(contentName);
    }

    /**
     * Check that the add to hold event calls an audit event.
     *
     */
    @Test
    public void testAddToHoldCausesAuditEvent()
    {
        addToHoldAuditEvent.onAddToHold(holdNodeRef, contentNodeRef);
        verify(mockedRecordsManagementAuditService, times(1)).auditEvent(eq(contentNodeRef), nullable(String.class), isNull(), anyMap(), eq(true), eq(false));
    }
}
