/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.rest.api.impl.node.ratings;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.rest.api.model.DocumentRatingSummary;
import org.alfresco.rest.api.model.NodeRating;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.rating.Rating;
import org.alfresco.service.cmr.rating.RatingService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.registry.NamedObjectRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.InitializingBean;

/**
 * Manages the mapping between the rest api's representation of a node rating and the repository's
 * representation of a node rating.
 * 
 * @author steveglover
 *
 */
public abstract class AbstractRatingScheme implements RatingScheme, InitializingBean
{
    private static Log logger = LogFactory.getLog(RatingScheme.class);  

	protected String ratingSchemeName; // for interaction with the RatingService
	protected String ratingSchemeId; // exposed through the api
	protected RatingService ratingService;
	protected NodeService nodeService;
	protected DictionaryService dictionaryService;
	protected ActivityService activityService;
	protected SiteService siteService;

	protected NamedObjectRegistry<RatingScheme> nodeRatingSchemeRegistry;

	public AbstractRatingScheme(String ratingSchemeId, String ratingSchemeName)
	{
		super();
		this.ratingSchemeName = ratingSchemeName;
		this.ratingSchemeId = ratingSchemeId;
	}
	
	public String getRatingSchemeId()
	{
		return ratingSchemeId;
	}

	public String getRatingServiceName()
	{
		return ratingSchemeName;
	}
	
    public void setNodeRatingSchemeRegistry(NamedObjectRegistry<RatingScheme> nodeRatingSchemeRegistry)
    {
		this.nodeRatingSchemeRegistry = nodeRatingSchemeRegistry;
	}

	public void setActivityService(ActivityService activityService)
	{
		this.activityService = activityService;
	}

	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	public void setNodeService(NodeService nodeService)
	{
		this.nodeService = nodeService;
	}

	public void setDictionaryService(DictionaryService dictionaryService)
	{
		this.dictionaryService = dictionaryService;
	}

	public void setRatingService(RatingService ratingService)
	{
		this.ratingService = ratingService;
	}

	protected org.alfresco.service.cmr.rating.RatingScheme getRepoRatingScheme()
	{
		return ratingService.getRatingScheme(ratingSchemeName);
	}

	protected abstract DocumentRatingSummary getDocumentRatingSummary(NodeRef nodeRef);
	protected abstract Object getApiRating(Float rating);

    @Override
    public void afterPropertiesSet() throws Exception
    {
		nodeRatingSchemeRegistry.register(ratingSchemeId, this);
		nodeRatingSchemeRegistry.register(ratingSchemeName, this);
    }
    
    public void validateRating(Float rating)
    {
		org.alfresco.service.cmr.rating.RatingScheme ratingScheme = getRepoRatingScheme();
		Float minRating = ratingScheme.getMinRating();
		Float maxRating = ratingScheme.getMaxRating();
		if(rating < minRating || rating > maxRating)
		{
			throw new InvalidArgumentException("Rating is out of bounds.");
		}
    }

	public NodeRating getNodeRating(NodeRef nodeRef)
	{
    	Rating ratingByCurrentUser = ratingService.getRatingByCurrentUser(nodeRef, ratingSchemeName);
    	Float rating = null;
    	Date appliedAt = null;

    	if(ratingByCurrentUser != null)
    	{
	    	rating = ratingByCurrentUser.getScore();
	    	appliedAt = ratingByCurrentUser.getAppliedAt();
    	}

    	Object myRating = null;
    	if(rating != null)
    	{
    		validateRating(rating);
        	myRating = getApiRating(rating);
    	}

    	DocumentRatingSummary documentRatingSummary = getDocumentRatingSummary(nodeRef);

        NodeRating nodeRating = new NodeRating(ratingSchemeId, myRating, appliedAt, documentRatingSummary);
        return nodeRating;
	}
	
    private String getSiteId(final NodeRef nodeRef)
    {
		// may not be able to read site data so run as system
		String siteId = AuthenticationUtil.runAsSystem(new RunAsWork<String>()
        {
			@Override
			public String doWork() throws Exception
			{
				String siteId = null;
		        SiteInfo siteInfo = siteService.getSite(nodeRef);
		        if(siteInfo != null)
		        {
		        	siteId = siteInfo.getShortName();
		        }
				return siteId;
			}
        });

		return siteId;
    }

    @SuppressWarnings("unchecked")
	private JSONObject getActivityData(final NodeRef nodeRef, String siteId)
    {
    	JSONObject activityData = null;

    	if(siteId != null)
    	{
            // may not be able to read these nodes, but we need to for the activity processing so run as system
    		activityData = AuthenticationUtil.runAsSystem(new RunAsWork<JSONObject>()
    		{
    			@Override
    			public JSONObject doWork() throws Exception
    			{
    				JSONObject activityData = new JSONObject();
    				activityData.put("title", nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
    				try
    				{
    					StringBuilder sb = new StringBuilder("document-details?nodeRef=");
    					sb.append(URLEncoder.encode(nodeRef.toString(), "UTF-8"));
    					activityData.put("page", sb.toString());
    				}
    				catch (UnsupportedEncodingException e)
    				{
    					logger.warn("Unable to urlencode page for create nodeRating activity");
    				}

    				return activityData;
    			}
    		});
    	}

		return activityData;
    }

	protected void postActivity(final NodeRef nodeRef, final String activityType)
    {
    	String siteId = getSiteId(nodeRef);
    	JSONObject activityData = getActivityData(nodeRef, siteId);
		if(activityData != null)
		{
			activityService.postActivity(activityType, siteId, "nodeRatings", activityData.toString());
		}
    }
}
