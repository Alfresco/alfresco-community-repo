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

package org.alfresco.module.org_alfresco_module_rm.test.integration.relationship;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.relationship.Relationship;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;

/**
 * Delete relationship test.
 *
 * @author Ana Bozianu
 * @since 2.3
 */
public class DeleteRelationshipTest extends BaseRMTestCase
{
	public void testDeleteRelationship() throws Exception
    {
    	doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            /** test data */
        	NodeRef sourceNode;
        	NodeRef targetNode;
        	String associationName = CUSTOM_REF_OBSOLETES.getLocalName();

            public void given()
            {

            	// create the source record
            	sourceNode = utils.createRecord(rmFolder, GUID.generate());

                //create the target record
            	targetNode = utils.createRecord(rmFolder, GUID.generate());

                //create relationship
                relationshipService.addRelationship(associationName, sourceNode, targetNode);
            }

            public void when()
            {
                //delete relationship
            	relationshipService.removeRelationship(associationName, sourceNode, targetNode);
            }

            public void then()
            {
               //check if relationship is deleted
            	Set<Relationship> relationships = relationshipService.getRelationshipsFrom(sourceNode);
            	for(Relationship r : relationships)
            	{
            		assertFalse(r.getTarget().equals(targetNode) && r.getUniqueName().equals(associationName));
            	}
            }
        });
    }
	
	public void testReadOnlyPermissionOnSource() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AccessDeniedException.class)
        {
            /** test data */
            private String roleName = GUID.generate();
            private String user = GUID.generate();
            private NodeRef sourceRecordCategory;
            private NodeRef targetRecordCategory;
            private NodeRef sourceRecordFolder;
            private NodeRef targetRecordFolder;
            private NodeRef sourceRecord;
            private NodeRef targetRecord;
            
            public void given() throws Exception
            {
                // test entities
                sourceRecordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                sourceRecordFolder = recordFolderService.createRecordFolder(sourceRecordCategory, GUID.generate());
                sourceRecord = utils.createRecord(sourceRecordFolder, GUID.generate());
                targetRecordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                targetRecordFolder = recordFolderService.createRecordFolder(targetRecordCategory, GUID.generate());
                targetRecord = utils.createRecord(targetRecordFolder, GUID.generate());
                
                // create role
                Set<Capability> capabilities = new HashSet<Capability>(2);
                capabilities.add(capabilityService.getCapability("ViewRecords"));
                capabilities.add(capabilityService.getCapability("ChangeOrDeleteReferences"));
                filePlanRoleService.createRole(filePlan, roleName, roleName, capabilities);

                // create user and assign to role
                createPerson(user, true);
                filePlanRoleService.assignRoleToAuthority(filePlan, roleName, user);
                
                // add relationship
                relationshipService.addRelationship("crossreference", sourceRecord, targetRecord);
            }

            public void when()
            {
                // assign permissions
                filePlanPermissionService.setPermission(sourceRecord, user, RMPermissionModel.READ_RECORDS);
                filePlanPermissionService.setPermission(targetRecord, user, RMPermissionModel.FILING);
                
                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        relationshipService.removeRelationship("crossreference", sourceRecord, targetRecord);
                        return null;
                    }
                }, user);
            }
        });
    }
    
    public void testReadOnlyPermissionOnTarget() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AccessDeniedException.class)
        {
            /** test data */
            private String roleName = GUID.generate();
            private String user = GUID.generate();
            private NodeRef sourceRecordCategory;
            private NodeRef targetRecordCategory;
            private NodeRef sourceRecordFolder;
            private NodeRef targetRecordFolder;
            private NodeRef sourceRecord;
            private NodeRef targetRecord;
            
            public void given() throws Exception
            {
                // test entities
                sourceRecordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                sourceRecordFolder = recordFolderService.createRecordFolder(sourceRecordCategory, GUID.generate());
                sourceRecord = utils.createRecord(sourceRecordFolder, GUID.generate());
                targetRecordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                targetRecordFolder = recordFolderService.createRecordFolder(targetRecordCategory, GUID.generate());
                targetRecord = utils.createRecord(targetRecordFolder, GUID.generate());
                
                // create role
                Set<Capability> capabilities = new HashSet<Capability>(2);
                capabilities.add(capabilityService.getCapability("ViewRecords"));
                capabilities.add(capabilityService.getCapability("ChangeOrDeleteReferences"));
                filePlanRoleService.createRole(filePlan, roleName, roleName, capabilities);

                // create user and assign to role
                createPerson(user, true);
                filePlanRoleService.assignRoleToAuthority(filePlan, roleName, user);
                
                // create relationship
                relationshipService.addRelationship("crossreference", sourceRecord, targetRecord);
            }

            public void when()
            {
                // assign permissions
                filePlanPermissionService.setPermission(sourceRecord, user, RMPermissionModel.FILING);
                filePlanPermissionService.setPermission(targetRecord, user, RMPermissionModel.READ_RECORDS);
                
                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        relationshipService.removeRelationship("crossreference", sourceRecord, targetRecord);
                        return null;
                    }
                }, user);
            }
        });
    }
    
    public void testFillingPermissionOnSourceAndTarget() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            /** test data */
            private String roleName = GUID.generate();
            private String user = GUID.generate();
            private NodeRef sourceRecordCategory;
            private NodeRef targetRecordCategory;
            private NodeRef sourceRecordFolder;
            private NodeRef targetRecordFolder;
            private NodeRef sourceRecord;
            private NodeRef targetRecord;
            
            public void given() throws Exception
            {
                // test entities
                sourceRecordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                sourceRecordFolder = recordFolderService.createRecordFolder(sourceRecordCategory, GUID.generate());
                sourceRecord = utils.createRecord(sourceRecordFolder, GUID.generate());
                targetRecordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                targetRecordFolder = recordFolderService.createRecordFolder(targetRecordCategory, GUID.generate());
                targetRecord = utils.createRecord(targetRecordFolder, GUID.generate());
                
                // create role
                Set<Capability> capabilities = new HashSet<Capability>(2);
                capabilities.add(capabilityService.getCapability("ViewRecords"));
                capabilities.add(capabilityService.getCapability("ChangeOrDeleteReferences"));
                filePlanRoleService.createRole(filePlan, roleName, roleName, capabilities);
    
                // create user and assign to role
                createPerson(user, true);
                filePlanRoleService.assignRoleToAuthority(filePlan, roleName, user);    
                
                // create relationship
                relationshipService.addRelationship("crossreference", sourceRecord, targetRecord);
            }
    
            public void when()
            {
                // assign permissions
                filePlanPermissionService.setPermission(sourceRecordCategory, user, RMPermissionModel.FILING);
                filePlanPermissionService.setPermission(targetRecordCategory, user, RMPermissionModel.FILING);
                
                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        relationshipService.removeRelationship("crossreference", sourceRecord, targetRecord);
                        return null;
                    }
                }, user);
            }
            
            @Override
            public void then() throws Exception
            {
                // assert that the relationship exists
                assertEquals(0, relationshipService.getRelationshipsFrom(sourceRecord).size());
                assertEquals(0, relationshipService.getRelationshipsTo(sourceRecord).size());
                assertEquals(0, relationshipService.getRelationshipsFrom(targetRecord).size());
                assertEquals(0, relationshipService.getRelationshipsTo(targetRecord).size());
            }
        });
    }

}
