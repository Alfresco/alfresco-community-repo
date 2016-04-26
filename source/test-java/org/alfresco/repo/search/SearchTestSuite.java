package org.alfresco.repo.search;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.search.impl.lucene.ADMLuceneCategoryTest;
import org.alfresco.repo.search.impl.lucene.ADMLuceneTest;
import org.alfresco.repo.search.impl.lucene.ALF947Test;
import org.alfresco.repo.search.impl.lucene.LuceneIndexBackupComponentTest;
import org.alfresco.repo.search.impl.lucene.MultiReaderTest;
import org.alfresco.repo.search.impl.lucene.index.IndexInfoTest;
import org.alfresco.repo.search.impl.parsers.CMISTest;
import org.alfresco.repo.search.impl.parsers.CMIS_FTSTest;
import org.alfresco.repo.search.impl.parsers.FTSTest;
import org.alfresco.util.NumericEncodingTest;

/**
 * @author Andy Hind
 *
 */
public class SearchTestSuite extends TestSuite
{

    /**
     * Creates the test suite
     * 
     * @return  the test suite
     */
    public static Test suite() 
    {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(MLAnaysisModeExpansionTest.class);
        suite.addTestSuite(QueryRegisterComponentTest.class);
        suite.addTestSuite(SearcherComponentTest.class);
        suite.addTestSuite(SearchServiceTest.class);
        suite.addTestSuite(DocumentNavigatorTest.class);
        suite.addTestSuite(ADMLuceneCategoryTest.class);
        suite.addTestSuite(ADMLuceneTest.class);
        suite.addTestSuite(ALF947Test.class);
        suite.addTestSuite(LuceneIndexBackupComponentTest.class);
        suite.addTestSuite(MultiReaderTest.class);
        suite.addTestSuite(NumericEncodingTest.class);
        suite.addTestSuite(IndexInfoTest.class);
        suite.addTestSuite(CMIS_FTSTest.class);
        suite.addTestSuite(CMISTest.class);
        suite.addTestSuite(FTSTest.class);

        return suite;
    }
}
