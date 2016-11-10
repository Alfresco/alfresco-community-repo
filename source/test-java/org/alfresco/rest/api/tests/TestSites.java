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
import static org.junit.Assert.fail;

import java.util.*;

import org.alfresco.repo.model.filefolder.HiddenAspect;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.rest.api.model.SiteUpdate;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiClient.Sites;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.*;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.httpclient.HttpStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

/**
 * V1 REST API tests for managing Sites
 * 
 * @author sglover
 * @author janv
 */
public class TestSites extends EnterpriseTestApi
{
    private static final String RUNID = System.currentTimeMillis()+"";
    
    // test network 1
	private TestNetwork network1;
	private String person1Id;
	private String person2Id;

    private Site site1;
    private Site site2;
    private Site site3;

    // test network 2
    private TestNetwork network2;
    private String person3Id;

    private Site site4;
    private Site site5;
    private Site site6;

    // test network 3
    private TestNetwork network3;
    private String person4Id;

    private Site site7;
    private Site site8;
    private Site site9;
    private Site site10;

    private String site4_id = "a-" + GUID.generate();
    private String site4_title = "c_" + GUID.generate();
    private String site4_description = "b_" + GUID.generate();
    private String site5_id = "b-" + GUID.generate();
    private String site5_title = "a_" + GUID.generate();
    private String site5_description = "c_" + GUID.generate();
    private String site6_id = "c-" + GUID.generate();
    private String site6_title = "b_" + GUID.generate();
    private String site6_description = "a_" + GUID.generate();

    private String preset = "sitePreset";
    private static final String AND_PREDICATE = " AND ";
    private static final String OR_PREDICATE = " OR ";

    @Override
	@Before
	public void 
    setup() throws Exception
	{
        initializeNetwork1();
	}

