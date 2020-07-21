/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
 *
 * <p>
 *    All of the tests are using Spring annotations to load full application context, see BaseSpringTest
 *    Any new tests included in this test suite must follow the same pattern
 * </p>
 */
@RunWith(Categories.class)
@Categories.ExcludeCategory({DBTests.class, NonBuildTests.class})
@Suite.SuiteClasses({
    // Requires a running ActiveMQ
    org.alfresco.repo.rawevents.EventBehaviourTest.class,
    org.alfresco.repo.rawevents.TransactionAwareEventProducerTest.class,
    org.alfresco.repo.event2.RepoEvent2ITSuite.class,

    // Requires running transformers
    org.alfresco.transform.client.registry.LocalTransformServiceRegistryConfigTest.class,
    org.alfresco.repo.rendition2.RenditionService2IntegrationTest.class,
    org.alfresco.repo.rendition2.LocalTransformServiceRegistryIntegrationTest.class,
    org.alfresco.repo.rendition2.LocalTransformClientIntegrationTest.class,
    org.alfresco.repo.rendition2.LegacyTransformServiceRegistryIntegrationTest.class,
    org.alfresco.repo.rendition2.LegacyTransformClientIntegrationTest.class,
    org.alfresco.repo.rendition2.LocalRenditionTest.class,
    org.alfresco.repo.rendition2.LegacyRenditionTest.class,
    org.alfresco.repo.rendition2.LegacyLocalRenditionTest.class,
    org.alfresco.repo.rendition2.NoneRenditionTest.class,

    org.alfresco.repo.solr.SOLRTrackingComponentTest.class,
    org.alfresco.repo.tagging.TaggingServiceImplTest.class,
    org.alfresco.repo.transaction.AlfrescoTransactionSupportTest.class,
    org.alfresco.repo.transaction.RetryingTransactionHelperTest.class,
    org.alfresco.repo.transaction.TransactionAwareSingletonTest.class,
    org.alfresco.repo.transaction.TransactionServiceImplTest.class,
    org.alfresco.repo.oauth1.OAuth1CredentialsStoreServiceTest.class,
    org.alfresco.repo.oauth2.OAuth2CredentialsStoreServiceTest.class,
    org.alfresco.repo.template.TemplateServiceImplTest.class,
    org.alfresco.repo.tenant.MultiTServiceImplTest.class,
    org.alfresco.repo.search.SearcherComponentTest.class,
    org.alfresco.repo.blog.BlogServiceImplTest.class,
    org.alfresco.repo.action.scheduled.ScheduledPersistedActionServiceTest.class,

    org.alfresco.repo.rendition2.RenditionDefinitionTest.class
})
public class AppContext06TestSuite
{
}
