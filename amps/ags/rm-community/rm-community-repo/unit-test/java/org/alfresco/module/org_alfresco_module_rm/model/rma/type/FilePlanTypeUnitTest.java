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

import java.util.Arrays;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

import com.google.common.collect.Sets;

/**
 * Unit test that test the conditions enforced by FilePlanType behavior bean
 * 
 * @author Ana Bozianu
 * @since 2.6
 */
public class FilePlanTypeUnitTest extends BaseUnitTest
{

    /** test object */
    private @InjectMocks FilePlanType filePlanType;

    /** existing fileplan node */
    private NodeRef filePlanContainer;

    @Before
    public void setup()
    {
        filePlanContainer = generateNodeRef(TYPE_FILE_PLAN, true);
    }

    /**
     * Having the Fileplan container
     * When adding a child of type TYPE_FILE_PLAN
     * Then an error should be thrown
     */
    @Test (expected = IntegrityException.class)
    public void testAddFileplanToFileplan()
    {
        ChildAssociationRef childAssoc = createFileplanContainerChild(TYPE_FILE_PLAN);
        filePlanType.onCreateChildAssociation(childAssoc, true);
    }

    /**
     * Having the Fileplan container
     * When adding multiple child of type TYPE_RECORD_CATEGORY
     * Then child associations should be created
     */
    @Test
    public void testAddCategoriesToFileplan()
    {
        // add the first child
        ChildAssociationRef childAssoc1 = createFileplanContainerChild(TYPE_RECORD_CATEGORY);
        filePlanType.onCreateChildAssociation(childAssoc1, true);

        // add the second child
        ChildAssociationRef childAssoc2 = createFileplanContainerChild(TYPE_RECORD_CATEGORY);
        filePlanType.onCreateChildAssociation(childAssoc2, true);
    }

    /**
     * Having the Fileplan container
     * When creating the first child of type TYPE_HOLD_CONTAINER
     * Then the fileplan behavior bean shouldn't complain
     */
    @Test
    public void testCreateHoldContainers()
    {
        ChildAssociationRef childAssoc = createFileplanContainerChild(TYPE_HOLD_CONTAINER);

        when(mockedNodeService.getChildAssocs(filePlanContainer, Sets.newHashSet(TYPE_HOLD_CONTAINER))).thenReturn(Arrays.asList(childAssoc));
        filePlanType.onCreateChildAssociation(childAssoc, true);
    }

    /**
     * Having the Fileplan container with a child of type TYPE_HOLD_CONTAINER
     * When adding another child of type TYPE_HOLD_CONTAINER
     * Then an error should be thrown
     */
    @Test (expected = IntegrityException.class)
    public void testCreateMultipleHoldContainers()
    {
        ChildAssociationRef existingHoldAssoc = createFileplanContainerChild(TYPE_HOLD_CONTAINER);
        ChildAssociationRef childAssoc = createFileplanContainerChild(TYPE_HOLD_CONTAINER);

        when(mockedNodeService.getChildAssocs(filePlanContainer, Sets.newHashSet(TYPE_HOLD_CONTAINER))).thenReturn(Arrays.asList(existingHoldAssoc, childAssoc));
        filePlanType.onCreateChildAssociation(childAssoc, true);
    }

    /**
     * Having the Fileplan container
     * When creating the first child of type TYPE_TRANSFER_CONTAINER
     * Then the fileplan behavior bean shouldn't complain
     */
    @Test
    public void testCreateTransferContainers()
    {
        ChildAssociationRef childAssoc = createFileplanContainerChild(TYPE_TRANSFER_CONTAINER);

        when(mockedNodeService.getChildAssocs(filePlanContainer, Sets.newHashSet(TYPE_TRANSFER_CONTAINER))).thenReturn(Arrays.asList(childAssoc));
        filePlanType.onCreateChildAssociation(childAssoc, true);
    }

    /**
     * Having the Fileplan container with a child of type TYPE_TRANSFER_CONTAINER
     * When adding another child of type TYPE_TRANSFER_CONTAINER
     * Then an error should be thrown
     */
    @Test (expected = IntegrityException.class)
    public void testCreateMultipleTransferContainers()
    {
        ChildAssociationRef existingHoldAssoc = createFileplanContainerChild(TYPE_TRANSFER_CONTAINER);
        ChildAssociationRef childAssoc = createFileplanContainerChild(TYPE_TRANSFER_CONTAINER);

        when(mockedNodeService.getChildAssocs(filePlanContainer, Sets.newHashSet(TYPE_TRANSFER_CONTAINER))).thenReturn(Arrays.asList(existingHoldAssoc, childAssoc));
        filePlanType.onCreateChildAssociation(childAssoc, true);
    }

    /**
     * Having the Fileplan container
     * When creating the first child of type TYPE_UNFILED_RECORD_CONTAINER
     * Then the fileplan behavior bean shouldn't complain
     */
    @Test
    public void testCreateUnfiledRecordsContainers()
    {
        ChildAssociationRef childAssoc = createFileplanContainerChild(TYPE_UNFILED_RECORD_CONTAINER);

        when(mockedNodeService.getChildAssocs(filePlanContainer, Sets.newHashSet(TYPE_UNFILED_RECORD_CONTAINER))).thenReturn(Arrays.asList(childAssoc));
        filePlanType.onCreateChildAssociation(childAssoc, true);
    }

    /**
     * Having the Fileplan container with a child of type TYPE_UNFILED_RECORD_CONTAINER
     * When adding another child of type TYPE_UNFILED_RECORD_CONTAINER
     * Then an error should be thrown
     */
    @Test (expected = IntegrityException.class)
    public void testCreateMultipleUnfiledRecordsContainers()
    {
        ChildAssociationRef existingHoldAssoc = createFileplanContainerChild(TYPE_UNFILED_RECORD_CONTAINER);
        ChildAssociationRef childAssoc = createFileplanContainerChild(TYPE_UNFILED_RECORD_CONTAINER);

        when(mockedNodeService.getChildAssocs(filePlanContainer, Sets.newHashSet(TYPE_UNFILED_RECORD_CONTAINER))).thenReturn(Arrays.asList(existingHoldAssoc, childAssoc));
        filePlanType.onCreateChildAssociation(childAssoc, true);
    }

    /**
     * Helper method that creates a child of the fileplan container with the provided type
     * @param childType the node type of the child to be created
     * @return the child association between the fileplan and the created node
     */
    private ChildAssociationRef createFileplanContainerChild(QName childType)
    {
        NodeRef child = generateNodeRef(childType);
        return new ChildAssociationRef( ContentModel.ASSOC_CONTAINS, filePlanContainer, childType, child);
    }
}
