import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Categories.class)
@Suite.SuiteClasses({
        org.alfresco.config.SystemPropertiesSetterBeanTest.class,
        org.alfresco.encryption.AlfrescoKeyStoreTest.class,
        org.alfresco.encryption.EncryptingOutputStream.class,
        org.alfresco.error.AlfrescoRuntimeExceptionTest.class,
        org.alfresco.query.CannedQueryTest.class,
        org.alfresco.util.collections.CollectionUtilsTest.class,
        org.alfresco.util.exec.ExecParameterTokenizerTest.class,
        org.alfresco.util.exec.RuntimeExecBeansTest.class,
        org.alfresco.util.exec.RuntimeExecTest.class,
        org.alfresco.util.random.NormalDistributionHelperTest.class,
        org.alfresco.util.shard.ExplicitShardingPolicyTest.class,
        org.alfresco.util.transaction.SpringAwareUserTransactionTest.class,
        org.alfresco.util.BaseTest.class,
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
        org.alfresco.util.VersionNumberTest.class
})
public class AllCoreUnitTestSuite {
}
