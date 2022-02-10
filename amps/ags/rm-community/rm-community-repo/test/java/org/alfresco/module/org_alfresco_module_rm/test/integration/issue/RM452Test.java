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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.TestService;

/**
 * System test for RM-452
 *
 * See alfresco.extension.rm-method-security.properties
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class RM452Test extends BaseRMTestCase
{
    private TestService testService;

    @Override
    protected void initServices()
    {
        super.initServices();

        testService = (TestService)applicationContext.getBean("TestService");
    }

    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    @Override
    protected boolean isRecordTest()
    {
        return true;
    }

    public void testRM452() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertNotNull(folder);
                assertNotNull(recordOne);
                assertFalse(filePlanService.isFilePlanComponent(folder));
                assertTrue(filePlanService.isFilePlanComponent(recordOne));

                // call methodOne with non-RM artifact .. expect success
                testService.testMethodOne(folder);

                // call methodTwo with non-RM artifact .. expect success
                testService.testMethodTwo(folder);

                // call methodOne with an RM artifact .. expect success
                testService.testMethodOne(recordOne);

                return null;
            }
        });

        doTestInTransaction(new FailureTest
        (
                "Shouldn't be able to call testMethodTwo on TestService, because override RM security for method is not configred.",
                AlfrescoRuntimeException.class
        )
        {

            @Override
            public void run() throws Exception
            {
                // call methodTwo with an RM artifact .. expect failure
                testService.testMethodTwo(recordOne);
            }
        });
    }
}
