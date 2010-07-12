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
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * This class tests the ReST API of the {@link RatingService}.
 * 
 * @author Neil McErlean
 * @since 3.4
 */
public class RatingRestApiTest extends BaseWebScriptTest
{
    // Miscellaneous constants used throughout this test class.
    private static final String FIVE_STAR_RATING_SCHEME = "fiveStarRatingScheme";
    private static final String LIKES_RATING_SCHEME = "likesRatingScheme";

    private static final String USER_ONE = "UserOne";
    private static final String USER_TWO = "UserTwo";

    private static final String RATING_SCHEMES = "ratingSchemes";
    private static final String NAME = "name";
    private static final String MIN_RATING = "minRating";
    private static final String MAX_RATING = "maxRating";
    private static final String RATINGS = "ratings";
    private static final String NODE_REF = "nodeRef";
    private static final String DATA = "data";
    private static final String RATINGS_TOTAL = "ratingsTotal";
    private static final String RATINGS_COUNT = "ratingsCount";
    private static final String AVERAGE_RATING = "averageRating";
    private static final String NODE_STATISTICS = "nodeStatistics";

    private final static String NODE_RATINGS_URL_FORMAT = "/api/node/{0}/ratings";
    private final static String GET_RATING_DEFS_URL = "/api/rating/schemedefinitions";

    private static final String APPLICATION_JSON = "application/json";
    
    private NodeRef testNode;
    
