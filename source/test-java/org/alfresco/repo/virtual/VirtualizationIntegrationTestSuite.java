
package org.alfresco.repo.virtual;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.virtual.bundle.FileInfoPropsComparatorTest;
import org.alfresco.repo.virtual.bundle.VirtualCheckOutCheckInServiceExtensionTest;
import org.alfresco.repo.virtual.bundle.VirtualFileFolderServiceExtensionTest;
import org.alfresco.repo.virtual.bundle.VirtualLockableAspectInterceptorExtensionTest;
import org.alfresco.repo.virtual.bundle.VirtualNodeServiceExtensionTest;
import org.alfresco.repo.virtual.bundle.VirtualPermissionServiceExtensionTest;
import org.alfresco.repo.virtual.bundle.VirtualPreferenceServiceExtensionTest;
import org.alfresco.repo.virtual.bundle.VirtualRatingServiceExtensionTest;
import org.alfresco.repo.virtual.bundle.VirtualVersionServiceExtensionTest;
import org.alfresco.repo.virtual.config.NodeRefPathExpressionTest;
import org.alfresco.repo.virtual.model.SystemTemplateLocationsConstraintTest;
import org.alfresco.repo.virtual.store.SystemVirtualizationMethodTest;
import org.alfresco.repo.virtual.store.TypeVirtualizationMethodTest;
import org.alfresco.repo.virtual.store.VirtualStoreImplTest;
import org.alfresco.repo.virtual.template.ApplyTemplateMethodTest;
import org.alfresco.repo.virtual.template.TemplateFilingRuleTest;
import org.alfresco.repo.virtual.template.TemplateResourceProcessorTest;

/**
 * @author Bogdan Horje
 */
public class VirtualizationIntegrationTestSuite extends TestSuite implements VirtualizationTest
{

    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new JUnit4TestAdapter(VirtualPreferenceServiceExtensionTest.class));
        suite.addTest(new JUnit4TestAdapter(VirtualLockableAspectInterceptorExtensionTest.class));
        suite.addTest(new JUnit4TestAdapter(VirtualVersionServiceExtensionTest.class));
        suite.addTest(new JUnit4TestAdapter(VirtualRatingServiceExtensionTest.class));
        suite.addTest(new JUnit4TestAdapter(VirtualCheckOutCheckInServiceExtensionTest.class));
        suite.addTest(new JUnit4TestAdapter(VirtualPermissionServiceExtensionTest.class));
        suite.addTest(new JUnit4TestAdapter(VirtualNodeServiceExtensionTest.class));
        suite.addTest(new JUnit4TestAdapter(VirtualFileFolderServiceExtensionTest.class));
        suite.addTest(new JUnit4TestAdapter(ApplyTemplateMethodTest.class));
        suite.addTest(new JUnit4TestAdapter(SystemTemplateLocationsConstraintTest.class));
        suite.addTest(new JUnit4TestAdapter(SystemVirtualizationMethodTest.class));
        suite.addTest(new JUnit4TestAdapter(TypeVirtualizationMethodTest.Integration.class));
        suite.addTest(new JUnit4TestAdapter(TemplateResourceProcessorTest.class));
        suite.addTest(new JUnit4TestAdapter(VirtualStoreImplTest.class));
        suite.addTest(new JUnit4TestAdapter(NodeRefPathExpressionTest.class));
        suite.addTest(new JUnit4TestAdapter(TemplateFilingRuleTest.class));
        suite.addTest(new JUnit4TestAdapter(FileInfoPropsComparatorTest.class));

        return suite;

    }
}
