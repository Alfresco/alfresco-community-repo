/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.action;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.dm.CreateRecordAction;
import org.alfresco.module.org_alfresco_module_rm.action.dm.HideRecordAction;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Hide Record Action Unit Test
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class HideRecordActionTest extends BaseRMTestCase
{
    /** Services */
    protected ActionService dmActionService;

    @Override
    protected void initServices()
    {
        super.initServices();

        dmActionService = (ActionService) applicationContext.getBean("ActionService");
    }

    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    public void testHideRecordAction()
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                // The user must have the appropriate rights
                Role role = filePlanRoleService.getRole(filePlan, "RecordsManager");
                authorityService.addAuthority(role.getRoleGroupName(), dmCollaborator);

                return null;
            }
        },
        AuthenticationUtil.getAdminUserName());

        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                // Create a document so that the user has the write permissions for that document
                NodeRef doc = fileFolderService.create(dmFolder, "testfile.txt", ContentModel.TYPE_CONTENT).getNodeRef();

                // Create a record from that document
                Action createAction = dmActionService.createAction(CreateRecordAction.NAME);
                createAction.setParameterValue(CreateRecordAction.PARAM_FILE_PLAN, filePlan);
                dmActionService.executeAction(createAction, doc);

                // Check if the document is a record now
                assertTrue(recordService.isRecord(doc));

                // The record should have the original location information
                assertNotNull(nodeService.getProperty(doc, PROP_RECORD_ORIGINATING_LOCATION));

                // Check the parents. In this case the document should have two parents (doclib and fileplan)
                assertTrue(nodeService.getParentAssocs(doc).size() == 2);

                // Hide the document. The user has the write permissions so he should be able to hide it
                Action hideAction = dmActionService.createAction(HideRecordAction.NAME);
                dmActionService.executeAction(hideAction, doc);

                // The document should be removed from the collaboration site
                assertTrue(nodeService.getParentAssocs(doc).size() == 1);

                return null;
            }
        },
        dmCollaborator);
    }
}
