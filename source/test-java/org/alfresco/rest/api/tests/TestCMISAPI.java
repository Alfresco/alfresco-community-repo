package org.alfresco.rest.api.tests;

import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Public CMIS API tests.
 * 
 * @author steveglover
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestEnterpriseAtomPubTCK.class,
    TestPublicApiAtomPub10TCK.class,
    TestPublicApiAtomPub11TCK.class,
    TestPublicApiBrowser11TCK.class
})
public class TestCMISAPI
{
    @AfterClass
    public static void after() throws Exception
    {
//        EnterprisePublicApiTestFixture.cleanup();
    }
}
