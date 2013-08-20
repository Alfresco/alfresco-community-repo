package org.alfresco.rest.api.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.alfresco.rest.api.tests.client.data.SiteContainer;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;

public class TestSiteContainers extends EnterpriseTestApi
{
	private TestNetwork network1;
	private TestNetwork network2;

	private List<TestPerson> people1 = new ArrayList<TestPerson>(2);
	private List<TestPerson> people2 = new ArrayList<TestPerson>(2);
	
	private TestPerson person11; // site creator
	private TestPerson person12; // same network, not invited to site
	private TestPerson person13; // same network, invited to site
	private TestPerson person21; // different network, not invited to site
	
	private TestSite site1;
	
	@Before
	public void setup() throws Exception
	{
		Iterator<TestNetwork> networksIt = getTestFixture().getNetworksIt();

		assertTrue(networksIt.hasNext());
		this.network1 = networksIt.next();

		assertTrue(networksIt.hasNext());
		this.network2 = networksIt.next();

		// Create some users in different networks
		TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				// add as external user
				TestPerson person1 = network1.createUser();
				people1.add(person1);
				TestPerson person2 = network1.createUser();
				people1.add(person2);
				TestPerson person3 = network1.createUser();
				people1.add(person3);

				return null;
			}
		}, network1.getId());

		TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				TestPerson person1 = network2.createUser();
				people2.add(person1);

				return null;
			}
		}, network2.getId());

		this.person11 = people1.get(0); // site creator
		this.person12 = people1.get(1); // same network, not invited to site
		this.person21 = people2.get(0); // different network, not invited to site
		this.person13 = people1.get(2); // same network, invited to site
		
//		TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
//		{
//			@Override
//			public Void doWork() throws Exception
//			{
//				// add as external user
//				TestPerson person1 = network2.createUser();
//				network2People.add(person1);
//
//				return null;
//			}
//		}, network2.getId());
//		
//		final TestPerson person3 = network2People.get(0);
		
		// Create a public site
		this.site1 = TenantUtil.runAsUserTenant(new TenantRunAsWork<TestSite>()
		{
			@Override
			public TestSite doWork() throws Exception
			{
				TestSite testSite = network1.createSite(SiteVisibility.PUBLIC);
				return testSite;
			}
		}, person11.getId(), network1.getId());

		TenantUtil.runAsUserTenant(new TenantRunAsWork<Map<String, NodeRef>>()
		{
			@Override
			public Map<String, NodeRef> doWork() throws Exception
			{
				Map<String, NodeRef> containers = new HashMap<String, NodeRef>();
				containers.put("test1", site1.createContainer("test1"));
				containers.put("test2", site1.createContainer("test2"));
				containers.put("test3", site1.createContainer("test3"));
				return containers;
			}
		}, person11.getId(), network1.getId());
	}
	
	@Test
	public void testSiteContainers() throws Exception
	{
		Sites sitesProxy = publicApiClient.sites();
		
		List<SiteContainer> expectedSiteContainers = network1.getSiteContainers(site1.getSiteId(), person11);

		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
	
			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedSiteContainers.size(), expectedSiteContainers.size());
			ListResponse<SiteContainer> resp = sitesProxy.getSiteContainers(site1.getSiteId(), createParams(paging, null));
			checkList(expectedSiteContainers.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
	
			skipCount = 2;
			maxItems = expectedSiteContainers.size();
			paging = getPaging(skipCount, maxItems, expectedSiteContainers.size(), expectedSiteContainers.size());
			resp = sitesProxy.getSiteContainers(site1.getSiteId(), createParams(paging, null));
			checkList(expectedSiteContainers.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
	
			skipCount = 2;
			maxItems = expectedSiteContainers.size() + 2;
			paging = getPaging(skipCount, maxItems, expectedSiteContainers.size(), expectedSiteContainers.size());
			resp = sitesProxy.getSiteContainers(site1.getSiteId(), createParams(paging, null));
			checkList(expectedSiteContainers.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
		}

		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));

			SiteContainer expectedSiteContainer = new SiteContainer(site1.getSiteId(), "test2", null);
			SiteContainer sc = sitesProxy.getSingleSiteContainer(site1.getSiteId(), "test2");
			check(expectedSiteContainer, sc);
		}

		// site does not exist
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));

			sitesProxy.getSingleSiteContainer("gfyuosfgsf8y7s", "documentLibrary");
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}

		// container does not exist
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));

			sitesProxy.getSingleSiteContainer(site1.getSiteId(), "container1");
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}

		// site containers - site does not exist
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));

			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedSiteContainers.size(), expectedSiteContainers.size());
			sitesProxy.getSiteContainers(GUID.generate(), createParams(paging, null));
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
		// a user in the same network, not invited to the site
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person12.getId()));

			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedSiteContainers.size(), expectedSiteContainers.size());
			ListResponse<SiteContainer> ret = sitesProxy.getSiteContainers(site1.getSiteId(), createParams(paging, null));
			checkList(expectedSiteContainers.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), ret);
		}

		// a user in a different network
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person21.getId()));

			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedSiteContainers.size(), expectedSiteContainers.size());
			sitesProxy.getSiteContainers(site1.getSiteId(), createParams(paging, null));
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
		}

		// TODO a user in the same network, invited to the site
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person12.getId()));

			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedSiteContainers.size(), expectedSiteContainers.size());
			ListResponse<SiteContainer> ret = sitesProxy.getSiteContainers(site1.getSiteId(), createParams(paging, null));
			checkList(expectedSiteContainers.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), ret);
		}

		// person invited to site
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person13.getId()));

			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedSiteContainers.size(), expectedSiteContainers.size());
			ListResponse<SiteContainer> resp = sitesProxy.getSiteContainers(site1.getSiteId(), createParams(paging, null));
			checkList(expectedSiteContainers.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
		}

		// invalid methods
		try
		{
			sitesProxy.create("sites", site1.getSiteId(), "containers", null, null, "Unable to POST to site containers");
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}

		try
		{
			sitesProxy.create("sites", site1.getSiteId(), "containers", "documentLibrary", null, "Unable to POST to a site container");
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}

		try
		{
			sitesProxy.update("sites", site1.getSiteId(), "containers", null, null, "Unable to PUT site containers");
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}

		try
		{
			sitesProxy.update("sites", site1.getSiteId(), "containers", "documentLibrary", null, "Unable to PUT a site container");
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}
		
		try
		{
			sitesProxy.remove("sites", site1.getSiteId(), "containers", null, "Unable to DELETE site containers");
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}

		try
		{
			sitesProxy.remove("sites", site1.getSiteId(), "containers", "documentLibrary", "Unable to DELETE a site container");
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}
		
		// 1481
		// user in external network, list site containers
	}
}
