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

package org.alfresco.module.org_alfresco_module_rm.test.integration.record;

import static org.alfresco.module.org_alfresco_module_rm.test.util.bdt.BehaviourTest.test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CutOffAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.DeclareRecordAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.DestroyAction;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.module.org_alfresco_module_rm.test.util.bdt.BehaviourTest;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.GUID;

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
    
    /** test data */
    private NodeRef contribDoc;    
    private NodeRef deleteUserDoc;
    private NodeRef copiedDoc;
    private NodeRef copyDoc;
    private String deletedUser;
    
    /** services */
    private NodeService dbNodeService;
    
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
        
        // get services
        dbNodeService = (NodeService)applicationContext.getBean("dbNodeService");
        
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
     * Given a document in a collaboration site that is not a record
     * And a contributor the didn't create the document
     * When the contributor tries to declare the document as a record
     * Then the document does not become a record
     */
    public void testContributorThatIsntOwnerDeclareInPlaceRecord()
    {
        test()
            .given()
            
                // Given a document in a collaboration site that is not a record            
                .expect(false)
                    .from(() -> recordService.isRecord(dmDocument))
                    .because("The document is not a record.")
            
                // And a contributor the didn't create the document
                .as(dmContributor)
                    .expect(AccessStatus.DENIED.toString())
                        .from(() -> permissionService.hasPermission(dmDocument, PermissionService.WRITE).toString())
                        .because("Contributor does not have write access to document.")
            
            // When the user tries to declare the record
            // When the contributor tries to declare the document as a record
            .when()
                .as(dmContributor)
                    .expectException(AccessDeniedException.class)
                        .from(() -> recordService.createRecord(filePlan, dmDocument))
                        .because("The contributor does not have write permission on the document.");        
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
                
                // And a site contributor has read and view    
                .as(dmContributor)
                    .perform(() ->               
                        checkInPlaceAccess(dmDocument, 
                                AccessStatus.ALLOWED,  // read record permission 
                                AccessStatus.DENIED,  // filing permission
                                AccessStatus.ALLOWED,  // view record capability 
                                AccessStatus.DENIED,  // edit non record metadata capability
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
        assertEquals("Incorrect read record permission access.", accessStatus[0], permissionService.hasPermission(nodeRef, RMPermissionModel.READ_RECORDS));
        assertEquals("Incorrect filling permission access.", accessStatus[1], permissionService.hasPermission(nodeRef, RMPermissionModel.FILING));
        
        // check capability access
        Map<Capability, AccessStatus> access = capabilityService.getCapabilitiesAccessState(nodeRef, CAPABILITIES);                        
        assertEquals("Incorrect view records capability access", accessStatus[2], access.get(viewRecordsCapability));
        assertEquals("Incorrect edit non record metadata capability access", accessStatus[3], access.get(editNonRecordMetadataCapability));
        assertEquals("Incorrect edit record metadata capability access", accessStatus[4], access.get(editRecordMetadataCapability));        
    }
    
    /**
     * Given that a document is created by contributor 
     * When it is declared as an inplace record
     * Then it becomes a record 
     * And it isn't filed
     * And a site collaborator has filling permissions and filling capability on the record
     * And a site contributor has filling capability and permissions
     * And a site consumer has read permissions and view record capability on the record
     * And a user that is not a member of the site has no access to the inplace record  
     */
    public void testCreateInplaceRecordFromCollabSiteWhenContribIsCreatorOfDocument()
    {
        test()
        
            // Given that a document is created by contributor        
            .given()                
                .as(dmContributor)
                    .perform(() -> 
                    {   
                        contribDoc = fileFolderService.create(dmFolder, "contrib.txt" , ContentModel.TYPE_CONTENT).getNodeRef();
                        dbNodeService.addAspect(contribDoc, ContentModel.ASPECT_AUDITABLE, null);
                    })
                    .expect(false)
                        .from(() -> recordService.isRecord(contribDoc))
                        .because("It is not a record.")
                .asAdmin()
                    .expect(dmContributor)
                        .from(() -> ownableService.getOwner(contribDoc))
                        .because("As the creator of the document the contributor is also the owner")
                .as(dmContributor)
                    .expect(AccessStatus.ALLOWED.toString())
                        .from(() -> permissionService.hasPermission(contribDoc, PermissionService.WRITE).toString())
                        .because("Contrib user has write permissions on created document as the owner.")
                        
            // When it is declared as an inplace record            
            .when()        
                .as(dmContributor)
                    .perform(() -> recordService.createRecord(filePlan, contribDoc))
            
            .then()        
                .asAdmin()
                    // Then it becomes a record
                    .expect(true)
                        .from(() -> recordService.isRecord(contribDoc))
                        .because("The document is a record")
                    
                    // And it isn't filed
                    .expect(false)
                        .from(() -> recordService.isFiled(contribDoc))
                        .because("The record is not filed")
                
                // And a site collaborator has filling permissions and filling capability on the record     
                .as(dmCollaborator)
                    .perform(() ->               
                        checkInPlaceAccess(contribDoc, 
                                AccessStatus.ALLOWED,  // read record permission 
                                AccessStatus.ALLOWED,  // filing permission
                                AccessStatus.ALLOWED,  // view record capability 
                                AccessStatus.ALLOWED,  // edit non record metadata capability
                                AccessStatus.DENIED))  // edit record metadata capability
                
                // And a site contributor has filling capability and permissions  
                .as(dmContributor)
                    .perform(() ->               
                        checkInPlaceAccess(contribDoc, 
                                AccessStatus.ALLOWED,  // read record permission 
                                AccessStatus.ALLOWED,  // filing permission
                                AccessStatus.ALLOWED,  // view record capability 
                                AccessStatus.ALLOWED,  // edit non record metadata capability
                                AccessStatus.DENIED))  // edit record metadata capability
                    
                // And a site consumer has read permissions and view record capability on the record
                .as(dmConsumer)
                    .perform(() -> 
                        checkInPlaceAccess(contribDoc, 
                                AccessStatus.ALLOWED,  // read record permission 
                                AccessStatus.DENIED,   // filing permission
                                AccessStatus.ALLOWED,  // view record capability 
                                AccessStatus.DENIED,   // edit non record metadata capability
                                AccessStatus.DENIED))  // edit record metadata capability   
            
                // And a user that is not a member of the site has no access to the inplace record     
                .as(userName)
                    .perform(() ->    
                        checkInPlaceAccess(contribDoc, 
                                AccessStatus.DENIED,   // read record permission 
                                AccessStatus.DENIED,   // filing permission
                                AccessStatus.DENIED,   // view record capability 
                                AccessStatus.DENIED,   // edit non record metadata capability
                                AccessStatus.DENIED));  // edit record metadata capability   
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
                    
                 // And a site contributor has read and view    
                .as(dmContributor)
                    .perform(() ->               
                        checkInPlaceAccess(dmDocument, 
                            AccessStatus.ALLOWED,  // read record permission 
                            AccessStatus.DENIED,  // filing permission
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
                    
                // And a site contributor has read and view    
                .as(dmContributor)
                    .perform(() ->               
                        checkInPlaceAccess(dmDocument, 
                            AccessStatus.ALLOWED,  // read record permission 
                            AccessStatus.DENIED,  // filing permission
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
     * Given an inplace record ready for destruction
     * When it is destroyed
     * And it's metadata is maintained
     * Then the inplace users will no longer see the record
     */
    public void testDestroyedRecordInplacePermissions()
    {
        test()
            .given()
            
                // Given that a record is declared by a collaborator
                .as(dmCollaborator)
                    .perform(() -> recordService.createRecord(filePlan, dmDocument))
                    .expect(true)
                        .from(() -> recordService.isRecord(dmDocument))
                        .because("Document is a record.")
            
               // And it is filed into the file plan
               // And eligible for destruction
               .asAdmin()
                   .perform(() ->
                   {
                       // create record category and disposition schedule
                       NodeRef recordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                       utils.createBasicDispositionSchedule(recordCategory, GUID.generate(), GUID.generate(), true, true);
                       
                       // create record folder and file record 
                       NodeRef recordFolder = recordFolderService.createRecordFolder(recordCategory, GUID.generate());
                       fileFolderService.move(dmDocument, recordFolder, null);
                       
                       // cut off record
                       rmActionService.executeRecordsManagementAction(dmDocument, DeclareRecordAction.NAME);
                       utils.completeEvent(dmDocument, CommonRMTestUtils.DEFAULT_EVENT_NAME);
                       rmActionService.executeRecordsManagementAction(dmDocument, CutOffAction.NAME);                                              
                   })
                   .expect("destroy")
                       .from(() -> dispositionService.getNextDispositionAction(dmDocument).getName())
                       .because("The next action is destroy.")
                   .expect(true)
                       .from(() -> dispositionService.isNextDispositionActionEligible(dmDocument))
                       .because("The next action is eligible.")
             
            // When the record is destroyed           
            .when(() -> rmActionService.executeRecordsManagementAction(dmDocument, DestroyAction.NAME))                                
            
            .then()
                .expect(true)
                    .from(() -> recordService.isMetadataStub(dmDocument))
                    .because("The record has been destroyed and the meta-stub remains.")    
            
                // Then the collaborator has no permissions or capabilities        
                .as(dmCollaborator)
                    .perform(() -> 
                        checkInPlaceAccess(dmDocument, 
                            AccessStatus.DENIED,  // read record permission 
                            AccessStatus.DENIED,  // filing permission
                            AccessStatus.DENIED,  // view record capability 
                            AccessStatus.DENIED,  // edit non record metadata capability
                            AccessStatus.DENIED))  // edit record metadata capability
                    
                // And a site contributor has no permissions or capabilities
                .as(dmContributor)
                    .perform(() ->               
                        checkInPlaceAccess(dmDocument, 
                            AccessStatus.DENIED,  // read record permission 
                            AccessStatus.DENIED,  // filing permission
                            AccessStatus.DENIED,  // view record capability 
                            AccessStatus.DENIED,  // edit non record metadata capability
                            AccessStatus.DENIED))  // edit record metadata capability
            
                // And the consumer has no permissions or capabilities   
                .as(dmConsumer)
                    .perform(() -> 
                        checkInPlaceAccess(dmDocument, 
                            AccessStatus.DENIED,  // read record permission 
                            AccessStatus.DENIED,   // filing permission
                            AccessStatus.DENIED,  // view record capability 
                            AccessStatus.DENIED,   // edit non record metadata capability
                            AccessStatus.DENIED))  // edit record metadata capability     
                
                // And a user that is not in the site has no permissions or capabilities
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
     * Given an inplace record
     * And the collaborator has view and edit non-record capability
     * And doesn't have edit record capability
     * When we add edit record metadata capability to the extended writer role
     * Then the collaborator now has edit record metadata capability
     */
    public void testAddUserToRole()
    {
        test()
            .given()
                .as(dmCollaborator)
                    
                    // Given an inplace record
                    .perform(() -> recordService.createRecord(filePlan, dmDocument))
                    .expect(true)
                        .from(() -> recordService.isRecord(dmDocument))
                        .because("Document is a record.")
                        
                    // And the collaborator has view and edit non-record capability
                    // And doesn't have edit record capability
                    .perform(() -> 
                        checkInPlaceAccess(dmDocument, 
                            AccessStatus.ALLOWED,  // read record permission 
                            AccessStatus.ALLOWED,  // filing permission
                            AccessStatus.ALLOWED,  // view record capability 
                            AccessStatus.ALLOWED,  // edit non record metadata capability
                            AccessStatus.DENIED))  // edit record metadata capability
            .when()
                .asAdmin()
                
                     // When we add edit record metadata capability to the extended writer role
                    .perform(() -> filePlanRoleService.updateRole(filePlan, 
                                                                  FilePlanRoleService.ROLE_EXTENDED_WRITERS, 
                                                                  "", 
                                                                  Stream
                                                                      .of(viewRecordsCapability, editNonRecordMetadataCapability, editRecordMetadataCapability)
                                                                      .collect(Collectors.toSet())))
            
            .then()
                .as(dmCollaborator)
                
                    // Then the collaborator now has edit record metadata capability
                    .perform(() -> 
                    checkInPlaceAccess(dmDocument, 
                           AccessStatus.ALLOWED,  // read record permission 
                           AccessStatus.ALLOWED,  // filing permission
                           AccessStatus.ALLOWED,  // view record capability 
                           AccessStatus.ALLOWED,  // edit non record metadata capability
                           AccessStatus.ALLOWED)) // edit record metadata capability            
        ;    
    }
    
    /**
     * Given an inplace record
     * When the record is hidden
     * Then the collaborator has no access to the record 
     * And the consumer has no access to the record
     * And a user that is not in the site has no permissions or capabilities
     */
    public void testNoPermissionsAfterHide()
    {
        test()
            .given()
                .as(dmCollaborator)
                
                    // Given an inplace record
                    .perform(() -> recordService.createRecord(filePlan, dmDocument))
                    .expect(true)
                        .from(() -> recordService.isRecord(dmDocument))
                        .because("Document is a record.")
             .when()
                 .asAdmin()
                 
                     // When the record is hidden
                     .perform(() -> inplaceRecordService.hideRecord(dmDocument))
                     
             .then()
             
                 // Then the collaborator has no access to the record       
                 .as(dmCollaborator)
                     .perform(() -> 
                         checkInPlaceAccess(dmDocument, 
                             AccessStatus.DENIED,  // read record permission 
                             AccessStatus.DENIED,  // filing permission
                             AccessStatus.DENIED,  // view record capability 
                             AccessStatus.DENIED,  // edit non record metadata capability
                             AccessStatus.DENIED))  // edit record metadata capability
                     
                 // And a site contributor has read and view    
                 .as(dmContributor)
                     .perform(() ->               
                         checkInPlaceAccess(dmDocument, 
                             AccessStatus.DENIED,  // read record permission 
                             AccessStatus.DENIED,  // filing permission
                             AccessStatus.DENIED,  // view record capability 
                             AccessStatus.DENIED,  // edit non record metadata capability
                             AccessStatus.DENIED))  // edit record metadata capability
             
                 // And the consumer has no access to the record   
                 .as(dmConsumer)
                     .perform(() -> 
                         checkInPlaceAccess(dmDocument, 
                             AccessStatus.DENIED,  // read record permission 
                             AccessStatus.DENIED,   // filing permission
                             AccessStatus.DENIED,  // view record capability 
                             AccessStatus.DENIED,   // edit non record metadata capability
                             AccessStatus.DENIED))  // edit record metadata capability     
                 
                 // And a user that is not in the site has no permissions or capabilities
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
     * Given an inplace record
     * When the record is rejected
     * Then the collaborator has no access to the record 
     * And the consumer has no access to the record
     * And a user that is not in the site has no permissions or capabilities
     */
    public void testNoPermissionsAfterReject()
    {
        test()
            .given()
                .as(dmCollaborator)
                
                    // Given an inplace record
                    .perform(() -> recordService.createRecord(filePlan, dmDocument))
                    .expect(true)
                        .from(() -> recordService.isRecord(dmDocument))
                        .because("Document is a record.")
             .when()
                 .asAdmin()
                 
                     // When the record is rejected
                     .perform(() -> recordService.rejectRecord(dmDocument, GUID.generate()))
                     
             .then()
             
                 // Then the collaborator has no access to the record       
                 .as(dmCollaborator)
                     .perform(() -> 
                         checkInPlaceAccess(dmDocument, 
                             AccessStatus.DENIED,  // read record permission 
                             AccessStatus.DENIED,  // filing permission
                             AccessStatus.DENIED,  // view record capability 
                             AccessStatus.DENIED,  // edit non record metadata capability
                             AccessStatus.DENIED))  // edit record metadata capability
                     
                 // And a site contributor has read and view    
                 .as(dmContributor)
                     .perform(() ->               
                         checkInPlaceAccess(dmDocument, 
                             AccessStatus.DENIED,  // read record permission 
                             AccessStatus.DENIED,  // filing permission
                             AccessStatus.DENIED,  // view record capability 
                             AccessStatus.DENIED,  // edit non record metadata capability
                             AccessStatus.DENIED))  // edit record metadata capability
             
                 // And the consumer has no access to the record   
                 .as(dmConsumer)
                     .perform(() -> 
                         checkInPlaceAccess(dmDocument, 
                             AccessStatus.DENIED,  // read record permission 
                             AccessStatus.DENIED,   // filing permission
                             AccessStatus.DENIED,  // view record capability 
                             AccessStatus.DENIED,   // edit non record metadata capability
                             AccessStatus.DENIED))  // edit record metadata capability     
                 
                 // And a user that is not in the site has no permissions or capabilities
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
     * Given a user is the cm:creator of a document
     * And the user is deleted
     * When the document is declared as a record by a manager
     * Then it successfully becomes a record
     */
    public void testCmCreatorDeletedBeforeRecordDeclaration()
    {
        test()
            .given()
                .asAdmin()
                    .perform(() -> 
                    {
                        deletedUser = GUID.generate();
                        createPerson(deletedUser);
                        siteService.setMembership(collabSiteId, deletedUser, SiteModel.SITE_CONTRIBUTOR);
                    })
               .as(deletedUser)
                   .perform(() ->
                   {
                        deleteUserDoc = fileFolderService.create(dmFolder, "deleteUserDoc.txt" , ContentModel.TYPE_CONTENT).getNodeRef();
                        dbNodeService.addAspect(deleteUserDoc, ContentModel.ASPECT_AUDITABLE, null);
                    })  
               .asAdmin()
                   .perform(() -> personService.deletePerson(deletedUser))              
            .when()
                .as(dmCollaborator)
                    .perform(() -> recordService.createRecord(filePlan, deleteUserDoc))
            .then()
                .expect(true)
                    .from(() -> recordService.isRecord(deleteUserDoc))
                    .because("The document is now a record.")
        ;
    }
    
    /**
     * Given a document created by the collaborator
     * And declared as a record by the collaborator
     * And filed by the records manager
     * When the records manager copies the record
     * Then the collaborator has no access to the record copy 
     * And a site contributor has no access to the record copy
     * And the consumer has no access to the record copy
     * And a user that is not in the site has no access to the record copy
     */
    public void testNoPermissionsOnCopy()
    {
        test()
            .given()
               .as(dmCollaborator)
                   .perform(() ->
                   {
                       // Given a document created by the collaborator
                       copiedDoc = fileFolderService.create(dmFolder, "copiedDoc.txt" , ContentModel.TYPE_CONTENT).getNodeRef();
                       dbNodeService.addAspect(copiedDoc, ContentModel.ASPECT_AUDITABLE, null);
                       
                       // And declared as a record by the collaborator
                       recordService.createRecord(filePlan, copiedDoc);
                   })  
               .asAdmin()
               
                    // And filed by the records manager
                   .perform(() -> fileFolderService.move(copiedDoc, rmFolder, null))
                   
               .as(dmCollaborator)
                   .perform(() -> 
                       checkInPlaceAccess(copiedDoc, 
                           AccessStatus.ALLOWED,  // read record permission 
                           AccessStatus.ALLOWED,  // filing permission
                           AccessStatus.ALLOWED,  // view record capability 
                           AccessStatus.ALLOWED,  // edit non record metadata capability
                           AccessStatus.DENIED))  // edit record metadata capability    
                   
            .when()
                .asAdmin()
                
                    // When the records manager copies the record
                    .perform(() -> copyDoc = fileFolderService.copy(copiedDoc, rmFolder, "newRecord.txt").getNodeRef())
                   
            .then()

                // Then the collaborator has no access to the record copy     
                .as(dmCollaborator)
                    .perform(() -> 
                        checkInPlaceAccess(copyDoc, 
                            AccessStatus.DENIED,  // read record permission 
                            AccessStatus.DENIED,  // filing permission
                            AccessStatus.DENIED,  // view record capability 
                            AccessStatus.DENIED,  // edit non record metadata capability
                            AccessStatus.DENIED))  // edit record metadata capability
                    .perform(() -> 
                        checkInPlaceAccess(copiedDoc, 
                            AccessStatus.ALLOWED,  // read record permission 
                            AccessStatus.ALLOWED,  // filing permission
                            AccessStatus.ALLOWED,  // view record capability 
                            AccessStatus.ALLOWED,  // edit non record metadata capability
                            AccessStatus.DENIED))  // edit record metadata capability
                    
                // And a site contributor has no access to the record copy     
                .as(dmContributor)
                    .perform(() ->               
                        checkInPlaceAccess(copyDoc, 
                            AccessStatus.DENIED,  // read record permission 
                            AccessStatus.DENIED,  // filing permission
                            AccessStatus.DENIED,  // view record capability 
                            AccessStatus.DENIED,  // edit non record metadata capability
                            AccessStatus.DENIED))  // edit record metadata capability
            
                // And the consumer has no access to the record copy   
                .as(dmConsumer)
                    .perform(() -> 
                        checkInPlaceAccess(copyDoc, 
                            AccessStatus.DENIED,  // read record permission 
                            AccessStatus.DENIED,   // filing permission
                            AccessStatus.DENIED,  // view record capability 
                            AccessStatus.DENIED,   // edit non record metadata capability
                            AccessStatus.DENIED))  // edit record metadata capability     
                
                // And a user that is not in the site has no access to the record copy 
                .as(userName)
                    .perform(() -> 
                        checkInPlaceAccess(copyDoc, 
                            AccessStatus.DENIED,   // read record permission 
                            AccessStatus.DENIED,   // filing permission
                            AccessStatus.DENIED,   // view record capability 
                            AccessStatus.DENIED,   // edit non record metadata capability
                            AccessStatus.DENIED));  // edit record metadata capability 
    }
    
    /**
     * Test group reuse
     */
    public void testGroupReuse()
    {
        test()
            .when()
                .as(dmCollaborator)
                    .perform(50, () ->
                    {
                        NodeRef newDocument = fileFolderService.create(dmFolder, GUID.generate(), ContentModel.TYPE_CONTENT).getNodeRef();
                        recordService.createRecord(filePlan, newDocument);
                    })
                .as(dmContributor)
                    .perform(50, () ->
                    {
                        NodeRef newDocument = fileFolderService.create(dmFolder, GUID.generate(), ContentModel.TYPE_CONTENT).getNodeRef();
                        recordService.createRecord(filePlan, newDocument);
                    })
            .then()
                .asAdmin()
                    .expect(101)
                        .from(() -> nodeService.getChildAssocs(dmFolder).size())
                        .because("One hundred inplace records have been created.")
                    .expect(3)
                        .from(() -> authorityService.getContainedAuthorities(null, "GROUP_INPLACE_RECORD_MANAGEMENT", true).size())
                        .because("The read and write groups are reused.");
    }
    
    /**
     * Test tear down
     */
    @Override
    protected void tearDownImpl()
    {
        super.tearDownImpl();
        
        // clear up groups
        authorityService.getContainedAuthorities(null, "GROUP_INPLACE_RECORD_MANAGEMENT", true)
            .stream()
            .forEach((group) -> authorityService.deleteAuthority(group));
    }
}
