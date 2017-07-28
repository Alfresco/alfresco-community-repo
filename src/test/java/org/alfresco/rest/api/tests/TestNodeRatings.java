/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
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
import org.alfresco.rest.api.tests.client.PublicApiClient;
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
import org.alfresco.rest.api.tests.client.data.Site;
import org.alfresco.rest.api.tests.client.data.SiteContainer;
import org.alfresco.rest.api.tests.client.data.SiteImpl;
import org.alfresco.rest.api.tests.client.data.Tag;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;
import org.apache.commons.httpclient.HttpStatus;
import org.json.simple.JSONArray;
import org.junit.Test;

public class TestNodeRatings extends AbstractBaseApiTest
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
		
		PublicApiClient.Sites sitesProxy = publicApiClient.sites();
		Comments commentsProxy = publicApiClient.comments();
		People peopleProxy = publicApiClient.people();
		Nodes nodesProxy = publicApiClient.nodes();
		DateFormat format = PublicApiDateFormat.getDateFormat();

		// Create site and document

		publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));

		String siteId = "TESTSITE" + GUID.generate();
		Site site = new SiteImpl(siteId, siteId, siteId, SiteVisibility.PRIVATE.toString());
		site = sitesProxy.createSite(site);

		SiteContainer sc = sitesProxy.getSingleSiteContainer(site.getSiteId(), "documentLibrary");
		final String node1Id = createTextFile(sc.getId(), "Test Doc 1.txt", "Test Content").getId();

		// TEMP - pending remote api to list node ratings
		NodeRef nodeRef1 = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, node1Id);

		// Test Case cloud-1976
		// Create node ratings
		// try to add a rating to a comment
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));

			Comment comment = new Comment("Test Comment", "Test Comment");
			Comment newComment = commentsProxy.createNodeComment(node1Id, comment);
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
			Tag newTag = nodesProxy.createNodeTag(node1Id, tag);
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
			nodesProxy.createNodeRating(node1Id, new NodeRating("missingRatingScheme", Double.valueOf(1.0f)));
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
			nodesProxy.createNodeRating(node1Id, new NodeRating("likes", Double.valueOf(2.0f)));
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
			nodesProxy.createNodeRating(node1Id, new NodeRating("fiveStar", true));
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
			nodesProxy.createNodeRating(node1Id, new NodeRating("fiveStar", 5));
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
			NodeRating ret = nodesProxy.createNodeRating(node1Id, rating);
			assertEquals(rating.getMyRating(), ret.getMyRating());
			assertTrue(format.parse(ret.getRatedAt()).after(time));
			assertEquals(rating.getId(), ret.getId());
			assertEquals(new NodeRating.Aggregate(1, null), ret.getAggregate());

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person12.getId()));
			ret = nodesProxy.createNodeRating(node1Id, rating);
			assertEquals(rating.getMyRating(), ret.getMyRating());
			assertTrue(format.parse(ret.getRatedAt()).after(time));
			assertEquals(rating.getId(), ret.getId());
			assertEquals(new NodeRating.Aggregate(2, null), ret.getAggregate());
			
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person12.getId()));
			ret = nodesProxy.createNodeRating(node1Id, rating);
			assertEquals(rating.getMyRating(), ret.getMyRating());
			assertTrue(format.parse(ret.getRatedAt()).after(time));
			assertEquals(rating.getId(), ret.getId());
			assertEquals(new NodeRating.Aggregate(2, null), ret.getAggregate());
			
			// different network - unauthorized
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person21.getId()));
				nodesProxy.createNodeRating(node1Id, rating);
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
	 				ListResponse<NodeRating> resp = nodesProxy.getNodeRatings(node1Id, createParams(paging, null));
					checkList(expectedRatings.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
				}
	
				{
					int skipCount = 1;
					int maxItems = Integer.MAX_VALUE;
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
					Paging paging = getPaging(skipCount, maxItems, expectedRatings.size(), expectedRatings.size());
					ListResponse<NodeRating> resp = nodesProxy.getNodeRatings(node1Id, createParams(paging, null));
					checkList(expectedRatings.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
				}
	
				{
					int skipCount = 1;
					int maxItems = expectedRatings.size();
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
					Paging paging = getPaging(skipCount, maxItems, expectedRatings.size(), expectedRatings.size());
					ListResponse<NodeRating> resp = nodesProxy.getNodeRatings(node1Id, createParams(paging, null));
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
					ListResponse<NodeRating> resp = nodesProxy.getNodeRatings(node1Id, createParams(paging, null));
					checkList(expectedRatings.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
				}
	
				{
					int skipCount = 1;
					int maxItems = Integer.MAX_VALUE;
					Paging paging = getPaging(skipCount, maxItems, expectedRatings.size(), expectedRatings.size());
					ListResponse<NodeRating> resp = nodesProxy.getNodeRatings(node1Id, createParams(paging, null));
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
					nodesProxy.getNodeRatings(node1Id, createParams(paging, null));
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
						if(node1Id.equals(objectId))
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
				nodesProxy.removeNodeRating(node1Id, rating);
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
			}

			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person12.getId()));
				nodesProxy.removeNodeRating(node1Id, rating);
			}

			// check list
			{
				List<NodeRating> ratings = repoService.getNodeRatings(person11.getId(), network1.getId(), nodeRef1);

				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
				Paging paging = getPaging(skipCount, maxItems, ratings.size(), ratings.size());
				ListResponse<NodeRating> resp = nodesProxy.getNodeRatings(node1Id, createParams(paging, null));
				checkList(ratings.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
			}
		}

		// get a node rating
		// 1977
		{
			NodeRating rating = new NodeRating("likes", true);
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));

			NodeRating expected = nodesProxy.createNodeRating(node1Id, rating);
			NodeRating actual = nodesProxy.getNodeRating(node1Id, "likes");
			expected.expected(actual);
		}

		{
			// update node rating
			NodeRating rating = new NodeRating("fiveStar", 2);

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person12.getId()));

			// create initial rating
			NodeRating createdRating = nodesProxy.createNodeRating(node1Id, rating);
			NodeRating updateRating = new NodeRating(createdRating.getId(), 5);

			// update - not supported
			try
			{
				nodesProxy.updateNodeRating(node1Id, updateRating);
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
			try
			{
				// -ve test - cannot create multiple ratings in single POST call (unsupported)
				List<NodeRating> ratings = new ArrayList<>(2);
				ratings.add(new NodeRating("likes", true));
				ratings.add(new NodeRating("likes", false));
                
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
				nodesProxy.create("nodes", node1Id, "ratings", null, JSONArray.toJSONString(ratings), "Unable to POST to multiple ratings");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}

			// get an arbitrary rating
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			ListResponse<NodeRating> resp = nodesProxy.getNodeRatings(node1Id, createParams(getPaging(0, Integer.MAX_VALUE), null));
			List<NodeRating> nodeRatings = resp.getList();
			assertTrue(nodeRatings.size() > 0);

			try
			{
				NodeRating rating = new NodeRating("likes", true);

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
				nodesProxy.create("nodes", node1Id, "ratings", "likes", rating.toJSON().toString(), "Unable to POST to a node rating");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}

			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
				nodesProxy.update("nodes", node1Id, "ratings", null, null, "Unable to PUT node ratings");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}
			
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
				nodesProxy.remove("nodes", node1Id, "ratings", null, "Unable to DELETE node ratings");
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
				nodesProxy.update("nodes", node1Id, "ratings", rating.getId(), null, "Unable to PUT a node rating");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}
		}
		
        // Test case ACE-5453
        {
            try
            {
                testSkipCountLargeValue(person11, network1, nodeRef1, nodesProxy);
            }
            catch (PublicApiException e)
            {
                fail();
            }
        }
    }

    // test for retrieving the list of ratings with high value of skipCount(e.g. 10)
    public void testSkipCountLargeValue(TestPerson person11, TestNetwork network1, NodeRef nodeRef1, Nodes nodesProxy) throws PublicApiException
    {
        List<NodeRating> expectedRatings = repoService.getNodeRatings(person11.getId(), network1.getId(), nodeRef1);
        int skipCount = 10;
        int maxItems = Integer.MAX_VALUE;
        Paging paging = getPaging(skipCount, maxItems, expectedRatings.size(), expectedRatings.size());
        nodesProxy.getNodeRatings(nodeRef1.getId(), createParams(paging, null));
    }

	@Override
	public String getScope()
	{
		return "public";
	}
}
