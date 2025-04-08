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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.rest.api.tests.RepoService.SiteInformation;
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

public class TestPersonSites extends EnterpriseTestApi
{
    private TestNetwork network1;
    private TestNetwork network2;
    private TestNetwork network4;

    private TestPerson person11;
    private TestPerson person12;
    private TestPerson person21;
    private TestPerson person41;
    private TestPerson person42;

    private List<TestSite> sites = new ArrayList<>(10);

    /* Sites and users used to test the site sorting */
    private TestPerson person31;
    private TestPerson person32;
    private TestSite site1;
    private TestSite site2;
    private TestSite site3;
    private String site1_id = "a_" + GUID.generate();
    private String site1_title = "c_" + GUID.generate();
    private SiteRole site1_role = SiteRole.SiteContributor;
    private String site2_id = "b_" + GUID.generate();
    private String site2_title = "a_" + GUID.generate();
    private SiteRole site2_role = SiteRole.SiteManager;
    private String site3_id = "c_" + GUID.generate();
    private String site3_title = "b_" + GUID.generate();
    private SiteRole site3_role = SiteRole.SiteConsumer;

    private TestSite site41;
    private TestSite site42;
    private TestSite site43;
    private TestSite site44;

    @Override
    @Before
    public void setup() throws Exception
    {
        // init networks
        super.setup();

        Iterator<TestNetwork> networksIt = getTestFixture().getNetworksIt();

        assertTrue(networksIt.hasNext());
        this.network1 = networksIt.next();

        assertTrue(networksIt.hasNext());
        this.network2 = networksIt.next();

        // create a user

        final List<TestPerson> people = new ArrayList<TestPerson>(1);

        // Create some users
        TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>() {
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

        TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>() {
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
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>() {
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

                // Special site for person removal
                site = network1.createSite(SiteVisibility.PRIVATE);
                site.inviteToSite(person11.getId(), SiteRole.SiteConsumer);
                sites.add(site);
                return null;
            }
        }, person12.getId(), network1.getId());
    }

