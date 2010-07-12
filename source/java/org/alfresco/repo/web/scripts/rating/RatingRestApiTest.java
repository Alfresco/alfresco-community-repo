/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.rating;

import java.text.MessageFormat;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

public class RatingRestApiTest extends BaseWebScriptTest
{
    private final static String GET_RATINGS_URL_FORMAT = "/api/node/{0}/ratings";
    private final static String GET_RATING_DEFS_URL = "/api/rating/schemedefinitions";

    private static final String APPLICATION_JSON = "application/json";
    
    private NodeRef testNode;
    
    protected NodeService nodeService;
    protected Repository repositoryHelper;
    protected RetryingTransactionHelper transactionHelper;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        nodeService = (NodeService) getServer().getApplicationContext().getBean("NodeService");
        repositoryHelper = (Repository) getServer().getApplicationContext().getBean("repositoryHelper");
        transactionHelper = (RetryingTransactionHelper)getServer().getApplicationContext().getBean("retryingTransactionHelper");  
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        
        // Create a test node which we will rate. It doesn't matter that it has no content.
        testNode = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        ChildAssociationRef result = nodeService.createNode(repositoryHelper.getCompanyHome(),
                                                                ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS,
                                                                ContentModel.TYPE_CONTENT, null);
                        return result.getChildRef();
                    }          
                });        
    }
    
    @Override
    public void tearDown() throws Exception
    {
        super.tearDown();
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        if (testNode != null && nodeService.exists(testNode))
                        {
                            nodeService.deleteNode(testNode);
                        }
                        return null;
                    }          
                });        
    }

    //TODO test POST out-of-range.
    //TODO test get my-ratings on node with mine & others' ratings.
    //TODO test GET average
    //TODO test POST and PUT (same)
    
    public void testGetRatingSchemeDefinitions() throws Exception
    {
        // May as well return all of them in one call.
        final int expectedStatus = 200;
        Response rsp = sendRequest(new GetRequest(GET_RATING_DEFS_URL), expectedStatus);

        JSONObject jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        
        
        System.err.println(jsonRsp);
        
        JSONObject dataObj = (JSONObject)jsonRsp.get("data");
        assertNotNull("JSON 'data' object was null", dataObj);
        
        JSONArray ratingSchemesArray = (JSONArray)dataObj.get("ratingSchemes");
        assertNotNull("JSON 'ratingSchemesArray' object was null", ratingSchemesArray);
        assertEquals(2, ratingSchemesArray.length());
        
        JSONObject scheme1 = ratingSchemesArray.getJSONObject(0);
        JSONObject scheme2 = ratingSchemesArray.getJSONObject(1);
        
        assertEquals("likesRatingScheme", scheme1.getString("name"));
        assertEquals(1, scheme1.getInt("minRating"));
        assertEquals(1, scheme1.getInt("maxRating"));
        assertEquals("fiveStarRatingScheme", scheme2.getString("name"));
        assertEquals(1, scheme2.getInt("minRating"));
        assertEquals(5, scheme2.getInt("maxRating"));
    }
    
    public void testGetRatingsFromUnratedNodeRef() throws Exception
    {
        // GET ratings
        String nodeUrl = testNode.toString().replace("://", "/");
        String ratingUrl = MessageFormat.format(GET_RATINGS_URL_FORMAT, nodeUrl);

        final int expectedStatus = 200;
        Response rsp = sendRequest(new GetRequest(ratingUrl), expectedStatus);

        JSONObject jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        
        System.err.println(jsonRsp);

        JSONObject dataObj = (JSONObject)jsonRsp.get("data");
        assertNotNull("JSON 'data' object was null", dataObj);
        
        assertEquals(testNode.toString(), dataObj.getString("nodeRef"));
        final JSONArray ratingsArray = dataObj.getJSONArray("ratings");
        assertEquals(0, ratingsArray.length());
    }
}
