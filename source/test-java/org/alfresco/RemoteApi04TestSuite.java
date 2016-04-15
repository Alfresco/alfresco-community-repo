package org.alfresco;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * See {@link RemoteApi01TestSuite}
 *
 * @author Alan Davis
 */
public class RemoteApi04TestSuite extends TestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        RemoteApi01TestSuite.tests4(suite);
        return suite;
    }
}
