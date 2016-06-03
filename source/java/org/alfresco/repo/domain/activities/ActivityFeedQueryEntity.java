package org.alfresco.repo.domain.activities;


/**
 * Entity bean to carry query parameters for <tt>alf_activity_feed</tt>
 *
 * @since 3.5
 */
public class ActivityFeedQueryEntity
{
    private Long minId;
    private Long maxId;
    private int maxFeedSize;
    private String feedUserId;
    private String siteNetwork;
    
    public String getNullValue()
    {
        return ActivitiesDAO.KEY_ACTIVITY_NULL_VALUE;
    }
    
    public Long getMinId()
    {
        return minId;
    }
    
    public void setMinId(Long minId)
    {
        this.minId = minId;
    }

    public Long getMaxId()
    {
		return maxId;
	}

	public void setMaxId(Long maxId)
	{
		this.maxId = maxId;
	}

    public int getMaxFeedSize()
    {
        return maxFeedSize;
    }
    
    public void setMaxFeedSize(int maxFeedSize)
    {
        this.maxFeedSize = maxFeedSize;
    }

    public String getFeedUserId()
    {
        return feedUserId;
    }
    
    public void setFeedUserId(String feedUserId)
    {
        this.feedUserId = feedUserId;
    }
    
    public String getSiteNetwork()
    {
        return siteNetwork;
    }
    
    public void setSiteNetwork(String siteNetwork)
    {
        this.siteNetwork = siteNetwork;
    }
}
