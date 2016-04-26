
package org.alfresco.repo.rating;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.rating.Rating;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * An implementation of {@link AbstractRatingRollupAlgorithm} which calculates the count (number) of all
 * ratings in a given scheme.
 * 
 * @author Neil McErlean
 * @since 3.5
 */
public class RatingCountRollupAlgorithm extends AbstractRatingRollupAlgorithm
{
    public static final String ROLLUP_NAME = "Count";
    
    public RatingCountRollupAlgorithm()
    {
        super(ROLLUP_NAME);
    }
    
    public Integer recalculate(NodeRef ratedNode)
    {
        int result = 0;

        // If the node is not rateable, then it has no ratings in any scheme.
        if (nodeService.hasAspect(ratedNode, ContentModel.ASPECT_RATEABLE))
        {
            List<ChildAssociationRef> ratingsNodes = ratingServiceImpl.getRatingNodeChildren(ratedNode, ratingSchemeName, null);
            // Filter by scheme
            for (ChildAssociationRef chAssRef : ratingsNodes)
            {
                NodeRef nextRatingNode = chAssRef.getChildRef();
                Rating rating = ratingServiceImpl.getRatingFrom(nextRatingNode);
                if (ratingSchemeName.equals(rating.getScheme().getName()))
                {
                    result++;
                }
            }
        }
        
        return result;
    }
}
