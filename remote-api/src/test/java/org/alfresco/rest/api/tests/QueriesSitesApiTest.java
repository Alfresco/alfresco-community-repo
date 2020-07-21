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

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.Queries;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.testing.category.LuceneTests;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
* V1 REST API tests for pre-defined 'live' search Queries on Sites
 * 
 * <ul>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/queries/sites} </li>
 * </ul>
 *
 * @author janv
 */
public class QueriesSitesApiTest extends AbstractSingleNetworkSiteTest
{
    private static final String URL_QUERIES_LSS = "queries/sites";
    
    private SiteService siteService;

    @Before
    @Override
    public void setup() throws Exception
    {
        super.setup();
        siteService = (SiteService)applicationContext.getBean("SiteService");
    }

    // Note expectedIds defaults to ids
    private void checkApiCall(String term, String orderBy, Paging paging, int expectedStatus, String[] expectedIds, String... ids) throws Exception
    {
        Map<String, String> params = new HashMap<>(1);
        params.put(Queries.PARAM_TERM, "\"" + term + "\"");
        if (orderBy != null)
        {
            params.put(Queries.PARAM_ORDERBY, orderBy);
        }

        dummySearchServiceQueryNodeRefs.clear();
        for (String id: ids)
        {
            NodeRef nodeRef = getNodeRef(id);
            dummySearchServiceQueryNodeRefs.add(nodeRef);
        }
        expectedIds = expectedIds != null ? expectedIds : ids;

        HttpResponse response = getAll(URL_QUERIES_LSS, paging, params, 200);

        if (expectedStatus == 200)
        {
            String termWithEscapedAsterisks = term.replaceAll("\\*", "\\\\*");
            String expectedQuery = "TYPE:\"{http://www.alfresco.org/model/site/1.0}site\" AND (\"*"+ termWithEscapedAsterisks +"*\")";
            ArgumentCaptor<SearchParameters> searchParametersCaptor = ArgumentCaptor.forClass(SearchParameters.class);
            verify(mockSearchService, times(++callCountToMockSearchService)).query(searchParametersCaptor.capture());
            SearchParameters parameters = searchParametersCaptor.getValue();
            assertEquals("Query", expectedQuery, parameters.getQuery());

            List<Site> sites = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Site.class);
            assertEquals(expectedIds.length, sites.size());

            if (orderBy != null)
            {
                for (int i=0; i<expectedIds.length; i++)
                {
                    String id = expectedIds[i];
                    String actualId = sites.get(i).getId();
                    assertEquals("Order "+i+":", id, actualId);
                }
            }
        }
    }

   /**
     * Tests basic api for nodes live search sites - metadata (id, title, description)
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/queries/sites}
     */
    @Test
    public void testLiveSearchSites() throws Exception
    {
        setRequestContext(user1);
        
        int sCount = 5;
        assertTrue(sCount > 4); // as relied on by test below
        
        List<String> siteIds = new ArrayList<>(sCount);

        try
        {
            // As user 1 ...

            Paging paging = getPaging(0, 100);

            // We can no longer check the assumption that there is a clean repo (ie. no sites start with ab)
            // in the same way as before which used search - Generally ok not to check.

            String siteI = "a";
            String siteT = "siteT";
            String siteD = "siteD";

            int charValue = siteI.charAt(0);
            
            // create some some sites with site id: ab00001, abc00002, abcd00003, abcde00004, abcdef00005 (and some specific titles and descriptions)
            for (int i = 1; i <= sCount; i++)
            {
                String num = String.format("%05d", i);

                charValue = charValue+1;
                siteI = siteI + String.valueOf((char)charValue);

                String siteId = siteI + num + RUNID;
                String siteTitle = siteT + num + siteT;
                String siteDescrip = siteD + num + siteD;

                // create site
                String createdSiteId = createSite(siteId, siteTitle, siteDescrip, SiteVisibility.PRIVATE, 201).getId();
                siteIds.add(createdSiteId);
            }
            String ab = siteIds.get(0);
            String abc = siteIds.get(1);
            String abcd = siteIds.get(2);
            String abcde = siteIds.get(3);
            String abcdef = siteIds.get(4);

            // basic search tests
            {
                // Search hits based on site id
                checkApiCall("ab", null, paging, 200, null, ab, abc, abcd, abcde, abcdef);
                checkApiCall("abc", null, paging, 200, null, abc, abcd, abcde, abcdef);
                checkApiCall("abcd", null, paging, 200, null, abcd, abcde, abcdef);
                checkApiCall("abcde", null, paging, 200, null, abcde, abcdef);

                // Single search hit based on site id
                checkApiCall("abcd00003", null, paging, 200, null, abcd);

                // Search hits based on site title
                checkApiCall(siteT, null, paging, 200, null, ab, abc, abcd, abcde, abcdef);

                // Single search hit based on site title
                checkApiCall(siteT + String.format("%05d", 2) + siteT, null, paging, 200, null, abc);

                // Search hits based on site description
                checkApiCall(siteD + "*", null, paging, 200, null, ab, abc, abcd, abcde, abcdef);

                // Single search hit based on site description
                checkApiCall(siteD + String.format("%05d", 3) + siteD, null, paging, 200, null, abcd);
            }

            // -ve tests
            {
                // -ve test - no params (ie. no term)
                getAll(URL_QUERIES_LSS, paging, null, 400);

                // -ve test - term too short
                Map<String, String> params = new HashMap<>(1);
                params.put(Queries.PARAM_TERM, "a");
                getAll(URL_QUERIES_LSS, paging, params, 400);

                // -ve test - term is still too short
                params = new HashMap<>(1);
                params.put(Queries.PARAM_TERM, "  \"a *\"  ");
                getAll(URL_QUERIES_LSS, paging, params, 400);

                // -ve test - unauthenticated - belts-and-braces ;-)
                setRequestContext(null);
                getAll(URL_QUERIES_LSS, paging, params, 401);
            }
        }
        finally
        {
            // some cleanup
            setRequestContext(user1);
            
            for (String siteId : siteIds)
            {
                deleteSite(siteId, true, 204);
            }
        }
    }

    private NodeRef getNodeRef(String createdSiteId)
    {
        AuthenticationUtil.setFullyAuthenticatedUser(user1);
        // The following call to siteService.getSite(createdSiteId).getNodeRef() returns a NodeRef like:
        //    workspace://SpacesStore/9db76769-96de-4de4-bdb4-a127130af362
        // We call tenantService.getName(nodeRef) to get a fully qualified NodeRef as Solr returns this.
        // They look like:
        //    workspace://@org.alfresco.rest.api.tests.queriespeopleapitest@SpacesStore/9db76769-96de-4de4-bdb4-a127130af362
        NodeRef nodeRef = siteService.getSite(createdSiteId).getNodeRef();
        nodeRef = tenantService.getName(nodeRef);
        return nodeRef;
    }

    @Test
    public void testLiveSearchSites_SortPage() throws Exception
    {
        setRequestContext(user1);
        
        List<String> siteIds = new ArrayList<>(5);

        try
        {
            // As user 1 ...

            Paging paging = getPaging(0, 100);
            
            // create site
            String s1 = createSite("siABCDEF"+RUNID, "ABCDEF DEF", "sdABCDEF", SiteVisibility.PRIVATE, 201).getId();
            String s2 = createSite("siABCD"+RUNID, "ABCD DEF", "sdABCD", SiteVisibility.PRIVATE, 201).getId();
            String s3 = createSite("siABCDE"+RUNID, "ABCDE DEF", "sdABCDE", SiteVisibility.PRIVATE, 201).getId();
            String s4 = createSite("siAB"+RUNID, "AB DEF", "sdAB", SiteVisibility.PRIVATE, 201).getId();
            String s5 = createSite("siABC"+RUNID, "ABC DEF", "sdABC", SiteVisibility.PRIVATE, 201).getId();
            
            // test sort order
            {
                // default sort order - title asc
                checkApiCall("siAB", null, paging, 200, null, s4, s5, s2, s3, s1);

                // sort order - title asc
                checkApiCall("siAB", "title asc", paging, 200, null, s4, s5, s2, s3, s1);

                // sort order - title desc
                checkApiCall("siAB", "title desc", paging, 200, null, s1, s3, s2, s5, s4);
            }

            // basic paging tests
            {
                // sort order - title desc
                checkApiCall("siAB", "title desc", getPaging(0, 2), 200, new String[] {s1, s3}, s1, s3, s2, s5, s4);
                checkApiCall("siAB", "title desc", getPaging(2, 2), 200, new String[] {s2, s5}, s1, s3, s2, s5, s4);
                checkApiCall("siAB", "title desc", getPaging(4, 2), 200, new String[] {s4}, s1, s3, s2, s5, s4);
            }
            
            // -ve tests
            {
                // -ve test - invalid sort field
                Map<String, String> params = new HashMap<>(2);
                params.put(Queries.PARAM_TERM, "siAB");
                params.put(Queries.PARAM_ORDERBY, "invalid asc");
                getAll(URL_QUERIES_LSS, paging, params, 400);

                // -ve test - unauthenticated - belts-and-braces ;-)
                setRequestContext(null);
                getAll(URL_QUERIES_LSS, paging, params, 401);
            }
        }
        finally
        {
            // some cleanup
            setRequestContext(user1);
            for (String siteId : siteIds)
            {
                deleteSite(siteId, true, 204);
            }
        }
    }
    
    @Override
    public String getScope()
    {
        return "public";
    }
}
