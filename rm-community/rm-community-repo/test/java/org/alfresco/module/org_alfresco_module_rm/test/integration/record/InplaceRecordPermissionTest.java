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

package org.alfresco.module.org_alfresco_module_rm.test.integration.record;

import static org.alfresco.module.org_alfresco_module_rm.test.util.bdt.BehaviourTest.test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.module.org_alfresco_module_rm.action.impl.DeclareRecordAction;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.bdt.BehaviourTest;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * In-place record permission integration test.
 *
 * @author Roy Wetherall
 * @since 2.5
 */
public class InplaceRecordPermissionTest extends BaseRMTestCase
{    
    /** capability list */
    private static final List<String> CAPABILITIES = Stream
        .of(RMPermissionModel.VIEW_RECORDS, 
            RMPermissionModel.EDIT_NON_RECORD_METADATA,
            RMPermissionModel.EDIT_RECORD_METADATA)
        .collect(Collectors.toList());
    
    /** capabilities */
    private Capability viewRecordsCapability;
    private Capability editNonRecordMetadataCapability;
    private Capability editRecordMetadataCapability;
    
    /** test characteristics */
    @Override protected boolean isCollaborationSiteTest() { return true; }    
    @Override protected boolean isUserTest()              { return true; }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#initServices()
     */
    @Override
    protected void initServices()
    {
        super.initServices();
        
        // initialise behaviour tests
        BehaviourTest.initBehaviourTests(retryingTransactionHelper);
        
        // get capability references
        viewRecordsCapability = capabilityService.getCapability(RMPermissionModel.VIEW_RECORDS);
        editNonRecordMetadataCapability = capabilityService.getCapability(RMPermissionModel.EDIT_NON_RECORD_METADATA);
        editRecordMetadataCapability = capabilityService.getCapability(RMPermissionModel.EDIT_RECORD_METADATA);
    }
    
    /** 
     * Given a document in a collaboration site
     * When a user without write permissions on the document tries to declare it as a record
     * Then the declaration fails
     * And the document does not become a record
     */
    public void testUserWithOutWriteCantDeclareInPlaceRecord()
    {
        test()
            .given()
            
                // Given a document in a collaboration site that is not a record            
                .expect(false)
                    .from(() -> recordService.isRecord(dmDocument))
                    .because("The document is not a record.")
            
                // And a user with no write permission on the document
                .as(userName)
                    .expect(AccessStatus.DENIED.toString())
                        .from(() -> permissionService.hasPermission(dmDocument, PermissionService.WRITE).toString())
                        .because("User does not have write access to document.")
            
            // When the user tries to declare the record
            // Then we expect this to fail
            .when()
                .as(userName)
                    .expectException(AccessDeniedException.class)
                        .from(() -> recordService.createRecord(filePlan, dmDocument))
                        .because("The user does not have write permission on the document.");
    }
    

    /**
     * Given a document in a collaboration site is not a record
     * When the document is declared by a site collaborator
     * Then the document becomes a record
     * And the site users have the appropriate in-place permissions on the record
     */
    public void testCreateInplaceRecordFromCollabSite()
    {
        test()
        
            // Given that a document in a collaboration site is not a record
            .given()                
                .asAdmin()
                    .expect(false)
                        .from(() -> recordService.isRecord(dmDocument))
                        .because("The document is not a record")
             
            // When it is declared as an inplace record            
            .when()        
                .as(dmCollaborator)
                    .perform(() -> recordService.createRecord(filePlan, dmDocument))
            
            .then()        
                .asAdmin()
                    // Then it becomes a record
                    .expect(true)
                        .from(() -> recordService.isRecord(dmDocument))
                        .because("The document is a record")
                    
                    // And it isn't filed
                    .expect(false)
                        .from(() -> recordService.isFiled(dmDocument))
                        .because("The record is not filed")
                
                // And a site collaborator has filling permissions and filling capability on the record     
                .as(dmCollaborator)
                    .perform(() ->               
                        checkInPlaceAccess(dmDocument, 
                                AccessStatus.ALLOWED,  // read record permission 
                                AccessStatus.ALLOWED,  // filing permission
                                AccessStatus.ALLOWED,  // view record capability 
                                AccessStatus.ALLOWED,  // edit non record metadata capability
                                AccessStatus.DENIED))  // edit record metadata capability
                
                // And a site consumer has read permissions and view record capability on the record
                .as(dmConsumer)
                    .perform(() -> 
                        checkInPlaceAccess(dmDocument, 
                                AccessStatus.ALLOWED,  // read record permission 
                                AccessStatus.DENIED,   // filing permission
                                AccessStatus.ALLOWED,  // view record capability 
                                AccessStatus.DENIED,   // edit non record metadata capability
                                AccessStatus.DENIED))  // edit record metadata capability   
            
                // And a user that is not a member of the site has no access to the inplace record     
                .as(userName)
                    .perform(() ->    
                        checkInPlaceAccess(dmDocument, 
                                AccessStatus.DENIED,   // read record permission 
                                AccessStatus.DENIED,   // filing permission
                                AccessStatus.DENIED,   // view record capability 
                                AccessStatus.DENIED,   // edit non record metadata capability
                                AccessStatus.DENIED));  // edit record metadata capability   
    }
    
