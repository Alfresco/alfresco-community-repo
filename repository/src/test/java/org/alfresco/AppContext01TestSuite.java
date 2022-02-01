/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco;

import org.alfresco.util.testing.category.DBTests;
import org.alfresco.util.testing.category.NonBuildTests;
import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Repository project tests using the main context alfresco/application-context.xml.
 * To balance test jobs tests using this context have been split into multiple test suites.
 * Tests marked as DBTests are automatically excluded and are run as part of {@link AllDBTestsTestSuite}.
 */
@RunWith(Categories.class)
@Categories.ExcludeCategory({DBTests.class, NonBuildTests.class})
@Suite.SuiteClasses({
    org.alfresco.repo.action.evaluator.IsSubTypeEvaluatorTest.class,
    org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluatorTest.class,
    org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluatorTest.class,
    org.alfresco.repo.action.evaluator.HasAspectEvaluatorTest.class,
    org.alfresco.repo.action.executer.SetPropertyValueActionExecuterTest.class,
    org.alfresco.repo.action.executer.AddFeaturesActionExecuterTest.class,

    org.alfresco.repo.action.executer.ContentMetadataExtracterTest.class,
    org.alfresco.repo.action.executer.ContentMetadataExtracterTagMappingTest.class,
    org.alfresco.repo.action.executer.ContentMetadataEmbedderTest.class,
    org.alfresco.repo.action.executer.AsynchronousExtractorTest.class,

    org.alfresco.repo.rule.RuleLinkTest.class,
    org.alfresco.repo.rule.RuleServiceCoverageTest.class,
    org.alfresco.repo.rule.RuleServiceImplTest.class,
    org.alfresco.repo.rule.RuleTypeImplTest.class,
    org.alfresco.repo.rule.ruletrigger.RuleTriggerTest.class,
    org.alfresco.repo.security.authentication.AuthenticationTest.class,
    org.alfresco.repo.action.executer.SpecialiseTypeActionExecuterTest.class,
    org.alfresco.repo.action.executer.RemoveFeaturesActionExecuterTest.class,
    org.alfresco.repo.action.ActionTrackingServiceImplTest.class,
    org.alfresco.email.server.EmailServiceImplTest.class,
    org.alfresco.email.server.EmailServerTest.class,

    // FTPServerTest fails when run from Eclipse
    org.alfresco.filesys.FTPServerTest.class,
    org.alfresco.filesys.repo.ContentDiskDriverTest.class,
    org.alfresco.filesys.repo.LockKeeperImplTest.class,
    org.alfresco.repo.activities.ActivityServiceImplTest.class,
    org.alfresco.repo.activities.feed.cleanup.FeedCleanerTestCaseInsensitivity.class,
    org.alfresco.repo.activities.SiteActivityTestCaseSensitivity.class,
    org.alfresco.repo.activities.feed.cleanup.FeedCleanerTestCaseSensitivity.class,
    org.alfresco.repo.activities.SiteActivityTestCaseInsensitivity.class,
    org.alfresco.repo.admin.registry.RegistryServiceImplTest.class
})
public class AppContext01TestSuite
{
}
