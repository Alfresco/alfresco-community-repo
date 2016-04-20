
package org.alfresco;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All Repository project UNIT test classes should be added to this test suite.
 */
public class Repository70TestSuite extends TestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        Repository01TestSuite.tests70(suite);
        return suite;
    }
}
