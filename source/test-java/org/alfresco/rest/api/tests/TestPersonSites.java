/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

    private List<TestSite> sites = new ArrayList<>(10);

    /* Sites and users used to test the site sorting */
    private TestPerson person31;
    private TestPerson person32;
    private TestSite site1;
    private TestSite site2;
    private TestSite site3;
    private String site1_name = "a_" + GUID.generate();
    private String site1_title = "c_" + GUID.generate();
    private SiteRole site1_role = SiteRole.SiteContributor;
    private String site2_name = "b_" + GUID.generate();
    private String site2_title = "a_" + GUID.generate();
    private SiteRole site2_role = SiteRole.SiteManager;
    private String site3_name = "c_" + GUID.generate();
    private String site3_title = "b_" + GUID.generate();
    private SiteRole site3_role = SiteRole.SiteConsumer;


    public void initializeSites() throws Exception
    {
        /*
         * Create data for testing the site sorting. We create the sites as
         * person31 and assign roles to person32. The list requests will be
         * performed as person32.
         */
        TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                person31 = network1.createUser();
                person32 = network1.createUser();
                return null;
            }
        }, network1.getId());

        this.site1 = TenantUtil.runAsUserTenant(new TenantRunAsWork<TestSite>()
        {
            @Override
            public TestSite doWork() throws Exception
            {
                SiteInformation siteInfo = new SiteInformation(site1_name, site1_title, site1_title, SiteVisibility.PRIVATE);
                TestSite site = network1.createSite(siteInfo);
                site.inviteToSite(person32.getId(), site1_role);
                return site;
            }
        }, person31.getId(), network1.getId());

        this.site2 = TenantUtil.runAsUserTenant(new TenantRunAsWork<TestSite>()
        {
            @Override
            public TestSite doWork() throws Exception
            {
                SiteInformation siteInfo = new SiteInformation(site2_name, site2_title, site2_title, SiteVisibility.PRIVATE);
                TestSite site = network1.createSite(siteInfo);
                site.inviteToSite(person32.getId(), site2_role);
                return site;
            }
        }, person31.getId(), network1.getId());

        this.site3 = TenantUtil.runAsUserTenant(new TenantRunAsWork<TestSite>()
        {
            @Override
            public TestSite doWork() throws Exception
            {
                SiteInformation siteInfo = new SiteInformation(site3_name, site3_title, site3_title, SiteVisibility.PRIVATE);
                TestSite site = network1.createSite(siteInfo);
                site.inviteToSite(person32.getId(), site3_role);
                return site;
            }
        }, person31.getId(), network1.getId());
    }

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
        TestSite personRemoveSite = sites.get(sites.size() - 1);
        sites.remove(sites.size() - 1);

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
            //Tests removing a person from the site
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
        ListResponse<MemberOfSite> resp = TenantUtil.runAsUserTenant(new TenantRunAsWork<ListResponse<MemberOfSite>>()
        {
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
     * Tests the capability to sort and paginate the site memberships associated
     * to a user order = Title ASC skip = 1, count = 2
     *
     * @throws Exception
     */
    public void testSortingAndPagingByTitleAsc() throws Exception
    {
        // paging
        int skipCount = 1;
        int maxItems = 2;
        int totalResults = 3;
        Paging paging = getPaging(skipCount, maxItems, totalResults, null);

        // get memberships
        ListResponse<MemberOfSite> resp = getSiteMembershipsForPerson32(paging, "SiteTitle", true);

        // check results
        List<MemberOfSite> expectedList = new LinkedList<>();
        expectedList.add(new MemberOfSite(site3, site3_role));
        expectedList.add(new MemberOfSite(site1, site1_role));

        checkList(expectedList, paging.getExpectedPaging(), resp);

    }

    /**
     * Tests the capability to sort and paginate the site memberships associated
     * to a user order = Title DESC skip = 1, count = 2
     *
     * @throws Exception
     */
    public void testSortingAndPagingByTitleDesc() throws Exception
    {
        // paging
        int skipCount = 1;
        int maxItems = 2;
        int totalResults = 3;
        Paging paging = getPaging(skipCount, maxItems, totalResults, null);

        // get memberships
        ListResponse<MemberOfSite> resp = getSiteMembershipsForPerson32(paging, "SiteTitle", false);

        // check results
        List<MemberOfSite> expectedList = new LinkedList<>();
        expectedList.add(new MemberOfSite(site3, site3_role));
        expectedList.add(new MemberOfSite(site2, site2_role));

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability to sort and paginate the site memberships associated
     * to a user order = Role ASC skip = 1, count = 2
     *
     * @throws Exception
     */
    public void testSortingAndPagingByRoleAsc() throws Exception
    {
        // paging
        int skipCount = 1;
        int maxItems = 2;
        int totalResults = 3;
        Paging paging = getPaging(skipCount, maxItems, totalResults, null);

        // get memberships
        ListResponse<MemberOfSite> resp = getSiteMembershipsForPerson32(paging, "Role", true);

        // check results
        List<MemberOfSite> expectedList = new LinkedList<>();
        expectedList.add(new MemberOfSite(site1, site1_role));
        expectedList.add(new MemberOfSite(site2, site2_role));

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability to sort and paginate the site memberships associated
     * to a user order = Role DESC skip = 1, count = 2
     *
     * @throws Exception
     */
    public void testSortingAndPagingByRoleDesc() throws Exception
    {
        // paging
        int skipCount = 1;
        int maxItems = 2;
        int totalResults = 3;
        Paging paging = getPaging(skipCount, maxItems, totalResults, null);

        // get memberships
        ListResponse<MemberOfSite> resp = getSiteMembershipsForPerson32(paging, "Role", false);

        // check results
        List<MemberOfSite> expectedList = new LinkedList<>();
        expectedList.add(new MemberOfSite(site1, site1_role));
        expectedList.add(new MemberOfSite(site3, site3_role));

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability to sort and paginate the site memberships associated
     * to a user order = Site Name ASC skip = 1, count = 2
     *
     * @throws Exception
     */
    public void testSortingAndPagingBySiteNameAsc() throws Exception
    {
        // paging
        int skipCount = 1;
        int maxItems = 2;
        int totalResults = 3;
        Paging paging = getPaging(skipCount, maxItems, totalResults, null);

        // get memberships
        ListResponse<MemberOfSite> resp = getSiteMembershipsForPerson32(paging, "SiteShortName", true);

        // check results
        List<MemberOfSite> expectedList = new LinkedList<>();
        expectedList.add(new MemberOfSite(site2, site2_role));
        expectedList.add(new MemberOfSite(site3, site3_role));

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability to sort and paginate the site memberships associated
     * to a user order = Site Name DESC skip = 1, count = 2
     *
     * @throws Exception
     */
    public void testSortingAndPagingBySiteNameDesc() throws Exception
    {
        // paging
        int skipCount = 1;
        int maxItems = 2;
        int totalResults = 3;
        Paging paging = getPaging(skipCount, maxItems, totalResults, null);

        // get memberships
        ListResponse<MemberOfSite> resp = getSiteMembershipsForPerson32(paging, "SiteShortName", false);

        // check results
        List<MemberOfSite> expectedList = new LinkedList<>();
        expectedList.add(new MemberOfSite(site2, site2_role));
        expectedList.add(new MemberOfSite(site1, site1_role));

        checkList(expectedList, paging.getExpectedPaging(), resp);
    }

    /**
     * Tests the capability to sort and paginate the site memberships associated
     * default sorting, all results
     *
     * @throws Exception
     */
    public void testSortingAndPagingDefault() throws Exception
    {
        // paging
        int totalResults = 3;
        Paging paging = getPaging(null, null, totalResults, null);

        // get memberships
        ListResponse<MemberOfSite> resp = getSiteMembershipsForPerson32(null, null, false);

        // check results
        List<MemberOfSite> expectedList = new LinkedList<>();
        expectedList.add(new MemberOfSite(site2, site2_role));
        expectedList.add(new MemberOfSite(site3, site3_role));
        expectedList.add(new MemberOfSite(site1, site1_role));

        checkList(expectedList, paging.getExpectedPaging(), resp);

    }

    @Test
    public void testSortingAndPaging() throws Exception
    {
        initializeSites();

        testSortingAndPagingByTitleAsc();
        testSortingAndPagingByTitleDesc();
        testSortingAndPagingByRoleAsc();
        testSortingAndPagingByRoleDesc();
        testSortingAndPagingBySiteNameAsc();
        testSortingAndPagingBySiteNameDesc();
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

        TestSite site4 = TenantUtil.runAsUserTenant(new TenantRunAsWork<TestSite>()
        {
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
        Paging paging = getPaging(null, null, totalResults, null);

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
}
