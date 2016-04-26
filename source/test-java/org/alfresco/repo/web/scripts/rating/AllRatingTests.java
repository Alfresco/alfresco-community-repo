package org.alfresco.repo.web.scripts.rating;

import org.alfresco.repo.rating.RatingServiceIntegrationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This class is a holder for the various test classes associated with the Rating Service.
 * It is not (at the time of writing) intended to be incorporated into the automatic build
 * which will find the various test classes and run them individually.
 * 
 * @author Neil McErlean
 * @since 3.4
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    RatingServiceIntegrationTest.class,
    RatingRestApiTest.class
})
public class AllRatingTests
{
    // Intentionally empty
}
