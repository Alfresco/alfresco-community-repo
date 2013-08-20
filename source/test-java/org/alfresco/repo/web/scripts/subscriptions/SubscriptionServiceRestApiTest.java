/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.subscriptions;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Subscription Service REST API tests
 */
public class SubscriptionServiceRestApiTest extends BaseWebScriptTest
{
    public static final String USER_BOB = "bob";
    public static final String USER_TOM = "tom";
    public static final String USER_LISA = "lisa";

    private static final String URL_FOLLOW = "/api/subscriptions/{userid}/follow";
    private static final String URL_UNFOLLOW = "/api/subscriptions/{userid}/unfollow";
    private static final String URL_FOLLOWERS = "/api/subscriptions/{userid}/followers";
    private static final String URL_FOLLOWERS_COUNT = "/api/subscriptions/{userid}/followers/count";
    private static final String URL_FOLLOWING = "/api/subscriptions/{userid}/following";
    private static final String URL_FOLLOWING_COUNT = "/api/subscriptions/{userid}/following/count";
    private static final String URL_FOLLOWS = "/api/subscriptions/{userid}/follows";
    private static final String URL_PRIVATE = "/api/subscriptions/{userid}/private";

    protected PersonService personService;

    @Override
    public void setUp() throws Exception
    {
        // Get the required services
        personService = (PersonService) getServer().getApplicationContext().getBean("PersonService");

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        createPerson(USER_BOB);
        createPerson(USER_TOM);
        createPerson(USER_LISA);
    }

    @Override
    protected void tearDown() throws Exception
    {
        deletePerson(USER_BOB);
        deletePerson(USER_TOM);
        deletePerson(USER_LISA);
    }

    protected void deletePerson(String userId)
    {
        personService.deletePerson(userId);
    }

    protected NodeRef createPerson(String userId)
    {
        deletePerson(userId);

        PropertyMap properties = new PropertyMap(5);
        properties.put(ContentModel.PROP_USERNAME, userId);
        properties.put(ContentModel.PROP_FIRSTNAME, userId);
        properties.put(ContentModel.PROP_LASTNAME, "Test");
        properties.put(ContentModel.PROP_EMAIL, userId + "@test.demo.alfresco.com");

        return personService.createPerson(properties);
    }

    protected String getUrl(String urlPattern, String user)
    {
        return urlPattern.replaceFirst("\\{userid\\}", user);
    }

    protected void follow(String user1, String user2) throws Exception
    {
        JSONArray jsonUsers = new JSONArray();
        jsonUsers.put(user2);

        String url = getUrl(URL_FOLLOW, user1);
        sendRequest(new PostRequest(url, jsonUsers.toString(), "application/json"), Status.STATUS_NO_CONTENT);
    }

    protected void unfollow(String user1, String user2) throws Exception
    {
        JSONArray jsonUsers = new JSONArray();
        jsonUsers.put(user2);

        String url = getUrl(URL_UNFOLLOW, user1);
        sendRequest(new PostRequest(url, jsonUsers.toString(), "application/json"), Status.STATUS_NO_CONTENT);
    }

    protected boolean follows(String user1, String user2) throws Exception
    {
        JSONArray jsonUsers = new JSONArray();
        jsonUsers.put(user2);

        String url = getUrl(URL_FOLLOWS, user1);
        Response response = sendRequest(new PostRequest(url, jsonUsers.toString(), "application/json"),
                Status.STATUS_OK);

        JSONArray resultArray = new JSONArray(response.getContentAsString());
        assertEquals(1, resultArray.length());

        JSONObject resultObject = resultArray.getJSONObject(0);
        assertTrue(resultObject.has(user2));

        return resultObject.getBoolean(user2);
    }

    protected int getFollowingCount(String user) throws Exception
    {
        String url = getUrl(URL_FOLLOWING_COUNT, user);
        Response response = sendRequest(new GetRequest(url), Status.STATUS_OK);

        JSONObject resultObject = new JSONObject(response.getContentAsString());
        assertTrue(resultObject.has("count"));

        return resultObject.getInt("count");
    }

    protected int getFollowersCount(String user) throws Exception
    {
        String url = getUrl(URL_FOLLOWERS_COUNT, user);
        Response response = sendRequest(new GetRequest(url), Status.STATUS_OK);

        JSONObject resultObject = new JSONObject(response.getContentAsString());
        assertTrue(resultObject.has("count"));

        return resultObject.getInt("count");
    }

