
package org.alfresco.repo.virtual;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

public class VirtualizationUnitTestSuite
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
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.page.PageCollatorTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.GetChildByIdMethodTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.GetParentReferenceMethodTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.NewVirtualReferenceMethodTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.PlainReferenceParserTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.PlainStringifierTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.ProtocolTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.ReferenceTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.ResourceParameterTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.StringParameterTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.VirtualProtocolTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.store.ReferenceComparatorTest.class));

        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.ZeroReferenceParserTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.ZeroStringifierTest.class));

        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.HashStringifierTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.NodeRefRadixHasherTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.NumericPathHasherTest.class));
        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.ref.StoredPathHasherTest.class));

        suite.addTest(new JUnit4TestAdapter(org.alfresco.repo.virtual.template.VirtualQueryImplTest.class));
    }
}
