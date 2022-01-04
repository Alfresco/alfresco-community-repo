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

package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.forms.RecordsManagementNodeFormFilter;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.Item;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;

/**
 * Test for RM-1727
 *
 * @author Tatsiana Shalima
 * @since 2.3
 */
public class RM1727Test extends BaseRMTestCase
{
    private String myUser;
    private NodeRef folder;
    private NodeRef record;

    private RecordsManagementNodeFormFilter nodeFormFilter;
    private Form form;

    @Override
    protected boolean isRecordTest()
    {
        return true;
    }

    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    @Override
    protected void setupTestUsersImpl(NodeRef filePlan)
    {
        nodeFormFilter = (RecordsManagementNodeFormFilter)applicationContext.getBean("rmNodeFormFilter");
        //create user
        myUser = GUID.generate();
        createPerson(myUser);
        //give user RM Manager role
        filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_RECORDS_MANAGER, myUser);
        //create category > folder > record
        NodeRef category = filePlanService.createRecordCategory(filePlan, GUID.generate());
        folder = recordFolderService.createRecordFolder(category, GUID.generate());
        record = recordService.createRecordFromContent(folder, GUID.generate(), TYPE_CONTENT, null, null);
    }

    public void testRM1727()
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                //set read and file permissions for folder
                filePlanPermissionService.setPermission(folder, myUser, RMPermissionModel.FILING);
                return null;
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                Item item = new Item("node",folder.toString());
                item.setType("rma:recordFolder");
                form = new Form(item);
                nodeFormFilter.afterGenerate(folder, null, null, form, null);
                return null;
            }
        }, myUser);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                Item item = new Item("node",record.toString());
                item.setType("rma:record");
                form = new Form(item);
                nodeFormFilter.afterGenerate(record, null, null, form, null);
                return null;
            }
        }, myUser);
    }
}
