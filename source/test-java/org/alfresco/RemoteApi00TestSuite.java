package org.alfresco;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Dmitry Velichkevich
 */
public class RemoteApi00TestSuite extends TestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        RemoteApi01TestSuite.tests0(suite);
        return suite;
    }
}
