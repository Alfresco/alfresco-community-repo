/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.module.org_alfresco_module_rm.security;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

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
    public void before()
    {
        super.before();
        
        // mock up run as methods
        mockRunAsMethods(filePlanPermissionService);
        
        // initialize node's        
        unfiledRecordContainer = generateContainerNodeRef(TYPE_UNFILED_RECORD_CONTAINER);
        unfiledRecordFolder = generateContainerNodeRef(TYPE_UNFILED_RECORD_FOLDER);
        unfiledRecordFolderChild = generateContainerNodeRef(TYPE_UNFILED_RECORD_FOLDER);        
        unfiledRecord = generateRecord();        
        holdContainer = generateContainerNodeRef(TYPE_HOLD_CONTAINER);
        hold = generateHoldNodeRef("my test hold");
        heldRecord = generateRecord();
        
        // setup parent hierarchy
        makePrimaryParentOf(filePlan, generateNodeRef(ContentModel.TYPE_FOLDER));
        
        makePrimaryParentOf(unfiledRecordFolder, unfiledRecordContainer);
        makePrimaryParentOf(unfiledRecordContainer, filePlan);
        
        makePrimaryParentOf(hold, holdContainer);
        makePrimaryParentOf(holdContainer, filePlan);
        
        // setup child hierarchy
        makeChildrenOf(unfiledRecordFolder, unfiledRecordFolderChild);
        makeChildrenOf(unfiledRecordFolderChild, unfiledRecord);
        
        makeChildrenOf(hold, heldRecord);
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
        
        // verify READ permission set up hierarchy
        verify(mockedPermissionService, times(1)).setPermission(unfiledRecordContainer, AUTHORITY, RMPermissionModel.READ_RECORDS, true);
        verify(mockedPermissionService, times(1)).setPermission(filePlan, AUTHORITY, RMPermissionModel.READ_RECORDS, true);       
        
        // verify READ permission set down hierarchy
        verify(mockedPermissionService, times(1)).setPermission(unfiledRecordFolderChild, AUTHORITY, RMPermissionModel.READ_RECORDS, true);
        verify(mockedPermissionService, times(1)).setPermission(unfiledRecord, AUTHORITY, RMPermissionModel.READ_RECORDS, true);  
        
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
        
        // verify READ permission set up hierarchy
        verify(mockedPermissionService, times(1)).setPermission(unfiledRecordContainer, AUTHORITY, RMPermissionModel.READ_RECORDS, true);
        verify(mockedPermissionService, times(1)).setPermission(filePlan, AUTHORITY, RMPermissionModel.READ_RECORDS, true);       
        
        // verify FILING permission set down hierarchy
        verify(mockedPermissionService, times(1)).setPermission(unfiledRecordFolderChild, AUTHORITY, RMPermissionModel.FILING, true);
        verify(mockedPermissionService, times(1)).setPermission(unfiledRecord, AUTHORITY, RMPermissionModel.FILING, true);         
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
        
        // verify no permissions deleted up the hierarchy
        verify(mockedPermissionService, never()).deletePermission(eq(unfiledRecordContainer), eq(AUTHORITY), anyString());
        verify(mockedPermissionService, never()).deletePermission(eq(filePlan), eq(AUTHORITY), anyString());       
        
        // verify READ permission removed down hierarchy
        verify(mockedPermissionService, times(1)).deletePermission(unfiledRecordFolderChild, AUTHORITY, RMPermissionModel.READ_RECORDS);
        verify(mockedPermissionService, times(1)).deletePermission(unfiledRecord, AUTHORITY, RMPermissionModel.READ_RECORDS); 
    }
    
    /**
     * Set read permission on hold container
     */
    public void setReadPermissionOnHoldContainer()
    {
        // set read permission on hold
        filePlanPermissionService.setPermission(holdContainer, AUTHORITY, RMPermissionModel.READ_RECORDS);
        
        // verify permission set on target node
        verify(mockedPermissionService, times(1)).setPermission(holdContainer, AUTHORITY, RMPermissionModel.READ_RECORDS, true);
        
        // verify READ permission set up hierarchy
        verify(mockedPermissionService, times(1)).setPermission(filePlan, AUTHORITY, RMPermissionModel.READ_RECORDS, true);       
        
        // verify READ permission set on hold
        verify(mockedPermissionService, times(1)).setPermission(hold, AUTHORITY, RMPermissionModel.READ_RECORDS, true);
        
        // verify permission not set on child of hold
        verify(mockedPermissionService, never()).setPermission(eq(heldRecord), eq(AUTHORITY), anyString(), eq(true));   
        
    }
    
    /**
     * Set filing permission on hold container
     */
    public void setFilingPermissionOnHoldContainer()
    {
        // set read permission on hold
        filePlanPermissionService.setPermission(holdContainer, AUTHORITY, RMPermissionModel.FILING);
        
        // verify permission set on target node
        verify(mockedPermissionService, times(1)).setPermission(holdContainer, AUTHORITY, RMPermissionModel.FILING, true);
        
        // verify READ permission set up hierarchy
        verify(mockedPermissionService, times(1)).setPermission(filePlan, AUTHORITY, RMPermissionModel.READ_RECORDS, true);       
        
        // verify FILING permission set on hold
        verify(mockedPermissionService, times(1)).setPermission(hold, AUTHORITY, RMPermissionModel.FILING, true);
        
        // verify permission not set on child of hold
        verify(mockedPermissionService, never()).setPermission(eq(heldRecord), eq(AUTHORITY), anyString(), eq(true));   
        
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
        
        // verify READ permission set up hierarchy
        verify(mockedPermissionService, times(1)).setPermission(holdContainer, AUTHORITY, RMPermissionModel.READ_RECORDS, true);
        verify(mockedPermissionService, times(1)).setPermission(filePlan, AUTHORITY, RMPermissionModel.READ_RECORDS, true);       
        
        // verify permission not set on child of hold
        verify(mockedPermissionService, never()).setPermission(eq(heldRecord), eq(AUTHORITY), anyString(), eq(true));          
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
        
        // verify READ permission set up hierarchy
        verify(mockedPermissionService, times(1)).setPermission(holdContainer, AUTHORITY, RMPermissionModel.READ_RECORDS, true);
        verify(mockedPermissionService, times(1)).setPermission(filePlan, AUTHORITY, RMPermissionModel.READ_RECORDS, true);       
        
        // verify permission not set on child of hold
        verify(mockedPermissionService, never()).setPermission(eq(heldRecord), eq(AUTHORITY), anyString(), eq(true));          
    }

}
