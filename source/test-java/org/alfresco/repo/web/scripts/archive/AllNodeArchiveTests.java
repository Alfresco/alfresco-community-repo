package org.alfresco.repo.web.scripts.archive;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This class is a holder for the various test classes associated with the Node Archive Service.
 * It is not (at the time of writing) intended to be incorporated into the automatic build
 * which will find the various test classes and run them individually.
 * 
 * @author Neil McErlean
 * @since 3.5
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    NodeArchiveServiceRestApiTest.class
})
public class AllNodeArchiveTests
{
    // Intentionally empty
}
