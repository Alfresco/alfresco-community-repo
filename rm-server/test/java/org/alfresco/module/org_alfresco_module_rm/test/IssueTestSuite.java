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

import org.alfresco.module.org_alfresco_module_rm.test.issue.RM1008Test;
import org.alfresco.module.org_alfresco_module_rm.test.issue.RM1027Test;
import org.alfresco.module.org_alfresco_module_rm.test.issue.RM1030Test;
import org.alfresco.module.org_alfresco_module_rm.test.issue.RM1039Test;
import org.alfresco.module.org_alfresco_module_rm.test.issue.RM452Test;
import org.alfresco.module.org_alfresco_module_rm.test.issue.RM804Test;
import org.alfresco.module.org_alfresco_module_rm.test.issue.RM994Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Issue test suite
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@RunWith(Suite.class)
@SuiteClasses(
{
    RM452Test.class,
    RM994Test.class,
    RM1008Test.class,
    RM1030Test.class,
    RM1027Test.class,
    RM1039Test.class,
    RM804Test.class
})
public class IssueTestSuite
{
}
