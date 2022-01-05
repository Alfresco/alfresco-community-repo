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

import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ContentData;
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
                Set<Capability> capabilities = new HashSet<>(2);
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
     * 
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
                Set<Capability> capabilities = new HashSet<>(2);
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

    /**
     * unit test for RM1649 fix
     * test if a user with create record permissions and without file record permission is able to create a record within unfiled record container
     */
    public void testCreateRecordCapabilityInsideUnfiledRecordsContainer() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            /** test data */
            String roleName = GUID.generate();
            String user = GUID.generate();
            NodeRef record;

            public void given()
            {
                // create a role with view and create capabilities
                Set<Capability> capabilities = new HashSet<>(2);
                capabilities.add(capabilityService.getCapability("ViewRecords"));
                capabilities.add(capabilityService.getCapability("CreateRecords"));
                filePlanRoleService.createRole(filePlan, roleName, roleName, capabilities);


                // create user and assign to role
                createPerson(user, true);
                filePlanRoleService.assignRoleToAuthority(filePlan, roleName, user);

                //give read and file permission to user on unfiled records container
                filePlanPermissionService.setPermission(unfiledContainer , user, RMPermissionModel.FILING);
            }

            public void when()
            {
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        record = recordService.createRecordFromContent(unfiledContainer, GUID.generate(), TYPE_CONTENT, null, null);

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
     * Given I have ViewRecord and CreateRecord capabilities
     * And I have filling on a record folder
     * When I create content via ScriptNode (simulated)
     * Then the record is successfully created
     * 
     * @see https://issues.alfresco.com/jira/browse/RM-1956
     */
    public void testCreateRecordViaCoreServices() throws Exception
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
                // create a role with view and create capabilities
                Set<Capability> capabilities = new HashSet<>(2);
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
                
                record = AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
                {
                    public NodeRef doWork() throws Exception
                    {
                        NodeRef record = fileFolderService.create(recordFolder, "testRecord.txt", ContentModel.TYPE_CONTENT).getNodeRef();                        
                        ContentData content = (ContentData)nodeService.getProperty(record, PROP_CONTENT);
                        nodeService.setProperty(record, PROP_CONTENT, ContentData.setMimetype(content, MimetypeMap.MIMETYPE_TEXT_PLAIN));                        
                        return record;
                    }
                }, user);
            }

            public void then()
            {
                // check the details of the record
                assertTrue(recordService.isRecord(record));
                
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        // we are expecting an expception here
                        try
                        {
                            ContentData content = (ContentData)nodeService.getProperty(record, PROP_CONTENT);
                            nodeService.setProperty(record, PROP_CONTENT, ContentData.setMimetype(content, MimetypeMap.MIMETYPE_TEXT_PLAIN));
                            fail("Expecting access denied exception");
                        }
                        catch (AccessDeniedException exception)
                        {
                            // expceted
                        }
                        
                        return null;
                    }
                }, user);
            }
  
        });
    }    
}
