package org.alfresco.rest.api.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.rest.api.Activities.ActivityWho;
import org.alfresco.rest.api.tests.RepoService.SiteInformation;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiClient.People;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Activity;
import org.alfresco.rest.api.tests.client.data.SiteRole;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;
import org.apache.commons.httpclient.HttpStatus;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class TestActivities extends EnterpriseTestApi
{
	private TestNetwork network1;
	private TestNetwork network2;

	private TestPerson person1; // network1
	private TestPerson person2; // network1
	private TestPerson person3; // network2

	private TestSite testSite; // network1
	private TestSite testSite1; // network1
	private TestSite testSite2; // network2

	@Before
	public void setup() throws Exception
	{
		this.network1 = repoService.createNetworkWithAlias("activitiesNetwork1", true);
		this.network2 = repoService.createNetworkWithAlias("activitiesNetwork2", true);

		try
		{
			AuthenticationUtil.pushAuthentication();
			AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

			network1.create();
			network2.create();
		}
		finally
		{
			AuthenticationUtil.popAuthentication();
		}

		// Create some users and sites
		final List<TestPerson> people = new ArrayList<TestPerson>(5);

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

		this.person1 = people.get(0);
		this.person2 = people.get(1);
		this.person3 = people.get(2);
		
		this.testSite = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<TestSite>()
        {
        	@SuppressWarnings("synthetic-access")
        	public TestSite execute() throws Throwable
        	{
				return TenantUtil.runAsUserTenant(new TenantRunAsWork<TestSite>()
				{
					public TestSite doWork() throws Exception
					{
						SiteInformation siteInfo = new SiteInformation(GUID.generate(), "", "", SiteVisibility.PUBLIC);
						TestSite site = network1.createSite(siteInfo);
						site.inviteToSite(person2.getId(), SiteRole.SiteCollaborator);
						
						return site;
					}
				}, person1.getId(), network1.getId());
        	}
        }, false, true);

		this.testSite1 = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<TestSite>()
        {
        	@SuppressWarnings("synthetic-access")
        	public TestSite execute() throws Throwable
        	{
				return TenantUtil.runAsUserTenant(new TenantRunAsWork<TestSite>()
				{
					public TestSite doWork() throws Exception
					{
						SiteInformation siteInfo = new SiteInformation(GUID.generate(), "", "", SiteVisibility.PUBLIC);
						return network1.createSite(siteInfo);
					}
				}, person1.getId(), network1.getId());
        	}
        }, false, true);

		// create some activities against those sites
		TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
		{
			@SuppressWarnings("unchecked")
			@Override
			public Void doWork() throws Exception
			{
				// ensure at least 3 activities
				JSONObject activityData = new JSONObject();
				activityData.put("title", GUID.generate());
				activityData.put("nodeRef", testSite.getContainerNodeRef("documentLibrary").toString());
				repoService.postActivity("org.alfresco.documentlibrary.file-added", testSite.getSiteId(), activityData);
				
				activityData = new JSONObject();
				activityData.put("title", GUID.generate());
				activityData.put("nodeRef", testSite.getContainerNodeRef("documentLibrary").toString());
				repoService.postActivity("org.alfresco.documentlibrary.file-added", testSite.getSiteId(), activityData);
				
				activityData = new JSONObject();
				activityData.put("title", GUID.generate());
				activityData.put("nodeRef", testSite.getContainerNodeRef("documentLibrary").toString());
				repoService.postActivity("org.alfresco.documentlibrary.file-added", testSite.getSiteId(), activityData);

				activityData = new JSONObject();
				activityData.put("title", GUID.generate());
				activityData.put("nodeRef", testSite1.getContainerNodeRef("documentLibrary").toString());
				repoService.postActivity("org.alfresco.documentlibrary.file-added", testSite1.getSiteId(), activityData);

				return null;
			}
		}, person1.getId(), network1.getId());

		TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
		{
			@SuppressWarnings("unchecked")
			@Override
			public Void doWork() throws Exception
			{
				// ensure at least 3 activities
				JSONObject activityData = new JSONObject();
				activityData.put("title", GUID.generate());
				activityData.put("nodeRef", testSite.getContainerNodeRef("documentLibrary").toString());
				repoService.postActivity("org.alfresco.documentlibrary.file-added", testSite.getSiteId(), activityData);
				
				activityData = new JSONObject();
				activityData.put("title", GUID.generate());
				activityData.put("nodeRef", testSite.getContainerNodeRef("documentLibrary").toString());
				repoService.postActivity("org.alfresco.documentlibrary.file-added", testSite.getSiteId(), activityData);
				
				activityData = new JSONObject();
				activityData.put("title", GUID.generate());
				activityData.put("nodeRef", testSite.getContainerNodeRef("documentLibrary").toString());
				repoService.postActivity("org.alfresco.documentlibrary.file-added", testSite.getSiteId(), activityData);

				return null;
			}
		}, person2.getId(), network1.getId());

		// user generates activities in 2 networks
		this.testSite2 = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<TestSite>()
        {
        	@SuppressWarnings("synthetic-access")
        	public TestSite execute() throws Throwable
        	{
				return TenantUtil.runAsUserTenant(new TenantRunAsWork<TestSite>()
				{
					public TestSite doWork() throws Exception
					{
						SiteInformation siteInfo = new SiteInformation(GUID.generate(), "", "", SiteVisibility.PUBLIC);
						return network2.createSite(siteInfo);
					}
				}, person3.getId(), network2.getId());
        	}
        }, false, true);

		TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
		{
			@SuppressWarnings("unchecked")
			@Override
			public Void doWork() throws Exception
			{
				// ensure at least 3 activities
				JSONObject activityData = new JSONObject();
				activityData.put("title", GUID.generate());
				activityData.put("nodeRef", testSite.getContainerNodeRef("documentLibrary").toString());
				repoService.postActivity("org.alfresco.documentlibrary.file-added", testSite.getSiteId(), activityData);
				
				activityData = new JSONObject();
				activityData.put("title", GUID.generate());
				activityData.put("nodeRef", testSite.getContainerNodeRef("documentLibrary").toString());
				repoService.postActivity("org.alfresco.documentlibrary.file-added", testSite.getSiteId(), activityData);
				
				activityData = new JSONObject();
				activityData.put("title", GUID.generate());
				activityData.put("nodeRef", testSite.getContainerNodeRef("documentLibrary").toString());
				repoService.postActivity("org.alfresco.documentlibrary.file-added", testSite.getSiteId(), activityData);

				return null;
			}
		}, person3.getId(), network1.getId());
		
		TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
		{
			@SuppressWarnings("unchecked")
			@Override
			public Void doWork() throws Exception
			{
				// ensure at least 3 activities
				JSONObject activityData = new JSONObject();
				activityData.put("title", GUID.generate());
				activityData.put("nodeRef", testSite2.getContainerNodeRef("documentLibrary").toString());
				repoService.postActivity("org.alfresco.documentlibrary.file-added", testSite2.getSiteId(), activityData);
				
				activityData = new JSONObject();
				activityData.put("title", GUID.generate());
				activityData.put("nodeRef", testSite2.getContainerNodeRef("documentLibrary").toString());
				repoService.postActivity("org.alfresco.documentlibrary.file-added", testSite2.getSiteId(), activityData);
				
				activityData = new JSONObject();
				activityData.put("title", GUID.generate());
				activityData.put("nodeRef", testSite2.getContainerNodeRef("documentLibrary").toString());
				repoService.postActivity("org.alfresco.documentlibrary.file-added", testSite2.getSiteId(), activityData);

				return null;
			}
		}, person3.getId(), network2.getId());
		
		repoService.generateFeed();	
	}

	@Test
	public void testPersonActivities() throws Exception
	{
		People peopleProxy = publicApiClient.people();

		// Test Case cloud-2204
		// Test case cloud-1500
		// Test Case cloud-2216
		// paging
		
		// Test Case cloud-1500
		{
			List<Activity> expectedActivities = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<Activity>>()
			{
				@Override
				public List<Activity> doWork() throws Exception
				{
					List<Activity> activities = repoService.getActivities(person1.getId(), null, false, true);
					return activities;
				}
			}, person1.getId(), network1.getId());
	
			{
				int skipCount = 0;
				int maxItems = 2;
				Paging paging = getPaging(skipCount, maxItems, expectedActivities.size(), null);

				Map<String, String> params = createParams(paging, null);
				params.put("who", String.valueOf(ActivityWho.me));
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				ListResponse<Activity> response = peopleProxy.getActivities(person1.getId(), params);
				checkList(expectedActivities.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), response);
			}

			{
				int skipCount = 2;
				int maxItems = expectedActivities.size() - 2;
				assertTrue(maxItems > 0);
				Paging paging = getPaging(skipCount, maxItems, expectedActivities.size(), null);
	
				Map<String, String> params = createParams(paging, null);
				params.put("who", String.valueOf(ActivityWho.me));
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				ListResponse<Activity> response = peopleProxy.getActivities(person1.getId(), params);
				checkList(expectedActivities.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), response);
			}

			// "-me-" user
			{
				int skipCount = 0;
				int maxItems = 2;
				Paging paging = getPaging(skipCount, maxItems, expectedActivities.size(), null);

				Map<String, String> params = createParams(paging, null);
				params.put("who", String.valueOf(ActivityWho.me));
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				ListResponse<Activity> response = peopleProxy.getActivities(org.alfresco.rest.api.People.DEFAULT_USER, params);
				checkList(expectedActivities.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), response);
			}
		}
		
		// unknown user - 404
		try
		{
			List<Activity> expectedActivities = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<Activity>>()
			{
				@Override
				public List<Activity> doWork() throws Exception
				{
					List<Activity> activities = repoService.getActivities(person1.getId(), null, false, true);
					return activities;
				}
			}, person1.getId(), network1.getId());
			
			int skipCount = 0;
			int maxItems = 2;
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			Paging paging = getPaging(skipCount, maxItems, expectedActivities.size(), null);
			peopleProxy.getActivities(GUID.generate(), createParams(paging, null));
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
		// unknown site - 404
		try
		{
			List<Activity> expectedActivities = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<Activity>>()
			{
				@Override
				public List<Activity> doWork() throws Exception
				{
					List<Activity> activities = repoService.getActivities(person1.getId(), null, false, true);
					return activities;
				}
			}, person1.getId(), network1.getId());

			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedActivities.size(), null);
			Map<String, String> params = createParams(paging, null);
			params.put("siteId", GUID.generate());
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			peopleProxy.getActivities(GUID.generate(), params);
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}

		// user from another network - 404
		try
		{
			List<Activity> expectedActivities = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<Activity>>()
			{
				@Override
				public List<Activity> doWork() throws Exception
				{
					List<Activity> activities = repoService.getActivities(person1.getId(), null, false, true);
					return activities;
				}
			}, person1.getId(), network1.getId());

			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedActivities.size(), null);
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person3.getId()));
			peopleProxy.getActivities(person1.getId(), createParams(paging, null));
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
		}

		// another user from the same network - 403
		try
		{
			List<Activity> expectedActivities = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<Activity>>()
			{
				@Override
				public List<Activity> doWork() throws Exception
				{
					List<Activity> activities = repoService.getActivities(person1.getId(), null, false, true);
					return activities;
				}
			}, person1.getId(), network1.getId());

			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedActivities.size(), null);
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
			peopleProxy.getActivities(person1.getId(), createParams(paging, null));
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_FORBIDDEN, e.getHttpResponse().getStatusCode());
		}
		
		try
		{
			List<Activity> activities = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<Activity>>()
			{
				@Override
				public List<Activity> doWork() throws Exception
				{
					List<Activity> activities = repoService.getActivities(person1.getId(), null, false, true);
					return activities;
				}
			}, person1.getId(), network1.getId());
			assertTrue(activities.size() > 0);
			Activity activity = activities.get(0);

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			peopleProxy.remove("people", person1.getId(), "activities", String.valueOf(activity.getId()), "Unable to DELETE a person activity");
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}
		
		// Test Case cloud-1500
		// other user activities
		{
			List<Activity> expectedActivities = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<Activity>>()
			{
				@Override
				public List<Activity> doWork() throws Exception
				{
					List<Activity> expectedActivities = repoService.getActivities(person1.getId(), null, true, false);
					return expectedActivities;
				}
			}, person1.getId(), network1.getId());

			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedActivities.size(), null);

			Map<String, String> params = createParams(paging, null);
			params.put("who", String.valueOf(ActivityWho.others));
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			ListResponse<Activity> response = peopleProxy.getActivities(person1.getId(), params);
			checkList(expectedActivities.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), response);
		}

		// all activities with siteId exclusion
		{
			List<Activity> expectedActivities = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<Activity>>()
			{
				@Override
				public List<Activity> doWork() throws Exception
				{
					List<Activity> expectedActivities = repoService.getActivities(person1.getId(), testSite.getSiteId(), false, false);
					return expectedActivities;
				}
			}, person1.getId(), network1.getId());

			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedActivities.size(), null);

			Map<String, String> params = createParams(paging, null);
			params.put("siteId", testSite.getSiteId());
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			ListResponse<Activity> response = peopleProxy.getActivities(person1.getId(), params);
			checkList(expectedActivities.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), response);
		}
		
		// all activities with siteId exclusion, unknown site id
		try
		{
			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems);

			Map<String, String> params = createParams(paging, null);
			params.put("siteId", GUID.generate());
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			peopleProxy.getActivities(person1.getId(), params);
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}

		// unknown person id
		try
		{
			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems);

			Map<String, String> params = createParams(paging, null);
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			peopleProxy.getActivities(GUID.generate(), params);
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
		// invalid who parameter
		try
		{
			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems);

			Map<String, String> params = createParams(paging, null);
			params.put("who", GUID.generate());
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			peopleProxy.getActivities(person1.getId(), params);
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
		}

		// Test Case cloud-1970
		// Not allowed methods
