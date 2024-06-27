/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.module.org_alfresco_module_rm.disposition;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.model.RetentionSchedule;
import org.alfresco.rm.rest.api.model.RetentionScheduleActionDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Period;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Retention schedule model unit test
 */
public class RetentionScheduleModelUnitTest extends BaseUnitTest
{
    private static final String AUTHORITY = "authority";
    private static final String INSTRUCTIONS = "instructions";
    private static final String RETAIN_STEP = "retain";

    @InjectMocks
    private ApiNodesModelFactory apiNodesModelFactory;

    @Mock
    DispositionSchedule dispositionSchedule;

    @Mock
    DispositionActionDefinition dispositionActionDefinition;

    @Test
    public void mapRetentionScheduleDataTest()
    {
        // Mock data
        NodeRef nodeRef = generateNodeRef(RecordsManagementModel.TYPE_DISPOSITION_SCHEDULE, true);
        ChildAssociationRef childAssociationRef = generateChildAssociationRef(filePlan, record);
        when(dispositionSchedule.getDispositionAuthority()).thenReturn(AUTHORITY);
        when(dispositionSchedule.getDispositionInstructions()).thenReturn(INSTRUCTIONS);
        when(dispositionSchedule.getNodeRef()).thenReturn(nodeRef);
        when(dispositionSchedule.isRecordLevelDisposition()).thenReturn(false);
        when(mockedNodeService.getPrimaryParent(nodeRef)).thenReturn(childAssociationRef);
        // Call the method
        RetentionSchedule expectedResult = apiNodesModelFactory.mapRetentionScheduleData(dispositionSchedule);
        assertEquals(expectedResult.getId(), dispositionSchedule.getNodeRef().getId());
        assertEquals(expectedResult.getAuthority(), dispositionSchedule.getDispositionAuthority());
        assertEquals(expectedResult.getInstructions(), dispositionSchedule.getDispositionInstructions());
        assertEquals(expectedResult.getIsRecordLevel(), dispositionSchedule.isRecordLevelDisposition());
    }

    @Test
    public void mapRetentionScheduleActionDefDataTest()
    {
        // Mock data
        NodeRef nodeRef = generateNodeRef(RecordsManagementModel.TYPE_DISPOSITION_SCHEDULE, true);
        String period = "month|10";
        ChildAssociationRef childAssociationRef = generateChildAssociationRef(filePlan, record);
        when(dispositionActionDefinition.getNodeRef()).thenReturn(nodeRef);
        when(dispositionActionDefinition.getName()).thenReturn(RETAIN_STEP);
        when(dispositionActionDefinition.getDescription()).thenReturn("Description");
        when(dispositionActionDefinition.getIndex()).thenReturn(1);
        when(dispositionActionDefinition.getGhostOnDestroy()).thenReturn("ghost");
        when(dispositionActionDefinition.getPeriod()).thenReturn(new Period(period));
        when(dispositionActionDefinition.getLocation()).thenReturn("location");
        when(dispositionActionDefinition.getId()).thenReturn(nodeRef.getId());
        when(mockedNodeService.getPrimaryParent(nodeRef)).thenReturn(childAssociationRef);
        // Call the method
        RetentionScheduleActionDefinition expectedResult = apiNodesModelFactory.mapRetentionScheduleActionDefData(dispositionActionDefinition);
        String resultPeriod = expectedResult.getPeriod() + "|" + expectedResult.getPeriodAmount();
        // Assertions
        assertEquals(expectedResult.getId(), dispositionActionDefinition.getId());
        assertEquals(expectedResult.getName(), dispositionActionDefinition.getName());
        assertEquals(expectedResult.getDescription(), dispositionActionDefinition.getDescription());
        assertEquals(expectedResult.getIndex(), dispositionActionDefinition.getIndex());
        assertEquals(expectedResult.getLocation(), dispositionActionDefinition.getLocation());
        assertEquals(new Period(resultPeriod), dispositionActionDefinition.getPeriod());
        assertTrue(expectedResult.isRetainRecordMetadataAfterDestruction());
    }
}