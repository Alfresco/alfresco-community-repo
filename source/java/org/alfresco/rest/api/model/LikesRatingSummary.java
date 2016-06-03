package org.alfresco.rest.api.model;

public class LikesRatingSummary implements DocumentRatingSummary
{
	private Integer numberOfRatings;

	public LikesRatingSummary(Integer numberOfRatings)
	{
		super();
		this.numberOfRatings = numberOfRatings;
	}

	public Integer getNumberOfRatings()
	{
		return numberOfRatings;
	}

	@Override
	public String toString()
	{
		return "LikesRatingSummary [numberOfRatings=" + numberOfRatings + "]";
	}

}
