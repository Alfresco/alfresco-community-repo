/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.integration;

import org.alfresco.module.org_alfresco_module_rm.test.integration.disposition.DispositionTestSuite;
import org.alfresco.module.org_alfresco_module_rm.test.integration.dod.DoD5015TestSuite;
import org.alfresco.module.org_alfresco_module_rm.test.integration.event.EventTestSuite;
import org.alfresco.module.org_alfresco_module_rm.test.integration.hold.HoldTestSuite;
import org.alfresco.module.org_alfresco_module_rm.test.integration.issue.IssueTestSuite;
import org.alfresco.module.org_alfresco_module_rm.test.integration.job.JobTestSuite;
import org.alfresco.module.org_alfresco_module_rm.test.integration.record.RecordTestSuite;
import org.alfresco.module.org_alfresco_module_rm.test.integration.recordfolder.RecordFolderTestSuite;
import org.alfresco.module.org_alfresco_module_rm.test.integration.report.ReportTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


/**
 * RM Integration Test Suite
 *
 * @author Roy Wetherall
 * @since 2.2
 */
@RunWith(Suite.class)
@SuiteClasses(
{
    DoD5015TestSuite.class,
    IssueTestSuite.class,
    EventTestSuite.class,
    ReportTestSuite.class,
    DispositionTestSuite.class,
    RecordTestSuite.class,
    RecordFolderTestSuite.class,
    JobTestSuite.class,
    HoldTestSuite.class
})
public class IntegrationTestSuite
{
}
