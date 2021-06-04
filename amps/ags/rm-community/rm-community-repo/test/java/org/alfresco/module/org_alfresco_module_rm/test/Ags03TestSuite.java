/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite that runs a subset of AGS tests (from {@link AllTestSuite}) so that the AGS build jobs are shorter and
 * more in keeping with the repo jobs, making the build shorter over all.
 *
 * @author Alan Davis
 * @since 11
 */
@RunWith(Categories.class)
@Suite.SuiteClasses({
//        org.alfresco.module.org_alfresco_module_rm.test.system.DataLoadSystemTest.class,
//        org.alfresco.module.org_alfresco_module_rm.test.system.NotificationServiceHelperSystemTest.class,
        org.alfresco.module.org_alfresco_module_rm.script.BootstrapTestDataGet.class,
        org.alfresco.module.org_alfresco_module_rm.recorded.version.config.BaseRecordedVersionConfigTest.class,
        org.alfresco.module.org_alfresco_module_rm.version.TestRecordableVersionServiceImpl.class,
        org.alfresco.module.org_alfresco_module_rm.api.PublicAPITestUtil.class
})
public class Ags03TestSuite
{
}
