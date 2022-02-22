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

package org.alfresco.module.org_alfresco_module_rm.patch.v33;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.alfresco.module.org_alfresco_module_rm.query.RecordsManagementQueryDAO;
import org.alfresco.repo.domain.propval.PropertyStringValueEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * RM V3.3 Hold audit entries values patch unit test
 *
 * @author Ramona Popa
 * @since 3.3
 */
public class RMv33HoldAuditEntryValuesPatchUnitTest
{
    @Mock
    private RecordsManagementQueryDAO mockedRecordsManagementQueryDAO;

    @InjectMocks
    private RMv33HoldAuditEntryValuesPatch patch;


    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * addtohold, removeFromHold and deleteHold audit entries values are updated after the patch is executed
     */
    @Test
    public void holdAuditEntriesAreUpdatedAfterUpgrade()
    {
        PropertyStringValueEntity addToHoldPropertyStringValueEntity = new PropertyStringValueEntity();
        addToHoldPropertyStringValueEntity.setValue("addToHold");
        when(mockedRecordsManagementQueryDAO.getPropertyStringValueEntity("addToHold")).thenReturn(addToHoldPropertyStringValueEntity);
        when(mockedRecordsManagementQueryDAO.updatePropertyStringValueEntity(addToHoldPropertyStringValueEntity)).thenReturn(1);

        PropertyStringValueEntity removeFromHoldPropertyStringValueEntity = new PropertyStringValueEntity();
        removeFromHoldPropertyStringValueEntity.setValue("removeFromHold");
        when(mockedRecordsManagementQueryDAO.getPropertyStringValueEntity("removeFromHold")).thenReturn(removeFromHoldPropertyStringValueEntity);
        when(mockedRecordsManagementQueryDAO.updatePropertyStringValueEntity(removeFromHoldPropertyStringValueEntity)).thenReturn(1);

        PropertyStringValueEntity deleteHoldPropertyStringValueEntity = new PropertyStringValueEntity();
        deleteHoldPropertyStringValueEntity.setValue("deleteHold");
        when(mockedRecordsManagementQueryDAO.getPropertyStringValueEntity("deleteHold")).thenReturn(deleteHoldPropertyStringValueEntity);
        when(mockedRecordsManagementQueryDAO.updatePropertyStringValueEntity(deleteHoldPropertyStringValueEntity)).thenReturn(1);

        patch.applyInternal();

        verify(mockedRecordsManagementQueryDAO, times(1)).getPropertyStringValueEntity("addToHold");
        verify(mockedRecordsManagementQueryDAO, times(1)).updatePropertyStringValueEntity(addToHoldPropertyStringValueEntity);
        verify(mockedRecordsManagementQueryDAO, times(1)).getPropertyStringValueEntity("removeFromHold");
        verify(mockedRecordsManagementQueryDAO, times(1)).updatePropertyStringValueEntity(removeFromHoldPropertyStringValueEntity);
        verify(mockedRecordsManagementQueryDAO, times(1)).getPropertyStringValueEntity("deleteHold");
        verify(mockedRecordsManagementQueryDAO, times(1)).updatePropertyStringValueEntity(deleteHoldPropertyStringValueEntity);

        assertEquals("Add To Hold", addToHoldPropertyStringValueEntity.getStringValue());
        assertEquals("add to hold", addToHoldPropertyStringValueEntity.getStringEndLower());
        assertEquals(Long.valueOf(770_786_109L), addToHoldPropertyStringValueEntity.getStringCrc());

        assertEquals("Remove From Hold", removeFromHoldPropertyStringValueEntity.getStringValue());
        assertEquals("remove from hold", removeFromHoldPropertyStringValueEntity.getStringEndLower());
        assertEquals(Long.valueOf(2_967_613_012L), removeFromHoldPropertyStringValueEntity.getStringCrc());

        assertEquals("Delete Hold", deleteHoldPropertyStringValueEntity.getStringValue());
        assertEquals("delete hold", deleteHoldPropertyStringValueEntity.getStringEndLower());
        assertEquals(Long.valueOf(132_640_810L), deleteHoldPropertyStringValueEntity.getStringCrc());
    }

    /**
     * if there are no hold audit entries, the patch is executed with success; no entries are updated
     */
    @Test
    public void patchRunWithSuccessWhenNoHoldEntries()
    {
        when(mockedRecordsManagementQueryDAO.getPropertyStringValueEntity("addToHold")).thenReturn(null);
        when(mockedRecordsManagementQueryDAO.getPropertyStringValueEntity("removeFromHold")).thenReturn(null);
        when(mockedRecordsManagementQueryDAO.getPropertyStringValueEntity("deleteHold")).thenReturn(null);

        patch.applyInternal();

        verify(mockedRecordsManagementQueryDAO, times(1)).getPropertyStringValueEntity("addToHold");
        verify(mockedRecordsManagementQueryDAO, times(1)).getPropertyStringValueEntity("removeFromHold");
        verify(mockedRecordsManagementQueryDAO, times(1)).getPropertyStringValueEntity("deleteHold");
        verify(mockedRecordsManagementQueryDAO, times(0)).updatePropertyStringValueEntity(any());
    }
}


