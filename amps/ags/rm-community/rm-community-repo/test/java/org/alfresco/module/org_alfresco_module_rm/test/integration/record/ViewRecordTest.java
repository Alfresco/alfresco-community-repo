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

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;

/**
 * View record tests.
 *
 * @author Ana Bozianu
 * @since 2.3
 */
public class ViewRecordTest extends BaseRMTestCase
{
    /**
     * Given a user with read permission on a record and without read permission on the parent folder check if the user can check if the record is filed
     *
     * @see https://issues.alfresco.com/jira/browse/RM-1738
     */
    public void testReadIsFiledPropertyWithoutReadPermissionOnParentFolder() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            /** test data */
            String roleName = GUID.generate();
            String user = GUID.generate();
            NodeRef rc;
            NodeRef recordFolder;
            NodeRef record;
            boolean recordIsFiled = false;

            public void given()
            {
             // create role
                Set<Capability> capabilities = new HashSet<>(2);
                capabilities.add(capabilityService.getCapability("ViewRecords"));
                filePlanRoleService.createRole(filePlan, roleName, roleName, capabilities);

                // create user and assign to role
                createPerson(user, true);
                filePlanRoleService.assignRoleToAuthority(filePlan, roleName, user);

                // create file plan structure
                rc = filePlanService.createRecordCategory(filePlan, GUID.generate());
                NodeRef rsc = filePlanService.createRecordCategory(rc, GUID.generate());
                recordFolder = recordFolderService.createRecordFolder(rsc, GUID.generate());
                record = recordService.createRecordFromContent(recordFolder, GUID.generate(), TYPE_CONTENT, null, null);
            }

            public void when()
            {
                // give read and file permissions on folder and remove permission from parent
                filePlanPermissionService.setPermission(rc, user, RMPermissionModel.READ_RECORDS);
                permissionService.setInheritParentPermissions(recordFolder, false);
                filePlanPermissionService.setPermission(record, user, RMPermissionModel.READ_RECORDS);

                //check if the user can read the isFiled property
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        recordIsFiled = recordService.isFiled(record);

                        return null;
                    }
                }, user);
            }

            public void then()
            {
                //check if the property is evaluated correctly
                assertTrue(recordIsFiled);
            }

        });
    }

}
