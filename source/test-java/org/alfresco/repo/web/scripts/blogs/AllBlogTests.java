package org.alfresco.repo.web.scripts.blogs;

import org.alfresco.repo.blog.BlogServiceImplTest;
import org.alfresco.service.cmr.blog.BlogService;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This class is a holder for the various test classes associated with the {@link BlogService}.
 * It is not (at the time of writing) intended to be incorporated into the automatic build
 * which will find the various test classes and run them individually.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    BlogServiceImplTest.class,
    BlogServiceTest.class
})
public class AllBlogTests
{
    // Intentionally empty
}
