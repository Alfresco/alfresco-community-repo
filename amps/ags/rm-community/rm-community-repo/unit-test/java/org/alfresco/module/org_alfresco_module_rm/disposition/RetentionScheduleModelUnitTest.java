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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Period;
import org.junit.Test;
import org.mockito.InjectMocks;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Api nodes modal factory unit test
 */
public class RetentionScheduleModelUnitTest extends BaseUnitTest
{
    private static final String AUTHORITY = "authority";
    private static final String INSTRUCTIONS = "instructions";
    private static final String RETAIN_STEP = "retain";

    @InjectMocks
    private ApiNodesModelFactory apiNodesModelFactory;

    @Test
    public void mapRetentionScheduleDataTest()
    {
        // Mock data
        DispositionSchedule dispositionSchedule = mock(DispositionSchedule.class);
        RetentionSchedule retentionSchedule = mock(RetentionSchedule.class);
        NodeRef nodeRef = generateNodeRef(RecordsManagementModel.TYPE_DISPOSITION_SCHEDULE, true);
        when(dispositionSchedule.getDispositionAuthority()).thenReturn(AUTHORITY);
        when(dispositionSchedule.getDispositionInstructions()).thenReturn(INSTRUCTIONS);
        when(dispositionSchedule.getNodeRef()).thenReturn(nodeRef);
        when(dispositionSchedule.isRecordLevelDisposition()).thenReturn(false);
        when(apiNodesModelFactory.mapRetentionScheduleData(dispositionSchedule)).thenReturn(retentionSchedule);
        // Call the method
        RetentionSchedule result = apiNodesModelFactory.mapRetentionScheduleData(dispositionSchedule);

        assertEquals(dispositionSchedule.getNodeRef().getId(),result.getId());
        assertEquals(dispositionSchedule.getDispositionAuthority(),result.getAuthority());
        assertEquals(dispositionSchedule.getDispositionInstructions(),result.getInstructions());
        assertEquals(dispositionSchedule.isRecordLevelDisposition(),result.isRecordLevel());
    }

    @Test
    public void mapRetentionScheduleActionDefDataTest()
    {
        // Mock data
        DispositionActionDefinition dispositionActionDefinition = mock(DispositionActionDefinition.class);
        NodeRef nodeRef = generateNodeRef(RecordsManagementModel.TYPE_DISPOSITION_SCHEDULE, true);
        String period = "month|10";
        when(dispositionActionDefinition.getNodeRef()).thenReturn(nodeRef);
        when(dispositionActionDefinition.getName()).thenReturn(RETAIN_STEP);
        when(dispositionActionDefinition.getDescription()).thenReturn("Description");
        when(dispositionActionDefinition.getIndex()).thenReturn(1);
        when(dispositionActionDefinition.getGhostOnDestroy()).thenReturn("ghost");
        when(dispositionActionDefinition.getPeriod()).thenReturn(new Period(period));
        when(dispositionActionDefinition.getLocation()).thenReturn("location");
        // Call the method
        RetentionScheduleActionDefinition result = apiNodesModelFactory.mapRetentionScheduleActionDefData(dispositionActionDefinition);
        String resultPeriod = result.getPeriod()+"|"+result.getPeriodAmount();
        // Assertions
        assertEquals(dispositionActionDefinition.getNodeRef().getId(),result.getId());
        assertEquals(dispositionActionDefinition.getName(),result.getName());
        assertEquals(dispositionActionDefinition.getDescription(),result.getDescription());
        assertEquals(dispositionActionDefinition.getIndex(),result.getIndex());
        assertEquals(dispositionActionDefinition.getLocation(),result.getLocation());
        assertEquals(dispositionActionDefinition.getPeriodProperty().getLocalName(),result.getPeriodProperty());
        assertEquals(dispositionActionDefinition.getPeriod(),new Period(resultPeriod));
    }
}