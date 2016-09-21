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

package org.alfresco.module.org_alfresco_module_rm.test.legacy.action;

import org.alfresco.module.org_alfresco_module_rm.action.dm.CreateRecordAction;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * Record service implementation unit test.
 *
 * @author Roy Wetherall
 */
public class CreateRecordActionTest extends BaseRMTestCase
{
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

    public void testCreateRecordAction()
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(dmDocument, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(filePlan, RMPermissionModel.VIEW_RECORDS));

                Action action = actionService.createAction(CreateRecordAction.NAME);
                action.setParameterValue(CreateRecordAction.PARAM_HIDE_RECORD, false);
                action.setParameterValue(CreateRecordAction.PARAM_FILE_PLAN, filePlan);
                actionService.executeAction(action, dmDocument);

                return null;
            }

            public void test(Void result) throws Exception
            {
                assertTrue(recordService.isRecord(dmDocument));

                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(dmDocument, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(filePlan, RMPermissionModel.VIEW_RECORDS));
            };
        },
        dmCollaborator);
    }
}
