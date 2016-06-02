/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
import org.alfresco.rest.api.tests.client.data.Site;
import org.alfresco.rest.api.tests.client.data.SiteImpl;
import org.alfresco.rest.api.tests.client.data.SiteRole;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;

/**
 * @author sglover
 * @author janv
 */
public class TestSites extends EnterpriseTestApi
{
	private TestNetwork network1;
	private String person1Id;
	private String person2Id;

    private Site site1;
    private Site site2;
    private Site site3;
	
	@Before
	public void setup() throws Exception
	{
		// Test: user is member of an account
		this.network1 = getTestFixture().getRandomNetwork();

		Iterator<String> personIt = network1.getPersonIds().iterator();

    	this.person1Id = personIt.next();
    	assertNotNull(person1Id);

    	this.person2Id = personIt.next();
    	assertNotNull(person2Id);
	}

	@Test
	public void testSites() throws Exception
	{
		Sites sitesProxy = publicApiClient.sites();

		// create & get sites (as person 2)
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2Id));

            String siteTitle = "site 1 " + System.currentTimeMillis();
            Site site = new SiteImpl(siteTitle, SiteVisibility.PRIVATE.toString());
            site1 = sitesProxy.createSite(site);

            Site ret = sitesProxy.getSite(site1.getSiteId());
            site1.expected(ret);

            siteTitle = "site 2 " + System.currentTimeMillis();
            site = new SiteImpl(siteTitle, SiteVisibility.PUBLIC.toString());
            site2 = sitesProxy.createSite(site);

            ret = sitesProxy.getSite(site2.getSiteId());
            site2.expected(ret);

            siteTitle = "site 3 " + System.currentTimeMillis();
            site = new SiteImpl(siteTitle, SiteVisibility.MODERATED.toString());
            site3 = sitesProxy.createSite(site);

            ret = sitesProxy.getSite(site3.getSiteId());
            site3.expected(ret);
        }

		List<TestSite> expectedSites = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<TestSite>>()
		{
			@Override
			public List<TestSite> doWork() throws Exception
			{
				List<TestSite> sites = network1.getSites(person1Id);
				return sites;
			}
		}, person1Id, network1.getId());

		{
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1Id));

			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedSites.size(), expectedSites.size());
			ListResponse<Site> resp = sitesProxy.getSites(createParams(paging, null));
			checkList(expectedSites.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
		}

		{
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1Id));

			int skipCount = 2;
			int maxItems = Integer.MAX_VALUE;
			Paging paging = getPaging(skipCount, maxItems, expectedSites.size(), expectedSites.size());
			ListResponse<Site> resp = sitesProxy.getSites(createParams(paging, null));
			checkList(expectedSites.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
		}

		// test create and delete site
		{
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1Id));

            String siteTitle = "my site !*#$ 123";

			Site site = new SiteImpl(siteTitle, SiteVisibility.PRIVATE.toString());
			Site ret = sitesProxy.createSite(site);
			String siteId = ret.getSiteId();

			String expectedSiteId = "my-site-123";
			Site siteExp = new SiteImpl(null, expectedSiteId, ret.getGuid(), siteTitle, null,  SiteVisibility.PRIVATE.toString(), null, SiteRole.SiteManager);
			siteExp.expected(ret);

            ret = sitesProxy.getSite(siteId);
            siteExp.expected(ret);

            sitesProxy.removeSite(siteId);

            sitesProxy.getSite(siteId, 404);
        }

		// -ve tests
		{
            // invalid auth
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), GUID.generate(), "password"));
            sitesProxy.getSite(site1.getSiteId(), 401);

            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1Id));

            // -ve - cannot view or delete a private site
            sitesProxy.getSite(site1.getSiteId(), 404);
            sitesProxy.removeSite(site1.getSiteId(), 404);

            // -ve - test cannot delete a public site (but can view it)
            sitesProxy.getSite(site2.getSiteId(), 200);
            sitesProxy.removeSite(site2.getSiteId(), 403);

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

        {
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1Id));

            // -ve - cannot call POST method on /sites/siteId
            try
            {
                sitesProxy.create("sites", "site", null, null, null, "Unable to POST to a site");
                fail();
            } catch (PublicApiException e)
            {
                assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
            }

            // -ve - cannot call DELETE method on /sites
            try
            {
                sitesProxy.remove("sites", null, null, null, "Unable to DELETE sites");
                fail();
            } catch (PublicApiException e)
            {
                assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
            }
        }

        // Test Case cloud-1478
		// Test Case cloud-1479
		// user invited to network and user invited to site
		// user invited to network and user not invited to site
	}
}
