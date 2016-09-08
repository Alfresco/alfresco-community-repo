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
package org.alfresco.module.org_alfresco_module_rm.test.integration.record;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;

/**
 * Create record tests.
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public class CreateRecordTest extends BaseRMTestCase
{    
    public void testCreateRecordCapabilityOnly() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            /** test data */
            String roleName = GUID.generate();
            String user = GUID.generate();            
            NodeRef recordFolder;
            NodeRef record;
            
            public void given()
            {
                // create role
                Set<Capability> capabilities = new HashSet<Capability>(2);
                capabilities.add(capabilityService.getCapability("ViewRecords"));
                capabilities.add(capabilityService.getCapability("CreateRecords"));
                filePlanRoleService.createRole(filePlan, roleName, roleName, capabilities);
                
                // create user and assign to role
                createPerson(user, true);                
                filePlanRoleService.assignRoleToAuthority(filePlan, roleName, user);
                
                // create file plan structure
                NodeRef rc = filePlanService.createRecordCategory(filePlan, GUID.generate());
                recordFolder = recordFolderService.createRecordFolder(rc, GUID.generate());
            }
            
            public void when()
            {
                // give read and file permissions to user
                filePlanPermissionService.setPermission(recordFolder, user, RMPermissionModel.FILING);
                
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        record = recordService.createRecordFromContent(recordFolder, GUID.generate(), TYPE_CONTENT, null, null);
                        
                        return null;
                    }
                }, user);                             
            }
            
            public void then()
            {
                // check the details of the record
                assertTrue(recordService.isRecord(record));
            }
        });           
    }
    
    /**
     * @see 
     */
    public void testCreateRecordCapabilityOnlyFromFileFolderService() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            /** test data */
            String roleName = GUID.generate();
            String user = GUID.generate();            
            NodeRef recordFolder;
            NodeRef record;
            
            public void given()
            {
                // create role
                Set<Capability> capabilities = new HashSet<Capability>(2);
                capabilities.add(capabilityService.getCapability("ViewRecords"));
                capabilities.add(capabilityService.getCapability("CreateRecords"));
                filePlanRoleService.createRole(filePlan, roleName, roleName, capabilities);
                
                // create user and assign to role
                createPerson(user, true);                
                filePlanRoleService.assignRoleToAuthority(filePlan, roleName, user);
                
                // create file plan structure
                NodeRef rc = filePlanService.createRecordCategory(filePlan, GUID.generate());
                recordFolder = recordFolderService.createRecordFolder(rc, GUID.generate());
            }
            
            public void when()
            {
                // give read and file permissions to user
                filePlanPermissionService.setPermission(recordFolder, user, RMPermissionModel.FILING);
                
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        record = fileFolderService.create(recordFolder, GUID.generate(), ContentModel.TYPE_CONTENT).getNodeRef();
                        
                        ContentWriter writer = contentService.getWriter(record, ContentModel.TYPE_CONTENT, true);
                        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                        writer.setEncoding("UTF-8");
                        writer.putContent("Lucy Wetherall");
                        
                        return null;
                    }
                }, user);                             
            }
            
            public void then()
            {
                // check the details of the record
                assertTrue(recordService.isRecord(record));
            }
        });           
    }
}
