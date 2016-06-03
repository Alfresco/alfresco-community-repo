package org.alfresco.rest.api.impl.node.ratings;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.rest.api.model.DocumentRatingSummary;
import org.alfresco.rest.api.model.LikesRatingSummary;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.cmr.rating.RatingServiceException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * The rest api representation of the 'likes' rating scheme.
 * 
 * @author steveglover
 *
 */
public class LikesRatingScheme extends AbstractRatingScheme
{
	public LikesRatingScheme()
	{
		super("likes", "likesRatingScheme");
	}
	
	public Float getRatingServiceRating(Object rating)
	{
		Float ratingToApply = null;

		if(rating instanceof Boolean)
		{
    		boolean liked = ((Boolean)rating).booleanValue();
    		ratingToApply = Float.valueOf((liked ? 1.0f : 0.0f));	    			
		}
		else
		{
			throw new InvalidArgumentException("Rating should be non-null and a boolean for 'likes' rating scheme.");
		}

		return ratingToApply;
	}
	
	@Override
	public void applyRating(NodeRef nodeRef, Object rating)
	{
		try
		{
			Float ratingServiceRating = getRatingServiceRating(rating);
			ratingService.applyRating(nodeRef, ratingServiceRating, getRatingServiceName());

			QName nodeType = nodeService.getType(nodeRef);
            boolean isContainer = dictionaryService.isSubClass(nodeType, ContentModel.TYPE_FOLDER) &&
                    !dictionaryService.isSubClass(nodeType, ContentModel.TYPE_SYSTEM_FOLDER);
			postActivity(nodeRef, isContainer ? ActivityType.FOLDER_LIKED : ActivityType.FILE_LIKED);
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

	@Override
	protected Object getApiRating(Float rating)
	{
		Object apiRating = null;
		if(rating == 1.0f)
		{
			apiRating = true;
		}
		else if(rating == 0.0f)
		{
			apiRating = false;
		}
		else
		{
			throw new InvalidArgumentException("Rating is invalid.");
		}

		return apiRating;
	}
	
	@Override
	protected DocumentRatingSummary getDocumentRatingSummary(NodeRef nodeRef)
	{
		return new LikesRatingSummary(ratingService.getRatingsCount(nodeRef, ratingSchemeName));
	}

}
