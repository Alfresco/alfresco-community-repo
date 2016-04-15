package org.alfresco;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * See {@link Repository01TestSuite}
 *
 * @author Alan Davis
 */
public class Repository63TestSuite extends TestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        Repository01TestSuite.tests63(suite);
        return suite;
    }
}
