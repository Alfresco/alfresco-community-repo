package org.alfresco.rest.api.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.rest.api.tests.RepoService.SiteInformation;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiClient.Sites;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.FavouriteSite;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;

public class TestFavouriteSites extends EnterpriseTestApi
{
	@Test
	public void testFavouriteSites() throws Exception
	{
		Iterator<TestNetwork> networksIt = getTestFixture().getNetworksIt();
		assertTrue(networksIt.hasNext());
		final TestNetwork network1 = networksIt.next();
		assertTrue(networksIt.hasNext());
		final TestNetwork network2 = networksIt.next();

		// Create some users and sites
		final List<TestPerson> people = new ArrayList<TestPerson>();

		TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
        		TestPerson person = network1.createUser();
        		people.add(person);
        		person = network1.createUser();
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
        		person = network2.createUser();
        		people.add(person);

        		return null;
        	}
        }, network2.getId());

		final TestPerson person1 = people.get(0);
		final TestPerson person2 = people.get(1);
		final TestPerson person3 = people.get(2);
		final TestPerson person4 = people.get(3);
		final TestPerson person5 = people.get(3);

		TestSite testSite = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<TestSite>()
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

		TestSite testSite1 = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<TestSite>()
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

		TestSite testSite3 = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<TestSite>()
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
		
		TestSite testSite4 = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<TestSite>()
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
				}, person5.getId(), network2.getId());
        	}
        }, false, true);

		Sites sitesProxy = publicApiClient.sites();

		// invalid methods
		try
		{
			FavouriteSite fs = new FavouriteSite(testSite.getSiteId());

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			sitesProxy.create("people", person1.getId(), "favorite-sites", testSite.getSiteId(), fs.toJSON().toString(), "Unable to POST to a favorite-site");
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}
		
		try
		{
			FavouriteSite fs = new FavouriteSite(testSite.getSiteId());

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			sitesProxy.update("people", person1.getId(), "favorite-sites", null, fs.toJSON().toString(), "Unable to PUT favorite-sites");
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}

		try
		{
			FavouriteSite fs = new FavouriteSite(testSite.getSiteId());

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			sitesProxy.update("people", person1.getId(), "favorite-sites", testSite.getSiteId(), fs.toJSON().toString(), "Unable to PUT a favorite-site");
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}
		
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			sitesProxy.remove("people", person1.getId(), "favorite-sites", null, "Unable to DELETE favorite-sites");
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}

		// Create favourite site

		// unknown user - 404
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			FavouriteSite fs = new FavouriteSite(testSite.getSiteId());
			sitesProxy.createFavouriteSite("invalid.user", fs);
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
		// user from another network - 401 (not able to auth against tenant)
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person4.getId()));
			FavouriteSite fs = new FavouriteSite(testSite.getSiteId());
			sitesProxy.createFavouriteSite(person1.getId(), fs);
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
		}

		// another user from the same network
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			FavouriteSite fs = new FavouriteSite(testSite.getSiteId());
			sitesProxy.createFavouriteSite(person2.getId(), fs);
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_FORBIDDEN, e.getHttpResponse().getStatusCode());
		}

		// a member of this site
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			FavouriteSite fs = new FavouriteSite(testSite.getSiteId());
			FavouriteSite resp = sitesProxy.createFavouriteSite(person1.getId(), fs);
			fs.expected(resp);
		}

		// add same favourite site
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			sitesProxy.createFavouriteSite(person1.getId(), new FavouriteSite(testSite.getSiteId()));
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(409, e.getHttpResponse().getStatusCode());
		}

		// "-me" user
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
			FavouriteSite fs = new FavouriteSite(testSite.getSiteId());
			FavouriteSite resp = sitesProxy.createFavouriteSite(org.alfresco.rest.api.People.DEFAULT_USER, fs);
			fs.expected(resp);

			final List<FavouriteSite> expectedFavouriteSites = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<FavouriteSite>>()
			{
				@Override
				public List<FavouriteSite> doWork() throws Exception
				{
					return repoService.getFavouriteSites(person2);
				}
			}, person2.getId(), network1.getId());
			
			// check it's there
			int skipCount = 0;
			int maxItems = Integer.MAX_VALUE;
			Paging paging = getPaging(skipCount, maxItems, expectedFavouriteSites.size(), expectedFavouriteSites.size());

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
			sitesProxy.getFavouriteSites(person2.getId(), createParams(paging, null));
		}

		// not a member of this site
		{
			FavouriteSite fs = new FavouriteSite(testSite1.getSiteId());

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			FavouriteSite ret = sitesProxy.createFavouriteSite(person1.getId(), fs);
			fs.expected(ret);
		}

		// GET favourite sites
		{
			final List<FavouriteSite> expectedFavouriteSites = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<FavouriteSite>>()
			{
				@Override
				public List<FavouriteSite> doWork() throws Exception
				{
					return repoService.getFavouriteSites(person1);
				}
			}, person1.getId(), network1.getId());

			// Test Case cloud-1490
			// unknown user
			try
			{
				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expectedFavouriteSites.size(), expectedFavouriteSites.size());
	
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				sitesProxy.getFavouriteSites(GUID.generate(), createParams(paging, null));
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}
	
			// authentication: unknown user
			try
			{
				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expectedFavouriteSites.size(), expectedFavouriteSites.size());
	
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), GUID.generate(), "password"));
				sitesProxy.getFavouriteSites(person1.getId(), createParams(paging, null));
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
			}
	
			// another user from the same network - 403
			try
			{
				int skipCount = 0;
				int maxItems = 2;
				Paging paging = getPaging(skipCount, maxItems, expectedFavouriteSites.size(), expectedFavouriteSites.size());
	
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
				ListResponse<FavouriteSite> response = sitesProxy.getFavouriteSites(person1.getId(), createParams(paging, null));
				checkList(expectedFavouriteSites, paging.getExpectedPaging(), response);
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_FORBIDDEN, e.getHttpResponse().getStatusCode());
			}
			
			// another user from another network - 401
			try
			{
				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expectedFavouriteSites.size(), expectedFavouriteSites.size());
	
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person4.getId()));
				sitesProxy.getFavouriteSites(person1.getId(), createParams(paging, null));
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
			}
			
			// successful GET
			{
				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expectedFavouriteSites.size(), expectedFavouriteSites.size());
	
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				ListResponse<FavouriteSite> response = sitesProxy.getFavouriteSites(person1.getId(), createParams(paging, null));
				checkList(expectedFavouriteSites, paging.getExpectedPaging(), response);
			}
	
			// skipCount is greater than the number of favourite sites
			{
				int skipCount = expectedFavouriteSites.size() + 100;
				Paging paging = getPaging(skipCount, null, expectedFavouriteSites.size(), expectedFavouriteSites.size());
				List<FavouriteSite> expected = Collections.emptyList();
				ListResponse<FavouriteSite> response = sitesProxy.getFavouriteSites(person1.getId(), createParams(paging, null));
				checkList(expected, paging.getExpectedPaging(), response);
			}
			
	    	// "-me-" user
	    	{
				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expectedFavouriteSites.size(), expectedFavouriteSites.size());
	
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				ListResponse<FavouriteSite> response = sitesProxy.getFavouriteSites(org.alfresco.rest.api.People.DEFAULT_USER, createParams(paging, null));
				checkList(expectedFavouriteSites, paging.getExpectedPaging(), response);
	    	}
		}

		// user is a member of the site which he has favourited
		{
			publicApiClient.setRequestContext(new RequestContext(network2.getId(), person5.getId()));

			List<FavouriteSite> expectedFavouriteSites = new ArrayList<FavouriteSite>(1);
			FavouriteSite fs = new FavouriteSite(testSite4.getSiteId());
			expectedFavouriteSites.add(fs);

			FavouriteSite ret = sitesProxy.createFavouriteSite(person5.getId(), fs);
			fs.expected(ret);

			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedFavouriteSites.size(), expectedFavouriteSites.size());
			ListResponse<FavouriteSite> response = sitesProxy.getFavouriteSites(person5.getId(), createParams(paging, null));
			checkList(expectedFavouriteSites, paging.getExpectedPaging(), response);
		}

		// remove
		{
			// create some favourite sites
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person3.getId()));
				FavouriteSite fs = new FavouriteSite(testSite);
				sitesProxy.createFavouriteSite(person3.getId(), fs);
				fs = new FavouriteSite(testSite1);
				sitesProxy.createFavouriteSite(person3.getId(), fs);
			}

			// known user
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				FavouriteSite fs = new FavouriteSite(testSite);
				sitesProxy.removeFavouriteSite(person1.getId(), fs);
	
				List<FavouriteSite> expectedFavouriteSites = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<FavouriteSite>>()
				{
					@Override
					public List<FavouriteSite> doWork() throws Exception
					{
						return repoService.getFavouriteSites(person1);
					}
				}, person1.getId(), network1.getId());
	
				// check removed
				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expectedFavouriteSites.size(), expectedFavouriteSites.size());
	
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				ListResponse<FavouriteSite> response = sitesProxy.getFavouriteSites(person1.getId(), createParams(paging, null));
				assertFalse(response.getList().contains(fs));
			}
			
			// unknown user
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				FavouriteSite fs = new FavouriteSite(testSite);
				sitesProxy.removeFavouriteSite(GUID.generate(), fs);
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}

			// unknown site
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				FavouriteSite fs = new FavouriteSite(GUID.generate());
				sitesProxy.removeFavouriteSite(person1.getId(), fs);
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}

			// try to remove a favourite site that is not a favourite site
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				FavouriteSite fs = new FavouriteSite(testSite3);
				sitesProxy.removeFavouriteSite(person1.getId(), fs);
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}

			// "-me-" user
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person3.getId()));
				FavouriteSite fs = new FavouriteSite(testSite1);
				sitesProxy.removeFavouriteSite(org.alfresco.rest.api.People.DEFAULT_USER, fs);

				List<FavouriteSite> expectedFavouriteSites = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<FavouriteSite>>()
				{
					@Override
					public List<FavouriteSite> doWork() throws Exception
					{
						return repoService.getFavouriteSites(person3);
					}
				}, person3.getId(), network1.getId());
	
				// check removed
				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expectedFavouriteSites.size(), expectedFavouriteSites.size());
	
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person3.getId()));
				ListResponse<FavouriteSite> response = sitesProxy.getFavouriteSites(person3.getId(), createParams(paging, null));
				assertFalse(response.getList().contains(fs));
			}
		}
	}
}