    /**
     * Helper method to check in place access for a user on a record.
     */
    private void checkInPlaceAccess(NodeRef nodeRef, AccessStatus ... accessStatus)
    {
        // check permission access
        assertEquals(accessStatus[0], permissionService.hasPermission(nodeRef, RMPermissionModel.READ_RECORDS));
        assertEquals(accessStatus[1], permissionService.hasPermission(nodeRef, RMPermissionModel.FILING));
        
        // check capability access
        Map<Capability, AccessStatus> access = capabilityService.getCapabilitiesAccessState(nodeRef, CAPABILITIES);                        
        assertEquals(accessStatus[2], access.get(viewRecordsCapability));
        assertEquals(accessStatus[3], access.get(editNonRecordMetadataCapability));
        assertEquals(accessStatus[4], access.get(editRecordMetadataCapability));        
    }
    
    /**
     * Given an unfiled in-place record
     * When the record is moved to the file plan (ie filed)
     * Then the site users still have the appropriate in-place permissions on the record
     */
    public void testFileInplaceRecordFromCollabSite() throws Exception
    {
        test()          
        
            // Given an unfiled inplace record
            .given()
                .as(dmCollaborator)
                    .perform(() -> recordService.createRecord(filePlan, dmDocument))                    
                    .expect(true)
                        .from(() -> recordService.isRecord(dmDocument))
                        .because("The document is a record.")                    
                    .expect(false)
                        .from(() -> recordService.isFiled(dmDocument))
                        .because("The record is not filed")            
                        
            // When the record is filed          
            .when()            
                .asAdmin()
                    .perform(() -> fileFolderService.move(dmDocument, rmFolder, null))                                
            
            .then()
            
                // Then the record is filed
                .asAdmin()  
                    .expect(true)
                        .from(() -> recordService.isFiled(dmDocument))
                        .because("The record is filed.")
                
                // And the collaborator has filling permissions and filling capability on the record        
                .as(dmCollaborator)
                    .perform(() -> 
                        checkInPlaceAccess(dmDocument, 
                            AccessStatus.ALLOWED,  // read record permission 
                            AccessStatus.ALLOWED,  // filing permission
                            AccessStatus.ALLOWED,  // view record capability 
                            AccessStatus.ALLOWED,  // edit non record metadata capability
                            AccessStatus.DENIED))  // edit record metadata capability
            
                // And the consumer has read permissions and view record capability on the record       
                .as(dmConsumer)
                    .perform(() -> 
                        checkInPlaceAccess(dmDocument, 
                            AccessStatus.ALLOWED,  // read record permission 
                            AccessStatus.DENIED,   // filing permission
                            AccessStatus.ALLOWED,  // view record capability 
                            AccessStatus.DENIED,   // edit non record metadata capability
                            AccessStatus.DENIED))  // edit record metadata capability     
                
                // And a user that is not in the site has no permissions on the record
                .as(userName)
                    .perform(() -> 
                        checkInPlaceAccess(dmDocument, 
                            AccessStatus.DENIED,   // read record permission 
                            AccessStatus.DENIED,   // filing permission
                            AccessStatus.DENIED,   // view record capability 
                            AccessStatus.DENIED,   // edit non record metadata capability
                            AccessStatus.DENIED));  // edit record metadata capability             
    }
    
    /**
     * Given an incomplete inplace record
     * When it is completed
     * Then the inplace users still have access to the record
     * And can't edit the records meta-data
     */
    public void testCompletedInPlaceRecord()
    {
        test()
        
            // Given an incomplete record
            .given()
                .as(dmCollaborator)
                    .perform(() -> recordService.createRecord(filePlan, dmDocument))
                    .expect(false)
                        .from(() -> recordService.isDeclared(dmDocument))
                        .because("Record is not complete.")
            
            // When it is completed            
            .when()
                .asAdmin()
                    .perform(() -> rmActionService.executeRecordsManagementAction(dmDocument, DeclareRecordAction.NAME))
                    .expect(true)
                        .from(() -> recordService.isDeclared(dmDocument))
                        .because("Record is complete.")            
            
            .then()
            
                // Then the collaborator has filling permissions, view record capability, but not edit non-record metadata        
                .as(dmCollaborator)
                    .perform(() -> 
                        checkInPlaceAccess(dmDocument, 
                            AccessStatus.ALLOWED,  // read record permission 
                            AccessStatus.ALLOWED,  // filing permission
                            AccessStatus.ALLOWED,  // view record capability 
                            AccessStatus.DENIED,  // edit non record metadata capability
                            AccessStatus.DENIED))  // edit record metadata capability
            
                // And the consumer has read permissions and view record capability on the record       
                .as(dmConsumer)
                    .perform(() -> 
                        checkInPlaceAccess(dmDocument, 
                            AccessStatus.ALLOWED,  // read record permission 
                            AccessStatus.DENIED,   // filing permission
                            AccessStatus.ALLOWED,  // view record capability 
                            AccessStatus.DENIED,   // edit non record metadata capability
                            AccessStatus.DENIED))  // edit record metadata capability     
                
                // And a user that is not in the site has no permissions on the record
                .as(userName)
                    .perform(() -> 
                        checkInPlaceAccess(dmDocument, 
                            AccessStatus.DENIED,   // read record permission 
                            AccessStatus.DENIED,   // filing permission
                            AccessStatus.DENIED,   // view record capability 
                            AccessStatus.DENIED,   // edit non record metadata capability
                            AccessStatus.DENIED));  // edit record metadata capability              
    }
    
    /**
     * Given a record
     * When it is destroyed
     * And it's metadata is maintained
     * Then the inplace users still have access to the meta-data stub
     */
    // TODO
    
    /**
     * Given an inplace user with write access
     * When a role is added to the inplace writers role
     * Then then they receive that additional capability on the inplace record
     */
    // TODO
    
    // hide?
    // TODO
    
    // reject?
    // TODO
    
    // user added to group ?
    // TODO
}
