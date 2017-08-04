package org.alfresco;

import junit.framework.Test;
import junit.framework.TestSuite;

public class Repository100TestSuite_ApplicationContext_GlobalIntegrationTestContext extends TestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        AllRepositoryTestsCatalogue.applicationContext_globalIntegrationTestContext_01(suite);
        return suite;
    }
}
