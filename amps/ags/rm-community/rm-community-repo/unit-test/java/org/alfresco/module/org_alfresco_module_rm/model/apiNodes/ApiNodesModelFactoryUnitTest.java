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
package org.alfresco.module.org_alfresco_module_rm.model.apiNodes;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.model.RetentionSchedule;
import org.alfresco.rm.rest.api.model.RetentionScheduleActionDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.mockito.InjectMocks;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Api nodes modal factory unit test
 */
public class ApiNodesModelFactoryUnitTest extends BaseUnitTest
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
        NodeRef nodeRef = mock(NodeRef.class);
        when(dispositionSchedule.getDispositionAuthority()).thenReturn(AUTHORITY);
        when(dispositionSchedule.getDispositionInstructions()).thenReturn(INSTRUCTIONS);
        when(dispositionSchedule.getNodeRef()).thenReturn(nodeRef);
        when(dispositionSchedule.isRecordLevelDisposition()).thenReturn(false);
        when(apiNodesModelFactory.mapRetentionScheduleData(dispositionSchedule)).thenReturn(mock(RetentionSchedule.class));

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
        NodeRef nodeRef = mock(NodeRef.class);
        Period mockPeriod = mock(Period.class);
        QName mockQName = mock(QName.createQName("qname"));
        when(dispositionActionDefinition.getNodeRef()).thenReturn(nodeRef);
        when(dispositionActionDefinition.getName()).thenReturn(RETAIN_STEP);
        when(dispositionActionDefinition.getDescription()).thenReturn("Description");
        when(dispositionActionDefinition.getIndex()).thenReturn(1);
        when(dispositionActionDefinition.getGhostOnDestroy()).thenReturn("ghost");
        when(dispositionActionDefinition.getPeriod()).thenReturn(mockPeriod);
        when(dispositionActionDefinition.getPeriodProperty()).thenReturn(mockQName);
        when(dispositionActionDefinition.getLocation()).thenReturn("location");
        // Call the method
        RetentionScheduleActionDefinition result = apiNodesModelFactory.mapRetentionScheduleActionDefData(dispositionActionDefinition);
        String period = result.getPeriod()+"|"+result.getPeriodAmount();
        // Assertions
        assertEquals(dispositionActionDefinition.getNodeRef().getId(),result.getId());
        assertEquals(dispositionActionDefinition.getName(),result.getName());
        assertEquals(dispositionActionDefinition.getDescription(),result.getDescription());
        assertEquals(dispositionActionDefinition.getIndex(),result.getIndex());
        assertEquals(dispositionActionDefinition.getLocation(),result.getLocation());
        assertEquals(dispositionActionDefinition.getPeriodProperty().getLocalName(),result.getPeriodProperty());
        assertEquals(dispositionActionDefinition.getPeriod(),new Period(period));
    }

    @Test
    public void mapRetentionScheduleOptionalInfoTest()
    {
        // Mock data
        RetentionSchedule retentionSchedule = new RetentionSchedule();
        DispositionSchedule schedule = mock(DispositionSchedule.class);
        List<String> includeParam = List.of("actions");
        when(schedule.getDispositionActionDefinitions()).thenReturn(List.of(mock(DispositionActionDefinition.class)));
        // Call the method
        apiNodesModelFactory.mapRetentionScheduleOptionalInfo(retentionSchedule, schedule, includeParam);
        // Assertions
        assertNotNull(retentionSchedule);
    }
}
