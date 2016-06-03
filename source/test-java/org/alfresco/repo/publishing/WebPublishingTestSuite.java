
package org.alfresco.repo.publishing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Nick Smith
 * @since 4.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses( { ChannelServiceImplTest.class,
    PublishingEventHelperTest.class,
    ChannelServiceImplIntegratedTest.class,
    PublishingRootObjectTest.class,
//    EnvironmentImplTest.class,
    PublishingQueueImplTest.class,
    PublishingPackageSerializerTest.class,
//  PublishEventActionTest.class,
    PublishingIntegratedTest.class
    })
public class WebPublishingTestSuite
{
    //NOOP
}
