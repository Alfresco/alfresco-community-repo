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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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
            String siteDescription = "my site description";

			SiteImpl site = new SiteImpl(siteTitle, SiteVisibility.PRIVATE.toString());
            site.setDescription(siteDescription);

			Site ret = sitesProxy.createSite(site);
			String siteId = ret.getSiteId();

			String expectedSiteId = "my-site-123";
			Site siteExp = new SiteImpl(null, expectedSiteId, ret.getGuid(), siteTitle, siteDescription,  SiteVisibility.PRIVATE.toString(), null, SiteRole.SiteManager);
			siteExp.expected(ret);

            ret = sitesProxy.getSite(siteId);
            siteExp.expected(ret);

            sitesProxy.removeSite(siteId);

            // -ve test - ie. cannot get site after it has been deleted
            sitesProxy.getSite(siteId, 404);
        }

        // test create + permanent delete + create
        {

            String siteId = "bbb";
            String siteTitle = "BBB site";

            Site site = new SiteImpl(null, siteId, null, siteTitle, null, SiteVisibility.PUBLIC.toString(), null, null);

            sitesProxy.createSite(site);

            // permanent site delete (bypass trashcan/archive)
            sitesProxy.removeSite(siteId, true, 204);

            sitesProxy.createSite(site);
        }

        // test create using site id = "true" (RA-1101)
        {

            String siteId = "true";
            String siteTitle = "string";
            String siteDescription = "string";

            Site site = new SiteImpl(null, siteId, null, siteTitle, siteDescription, SiteVisibility.PUBLIC.toString(), null, null);

            sitesProxy.createSite(site);
        }

        // -ve tests
        {
            // invalid auth
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), GUID.generate(), "password"));
            sitesProxy.getSite(site1.getSiteId(), 401);

            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1Id));

            // -ve - cannot view or delete a private site
            sitesProxy.getSite(site1.getSiteId(), 404);
            sitesProxy.removeSite(site1.getSiteId(), false, 404);

            // -ve - test cannot delete a public site (but can view it)
            sitesProxy.getSite(site2.getSiteId(), 200);
            sitesProxy.removeSite(site2.getSiteId(), false, 403);

            // -ve - try to get unknown site
            sitesProxy.getSite(GUID.generate(), 404);

            SiteImpl site = new SiteImpl("my site 123", "invalidsitevisibility");
            sitesProxy.createSite(site, 400);

            site = new SiteImpl(null, "invalid site id", null, "my site 123", null, SiteVisibility.PRIVATE.toString(), null, null);
            sitesProxy.createSite(site, 400);

            site = new SiteImpl(null, "invalidsiteid*", null, "my site 123", null, SiteVisibility.PRIVATE.toString(), null, null);
            sitesProxy.createSite(site, 400);

            site = new SiteImpl();
            site.setSiteId(new String(new char[72]).replace('\0', 'a'));
            site.setTitle(new String(new char[256]).replace('\0', 'a'));
            site.setDescription(new String(new char[512]).replace('\0', 'a'));
            site.setVisibility(SiteVisibility.PUBLIC.toString());
            sitesProxy.createSite(site, 201);

            // -ve - site id too long
            site = new SiteImpl();
            site.setSiteId(new String(new char[73]).replace('\0', 'a'));
            site.setTitle("ok");
            site.setDescription("ok");
            site.setVisibility(SiteVisibility.PUBLIC.toString());
            sitesProxy.createSite(site, 400);

            // -ve - site title too long
            site = new SiteImpl();
            site.setSiteId("ok");
            site.setTitle(new String(new char[257]).replace('\0', 'a'));
            site.setDescription("ok");
            site.setVisibility(SiteVisibility.PUBLIC.toString());
            sitesProxy.createSite(site, 400);

            // -ve - site description too long
            site = new SiteImpl();
            site.setSiteId("ok");
            site.setTitle("ok");
            site.setDescription(new String(new char[513]).replace('\0', 'a'));
            site.setVisibility(SiteVisibility.PUBLIC.toString());
            sitesProxy.createSite(site, 400);

            // site already exists (409)
            String siteTitle = "my site 456";
            site = new SiteImpl(siteTitle, SiteVisibility.PRIVATE.toString());
            String siteId = sitesProxy.createSite(site, 201).getSiteId();
            sitesProxy.createSite(site, 409);
            sitesProxy.removeSite(siteId); // cleanup

            sitesProxy.removeSite(GUID.generate(), false, 404);
        }

        // -ve - cannot create site with same site id as an existing site (even if it is in the trashcan/archive)
        {
            String siteId = "aaa";
            String siteTitle = "AAA site";

            Site site = new SiteImpl(null, siteId, null, siteTitle, null, SiteVisibility.PUBLIC.toString(), null, null);

            String siteNodeId = sitesProxy.createSite(site).getGuid();

            // -ve - duplicate site id
            sitesProxy.createSite(site, 409);

            sitesProxy.removeSite(siteId);

            // -ve - duplicate site id (even if site is in trashcan)
            sitesProxy.createSite(site, 409);

            // now purge the site
            sitesProxy.remove("deleted-nodes", siteNodeId, null, null, "Cannot purge site");

            sitesProxy.createSite(site);
        }

        // -ve - minor: error code if updating via nodes api (REPO-512)
        {
            String siteId = "zzz";
            String siteTitle = "ZZZ site";

            Site site = new SiteImpl(null, siteId, null, siteTitle, null, SiteVisibility.PRIVATE.toString(), null, null);
            String siteNodeId = sitesProxy.createSite(site).getGuid();

            // try to update to invalid site visibility
            JSONObject prop = new JSONObject();
            prop.put("st:siteVisibility","INVALID");
            JSONObject properties = new JSONObject();
            properties.put("properties", new JSONObject(prop));
            try
            {
                sitesProxy.update("nodes", siteNodeId, null, null, properties.toJSONString(), null);
                fail();
            } catch (PublicApiException e)
            {
                assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
            }

            sitesProxy.removeSite(siteId); // cleanup
        }
        
        // -ve test - cannot create multiple sites in single POST call (unsupported)
        {
            List<Site> sites = new ArrayList<>(2);
            sites.add(new SiteImpl(null, "siteA1", null, "siteA1", null, SiteVisibility.PRIVATE.toString(), null, null));
            sites.add(new SiteImpl(null, "siteB1", null, "siteB1", null, SiteVisibility.PRIVATE.toString(), null, null));
            
            sitesProxy.create("sites", null, null, null, JSONArray.toJSONString(sites), null, 405);
        }

        // -ve tests - belts-and-braces for unsupported methods
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