//		try
//		{
//			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
//			peopleProxy.create("people", person1.getId(), "activities", null, null, "Unable to POST to person activities");
//			fail("");
//		}
//		catch(PublicApiException e)
//		{
//			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
//		}
//		
//		try
//		{
//			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
//			peopleProxy.update("people", person1.getId(), "activities", null, null, "Unable to PUT person activities");
//			fail("");
//		}
//		catch(PublicApiException e)
//		{
//			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
//		}
//
//		try
//		{
//			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
//			peopleProxy.remove("people", person1.getId(), "activities", null, "Unable to DELETE person activities");
//			fail("");
//		}
//		catch(PublicApiException e)
//		{
//			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
//		}
//
//		try
//		{
//			List<Activity> activities = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<Activity>>()
//			{
//				@Override
//				public List<Activity> doWork() throws Exception
//				{
//					List<Activity> activities = repoService.getActivities(person1.getId(), null, false, true);
//					return activities;
//				}
//			}, person1.getId(), network1.getId());
//			assertTrue(activities.size() > 0);
//			Activity activity = activities.get(0);
//
//			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
//			peopleProxy.create("people", person1.getId(), "activities", String.valueOf(activity.getId()), null, "Unable to POST to a person activity");
//			fail("");
//		}
//		catch(PublicApiException e)
//		{
//			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
//		}
//
//		try
//		{
//			List<Activity> activities = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<Activity>>()
//			{
//				@Override
//				public List<Activity> doWork() throws Exception
//				{
//					List<Activity> activities = repoService.getActivities(person1.getId(), null, false, true);
//					return activities;
//				}
//			}, person1.getId(), network1.getId());
//			assertTrue(activities.size() > 0);
//			Activity activity = activities.get(0);
//
//			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
//			peopleProxy.update("people", person1.getId(), "activities", String.valueOf(activity.getId()), null, "Unable to PUT a person activity");
//			fail("");
//		}
//		catch(PublicApiException e)
//		{
//			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
//		}
		
		// Test Case cloud-1970
		// not allowed methods
		{
			List<Activity> activities = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<Activity>>()
			{
				@Override
				public List<Activity> doWork() throws Exception
				{
					List<Activity> activities = repoService.getActivities(person1.getId(), null, false, false);
					return activities;
				}
			}, person1.getId(), network1.getId());
			assertTrue(activities.size() > 0);
			Activity activity = activities.get(0);

			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				peopleProxy.create("people", person1.getId(), "activities", null, null, "Unable to POST to activities");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}

			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				peopleProxy.create("people", person1.getId(), "activities", String.valueOf(activity.getId()), null, "Unable to POST to an activity");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}
	
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				peopleProxy.update("people", person1.getId(), "activities", null, null, "Unable to PUT activities");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}

			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				peopleProxy.update("people", person1.getId(), "activities", String.valueOf(activity.getId()), null, "Unable to PUT an activity");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}

			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				peopleProxy.remove("people", person1.getId(), "activities", null, "Unable to DELETE activities");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}

			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				peopleProxy.remove("people", person1.getId(), "activities", String.valueOf(activity.getId()), "Unable to DELETE an activity");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}
		}
	}
	
	/*
	 * Test that empty roles are not returned
	 */
	@Test
	public void testPUBLICAPI23() throws Exception
	{
		// Add and then remove personId as a member of the public site
		TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				testSite.updateMember(person2.getId(), SiteRole.SiteConsumer);
				testSite.removeMember(person2.getId());

				return null;
			}
		}, person1.getId(), network1.getId());

		// make sure activities have been generated
		repoService.generateFeed();	

		// check that (empty) role is not in the response

		People peopleProxy = publicApiClient.people();
		
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));

			int skipCount = 0;
			int maxItems = 10;
			Paging paging = getPaging(skipCount, maxItems);
			ListResponse<Activity> activities = peopleProxy.getActivities(person2.getId(), createParams(paging, null));
			for(Activity activity : activities.getList())
			{
				String activityType = activity.getActivityType();
				if(activityType.equals("org.alfresco.site.user-left"))
				{
					String role = (String)activity.getSummary().get("role");
					String feedPersonId = activity.getFeedPersonId();
					if(feedPersonId.equals(person2.getId()))
					{
						assertTrue(role == null);
						break;
					}
				}
			}
		}
	}
}
