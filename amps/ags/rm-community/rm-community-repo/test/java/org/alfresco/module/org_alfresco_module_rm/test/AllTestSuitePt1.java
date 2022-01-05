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
package org.alfresco.module.org_alfresco_module_rm.test;

import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.extensions.cpsuite.ClasspathSuite.ClassnameFilters;
import org.junit.extensions.cpsuite.ClasspathSuite.SuiteTypes;
import org.junit.extensions.cpsuite.SuiteType;
import org.junit.runner.RunWith;

/**
 * Convenience test suite that runs all the tests. THIS HAS BEEN SPLIT INTO PARTS SO THAT THE BUILD TIME IS REDUCED.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@RunWith(ClasspathSuite.class)
@SuiteTypes({SuiteType.TEST_CLASSES, SuiteType.RUN_WITH_CLASSES, SuiteType.JUNIT38_TEST_CLASSES})
@ClassnameFilters({

    // The following packages are run by Pt1. IF YOU CHANGE THIS LIST ALSO CHANGE IT IN AllTestSuitePt3.
    "org\\.alfresco\\.module\\.org_alfresco_module_rm\\.test\\.legacy\\.action\\..*Test",
    "org\\.alfresco\\.module\\.org_alfresco_module_rm\\.test\\.legacy\\.capabilities\\..*Test",
    "org\\.alfresco\\.module\\.org_alfresco_module_rm\\.test\\.legacy\\.jscript\\..*Test",
    "org\\.alfresco\\.module\\.org_alfresco_module_rm\\.test\\.legacy\\.security\\..*Test",
    "org\\.alfresco\\.module\\.org_alfresco_module_rm\\.test\\.legacy\\.service\\..*Test",
    "org\\.alfresco\\.module\\.org_alfresco_module_rm\\.test\\.legacy\\.webscript\\..*Test",
    // There appears to be some common setup taking place in the first 2 packages, which is why all legacy tests are
    // together even though they take a little longer to run that way.

    // Exclude all UnitTests
    "!.*UnitTest",

    // Put the test classes you want to exclude here
    "!.*DataLoadSystemTest",
    "!.*RM2072Test",
    "!.*RM2190Test",
    "!.*RM981SystemTest",
    "!.*RM3993Test",
    "!.*RM4163Test",
    "!.*RecordsManagementEventServiceImplTest",
    "!.*RmRestApiTest",
    "!.*NotificationServiceHelperSystemTest",
    "!.*RetryingTransactionHelperBaseTest",
    "!.*RMCaveatConfigServiceImplTest",
    // This test is running successfully locally but not on bamboo (if executed as a single test).
    // The problem can be reproduced if the whole test suite is run locally as well.
    // Tests should not be dependant on other test classes and should run in any order without any problems.
    "!.*EmailMapScriptTest"
})
public class AllTestSuitePt1
{
}
