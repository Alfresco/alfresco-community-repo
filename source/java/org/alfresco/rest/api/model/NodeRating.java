package org.alfresco.rest.api.model;

import java.util.Date;

import org.alfresco.rest.framework.resource.UniqueId;

/**
 * Represents a node rating.
 * 
 * @author steveglover
 *
 */
public class NodeRating implements Comparable<NodeRating>
{
	private String ratingSchemeId;
	private Object myRating;
    private Date ratedAt;
	private DocumentRatingSummary documentRatingSummary;

	public NodeRating()
	{
	}

	public NodeRating(String ratingSchemeId, Object myRating, Date ratedAt, DocumentRatingSummary documentRatingSummary)
	{
		if(ratingSchemeId == null)
		{
			throw new IllegalArgumentException();
		}

		this.ratingSchemeId = ratingSchemeId;
		this.documentRatingSummary = documentRatingSummary;
		this.myRating = myRating;
		this.ratedAt = ratedAt;
	}

	@UniqueId
	public String getScheme()
	{
		return ratingSchemeId;
	}

	public void setScheme(String ratingSchemeId)
	{
		if(ratingSchemeId == null)
		{
			throw new IllegalArgumentException();
		}
		this.ratingSchemeId = ratingSchemeId;
	}

	public Date getRatedAt()
	{
		return ratedAt;
	}

	public Object getMyRating()
	{
		return myRating;
	}

	public void setMyRating(Object myRating)
	{
		this.myRating = myRating;
	}
	
	public DocumentRatingSummary getAggregate()
	{
		return documentRatingSummary;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ratingSchemeId == null) ? 0 : ratingSchemeId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeRating other = (NodeRating) obj;
		if (ratingSchemeId == null) {
			if (other.getScheme() != null)
				return false;
		} else if (!ratingSchemeId.equals(other.getScheme()))
			return false;
		return true;
	}

	@Override
	public int compareTo(NodeRating other)
	{
		if(other != null)
		{
			int ret = (ratingSchemeId.compareTo(other.getScheme()));
			return ret;
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}

	@Override
	public String toString()
	{
		return "NodeRating [scheme=" + ratingSchemeId + ", myRating=" + myRating
				+ ", ratedAt=" + ratedAt
				+ ", documentRatingSummary=" + documentRatingSummary + "]";
	}
}
