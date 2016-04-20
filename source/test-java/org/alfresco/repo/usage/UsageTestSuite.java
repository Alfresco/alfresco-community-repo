package org.alfresco.repo.usage;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @author Derek Hulley
 * @since V3.4 Team
 */
public class UsageTestSuite extends TestSuite
{
    public static Test suite() 
    {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(RepoUsageComponentTest.class);
        suite.addTestSuite(UserUsageTest.class);
        suite.addTestSuite(UserUsageTrackingComponentTest.class);
        return suite;
    }
}
