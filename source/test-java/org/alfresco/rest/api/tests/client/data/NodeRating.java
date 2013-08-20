package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.rest.api.tests.PublicApiDateFormat;
import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class NodeRating implements Serializable, Comparable<NodeRating>, ExpectedComparison
{
	private static final long serialVersionUID = 2855422270577730898L;

	private String nodeId;
	private String ratingScheme;
	private String ratedAt;
	private Object myRating;
	private Aggregate aggregate;

	public NodeRating(String ratingScheme, Object myRating)
	{
		this.ratingScheme = ratingScheme;
		this.myRating = myRating;
	}

	public NodeRating(String ratingScheme, Object myRating, Aggregate aggregate)
	{
		this.ratingScheme = ratingScheme;
		this.myRating = myRating;
		this.aggregate = aggregate;
	}

	public NodeRating(String nodeId, String ratingScheme, String ratedAt, Object myRating, Aggregate aggregate)
	{
		this.nodeId = nodeId;
		this.ratingScheme = ratingScheme;
		this.ratedAt = ratedAt;
		this.myRating = myRating;
		this.aggregate = aggregate;
	}
	
	public Aggregate getAggregate()
	{
		return aggregate;
	}

	public Object getMyRating()
	{
		return myRating;
	}

	public String getNodeId()
	{
		return nodeId;
	}

	public String getId()
	{
		return ratingScheme;
	}
	
	public String getRatedAt()
	{
		return ratedAt;
	}
	
	@Override
	public String toString()
	{
		return "NodeRating [ratingScheme=" + ratingScheme + ", ratedAt="
				+ ratedAt + "]";
	}
	
	public static NodeRating parseNodeRating(String nodeId, JSONObject jsonObject)
	{
		String ratingScheme = (String)jsonObject.get("id");
		String ratedAt = (String)jsonObject.get("ratedAt");
		Object myRating = jsonObject.get("myRating");
		
		JSONObject aggregateJSON = (JSONObject)jsonObject.get("aggregate");
		Long numRatings = (Long)aggregateJSON.get("numberOfRatings");
		Double average = (Double)aggregateJSON.get("average");
		Aggregate aggregate = new Aggregate(numRatings != null ? numRatings.intValue() : null, average != null ? average.floatValue(): null);
		NodeRating nodeRating = new NodeRating(nodeId, ratingScheme, ratedAt, myRating, aggregate);
		return nodeRating;
	}

	public static ListResponse<NodeRating> parseNodeRatings(String nodeId, JSONObject jsonObject)
	{
		List<NodeRating> nodeRatings = new ArrayList<NodeRating>();

		JSONObject jsonList = (JSONObject)jsonObject.get("list");
		assertNotNull(jsonList);

		JSONArray jsonEntries = (JSONArray)jsonList.get("entries");
		assertNotNull(jsonEntries);

		for(int i = 0; i < jsonEntries.size(); i++)
		{
			JSONObject jsonEntry = (JSONObject)jsonEntries.get(i);
			JSONObject entry = (JSONObject)jsonEntry.get("entry");
			nodeRatings.add(NodeRating.parseNodeRating(nodeId, entry));
		}

		ExpectedPaging paging = ExpectedPaging.parsePagination(jsonList);

		ListResponse<NodeRating> resp = new ListResponse<NodeRating>(paging, nodeRatings);
		return resp;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject nodeRatingJSON = new JSONObject();

		nodeRatingJSON.put("myRating", getMyRating());
		nodeRatingJSON.put("id", getId());

		return nodeRatingJSON;
	}

	public static class Aggregate implements ExpectedComparison
	{
		private Integer numberOfRatings;
		private Float average;

		public Aggregate(Integer numberOfRatings, Float average)
		{
			super();
			this.numberOfRatings = numberOfRatings;
			this.average = average;
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
			return "Aggregate [numberOfRatings=" + numberOfRatings
					+ ", average=" + average + "]";
		}

		@Override
		public void expected(Object o)
		{
			assertTrue(o instanceof Aggregate);
			
			Aggregate other = (Aggregate)o;

			AssertUtil.assertEquals("numberOfRatings", numberOfRatings, other.getNumberOfRatings());
			AssertUtil.assertEquals("average", average, other.getAverage());
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
			Aggregate other = (Aggregate) obj;
			if (average == null) {
				if (other.average != null)
					return false;
			} else if (!average.equals(other.average))
				return false;
			if (numberOfRatings == null) {
				if (other.numberOfRatings != null)
					return false;
			} else if (!numberOfRatings.equals(other.numberOfRatings))
				return false;
			return true;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
		result = prime * result
				+ ((ratingScheme == null) ? 0 : ratingScheme.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeRating other = (NodeRating) obj;
		if (nodeId == null) {
			if (other.nodeId != null)
				return false;
		} else if (!nodeId.equals(other.nodeId))
			return false;
		if (ratingScheme == null) {
			if (other.ratingScheme != null)
				return false;
		} else if (!ratingScheme.equals(other.ratingScheme))
			return false;
		return true;
	}

	@Override
	public void expected(Object o)
	{
		assertTrue(o instanceof NodeRating);
		
		NodeRating other = (NodeRating)o;
		
		AssertUtil.assertEquals("nodeId", nodeId, other.getNodeId());
		AssertUtil.assertEquals("ratingScheme", ratingScheme, other.getId());

		DateFormat dateFormat = PublicApiDateFormat.getDateFormat();
		try
		{
			if(getRatedAt() != null)
			{
				Date date1 = dateFormat.parse(getRatedAt());
				Date date2 = dateFormat.parse(other.getRatedAt());
				assertTrue(date2.equals(date1) || date2.after(date1));
			}
		}
		catch (ParseException e)
		{
			throw new RuntimeException(e);
		}

		AssertUtil.assertEquals("myRating", myRating, other.getMyRating());
		if(aggregate != null)
		{
			aggregate.expected(other.getAggregate());
		}
	}

	@Override
	public int compareTo(NodeRating other) 
	{
		return ratingScheme.compareTo(other.getId());
	}
}