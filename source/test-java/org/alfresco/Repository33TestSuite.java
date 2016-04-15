package org.alfresco;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * See {@link Repository01TestSuite}
 *
 * @author Alan Davis
 */
public class Repository33TestSuite extends TestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        Repository01TestSuite.tests33(suite);
        return suite;
    }
}
