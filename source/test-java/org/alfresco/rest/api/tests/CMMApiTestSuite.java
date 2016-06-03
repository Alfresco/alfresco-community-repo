
package org.alfresco.rest.api.tests;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Jamal Kaabi-Mofrad
 */
public class CMMApiTestSuite extends TestSuite
{
    /**
     * Creates the test suite
     *
     * @return the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(new JUnit4TestAdapter(TestCustomModel.class));
        suite.addTest(new JUnit4TestAdapter(TestCustomTypeAspect.class));
        suite.addTest(new JUnit4TestAdapter(TestCustomProperty.class));
        suite.addTest(new JUnit4TestAdapter(TestCustomConstraint.class));
        suite.addTest(new JUnit4TestAdapter(TestCustomModelExport.class));

        return suite;
    }
}
