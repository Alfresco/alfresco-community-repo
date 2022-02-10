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

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.mockito.InjectMocks;

/**
 * Unit test for UnfiledRecordContainerType
 *
 * @author Silviu Dinuta
 * @since 2.6
 */
public class UnfiledRecordContainerTypeUnitTest extends BaseUnitTest
{
    @InjectMocks
    private UnfiledRecordContainerType unfiledRecordContainerType;

    /**
     * Given that we try to add a type that is not one of "rma:unfiledRecordFolder", "cm:content" or "rma:nonElectronicDocument" types to unfiled record container,
     * Then IntegrityException is thrown.
     */
    @Test(expected = IntegrityException.class)
    public void testAddNonAcceptedTypeToUnfiledRecordContainer()
    {
        QName type = AlfMock.generateQName();
        when(mockedNodeTypeUtility.instanceOf(type, TYPE_UNFILED_RECORD_FOLDER)).thenReturn(false);
        when(mockedNodeTypeUtility.instanceOf(type, ContentModel.TYPE_CONTENT)).thenReturn(false);
        when(mockedNodeTypeUtility.instanceOf(type, TYPE_NON_ELECTRONIC_DOCUMENT)).thenReturn(false);

        NodeRef nodeRef= AlfMock.generateNodeRef(mockedNodeService, type);

        NodeRef unfiledRecordContainer = generateNodeRef(TYPE_UNFILED_RECORD_CONTAINER, true);
        ChildAssociationRef mockedChildAssoc = mock(ChildAssociationRef.class);
        when(mockedChildAssoc.getChildRef()).thenReturn(nodeRef);
        when(mockedChildAssoc.getParentRef()).thenReturn(unfiledRecordContainer);
        unfiledRecordContainerType.onCreateChildAssociation(mockedChildAssoc, true);
    }

    /**
     * Given that we try to add "rma:unfiledRecordFolder" sub-type to unfiled record container,
     * Then the operation is successful.
     */
    @Test
    public void testAddUnfiledRecordFolderTypeToUnfiledRecordContainer()
    {
        QName type = AlfMock.generateQName();
        when(mockedNodeTypeUtility.instanceOf(type, TYPE_UNFILED_RECORD_FOLDER)).thenReturn(true);
        when(mockedNodeTypeUtility.instanceOf(type, ContentModel.TYPE_CONTENT)).thenReturn(false);
        when(mockedNodeTypeUtility.instanceOf(type, TYPE_NON_ELECTRONIC_DOCUMENT)).thenReturn(false);

        NodeRef nodeRef= AlfMock.generateNodeRef(mockedNodeService, type);

        NodeRef unfiledRecordContainer = generateNodeRef(TYPE_UNFILED_RECORD_CONTAINER, true);
        ChildAssociationRef mockedChildAssoc = mock(ChildAssociationRef.class);
        when(mockedChildAssoc.getChildRef()).thenReturn(nodeRef);
        when(mockedChildAssoc.getParentRef()).thenReturn(unfiledRecordContainer);
        unfiledRecordContainerType.onCreateChildAssociation(mockedChildAssoc, true);
    }

    /**
     * Given that we try to add "cm:content" sub-type to unfiled record container,
     * Then the operation is successful.
     */
    @Test
    public void testAddContentTypeToUnfiledRecordContainer()
    {
        QName type = AlfMock.generateQName();
        when(mockedNodeTypeUtility.instanceOf(type, TYPE_UNFILED_RECORD_FOLDER)).thenReturn(false);
        when(mockedNodeTypeUtility.instanceOf(type, ContentModel.TYPE_CONTENT)).thenReturn(true);
        when(mockedNodeTypeUtility.instanceOf(type, TYPE_NON_ELECTRONIC_DOCUMENT)).thenReturn(false);

        NodeRef nodeRef= AlfMock.generateNodeRef(mockedNodeService, type);

        NodeRef unfiledRecordContainer = generateNodeRef(TYPE_UNFILED_RECORD_CONTAINER, true);
        ChildAssociationRef mockedChildAssoc = mock(ChildAssociationRef.class);
        when(mockedChildAssoc.getChildRef()).thenReturn(nodeRef);
        when(mockedChildAssoc.getParentRef()).thenReturn(unfiledRecordContainer);
        unfiledRecordContainerType.onCreateChildAssociation(mockedChildAssoc, true);
    }

    /**
     * Given that we try to add "rma:nonElectronicDocument" sub-type to unfiled record container,
     * Then the operation is successful.
     */
    @Test
    public void testNonElectronicDocumentTypeToUnfiledRecordContainer()
    {
        QName type = AlfMock.generateQName();
        when(mockedNodeTypeUtility.instanceOf(type, TYPE_UNFILED_RECORD_FOLDER)).thenReturn(false);
        when(mockedNodeTypeUtility.instanceOf(type, ContentModel.TYPE_CONTENT)).thenReturn(false);
        when(mockedNodeTypeUtility.instanceOf(type, TYPE_NON_ELECTRONIC_DOCUMENT)).thenReturn(true);

        NodeRef nodeRef= AlfMock.generateNodeRef(mockedNodeService, type);

        NodeRef unfiledRecordContainer = generateNodeRef(TYPE_UNFILED_RECORD_CONTAINER, true);
        ChildAssociationRef mockedChildAssoc = mock(ChildAssociationRef.class);
        when(mockedChildAssoc.getChildRef()).thenReturn(nodeRef);
        when(mockedChildAssoc.getParentRef()).thenReturn(unfiledRecordContainer);
        unfiledRecordContainerType.onCreateChildAssociation(mockedChildAssoc, true);
    }
}
