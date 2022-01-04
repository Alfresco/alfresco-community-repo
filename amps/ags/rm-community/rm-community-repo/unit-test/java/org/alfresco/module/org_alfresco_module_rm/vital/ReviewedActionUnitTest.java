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
package org.alfresco.module.org_alfresco_module_rm.vital;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.dictionary.types.period.Days;
import org.alfresco.repo.dictionary.types.period.Immediately;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import static org.mockito.Mockito.verify;

import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.extensions.webscripts.GUID;

/**
 * Unit test for {@link ReviewedAction} class
 * 
 * @author Ana Bozianu
 * @sincev 2.6
 */
public class ReviewedActionUnitTest implements RecordsManagementModel
{
    private @Mock VitalRecordService mockedVitalRecordService;
    private @Mock RecordService mockedRecordService;
    private @Mock NodeService mockedNodeService;

    private @InjectMocks ReviewedAction reviewedAction;

    @Before
    public void testSetup()
    {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Given a record having the vital record definition of immediately 
     * When I mark the record as reviewed 
     * Then review as of date is removed from the record
     */
    @Test
    public void testReviewRecordWithAdHocReviewPeriod()
    {
        /*
         * Given
         */
        NodeRef mockedRecord = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
        when(mockedRecordService.isRecord(mockedRecord)).thenReturn(true);

        VitalRecordDefinition mockedVRDef = mock(VitalRecordDefinition.class);
        when(mockedVRDef.isEnabled()).thenReturn(true);
        when(mockedVitalRecordService.getVitalRecordDefinition(mockedRecord)).thenReturn(mockedVRDef);

        Period mockedReviewPeriod = mock(Period.class);
        when(mockedReviewPeriod.getPeriodType()).thenReturn(Immediately.PERIOD_TYPE);
        when(mockedVRDef.getReviewPeriod()).thenReturn(mockedReviewPeriod);

        /*
         * When
         */
        reviewedAction.executeImpl(null, mockedRecord);

        /*
         * Then
         */
        verify(mockedNodeService).removeProperty(mockedRecord, PROP_REVIEW_AS_OF);
    }

    /**
     * Given a record having a recurent vital record definition 
     * When I mark the record as reviewed 
     * Then the review as of date is updated according to the next review period computed by the vital record definition
     */
    @Test
    public void testReviewRecordWithRecurentReviewPeriod()
    {
        /*
         * Given
         */
        NodeRef mockedRecord = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
        when(mockedRecordService.isRecord(mockedRecord)).thenReturn(true);

        VitalRecordDefinition mockedVRDef = mock(VitalRecordDefinition.class);
        when(mockedVRDef.isEnabled()).thenReturn(true);
        when(mockedVitalRecordService.getVitalRecordDefinition(mockedRecord)).thenReturn(mockedVRDef);

        Date mockedNextReviewDate = mock(Date.class);
        when(mockedVRDef.getNextReviewDate()).thenReturn(mockedNextReviewDate);

        Period mockedReviewPeriod = mock(Period.class);
        when(mockedReviewPeriod.getPeriodType()).thenReturn(Days.PERIOD_TYPE);
        when(mockedVRDef.getReviewPeriod()).thenReturn(mockedReviewPeriod);

        /*
         * When
         */
        reviewedAction.executeImpl(null, mockedRecord);

        /*
         * Then
         */
        verify(mockedNodeService).setProperty(mockedRecord, PROP_REVIEW_AS_OF, mockedNextReviewDate);
    }
}
