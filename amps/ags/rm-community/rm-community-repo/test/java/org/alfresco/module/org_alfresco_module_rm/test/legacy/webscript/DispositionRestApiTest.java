/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript;

import java.text.MessageFormat;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.GUID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * This class tests the Rest API for disposition related operations
 *
 * @author Gavin Cornwell
 */
public class DispositionRestApiTest extends BaseRMWebScriptTestCase implements RecordsManagementModel
{
    protected static StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
    protected static final String GET_SCHEDULE_URL_FORMAT = "/api/node/{0}/dispositionschedule";
    protected static final String GET_LIFECYCLE_URL_FORMAT = "/api/node/{0}/nextdispositionaction";
    protected static final String POST_ACTIONDEF_URL_FORMAT = "/api/node/{0}/dispositionschedule/dispositionactiondefinitions";
    protected static final String DELETE_ACTIONDEF_URL_FORMAT = "/api/node/{0}/dispositionschedule/dispositionactiondefinitions/{1}";
    protected static final String PUT_ACTIONDEF_URL_FORMAT = "/api/node/{0}/dispositionschedule/dispositionactiondefinitions/{1}";
    protected static final String GET_LIST_URL = "/api/rma/admin/listofvalues";
    protected static final String SERVICE_URL_PREFIX = "/alfresco/service";
    protected static final String APPLICATION_JSON = "application/json";


    public void testGetDispositionSchedule() throws Exception
    {
        // Test 404 status for non existent node
        int expectedStatus = 404;
        String nonExistentNode = "workspace/SpacesStore/09ca1e02-1c87-4a53-97e7-xxxxxxxxxxxx";
        String nonExistentUrl = MessageFormat.format(GET_SCHEDULE_URL_FORMAT, nonExistentNode);
        Response rsp = sendRequest(new GetRequest(nonExistentUrl), expectedStatus);

        // Test 404 status for node that doesn't have dispostion schedule i.e. a record series
        String seriesNodeUrl = recordSeries.toString().replace("://", "/");
        String wrongNodeUrl = MessageFormat.format(GET_SCHEDULE_URL_FORMAT, seriesNodeUrl);
        rsp = sendRequest(new GetRequest(wrongNodeUrl), expectedStatus);

        // Test data structure returned from "AIS Audit Records"
        expectedStatus = 200;

        String categoryNodeUrl = recordCategory.toString().replace("://", "/");
        String requestUrl = MessageFormat.format(GET_SCHEDULE_URL_FORMAT, categoryNodeUrl);
        rsp = sendRequest(new GetRequest(requestUrl), expectedStatus);
        assertEquals("application/json;charset=UTF-8", rsp.getContentType());

        // get response as JSON
        JSONObject jsonParsedObject = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        assertNotNull(jsonParsedObject);

        // check JSON data
        JSONObject dataObj = jsonParsedObject.getJSONObject("data");
        assertNotNull(dataObj);
        JSONObject rootDataObject = (JSONObject)dataObj;
        assertEquals(10, rootDataObject.length());

        // check individual data items
        String serviceUrl = SERVICE_URL_PREFIX + requestUrl;
        String url = rootDataObject.getString("url");
        assertEquals(serviceUrl, url);

        String authority = rootDataObject.getString("authority");

        assertEquals(CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY, authority);

        String instructions = rootDataObject.getString("instructions");
        assertEquals(CommonRMTestUtils.DEFAULT_DISPOSITION_INSTRUCTIONS, instructions);

        String actionsUrl = rootDataObject.getString("actionsUrl");
        assertEquals(serviceUrl + "/dispositionactiondefinitions", actionsUrl);

        boolean recordLevel = rootDataObject.getBoolean("recordLevelDisposition");
        assertFalse(recordLevel);

        assertFalse(rootDataObject.getBoolean("canStepsBeRemoved"));

        JSONArray actions = rootDataObject.getJSONArray("actions");
        assertNotNull(actions);
        assertEquals(2, actions.length());
        JSONObject action1 = (JSONObject)actions.get(0);
        assertEquals(9, action1.length());
        assertNotNull(action1.get("id"));
        assertNotNull(action1.get("url"));
        assertEquals(0, action1.getInt("index"));
        assertEquals("cutoff", action1.getString("name"));
        assertTrue(action1.getBoolean("eligibleOnFirstCompleteEvent"));

        JSONObject action2 = (JSONObject)actions.get(1);
        assertEquals(8, action2.length());

        // make sure the disposition schedule node ref is present and valid
        String scheduleNodeRefJSON = rootDataObject.getString("nodeRef");
        NodeRef scheduleNodeRef = new NodeRef(scheduleNodeRefJSON);
        assertTrue(this.nodeService.exists(scheduleNodeRef));

        // create a new recordCategory node in the recordSeries and then get
        // the disposition schedule
        NodeRef newRecordCategory = filePlanService.createRecordCategory(recordSeries, GUID.generate());
        dispositionService.createDispositionSchedule(newRecordCategory, null);

        categoryNodeUrl = newRecordCategory.toString().replace("://", "/");
        requestUrl = MessageFormat.format(GET_SCHEDULE_URL_FORMAT, categoryNodeUrl);
        //System.out.println("GET response: " + rsp.getContentAsString());
        rsp = sendRequest(new GetRequest(requestUrl), expectedStatus);

        // get response as JSON
        jsonParsedObject = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        System.out.println(rsp.getContentAsString());
        assertNotNull(jsonParsedObject);

        // check JSON data
        dataObj = jsonParsedObject.getJSONObject("data");
        assertNotNull(dataObj);
        rootDataObject = (JSONObject)dataObj;
        assertEquals(8, rootDataObject.length());
        actions = rootDataObject.getJSONArray("actions");
        assertNotNull(actions);
        assertEquals(0, actions.length());
    }