    // TODO switch to use V1 createSite (instead of RepoService)
    private void initializeSites() throws Exception
    {
        /* Create data for testing the site sorting. We create the sites as person31 and assign roles to person32. The list requests will be performed as person32. */
        TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception
            {
                person31 = network1.createUser();
                person32 = network1.createUser();
                return null;
            }
        }, network1.getId());

        this.site1 = TenantUtil.runAsUserTenant(new TenantRunAsWork<TestSite>() {
            @Override
            public TestSite doWork() throws Exception
            {
                SiteInformation siteInfo = new SiteInformation(site1_id, site1_title, site1_title, SiteVisibility.PRIVATE);
                TestSite site = network1.createSite(siteInfo);
                site.inviteToSite(person32.getId(), site1_role);
                return site;
            }
        }, person31.getId(), network1.getId());

        this.site2 = TenantUtil.runAsUserTenant(new TenantRunAsWork<TestSite>() {
            @Override
            public TestSite doWork() throws Exception
            {
                SiteInformation siteInfo = new SiteInformation(site2_id, site2_title, site2_title, SiteVisibility.PRIVATE);
                TestSite site = network1.createSite(siteInfo);
                site.inviteToSite(person32.getId(), site2_role);
                return site;
            }
        }, person31.getId(), network1.getId());

        this.site3 = TenantUtil.runAsUserTenant(new TenantRunAsWork<TestSite>() {
            @Override
            public TestSite doWork() throws Exception
            {
                SiteInformation siteInfo = new SiteInformation(site3_id, site3_title, site3_title, SiteVisibility.PRIVATE);
                TestSite site = network1.createSite(siteInfo);
                site.inviteToSite(person32.getId(), site3_role);
                return site;
            }
        }, person31.getId(), network1.getId());
    }

    private void initializePersonAndNetwork4WithSites() throws Exception
    {
        if (network4 == null)
        {
            network4 = getRepoService().createNetwork(this.getClass().getSimpleName().toLowerCase() + "-3-" + GUID.generate(), true);
            network4.create();

            // Create some users
            TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>() {
                @Override
                public Void doWork() throws Exception
                {
                    person41 = network4.createUser();
                    person42 = network4.createUser();
                    return null;
                }
            }, network4.getId());

            // ...and some sites
            TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>() {
                @Override
                public Void doWork() throws Exception
                {
                    site41 = network4.createSite("A", SiteVisibility.PRIVATE);
                    site41.inviteToSite(person41.getId(), SiteRole.SiteContributor);

                    site42 = network4.createSite("B", SiteVisibility.PUBLIC);
                    site42.inviteToSite(person41.getId(), SiteRole.SiteContributor);

                    site43 = network4.createSite("C", SiteVisibility.PUBLIC);
                    site43.inviteToSite(person41.getId(), SiteRole.SiteContributor);

                    site44 = network4.createSite("D", SiteVisibility.MODERATED);
                    site44.inviteToSite(person41.getId(), SiteRole.SiteContributor);
                    return null;
                }
            }, person42.getId(), network4.getId());
        }
    }

    @Test
    public void testPersonSites() throws Exception
    {
        Set<MemberOfSite> personSites = new TreeSet<MemberOfSite>();

        // Get last site for use with personRemoveSite
        TestSite personRemoveSite = sites.get(sites.size() - 1);
        sites.remove(sites.size() - 1);

        personSites.addAll(network1.getSiteMemberships(person11.getId()));

        // Create some sites
        personSites.addAll(TenantUtil.runAsUserTenant(new TenantRunAsWork<List<MemberOfSite>>() {
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

        personSites.addAll(TenantUtil.runAsUserTenant(new TenantRunAsWork<List<MemberOfSite>>() {
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
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
        }

        // Test Case cloud-2200
        // Test Case cloud-2213
        // user should be able to list their sites
        {
            int skipCount = 0;
            int maxItems = 2;
            Paging paging = getPaging(skipCount, maxItems, expectedSites.size(), expectedSites.size());
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
            ListResponse<MemberOfSite> resp = sitesProxy.getPersonSites(person11.getId(), createParams(paging, null));
            checkList(expectedSites.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
        }

        {
            int skipCount = 2;
            int maxItems = 8;
            Paging paging = getPaging(skipCount, maxItems, expectedSites.size(), expectedSites.size());
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
            ListResponse<MemberOfSite> resp = sitesProxy.getPersonSites(person11.getId(), createParams(paging, null));
            checkList(expectedSites.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
        }

        // "-me-" user
        {
            int skipCount = 0;
            int maxItems = 2;
            Paging paging = getPaging(skipCount, maxItems, expectedSites.size(), expectedSites.size());
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
        catch (PublicApiException e)
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
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
        }

        try
        {
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
            sitesProxy.getPersonSite(person11.getId(), GUID.generate());
            fail();
        }
        catch (PublicApiException e)
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
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
        }

        try
        {
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
            sitesProxy.getPersonSite(person11.getId(), GUID.generate());
            fail();
        }
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
        }

        {
            // Tests removing a person from the site
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
            sitesProxy.remove("people", person11.getId(), "sites", personRemoveSite.getSiteId(), "Unable to DELETE a person site");

            try
            {
                sitesProxy.getPersonSite(person11.getId(), personRemoveSite.getSiteId());
            }
            catch (PublicApiException e)
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
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
        }

        try
        {
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
            sitesProxy.create("people", person11.getId(), "sites", null, null, "Unable to POST to person sites");
            fail();
        }
        catch (PublicApiException e)
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
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
        }

        try
        {
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
            sitesProxy.update("people", person11.getId(), "sites", null, null, "Unable to PUT person sites");
            fail();
        }
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
        }

        try
        {
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
            sitesProxy.remove("people", person11.getId(), "sites", null, "Unable to DELETE person sites");
            fail();
        }
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
        }
    }

    /**
     * Retrieves the site memberships associated to a user
     *
     * @param sortColumn
     * @param asc
     * @return
     * @throws Exception
     */
    private ListResponse<MemberOfSite> getSiteMembershipsForPerson32(final Paging paging, String sortColumn, boolean asc) throws Exception
    {
        final Sites sitesProxy = publicApiClient.sites();
        publicApiClient.setRequestContext(new RequestContext(network1.getId(), person32.getId()));

        // sort params
        final Map<String, String> params = new HashMap<String, String>();
        if (sortColumn != null)
        {
            params.put("orderBy", sortColumn + " " + (asc ? "ASC" : "DESC"));
        }

        // get memberships
        ListResponse<MemberOfSite> resp = TenantUtil.runAsUserTenant(new TenantRunAsWork<ListResponse<MemberOfSite>>() {
            @Override
            public ListResponse<MemberOfSite> doWork() throws Exception
            {
                ListResponse<MemberOfSite> resp = sitesProxy.getPersonSites(person32.getId(), createParams(paging, params));

                return resp;
            }
        }, person32.getId(), network1.getId());

        return resp;
    }

    /**
     * Tests the capability to sort and paginate the site memberships associated to a user orderBy = title ASC skip = 1, count = 2
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

        // get memberships
        ListResponse<MemberOfSite> resp = getSiteMembershipsForPerson32(paging, "title", true);

        // check results
        List<MemberOfSite> expectedList = new LinkedList<>();
        expectedList.add(new MemberOfSite(site3, site3_role));
        expectedList.add(new MemberOfSite(site1, site1_role));

        checkList(expectedList, paging.getExpectedPaging(), resp);

    }

    /**
     * Tests the capability to sort and paginate the site memberships associated to a user orderBy = title DESC skip = 1, count = 2
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

        // get memberships
        ListResponse<MemberOfSite> resp = getSiteMembershipsForPerson32(paging, "title", false);

        // check results
        List<MemberOfSite> expectedList = new LinkedList<>();
        expectedList.add(new MemberOfSite(site3, site3_role));
        expectedList.add(new MemberOfSite(site2, site2_role));

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability to sort and paginate the site memberships associated to a user orderBy = role ASC skip = 1, count = 2
     *
     * @throws Exception
     */
    public void testSortingAndPagingByRoleAsc() throws Exception
    {
        // paging
        int skipCount = 1;
        int maxItems = 2;
        int totalResults = 3;
        Paging paging = getPaging(skipCount, maxItems, totalResults, totalResults);

        // get memberships
        ListResponse<MemberOfSite> resp = getSiteMembershipsForPerson32(paging, "role", true);

        // check results
        List<MemberOfSite> expectedList = new LinkedList<>();
        expectedList.add(new MemberOfSite(site1, site1_role));
        expectedList.add(new MemberOfSite(site2, site2_role));

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability to sort and paginate the site memberships associated to a user orderBy = role DESC skip = 1, count = 2
     *
     * @throws Exception
     */
    public void testSortingAndPagingByRoleDesc() throws Exception
    {
        // paging
        int skipCount = 1;
        int maxItems = 2;
        int totalResults = 3;
        Paging paging = getPaging(skipCount, maxItems, totalResults, totalResults);

        // get memberships
        ListResponse<MemberOfSite> resp = getSiteMembershipsForPerson32(paging, "role", false);

        // check results
        List<MemberOfSite> expectedList = new LinkedList<>();
        expectedList.add(new MemberOfSite(site1, site1_role));
        expectedList.add(new MemberOfSite(site3, site3_role));

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability to sort and paginate the site memberships associated to a user orderBy = id ASC skip = 1, count = 2
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

        // get memberships
        ListResponse<MemberOfSite> resp = getSiteMembershipsForPerson32(paging, "id", true);

        // check results
        List<MemberOfSite> expectedList = new LinkedList<>();
        expectedList.add(new MemberOfSite(site2, site2_role));
        expectedList.add(new MemberOfSite(site3, site3_role));

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability to sort and paginate the site memberships associated to a user orderBy = id DESC skip = 1, count = 2
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

        // get memberships
        ListResponse<MemberOfSite> resp = getSiteMembershipsForPerson32(paging, "id", false);

        // check results
        List<MemberOfSite> expectedList = new LinkedList<>();
        expectedList.add(new MemberOfSite(site2, site2_role));
        expectedList.add(new MemberOfSite(site1, site1_role));

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability to sort and paginate the site memberships associated default sorting (title asc), all results
     *
     * @throws Exception
     */
    public void testSortingAndPagingDefault() throws Exception
    {
        // paging
        int totalResults = 3;
        Paging paging = getPaging(null, null, totalResults, totalResults);

        // get memberships
        ListResponse<MemberOfSite> resp = getSiteMembershipsForPerson32(null, null, false);

        // check results
        List<MemberOfSite> expectedList = new LinkedList<>();
        expectedList.add(new MemberOfSite(site2, site2_role));
        expectedList.add(new MemberOfSite(site3, site3_role));
        expectedList.add(new MemberOfSite(site1, site1_role));

        checkList(expectedList, paging.getExpectedPaging(), resp);

    }

    // see also TestSites.testSortingAndPaging
    @Test
    public void testSortingAndPaging() throws Exception
    {
        initializeSites();

        testSortingAndPagingByTitleAsc();
        testSortingAndPagingByTitleDesc();
        testSortingAndPagingByRoleAsc();
        testSortingAndPagingByRoleDesc();
        testSortingAndPagingBySiteIdAsc();
        testSortingAndPagingBySiteIdDesc();
        testSortingAndPagingDefault();
    }

    // ACE-4823
    @Test
    public void testSitesWithSameTitles() throws Exception
    {
        // Creates 3 sites
        initializeSites();

        final String site4_name = "d_" + GUID.generate();
        final String site4_title = site3_title; // Same title as site3
        final SiteRole site4_role = SiteRole.SiteCollaborator;

        TestSite site4 = TenantUtil.runAsUserTenant(new TenantRunAsWork<TestSite>() {
            @Override
            public TestSite doWork() throws Exception
            {
                SiteInformation siteInfo = new SiteInformation(site4_name, site4_title, site4_title, SiteVisibility.PRIVATE);
                TestSite site = network1.createSite(siteInfo);
                site.inviteToSite(person32.getId(), site4_role);
                return site;
            }
        }, person31.getId(), network1.getId());
        assertNotNull(site4);

        // paging
        int totalResults = 4;
        Paging paging = getPaging(null, null, totalResults, totalResults);

        // get memberships
        ListResponse<MemberOfSite> resp = getSiteMembershipsForPerson32(null, null, false);

        // check results
        List<MemberOfSite> expectedList = new LinkedList<>();
        expectedList.add(new MemberOfSite(site2, site2_role));
        expectedList.add(new MemberOfSite(site3, site3_role));
        expectedList.add(new MemberOfSite(site4, site4_role));
        expectedList.add(new MemberOfSite(site1, site1_role));

        try
        {
            checkList(expectedList, paging.getExpectedPaging(), resp);
        }
        catch (AssertionError error)
        {
            // Site3 and Site4 have a same title, and as we are sorting on titles (default sorting),
            // we can't guarantee the order in which the sites will
            // return, hence swap the sites and compare again.
            Collections.swap(expectedList, 1, 2);
            checkList(expectedList, paging.getExpectedPaging(), resp);
        }
    }

    /**
     * Retrieves the site memberships associated to a user.
     *
     * @param paging
     *            The paging object
     * @param params
     *            Public api parameters map.
     * @param person
     *            The test person.
     * @param network
     *            The test network.
     * @return The site memberships associated to the give user.
     * @throws Exception
     */
    private ListResponse<MemberOfSite> getSiteMembershipsForPersonAndNetwork(final Paging paging, Map<String, String> params, TestPerson person, TestNetwork network, boolean runAsUserTenant)
            throws Exception
    {
        final Sites sitesProxy = publicApiClient.sites();
        publicApiClient.setRequestContext(new RequestContext(network.getId(), person.getId()));

        ListResponse<MemberOfSite> resp;
        if (runAsUserTenant)
        {
            // get memberships
            resp = TenantUtil.runAsUserTenant(new TenantRunAsWork<ListResponse<MemberOfSite>>() {
                @Override
                public ListResponse<MemberOfSite> doWork() throws Exception
                {
                    ListResponse<MemberOfSite> resp = sitesProxy.getPersonSites(person.getId(), createParams(paging, params));
                    return resp;
                }
            }, person.getId(), network.getId());
        }
        else
        {
            resp = sitesProxy.getPersonSites(person.getId(), createParams(paging, params));
        }

        return resp;
    }

    private List<MemberOfSite> getPersonSites(TestPerson person, TestSite... sites) throws PublicApiException
    {
        Sites sitesProxy = publicApiClient.sites();

        List<MemberOfSite> memberOfSiteList = new ArrayList<>();
        for (TestSite site : sites)
        {
            memberOfSiteList.add(sitesProxy.getPersonSite(person.getId(), site.getSiteId()));
        }

        return memberOfSiteList;
    }

    private ListResponse<MemberOfSite> getSiteMembershipsForPerson41(final Paging paging, String siteVisibility, boolean runAsUserTenant) throws Exception
    {
        final Map<String, String> params = new HashMap<>();
        params.put("orderBy", "title" + " " + "ASC");

        if (siteVisibility != null)
        {
            params.put("where", "(visibility=" + siteVisibility + ")");
        }

        return getSiteMembershipsForPersonAndNetwork(paging, params, person41, network4, runAsUserTenant);
    }

    private ListResponse<MemberOfSite> getSiteMembershipsForPerson41NOTWhere(final Paging paging, String siteVisibility, boolean runAsUserTenant) throws Exception
    {
        final Map<String, String> params = new HashMap<>();
        params.put("orderBy", "title" + " " + "ASC");

        if (siteVisibility != null)
        {
            params.put("where", "(NOT visibility=" + siteVisibility + ")");
        }

        return getSiteMembershipsForPersonAndNetwork(paging, params, person41, network4, runAsUserTenant);
    }

    private ListResponse<MemberOfSite> getSiteMembershipsForPerson41(final Paging paging, String siteVisibility) throws Exception
    {
        return getSiteMembershipsForPerson41(paging, siteVisibility, true);
    }

    public void testGetSiteMembershipsWhereSiteVisibilityPrivate() throws Exception
    {
        // paging
        int totalResults = 1;
        Paging paging = getPaging(null, null, totalResults, totalResults);

        // list sites
        ListResponse<MemberOfSite> resp = getSiteMembershipsForPerson41(null, SiteVisibility.PRIVATE.name());

        // check results
        List<MemberOfSite> expectedList = getPersonSites(person41, site41);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    public void testGetSiteMembershipsWhereSiteVisibilityPublic() throws Exception
    {
        // paging
        int totalResults = 2;
        Paging paging = getPaging(null, null, totalResults, totalResults);

        // list sites
        ListResponse<MemberOfSite> resp = getSiteMembershipsForPerson41(null, SiteVisibility.PUBLIC.name());

        // check results
        List<MemberOfSite> expectedList = getPersonSites(person41, site42, site43);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    public void testGetSiteMembershipsWhereSiteVisibilityPublicAndSkipCount() throws Exception
    {
        // paging
        Integer skipCount = 1;
        int maxItems = 2;
        int totalResults = 2;
        Paging paging = getPaging(skipCount, maxItems, totalResults, totalResults);

        // list sites
        ListResponse<MemberOfSite> resp = getSiteMembershipsForPerson41(paging, SiteVisibility.PUBLIC.name());

        // check results
        List<MemberOfSite> expectedList = getPersonSites(person41, site43);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    public void testGetSiteMembershipsWhereSiteVisibilityModerated() throws Exception
    {
        // paging
        int totalResults = 1;
        Paging paging = getPaging(null, null, totalResults, totalResults);

        // list sites
        ListResponse<MemberOfSite> resp = getSiteMembershipsForPerson41(null, SiteVisibility.MODERATED.name());

        // check results
        List<MemberOfSite> expectedList = getPersonSites(person41, site44);

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    public void testGetSiteMembershipsWhereSiteVisibilityInvalid() throws Exception
    {
        try
        {
            getSiteMembershipsForPerson41(null, "invalidVisibility", false);
            fail("");
        }
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
        }
    }

    public void testGetSiteMembershipsWhereSiteVisibilityNOTIncluded() throws Exception
    {
        try
        {
            getSiteMembershipsForPerson41NOTWhere(null, SiteVisibility.MODERATED.name(), false);
            fail("");
        }
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
        }
    }

    @Test
    public void testGetSiteMembershipsWithWhereClause() throws Exception
    {
        initializePersonAndNetwork4WithSites();
        publicApiClient.setRequestContext(new RequestContext(network4.getId(), person41.getId()));

        testGetSiteMembershipsWhereSiteVisibilityPrivate();
        testGetSiteMembershipsWhereSiteVisibilityPublic();
        testGetSiteMembershipsWhereSiteVisibilityPublicAndSkipCount();
        testGetSiteMembershipsWhereSiteVisibilityModerated();
        testGetSiteMembershipsWhereSiteVisibilityInvalid();
        testGetSiteMembershipsWhereSiteVisibilityNOTIncluded();
    }

}
