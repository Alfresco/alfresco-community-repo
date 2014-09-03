package org.alfresco.web;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.web.app.ResourceBundleWrapperTest;
import org.alfresco.web.app.servlet.AuthenticationFilterTest;
import org.alfresco.web.config.WebClientConfigTest;

/**
 * All Alfresco web client project UNIT test classes should be added to this test suite.
 */
public class AllUnitTestsSuite extends TestSuite
{
    /**
     * Creates the test suite
     *
     * @return  the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new JUnit4TestAdapter(AuthenticationFilterTest.class));
        suite.addTestSuite(ResourceBundleWrapperTest.class);
        suite.addTestSuite(WebClientConfigTest.class);
        return suite;
    }
 
}
