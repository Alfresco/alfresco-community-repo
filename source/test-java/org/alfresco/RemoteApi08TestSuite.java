
package org.alfresco;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * See {@link RemoteApi01TestSuite}
 *
 * @author Jamal Kaabi-Mofrad
 */
public class RemoteApi08TestSuite extends TestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        RemoteApi01TestSuite.tests8(suite);
        return suite;
    }
}