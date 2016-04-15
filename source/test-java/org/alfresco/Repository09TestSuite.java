package org.alfresco;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * See {@link Repository01TestSuite}
 *
 * @author Alan Davis
 */
public class Repository09TestSuite extends TestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        Repository01TestSuite.tests9(suite);
        return suite;
    }
}
