/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.module.org_alfresco_module_rm.disposition.property;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.impl.UnCutoffAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

/**
 * Unit test for {@link DispositionProperty}.
 */
public class DispositionPropertyUnitTest extends BaseUnitTest
{
    @InjectMocks
    private DispositionProperty dispositionProperty;

    @InjectMocks
    private UnCutoffAction unCutoffAction;

    @Before
    public void setupDispositionProperty()
    {
        dispositionProperty.setName("rma:cutOffDate");
    }

    @Test(expected = AlfrescoRuntimeException.class)
    public void cutOffDateClearedWithoutUndoCutOffThrows()
    {
        NodeRef nodeRef = generateNodeRef();

        dispositionProperty.onUpdateProperties(nodeRef, cutOffDateProperties(new Date()), cutOffDateProperties(null));
    }

    @Test
    public void cutOffDateClearedDuringUndoCutOffDoesNotThrow() throws ReflectiveOperationException
    {
        NodeRef nodeRef = markUndoCutOffInProgress();

        try
        {
            dispositionProperty.onUpdateProperties(nodeRef, cutOffDateProperties(new Date()), cutOffDateProperties(null));
        }
        catch (AlfrescoRuntimeException e)
        {
            fail("Clearing the cut off date during an undo cut off should not throw, but threw: " + e.getMessage());
        }
    }

    @Test(expected = AlfrescoRuntimeException.class)
    public void clearingADifferentPropertyStillThrowsDuringUndoCutOff() throws ReflectiveOperationException
    {
        NodeRef nodeRef = markUndoCutOffInProgress();
        dispositionProperty.setName("rma:dispositionAsOf");

        Map<QName, Serializable> before = new HashMap<>();
        before.put(PROP_DISPOSITION_AS_OF, new Date());
        Map<QName, Serializable> after = new HashMap<>();
        after.put(PROP_DISPOSITION_AS_OF, null);

        dispositionProperty.onUpdateProperties(nodeRef, before, after);
    }

    private NodeRef markUndoCutOffInProgress() throws ReflectiveOperationException
    {
        NodeRef nodeRef = generateNodeRef();
        doReturn(true).when(mockedNodeService).hasAspect(nodeRef, ASPECT_DISPOSITION_LIFECYCLE);
        doReturn(true).when(mockedNodeService).hasAspect(nodeRef, ASPECT_CUT_OFF);
        doReturn(false).when(mockedRecordFolderService).isRecordFolder(nodeRef);

        DispositionAction lastCompleted = mock(DispositionAction.class);
        doReturn("cutoff").when(lastCompleted).getName();
        doReturn(generateNodeRef()).when(lastCompleted).getNodeRef();
        doReturn(lastCompleted).when(mockedDispositionService).getLastCompletedDispostionAction(nodeRef);

        Method executeImpl = UnCutoffAction.class.getDeclaredMethod("executeImpl", Action.class, NodeRef.class);
        executeImpl.setAccessible(true);
        executeImpl.invoke(unCutoffAction, mock(Action.class), nodeRef);

        assertTrue("UnCutoffAction.executeImpl did not mark the node as undergoing an undo cut off - "
                + "the guard clause likely short-circuited (stub mismatch), not the onUpdateProperties fix itself",
                UnCutoffAction.isUndoCutOffInProgress(nodeRef));

        return nodeRef;
    }

    private Map<QName, Serializable> cutOffDateProperties(Date date)
    {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(PROP_CUT_OFF_DATE, date);
        return properties;
    }
}
