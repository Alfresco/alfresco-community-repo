package org.alfresco.repo.web.scripts.site;

import org.alfresco.repo.activities.SiteActivityTestCaseSensitivity;
import org.alfresco.repo.activities.SiteActivityTestCaseInsensitivity;
import org.alfresco.repo.site.SiteServiceImplMoreTest;
import org.alfresco.repo.site.SiteServiceImplTest;
import org.alfresco.service.cmr.site.SiteService;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This class is a holder for the various test classes associated with the {@link SiteService}.
 * It is not (at the time of writing) intended to be incorporated into the automatic build
 * which will find the various test classes and run them individually.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    SiteServiceImplTest.class,
    SiteServiceImplMoreTest.class,
    SiteServiceTest.class,
    SiteExportServiceTest.class,
    SiteActivityTestCaseSensitivity.class,
    SiteActivityTestCaseInsensitivity.class
})
public class AllSiteTests
{
    // Intentionally empty
}