    public void testPostDispositionAction() throws Exception
    {
        // create a new recordCategory node in the recordSeries and then get
        // the disposition schedule
        NodeRef newRecordCategory = filePlanService.createRecordCategory(recordSeries, GUID.generate());
        dispositionService.createDispositionSchedule(newRecordCategory, null);

        String categoryNodeUrl = newRecordCategory.toString().replace("://", "/");
        String requestUrl = MessageFormat.format(POST_ACTIONDEF_URL_FORMAT, categoryNodeUrl);

        // Construct the JSON request.
        String name = "destroy";
        String desc = "Destroy this record after 5 years";
        String period = "year|5";
        String periodProperty = "rma:cutOffDate";
        boolean eligibleOnFirstCompleteEvent = true;

        JSONObject jsonPostData = new JSONObject();
        jsonPostData.put("name", name);
        jsonPostData.put("description", desc);
        jsonPostData.put("period", period);
        jsonPostData.put("location", "my location");
        jsonPostData.put("periodProperty", periodProperty);
        jsonPostData.put("eligibleOnFirstCompleteEvent", eligibleOnFirstCompleteEvent);
        JSONArray events = new JSONArray();
        events.put("superseded");
        events.put("no_longer_needed");
        jsonPostData.put("events", events);

        // Submit the JSON request.
        String jsonPostString = jsonPostData.toString();
        Response rsp = sendRequest(new PostRequest(requestUrl, jsonPostString, APPLICATION_JSON), 200);

        // check the returned data is what was expected
        JSONObject jsonResponse = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        JSONObject dataObj = jsonResponse.getJSONObject("data");
        JSONObject rootDataObject = (JSONObject)dataObj;
        assertNotNull(rootDataObject.getString("id"));
        assertNotNull(rootDataObject.getString("url"));
        assertEquals(0, rootDataObject.getInt("index"));
        assertEquals(name, rootDataObject.getString("name"));
        assertEquals("Destroy", rootDataObject.getString("label"));
        assertEquals(desc, rootDataObject.getString("description"));
        assertEquals(period, rootDataObject.getString("period"));
        assertEquals("my location", rootDataObject.getString("location"));
        assertEquals(periodProperty, rootDataObject.getString("periodProperty"));
        assertTrue(rootDataObject.getBoolean("eligibleOnFirstCompleteEvent"));
        events = rootDataObject.getJSONArray("events");
        assertNotNull(events);
        assertEquals(2, events.length());
        assertEquals("superseded", events.get(0));
        assertEquals("no_longer_needed", events.get(1));

        // test the minimum amount of data required to create an action definition
        jsonPostData = new JSONObject();
        jsonPostData.put("name", name);
        jsonPostString = jsonPostData.toString();
        rsp = sendRequest(new PostRequest(requestUrl, jsonPostString, APPLICATION_JSON), 200);

        // check the returned data is what was expected
        jsonResponse = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        dataObj = jsonResponse.getJSONObject("data");
        assertNotNull(rootDataObject.getString("id"));
        assertNotNull(rootDataObject.getString("url"));
        assertEquals(0, rootDataObject.getInt("index"));
        assertEquals(name, dataObj.getString("name"));
        assertEquals("none|0", dataObj.getString("period"));
        assertFalse(dataObj.has("description"));
        assertFalse(dataObj.has("periodProperty"));
        assertFalse(dataObj.has("events"));
        assertTrue(dataObj.getBoolean("eligibleOnFirstCompleteEvent"));

        // negative test to ensure not supplying mandatory data results in an error
        jsonPostData = new JSONObject();
        jsonPostData.put("description", desc);
        jsonPostData.put("period", period);
        jsonPostString = jsonPostData.toString();
        sendRequest(new PostRequest(requestUrl, jsonPostString, APPLICATION_JSON), 400);
    }