    private MutableAuthenticationService authenticationService;
    private NodeService nodeService;
    private PersonService personService;
    private Repository repositoryHelper;
    private RetryingTransactionHelper transactionHelper;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        authenticationService = (MutableAuthenticationService) getServer().getApplicationContext().getBean("AuthenticationService");
        nodeService = (NodeService) getServer().getApplicationContext().getBean("NodeService");
        personService = (PersonService) getServer().getApplicationContext().getBean("PersonService");
        repositoryHelper = (Repository) getServer().getApplicationContext().getBean("repositoryHelper");
        transactionHelper = (RetryingTransactionHelper)getServer().getApplicationContext().getBean("retryingTransactionHelper");  
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        
        // Create some users to rate each other's content
        // and a test node which we will rate.
        // It doesn't matter that it has no content.
        testNode = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        createUser(USER_ONE);
                        createUser(USER_TWO);

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

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        if (testNode != null && nodeService.exists(testNode))
                        {
                            nodeService.deleteNode(testNode);
                            deleteUser(USER_ONE);
                            deleteUser(USER_TWO);
                        }
                        return null;
                    }          
                });        
    }

    public void testGetRatingSchemeDefinitions() throws Exception
    {
        final int expectedStatus = 200;
        Response rsp = sendRequest(new GetRequest(GET_RATING_DEFS_URL), expectedStatus);

        JSONObject jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        
        JSONObject dataObj = (JSONObject)jsonRsp.get(DATA);
        assertNotNull("JSON 'data' object was null", dataObj);
        
        JSONArray ratingSchemesArray = (JSONArray)dataObj.get(RATING_SCHEMES);
        assertNotNull("JSON 'ratingSchemesArray' object was null", ratingSchemesArray);
        assertEquals(2, ratingSchemesArray.length());
        
        JSONObject scheme1 = ratingSchemesArray.getJSONObject(0);
        JSONObject scheme2 = ratingSchemesArray.getJSONObject(1);
        
        assertEquals(LIKES_RATING_SCHEME, scheme1.getString(NAME));
        assertEquals(1, scheme1.getInt(MIN_RATING));
        assertEquals(1, scheme1.getInt(MAX_RATING));
        assertEquals(FIVE_STAR_RATING_SCHEME, scheme2.getString(NAME));
        assertEquals(1, scheme2.getInt(MIN_RATING));
        assertEquals(5, scheme2.getInt(MAX_RATING));
    }
    
    public void testGetRatingsFromUnratedNodeRef() throws Exception
    {
        // GET ratings
        String ratingUrl = getRatingUrl(testNode);

        final int expectedStatus = 200;
        Response rsp = sendRequest(new GetRequest(ratingUrl), expectedStatus);

        JSONObject jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        
        JSONObject dataObj = (JSONObject)jsonRsp.get(DATA);
        assertNotNull("JSON 'data' object was null", dataObj);
        
        assertEquals(testNode.toString(), dataObj.getString(NODE_REF));
        final JSONArray ratingsArray = dataObj.getJSONArray(RATINGS);
        assertEquals(0, ratingsArray.length());

        // Unrated content
        JSONObject statsObject = dataObj.getJSONObject(NODE_STATISTICS);
        JSONObject likesStats = statsObject.getJSONObject(LIKES_RATING_SCHEME);
        assertEquals("Average rating was wrong.", -1.0, likesStats.getDouble(AVERAGE_RATING));
        assertEquals("Ratings count rating was wrong.", 0, likesStats.getInt(RATINGS_COUNT));
        assertEquals("Ratings total was wrong.", 0, likesStats.getInt(RATINGS_TOTAL));
    }

    /**
     * This test method applies ratings from multiple users in a single rating
     * scheme to a single test node. It then retrieves those ratings to ensure they
     * were persisted correctly.
     */
    public void testApplyRatingsAsMultipleUsersAndRetrieve() throws Exception
    {
        // POST a new rating to the testNode - as User One.
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);

        final String testNodeRatingUrl = getRatingUrl(testNode);
        
        final int userOneRatingValue = 5;
        String jsonString = new JSONStringer().object()
            .key("rating").value(userOneRatingValue)
            .key("ratingScheme").value(FIVE_STAR_RATING_SCHEME)
        .endObject()
        .toString();
        
        Response rsp = sendRequest(new PostRequest(testNodeRatingUrl,
                                 jsonString, APPLICATION_JSON), 200);
        
        String rspContent = rsp.getContentAsString();
        
        // Get the returned URL and validate
        JSONObject jsonRsp = new JSONObject(new JSONTokener(rspContent));
        
        JSONObject dataObj = (JSONObject)jsonRsp.get(DATA);
        assertNotNull("JSON 'data' object was null", dataObj);
        String returnedUrl =  dataObj.getString("ratedNodeUrl");
        assertEquals(testNodeRatingUrl, returnedUrl);
        assertEquals(FIVE_STAR_RATING_SCHEME, dataObj.getString("ratingScheme"));
        assertEquals(userOneRatingValue, dataObj.getInt("rating"));
        
        // Now GET the ratings via that returned URL
        rsp = sendRequest(new GetRequest(testNodeRatingUrl), 200);

        jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        
        dataObj = (JSONObject)jsonRsp.get(DATA);
        assertNotNull("JSON 'data' object was null", dataObj);
        
        // There should only be the one rating in there.
        final JSONArray ratingsArray = dataObj.getJSONArray(RATINGS);
        assertEquals(1, ratingsArray.length());
        JSONObject recoveredRating = (JSONObject)ratingsArray.get(0);
        assertEquals(userOneRatingValue, recoveredRating.getInt("rating"));
        assertEquals(FIVE_STAR_RATING_SCHEME, recoveredRating.getString("ratingScheme"));

        // As well as the average, total ratings.
        JSONObject statsObject = dataObj.getJSONObject(NODE_STATISTICS);
        JSONObject fiveStarStats = statsObject.getJSONObject(FIVE_STAR_RATING_SCHEME);
        assertEquals("Average rating was wrong.", (double)userOneRatingValue, fiveStarStats.getDouble(AVERAGE_RATING));
        assertEquals("Ratings count rating was wrong.", 1, fiveStarStats.getInt(RATINGS_COUNT));
        assertEquals("Ratings total was wrong.", userOneRatingValue, fiveStarStats.getInt(RATINGS_TOTAL));
        

        // Now POST a second new rating to the testNode - as User Two.
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);

        final int userTwoRatingValue = 3;
        jsonString = new JSONStringer().object()
            .key("rating").value(userTwoRatingValue)
            .key("ratingScheme").value(FIVE_STAR_RATING_SCHEME)
        .endObject()
        .toString();
        
        rsp = sendRequest(new PostRequest(testNodeRatingUrl,
                                 jsonString, APPLICATION_JSON), 200);
        rspContent = rsp.getContentAsString();
        
        // Get the returned URL and validate
        jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        
        dataObj = (JSONObject)jsonRsp.get(DATA);
        assertNotNull("JSON 'data' object was null", dataObj);
        returnedUrl =  dataObj.getString("ratedNodeUrl");

        // Again GET the ratings via that returned URL
        rsp = sendRequest(new GetRequest(returnedUrl), 200);

        jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        
        dataObj = (JSONObject)jsonRsp.get(DATA);
        assertNotNull("JSON 'data' object was null", dataObj);

        // There should still only be the one rating in the results - because we're running
        // as UserTwo and should not see UserOne's rating.
        final JSONArray userTwoRatingsArray = dataObj.getJSONArray(RATINGS);
        assertEquals(1, userTwoRatingsArray.length());
        JSONObject secondRating = (JSONObject)userTwoRatingsArray.get(0);
        assertEquals(userTwoRatingValue, secondRating.getInt("rating"));
        assertEquals(FIVE_STAR_RATING_SCHEME, secondRating.getString("ratingScheme"));

        // Now the average should have changed.
        statsObject = dataObj.getJSONObject(NODE_STATISTICS);
        fiveStarStats = statsObject.getJSONObject(FIVE_STAR_RATING_SCHEME);
        assertEquals("Average rating was wrong.", (userOneRatingValue + userTwoRatingValue) / 2.0,
                                                  fiveStarStats.getDouble(AVERAGE_RATING));
        assertEquals("Ratings count rating was wrong.", 2, fiveStarStats.getInt(RATINGS_COUNT));
        assertEquals("Ratings total was wrong.", userOneRatingValue + userTwoRatingValue,
                                                 fiveStarStats.getInt(RATINGS_TOTAL));
    }
    
    /**
     * This method gives the 'ratings' URL for the specified NodeRef.
     */
    private String getRatingUrl(NodeRef nodeRef)
    {
        String nodeUrl = nodeRef.toString().replace("://", "/");
        String ratingUrl = MessageFormat.format(NODE_RATINGS_URL_FORMAT, nodeUrl);
        return ratingUrl;
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
