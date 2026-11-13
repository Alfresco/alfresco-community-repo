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
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.BaseActionUnitTest;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Unit test for {@link UnCutoffAction}.
 * <p>
 * Coverage for the collaboration with {@code DispositionProperty} lives in
 * {@code DispositionPropertyUnitTest}, since that is the class whose contract is under test there.
 */
public class UnCutoffActionUnitTest extends BaseActionUnitTest
{
    @InjectMocks
    private UnCutoffAction action;

    @Mock
    private DispositionAction lastCompletedDispositionAction;

    @Test
    public void undoCutOffMarksFolderAndRecordInProgress()
    {
        setupCutOff(recordFolder);
        setupLastCompletedCutOff(recordFolder);

        action.executeImpl(getMockedAction(), recordFolder);

        verify(mockedNodeService).removeAspect(recordFolder, ASPECT_CUT_OFF);
        verify(mockedNodeService).addAspect(recordFolder, ASPECT_UNCUT_OFF, null);
        verify(mockedNodeService).removeAspect(record, ASPECT_CUT_OFF);
        verify(mockedNodeService).addAspect(record, ASPECT_UNCUT_OFF, null);

        assertTrue(UnCutoffAction.isUndoCutOffInProgress(recordFolder));
        assertTrue(UnCutoffAction.isUndoCutOffInProgress(record));
    }

    @Test
    public void nodeNotTouchedIsNotInProgress()
    {
        NodeRef untouched = generateNodeRef();

        assertFalse(UnCutoffAction.isUndoCutOffInProgress(untouched));
    }

    @Test
    public void nodeNotCutOffDoesNothing()
    {
        doReturn(true).when(mockedNodeService).hasAspect(recordFolder, ASPECT_DISPOSITION_LIFECYCLE);
        doReturn(false).when(mockedNodeService).hasAspect(recordFolder, ASPECT_CUT_OFF);

        action.executeImpl(getMockedAction(), recordFolder);

        verify(mockedNodeService, never()).removeAspect(recordFolder, ASPECT_CUT_OFF);
        assertFalse(UnCutoffAction.isUndoCutOffInProgress(recordFolder));
    }

    @Test(expected = AlfrescoRuntimeException.class)
    public void lastActionNotCutOffThrows()
    {
        setupCutOff(recordFolder);
        doReturn("destroy").when(lastCompletedDispositionAction).getName();
        doReturn(lastCompletedDispositionAction).when(mockedDispositionService).getLastCompletedDispostionAction(recordFolder);

        action.executeImpl(getMockedAction(), recordFolder);
    }

    private void setupCutOff(NodeRef nodeRef)
    {
        doReturn(true).when(mockedNodeService).hasAspect(nodeRef, ASPECT_DISPOSITION_LIFECYCLE);
        doReturn(true).when(mockedNodeService).hasAspect(nodeRef, ASPECT_CUT_OFF);
    }

    private void setupLastCompletedCutOff(NodeRef nodeRef)
    {
        doReturn("cutoff").when(lastCompletedDispositionAction).getName();
        doReturn(generateNodeRef()).when(lastCompletedDispositionAction).getNodeRef();
        doReturn(lastCompletedDispositionAction).when(mockedDispositionService).getLastCompletedDispostionAction(nodeRef);
    }
}
