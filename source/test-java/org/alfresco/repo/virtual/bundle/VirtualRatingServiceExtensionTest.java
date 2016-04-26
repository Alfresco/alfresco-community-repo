
package org.alfresco.repo.virtual.bundle;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.virtual.VirtualizationIntegrationTest;
import org.alfresco.repo.virtual.ref.GetActualNodeRefMethod;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.cmr.rating.RatingService;
import org.alfresco.service.cmr.rating.RatingServiceException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.junit.Test;

public class VirtualRatingServiceExtensionTest extends VirtualizationIntegrationTest
{
    private final static String LIKES_RATING_SCHEME = "likesRatingScheme";

    private final static String FIVE_STAR_RATING_SCHEME = "fiveStarRatingScheme";

    private RatingService ratingService;

    private NodeRef vf1Node2;

    private NodeRef virtualContent;

    private String user1;

    private String user2;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        ratingService = VirtualPermissionServiceExtensionTest.ctx.getBean("ratingService",
                                                                          RatingService.class);

        user1 = "user1";

        user2 = "user2";

        vf1Node2 = nodeService.getChildByName(this.virtualFolder1NodeRef,
                                              ContentModel.ASSOC_CONTAINS,
                                              "Node2");

        virtualContent = createContent(vf1Node2,
                                       "virtualContent").getChildRef();
    }

    private void applyRatingAs(final NodeRef targetNode, final float rating, final String ratingSchemeName,
                String asUser) throws RatingServiceException
    {

        String fau = AuthenticationUtil.getFullyAuthenticatedUser();
        try
        {
            AuthenticationUtil.setFullyAuthenticatedUser(asUser);
            RunAsWork<Void> applyRatingsAsWork = new RunAsWork<Void>()
            {

                @Override
                public Void doWork() throws Exception
                {
                    ratingService.applyRating(targetNode,
                                              rating,
                                              ratingSchemeName);
                    return null;
                }

            };
            AuthenticationUtil.runAs(applyRatingsAsWork,
                                     asUser);
        }
        finally
        {
            AuthenticationUtil.setFullyAuthenticatedUser(fau);
        }
    }

    @Test
    public void testApplyRatings() throws Exception
    {
        assertTrue(Reference.isReference(virtualContent));
        NodeRef actualNodeRef = Reference.fromNodeRef(virtualContent).execute(new GetActualNodeRefMethod(environment));

        applyRatingAs(virtualContent,
                      1f,
                      LIKES_RATING_SCHEME,
                      user1);

        applyRatingAs(virtualContent,
                      1f,
                      LIKES_RATING_SCHEME,
                      user2);

        assertEquals(1f,
                     ratingService.getAverageRating(virtualContent,
                                                    LIKES_RATING_SCHEME));
        assertEquals(1f,
                     ratingService.getAverageRating(actualNodeRef,
                                                    LIKES_RATING_SCHEME));

        applyRatingAs(virtualContent,
                      1f,
                      FIVE_STAR_RATING_SCHEME,
                      user1);
        applyRatingAs(virtualContent,
                      3f,
                      FIVE_STAR_RATING_SCHEME,
                      user2);
        assertEquals(2f,
                     ratingService.getAverageRating(virtualContent,
                                                    FIVE_STAR_RATING_SCHEME));
        assertEquals(2f,
                     ratingService.getAverageRating(actualNodeRef,
                                                    FIVE_STAR_RATING_SCHEME));
    }
}
