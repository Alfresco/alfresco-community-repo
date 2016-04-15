package org.alfresco;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * See {@link Repository01TestSuite}
 *
 * @author Alan Davis
 */
public class Repository23TestSuite extends TestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        Repository01TestSuite.tests23(suite);
        return suite;
    }
}
