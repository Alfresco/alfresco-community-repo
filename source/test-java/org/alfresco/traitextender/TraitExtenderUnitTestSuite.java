
package org.alfresco.traitextender;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

public class TraitExtenderUnitTestSuite
{
    /**
     * Creates the test suite
     *
     * @return the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        unitTests(suite);
        return suite;
    }

    static void unitTests(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(org.alfresco.traitextender.TraitExtenderIntegrationTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.traitextender.AJExtensionsCompileTest.class));
    }
}
