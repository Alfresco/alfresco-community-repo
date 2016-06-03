package org.alfresco.repo.content.transform;

import org.alfresco.util.LogAdapterTest;
import org.alfresco.util.LogTeeTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author adavis
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
    TransformerConfigStatisticsTest.class,
    TransformerConfigLimitsTest.class,
    TransformerConfigSupportedTest.class,
    TransformerConfigPropertyTest.class,
    TransformerPropertyNameExtractorTest.class,
    TransformerPropertyGetterTest.class,
    TransformerPropertySetterTest.class,
    TransformerConfigDynamicTransformersTest.class,

    LogAdapterTest.class,
    LogTeeTest.class,
    
    TransformerLoggerTest.class,
    TransformerLogTest.class,
    TransformerDebugLogTest.class,
    TransformerDebugTest.class,
    
    TransformerConfigImplTest.class,
    TransformerConfigMBeanImplTest.class,

    TransformerSelectorImplTest.class})

/**
 * Test classes in the Transformers subsystem
 * 
 * @author Alan Davis
 */
public class TransformerConfigTestSuite
{
}
