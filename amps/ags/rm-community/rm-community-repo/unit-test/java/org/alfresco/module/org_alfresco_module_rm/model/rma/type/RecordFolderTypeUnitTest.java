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

package org.alfresco.module.org_alfresco_module_rm.model.rma.type;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.module.org_alfresco_module_rm.test.util.MockAuthenticationUtilHelper;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordService;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit test class for RecordFolderType
 *
 * @author Silviu Dinuta
 * @since 2.6
 *
 */
public class RecordFolderTypeUnitTest extends BaseUnitTest
{
    @Mock
    private AuthenticationUtil mockAuthenticationUtil;

    @Mock
    private VitalRecordService mockedVitalRecordService;

    private @InjectMocks RecordFolderType recordFolderType;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
        MockAuthenticationUtilHelper.setup(mockAuthenticationUtil);
        when(mockedApplicationContext.getBean("dbNodeService")).thenReturn(mockedNodeService);
    }

    /**
     * Given that we try to add one rma:transfer to a record folder,
     * Then IntegrityException is thrown.
     */
    @Test(expected = IntegrityException.class)
    public void testCreateTransferFolder() throws Exception
    {
        NodeRef recordFolderNodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_RECORD_FOLDER);
        QName type = AlfMock.generateQName();
        when(mockedNodeTypeUtility.instanceOf(type, TYPE_TRANSFER)).thenReturn(true);
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, type, true);
        ChildAssociationRef childAssocRef = generateChildAssociationRef(recordFolderNodeRef, nodeRef);
        recordFolderType.onCreateChildAssociation(childAssocRef, true);
    }

    /**
     * Given that we try to add one record folder to a record folder,
     * Then IntegrityException is thrown.
     */
    @Test(expected = IntegrityException.class)
    public void testCreateRecordFolder() throws Exception
    {
        NodeRef recordFolderNodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_RECORD_FOLDER);
        QName type = AlfMock.generateQName();
        when(mockedNodeTypeUtility.instanceOf(type, TYPE_RECORD_FOLDER)).thenReturn(true);
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, type, true);
        ChildAssociationRef childAssocRef = generateChildAssociationRef(recordFolderNodeRef, nodeRef);
        recordFolderType.onCreateChildAssociation(childAssocRef, true);
    }

    /**
     * Given that we try to add sub-type of rma:recordsManagementContainer to a record folder,
     * Then IntegrityException is thrown.
     */
    @Test(expected = IntegrityException.class)
    public void testCreateSubTypesOfRecordManagementContainer() throws Exception
    {
        NodeRef recordFolderNodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_RECORD_FOLDER);
        QName type = AlfMock.generateQName();
        when(mockedNodeTypeUtility.instanceOf(type, TYPE_RECORDS_MANAGEMENT_CONTAINER)).thenReturn(true);
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, type, true);
        ChildAssociationRef childAssocRef = generateChildAssociationRef(recordFolderNodeRef, nodeRef);
        recordFolderType.onCreateChildAssociation(childAssocRef, true);
    }

    /**
     * Given that we try to add cm:folder sub-type to a record folder,
     * Then the operation is successful.
     */
    @Test
    public void testCreateFolderSubType() throws Exception
    {
        NodeRef recordFolderNodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_RECORD_FOLDER);
        QName type = AlfMock.generateQName();
        when(mockedNodeTypeUtility.instanceOf(type, TYPE_FOLDER)).thenReturn(true);
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, type, true);
        ChildAssociationRef childAssocRef = generateChildAssociationRef(recordFolderNodeRef, nodeRef);
        recordFolderType.onCreateChildAssociation(childAssocRef, true);
    }

    /**
     * Given that we try to add non cm:folder sub-type to a record folder,
     * Then IntegrityException is thrown.
     */
    @Test(expected = IntegrityException.class)
    public void testCreateNonFolderSubType() throws Exception
    {
        NodeRef recordFolderNodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_RECORD_FOLDER);
        QName type = AlfMock.generateQName();
        when(mockedNodeTypeUtility.instanceOf(type, TYPE_FOLDER)).thenReturn(false);
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, type, true);
        ChildAssociationRef childAssocRef = generateChildAssociationRef(recordFolderNodeRef, nodeRef);
        recordFolderType.onCreateChildAssociation(childAssocRef, true);
    }

    /**
     * Given that we try to add cm:content sub-type to a record folder,
     * Then the operation is successful.
     */
    public void testCreateContent() throws Exception
    {
        NodeRef recordFolderNodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_RECORD_FOLDER);
        QName type = AlfMock.generateQName();
        when(mockedNodeTypeUtility.instanceOf(type, TYPE_CONTENT)).thenReturn(true);
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, type, true);
        ChildAssociationRef childAssocRef = generateChildAssociationRef(recordFolderNodeRef, nodeRef);
        recordFolderType.onCreateChildAssociation(childAssocRef, true);
    }

    /**
     * Given that we try to add non cm:content or non cm:folder sub-type to a record folder,
     * Then IntegrityException is thrown.
     */
    @Test(expected = IntegrityException.class)
    public void testCreateNonContent() throws Exception
    {
        NodeRef recordFolderNodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_RECORD_FOLDER);
        QName type = AlfMock.generateQName();
        when(mockedNodeTypeUtility.instanceOf(type, TYPE_CONTENT)).thenReturn(false);
        when(mockedNodeTypeUtility.instanceOf(type, TYPE_FOLDER)).thenReturn(false);
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, type, true);
        ChildAssociationRef childAssocRef = generateChildAssociationRef(recordFolderNodeRef, nodeRef);
        recordFolderType.onCreateChildAssociation(childAssocRef, true);
    }

    /**
     * Given that we try to add not hidden cm:folder sub-type to a record folder,
     * Then IntegrityException is thrown on commit.
     */
    @Test(expected = IntegrityException.class)
    public void testCreateNotHiddenFolderSubTypeOnCommit() throws Exception
    {
        NodeRef recordFolderNodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_RECORD_FOLDER);
        QName type = AlfMock.generateQName();
        when(mockedNodeTypeUtility.instanceOf(type, TYPE_FOLDER)).thenReturn(true);
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, type, true);
        ChildAssociationRef childAssocRef = generateChildAssociationRef(recordFolderNodeRef, nodeRef);
        recordFolderType.onCreateChildAssociationOnCommit(childAssocRef, true);
    }

    /**
     * Given that we try to add hidden cm:folder sub-type to a record folder,
     * Then the operation is successful.
     */
    @Test
    public void testCreateHiddenFolderSubTypeOnCommit() throws Exception
    {
        NodeRef recordFolderNodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_RECORD_FOLDER);
        QName type = AlfMock.generateQName();
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, type, true);

        when(mockedNodeTypeUtility.instanceOf(type, TYPE_FOLDER)).thenReturn(true);
        when(mockedNodeService.hasAspect(nodeRef, ASPECT_HIDDEN)).thenReturn(true);

        ChildAssociationRef mockedPrimaryParentAssoc = mock(ChildAssociationRef.class);
        when(mockedNodeService.getPrimaryParent(nodeRef)).thenReturn(mockedPrimaryParentAssoc);

        ChildAssociationRef childAssocRef = generateChildAssociationRef(recordFolderNodeRef, nodeRef);
        recordFolderType.onCreateChildAssociationOnCommit(childAssocRef, true);
    }

    /**
     * Given that we try to add non cm:folder sub-type to a record folder,
     * Then the operation is successful.
     */
    @Test
    public void testCreateNonFolderSubTypeOnCommit() throws Exception
    {
        NodeRef recordFolderNodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_RECORD_FOLDER);
        QName type = AlfMock.generateQName();
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, type, true);

        when(mockedNodeTypeUtility.instanceOf(type, TYPE_FOLDER)).thenReturn(false);

        ChildAssociationRef mockedPrimaryParentAssoc = mock(ChildAssociationRef.class);
        when(mockedNodeService.getPrimaryParent(nodeRef)).thenReturn(mockedPrimaryParentAssoc);

        ChildAssociationRef childAssocRef = generateChildAssociationRef(recordFolderNodeRef, nodeRef);
        recordFolderType.onCreateChildAssociationOnCommit(childAssocRef, true);
    }
}
