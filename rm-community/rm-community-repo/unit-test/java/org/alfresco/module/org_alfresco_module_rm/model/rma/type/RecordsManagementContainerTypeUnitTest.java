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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.module.org_alfresco_module_rm.test.util.TestModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.mockito.InjectMocks;

/**
 * Unit test for RecordsManagementContainerType
 * @author Ana Bozianu
 * @since 2.4
 */
public class RecordsManagementContainerTypeUnitTest extends BaseUnitTest
{
    /** test object */
    private @InjectMocks RecordsManagementContainerType recordManagementContainerType;

    /**
     * Having the Unfilled Record container and a folder
     * When adding a child association between the folder and the container
     * Then the folder type shouldn't be renamed
     */
    @Test
    public void testAddFolderToRMContainer()
    {
        /* Having a RM container and a folder */
        NodeRef rmContainer = generateRMContainer();
        NodeRef rmFolder = generateFolderNode(false);

        /*
         * When adding a child association between the folder and the container
         */
        ChildAssociationRef childAssoc = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, rmContainer, ContentModel.ASSOC_CONTAINS, rmFolder);
        recordManagementContainerType.onCreateChildAssociation(childAssoc, true);

        /* Then the node type should not be changed to TYPE_RECORD_FOLDER */
        verify(mockedNodeService).setType(rmFolder, TYPE_RECORD_FOLDER);
        verify(mockedRecordFolderService).setupRecordFolder(rmFolder);
    }

    /**
     * Having the Unfilled Record container and a folder having the aspect ASPECT_HIDDEN
     * When adding a child association between the folder and the container
     * Then the folder type shouldn't be renamed
     */
     @Test
    public void testAddHiddenFolderToRMContainer()
    {
        /* Having a RM container and a folder with ASPECT_HIDDEN applied */
        NodeRef rmContainer = generateRMContainer();
        NodeRef rmFolder = generateFolderNode(true);

        /*
         * When adding a child association between the folder and the container
         */
        ChildAssociationRef childAssoc = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, rmContainer, ContentModel.ASSOC_CONTAINS, rmFolder);
        recordManagementContainerType.onCreateChildAssociation(childAssoc, true);

        /* Then the node type should not be changed to TYPE_RECORD_FOLDER */
        verify(mockedNodeService, never()).setType(rmFolder, TYPE_RECORD_FOLDER);
        verify(mockedRecordFolderService, never()).setupRecordFolder(rmFolder);
    }

    /**
     * Trying to create a RM folder and its sub-types via SFDC, IMap, WebDav, etc
     * Check that exception is thrown on creating child associations
     */
    @Test
    public void testRM3450()
    {
        NodeRef rmContainer = generateRMContainer();
        NodeRef nonRmFolder = generateNonRmFolderNode();

        ChildAssociationRef childAssoc = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, rmContainer, TestModel.NOT_RM_FOLDER_TYPE, nonRmFolder);
        try
        {
            recordManagementContainerType.onCreateChildAssociation(childAssoc, true);
            fail("Expected to throw exception on create child association.");
        }
        catch (Throwable e)
        {
            assertTrue(e instanceof AlfrescoRuntimeException);
        }
    }

    /**
     * Generates a record management container
     * @return reference to the generated container
     */
    private NodeRef generateRMContainer()
    {
        NodeRef rmContainer = generateNodeRef();
        when(mockedNodeService.getType(rmContainer)).thenReturn(RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER);
        when(mockedDictionaryService.isSubClass(RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER, TYPE_FILE_PLAN)).thenReturn(false);
        return rmContainer;
    }

    /**
     * Generates a folder node
     * @param hasHiddenAspect does the folder node have the aspect ASPECT_HIDDEN
     * @return reference to the created folder
     */
    private NodeRef generateFolderNode(boolean hasHiddenAspect)
    {
        NodeRef rmFolder = generateNodeRef();
        when(mockedDictionaryService.isSubClass(ContentModel.TYPE_FOLDER, ContentModel.TYPE_FOLDER)).thenReturn(true);
        when(mockedDictionaryService.isSubClass(ContentModel.TYPE_FOLDER, ContentModel.TYPE_SYSTEM_FOLDER)).thenReturn(false);
        when(mockedNodeService.getType(rmFolder)).thenReturn(ContentModel.TYPE_FOLDER);
        when(mockedNodeService.exists(rmFolder)).thenReturn(true);
        when(mockedNodeService.hasAspect(rmFolder, ContentModel.ASPECT_HIDDEN)).thenReturn(hasHiddenAspect);
        when(mockedNodeService.hasAspect(rmFolder, ASPECT_FILE_PLAN_COMPONENT)).thenReturn(false);
        return rmFolder;
    }
    
    /**
     * Generates a folder node
     * @return reference to the created folder
     */
    private NodeRef generateNonRmFolderNode()
    {
        NodeRef nonRmFolder = generateNodeRef();
        when(mockedDictionaryService.isSubClass(TestModel.NOT_RM_FOLDER_TYPE, ContentModel.TYPE_FOLDER)).thenReturn(true);
        when(mockedNodeService.getType(nonRmFolder)).thenReturn(TestModel.NOT_RM_FOLDER_TYPE);
        when(mockedNodeService.exists(nonRmFolder)).thenReturn(true);
        when(mockedNodeService.hasAspect(nonRmFolder, ContentModel.ASPECT_HIDDEN)).thenReturn(false);
        when(mockedNodeService.hasAspect(nonRmFolder, ASPECT_FILE_PLAN_COMPONENT)).thenReturn(false);
        return nonRmFolder;
    }
}
