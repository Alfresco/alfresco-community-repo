/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

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