    protected List<String> getFollowing(String user) throws Exception
    {
        String url = getUrl(URL_FOLLOWING, user);
        Response response = sendRequest(new GetRequest(url), Status.STATUS_OK);

        JSONObject resultObject = new JSONObject(response.getContentAsString());
        assertTrue(resultObject.has("people"));

        List<String> result = new ArrayList<String>();
        JSONArray people = resultObject.getJSONArray("people");

        for (int i = 0; i < people.length(); i++)
        {
            JSONObject person = people.getJSONObject(i);
            assertTrue(person.has("userName"));
            assertTrue(person.has("firstName"));
            assertTrue(person.has("lastName"));

            result.add(person.getString("userName"));
        }

        return result;
    }

    protected List<String> getFollowers(String user) throws Exception
    {
        String url = getUrl(URL_FOLLOWERS, user);
        Response response = sendRequest(new GetRequest(url), Status.STATUS_OK);

        JSONObject resultObject = new JSONObject(response.getContentAsString());
        assertTrue(resultObject.has("people"));

        List<String> result = new ArrayList<String>();
        JSONArray people = resultObject.getJSONArray("people");

        for (int i = 0; i < people.length(); i++)
        {
            JSONObject person = people.getJSONObject(i);
            assertTrue(person.has("userName"));
            assertTrue(person.has("firstName"));
            assertTrue(person.has("lastName"));

            result.add(person.getString("userName"));
        }

        return result;
    }

    protected boolean isSubscriptionListPrivate(String user) throws Exception
    {
        String url = getUrl(URL_PRIVATE, user);
        Response response = sendRequest(new GetRequest(url), Status.STATUS_OK);

        JSONObject resultObject = new JSONObject(response.getContentAsString());
        assertTrue(resultObject.has("private"));

        return resultObject.getBoolean("private");
    }

    protected void setSubscriptionListPrivate(String user, boolean setPrivate) throws Exception
    {
        JSONObject privateObject = new JSONObject();
        privateObject.put("private", setPrivate);

        String url = getUrl(URL_PRIVATE, user);
        Response response = sendRequest(new PutRequest(url, privateObject.toString(), "application/json"),
                Status.STATUS_OK);

        JSONObject resultObject = new JSONObject(response.getContentAsString());
        assertTrue(resultObject.has("private"));
        assertEquals(setPrivate, resultObject.getBoolean("private"));
    }

    public void testFollow() throws Exception
    {
        String userId1 = USER_BOB;
        String userId2 = USER_TOM;
        String userId3 = USER_LISA;

        // check follows first
        if (follows(userId1, userId2))
        {
            unfollow(userId1, userId2);
        }
        assertFalse(follows(userId1, userId2));

        // count the people user 1 is following
        int count = getFollowingCount(userId1);
        assertTrue(count >= 0);

        // user 1 follows user 2 -- twice (the second follow request should be
        // ignored)
        follow(userId1, userId2);
        follow(userId1, userId2);
        assertEquals(count + 1, getFollowingCount(userId1));
        assertTrue(follows(userId1, userId2));

        // user 1 follows user 3
        follow(userId1, userId3);
        assertEquals(count + 2, getFollowingCount(userId1));
        assertTrue(follows(userId1, userId3));

        // get following list of user 1
        List<String> following = getFollowing(userId1);
        assertNotNull(following);
        assertTrue(following.contains(userId2));
        assertTrue(following.contains(userId3));

        // count followers of user 2
        int followerCount = getFollowersCount(userId2);
        assertTrue(followerCount > 0);

        // get followers of user 2
        List<String> followers = getFollowers(userId2);
        assertNotNull(followers);
        assertTrue(followers.contains(userId1));

        // unfollow
        unfollow(userId1, userId2);
        assertEquals(count + 1, getFollowingCount(userId1));
        assertFalse(follows(userId1, userId2));
        assertTrue(follows(userId1, userId3));

        unfollow(userId1, userId3);
        assertEquals(count, getFollowingCount(userId1));
        assertFalse(follows(userId1, userId3));
    }

    public void testPrivateList() throws Exception
    {
        final String userId1 = USER_BOB;

        assertFalse(isSubscriptionListPrivate(userId1));

        setSubscriptionListPrivate(userId1, false);
        assertFalse(isSubscriptionListPrivate(userId1));

        setSubscriptionListPrivate(userId1, true);
        assertTrue(isSubscriptionListPrivate(userId1));

        setSubscriptionListPrivate(userId1, false);
        assertFalse(isSubscriptionListPrivate(userId1));

        setSubscriptionListPrivate(userId1, true);
        assertTrue(isSubscriptionListPrivate(userId1));
    }
}
