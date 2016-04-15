/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.model.rma.type;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.test.util.MockAuthenticationUtilHelper;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.GUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author silviudinuta
 */
public class NonElectronicRecordTypeUnitTest implements RecordsManagementModel, ContentModel
{

    @InjectMocks
    NonElectronicRecordType nonElectronicRecordType;

    @Mock
    AuthenticationUtil mockAuthenticationUtil;

    @Mock
    RecordService mockedRecordService;

    @Mock
    NodeService mockedNodeService;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        MockAuthenticationUtilHelper.setup(mockAuthenticationUtil);
    }

    @Test
    public void testOnUpdateWithAspectsAlreadyPresent()
    {
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
        when(mockedNodeService.exists(eq(nodeRef))).thenReturn(true);

        NodeRef parentNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
        when(mockedNodeService.exists(eq(parentNodeRef))).thenReturn(true);

        ChildAssociationRef generateChildAssociationRef = mock(ChildAssociationRef.class);
        doReturn(parentNodeRef).when(generateChildAssociationRef).getParentRef();
        doReturn(nodeRef).when(generateChildAssociationRef).getChildRef();

        when(mockedNodeService.getPrimaryParent(nodeRef)).thenReturn(generateChildAssociationRef);
        when(mockedNodeService.getType(parentNodeRef)).thenReturn(TYPE_UNFILED_RECORD_FOLDER);
        when(mockedNodeService.hasAspect(nodeRef, ASPECT_FILE_PLAN_COMPONENT)).thenReturn(true);
        when(mockedNodeService.hasAspect(nodeRef, ASPECT_RECORD)).thenReturn(true);

        nonElectronicRecordType.onUpdateNode(nodeRef);

        verify(mockedNodeService, never()).addAspect(nodeRef, ASPECT_FILE_PLAN_COMPONENT, null);
        verify(mockedRecordService, never()).makeRecord(nodeRef);
    }

    @Test
    public void testOnUpdateWithoutTheAspects()
    {
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
        when(mockedNodeService.exists(eq(nodeRef))).thenReturn(true);

        NodeRef parentNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
        when(mockedNodeService.exists(eq(parentNodeRef))).thenReturn(true);

        ChildAssociationRef generateChildAssociationRef = mock(ChildAssociationRef.class);
        doReturn(parentNodeRef).when(generateChildAssociationRef).getParentRef();
        doReturn(nodeRef).when(generateChildAssociationRef).getChildRef();

        when(mockedNodeService.getPrimaryParent(nodeRef)).thenReturn(generateChildAssociationRef);
        when(mockedNodeService.getType(parentNodeRef)).thenReturn(TYPE_UNFILED_RECORD_FOLDER);
        when(mockedNodeService.hasAspect(nodeRef, ASPECT_FILE_PLAN_COMPONENT)).thenReturn(false);
        when(mockedNodeService.hasAspect(nodeRef, ASPECT_RECORD)).thenReturn(false);

        nonElectronicRecordType.onUpdateNode(nodeRef);

        verify(mockedNodeService, times(1)).addAspect(nodeRef, ASPECT_FILE_PLAN_COMPONENT, null);
        verify(mockedRecordService, times(1)).makeRecord(nodeRef);
    }
}