    public void testPutDispositionAction() throws Exception
    {
        NodeRef newRecordCategory = filePlanService.createRecordCategory(recordSeries, GUID.generate());
        dispositionService.createDispositionSchedule(newRecordCategory, null);

        // create an action definition to then update
        String categoryNodeUrl = newRecordCategory.toString().replace("://", "/");
        String postRequestUrl = MessageFormat.format(POST_ACTIONDEF_URL_FORMAT, categoryNodeUrl);
        JSONObject jsonPostData = new JSONObject();
        jsonPostData.put("name", "cutoff");
        String jsonPostString = jsonPostData.toString();
        sendRequest(new PostRequest(postRequestUrl, jsonPostString, APPLICATION_JSON), 200);

        // verify the action definition is present and retrieve it's id
        String getRequestUrl = MessageFormat.format(GET_SCHEDULE_URL_FORMAT, categoryNodeUrl);
        Response rsp = sendRequest(new GetRequest(getRequestUrl), 200);
        JSONObject json = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        JSONObject actionDef = json.getJSONObject("data").getJSONArray("actions").getJSONObject(0);
        String actionDefId = actionDef.getString("id");
        assertEquals("cutoff", actionDef.getString("name"));
        assertEquals("none|0", actionDef.getString("period"));
        assertFalse(actionDef.has("description"));
        assertFalse(actionDef.has("events"));

        // define body for PUT request
        String name = "destroy";
        String desc = "Destroy this record after 5 years";
        String period = "year|5";
        String location = "my location";
        String periodProperty = "rma:cutOffDate";
        boolean eligibleOnFirstCompleteEvent = false;

        jsonPostData = new JSONObject();
        jsonPostData.put("name", name);
        jsonPostData.put("description", desc);
        jsonPostData.put("period", period);
        jsonPostData.put("location", location);
        jsonPostData.put("periodProperty", periodProperty);
        jsonPostData.put("eligibleOnFirstCompleteEvent", eligibleOnFirstCompleteEvent);
        JSONArray events = new JSONArray();
        events.put("superseded");
        events.put("no_longer_needed");
        jsonPostData.put("events", events);
        jsonPostString = jsonPostData.toString();

        // try and update a non existent action definition to check for 404
        String putRequestUrl = MessageFormat.format(PUT_ACTIONDEF_URL_FORMAT, categoryNodeUrl, "xyz");
        rsp = sendRequest(new PutRequest(putRequestUrl, jsonPostString, APPLICATION_JSON), 404);

        // update the action definition
        putRequestUrl = MessageFormat.format(PUT_ACTIONDEF_URL_FORMAT, categoryNodeUrl, actionDefId);
        rsp = sendRequest(new PutRequest(putRequestUrl, jsonPostString, APPLICATION_JSON), 200);

        // check the update happened correctly
        json = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        actionDef = json.getJSONObject("data");
        assertEquals(name, actionDef.getString("name"));
        assertEquals("Destroy", actionDef.getString("label"));
        assertEquals(desc, actionDef.getString("description"));
        assertEquals(period, actionDef.getString("period"));
        assertEquals(location, actionDef.getString("location"));
        assertEquals(periodProperty, actionDef.getString("periodProperty"));
        assertFalse(actionDef.getBoolean("eligibleOnFirstCompleteEvent"));
        assertEquals(2, actionDef.getJSONArray("events").length());
        assertEquals("superseded", actionDef.getJSONArray("events").getString(0));
        assertEquals("no_longer_needed", actionDef.getJSONArray("events").getString(1));
    }

