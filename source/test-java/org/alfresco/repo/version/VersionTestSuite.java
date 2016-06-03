package org.alfresco.repo.version;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.version.common.VersionHistoryImplTest;
import org.alfresco.repo.version.common.VersionImplTest;
import org.alfresco.repo.version.common.versionlabel.SerialVersionLabelPolicyTest;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * Version test suite
 * 
 * @author Roy Wetherall, janv
 */
public class VersionTestSuite extends TestSuite
{
    public static ApplicationContext getContext() 
    {
        ApplicationContextHelper.setUseLazyLoading(false);
        ApplicationContextHelper.setNoAutoStart(true);
        return ApplicationContextHelper.getApplicationContext(
             new String[] { "classpath:alfresco/minimal-context.xml" }
        );
    }
        
    /**
     * Creates the test suite
     * 
     * @return  the test suite
     */
    public static Test suite() 
    {
        // Setup the context
        getContext();
        
        TestSuite suite = new TestSuite();
        suite.addTestSuite(VersionImplTest.class);
        suite.addTestSuite(VersionHistoryImplTest.class);
        suite.addTestSuite(SerialVersionLabelPolicyTest.class);
        suite.addTestSuite(VersionServiceImplTest.class);
        suite.addTestSuite(NodeServiceImplTest.class);
        suite.addTestSuite(ContentServiceImplTest.class);
        return suite;
    }
}
