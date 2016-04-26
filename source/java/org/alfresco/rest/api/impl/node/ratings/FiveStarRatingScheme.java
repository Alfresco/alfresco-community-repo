package org.alfresco.rest.api.impl.node.ratings;

import org.alfresco.rest.api.model.DocumentRatingSummary;
import org.alfresco.rest.api.model.FiveStarRatingSummary;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.cmr.rating.RatingServiceException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The rest apis representation of the 'fiveStar' rating scheme.
 * 
 * @author steveglover
 *
 */
public class FiveStarRatingScheme extends AbstractRatingScheme
{
	public FiveStarRatingScheme()
	{
		super("fiveStar", "fiveStarRatingScheme");
	}

	public Float getRatingServiceRating(Object rating)
	{
		Float ratingToApply = null;

		if(rating instanceof Integer)
		{
			ratingToApply = ((Integer)rating).floatValue();
		}
		else
		{
			throw new InvalidArgumentException("Rating should be non-null and an integer for 'fiveStar' rating scheme.");
		}

		validateRating(ratingToApply);
		
		return ratingToApply;
	}
	
	public Object getApiRating(Float rating)
	{
		Object apiRating = Integer.valueOf(rating.intValue());
		return apiRating;
	}

	public DocumentRatingSummary getDocumentRatingSummary(NodeRef nodeRef)
	{
		return new FiveStarRatingSummary(ratingService.getRatingsCount(nodeRef, ratingSchemeName),
    			ratingService.getTotalRating(nodeRef, ratingSchemeName),
    			ratingService.getAverageRating(nodeRef, ratingSchemeName));
	}

	@Override
	public void applyRating(NodeRef nodeRef, Object rating)
	{
		try
		{
			Float ratingServiceRating = getRatingServiceRating(rating);
			ratingService.applyRating(nodeRef, ratingServiceRating, getRatingServiceName());
		}
		catch(RatingServiceException e)
		{
			throw new InvalidArgumentException(e.getMessage());
		}
	}

	@Override
	public void removeRating(NodeRef nodeRef)
	{
		try
		{
			ratingService.removeRatingByCurrentUser(nodeRef, getRatingServiceName());
		}
		catch(RatingServiceException e)
		{
			throw new InvalidArgumentException(e.getMessage());
		}
	}
}
