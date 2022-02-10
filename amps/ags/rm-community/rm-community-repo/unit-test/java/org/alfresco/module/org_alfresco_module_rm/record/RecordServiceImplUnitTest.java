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

package org.alfresco.module.org_alfresco_module_rm.record;

import static org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock.generateQName;
import static org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock.generateText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.notification.RecordsManagementNotificationHelper;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

/**
 * Unit test for RecordServiceImpl
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class RecordServiceImplUnitTest extends BaseUnitTest
{
    private NodeRef nonStandardFilePlanComponent;
    private NodeRef nonStandardFilePlan;
    private NodeRef dmNodeRef;
    private NodeRef unfiledRecordContainer;
    private NodeRef frozenRecordFolder;
    private NodeRef closedRecordFolder;
    private ChildAssociationRef parentAssoc;

    private final String recordId = "recordId";
    private final String documentOwner = "Bob";
    private final String originalName = "originalName";
    private final NodeRef originatingLocation = new NodeRef("workspace://SpacesStore/originalLocation");
    private final NodeRef link = new NodeRef("workspace://SpacesStore/linkLocation");

    @Mock
    private RenditionService mockedRenditionService;

    @Mock
    private RecordsManagementNotificationHelper mockedNotificationHelper;
    @Mock
    private ChildAssociationRef mockedChildAssoc;
    @Mock
    private ChildAssociationRef mockedLinkAssoc;
    @Mock
    private ChildAssociationRef mockedParentAssoc;

    private static QName TYPE_MY_FILE_PLAN                  = generateQName();
    private static QName ASPECT_FOR_FILE_PLAN               = generateQName();

    @Spy @InjectMocks private RecordServiceImpl recordService;

    @SuppressWarnings("unchecked")
    @Before
    @Override
    public void before() throws Exception
    {
        super.before();

        nonStandardFilePlanComponent = generateNodeRef(TYPE_RECORD_CATEGORY);
        nonStandardFilePlan = generateNodeRef(TYPE_MY_FILE_PLAN);
        dmNodeRef = generateNodeRef(TYPE_CONTENT);
        unfiledRecordContainer = generateNodeRef(TYPE_UNFILED_RECORD_CONTAINER);
        frozenRecordFolder = generateNodeRef(TYPE_RECORD_FOLDER);
        closedRecordFolder = generateNodeRef(TYPE_RECORD_FOLDER);
        parentAssoc = mock(ChildAssociationRef.class);

        // set-up node service
        when(mockedNodeService.getProperty(nonStandardFilePlanComponent, PROP_ROOT_NODEREF)).thenReturn(nonStandardFilePlan);

        // set-up dictionary service
        when(mockedDictionaryService.getAllAspects()).thenReturn(CollectionUtils.EMPTY_COLLECTION);
        
        // mock up getting behaviours
        when(recordService.getBehaviour(any(String.class))).thenReturn(mock(Behaviour.class));
    }

    @Test
    public void testRegisterRecordMetadataAspect()
    {
        Map<QName, Set<QName>> map = recordService.getRecordMetadataAspectsMap();
        assertTrue(map.isEmpty());
        recordService.registerRecordMetadataAspect(ASPECT_FOR_FILE_PLAN, TYPE_FILE_PLAN);
        map = recordService.getRecordMetadataAspectsMap();
        assertEquals(1, map.size());
        assertTrue(map.containsKey(ASPECT_FOR_FILE_PLAN));
        Set<QName> types = map.get(ASPECT_FOR_FILE_PLAN);
        assertNotNull(types);
        assertEquals(1, types.size());
        assertTrue(types.contains(TYPE_FILE_PLAN));
    }
    
    /**
     * Given invalid types
     * When linking
     * Then exception thrown
     */
    @Test
    public void linkNonRecord()
    {
        NodeRef nonRecord = generateNodeRef(TYPE_CONTENT);
        NodeRef recordFolder = generateRecordFolder();
        
        // set expected exception
        exception.expect(RecordLinkRuntimeException.class);
        
        // link
        recordService.link(nonRecord, recordFolder);
    }    
    @Test
    public void linkNonRecordFolder()
    {
        NodeRef record = generateRecord();
        NodeRef nonRecordFolder = generateNodeRef(TYPE_FOLDER);
        
        // set expected exception
        exception.expect(RecordLinkRuntimeException.class);
        
        // link
        recordService.link(record, nonRecordFolder);
    }
    
    /**
     * Given that the record is already a child of the record folder
     * When I try to link the record to the same record folder
     * Then an exception is thrown
     */
    @Test
    public void linkRecordToRecordFolderFailsIfAlreadyAChild()
    {
        NodeRef record = generateRecord();
        NodeRef recordFolder = generateRecordFolder();
        
        // given that the record is already a child of the record folder
        makeChildrenOf(recordFolder, record);
        
        // set expected exception
        exception.expect(RecordLinkRuntimeException.class);
        
        // link
        recordService.link(record, recordFolder);
    }
    
    /**
     * Given a record that is not a child of a record folder
     * When I link the record to the record folder
     * Then the record is now linked to the record folder
     */
    @Test
    public void linkRecordToRecordFolder()
    {
        NodeRef record = generateRecord();
        NodeRef recordFolder = generateRecordFolder();
        
        // given that the record is already a child of the record folder
        makeChildrenOf(generateRecordFolder(), record);
        
        // set the name of the record
        String name = generateText(); 
        doReturn(name).when(mockedNodeService).getProperty(record, PROP_NAME);
        
        // link
        recordService.link(record, recordFolder);
        
        // verify link was created
        verify(mockedNodeService, times(1)).addChild(
                    recordFolder, 
                    record, 
                    ContentModel.ASSOC_CONTAINS, 
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name));        
    }
    
    /**
     * Given that the source record has no disposition schedule
     * When I link
     * Then it is successful
     */
    @Test public void linkNoSourceDisposition()    
    {
        // create record and record folder
        NodeRef record = generateRecord();
        NodeRef recordFolder = generateRecordFolder();
        makeChildrenOf(generateRecordFolder(), record);
    
        // set the name of the record
        String name = generateText(); 
        doReturn(name).when(mockedNodeService).getProperty(record, PROP_NAME);
        
        // set dispositions
        when(mockedDispositionService.getDispositionSchedule(record))
            .thenReturn(null);
        
        // link
        recordService.link(record, recordFolder);
        
        // verify link was created
        verify(mockedNodeService, times(1)).addChild(
                    recordFolder, 
                    record, 
                    ContentModel.ASSOC_CONTAINS, 
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name));          
    }
    
    /**
     * Given that the target record folder has no disposition schedule
     * When I link
     * Then it is successful
     */
    @Test public void linkNoTargetDisposition()
    {
        // create record and record folder
        NodeRef record = generateRecord();
        NodeRef recordFolder = generateRecordFolder();
        makeChildrenOf(generateRecordFolder(), record);
    
        // set the name of the record
        String name = generateText(); 
        doReturn(name).when(mockedNodeService).getProperty(record, PROP_NAME);
        
        // set dispositions
        when(mockedDispositionService.getDispositionSchedule(record))
            .thenReturn(mock(DispositionSchedule.class));
        when(mockedDispositionService.getDispositionSchedule(record))
            .thenReturn(null);
        
        // link
        recordService.link(record, recordFolder);
        
        // verify link was created
        verify(mockedNodeService, times(1)).addChild(
                    recordFolder, 
                    record, 
                    ContentModel.ASSOC_CONTAINS, 
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name));         
    }
    
    /**
     * Given that the source record and target record folder have incompatible disposition schedules
     * When I link
     * Then I expect a failure
     */
    @Test public void linkIncompatibleDispositions()
    {
        // create record and record folder
        NodeRef record = generateRecord();
        NodeRef recordFolder = generateRecordFolder();
        makeChildrenOf(generateRecordFolder(), record);
    
        // set the name of the record
        String name = generateText(); 
        doReturn(name).when(mockedNodeService).getProperty(record, PROP_NAME);
        
        // set dispositions
        DispositionSchedule recordDispositionSchedule = mock(DispositionSchedule.class);
        when(recordDispositionSchedule.isRecordLevelDisposition())
            .thenReturn(true);
        when(mockedDispositionService.getOriginDispositionSchedule(record))
            .thenReturn(recordDispositionSchedule);
        
        DispositionSchedule recordFolderDispositionSchedule = mock(DispositionSchedule.class);
        when(recordFolderDispositionSchedule.isRecordLevelDisposition())
            .thenReturn(false);
        when(mockedDispositionService.getDispositionSchedule(recordFolder))
            .thenReturn(recordFolderDispositionSchedule);
        
        // expect exception
        exception.expect(RecordLinkRuntimeException.class);
        
        // link
        recordService.link(record, recordFolder);
    }
    
    /**
     * Given that the source record and target record folder have compatible disposition schedules
     * When I link
     * Then it is successful
     */
    @Test public void linkCompatibleDispositions()
    {
        // create record and record folder
        NodeRef record = generateRecord();
        NodeRef recordFolder = generateRecordFolder();
        makeChildrenOf(generateRecordFolder(), record);
    
        // set the name of the record
        String name = generateText(); 
        doReturn(name).when(mockedNodeService).getProperty(record, PROP_NAME);
        
        // set dispositions
        DispositionSchedule recordDispositionSchedule = mock(DispositionSchedule.class);
        when(recordDispositionSchedule.isRecordLevelDisposition())
            .thenReturn(true);
        when(mockedDispositionService.getDispositionSchedule(record))
            .thenReturn(recordDispositionSchedule);
        
        DispositionSchedule recordFolderDispositionSchedule = mock(DispositionSchedule.class);
        when(recordFolderDispositionSchedule.isRecordLevelDisposition())
            .thenReturn(true);
        when(mockedDispositionService.getDispositionSchedule(recordFolder))
            .thenReturn(recordFolderDispositionSchedule);
        
        // link
        recordService.link(record, recordFolder);  
        
        // verify link was created
        verify(mockedNodeService, times(1)).addChild(
                    recordFolder, 
                    record, 
                    ContentModel.ASSOC_CONTAINS, 
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name));       
    }
    
    /**
     * Given invalid types
     * When unlinking 
     * Then exception thrown
     */
    @Test
    public void unlinkNonRecord()
    {
        NodeRef nonRecord = generateNodeRef(TYPE_CONTENT);
        NodeRef recordFolder = generateRecordFolder();
        
        // set expected exception
        exception.expect(RecordLinkRuntimeException.class);
        
        // unlink
        recordService.unlink(nonRecord, recordFolder);        
    }    
    @Test
    public void unlinkNonRecordFolder()
    {
        NodeRef record = generateRecord();
        NodeRef nonRecordFolder = generateNodeRef(TYPE_FOLDER);
        
        // set expected exception
        exception.expect(RecordLinkRuntimeException.class);
        
        // unlink
        recordService.unlink(record, nonRecordFolder);
    }
    
    /**
     * Given a record folder is a records primary parent
     * When I try and unlink the record from that record folder
     * Then an exception is thrown
     */
    @Test
    public void unlinkRecordFromPrimaryRecordFolder()
    {
        NodeRef record = generateRecord();
        NodeRef recordFolder = generateRecordFolder();
        
        // given that the record is already a child of the record folder        
        makePrimaryParentOf(record, recordFolder);
        
        // set expected exception
        exception.expect(RecordLinkRuntimeException.class);
        
        // link
        recordService.unlink(record, recordFolder);
    }
    
    /**
     * Given a record that is linked to a record
     * And that the record is not the primary parent of the record
     * When I unlink the record to the record folder
     * Then the record is no longer linked to the record folder
     */
    @Test
    public void unlinkRecordFromRecordFolder()
    {
        NodeRef record = generateRecord();
        NodeRef recordFolder = generateRecordFolder();
        
        // the records primary parent is another record folder
        makePrimaryParentOf(record, generateRecordFolder());
        
        // unlink
        recordService.unlink(record, recordFolder);
        
        // verify link was created
        verify(mockedNodeService, times(1)).removeChild(recordFolder, record);       
    }   
    
    /**
     * Given that a new record is being created
     * When the behaviour is triggered
     * Then the record is stored for later reference in the transaction
     */
    @SuppressWarnings("unchecked")
    @Test
    public void onCreateChildAssociationNewRecord()
    {
        // standard content node
        NodeRef nodeRef = generateCmContent("test.txt");
        ChildAssociationRef assoc = generateChildAssociationRef(generateNodeRef(), nodeRef);
        
        doNothing().when(recordService).file(nodeRef);
        
        // doesn't have no content aspect
        when(mockedNodeService.hasAspect(nodeRef, ContentModel.ASPECT_NO_CONTENT))
            .thenReturn(false);
        
        Set<Object> values = mock(HashSet.class);
        when(mockedTransactionalResourceHelper.getSet(RecordServiceImpl.KEY_NEW_RECORDS))
            .thenReturn(values);
        
        // trigger behaviour
        recordService.onCreateChildAssociation(assoc, true);
        
        // verify
        verify(values, times(1)).add(nodeRef);
    }
    
    /**
     * Given that an existing record is linked
     * When the behaviour is triggered
     * Then the record is not stored for later reference in the transaction
     */
    @SuppressWarnings("unchecked")
    @Test
    public void onCreateChildAssociationExistingRecord()
    {
        // standard content node
        NodeRef nodeRef = generateCmContent("test.txt");
        ChildAssociationRef assoc = generateChildAssociationRef(generateNodeRef(), nodeRef);

        doNothing().when(recordService).file(nodeRef);
        
        // doesn't have no content aspect
        when(mockedNodeService.hasAspect(nodeRef, ContentModel.ASPECT_NO_CONTENT))
            .thenReturn(false);
        
        Set<Object> values = mock(HashSet.class);
        when(mockedTransactionalResourceHelper.getSet(RecordServiceImpl.KEY_NEW_RECORDS))
            .thenReturn(values);
        
        // trigger behaviour
        recordService.onCreateChildAssociation(assoc, false);
        
        // verify
        verify(values, never()).add(nodeRef);        
    }

    /**
     * Given a file that is not yet a record
     * When I create the record without specifying a location
     * Then the record is created in the unfiled record container
     */
    @Test
    public void createRecordIntoUnfiledRecordContainer()
    {
        mocksForRecordCreation();

        // create the record
        recordService.createRecord(filePlan, dmNodeRef);

        // verify record was created in unfiled record container
        verify(mockedNodeService, times(1)).moveNode(
                dmNodeRef,
                unfiledRecordContainer,
                ContentModel.ASSOC_CONTAINS,
                parentAssoc.getQName());
    }

    /**
     * Given a file that is not yet a record
     * And the unfiled record container can't be found
     * When I create the record without specifying a location
     * Then an exception is thrown
     */
    @Test (expected = AlfrescoRuntimeException.class)
    public void createRecordWhenUnfiledRecordContainerIsNull()
    {
        mocksForRecordCreation();
        when(mockedFilePlanService.getUnfiledContainer(filePlan)).thenReturn(null);

        // create the record
        recordService.createRecord(filePlan, dmNodeRef);
    }

    /**
     * Given a file that is not yet a record
     * When I create the record specifying the unfiled record container
     * Then the record is created in the unfiled record container
     */
    @Test
    public void createRecordIntoSpecifiedUnfiledRecordContainer()
    {
        mocksForRecordCreation();

        // create the record
        recordService.createRecord(filePlan, dmNodeRef, unfiledRecordContainer);

        // verify record was created in specified unfiled record container
        verify(mockedNodeService, times(1)).moveNode(
                dmNodeRef,
                unfiledRecordContainer,
                ContentModel.ASSOC_CONTAINS,
                parentAssoc.getQName());
    }

    /**
     * Given a file that is not yet a record
     * When I create the record specifying a location
     * Then the record is created in the specified record folder
     */
    @Test
    public void createRecordIntoSpecifiedRecordFolder()
    {
        mocksForRecordCreation();

        // create the record
        recordService.createRecord(filePlan, dmNodeRef, recordFolder);

        // verify record was created in specified record folder
        verify(mockedNodeService, times(1)).moveNode(
                dmNodeRef,
                recordFolder,
                ContentModel.ASSOC_CONTAINS,
                parentAssoc.getQName());
    }

    /**
     * Given a file that is not yet a record
     * When I create the record specifying an invalid location
     * Then an exception is thrown
     */
    @Test(expected=AlfrescoRuntimeException.class)
    public void createRecordIntoInvalidRecordFolder()
    {
        mocksForRecordCreation();
        NodeRef recordCategory = generateNodeRef(TYPE_RECORD_CATEGORY);

        // create the record
        recordService.createRecord(filePlan, dmNodeRef, recordCategory);
    }

    /**
     * Given a file that is not yet a record
     * When I create the record specifying a location where I don't have permissions
     * Then an exception is thrown
     */
    @Test (expected = AccessDeniedException.class)
    public void createRecordIntoSpecifiedRecordFolderWithoutFilePermission()
    {
        mocksForRecordCreation();
        when(mockedExtendedPermissionService.hasPermission(recordFolder, RMPermissionModel.FILING)).thenReturn(AccessStatus.DENIED);

        // create the record
        recordService.createRecord(filePlan, dmNodeRef, recordFolder);
    }

    /**
     * Given a file that is not yet a record
     * When I create the record specifying a folder which is in a hold
     * Then an exception is thrown
     */
    @Test(expected= IntegrityException.class)
    public void createRecordIntoRecordFolderInHold()
    {
        mocksForRecordCreation();

        // create the record
        recordService.createRecord(filePlan, dmNodeRef, frozenRecordFolder);
    }

    /**
     * Given a file that is not yet a record
     * When I create the record specifying a closed destination record folder
     * Then an exception is thrown
     */
    @Test(expected= IntegrityException.class)
    public void createRecordIntoClosedRecordFolder()
    {
        mocksForRecordCreation();

        // create the record
        recordService.createRecord(filePlan, dmNodeRef, closedRecordFolder);
    }

    /* Helper method to set up the mocks for record creation */
    private void mocksForRecordCreation()
    {
        when(mockedNodeService.getPrimaryParent(dmNodeRef))
                .thenReturn(parentAssoc);
        when(parentAssoc.getQName()).thenReturn(generateQName());

        // mocks for sanity checks on node, folder and fileplan
        when(mockedExtendedPermissionService.hasPermission(dmNodeRef, PermissionService.WRITE)).thenReturn(AccessStatus.ALLOWED);
        when(mockedDictionaryService.isSubClass(mockedNodeService.getType(dmNodeRef), ContentModel.TYPE_CONTENT)).thenReturn(true);
        when(mockedFilePlanService.isFilePlan(nonStandardFilePlan)).thenReturn(true);
        when(mockedFreezeService.isFrozen(recordFolder)).thenReturn(false);
        when(mockedFreezeService.isFrozen(frozenRecordFolder)).thenReturn(true);
        when(mockedNodeService.getProperty(closedRecordFolder, PROP_IS_CLOSED)).thenReturn(true);

        // mocks for policies
        doNothing().when(recordService).invokeBeforeRecordDeclaration(dmNodeRef);
        doNothing().when(recordService).invokeOnRecordDeclaration(dmNodeRef);

        when(mockedFilePlanService.getUnfiledContainer(filePlan)).thenReturn(unfiledRecordContainer);

        when(mockedVersionService.getVersionHistory(dmNodeRef)).thenReturn(null);
    }

    /**
     * Setup the mocks for the reject record scenarios
     * @param mockProperties the mock for the node properties
     */
    private void setUpReject(Map mockProperties)
    {
        when(mockedNodeService.getProperties(dmNodeRef)).thenReturn(mockProperties);
        when(mockedVersionService.getVersionHistory(dmNodeRef)).thenReturn(null);
        when(mockProperties.get(PROP_IDENTIFIER)).thenReturn(recordId);
        when(mockProperties.get(PROP_ORIGIONAL_NAME)).thenReturn(originalName);
        when(mockProperties.get(PROP_RECORD_ORIGINATING_LOCATION)).thenReturn(originatingLocation);
        List<ChildAssociationRef> assocs = new ArrayList<>();
        assocs.add(mockedChildAssoc);
        assocs.add(mockedLinkAssoc);
        when(mockedNodeService.getParentAssocs(dmNodeRef)).thenReturn(assocs);
        when(mockedChildAssoc.getParentRef()).thenReturn(originatingLocation);
        when(mockedLinkAssoc.getParentRef()).thenReturn(link);
        when(mockedNodeService.getType(link)).thenReturn(TYPE_RECORD_FOLDER);
        when(mockedNodeService.getPrimaryParent(dmNodeRef)).thenReturn(mockedParentAssoc);
        when(mockedParentAssoc.getQName()).thenReturn(ContentModel.TYPE_CATEGORY);
        doNothing().when(recordService).invokeBeforeRecordRejection(dmNodeRef);
        doNothing().when(recordService).invokeOnRecordRejection(dmNodeRef);
    }
    /**
     * Test for the reject record method
     */
    @Test
    public void testRejectRecord()
    {
        Map mockProperties = mock(HashMap.class);
        setUpReject(mockProperties);
        when(mockProperties.get(PROP_RECORD_ORIGINATING_USER_ID)).thenReturn(documentOwner);
        recordService.rejectRecord(dmNodeRef, "Just because..");
        verify(mockedNodeService, times(1)).removeChildAssociation(mockedChildAssoc);
        verify(mockedNodeService, times(1)).removeChildAssociation(mockedLinkAssoc);
    }

    /**
     * Test for the reject record method throws an error without document owner
     */
    @Test (expected = AlfrescoRuntimeException.class)
    public void testRejectRecordThrowsErrorWithoutDocumentOwner()
    {
        Map mockProperties = mock(HashMap.class);
        setUpReject(mockProperties);
        when(mockProperties.get(PROP_RECORD_ORIGINATING_USER_ID)).thenReturn(null);
        recordService.rejectRecord(dmNodeRef, "Just because..");
    }
}
