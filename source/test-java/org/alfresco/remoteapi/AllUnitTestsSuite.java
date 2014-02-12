package org.alfresco.remoteapi;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.web.util.PagingCursorTest;
import org.alfresco.repo.web.util.paging.PagingTest;
import org.alfresco.repo.webdav.GetMethodTest;
import org.alfresco.repo.webdav.LockInfoImplTest;
import org.alfresco.repo.webdav.RenameShuffleDetectionTest;
import org.alfresco.repo.webdav.WebDAVHelperTest;
import org.alfresco.repo.webdav.WebDAVLockServiceImplTest;
import org.alfresco.rest.framework.tests.core.AllRestFrameworkTest;
import org.alfresco.rest.framework.tests.metadata.WriterTests;

/**
 * All Remote API project UNIT test classes should be added to this test suite.
 */
public class AllUnitTestsSuite extends TestSuite
{
    /**
     * Creates the test suite
     *
     * @return  the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        publicApiTests(suite);
        webdavTests(suite);
        pagingTests(suite);

        return suite;
    }
    
    static void publicApiTests(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(AllRestFrameworkTest.class));
        suite.addTest(new JUnit4TestAdapter(WriterTests.class));
    }
    
    static void webdavTests(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(GetMethodTest.class));
        suite.addTest(new JUnit4TestAdapter(LockInfoImplTest.class));
        suite.addTest(new JUnit4TestAdapter(RenameShuffleDetectionTest.class));
        suite.addTest(new JUnit4TestAdapter(WebDAVHelperTest.class));
        suite.addTest(new JUnit4TestAdapter(WebDAVLockServiceImplTest.class));
    }
    
    static void pagingTests(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(PagingTest.class));
        suite.addTest(new JUnit4TestAdapter(PagingCursorTest.class));
    }
}
