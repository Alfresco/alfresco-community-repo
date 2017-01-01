/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

package org.alfresco.rm.rest.api.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.rest.api.model.Node;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

/**
 * Unit Test class for RMNodesImpl.getOrCreatePath method
 * 
 * @author Ana Bozianu
 * @since 2.6
 */
public class RMNodesImplRelativePathUnitTest  extends BaseUnitTest
{
    @InjectMocks
    private RMNodesImpl rmNodesImpl;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);

        when(mockedDictionaryService.isSubClass(TYPE_RECORD_CATEGORY, RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER)).thenReturn(false);
        when(mockedDictionaryService.isSubClass(TYPE_RECORD_CATEGORY, RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER)).thenReturn(false);
        when(mockedDictionaryService.isSubClass(ContentModel.TYPE_CONTENT, ContentModel.TYPE_CONTENT)).thenReturn(true);
        when(mockedDictionaryService.isSubClass(TYPE_UNFILED_RECORD_CONTAINER, RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER)).thenReturn(true);
        when(mockedDictionaryService.isSubClass(TYPE_RECORD_FOLDER, ContentModel.TYPE_CONTENT)).thenReturn(false);
        when(mockedNamespaceService.getNamespaceURI(NamespaceService.CONTENT_MODEL_PREFIX)).thenReturn(NamespaceService.CONTENT_MODEL_1_0_URI);
        when(mockedNamespaceService.getNamespaceURI(RM_PREFIX)).thenReturn(RM_URI);
    }

    /**
     * Given any parent node
     * When trying to create a node in the parent node with no relative path
     * Then the parent node is returned and no node is created
     */
    @Test
    public void testNoRelativePath() throws Exception
    {
        /*
         * Given any parent node
         */
        NodeRef parentNode = AlfMock.generateNodeRef(mockedNodeService);

        /*
         *  When trying to create a node in the parent node with no relative path
         */
        Node nodeInfo = mock(Node.class);
        NodeRef returnedPath = rmNodesImpl.getOrCreatePath(parentNode.getId(), null, ContentModel.TYPE_CONTENT);

        /*
         * Then the parent node is returned and no node is created
         */
        assertEquals(parentNode, returnedPath);
        verify(mockedFileFolderService, never()).create(any(), any(), any());
        verify(mockedFilePlanService, never()).createRecordCategory(any(), any());
    }

    /**
     * Given a parent node and an existing path c1/f1 under it
     * When trying to create a node in the parent node with the relative path c1/f1
     * Then the node f1 is returned and no node is created
     */
    @Test
    public void testGetExistingRelativePath() throws Exception
    {
        /*
         * Given a parent node and an existing path c1/f1 under it
         */
        NodeRef parentNode = AlfMock.generateNodeRef(mockedNodeService);

        String category = "c1";
        NodeRef categoryNode = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedNodeService.getChildByName(parentNode, ContentModel.ASSOC_CONTAINS, category)).thenReturn(categoryNode);

        String recordFolder = "f1";
        NodeRef recordFolderNode = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedNodeService.getChildByName(categoryNode, ContentModel.ASSOC_CONTAINS, recordFolder)).thenReturn(recordFolderNode);

        /*
         * When trying to create a node in the parent node with the relative path c1/f1
         */
        Node nodeInfo = mock(Node.class);
        NodeRef returnedPath = rmNodesImpl.getOrCreatePath(parentNode.getId(), category + "/" + recordFolder, ContentModel.TYPE_CONTENT);

        /*
         * Then the node f1 is returned and no node is created
         */
        assertEquals(recordFolderNode, returnedPath);
        verify(mockedFileFolderService, never()).create(any(), any(), any());
        verify(mockedFilePlanService, never()).createRecordCategory(any(), any());
    }

    /**
     * Given the fileplan and an existing path c1/c2 under fileplan
     * When creating a content node under fileplan with the relative path c1/c2/c3/f1
     * Then the category c3 and the record folder f1 should be created and f1 should be returned
     * 
     * @throws Exception
     */
    @Test
    public void testCreatePartiallyExistingRelativePath() throws Exception
    {
        /*
         *  Given the fileplan and an existing path c1/c2 under fileplan
         */
        NodeRef fileplanNodeRef = AlfMock.generateNodeRef(mockedNodeService);

        // create c1
        String category1 = "c1";
        NodeRef categoryNode1 = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedNodeService.getChildByName(fileplanNodeRef, ContentModel.ASSOC_CONTAINS, category1)).thenReturn(categoryNode1);

        // create c2
        String category2 = "c2";
        NodeRef categoryNode2 = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedNodeService.getChildByName(categoryNode1, ContentModel.ASSOC_CONTAINS, category2)).thenReturn(categoryNode2);
        when(mockedNodeService.getType(categoryNode2)).thenReturn(TYPE_RECORD_CATEGORY);

        /*
         *  When trying to create a content node in the relative path c1/c2/c3/f1
         */
        // c3
        String category3 = "c3";
        NodeRef categoryNode3 = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedFilePlanService.createRecordCategory(categoryNode2, category3)).thenReturn(categoryNode3);

        // f1
        String recordFolder = "f1";
        NodeRef recordFolderNode = AlfMock.generateNodeRef(mockedNodeService);
        FileInfo recordFolderFileInfo = mock(FileInfo.class);
        when(recordFolderFileInfo.getNodeRef()).thenReturn(recordFolderNode);
        when(mockedFileFolderService.create(categoryNode3, recordFolder, RecordsManagementModel.TYPE_RECORD_FOLDER)).thenReturn(recordFolderFileInfo);

        // call the class under tests
        NodeRef returnedPath = rmNodesImpl.getOrCreatePath(fileplanNodeRef.getId(), category1 + "/" + category2 + "/" + category3 + "/" + recordFolder, ContentModel.TYPE_CONTENT);

        /*
         *  Then the category c1 and the record folder f1 should be created and f1 should be returned
         */
        assertEquals(recordFolderNode, returnedPath);
        verify(mockedFilePlanService, times(1)).createRecordCategory(categoryNode2, category3);
        verify(mockedFileFolderService, times(1)).create(categoryNode3, recordFolder, RecordsManagementModel.TYPE_RECORD_FOLDER);
    }

    /**
     * Given the unfiled record container
     * When creating a content node under fileplan with the relative path f1/f2/f3
     * Then the 3 unfiled record folders should be created and f3 should be returned
     */
    @Test
    public void testCreateRelativePathInUnfiledRecords() throws Exception
    {
        /*
         *  Given the unfiled record folder
         */
        NodeRef unfiledRecordContainer = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedNodeService.getType(unfiledRecordContainer)).thenReturn(TYPE_UNFILED_RECORD_CONTAINER);

        /*
         *  When trying to create a content node in the relative path f1/f2/f3
         */
        // f1
        String folder1 = "f1";
        NodeRef folderNode1 = AlfMock.generateNodeRef(mockedNodeService);
        FileInfo folderFileInfo1 = mock(FileInfo.class);
        when(folderFileInfo1.getNodeRef()).thenReturn(folderNode1);
        when(mockedFileFolderService.create(unfiledRecordContainer, folder1, RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER)).thenReturn(folderFileInfo1);

        // f2
        String folder2 = "f2";
        NodeRef folderNode2 = AlfMock.generateNodeRef(mockedNodeService);
        FileInfo folderFileInfo2 = mock(FileInfo.class);
        when(folderFileInfo2.getNodeRef()).thenReturn(folderNode2);
        when(mockedFileFolderService.create(folderNode1, folder2, RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER)).thenReturn(folderFileInfo2);

        // f3
        String folder3 = "f3";
        NodeRef folderNode3 = AlfMock.generateNodeRef(mockedNodeService);
        FileInfo folderFileInfo3 = mock(FileInfo.class);
        when(folderFileInfo3.getNodeRef()).thenReturn(folderNode3);
        when(mockedFileFolderService.create(folderNode2, folder3, RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER)).thenReturn(folderFileInfo3);

        // call the class under tests
        NodeRef returnedParentNode = rmNodesImpl.getOrCreatePath(unfiledRecordContainer.getId(), folder1 + "/" + folder2 + "/" + folder3, ContentModel.TYPE_CONTENT);

        /*
         *  Then the category c1 and the record folder rf1 should be created 
         *  and an instance to the record folder should be returned
         */
        assertEquals(folderNode3, returnedParentNode);
        verify(mockedFileFolderService, times(1)).create(unfiledRecordContainer, folder1, RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER);
        verify(mockedFileFolderService, times(1)).create(folderNode1, folder2, RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER);
        verify(mockedFileFolderService, times(1)).create(folderNode2, folder3, RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER);

        //check no other node is created
        verify(mockedFilePlanService, never()).createRecordCategory(any(), any());
        verify(mockedFileFolderService, times(3)).create(any(), any(), any());
    }

    /**
     * Given the fileplan
     * When creating a record folder node under fileplan with the relative path c1/c2/c3
     * Then the categories c1, c2 and c3 should be created and c3 should be returned
     */
    @Test
    public void testCreateRelativePathToRecordFolder() throws Exception
    {
        /*
         *  Given the fileplan
         */
        NodeRef fileplanNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedNodeService.getType(fileplanNodeRef)).thenReturn(TYPE_FILE_PLAN);

        /*
         *  When trying to create a folder node in the relative path c1/c2/c3
         */
        // c1
        String category1 = "c1";
        NodeRef categoryNode1 = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedFilePlanService.createRecordCategory(fileplanNodeRef, category1)).thenReturn(categoryNode1);

        // c2
        String category2 = "c2";
        NodeRef categoryNode2 = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedFilePlanService.createRecordCategory(categoryNode1, category2)).thenReturn(categoryNode2);

        // c3
        String category3 = "c3";
        NodeRef categoryNode3 = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedFilePlanService.createRecordCategory(categoryNode2, category3)).thenReturn(categoryNode3);

        // call the class under tests
        NodeRef returnedParentNode = rmNodesImpl.getOrCreatePath(fileplanNodeRef.getId(), category1 + "/" + category2 + "/" + category3, RecordsManagementModel.TYPE_RECORD_FOLDER);

        /*
         *  Then the categories c1, c2 and c3 should be created and c3 should be returned
         */
        assertEquals(categoryNode3, returnedParentNode);
        verify(mockedFilePlanService, times(1)).createRecordCategory(fileplanNodeRef, category1);
        verify(mockedFilePlanService, times(1)).createRecordCategory(categoryNode1, category2);
        verify(mockedFilePlanService, times(1)).createRecordCategory(categoryNode2, category3);

        // check no other node is created
        verify(mockedFilePlanService, times(3)).createRecordCategory(any(), any());
        verify(mockedFileFolderService, never()).create(any(), any(), any());
    }
}
