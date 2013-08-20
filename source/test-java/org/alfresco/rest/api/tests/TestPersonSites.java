package org.alfresco.rest.api.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiClient.Sites;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.MemberOfSite;
import org.alfresco.rest.api.tests.client.data.SiteRole;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;

public class TestPersonSites extends EnterpriseTestApi
{
	private TestNetwork network1;
	private TestNetwork network2;
	
	private TestPerson person11;
	private TestPerson person12;
	private TestPerson person21;
	
	private List<TestSite> sites = new ArrayList<TestSite>(10);
	
	@Before
	public void setup() throws Exception
	{
		Iterator<TestNetwork> networksIt = getTestFixture().getNetworksIt();

		assertTrue(networksIt.hasNext());
		this.network1 = networksIt.next();
		
		assertTrue(networksIt.hasNext());
		this.network2 = networksIt.next();

		// create a user

		final List<TestPerson> people = new ArrayList<TestPerson>(1);

		// Create some users
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

		this.person11 = people.get(0);
		this.person12 = people.get(1);
		this.person21 = people.get(2);
		
		// ...and some sites
		TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				TestSite site = network1.createSite(SiteVisibility.PUBLIC);
				site.inviteToSite(person11.getId(), SiteRole.SiteContributor);
				sites.add(site);

				site = network1.createSite(SiteVisibility.MODERATED);
				site.inviteToSite(person11.getId(), SiteRole.SiteContributor);
				sites.add(site);
				
				site = network1.createSite(SiteVisibility.PRIVATE);
				site.inviteToSite(person11.getId(), SiteRole.SiteConsumer);
				sites.add(site);
				
				site = network1.createSite(SiteVisibility.PUBLIC);
				site.inviteToSite(person11.getId(), SiteRole.SiteManager);
				sites.add(site);

				site = network1.createSite(SiteVisibility.PRIVATE);
				site.inviteToSite(person11.getId(), SiteRole.SiteCollaborator);
				sites.add(site);

				//Special site for person removal
				site = network1.createSite(SiteVisibility.PRIVATE);
				site.inviteToSite(person11.getId(), SiteRole.SiteConsumer);
				sites.add(site);
				return null;
			}
		}, person12.getId(), network1.getId());
	}
	
	@Test
	public void testPersonSites() throws Exception
	{
		Set<MemberOfSite> personSites = new TreeSet<MemberOfSite>();
		
		//Get last site for use with personRemoveSite
		TestSite personRemoveSite = sites.get(sites.size()-1);
		sites.remove(sites.size()-1);

		personSites.addAll(network1.getSiteMemberships(person11.getId()));

		// Create some sites
		personSites.addAll(TenantUtil.runAsUserTenant(new TenantRunAsWork<List<MemberOfSite>>()
		{
			@Override
			public List<MemberOfSite> doWork() throws Exception
			{
				List<MemberOfSite> expectedSites = new ArrayList<MemberOfSite>();

				TestSite site = network1.createSite(SiteVisibility.PRIVATE);
				expectedSites.add(new MemberOfSite(site, SiteRole.SiteManager));

				site = network1.createSite(SiteVisibility.PUBLIC);
				expectedSites.add(new MemberOfSite(site, SiteRole.SiteManager));

				site = network1.createSite(SiteVisibility.MODERATED);
				expectedSites.add(new MemberOfSite(site, SiteRole.SiteManager));

				return expectedSites;
			}
		}, person11.getId(), network1.getId()));
    	
		personSites.addAll(TenantUtil.runAsUserTenant(new TenantRunAsWork<List<MemberOfSite>>()
		{
			@Override
			public List<MemberOfSite> doWork() throws Exception
			{
				List<MemberOfSite> expectedSites = new ArrayList<MemberOfSite>();

				TestSite site = network1.createSite(SiteVisibility.PRIVATE);
				site.inviteToSite(person11.getId(), SiteRole.SiteConsumer);
				expectedSites.add(new MemberOfSite(site, SiteRole.SiteConsumer));

				site = network1.createSite(SiteVisibility.PUBLIC);
				site.inviteToSite(person11.getId(), SiteRole.SiteConsumer);
				expectedSites.add(new MemberOfSite(site, SiteRole.SiteConsumer));

				site = network1.createSite(SiteVisibility.MODERATED);
				site.inviteToSite(person11.getId(), SiteRole.SiteConsumer);
				expectedSites.add(new MemberOfSite(site, SiteRole.SiteConsumer));

				return expectedSites;
			}
		}, person12.getId(), network1.getId()));

		final List<MemberOfSite> expectedSites = new ArrayList<MemberOfSite>(personSites);
		Sites sitesProxy = publicApiClient.sites();
		
		// Test Case cloud-1487
		
		// unknown user
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));

			sitesProxy.getPersonSites(GUID.generate(), null);
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}

		// Test Case cloud-2200
		// Test Case cloud-2213
		// user should be able to list their sites
		{
			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedSites.size(), null);
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			ListResponse<MemberOfSite> resp = sitesProxy.getPersonSites(person11.getId(), createParams(paging, null));
			checkList(expectedSites.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
		}
		
		{
			int skipCount = 2;
			int maxItems = 8;
			Paging paging = getPaging(skipCount, maxItems, expectedSites.size(), null);
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			ListResponse<MemberOfSite> resp = sitesProxy.getPersonSites(person11.getId(), createParams(paging, null));
			checkList(expectedSites.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
		}
		
		// "-me-" user
		{
			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedSites.size(), null);
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			ListResponse<MemberOfSite> resp = sitesProxy.getPersonSites(org.alfresco.rest.api.People.DEFAULT_USER, createParams(paging, null));
			checkList(expectedSites.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
		}

		// a user in another tenant should not be able to list a user's sites
		try
		{
			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedSites.size(), null);
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person21.getId()));
			sitesProxy.getPersonSites(person11.getId(), createParams(paging, null));
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
		}
		
		// Test case cloud-1488
		{
			MemberOfSite memberOfSite = expectedSites.get(0);

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			MemberOfSite ret = sitesProxy.getPersonSite(person11.getId(), memberOfSite.getSiteId());
			memberOfSite.expected(ret);
		}

		try
		{
			MemberOfSite memberOfSite = expectedSites.get(0);

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			sitesProxy.getPersonSite(GUID.generate(), memberOfSite.getSiteId());
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}

		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			sitesProxy.getPersonSite(person11.getId(), GUID.generate());
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}

		// Test Case cloud-1487
		// unknown person id
		try
		{
			MemberOfSite memberOfSite = expectedSites.get(0);

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			sitesProxy.getPersonSite(GUID.generate(), memberOfSite.getSiteId());
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			sitesProxy.getPersonSite(person11.getId(), GUID.generate());
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
		{
			//Tests removing a person from the site
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			sitesProxy.remove("people", person11.getId(), "sites", personRemoveSite.getSiteId(), "Unable to DELETE a person site");
		
			try
			{
				sitesProxy.getPersonSite(person11.getId(), personRemoveSite.getSiteId());
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}
		}
		
		// TODO
		// person from external network listing user sites
		
		// Test Case cloud-1966
		// Not allowed methods
		try
		{
			MemberOfSite memberOfSite = expectedSites.get(0);

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			sitesProxy.create("people", person11.getId(), "sites", memberOfSite.getSiteId(), null, "Unable to POST to a person site");
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}

		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			sitesProxy.create("people", person11.getId(), "sites", null, null, "Unable to POST to person sites");
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}
		
		try
		{
			MemberOfSite memberOfSite = expectedSites.get(0);

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			sitesProxy.update("people", person11.getId(), "sites", memberOfSite.getSiteId(), null, "Unable to PUT a person site");
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}

		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			sitesProxy.update("people", person11.getId(), "sites", null, null, "Unable to PUT person sites");
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}

		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			sitesProxy.remove("people", person11.getId(), "sites", null, "Unable to DELETE person sites");
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}
	}
}
