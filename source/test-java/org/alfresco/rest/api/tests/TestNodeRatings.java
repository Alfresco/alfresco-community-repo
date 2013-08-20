package org.alfresco.rest.api.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.client.PublicApiClient.Comments;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Nodes;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiClient.People;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Activity;
import org.alfresco.rest.api.tests.client.data.Comment;
import org.alfresco.rest.api.tests.client.data.NodeRating;
import org.alfresco.rest.api.tests.client.data.Tag;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;

public class TestNodeRatings extends EnterpriseTestApi
{
	@Test
	public void testNodeRatings() throws Exception
	{
		Iterator<TestNetwork> networksIt = getTestFixture().getNetworksIt();
		assertTrue(networksIt.hasNext());
		final TestNetwork network1 = networksIt.next();
		assertTrue(networksIt.hasNext());
		final TestNetwork network2 = networksIt.next();

		final List<TestPerson> people = new ArrayList<TestPerson>(3);

		// create users
		TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				TestPerson person = network1.createUser();
				people.add(person);
				person = network1.createUser();
				people.add(person);
				return null;
			}
		}, network1.getId());
		
		TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				TestPerson person = network2.createUser();
				people.add(person);
				return null;
			}
		}, network2.getId());

		final TestPerson person11 = people.get(0);
		final TestPerson person12 = people.get(1);
		final TestPerson person21 = people.get(2);

		final List<NodeRef> nodes = new ArrayList<NodeRef>();
		final List<TestSite> sites = new ArrayList<TestSite>();

		// Create site
		TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				TestSite site = network1.createSite(SiteVisibility.PRIVATE);
				sites.add(site);

				NodeRef nodeRef = repoService.createDocument(site.getContainerNodeRef("documentLibrary"), "Test Doc 1", "Test Content");
				nodes.add(nodeRef);
				
				nodeRef = repoService.createDocument(site.getContainerNodeRef("documentLibrary"), "Test Doc 2", "Test Content");
				nodes.add(nodeRef);

				return null;
			}
		}, person11.getId(), network1.getId());

		final NodeRef nodeRef1 = nodes.get(0);		

		Comments commentsProxy = publicApiClient.comments();
		People peopleProxy = publicApiClient.people();
		Nodes nodesProxy = publicApiClient.nodes();
		DateFormat format = PublicApiDateFormat.getDateFormat();

		// Test Case cloud-1976
		// Create node ratings
		// try to add a rating to a comment
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));

			Comment comment = new Comment("Test Comment", "Test Comment");
			Comment newComment = commentsProxy.createNodeComment(nodeRef1.getId(), comment);
			NodeRating rating = new NodeRating("likes", true);
			nodesProxy.createNodeRating(newComment.getId(), rating);
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}
		
		// invalid node id
		try
		{
			NodeRating rating = new NodeRating("likes", true);
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			nodesProxy.createNodeRating(GUID.generate(), rating);
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
		// try to add a rating to a tag
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));

			Tag tag = new Tag("testTag");
			Tag newTag = nodesProxy.createNodeTag(nodeRef1.getId(), tag);
			NodeRating rating = new NodeRating("likes", true);
			nodesProxy.createNodeRating(newTag.getId(), rating);
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}

		// invalid rating scheme
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			nodesProxy.createNodeRating(nodeRef1.getId(), new NodeRating("missingRatingScheme", Double.valueOf(1.0f)));
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
		}

		// invalid rating
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			nodesProxy.createNodeRating(nodeRef1.getId(), new NodeRating("likes", Double.valueOf(2.0f)));
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
		}
		
		// invalid rating
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			nodesProxy.createNodeRating(nodeRef1.getId(), new NodeRating("fiveStar", true));
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
		}
		
		// invalid rating - can't rate own content for fiveStar
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			nodesProxy.createNodeRating(nodeRef1.getId(), new NodeRating("fiveStar", 5));
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
		}

		// valid ratings
		{
			NodeRating rating = new NodeRating("likes", true);

			Date time = new Date();

			// rate by multiple users in more than 1 network
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			NodeRating ret = nodesProxy.createNodeRating(nodeRef1.getId(), rating);
			assertEquals(rating.getMyRating(), ret.getMyRating());
			assertTrue(format.parse(ret.getRatedAt()).after(time));
			assertEquals(rating.getId(), ret.getId());
			assertEquals(new NodeRating.Aggregate(1, null), ret.getAggregate());

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person12.getId()));
			ret = nodesProxy.createNodeRating(nodeRef1.getId(), rating);
			assertEquals(rating.getMyRating(), ret.getMyRating());
			assertTrue(format.parse(ret.getRatedAt()).after(time));
			assertEquals(rating.getId(), ret.getId());
			assertEquals(new NodeRating.Aggregate(2, null), ret.getAggregate());
			
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person12.getId()));
			ret = nodesProxy.createNodeRating(nodeRef1.getId(), rating);
			assertEquals(rating.getMyRating(), ret.getMyRating());
			assertTrue(format.parse(ret.getRatedAt()).after(time));
			assertEquals(rating.getId(), ret.getId());
			assertEquals(new NodeRating.Aggregate(2, null), ret.getAggregate());
			
			// different network - unauthorized
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person21.getId()));
				nodesProxy.createNodeRating(nodeRef1.getId(), rating);
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
			}
			
			// Test Case cloud-2209
			// Test Case cloud-2220
			// Test Case cloud-1520
			// check that the node ratings are there, test paging

			{
				// person11
				
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
				
				List<NodeRating> expectedRatings = repoService.getNodeRatings(person11.getId(), network1.getId(), nodeRef1);
				
				{
					int skipCount = 0;
					int maxItems = 1;
					Paging paging = getPaging(skipCount, maxItems, expectedRatings.size(), expectedRatings.size());
	 				ListResponse<NodeRating> resp = nodesProxy.getNodeRatings(nodeRef1.getId(), createParams(paging, null));
					checkList(expectedRatings.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
				}
	
				{
					int skipCount = 1;
					int maxItems = Integer.MAX_VALUE;
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
					Paging paging = getPaging(skipCount, maxItems, expectedRatings.size(), expectedRatings.size());
					ListResponse<NodeRating> resp = nodesProxy.getNodeRatings(nodeRef1.getId(), createParams(paging, null));
					checkList(expectedRatings.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
				}
	
				{
					int skipCount = 1;
					int maxItems = expectedRatings.size();
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
					Paging paging = getPaging(skipCount, maxItems, expectedRatings.size(), expectedRatings.size());
					ListResponse<NodeRating> resp = nodesProxy.getNodeRatings(nodeRef1.getId(), createParams(paging, null));
					checkList(expectedRatings.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
				}
			}
			
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person12.getId()));

				// person12
				List<NodeRating> expectedRatings = repoService.getNodeRatings(person12.getId(), network1.getId(), nodeRef1);
	
				{
					int skipCount = 0;
					int maxItems = 1;
					Paging paging = getPaging(skipCount, maxItems, expectedRatings.size(), expectedRatings.size());
					ListResponse<NodeRating> resp = nodesProxy.getNodeRatings(nodeRef1.getId(), createParams(paging, null));
					checkList(expectedRatings.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
				}
	
				{
					int skipCount = 1;
					int maxItems = Integer.MAX_VALUE;
					Paging paging = getPaging(skipCount, maxItems, expectedRatings.size(), expectedRatings.size());
					ListResponse<NodeRating> resp = nodesProxy.getNodeRatings(nodeRef1.getId(), createParams(paging, null));
					checkList(expectedRatings.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
				}
			}

			{
				// person21
				
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person21.getId()));

				List<NodeRating> expectedRatings = Collections.emptyList();

				try
				{
					int skipCount = 0;
					int maxItems = 1;

					Paging paging = getPaging(skipCount, maxItems, expectedRatings.size(), expectedRatings.size());
					nodesProxy.getNodeRatings(nodeRef1.getId(), createParams(paging, null));
					fail();
				}
				catch(PublicApiException e)
				{
					assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
				}
			}
			
			// invalid node id
			try
			{
				int skipCount = 1;
				int maxItems = Integer.MAX_VALUE;
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
				Paging paging = getPaging(skipCount, maxItems);
				nodesProxy.getNodeRatings(GUID.generate(), createParams(paging, null));
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}
			
			// check activities have been raised for the created ratings
			repoService.generateFeed();
			
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));

				Paging paging = getPaging(0, Integer.MAX_VALUE);
				ListResponse<Activity> activities = peopleProxy.getActivities(person11.getId(), createParams(paging, null));

				boolean found = false;
				for(Activity activity : activities.getList())
				{
					String activityType = activity.getActivityType();
					if(activityType.equals(ActivityType.FILE_LIKED))
					{
						Map<String, Object> summary = activity.getSummary();
						assertNotNull(summary);
						String objectId = (String)summary.get("objectId");
						assertNotNull(objectId);
						if(nodeRef1.getId().equals(objectId))
						{
							found = true;
							break;
						}
					}
				}

				assertTrue(found);
			}
		}

		{
			// remove node rating
			
			NodeRating rating = new NodeRating("likes", null);

			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person21.getId()));
				nodesProxy.removeNodeRating(nodeRef1.getId(), rating);
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
			}

			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person12.getId()));
				nodesProxy.removeNodeRating(nodeRef1.getId(), rating);
			}

			// check list
			{
				List<NodeRating> ratings = repoService.getNodeRatings(person11.getId(), network1.getId(), nodeRef1);

				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
				Paging paging = getPaging(skipCount, maxItems, ratings.size(), ratings.size());
				ListResponse<NodeRating> resp = nodesProxy.getNodeRatings(nodeRef1.getId(), createParams(paging, null));
				checkList(ratings.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
			}
		}

		// get a node rating
		// 1977
		{
			NodeRating rating = new NodeRating("likes", true);
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));

			NodeRating expected = nodesProxy.createNodeRating(nodeRef1.getId(), rating);
			NodeRating actual = nodesProxy.getNodeRating(nodeRef1.getId(), "likes");
			expected.expected(actual);
		}

		{
			// update node rating
			NodeRating rating = new NodeRating("fiveStar", 2);

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person12.getId()));

			// create initial rating
			NodeRating createdRating = nodesProxy.createNodeRating(nodeRef1.getId(), rating);
			NodeRating updateRating = new NodeRating(createdRating.getId(), 5);

			// update - not supported
			try
			{
				nodesProxy.updateNodeRating(nodeRef1.getId(), updateRating);
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}
		}

		// Test Case cloud-1977
		// invalid methods
		{
			// get an arbitrary rating
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			ListResponse<NodeRating> resp = nodesProxy.getNodeRatings(nodeRef1.getId(), createParams(getPaging(0, Integer.MAX_VALUE), null));
			List<NodeRating> nodeRatings = resp.getList();
			assertTrue(nodeRatings.size() > 0);

			try
			{
				NodeRating rating = new NodeRating("likes", true);

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
				nodesProxy.create("nodes", nodeRef1.getId(), "ratings", "likes", rating.toJSON().toString(), "Unable to POST to a node rating");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}

			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
				nodesProxy.update("nodes", nodeRef1.getId(), "ratings", null, null, "Unable to PUT node ratings");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}
			
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
				nodesProxy.remove("nodes", nodeRef1.getId(), "ratings", null, "Unable to DELETE node ratings");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}
			
			try
			{
				NodeRating rating = nodeRatings.get(0);

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
				nodesProxy.update("nodes", nodeRef1.getId(), "ratings", rating.getId(), null, "Unable to PUT a node rating");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}
		}
	}
}
