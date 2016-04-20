package org.alfresco.repo.audit;

import org.alfresco.repo.audit.access.AccessAuditorTest;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Suite for audit-related tests.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class AuditTestSuite extends TestSuite
{
    public static Test suite() 
    {
        TestSuite suite = new TestSuite();
        
        suite.addTestSuite(AuditableAnnotationTest.class);
        suite.addTestSuite(AuditableAspectTest.class);
        suite.addTestSuite(AuditBootstrapTest.class);
        suite.addTestSuite(AuditComponentTest.class);
        suite.addTestSuite(UserAuditFilterTest.class);
        suite.addTestSuite(AuditMethodInterceptorTest.class);
        
        suite.addTest(new JUnit4TestAdapter(PropertyAuditFilterTest.class));
        suite.addTest(new JUnit4TestAdapter(AccessAuditorTest.class));
                
        return suite;
    }
}
