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

package org.alfresco.module.org_alfresco_module_rm.test.integration.relationship;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;

/**
 * Create relationship integration test.
 *
 * @author Roy Wetherall
 * @since 2.3
 */
public class CreateRelationshipTest extends BaseRMTestCase
{
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
                Set<Capability> capabilities = new HashSet<>(2);
                capabilities.add(capabilityService.getCapability("ViewRecords"));
                capabilities.add(capabilityService.getCapability("ChangeOrDeleteReferences"));
                filePlanRoleService.createRole(filePlan, roleName, roleName, capabilities);

                // create user and assign to role
                createPerson(user, true);
                filePlanRoleService.assignRoleToAuthority(filePlan, roleName, user);
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
                        relationshipService.addRelationship("crossreference", sourceRecord, targetRecord);
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
                Set<Capability> capabilities = new HashSet<>(2);
                capabilities.add(capabilityService.getCapability("ViewRecords"));
                capabilities.add(capabilityService.getCapability("ChangeOrDeleteReferences"));
                filePlanRoleService.createRole(filePlan, roleName, roleName, capabilities);

                // create user and assign to role
                createPerson(user, true);
                filePlanRoleService.assignRoleToAuthority(filePlan, roleName, user);

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
                        relationshipService.addRelationship("crossreference", sourceRecord, targetRecord);
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
                Set<Capability> capabilities = new HashSet<>(2);
                capabilities.add(capabilityService.getCapability("ViewRecords"));
                capabilities.add(capabilityService.getCapability("ChangeOrDeleteReferences"));
                filePlanRoleService.createRole(filePlan, roleName, roleName, capabilities);
    
                // create user and assign to role
                createPerson(user, true);
                filePlanRoleService.assignRoleToAuthority(filePlan, roleName, user);    
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
                        relationshipService.addRelationship("crossreference", sourceRecord, targetRecord);
                        return null;
                    }
                }, user);
            }
            
            @Override
            public void then() throws Exception
            {
                // assert that the relationship exists
                assertEquals(1, relationshipService.getRelationshipsFrom(sourceRecord).size());
                assertEquals(0, relationshipService.getRelationshipsTo(sourceRecord).size());
                assertEquals(0, relationshipService.getRelationshipsFrom(targetRecord).size());
                assertEquals(1, relationshipService.getRelationshipsTo(targetRecord).size());
            }
        });
    }
}
