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

import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.Queries;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
* V1 REST API tests for pre-defined 'live' search Queries on Sites
 * 
 * <ul>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/queries/live-search-sites} </li>
 * </ul>
 *
 * @author janv
 */
public class QueriesSitesApiTest extends AbstractSingleNetworkSiteTest
{
    private static final String URL_QUERIES_LSS = "queries/live-search-sites";
    
    /**
     * Tests basic api for nodes live search sites - metadata (id, title, description)
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/queries/live-search-sites}
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

            String term = "abcd";
            
            Map<String, String> params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, term);

            // Try to get sites with search term 'abc123' - assume clean repo (ie. none to start with)
            HttpResponse response = getAll(URL_QUERIES_LSS, paging, params, 200);
            List<Site> sites = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Site.class);
            assertEquals(0, sites.size());

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

                String siteId = siteI + num;
                String siteTitle = siteT + num + siteT;
                String siteDescrip = siteD + num + siteD;

                // create site
                String createdSiteId = createSite(siteId, siteTitle, siteDescrip, SiteVisibility.PRIVATE, 201).getId();
                siteIds.add(createdSiteId);
            }
            
            // Search hits based on site id
            term = "ab";
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, "\""+term+"\"");
            response = getAll(URL_QUERIES_LSS, paging, params, 200);
            sites = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Site.class);
            assertEquals(sCount, sites.size());

            term = "abc";
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, "\""+term+"\"");
            response = getAll(URL_QUERIES_LSS, paging, params, 200);
            sites = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Site.class);
            assertEquals(sCount-1, sites.size());
            
            term = "abcd";
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, "\""+term+"\"");
            response = getAll(URL_QUERIES_LSS, paging, params, 200);
            sites = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Site.class);
            assertEquals(sCount-2, sites.size());

            term = "abcde";
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, "\""+term+"\"");
            response = getAll(URL_QUERIES_LSS, paging, params, 200);
            sites = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Site.class);
            assertEquals(sCount-3, sites.size());

            // Single search hit based on site id
            term = "abcd00003";
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, "\""+term+"\"");
            response = getAll(URL_QUERIES_LSS, paging, params, 200);
            sites = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Site.class);
            assertEquals(1, sites.size());
            assertEquals(term, sites.get(0).getId());

            // Search hits based on site title
            term = siteT;
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, "\""+term+"\"");
            response = getAll(URL_QUERIES_LSS, paging, params, 200);
            sites = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Site.class);
            assertEquals(sCount, sites.size());

            // Single search hit based on site title
            term = siteT+String.format("%05d", 2)+siteT;
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, "\""+term+"\"");
            response = getAll(URL_QUERIES_LSS, paging, params, 200);
            sites = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Site.class);
            assertEquals(1, sites.size());
            assertEquals(term, sites.get(0).getTitle());

            // Search hits based on site description
            term = siteD+"*"; // note: SiteService.findSites does not auto-add "*" when matching description
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, "\""+term+"\"");
            response = getAll(URL_QUERIES_LSS, paging, params, 200);
            sites = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Site.class);
            assertEquals(sCount, sites.size());
            
            // Single search hit based on site description
            term = siteD+String.format("%05d", 3)+siteD;
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, "\""+term+"\"");
            response = getAll(URL_QUERIES_LSS, paging, params, 200);
            sites = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Site.class);
            assertEquals(1, sites.size());
            assertEquals(term, sites.get(0).getDescription());

            // -ve test - no params (ie. no term)
            getAll(URL_QUERIES_LSS, paging, null, 400);
            
            // -ve test - term too short
            params = new HashMap<>(1);
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
