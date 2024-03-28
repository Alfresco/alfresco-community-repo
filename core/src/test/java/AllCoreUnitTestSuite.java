/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Categories.class)
@Suite.SuiteClasses({
        org.alfresco.config.SystemPropertiesSetterBeanTest.class,
        org.alfresco.encryption.AlfrescoKeyStoreTest.class,
        org.alfresco.encryption.EncryptingOutputStreamTest.class,
        org.alfresco.error.AlfrescoRuntimeExceptionTest.class,
        org.alfresco.query.CannedQueryTest.class,
        org.alfresco.util.BridgeTableTest.class,
        org.alfresco.util.CachingDateFormatTest.class,
        org.alfresco.util.DynamicallySizedThreadPoolExecutorTest.class,
        org.alfresco.util.EqualsHelperTest.class,
        org.alfresco.util.GuidTest.class,
        org.alfresco.util.ISO8601DateFormatTest.class,
        org.alfresco.util.LogAdapterTest.class,
        org.alfresco.util.LogTeeTest.class,
        org.alfresco.util.PathMapperTest.class,
        org.alfresco.util.TempFileProviderTest.class,
        org.alfresco.util.VersionNumberTest.class,
        org.alfresco.util.collections.CollectionUtilsTest.class,
        org.alfresco.util.exec.ExecParameterTokenizerTest.class,
        org.alfresco.util.exec.RuntimeExecBeansTest.class,
        org.alfresco.util.exec.RuntimeExecTest.class,
        org.alfresco.util.random.NormalDistributionHelperTest.class,
        org.alfresco.util.shard.ExplicitShardingPolicyTest.class,
        org.alfresco.util.transaction.SpringAwareUserTransactionTest.class
})
public class AllCoreUnitTestSuite {
}
