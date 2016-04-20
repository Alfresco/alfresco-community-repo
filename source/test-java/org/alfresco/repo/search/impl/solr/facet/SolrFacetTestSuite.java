
package org.alfresco.repo.search.impl.solr.facet;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Jamal Kaabi-Mofrad
 */
public class SolrFacetTestSuite extends TestSuite
{

    /**
     * Creates the test suite
     *
     * @return  the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(new JUnit4TestAdapter(SolrFacetQueriesDisplayHandlersTest.class));
        suite.addTest(new JUnit4TestAdapter(SolrFacetServiceImplTest.class));
        suite.addTest(new JUnit4TestAdapter(SolrFacetConfigTest.class));
        suite.addTest(new JUnit4TestAdapter(SolrFacetComparatorTest.class));

        return suite;
    }
}
