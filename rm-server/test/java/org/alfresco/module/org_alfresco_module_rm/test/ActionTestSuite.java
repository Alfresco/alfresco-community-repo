/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.module.org_alfresco_module_rm.test.action.CreateRecordActionTest;
import org.alfresco.module.org_alfresco_module_rm.test.action.FileToActionTest;
import org.alfresco.module.org_alfresco_module_rm.test.action.HideRecordActionTest;
import org.alfresco.module.org_alfresco_module_rm.test.action.RejectActionTest;
import org.alfresco.module.org_alfresco_module_rm.test.service.ExtendedActionServiceTest;
import org.alfresco.module.org_alfresco_module_rm.test.service.RecordsManagementActionServiceImplTest;


/**
 * RM test suite
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class ActionTestSuite extends TestSuite
{
    /**
     * Creates the test suite
     *
     * @return  the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(RecordsManagementActionServiceImplTest.class);
        suite.addTestSuite(ExtendedActionServiceTest.class);
        suite.addTestSuite(CreateRecordActionTest.class);
        suite.addTestSuite(HideRecordActionTest.class);
        suite.addTestSuite(RejectActionTest.class);
        suite.addTestSuite(FileToActionTest.class);
        return suite;
    }
}
