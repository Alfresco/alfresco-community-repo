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
        org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils.class,
        org.alfresco.module.org_alfresco_module_rm.test.util.TestActionPropertySubs.class,
        org.alfresco.module.org_alfresco_module_rm.test.util.TestServiceImpl.class,
        org.alfresco.module.org_alfresco_module_rm.test.util.bdt.BehaviourTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.util.TestContentCleanser.class,
        org.alfresco.module.org_alfresco_module_rm.test.util.TestAction2.class,
        org.alfresco.module.org_alfresco_module_rm.test.util.TestDmAction.class,
        org.alfresco.module.org_alfresco_module_rm.test.util.TestModel.class,
        org.alfresco.module.org_alfresco_module_rm.test.util.TestWebScriptRepoServer.class,
        org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.class,
//        org.alfresco.module.org_alfresco_module_rm.test.util.RetryingTransactionHelperBaseTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase.class,
        org.alfresco.module.org_alfresco_module_rm.test.util.TestService.class,
        org.alfresco.module.org_alfresco_module_rm.test.util.TestActionParams.class,
        org.alfresco.module.org_alfresco_module_rm.test.util.TestAction.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.disposition.CutOffTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.disposition.DispositionScheduleInheritanceTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.disposition.MultipleSchedulesTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.disposition.UpdateDispositionScheduleTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.disposition.UpdateNextDispositionActionTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.destroy.DestroyContentTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.record.InplaceRecordPermissionTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.record.CompleteRecordTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.record.RejectRecordTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.record.HideInplaceRecordTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.record.MoveInplaceRecordTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.record.UpdateRecordAspectsTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.record.DownloadAsZipRecordTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.record.ViewRecordTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.record.MoveRecordTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.record.CreateInplaceRecordTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.record.LinkRecordTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.record.CreateRecordTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.transfer.FilingPermissionsOnTransferFolderTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.transfer.CreateTransferFolderAsNonAdminUserTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.transfer.ReadPermissionsOnTransferFolderTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.transfer.NoPermissionsOnTransferFolderTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.rule.FilePlanRuleInheritanceTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.relationship.DeleteRelationshipTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.relationship.CreateRelationshipTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.version.DeclareAsRecordVersionTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.version.AdHocRecordableVersionsTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.version.DeleteRecordVersionTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.version.RecordableVersionsBaseTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.version.AutoRecordableVersionsTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.version.AutoVersionTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM452Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM1027Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM994Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM2192Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM4804Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM1030Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM1424Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM804Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM1887Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM1814Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM3450Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM1429Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM1008Test.class,
//        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM2072Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.MNT19114Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM1727Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM4101Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM5225Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM3341Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM978Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM4293Test.class,
//        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM2190Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM1799Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM1464Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.rm3314.RM3314Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.rm3314.RM3314TestListener.class,
//        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM981SystemTest.class,
//        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM3993Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM1914Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM4619Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM1463Test.class,
//        org.alfresco.module.org_alfresco_module_rm.test.integration.issue.RM4163Test.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.dod.RM1147DODRMSiteTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.dod.RM1194ExcludeDoDRecordTypesTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.report.HoldReportTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.event.CompleteEventsTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.hold.DeleteHoldTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.hold.RemoveFromHoldTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.hold.UpdateHeldActiveContentTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.hold.AddActiveContentToHoldTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.hold.AddToHoldTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.hold.CreateHoldTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.hold.RemoveActiveContentFromHoldTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.recordfolder.MoveRecordFolderTest.class,
        org.alfresco.module.org_alfresco_module_rm.test.integration.recordfolder.DeleteRecordFolderTest.class
})
public class Ags01TestSuite
{
}
