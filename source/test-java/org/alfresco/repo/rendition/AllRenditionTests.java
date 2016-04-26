package org.alfresco.repo.rendition;

import org.alfresco.repo.rendition.executer.HTMLRenderingEngineTest;
import org.alfresco.repo.thumbnail.ThumbnailServiceImplParameterTest;
import org.alfresco.repo.thumbnail.ThumbnailServiceImplTest;
import org.alfresco.repo.thumbnail.conditions.NodeEligibleForRethumbnailingEvaluatorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This class is a holder for the various test classes associated with the Rendition Service.
 * It is not (at the time of writing) intended to be incorporated into the automatic build
 * which will find the various test classes and run them individually.
 * 
 * @author Neil McErlean
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        RenditionServiceImplTest.class,
        ThumbnailServiceImplParameterTest.class,
        ThumbnailServiceImplTest.class,
        NodeEligibleForRethumbnailingEvaluatorTest.class,
        StandardRenditionLocationResolverTest.class,
        RenditionServiceIntegrationTest.class,
        RenditionServicePermissionsTest.class,
        RenditionNodeManagerTest.class,
        HTMLRenderingEngineTest.class,
        MultiUserRenditionTest.class
})
public class AllRenditionTests
{
    // Intentionally empty
}