    public void testDeleteDispositionAction() throws Exception
    {
        NodeRef newRecordCategory = filePlanService.createRecordCategory(recordSeries, GUID.generate());
        dispositionService.createDispositionSchedule(newRecordCategory, null);

        // create an action definition to then delete
        String categoryNodeUrl = newRecordCategory.toString().replace("://", "/");
        String postRequestUrl = MessageFormat.format(POST_ACTIONDEF_URL_FORMAT, categoryNodeUrl);
        JSONObject jsonPostData = new JSONObject();
        jsonPostData.put("name", "cutoff");
        String jsonPostString = jsonPostData.toString();
        sendRequest(new PostRequest(postRequestUrl, jsonPostString, APPLICATION_JSON), 200);

        // verify the action definition is present and retrieve it's id
        String getRequestUrl = MessageFormat.format(GET_SCHEDULE_URL_FORMAT, categoryNodeUrl);
        Response rsp = sendRequest(new GetRequest(getRequestUrl), 200);
        JSONObject json = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        String actionDefId = json.getJSONObject("data").getJSONArray("actions").getJSONObject(0).getString("id");

        // try and delete a non existent action definition to check for 404
        String deleteRequestUrl = MessageFormat.format(DELETE_ACTIONDEF_URL_FORMAT, categoryNodeUrl, "xyz");
        rsp = sendRequest(new DeleteRequest(deleteRequestUrl), 404);

        // now delete the action defintion created above
        deleteRequestUrl = MessageFormat.format(DELETE_ACTIONDEF_URL_FORMAT, categoryNodeUrl, actionDefId);
        rsp = sendRequest(new DeleteRequest(deleteRequestUrl), 200);

        // verify it got deleted
        getRequestUrl = MessageFormat.format(GET_SCHEDULE_URL_FORMAT, categoryNodeUrl);
        rsp = sendRequest(new GetRequest(getRequestUrl), 200);
        json = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        JSONArray actions = json.getJSONObject("data").getJSONArray("actions");
        assertEquals(0, actions.length());
    }

