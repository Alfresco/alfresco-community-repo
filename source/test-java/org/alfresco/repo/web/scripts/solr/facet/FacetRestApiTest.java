/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.solr.facet;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetService;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetServiceImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.collections.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.TestWebScriptServer.*;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * This class tests the ReST API of the {@link SolrFacetService}.
 * 
 * @author Neil Mc Erlean
 * @since 5.0
 */
public class FacetRestApiTest extends BaseWebScriptTest
{
    private static final String SEARCH_ADMIN_USER     = "searchAdmin";
    private static final String NON_SEARCH_ADMIN_USER = "nonSearchAdmin";
    
    private static final String FACETS = "facets";
    
    private final static String GET_FACETS_URL       = "/api/solr/facet-config";
    private final static String PUT_FACET_URL_FORMAT = "/api/solr/facet-config/{0}?relativePos={1}";
    
    private MutableAuthenticationService authenticationService;
    private AuthorityService             authorityService;
    private PersonService                personService;
    private RetryingTransactionHelper    transactionHelper;
    
    @Override protected void setUp() throws Exception
    {
        super.setUp();
        authenticationService = getServer().getApplicationContext().getBean("AuthenticationService", MutableAuthenticationService.class);
        authorityService      = getServer().getApplicationContext().getBean("AuthorityService", AuthorityService.class);
        personService         = getServer().getApplicationContext().getBean("PersonService", PersonService.class);
        transactionHelper     = getServer().getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        
        // Create test users. TODO Create these users @BeforeClass or at a testsuite scope.
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override public Void doWork() throws Exception
            {
                createUser(SEARCH_ADMIN_USER);
                createUser(NON_SEARCH_ADMIN_USER);
                
                if ( !authorityService.getContainingAuthorities(AuthorityType.GROUP,
                                                                SEARCH_ADMIN_USER,
                                                                true)
                                    .contains(SolrFacetServiceImpl.GROUP_ALFRESCO_SEARCH_ADMINISTRATORS_AUTHORITY))
                {
                    authorityService.addAuthority(SolrFacetServiceImpl.GROUP_ALFRESCO_SEARCH_ADMINISTRATORS_AUTHORITY,
                                                  SEARCH_ADMIN_USER);
                }
                return null;
            }
        });
    }
    
    @Override public void tearDown() throws Exception
    {
        super.tearDown();
        
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override public Void doWork() throws Exception
            {
                transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        deleteUser(SEARCH_ADMIN_USER);
                        deleteUser(NON_SEARCH_ADMIN_USER);
                        return null;
                    }
                });
                return null;
            }
        });
    }
    
    public void testNonSearchAdminUserCannotAccessSolrFacets() throws Exception
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override public Void doWork() throws Exception
            {
                sendRequest(new GetRequest(GET_FACETS_URL), 403);
                return null;
            }
        }, NON_SEARCH_ADMIN_USER);
    }
    
    public void testSearchAdminCanGetFacets() throws Exception
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override public Void doWork() throws Exception
            {
                Response rsp = sendRequest(new GetRequest(GET_FACETS_URL), 200);
                
                String contentAsString = rsp.getContentAsString();
                JSONObject jsonRsp = new JSONObject(new JSONTokener(contentAsString));
                
                // FIXME The JSON payload should be contained within a 'data' object.
                JSONArray facetsArray = (JSONArray)jsonRsp.get(FACETS);
                assertNotNull("JSON 'facets' array was null", facetsArray);
                
                // We'll not add any further assertions on the JSON content. If we've
                // got valid JSON at this point, then that's good enough.
                return null;
            }
        }, SEARCH_ADMIN_USER);
    }
    
    public void testSearchAdminReordersFacets() throws Exception
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override public Void doWork() throws Exception
            {
                // get the existing facets.
                Response rsp = sendRequest(new GetRequest(GET_FACETS_URL), 200);
                
                JSONObject jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
                
                final JSONArray facetsArray = (JSONArray)jsonRsp.get(FACETS);
                assertNotNull("JSON 'facets' array was null", facetsArray);
                
                System.out.println("Received " + facetsArray.length() + " facets");
                
                final List<Pair<Integer, String>> idsIndexes = getIdsIndexes(facetsArray);
                
                System.out.println(" IDs, indexes = " + idsIndexes);
                
                // Reorder them such that the last facet is moved left one place.
                assertTrue("There should be more than 1 built-in facet", facetsArray.length() > 1);
                
                final Pair<Integer, String> lastIndexIdPair = idsIndexes.get(idsIndexes.size() - 1);
                final String url = PUT_FACET_URL_FORMAT.replace("{0}", lastIndexIdPair.getSecond())
                                                       .replace("{1}", "-1");
                rsp = sendRequest(new PutRequest(url, "", "application/json"), 200);
                
                
                // Now get the facets back and we should see that one has moved.
                rsp = sendRequest(new GetRequest(GET_FACETS_URL), 200);
                
                jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
                
                JSONArray newfacetsArray = (JSONArray)jsonRsp.get(FACETS);
                assertNotNull("JSON 'facets' array was null", newfacetsArray);
                
                System.out.println("Received " + newfacetsArray.length() + " facets");
                
                final List<Pair<Integer, String>> newIdsIndexes = getIdsIndexes(newfacetsArray);
                
                System.out.println(" IDs, indexes = " + newIdsIndexes);
                
                
                // Note here that the last Facet JSON object *is* moved one place up the list.
                // But its index value does not change, which I think is as it should be.
                assertEquals(CollectionUtils.moveLeft(1, lastIndexIdPair, idsIndexes),
                             newIdsIndexes);
                return null;
            }
        }, SEARCH_ADMIN_USER);
    }
    
    private List<Pair<Integer, String>> getIdsIndexes(JSONArray facetsArray) throws JSONException
    {
        List<Pair<Integer, String>> result = new ArrayList<>();
        
        for (int i = 0; i < facetsArray.length(); i++)
        {
            final JSONObject nextFacet = facetsArray.getJSONObject(i);
            final int nextIndex = nextFacet.getInt("index");
            final String nextId = nextFacet.getString("filterID");
            result.add(new Pair<>(nextIndex, nextId));
        }
        return result;
    }
    
    private void createUser(String userName)
    {
        if (! authenticationService.authenticationExists(userName))
        {
            authenticationService.createAuthentication(userName, "PWD".toCharArray());
        }
        
        if (! personService.personExists(userName))
        {
            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");
            
            personService.createPerson(ppOne);
        }
    }
    
    private void deleteUser(String userName)
    {
        if (personService.personExists(userName))
        {
            personService.deletePerson(userName);
        }
    }
}
