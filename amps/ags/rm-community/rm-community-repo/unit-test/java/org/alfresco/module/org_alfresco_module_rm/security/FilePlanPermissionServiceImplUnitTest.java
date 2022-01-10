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

package org.alfresco.module.org_alfresco_module_rm.security;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.repo.security.permissions.impl.AccessPermissionImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.verification.VerificationMode;

/**
 * File plan permission service implementation unit test.
 * <p>
 * Primarily tests the file plan permission service interaction with the 
 * permission service.
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public class FilePlanPermissionServiceImplUnitTest extends BaseUnitTest
{
    /** test authority */
    protected static final String AUTHORITY = "anAuthority";
    protected static final String AUTHORITY2 = "anOtherAuthority";
    
    /** fileplan nodes */
    protected NodeRef rootRecordCategory;
    protected NodeRef recordCategory;
    protected NodeRef newRecordFolder;
    protected NodeRef newRecord;
    
    /** unfiled nodes */
    protected NodeRef unfiledRecordContainer;
    protected NodeRef unfiledRecordFolder;
    protected NodeRef unfiledRecordFolderChild;
    protected NodeRef unfiledRecord;
    
    /** held nodes */
    protected NodeRef holdContainer;
    protected NodeRef hold;
    protected NodeRef heldRecord;
    
    /** file plan permission service implementation */
    @Spy @InjectMocks FilePlanPermissionServiceImpl filePlanPermissionService;
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest#before()
     */
    @Override
    public void before() throws Exception
    {
        super.before();
        
        // initialize node's        
        unfiledRecordContainer = generateContainerNodeRef(TYPE_UNFILED_RECORD_CONTAINER);
        unfiledRecordFolder = generateContainerNodeRef(TYPE_UNFILED_RECORD_FOLDER);
        unfiledRecordFolderChild = generateContainerNodeRef(TYPE_UNFILED_RECORD_FOLDER);        
        unfiledRecord = generateRecord();        
        holdContainer = generateContainerNodeRef(TYPE_HOLD_CONTAINER);
        hold = generateHoldNodeRef("my test hold");
        heldRecord = generateRecord();
        rootRecordCategory = generateContainerNodeRef(TYPE_RECORD_CATEGORY);
        recordCategory = generateContainerNodeRef(TYPE_RECORD_CATEGORY);
        newRecordFolder = generateRecordFolder();
        newRecord = generateRecord();
        
        // setup parent hierarchy
        makePrimaryParentOf(filePlan, generateNodeRef(ContentModel.TYPE_FOLDER));  
        
        makePrimaryParentOf(rootRecordCategory, filePlan);
        makePrimaryParentOf(recordCategory, rootRecordCategory);
        makePrimaryParentOf(newRecordFolder, recordCategory);
        makePrimaryParentOf(newRecord, newRecordFolder);
        
        makePrimaryParentOf(unfiledRecordFolder, unfiledRecordContainer);
        makePrimaryParentOf(unfiledRecordContainer, filePlan);        
        
        makePrimaryParentOf(hold, holdContainer);
        makePrimaryParentOf(holdContainer, filePlan);
        
        
        // setup child hierarchy
        makeChildrenOf(filePlan, rootRecordCategory);
        makeChildrenOf(rootRecordCategory, recordCategory);
        makeChildrenOf(recordCategory, newRecordFolder);
        makeChildrenOf(newRecordFolder, newRecord);
        
        makeChildrenOf(unfiledRecordFolder, unfiledRecordFolderChild);
        makeChildrenOf(unfiledRecordFolderChild, unfiledRecord);        
        
        makeChildrenOf(holdContainer, hold);
        makeChildrenOf(hold, heldRecord);
        
        doReturn(FilePlanComponentKind.FILE_PLAN).when(filePlanPermissionService).getFilePlanComponentKind(filePlan);
        doReturn(FilePlanComponentKind.RECORD_CATEGORY).when(filePlanPermissionService).getFilePlanComponentKind(rootRecordCategory);
        doReturn(FilePlanComponentKind.RECORD_CATEGORY).when(filePlanPermissionService).getFilePlanComponentKind(recordCategory);
        doReturn(FilePlanComponentKind.RECORD_FOLDER).when(filePlanPermissionService).getFilePlanComponentKind(newRecordFolder);
        doReturn(FilePlanComponentKind.RECORD).when(filePlanPermissionService).getFilePlanComponentKind(newRecord);
        doReturn(FilePlanComponentKind.UNFILED_RECORD_FOLDER).when(filePlanPermissionService).getFilePlanComponentKind(unfiledRecordFolder);
        doReturn(FilePlanComponentKind.UNFILED_RECORD_CONTAINER).when(filePlanPermissionService).getFilePlanComponentKind(unfiledRecordContainer);
        doReturn(FilePlanComponentKind.RECORD).when(filePlanPermissionService).getFilePlanComponentKind(unfiledRecord);
        doReturn(FilePlanComponentKind.HOLD_CONTAINER).when(filePlanPermissionService).getFilePlanComponentKind(holdContainer);
        doReturn(FilePlanComponentKind.HOLD).when(filePlanPermissionService).getFilePlanComponentKind(hold);
    }
    
    /**
     * Helper method to generate a container node ref of a perticular type.
     * 
     * @param type  type of node reference
     * @return {@link NodeRef}  node reference that behaves like a container of the type given.
     */
    private NodeRef generateContainerNodeRef(QName type)
    {
        NodeRef nodeRef = generateNodeRef(type);
        setupAsFilePlanComponent(nodeRef);
        doReturn(true).when(filePlanPermissionService).isFilePlanContainer(nodeRef);
        return nodeRef;
    }
    
    /**
     * Set read permission on unfiled record folder.
     */
    @Test
    public void setReadPermissionOnUnfiledRecordFolder()
    {
        // set read permission on unfiled record folder
        filePlanPermissionService.setPermission(unfiledRecordFolder, AUTHORITY, RMPermissionModel.READ_RECORDS);
        
        // verify permission set on target node
        verify(mockedPermissionService, times(1)).setPermission(unfiledRecordFolder, AUTHORITY, RMPermissionModel.READ_RECORDS, true);        
    }
    
    /**
     * Set filling permission on unfiled record folder
     */
    @Test
    public void setReadAndFilePermissionOnUnfileRecordFolder()
    {
        // set read permission on unfiled record folder
        filePlanPermissionService.setPermission(unfiledRecordFolder, AUTHORITY, RMPermissionModel.FILING);
        
        // verify permission set on target node
        verify(mockedPermissionService, times(1)).setPermission(unfiledRecordFolder, AUTHORITY, RMPermissionModel.FILING, true);        
    }
    
    /**
     * Remove permission from unfiled record folders.
     */
    @Test
    public void deletePermissionFromUnfiledRecordFolder()
    {
        // delete read permission from unfiled record folder
        filePlanPermissionService.deletePermission(unfiledRecordFolder, AUTHORITY, RMPermissionModel.READ_RECORDS);
        
        // verify permission deleted on target node
        verify(mockedPermissionService, times(1)).deletePermission(unfiledRecordFolder, AUTHORITY, RMPermissionModel.READ_RECORDS);
    }
    
    /**
     * Set read permission on hold container
     */
    @Test
    public void setReadPermissionOnHoldContainer()
    {
        // set read permission on hold
        filePlanPermissionService.setPermission(holdContainer, AUTHORITY, RMPermissionModel.READ_RECORDS);
        
        // verify permission set on target node
        verify(mockedPermissionService, times(1)).setPermission(holdContainer, AUTHORITY, RMPermissionModel.READ_RECORDS, true);
    }
    
    /**
     * Set filing permission on hold container
     */
    @Test
    public void setFilingPermissionOnHoldContainer()
    {
        // set read permission on hold
        filePlanPermissionService.setPermission(holdContainer, AUTHORITY, RMPermissionModel.FILING);
        
        // verify permission set on target node
        verify(mockedPermissionService, times(1)).setPermission(holdContainer, AUTHORITY, RMPermissionModel.FILING, true);       
    }
    
    /**
     * Set read permission on hold.
     */
    @Test
    public void setReadPermissionOnHold()
    {
        // set read permission on hold
        filePlanPermissionService.setPermission(hold, AUTHORITY, RMPermissionModel.READ_RECORDS);
        
        // verify permission set on target node
        verify(mockedPermissionService, times(1)).setPermission(hold, AUTHORITY, RMPermissionModel.READ_RECORDS, true);
    }
    
    /**
     * Set filing permission on hold.
     */
    @Test
    public void setFilingPermissionOnHold()
    {
        // set filing permission on hold
        filePlanPermissionService.setPermission(hold, AUTHORITY, RMPermissionModel.FILING);
        
        // verify permission set on target node
        verify(mockedPermissionService, times(1)).setPermission(hold, AUTHORITY, RMPermissionModel.FILING, true);         
    }
    
    /**
     * Helper method to setup permissions on mock objects
     */
    private void setupPermissions(NodeRef nodeRef)
    {
        Set<AccessPermission> perms = new HashSet<>(4);
        
        // setup basic file and read for authorities
        perms.add(new AccessPermissionImpl(RMPermissionModel.READ_RECORDS, AccessStatus.ALLOWED, AUTHORITY, 0));
        perms.add(new AccessPermissionImpl(RMPermissionModel.FILING, AccessStatus.ALLOWED, AUTHORITY2, 1));

        doReturn(perms).when(mockedPermissionService).getAllSetPermissions(nodeRef);
    }
    
    /**
     * Helper to verify the core permissions have been initialized correctly
     */
    private void verifyInitPermissions(NodeRef nodeRef, boolean isInherited)
    {
        verify(mockedPermissionService).getAllSetPermissions(nodeRef);
        verify(mockedPermissionService).setInheritParentPermissions(nodeRef, isInherited);
        verify(mockedPermissionService).clearPermission(nodeRef, null);
        verify(mockedOwnableService).setOwner(nodeRef, OwnableService.NO_OWNER);
    }
    
    /**
     * Helper to verify that permissions have been set correctly on the child
     * 
     * @param parent        parent node 
     * @param child         child node
     * @param read          verification mode relating to setting read on the child
     * @param filling       verification mode relating to setting filling on the child
     */
    private void verifyInitPermissions(NodeRef parent, NodeRef child, VerificationMode read, VerificationMode filling, boolean isParentFilePlan, boolean isInherited)
    {
        // verify the core permissions are set-up correctly
        verifyInitPermissions(child, isInherited);        
        
        if (isParentFilePlan)
        {
            // verify the permissions came from the correct parent
            verify(mockedPermissionService).getAllSetPermissions(parent);
            
            // verify all the inherited permissions are set correctly (note read are not inherited from fileplan)
            verify(mockedPermissionService, filling).setPermission(child, AUTHORITY2, RMPermissionModel.FILING, true);
            verify(mockedPermissionService, read).setPermission(child, AUTHORITY, RMPermissionModel.READ_RECORDS, true);
        }       
    }
    
    /**
     * Test the initialisation of permissions for a new root category
     */
    @Test
    public void initPermissionsForNewRootRecordCategory()
    {
        // setup permissions for file plan
        setupPermissions(filePlan);
        
        // setup permissions 
        filePlanPermissionService.setupRecordCategoryPermissions(rootRecordCategory);
        
        // verify permission init
        verifyInitPermissions(filePlan, rootRecordCategory, never(), times(1), true, false);
    }
    
    /**
     * Test the initialisation of permissions for a new category
     */
    @Test
    public void initPermissionsForNewRecordCategory()
    {
        // setup permissions for parent
        setupPermissions(rootRecordCategory);
        
        // setup permissions
        filePlanPermissionService.setupRecordCategoryPermissions(recordCategory);
                
        // verify permission init
        verifyInitPermissions(rootRecordCategory, recordCategory, times(1), times(1), false, true);
    }
    
    /**
     * Test initialisation new record folder permissions
     */
    @Test
    public void initPermissionsForNewRecordFolder()
    {
        // setup permissions for parent 
        setupPermissions(recordCategory);
        
        // setup permissions 
        filePlanPermissionService.setupPermissions(recordCategory, newRecordFolder);
        
        // verify permission init
        verifyInitPermissions(recordCategory, newRecordFolder, times(1), times(1), false, true);
        
    }
    
    /**
     * Test setup of new record permissions
     */
    @Test
    public void initPermissionsForNewRecord()
    {
        // setup permissions for parent 
        setupPermissions(newRecordFolder);
        
        // setup permissions for record
        filePlanPermissionService.setupPermissions(newRecordFolder, newRecord);
        
        // verify permission init
        verifyInitPermissions(newRecordFolder, newRecord, times(1), times(1), false, true);        
    }
    
    /**
     * Test setup of permissions for new hold container
     */
    @Test
    public void initPermnissionsForNewHoldContainer()
    {
        // setup permissions for parent 
        setupPermissions(filePlan);
        
        // setup permissions for record
        filePlanPermissionService.setupPermissions(filePlan, holdContainer);
        
        // verify permissions are set-up correctly
        verifyInitPermissions(filePlan, holdContainer, times(1), times(1), false, true);        
    }
    
    /**
     * Test setup of permissions for new hold
     */
    @Test
    public void initPermissionsForNewHold()
    {
        // setup permissions for parent 
        setupPermissions(holdContainer);
        
        // setup permissions for record
        filePlanPermissionService.setupPermissions(holdContainer, hold);
        
        // verify permissions are set-up correctly
        verifyInitPermissions(holdContainer, hold, never(), times(1), false, false);    
        
    }
    
    /**
     * Test setup of permissions for new unfiled container
     */
    @Test
    public void initPermissionsForNewUnfiledContainer()
    {
        // setup permissions for parent 
        setupPermissions(filePlan);
        
        // setup permissions for record
        filePlanPermissionService.setupPermissions(filePlan, unfiledRecordContainer);
        
        // verify permissions are set-up correctly
        verifyInitPermissions(filePlan, unfiledRecordContainer, times(1), times(1), false, false);          
    }
    
    /**
     * Test setup of permissions for new unfiled record folder
     */
    @Test
    public void initPermissionsForNewUnfiledRecordFolder()
    {
        // setup permissions for parent 
        setupPermissions(unfiledRecordContainer);
        
        // setup permissions for record
        filePlanPermissionService.setupPermissions(unfiledRecordContainer, unfiledRecordFolder);
        
        // verify permissions are set-up correctly
        verifyInitPermissions(unfiledRecordContainer, unfiledRecordFolder, never(), times(1), false, true);  
        
    }
    
    /**
     * Test setup of permissions for new unfiled record
     */
    @Test
    public void initPermissionsForNewUnfiledRecord()
    {
        // setup permissions for parent 
        setupPermissions(unfiledRecordFolder);
        
        // setup permissions for record
        filePlanPermissionService.setupPermissions(unfiledRecordFolder, unfiledRecord);
        
        // verify permission init
        verifyInitPermissions(unfiledRecordFolder, unfiledRecord, times(1), times(1), false, true);  
        
    }
}
