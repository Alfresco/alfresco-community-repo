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

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.security.PermissionService;

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
    protected PermissionService dmPermissionService;

    @Override
    protected void initServices()
    {
        super.initServices();

        dmActionService = (ActionService) applicationContext.getBean("ActionService");
        dmPermissionService = (PermissionService) applicationContext.getBean("PermissionService");
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
                // FIXME
                return null;
            }

            public void test(Void result) throws Exception
            {
                // FIXME
            };
        },
        dmCollaborator);
    }
}