    public void testGetDispositionLifecycle() throws Exception
    {
        // Test 404 for disposition lifecycle request on incorrect node
        String categoryUrl = recordCategory.toString().replace("://", "/");
        String requestUrl = MessageFormat.format(GET_LIFECYCLE_URL_FORMAT, categoryUrl);
        Response rsp = sendRequest(new GetRequest(requestUrl), 200);

        JSONObject notFound = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        assertEquals(true, notFound.getJSONObject("data").getBoolean("notFound"));

        NodeRef newRecordFolder = recordFolderService.createRecordFolder(recordCategory, "recordFolder");


        // there should now be a disposition lifecycle for the record
        requestUrl = MessageFormat.format(GET_LIFECYCLE_URL_FORMAT, newRecordFolder.toString().replace("://", "/"));
        rsp = sendRequest(new GetRequest(requestUrl), 200);
        System.out.println("GET : " + rsp.getContentAsString());
        assertEquals("application/json;charset=UTF-8", rsp.getContentType());

        // get response as JSON
        JSONObject jsonParsedObject = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        assertNotNull(jsonParsedObject);

        // check mandatory stuff is present
        JSONObject dataObj = jsonParsedObject.getJSONObject("data");
        assertEquals(SERVICE_URL_PREFIX + requestUrl, dataObj.getString("url"));
        assertEquals("cutoff", dataObj.getString("name"));
        assertFalse(dataObj.getBoolean("eventsEligible"));
        assertTrue(dataObj.has("events"));
        JSONArray events = dataObj.getJSONArray("events");
        assertEquals(1, events.length());
        JSONObject event1 = events.getJSONObject(0);
        assertEquals("case_closed", event1.get("name"));
        assertEquals("Case Closed", event1.get("label"));
        assertFalse(event1.getBoolean("complete"));
        assertFalse(event1.getBoolean("automatic"));

        // check stuff expected to be missing is missing
        assertFalse(dataObj.has("asOf"));
        assertFalse(dataObj.has("startedAt"));
        assertFalse(dataObj.has("startedBy"));
        assertFalse(dataObj.has("completedAt"));
        assertFalse(dataObj.has("completedBy"));
        assertFalse(event1.has("completedAt"));
        assertFalse(event1.has("completedBy"));
    }

    public void testGetListOfValues() throws Exception
    {
        // call the list service
        Response rsp = sendRequest(new GetRequest(GET_LIST_URL), 200);
        assertEquals("application/json;charset=UTF-8", rsp.getContentType());

        // get response as JSON
        JSONObject jsonParsedObject = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        assertNotNull(jsonParsedObject);
        JSONObject data = jsonParsedObject.getJSONObject("data");

        // check dispostion actions
        JSONObject actions = data.getJSONObject("dispositionActions");
        assertEquals(SERVICE_URL_PREFIX + GET_LIST_URL + "/dispositionactions", actions.getString("url"));
        JSONArray items = actions.getJSONArray("items");
        assertEquals(actionService.getDispositionActions().size(), items.length());
        assertTrue(items.length() > 0);
        JSONObject item = items.getJSONObject(0);
        assertTrue(item.length() == 2);
        assertTrue(item.has("label"));
        assertTrue(item.has("value"));

        // check events
        JSONObject events = data.getJSONObject("events");
        assertEquals(SERVICE_URL_PREFIX + GET_LIST_URL + "/events", events.getString("url"));
        items = events.getJSONArray("items");
        assertEquals(eventService.getEvents().size(), items.length());
        assertTrue(items.length() > 0);
        item = items.getJSONObject(0);
        assertTrue(item.length() == 3);
        assertTrue(item.has("label"));
        assertTrue(item.has("value"));
        assertTrue(item.has("automatic"));

        // check period types
        JSONObject periodTypes = data.getJSONObject("periodTypes");
        assertEquals(SERVICE_URL_PREFIX + GET_LIST_URL + "/periodtypes", periodTypes.getString("url"));
        items = periodTypes.getJSONArray("items");
        assertEquals(Period.getProviderNames().size()-1, items.length());
        assertTrue(items.length() > 0);
        item = items.getJSONObject(0);
        assertTrue(item.length() == 2);
        assertTrue(item.has("label"));
        assertTrue(item.has("value"));

        // check period properties
        JSONObject periodProperties = data.getJSONObject("periodProperties");
        assertEquals(SERVICE_URL_PREFIX + GET_LIST_URL + "/periodproperties", periodProperties.getString("url"));
        items = periodProperties.getJSONArray("items");
        assertEquals(5, items.length());
        assertTrue(items.length() > 0);
        item = items.getJSONObject(0);
        assertTrue(item.length() == 2);
        assertTrue(item.has("label"));
        assertTrue(item.has("value"));
    }
}
