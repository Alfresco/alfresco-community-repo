package org.alfresco.repo.content;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.content.cleanup.ContentStoreCleanerTest;
import org.alfresco.repo.content.filestore.FileContentStoreTest;
import org.alfresco.repo.content.filestore.NoRandomAccessFileContentStoreTest;
import org.alfresco.repo.content.filestore.ReadOnlyFileContentStoreTest;
import org.alfresco.repo.content.filestore.SpoofedTextContentReaderTest;
import org.alfresco.repo.content.replication.ContentStoreReplicatorTest;
import org.alfresco.repo.content.replication.ReplicatingContentStoreTest;

/**
 * Suite for content-related tests.
 * 
 * This includes all the tests that need a full context, the
 *  rest are in {@link ContentMinimalContextTestSuite}
 * 
 * @author Derek Hulley
 */
public class ContentFullContextTestSuite extends TestSuite
{
    @SuppressWarnings("unchecked")
    public static Test suite() 
    {
        TestSuite suite = new TestSuite();
        
        // These tests need a full context, at least for now
        suite.addTestSuite(ContentStoreCleanerTest.class);
        //suite.addTestSuite(CharsetFinderTest.class);
        suite.addTest(new JUnit4TestAdapter(SpoofedTextContentReaderTest.class));
        suite.addTest(new JUnit4TestAdapter(FileContentStoreTest.class));
        suite.addTest(new JUnit4TestAdapter(NoRandomAccessFileContentStoreTest.class));
        suite.addTest(new JUnit4TestAdapter(ReadOnlyFileContentStoreTest.class));
        suite.addTestSuite(ContentStoreReplicatorTest.class);
        suite.addTest(new JUnit4TestAdapter(ReplicatingContentStoreTest.class));
        suite.addTestSuite(ContentDataTest.class);
        //suite.addTestSuite(MimetypeMapTest.class);
        suite.addTestSuite(RoutingContentServiceTest.class);
        suite.addTest(new JUnit4TestAdapter(RoutingContentStoreTest.class));
        suite.addTestSuite(GuessMimetypeTest.class);
        
        try
        {
            @SuppressWarnings("rawtypes")
            Class clazz = Class.forName("org.alfresco.repo.content.routing.StoreSelectorAspectContentStoreTest");
            suite.addTestSuite(clazz);
        }
        catch (Throwable e)
        {
            // Ignore
        }
        
                
        return suite;
    }
}
