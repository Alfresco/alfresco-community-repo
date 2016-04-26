package org.alfresco.rest.api.model;

import java.util.Date;
import java.util.Map;

import org.alfresco.rest.framework.resource.UniqueId;

/**
 * Represents an activity feed entry.
 * 
 * @author steveglover
 *
 */
public class Activity implements Comparable<Activity>
{
	private Long id;
	private String networkId;
	private String siteId;
	private String feedPersonId;
	private String postPersonId;
	private Date postedAt;
	private String activityType;
	private Map<String, Object> activitySummary;

	public Activity()
	{
	}

	public Activity(Long id, String networkId, String siteId, String feedPersonId,
			String postPersonId, Date postedAt, String activityType, Map<String, Object> activitySummary)
	{
		super();
		this.id = id;
		this.networkId = networkId;
		this.siteId = siteId;
		this.feedPersonId = feedPersonId;
		this.postPersonId = postPersonId;
		this.postedAt = postedAt;
		this.activityType = activityType;
		this.activitySummary = activitySummary;
	}

	@UniqueId
	public Long getId()
	{
		return id;
	}

	public String getNetworkId()
	{
		return networkId;
	}

	public String getSiteId()
	{
		return siteId;
	}

	public String getFeedPersonId()
	{
		return feedPersonId;
	}

	public String getPostPersonId()
	{
		return postPersonId;
	}

	public String getActivityType()
	{
		return activityType;
	}

	public Date getPostedAt()
	{
		return postedAt;
	}

	public Map<String, Object> getActivitySummary()
	{
		return activitySummary;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		
		if (obj == null)
		{
			return false;
		}
		
		if (getClass() != obj.getClass())
		{
			return false;
		}

		Activity other = (Activity) obj;
		return(id.equals(other.id));
	}

	@Override
	public int compareTo(Activity activity)
	{
		long otherId = activity.getId();
		long diff = id - otherId;
		if(diff == 0)
		{
			return 0;
		}
		else
		{
			return diff < 0 ? -1 : 1;
		}
	}

	@Override
	public String toString()
	{
		return "Activity [id=" + id + ", siteId=" + siteId
				+ ", feedPersonId=" + feedPersonId + ", postPersonId=" + postPersonId
				+ ", postedAt=" + postedAt
				+ ", activityType=" + activityType + ", activitySummary="
				+ activitySummary + "]";
	}
	
}
