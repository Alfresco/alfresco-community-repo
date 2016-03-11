package org.alfresco.module.org_alfresco_module_rm.test;

import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.extensions.cpsuite.ClasspathSuite.ClassnameFilters;
import org.junit.runner.RunWith;

/**
 * All unit test suite.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
@RunWith(ClasspathSuite.class)
@ClassnameFilters({
    // Execute all test classes ending with "UnitTest"
    ".*UnitTest",
    // Put the test classes you want to exclude here
    "!.*FilePlanPermissionServiceImplUnitTest"
})
public class AllUnitTestSuite
{
}
