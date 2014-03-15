package org.alfresco.rest.api.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.rest.api.tests.RepoService.SiteInformation;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiClient.Sites;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Site;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;

public class TestSites extends EnterpriseTestApi
{
	private TestNetwork network1;
	private String personId;
	private String person1Id;
	private TestSite site1;
	
	@Before
	public void setup() throws Exception
	{
		// Test: user is member of an account
		this.network1 = getTestFixture().getRandomNetwork();
		Iterator<String> personIt = network1.getPersonIds().iterator();
    	this.personId = personIt.next();
    	assertNotNull(personId);
    	this.person1Id = personIt.next();
    	assertNotNull(person1Id);

    	this.site1 = TenantUtil.runAsUserTenant(new TenantRunAsWork<TestSite>()
		{
			@Override
			public TestSite doWork() throws Exception
			{
				String siteName = "site" + System.currentTimeMillis();
				SiteInformation site = new SiteInformation(siteName, siteName, siteName, SiteVisibility.PRIVATE);
				return network1.createSite(site);
			}
		}, person1Id, network1.getId());
	}

	@Test
	public void testSites() throws Exception
	{
		Sites sitesProxy = publicApiClient.sites();

    	// make sure that a user can't see PRIVATE sites of which they are not a member by creating one and checking it's not in the results
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), personId));
			sitesProxy.getSite(site1.getSiteId());
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
		
		// Test Case cloud-1963
		// invalid methods
		try
		{
			sitesProxy.create("sites", null, null, null, null, "Unable to POST to sites");
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}
		
		try
		{
			sitesProxy.create("sites", "site", null, null, null, "Unable to POST to a site");
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}
		
//		try
//		{
//			sitesProxy.remove("sites", null, null, null, "Unable to DELETE sites");
//			fail();
//		}
//		catch(PublicApiException e)
//		{
//			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
//		}
//		
//		try
//		{
//			sitesProxy.remove("sites", "site", null, null, "Unable to DELETE sites");
//			fail();
//		}
//		catch(PublicApiException e)
//		{
//			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
//		}

		try
		{
			sitesProxy.remove("sites", null, null, null, "Unable to DELETE sites");
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}
		
		try
		{
			sitesProxy.remove("sites", "site", null, null, "Unable to DELETE sites");
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}
		
		// invalid site
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), personId));
			sitesProxy.getSite(GUID.generate());
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
		// invalid auth
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), GUID.generate(), "password"));
			sitesProxy.getSite(site1.getSiteId());
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
		}

		// get a site
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1Id));
			Site ret = sitesProxy.getSite(site1.getSiteId());
			site1.expected(ret);
		}

		List<TestSite> expectedSites = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<TestSite>>()
		{
			@Override
			public List<TestSite> doWork() throws Exception
			{
				List<TestSite> sites = network1.getSites(personId);
				return sites;
			}
		}, personId, network1.getId());

		{
			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedSites.size(), expectedSites.size());
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), personId));
			ListResponse<Site> resp = sitesProxy.getSites(createParams(paging, null));
			checkList(expectedSites.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
		}

		{
			int skipCount = 2;
			int maxItems = Integer.MAX_VALUE;
			Paging paging = getPaging(skipCount, maxItems, expectedSites.size(), expectedSites.size());
			ListResponse<Site> resp = sitesProxy.getSites(createParams(paging, null));
			checkList(expectedSites.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
		}
		
		// Test Case cloud-1478
		// Test Case cloud-1479
		// user invited to network and user invited to site
		// user invited to network and user not invited to site
	}
}
