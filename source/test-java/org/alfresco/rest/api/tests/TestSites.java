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
import org.alfresco.rest.api.tests.client.data.Comment;
import org.alfresco.rest.api.tests.client.data.Site;
import org.alfresco.rest.api.tests.client.data.SiteImpl;
import org.alfresco.rest.api.tests.client.data.SiteRole;
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

		// test create and delete site
		{
            String siteTitle = "my site !*#$ 123";

			Site site = new SiteImpl(siteTitle, SiteVisibility.PRIVATE.toString());
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), personId));
			Site ret = sitesProxy.createSite(site);
			String siteId = ret.getSiteId();

			String expectedSiteId = "my-site-123";
			Site siteExp = new SiteImpl(null, expectedSiteId, ret.getGuid(), siteTitle, null,  SiteVisibility.PRIVATE.toString(), null, SiteRole.SiteManager);
			siteExp.expected(ret);

            publicApiClient.setRequestContext(new RequestContext(network1.getId(), personId));
            ret = sitesProxy.getSite(siteId);
            siteExp.expected(ret);

            sitesProxy.removeSite(siteId);

            try
            {
                publicApiClient.setRequestContext(new RequestContext(network1.getId(), personId));
                sitesProxy.getSite(siteId);
                fail("");
            }
            catch(PublicApiException e)
            {
                assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
            }
        }

		// -ve tests
		{
            // invalid auth
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), GUID.generate(), "password"));
            sitesProxy.getSite(site1.getSiteId(), 401);

            publicApiClient.setRequestContext(new RequestContext(network1.getId(), personId));

            // -ve - permission test
            // make sure that a user can't see PRIVATE sites of which they are not a member by creating one and checking it's not in the results
            sitesProxy.getSite(site1.getSiteId(), 404);

            // -ve - try to get unknown site
            sitesProxy.getSite(GUID.generate(), 404);

            Site site = new SiteImpl("my site 123", "invalidsitevisibility");
			sitesProxy.createSite(site, 400);

			site = new SiteImpl(null, "invalid site id", null, "my site 123", null, SiteVisibility.PRIVATE.toString(), null, null);
			sitesProxy.createSite(site, 400);

			site = new SiteImpl(null, "invalidsiteid*", null, "my site 123", null, SiteVisibility.PRIVATE.toString(), null, null);
			sitesProxy.createSite(site, 400);

            // site already exists (409)
            String siteTitle = "my site 456";
            site = new SiteImpl(siteTitle, SiteVisibility.PRIVATE.toString());
            String siteId = sitesProxy.createSite(site, 201).getSiteId();
            sitesProxy.createSite(site, 409);
            sitesProxy.removeSite(siteId, 204); // cleanup

            sitesProxy.removeSite(GUID.generate(), 404);
        }

        // -ve - cannot call POST method on /sites/siteId
        try
        {
            sitesProxy.create("sites", "site", null, null, null, "Unable to POST to a site");
            fail();
        }
        catch(PublicApiException e)
        {
            assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
        }

        // -ve - cannot call DELETE method on /sites
        try
        {
            sitesProxy.remove("sites", null, null, null, "Unable to DELETE sites");
            fail();
        }
        catch(PublicApiException e)
        {
            assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
        }

        // Test Case cloud-1478
		// Test Case cloud-1479
		// user invited to network and user invited to site
		// user invited to network and user not invited to site

	}
}
