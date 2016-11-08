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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.InvalidParameterException;

import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.mockito.InjectMocks;

/**
 * Unit test for TransferContainerType
 *
 * @author Mihai Cozma
 * @since 2.4
 */
public class TransferContainerTypeUnitTest extends BaseUnitTest
{
    /** test object */
    private @InjectMocks TransferContainerType transferContainerType;

    /**
     * Given that we try to add a non "rma:transfer" type to transfer container,
     * Then InvalidParameterException is thrown.
     */
    @Test(expected = InvalidParameterException.class)
    public void testAddNonTransferTypeToTransferContainerTest()
    {
        NodeRef transferContainer = generateNodeRef(TYPE_TRANSFER_CONTAINER, true);

        QName type = AlfMock.generateQName();
        when(mockedDictionaryService.isSubClass(type, TYPE_TRANSFER)).thenReturn(false);
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, type);

        ChildAssociationRef mockedChildAssoc = mock(ChildAssociationRef.class);
        when(mockedChildAssoc.getChildRef()).thenReturn(nodeRef);
        when(mockedChildAssoc.getParentRef()).thenReturn(transferContainer);
        transferContainerType.onCreateChildAssociation(mockedChildAssoc, true);
    }
}
