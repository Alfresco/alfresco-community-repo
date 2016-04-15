package org.alfresco.rest.api.model;

public class FiveStarRatingSummary implements DocumentRatingSummary
{
	private Integer numberOfRatings;
	private Float average;

	public FiveStarRatingSummary(Integer numberOfRatings, Float ratingTotal, Float average)
	{
		super();
		this.numberOfRatings = numberOfRatings;
		this.average = (average == -1 ? null : average);
	}

	public Integer getNumberOfRatings()
	{
		return numberOfRatings;
	}

	public Float getAverage()
	{
		return average;
	}

	@Override
	public String toString()
	{
		return "FiveStarRatingSummary [numberOfRatings=" + numberOfRatings
				+ ", average=" + average + "]";
	}

}