    private void initializeNetwork1() throws Exception
    {
        if (network1 == null)
        {
            network1 = getRepoService().createNetwork(this.getClass().getName().toLowerCase()+"-1-"+RUNID, true);
            network1.create();

            TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    person1Id = network1.createUser().getId();
                    person2Id = network1.createUser().getId();
                    return null;
                }
            }, network1.getId());
        }
    }
    
    private void initializeNetwork2WithSites() throws Exception
    {
        if (network2 == null)
        {
            network2 = getRepoService().createNetwork(this.getClass().getName().toLowerCase()+"-2-"+RUNID, true);
            network2.create();

            TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    person3Id = network2.createUser().getId();
                    return null;
                }
            }, network2.getId());
            
            publicApiClient.setRequestContext(new RequestContext(network2.getId(), person3Id));

            Sites sitesProxy = publicApiClient.sites();
            
            Site site = new SiteImpl(site4_id, site4_title, site4_description, SiteVisibility.PRIVATE.toString());
            site4 = sitesProxy.createSite(site);

            site = new SiteImpl(site5_id, site5_title, site5_description, SiteVisibility.PRIVATE.toString());
            site5 = sitesProxy.createSite(site);

            site = new SiteImpl(site6_id, site6_title, site6_description, SiteVisibility.PRIVATE.toString());
            site6 = sitesProxy.createSite(site);
        }
    }

    private void initializeNetwork3WithSites() throws Exception
    {
        String siteDescription = "description";

        if (network3 == null)
        {
            network3 = getRepoService().createNetwork(this.getClass().getName().toLowerCase() + "-3-" + RUNID, true);
            network3.create();

            TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    person4Id = network3.createUser().getId();
                    return null;
                }
            }, network3.getId());

            TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    // Temporary solution for testing as for now creating a site
                    // with site preset is not allowed (please see REPO-194)
                    site7 = network3.createSite("a-" + GUID.generate(), "site A" + GUID.generate(), siteDescription, preset, SiteVisibility.PRIVATE);
                    site8 = network3.createSite("b-" + GUID.generate(), "site B" + GUID.generate(), siteDescription, preset, SiteVisibility.PUBLIC);
                    site9 = network3.createSite("c-" + GUID.generate(), "site C" + GUID.generate(), siteDescription, preset, SiteVisibility.PUBLIC);
                    site10 = network3.createSite("d-" + GUID.generate(), "site D" + GUID.generate(), siteDescription, "site-dashboard", SiteVisibility.MODERATED);
                    return null;
                }
            }, person4Id, network3.getId());
        }
    }

	@Test
	public void testGetSiteAndListSites() throws Exception
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
    }

    @Test
    public void testCreateAndDeleteSite() throws Exception
    {
        Sites sitesProxy = publicApiClient.sites();
        
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

            sitesProxy.removeSite(siteId); // cleanup
        }

        // test create using site id = "true" (RA-1101)
        {

            String siteId = "true";
            String siteTitle = "string";
            String siteDescription = "string";

            Site site = new SiteImpl(null, siteId, null, siteTitle, siteDescription, SiteVisibility.PUBLIC.toString(), null, null);

            sitesProxy.createSite(site);

            sitesProxy.removeSite(siteId); // cleanup
        }

        // -ve tests
        {
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2Id));
            
            SiteImpl site = new SiteImpl("a private site", SiteVisibility.PRIVATE.toString());
            String siteIdOfPrivateSite = sitesProxy.createSite(site, 201).getSiteId();

            site = new SiteImpl("a public site", SiteVisibility.PUBLIC.toString());
            String siteIdOfPublicSite = sitesProxy.createSite(site, 201).getSiteId();
            
            // invalid auth
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), GUID.generate(), "password"));
            sitesProxy.getSite(siteIdOfPrivateSite, 401);

            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1Id));

            // -ve - cannot view or delete a private site
            sitesProxy.getSite(siteIdOfPrivateSite, 404);
            sitesProxy.removeSite(siteIdOfPrivateSite, false, 404);
            
            // -ve - test cannot delete a public site (but can view it)
            sitesProxy.getSite(siteIdOfPublicSite, 200);
            sitesProxy.removeSite(siteIdOfPublicSite, false, 403);

            // cleanup
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2Id));
            sitesProxy.removeSite(siteIdOfPrivateSite, false, 204);
            sitesProxy.removeSite(siteIdOfPublicSite, false, 204);
            

            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1Id));

            // -ve - try to get unknown site
            sitesProxy.getSite(GUID.generate(), 404);

            site = new SiteImpl("my site 123", "invalidsitevisibility");
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

        // -ve test - create site with a given preset (REPO-194 and REPO-1523)
        {
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1Id));

            Site site = new SiteImpl().withPreset("testSitePreset");
            sitesProxy.createSite(site, 400);
        }
        // Test Case cloud-1478
		// Test Case cloud-1479
		// user invited to network and user invited to site
		// user invited to network and user not invited to site
	}

    @Test
    public void testUpdateSite() throws PublicApiException
    {
        Sites sitesProxy = publicApiClient.sites();
        publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1Id));


        // +ve test: simple happy path with successful update.
        {
            SiteImpl siteToCreate = new SiteImpl("Initial Title", SiteVisibility.PRIVATE.toString());
            siteToCreate.setRole(SiteRole.SiteManager);
            siteToCreate.setDescription("This is the initial description for the site");
            Site site = sitesProxy.createSite(siteToCreate, 201);

            // Testing with the actual JSON reveals more than testing with rich POJOs.
            // It shows us exactly what the input is and is easy to compare to the Open API spec.
            String updateJSON = "{\n" +
                    "  \"title\": \"Updated Title\",\n" +
                    "  \"description\": \"This is an updated description for the site\",\n" +
                    "  \"visibility\": \"PUBLIC\"\n" +
                    "}";
            // Update the site details
            HttpResponse response = sitesProxy.update("sites", site.getSiteId(), null, null, updateJSON, "Failed to update site " + site.getSiteId());

            Site updatedSite = SiteImpl.parseSite((JSONObject) response.getJsonResponse().get("entry"));
            Site expectedUpdate = new SiteImpl(null, "initial-title", site.getGuid(), "Updated Title", "This is an updated description for the site", SiteVisibility.PUBLIC.toString(), null, SiteRole.SiteManager);
            // Check the updated site is as expected.
            expectedUpdate.expected(updatedSite);

            // Double check by do a fresh get of the details, since the details returned by updateSite()
            // aren't necessarily retrieved from the DB.
            Site fresh = sitesProxy.getSite(site.getSiteId(), 200);
            expectedUpdate.expected(fresh);

            removeSiteQuietly(site.getSiteId());
        }

        // +ve test: remove the description.
        // This is a workaround for being unable to eplicitly null the field (see REPO-1409)
        {
            Site site = sitesProxy.createSite(new SiteImpl("NoDescriptionTest", SiteVisibility.PUBLIC.toString()), 201);

            Site updatedSite = sitesProxy.updateSite(
                    site.getSiteId(),
                    new SiteUpdate("title", "", SiteVisibility.PUBLIC),
                    200);

            // Note the null value for description, as the framework
            // treats empty strings and null as equivalent in responses, and does not return
            // those fields (as per API guidelines)
            Site expectedUpdate = new SiteImpl(
                    null,
                    "nodescriptiontest",
                    site.getGuid(),
                    "title",
                    null,
                    SiteVisibility.PUBLIC.toString(),
                    null,
                    SiteRole.SiteManager);

            // Check the return from updateSite(...)
            expectedUpdate.expected(updatedSite);

            // Double check a fresh retrieval matches.
            Site fresh = sitesProxy.getSite(site.getSiteId(), 200);
            expectedUpdate.expected(fresh);

            removeSiteQuietly(site.getSiteId());
        }

        // +ve test: partial updates must be allowed
        {
            Site site = sitesProxy.createSite(new SiteImpl(
                    "partial-updates-test",
                    "title-v1",
                    "description-v1",
                    SiteVisibility.PUBLIC.toString()),
                    201);

            // Check we can update just the title
            Site updatedSite = sitesProxy.updateSite(
                    site.getSiteId(),
                    new SiteUpdate("title-v2", null, null),
                    200);

            Site expectedUpdate = new SiteImpl(
                    null,
                    "partial-updates-test",
                    site.getGuid(),
                    "title-v2",
                    "description-v1",
                    SiteVisibility.PUBLIC.toString(),
                    null,
                    SiteRole.SiteManager);

            expectedUpdate.expected(updatedSite);

            // Check we can update just the description
            updatedSite = sitesProxy.updateSite(
                    site.getSiteId(),
                    new SiteUpdate(null, "description-v2", null),
                    200);

            expectedUpdate = new SiteImpl(
                    null,
                    "partial-updates-test",
                    site.getGuid(),
                    "title-v2",
                    "description-v2",
                    SiteVisibility.PUBLIC.toString(),
                    null,
                    SiteRole.SiteManager);

            expectedUpdate.expected(updatedSite);


            // Check we can update just the visibility
            updatedSite = sitesProxy.updateSite(
                    site.getSiteId(),
                    new SiteUpdate(null, null, SiteVisibility.PRIVATE),
                    200);

            expectedUpdate = new SiteImpl(
                    null,
                    "partial-updates-test",
                    site.getGuid(),
                    "title-v2",
                    "description-v2",
                    SiteVisibility.PRIVATE.toString(),
                    null,
                    SiteRole.SiteManager);

            expectedUpdate.expected(updatedSite);

            removeSiteQuietly(site.getSiteId());
        }

        // -ve test: 400, bad requests
        {
            Site site = sitesProxy.createSite(new SiteImpl("Site Update Bad Request Tests", SiteVisibility.PRIVATE.toString()), 201);

            // long title
            sitesProxy.updateSite(
                    site.getSiteId(),
                    new SiteUpdate(new String(new char[257]).replace('\0', 'a'), "description", SiteVisibility.PUBLIC),
                    400);

            // long description
            sitesProxy.updateSite(
                    site.getSiteId(),
                    new SiteUpdate("title", new String(new char[513]).replace('\0', 'a'), SiteVisibility.PUBLIC),
                    400);

            // Invalid visibility
            sitesProxy.update(
                    "sites",
                    site.getSiteId(),
                    null,
                    null,
                    "{\n" +
                    "  \"title\": \"Updated Title\",\n" +
                    "  \"description\": \"This is an updated description for the site\",\n" +
                    "  \"visibility\": \"INVALID_VISIBILITY\"\n" +
                    "}",
                    null,
                    "Expected 400 response when updating "+site.getSiteId(), 400);

            // Invalid fields
            // Check that id, guid and role are not silently ignored. This is until REPO-110
            // is implemented, since we currently have to bind to Site rather than SiteUpdate in
            // SiteEntityResource.update
            sitesProxy.update(
                    "sites",
                    site.getSiteId(),
                    null,
                    null,
                    "{\n" +
                    "  \"id\": \"a-new-id\"," +
                    "  \"title\": \"Updated Title\"\n" +
                    "}",
                    null,
                    "Expected 400 response when updating "+site.getSiteId(), 400);

            sitesProxy.update(
                    "sites",
                    site.getSiteId(),
                    null,
                    null,
                    "{\n" +
                    "  \"guid\": \"76ba60c1-f05b-406a-86a4-4eeb1bb49aaa\"" +
                    "}",
                    null,
                    "Expected 400 response when updating "+site.getSiteId(), 400);

            sitesProxy.update(
                    "sites",
                    site.getSiteId(),
                    null,
                    null,
                    "{\n" +
                    "  \"role\": \"SiteConsumer\"" +
                    "}",
                    null,
                    "Expected 400 response when updating "+site.getSiteId(), 400);

            sitesProxy.update(
                    "sites",
                    site.getSiteId(),
                    null,
                    null,
                    "{\n" +
                            "  \"preset\": \"sitePreset\"" +
                            "}",
                    null,
                    "Expected 400 response when updating "+site.getSiteId(), 400);

            // Details should not have changed.
            Site fresh = sitesProxy.getSite(site.getSiteId(), 200);
            site.expected(fresh);

            removeSiteQuietly(site.getSiteId());
        }

        // -ve test: 404 for non-existent site
        {
            // Attempt to update site details using an invalid site ID.
            sitesProxy.updateSite(
                    UUID.randomUUID().toString(), // Non-existent site.
                    new SiteUpdate("Updated title", "Updated description", SiteVisibility.PUBLIC),
                    HttpStatus.SC_NOT_FOUND);
        }

        // -ve test: 401 authorization failure
        {
            Site site = sitesProxy.createSite(new SiteImpl("SiteUpdate401", SiteVisibility.PRIVATE.toString()), 201);

            // Invalid auth details
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), GUID.generate(), "password"));

            // Update the site details
            sitesProxy.updateSite(
                    site.getSiteId(),
                    new SiteUpdate("Updated Title", "Updated description", SiteVisibility.PUBLIC),
                    HttpStatus.SC_UNAUTHORIZED);

            // Valid authentication again
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1Id));

            // Check the details haven't changed.
            Site fresh = sitesProxy.getSite(site.getSiteId(), 200);
            site.expected(fresh);

            removeSiteQuietly(site.getSiteId());
        }

        // -ve test: 403 for incorrect permissions
        {
            Site site = sitesProxy.createSite(new SiteImpl("SiteUpdate403", SiteVisibility.PUBLIC.toString()), 201);

            // Update as person2
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2Id));

            // Update the site details
            sitesProxy.updateSite(
                    site.getSiteId(),
                    new SiteUpdate("Updated Title", "Updated description", SiteVisibility.PUBLIC),
                    HttpStatus.SC_FORBIDDEN);

            // Check details haven't changed as original creator.
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1Id));
            Site fresh = sitesProxy.getSite(site.getSiteId(), 200);
            site.expected(fresh);

            removeSiteQuietly(site.getSiteId());
        }
    }

    private void removeSiteQuietly(String siteId) throws PublicApiException
    {
        Sites sitesProxy = publicApiClient.sites();
        try
        {
            sitesProxy.removeSite(siteId, true, 204);
        }
        catch(PublicApiException e)
        {
            // If it doesn't exist currently, that's fine.
            if (e.getHttpResponse().getStatusCode() != 404)
            {
                throw e;
            }
        }
    }

    /**
     * Tests the capability to sort and paginate the sites list
     * orderBy = title ASC skip = 1, count = 2
     *
     * @throws Exception
     */
    public void testSortingAndPagingByTitleAsc() throws Exception
    {
        // paging
        int skipCount = 1;
        int maxItems = 2;
        int totalResults = 3;
        Paging paging = getPaging(skipCount, maxItems, totalResults, totalResults);

        // list sites
        ListResponse<Site> resp = listSites(paging, "title", true);

        // check results
        List<SiteImpl> expectedList = new LinkedList<>();
        expectedList.add((SiteImpl)site6);
        expectedList.add((SiteImpl)site4);

        checkList(expectedList, paging.getExpectedPaging(), resp);

    }

    /**
     * Tests the capability to sort and paginate the sites list
     * orderBy = title DESC skip = 1, count = 2
     *
     * @throws Exception
     */
    public void testSortingAndPagingByTitleDesc() throws Exception
    {
        // paging
        int skipCount = 1;
        int maxItems = 2;
        int totalResults = 3;
        Paging paging = getPaging(skipCount, maxItems, totalResults, totalResults);

        // list sites
        ListResponse<Site> resp = listSites(paging, "title", false);

        // check results
        List<SiteImpl> expectedList = new LinkedList<>();
        expectedList.add((SiteImpl)site6);
        expectedList.add((SiteImpl)site5);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability to sort and paginate the sites list
     * orderBy = description ASC skip = 1, count = 2
     *
     * @throws Exception
     */
    public void testSortingAndPagingByDescriptionAsc() throws Exception
    {
        // paging
        int skipCount = 1;
        int maxItems = 2;
        int totalResults = 3;
        Paging paging = getPaging(skipCount, maxItems, totalResults, totalResults);

        // list sites
        ListResponse<Site> resp = listSites(paging, "description", true);

        // check results
        List<SiteImpl> expectedList = new LinkedList<>();
        expectedList.add((SiteImpl)site4);
        expectedList.add((SiteImpl)site5);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability to sort and paginate the sites list
     * orderBy = description DESC skip = 1, count = 2
     *
     * @throws Exception
     */
    public void testSortingAndPagingByDescriptionDesc() throws Exception
    {
        // paging
        int skipCount = 1;
        int maxItems = 2;
        int totalResults = 3;
        Paging paging = getPaging(skipCount, maxItems, totalResults, totalResults);

        // list sites
        ListResponse<Site> resp = listSites(paging, "description", false);

        // check results
        List<SiteImpl> expectedList = new LinkedList<>();
        expectedList.add((SiteImpl)site4);
        expectedList.add((SiteImpl)site6);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability to sort and paginate the sites list
     * orderBy = id ASC skip = 1, count = 2
     *
     * @throws Exception
     */
    public void testSortingAndPagingBySiteIdAsc() throws Exception
    {
        // paging
        int skipCount = 1;
        int maxItems = 2;
        int totalResults = 3;
        Paging paging = getPaging(skipCount, maxItems, totalResults, totalResults);

        // list sites
        ListResponse<Site> resp = listSites(paging, "id", true);

        // check results
        List<SiteImpl> expectedList = new LinkedList<>();
        expectedList.add((SiteImpl)site5);
        expectedList.add((SiteImpl)site6);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability to sort and paginate the sites list
     * orderBy = id DESC skip = 1, count = 2
     *
     * @throws Exception
     */
    public void testSortingAndPagingBySiteIdDesc() throws Exception
    {
        // paging
        int skipCount = 1;
        int maxItems = 2;
        int totalResults = 3;
        Paging paging = getPaging(skipCount, maxItems, totalResults, totalResults);

        // list sites
        ListResponse<Site> resp = listSites(paging, "id", false);

        // check results
        List<SiteImpl> expectedList = new LinkedList<>();
        expectedList.add((SiteImpl)site5);
        expectedList.add((SiteImpl)site4);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability to sort and paginate the sites list
     * default sorting (id asc), all results
     *
     * @throws Exception
     */
    public void testSortingAndPagingDefault() throws Exception
    {
        // paging
        int totalResults = 3;
        Paging paging = getPaging(null, null, totalResults, totalResults);

        // list sites
        ListResponse<Site> resp = listSites(null, null, false);

        // check results
        List<SiteImpl> expectedList = new LinkedList<>();
        expectedList.add((SiteImpl)site5);
        expectedList.add((SiteImpl)site6);
        expectedList.add((SiteImpl)site4);

        checkList(expectedList, paging.getExpectedPaging(), resp);

    }

    // based on TestPersonSites.testSortingAndPaging
    @Test
    public void testSortingAndPaging() throws Exception
    {
        initializeNetwork2WithSites();
        
        publicApiClient.setRequestContext(new RequestContext(network2.getId(), person3Id));

        testSortingAndPagingByTitleAsc();
        testSortingAndPagingByTitleDesc();
        testSortingAndPagingByDescriptionAsc();
        testSortingAndPagingByDescriptionDesc();
        testSortingAndPagingBySiteIdAsc();
        testSortingAndPagingBySiteIdDesc();
        testSortingAndPagingDefault();
    }

    private ListResponse<Site> listSites(final Paging paging, String sortColumn, boolean asc) throws Exception
    {
        final Sites sitesProxy = publicApiClient.sites();
        
        // sort params
        final Map<String, String> params = new HashMap<String, String>();
        if (sortColumn != null)
        {
            params.put("orderBy", sortColumn + " " + (asc ? "ASC" : "DESC"));
        }

       return sitesProxy.getSites(createParams(paging, params));
    }

    private ListResponse<Site> listSitesWithWhere(final Paging paging, Map<String, String> filters, String predicate) throws Exception
    {
        final Sites sitesProxy = publicApiClient.sites();

        String visibility = filters.get("visibility");
        String preset = filters.get("preset");
        String pred = predicate;

        final Map<String, String> params = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        if (visibility != null || preset != null)
        {
            sb.append("(");
            sb.append(visibility != null ? "visibility=" + visibility : "");
            sb.append(pred != null ? pred : "");
            sb.append(preset != null ? "preset='" + preset + "'" : "");
            sb.append(")");
        }
        params.put("where", sb.toString());

        return sitesProxy.getSites(createParams(paging, params));
    }

    public void testListSitesWhereSiteVisibilityPrivate() throws Exception
    {
        // paging
        int totalResults = 1;
        Paging paging = getPaging(null, null, totalResults, totalResults);

        Map<String, String> filters = new HashMap<String, String>();
        filters.put("visibility", SiteVisibility.PRIVATE.name());

        // list sites
        ListResponse<Site> resp =  listSitesWithWhere(null, filters, null);

        // check results
        List<SiteImpl> expectedList = new LinkedList<>();
        expectedList.add((SiteImpl) site7);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    public void testListSitesWhereSiteVisibilityPublic() throws Exception
    {
        // paging
        int totalResults = 2;
        Paging paging = getPaging(null, null, totalResults, totalResults);

        Map<String, String> filters = new HashMap<String, String>();
        filters.put("visibility", SiteVisibility.PUBLIC.name());

        // list sites
        ListResponse<Site> resp = listSitesWithWhere(null, filters, null);

        // check results
        List<SiteImpl> expectedList = new LinkedList<>();
        expectedList.add((SiteImpl) site8);
        expectedList.add((SiteImpl) site9);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    public void testListSitesWhereSiteVisibilityPublicAndSkipCount() throws Exception
    {
        // paging
        Integer skipCount = 1;
        int maxItems = 2;
        int totalResults = 2;
        Paging paging = getPaging(skipCount, maxItems, totalResults, totalResults);

        Map<String, String> filters = new HashMap<String, String>();
        filters.put("visibility", SiteVisibility.PUBLIC.name());

        // list sites
        ListResponse<Site> resp = listSitesWithWhere(paging, filters, null);

        // check results
        List<SiteImpl> expectedList = new LinkedList<>();
        expectedList.add((SiteImpl) site9);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    public void testListSitesWhereSiteVisibilityModerated() throws Exception
    {
        // paging
        int totalResults = 1;
        Paging paging = getPaging(null, null, totalResults, totalResults);

        Map<String, String> filters = new HashMap<String, String>();
        filters.put("visibility", SiteVisibility.MODERATED.name());

        // list sites
        ListResponse<Site> resp = listSitesWithWhere(null, filters, null);

        // check results
        List<SiteImpl> expectedList = new LinkedList<>();
        expectedList.add((SiteImpl) site10);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    public void testListSitesWhereSiteVisibilityInvalid() throws Exception
    {
        try
        {
            Map<String, String> filters = new HashMap<String, String>();
            filters.put("visibility", "invalidVisibility");

            listSitesWithWhere(null, filters, null);
            fail("");
        }
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
        }
    }

    public void testListSitesWhereByVisibilityORPreset() throws Exception
    {
        // paging
        int totalResults = 2;
        Paging paging = getPaging(null, null, totalResults, totalResults);

        Map<String, String> filters = new HashMap<String, String>();
        filters.put("visibility", SiteVisibility.PRIVATE.name());
        filters.put("preset", "site-dashboard");

        ListResponse<Site> resp =  listSitesWithWhere(null, filters, OR_PREDICATE);

        // check results
        List<SiteImpl> expectedList = new LinkedList<>();
        expectedList.add((SiteImpl) site7);
        expectedList.add((SiteImpl) site10);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    public void testListSitesWherePreset() throws Exception
    {
        // paging
        int totalResults = 1;
        Paging paging = getPaging(null, null, totalResults, totalResults);

        Map<String, String> filters = new HashMap<String, String>();
        filters.put("preset", "site-dashboard");
        ListResponse<Site> resp = listSitesWithWhere(null, filters, null);
        // check results
        List<SiteImpl> expectedList = new LinkedList<>();
        expectedList.add((SiteImpl) site10);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    public void testListSitesWherePresetNonexistent() throws Exception
    {
        Map<String, String> filters = new HashMap<String, String>();
        filters.put("preset", "nonexistentPreset");
        listSitesWithWhere(null, filters, null);
    }

    public void testListSitesWhereVisibilityANDPreset() throws Exception
    {
        try
        {
            Map<String, String> filters = new HashMap<String, String>();
            filters.put("visibility", SiteVisibility.PUBLIC.name());
            filters.put("preset", "sitePreset");
            listSitesWithWhere(null, filters, AND_PREDICATE);
        }
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
        }
    }
    @Test
    public void testListSitesWhereExpected() throws Exception
    {
        initializeNetwork3WithSites();
        publicApiClient.setRequestContext(new RequestContext(network3.getId(), person4Id));

        testListSitesWhereSiteVisibilityPrivate();
        testListSitesWhereSiteVisibilityPublic();
        testListSitesWhereSiteVisibilityPublicAndSkipCount();
        testListSitesWhereSiteVisibilityModerated();
        testListSitesWhereSiteVisibilityInvalid();

        testListSitesWherePreset();
        testListSitesWherePresetNonexistent();
        testListSitesWhereVisibilityANDPreset();
        testListSitesWhereByVisibilityORPreset();
    }


    public static class SiteUpdateJSONSerializer implements JSONAble
    {
        private SiteUpdate siteUpdate;

        public SiteUpdateJSONSerializer(SiteUpdate siteUpdate)
        {
            this.siteUpdate = siteUpdate;
        }

        @SuppressWarnings("unchecked")
        @Override
        public JSONObject toJSON()
        {
            JSONObject json = new JSONObject();

            if (siteUpdate.getTitle() != null)
            {
                json.put("title", siteUpdate.getTitle());
            }
            if (siteUpdate.getDescription() != null)
            {
                json.put("description", siteUpdate.getDescription());
            }
            if (siteUpdate.getVisibility() != null)
            {
                // JSONObject bug: if we don't toString() the visibility, it won't put quotes around it.
                json.put("visibility", siteUpdate.getVisibility().toString());
            }

            return json;
        }

        public static SiteUpdate parseSiteUpdate(JSONObject jsonObject)
        {
            String title = (String) jsonObject.get("title");
            String description = (String) jsonObject.get("description");
            SiteVisibility visibility = SiteVisibility.valueOf((String) jsonObject.get("visibility"));

            return new SiteUpdate(title, description, visibility);
        }
    }

    public static class SiteUpdateAssertions
    {
        public static void assertEquals(SiteUpdate expected, SiteUpdate actual)
        {
            // Could separate these out into discreet field assertions.
            assertEquals(expected, actual);
        }
    }
}
