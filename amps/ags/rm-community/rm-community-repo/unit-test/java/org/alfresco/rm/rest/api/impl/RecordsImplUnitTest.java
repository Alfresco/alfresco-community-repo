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
package org.alfresco.rm.rest.api.impl;

import static org.alfresco.model.ContentModel.TYPE_CONTENT;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.TYPE_RECORD_FOLDER;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.InvalidParameterException;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.module.org_alfresco_module_rm.test.util.MockAuthenticationUtilHelper;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.model.TargetContainer;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for RecordsImpl
 * 
 * @author Ana Bozianu
 * @since 2.6
 */
public class RecordsImplUnitTest
{
//    @Mock
//    private RecordService mockedRecordService;
//    @Mock
//    protected FilePlanService mockedFilePlanService;
//    @Mock
//    protected NodeService mockedNodeService;
//    @Mock
//    protected FileFolderService mockedFileFolderService;
//    @Mock
//    protected DictionaryService mockedDictionaryService;
//    @Mock
//    protected AuthenticationUtil mockedAuthenticationUtil;
//    @Mock
//    protected RMNodes mockedNodes;
//
//    @InjectMocks
//    private RecordsImpl recordsImpl;
//
//    @Before
//    public void before()
//    {
//        MockitoAnnotations.initMocks(this);
//
//        // setup mocked authentication util
//        MockAuthenticationUtilHelper.setup(mockedAuthenticationUtil);
//    }
//
//    /**
//     * Given a file and an existing fileplan
//     * When declaring a file as record
//     * Then a record is created under the existing fileplan from the provided file
//     */
//    @Test
//    public void testDeclareFileAsRecord()
//    {
//        /*
//         * Given a file and an existing fileplan
//         */
//        NodeRef mockedFile = AlfMock.generateNodeRef(mockedNodeService);
//        when(mockedNodes.validateNode(mockedFile.getId())).thenReturn(mockedFile);
//
//        NodeRef fileplan = AlfMock.generateNodeRef(mockedNodeService);
//        when(mockedFilePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID)).thenReturn(fileplan);
//
//        /*
//         * When declare the file as record
//         */
//        Parameters params = Mockito.mock(Parameters.class);
//        when(params.getParameter(Records.PARAM_HIDE_RECORD)).thenReturn("true");
//        recordsImpl.declareFileAsRecord(mockedFile.getId(), params);
//
//        /*
//         * Then a record is created under the existing fileplan from the provided file
//         */
//        verify(mockedRecordService).createRecord(fileplan, mockedFile, false);
//        verify(mockedNodes).getFolderOrDocument(mockedFile.getId(), params);
//    }
//
//    /**
//     * Given a record
//     * When trying to filing a record providing a blank destination path
//     * Then an InvalidParameterException is thrown
//     */
//    @Test(expected=InvalidParameterException.class)
//    public void testFileRecord_BlankDestinationPath()
//    {
//        /*
//         * Given a record
//         */
//        NodeRef mockedRecord = AlfMock.generateNodeRef(mockedNodeService);
//        when(mockedNodes.validateNode(mockedRecord.getId())).thenReturn(mockedRecord);
//
//        /*
//         * When trying to filing a record providing a blank destination path
//         */
//        Parameters params = Mockito.mock(Parameters.class);
//        TargetContainer target = new TargetContainer();
//        recordsImpl.fileOrLinkRecord(mockedRecord.getId(), target, params);
//    }
//
//    /**
//     * Given an unfiled record and an existing record folder
//     * When trying to file the record in the record folder
//     * Then the record is moved under the destination folder
//     */
//    @Test
//    public void testFileRecord_DestinationById() throws Exception
//    {
//        /*
//         * Given an unfiled record and an existing record folder
//         */
//        // mock the record to file
//        NodeRef mockedRecord = AlfMock.generateNodeRef(mockedNodeService);
//        when(mockedNodes.validateNode(mockedRecord.getId())).thenReturn(mockedRecord);
//
//        // mock the current primary parent
//        NodeRef mockedPrimaryParent = AlfMock.generateNodeRef(mockedNodeService);
//        ChildAssociationRef mockedChildAssoc = mock(ChildAssociationRef.class);
//        when(mockedChildAssoc.getParentRef()).thenReturn(mockedPrimaryParent);
//        when(mockedNodeService.getPrimaryParent(mockedRecord)).thenReturn(mockedChildAssoc);
//        when(mockedNodeService.getType(mockedPrimaryParent)).thenReturn(TYPE_UNFILED_RECORD_CONTAINER);
//        when(mockedDictionaryService.isSubClass(TYPE_UNFILED_RECORD_CONTAINER, TYPE_RECORD_FOLDER)).thenReturn(false);
//
//        // mock the target record folder to file the record into
//        NodeRef mockedTargetRecordFolder = AlfMock.generateNodeRef(mockedNodeService);
//        when(mockedNodes.getOrCreatePath(mockedTargetRecordFolder.getId(), null, TYPE_CONTENT)).thenReturn(mockedTargetRecordFolder);
//        when(mockedNodeService.getType(mockedTargetRecordFolder)).thenReturn(TYPE_RECORD_FOLDER);
//        when(mockedDictionaryService.isSubClass(TYPE_RECORD_FOLDER, TYPE_RECORD_FOLDER)).thenReturn(true);
//
//        /*
//         * When trying to file the record in the record folder
//         */
//        TargetContainer destination = new TargetContainer();
//        destination.setTargetParentId(mockedTargetRecordFolder.getId());
//
//        Parameters params = Mockito.mock(Parameters.class);
//        recordsImpl.fileOrLinkRecord(mockedRecord.getId(), destination, params);
//
//        /*
//         * Then the record is moved under the destination folder
//         */
//        verify(mockedNodes).getOrCreatePath(mockedTargetRecordFolder.getId(), null, TYPE_CONTENT);
//        verify(mockedFileFolderService).moveFrom(mockedRecord, mockedPrimaryParent, mockedTargetRecordFolder, null);
//    }
//
//    /**
//     * Given an unfiled record 
//     *   and an existing record folder with relative path category/recordFolder from fileplan
//     * When trying to file the record using a relative path and no target id
//     * Then the record is moved under the destination folder relative to the fileplan
//     */
//    @Test
//    public void testFileRecord_DestinationRelativeToFileplan() throws Exception
//    {
//        /*
//         * Given an unfiled record and an existing record folder
//         */
//        // mock the record to file
//        NodeRef mockedRecord = AlfMock.generateNodeRef(mockedNodeService);
//        when(mockedNodes.validateNode(mockedRecord.getId())).thenReturn(mockedRecord);
//
//        // mock the current primary parent
//        NodeRef mockedPrimaryParent = AlfMock.generateNodeRef(mockedNodeService);
//        ChildAssociationRef mockedChildAssoc = mock(ChildAssociationRef.class);
//        when(mockedChildAssoc.getParentRef()).thenReturn(mockedPrimaryParent);
//        when(mockedNodeService.getPrimaryParent(mockedRecord)).thenReturn(mockedChildAssoc);
//        when(mockedNodeService.getType(mockedPrimaryParent)).thenReturn(TYPE_UNFILED_RECORD_CONTAINER);
//        when(mockedDictionaryService.isSubClass(TYPE_UNFILED_RECORD_CONTAINER, TYPE_RECORD_FOLDER)).thenReturn(false);
//
//        // mock the fileplan
//        NodeRef fileplan = AlfMock.generateNodeRef(mockedNodeService);
//        when(mockedFilePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID)).thenReturn(fileplan);
//
//        // mock the target record folder to file the record into
//        String relativePath = "category/recordFolder";
//        NodeRef mockedTargetRecordFolder = AlfMock.generateNodeRef(mockedNodeService);
//        when(mockedNodes.getOrCreatePath(fileplan.getId(), relativePath, TYPE_CONTENT)).thenReturn(mockedTargetRecordFolder);
//        when(mockedNodeService.getType(mockedTargetRecordFolder)).thenReturn(TYPE_RECORD_FOLDER);
//        when(mockedDictionaryService.isSubClass(TYPE_RECORD_FOLDER, TYPE_RECORD_FOLDER)).thenReturn(true);
//
//        /*
//         * When trying to file the record using a relative path and no target id
//         */
//        TargetContainer destination = new TargetContainer();
//        destination.setRelativePath(relativePath);
//
//        Parameters params = Mockito.mock(Parameters.class);
//        recordsImpl.fileOrLinkRecord(mockedRecord.getId(), destination, params);
//
//        /*
//         * Then the record is moved under the destination folder relative to the fileplan
//         */
//        verify(mockedNodes).getOrCreatePath(fileplan.getId(), relativePath, TYPE_CONTENT);
//        verify(mockedFileFolderService).moveFrom(mockedRecord, mockedPrimaryParent, mockedTargetRecordFolder, null);
//    }
//
//    /**
//     * Given an unfiled record 
//     *   and an existing record folder with relative path category/recordFolder from a given category
//     * When trying to file the record describing the target folder with the category id and the relative path
//     * Then the record is moved under the correct destination folder
//     */
//    @Test
//    public void testFileRecord_DestinationRelativeToProvidedId() throws Exception
//    {
//        /*
//         * Given an unfiled record and an existing record folder with relative path category/recordFolder from a given category
//         */
//        // mock the record to file
//        NodeRef mockedRecord = AlfMock.generateNodeRef(mockedNodeService);
//        when(mockedNodes.validateNode(mockedRecord.getId())).thenReturn(mockedRecord);
//
//        // mock the current primary parent
//        NodeRef mockedPrimaryParent = AlfMock.generateNodeRef(mockedNodeService);
//        ChildAssociationRef mockedChildAssoc = mock(ChildAssociationRef.class);
//        when(mockedChildAssoc.getParentRef()).thenReturn(mockedPrimaryParent);
//        when(mockedNodeService.getPrimaryParent(mockedRecord)).thenReturn(mockedChildAssoc);
//        when(mockedNodeService.getType(mockedPrimaryParent)).thenReturn(TYPE_UNFILED_RECORD_CONTAINER);
//        when(mockedDictionaryService.isSubClass(TYPE_UNFILED_RECORD_CONTAINER, TYPE_RECORD_FOLDER)).thenReturn(false);
//
//        // mock the target category
//        NodeRef mockedTargetCategory = AlfMock.generateNodeRef(mockedNodeService);
//
//        // mock the target record folder to file the record into
//        String relativePath = "category/recordFolder";
//        NodeRef mockedTargetRecordFolder = AlfMock.generateNodeRef(mockedNodeService);
//        when(mockedNodes.getOrCreatePath(mockedTargetCategory.getId(), relativePath, TYPE_CONTENT)).thenReturn(mockedTargetRecordFolder);
//        when(mockedNodeService.getType(mockedTargetRecordFolder)).thenReturn(TYPE_RECORD_FOLDER);
//        when(mockedDictionaryService.isSubClass(TYPE_RECORD_FOLDER, TYPE_RECORD_FOLDER)).thenReturn(true);
//
//        /*
//         *  When trying to file the record describing the target folder with the category id and the relative path
//         */
//        TargetContainer destination = new TargetContainer();
//        destination.setTargetParentId(mockedTargetCategory.getId());
//        destination.setRelativePath(relativePath);
//
//        Parameters params = Mockito.mock(Parameters.class);
//        recordsImpl.fileOrLinkRecord(mockedRecord.getId(), destination, params);
//
//        /*
//         * Then the record is moved under the correct destination folder
//         */
//        verify(mockedNodes).getOrCreatePath(mockedTargetCategory.getId(), relativePath, TYPE_CONTENT);
//        verify(mockedFileFolderService).moveFrom(mockedRecord, mockedPrimaryParent, mockedTargetRecordFolder, null);
//    }
//
//    /**
//     * Given an filed record and an existing record folder
//     * When trying to link the record to the record folder
//     * Then the record is linked to the destination folder
//     */
//    @Test
//    public void testLinkRecord()
//    {
//        /*
//         * Given an filed record and an existing record folder
//         */
//        // mock the record to link
//        NodeRef mockedRecord = AlfMock.generateNodeRef(mockedNodeService);
//        when(mockedNodes.validateNode(mockedRecord.getId())).thenReturn(mockedRecord);
//
//        // mock the current primary parent
//        NodeRef mockedPrimaryParent = AlfMock.generateNodeRef(mockedNodeService);
//        ChildAssociationRef mockedChildAssoc = mock(ChildAssociationRef.class);
//        when(mockedChildAssoc.getParentRef()).thenReturn(mockedPrimaryParent);
//        when(mockedNodeService.getPrimaryParent(mockedRecord)).thenReturn(mockedChildAssoc);
//        when(mockedNodeService.getType(mockedPrimaryParent)).thenReturn(TYPE_RECORD_FOLDER);
//        when(mockedDictionaryService.isSubClass(TYPE_RECORD_FOLDER, TYPE_RECORD_FOLDER)).thenReturn(true);
//
//        // mock the target record folder to file the record into
//        NodeRef mockedTargetRecordFolder = AlfMock.generateNodeRef(mockedNodeService);
//        when(mockedNodes.getOrCreatePath(mockedTargetRecordFolder.getId(), null, TYPE_CONTENT)).thenReturn(mockedTargetRecordFolder);
//        when(mockedNodeService.getType(mockedTargetRecordFolder)).thenReturn(TYPE_RECORD_FOLDER);
//
//        /*
//         * When trying to link the record to the record folder
//         */
//        TargetContainer destination = new TargetContainer();
//        destination.setTargetParentId(mockedTargetRecordFolder.getId());
//
//        Parameters params = Mockito.mock(Parameters.class);
//        recordsImpl.fileOrLinkRecord(mockedRecord.getId(), destination, params);
//
//        /*
//         * Then the record is linked to the destination folder
//         */
//        verify(mockedNodes).getOrCreatePath(mockedTargetRecordFolder.getId(), null, TYPE_CONTENT);
//        verify(mockedRecordService).link(mockedRecord, mockedTargetRecordFolder);
//    }
}
