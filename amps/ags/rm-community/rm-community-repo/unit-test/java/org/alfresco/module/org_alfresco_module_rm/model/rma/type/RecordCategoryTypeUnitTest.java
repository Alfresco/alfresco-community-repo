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

import static org.mockito.Mockito.verify;
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
 * Unit test class for RecordCategoryType
 *
 * @author Silviu Dinuta
 * @since 2.6
 *
 */
public class RecordCategoryTypeUnitTest extends BaseUnitTest
{
    @Mock
    private AuthenticationUtil mockAuthenticationUtil;

    @Mock
    private VitalRecordService mockedVitalRecordService;

    private @InjectMocks RecordCategoryType recordCategoryType;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
        MockAuthenticationUtilHelper.setup(mockAuthenticationUtil);
        when(mockedApplicationContext.getBean("dbNodeService")).thenReturn(mockedNodeService);
    }

    /**
     * Given that we try to add types different than "rma:recordCategory" and "rma:recordFolder" to a record category,
     * Then IntegrityException is thrown.
     */
    @Test(expected = IntegrityException.class)
    public void testCreateNonAceptedTypes() throws Exception
    {
        NodeRef recordCategoryNodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_RECORD_CATEGORY);
        QName type = AlfMock.generateQName();
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, type, true);
        ChildAssociationRef childAssocRef = generateChildAssociationRef(recordCategoryNodeRef, nodeRef);
        recordCategoryType.onCreateChildAssociation(childAssocRef, true);
    }

    /**
     * Given that we try to add "rma:recordCategory" type to a record category,
     * Then operation is successful.
     */
    @Test
    public void testCreateRecordCategory() throws Exception
    {
        NodeRef recordCategoryNodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_RECORD_CATEGORY);
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_RECORD_CATEGORY, true);
        ChildAssociationRef childAssocRef = generateChildAssociationRef(recordCategoryNodeRef, nodeRef);
        recordCategoryType.onCreateChildAssociation(childAssocRef, true);
    }

    /**
     * Given that we try to add "rma:recordFolder" type to a record category,
     * Then operation is successful.
     */
    @Test
    public void testCreateRecordFolder() throws Exception
    {
        NodeRef recordCategoryNodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_RECORD_CATEGORY);
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_RECORD_FOLDER, true);
        ChildAssociationRef childAssocRef = generateChildAssociationRef(recordCategoryNodeRef, nodeRef);
        recordCategoryType.onCreateChildAssociation(childAssocRef, true);
    }
}
