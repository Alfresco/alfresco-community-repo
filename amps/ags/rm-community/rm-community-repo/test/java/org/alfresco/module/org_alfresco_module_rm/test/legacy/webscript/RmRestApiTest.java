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

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementCustomModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipType;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.TestActionParams;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * This class tests the Rest API for RM.
 *
 * @author Neil McErlean
 */
public class RmRestApiTest extends BaseRMWebScriptTestCase implements RecordsManagementModel
{
    protected static final String GET_NODE_AUDITLOG_URL_FORMAT = "/api/node/{0}/rmauditlog";
    protected static final String GET_TRANSFER_URL_FORMAT = "/api/node/{0}/transfers/{1}";
    protected static final String TRANSFER_REPORT_URL_FORMAT = "/api/node/{0}/transfers/{1}/report";
    protected static final String REF_INSTANCES_URL_FORMAT = "/api/node/{0}/customreferences";
    protected static final String RMA_AUDITLOG_URL = "/api/rma/admin/rmauditlog";
    protected static final String RMA_AUDITLOG_STATUS_URL = "/api/rma/admin/rmauditlog/status";
    protected static final String GET_LIST_URL = "/api/rma/admin/listofvalues";
    protected static final String RMA_ACTIONS_URL = "/api/rma/actions/ExecutionQueue";
    protected static final String APPLICATION_JSON = "application/json";
    protected static final String RMA_CUSTOM_PROPS_DEFINITIONS_URL = "/api/rma/admin/custompropertydefinitions";
    protected static final String RMA_CUSTOM_REFS_DEFINITIONS_URL = "/api/rma/admin/customreferencedefinitions";

    private static final String BI_DI = "BiDi";
    private static final String CHILD_SRC = "childSrc";
    private static final String CHILD_TGT = "childTgt";

    /**
     * This test method ensures that a POST of an RM action to a non-existent node
     * will result in a 404 status.
     *
     * @throws Exception
     */
    public void testPostActionToNonExistentNode() throws Exception
    {
        NodeRef nonExistentNode = new NodeRef("workspace://SpacesStore/09ca1e02-1c87-4a53-97e7-xxxxxxxxxxxx");

        // Construct the JSON request.
        JSONObject jsonPostData = new JSONObject();
        jsonPostData.put("nodeRef", nonExistentNode.toString());
        // Although the request specifies a 'reviewed' action, it does not matter what
        // action is specified here, as the non-existent Node should trigger a 404
        // before the action is executed.
        jsonPostData.put("name", "reviewed");

        // Submit the JSON request.
        String jsonPostString = jsonPostData.toString();

        final int expectedStatus = 404;
        sendRequest(new PostRequest(RMA_ACTIONS_URL, jsonPostString, APPLICATION_JSON), expectedStatus);
    }

    public void testPostReviewedAction() throws IOException, JSONException
    {
        NodeRef testRecord = utils.createRecord(recordFolder, "test.txt");

        // In this test, this property has a date-value equal to the model import time.
        Serializable pristineReviewAsOf = this.nodeService.getProperty(testRecord, PROP_REVIEW_AS_OF);

        // Construct the JSON request for 'reviewed'.
        String jsonString = new JSONStringer().object()
            .key("name").value("reviewed")
            .key("nodeRef").value(testRecord.toString())
            // These JSON params are just to test the submission of params. They'll be ignored.
            .key("params").object()
                .key("param1").value("one")
                .key("param2").value("two")
            .endObject()
        .endObject()
        .toString();

        // Submit the JSON request.
        final int expectedStatus = 200;
        Response rsp = sendRequest(new PostRequest(RMA_ACTIONS_URL,
                                 jsonString, APPLICATION_JSON), expectedStatus);

        String rspContent = rsp.getContentAsString();
        assertTrue(rspContent.contains("Successfully queued action [reviewed]"));

        Serializable newReviewAsOfDate = this.nodeService.getProperty(testRecord, PROP_REVIEW_AS_OF);
        assertFalse("The reviewAsOf property should have changed. Was " + pristineReviewAsOf,
        		pristineReviewAsOf.equals(newReviewAsOfDate));
    }

    public void testPostMultiReviewedAction() throws IOException, JSONException
    {
        NodeRef testRecord = utils.createRecord(recordFolder, "test1.txt");
        NodeRef testRecord2 = utils.createRecord(recordFolder, "test2.txt");
        NodeRef testRecord3 = utils.createRecord(recordFolder, "test3.txt");

        // In this test, this property has a date-value equal to the model import time.
        Serializable pristineReviewAsOf = this.nodeService.getProperty(testRecord, PROP_REVIEW_AS_OF);
        Serializable pristineReviewAsOf2 = this.nodeService.getProperty(testRecord2, PROP_REVIEW_AS_OF);
        Serializable pristineReviewAsOf3 = this.nodeService.getProperty(testRecord3, PROP_REVIEW_AS_OF);

        // Construct the JSON request for 'reviewed'.
        String jsonString = new JSONStringer().object()
            .key("name").value("reviewed")
            .key("nodeRefs").array()
                .value(testRecord.toString())
                .value(testRecord2.toString())
                .value(testRecord3.toString())
                .endArray()
            // These JSON params are just to test the submission of params. They'll be ignored.
            .key("params").object()
                .key("param1").value("one")
                .key("param2").value("two")
            .endObject()
        .endObject()
        .toString();

        // Submit the JSON request.
        final int expectedStatus = 200;
        Response rsp = sendRequest(new PostRequest(RMA_ACTIONS_URL,
                                 jsonString, APPLICATION_JSON), expectedStatus);

        String rspContent = rsp.getContentAsString();
        assertTrue(rspContent.contains("Successfully queued action [reviewed]"));

        Serializable newReviewAsOfDate = this.nodeService.getProperty(testRecord, PROP_REVIEW_AS_OF);
        assertFalse("The reviewAsOf property should have changed. Was " + pristineReviewAsOf,
                pristineReviewAsOf.equals(newReviewAsOfDate));
        Serializable newReviewAsOfDate2 = this.nodeService.getProperty(testRecord2, PROP_REVIEW_AS_OF);
        assertFalse("The reviewAsOf property should have changed. Was " + pristineReviewAsOf2,
                pristineReviewAsOf2.equals(newReviewAsOfDate2));
        Serializable newReviewAsOfDate3 = this.nodeService.getProperty(testRecord3, PROP_REVIEW_AS_OF);
        assertFalse("The reviewAsOf property should have changed. Was " + pristineReviewAsOf3,
                pristineReviewAsOf3.equals(newReviewAsOfDate3));
    }

    public void testActionParams() throws Exception
    {
     // Construct the JSON request for 'reviewed'.
        String jsonString = new JSONStringer().object()
            .key("name").value("testActionParams")
            .key("nodeRef").array()
                .value("nothing://nothing/nothing")
                .endArray()
            // These JSON params are just to test the submission of params. They'll be ignored.
            .key("params").object()
                .key(TestActionParams.PARAM_DATE).object()
                    .key("iso8601")
                    .value(ISO8601DateFormat.format(new Date()))
                    .endObject()
            .endObject()
        .endObject()
        .toString();

        // Submit the JSON request.
        final int expectedStatus = 200;
        //TODO Currently failing unit test.
        sendRequest(new PostRequest(RMA_ACTIONS_URL,
                                 jsonString, APPLICATION_JSON), expectedStatus);
    }

    public void testPostCustomReferenceDefinitions() throws IOException, JSONException
    {
        postCustomReferenceDefinitions();
    }

    /**
     * This method creates a child and a non-child reference and returns their generated ids.
     *
     *
     * @return String[] with element 0 = refId of p/c ref, 1 = refId pf bidi.
     */
	private String[] postCustomReferenceDefinitions() throws JSONException, IOException,
			UnsupportedEncodingException {
	    String[] result = new String[2];

		// 1. Child association.
        String jsonString = new JSONStringer().object()
            .key("referenceType").value(RelationshipType.PARENTCHILD)
            .key("source").value(CHILD_SRC)
            .key("target").value(CHILD_TGT)
        .endObject()
        .toString();

//        System.out.println(jsonString);

        // Submit the JSON request.
        final int expectedStatus = 200;
        Response rsp = sendRequest(new PostRequest(RMA_CUSTOM_REFS_DEFINITIONS_URL,
                                 jsonString, APPLICATION_JSON), expectedStatus);

        String rspContent = rsp.getContentAsString();
        assertTrue(rspContent.contains("success"));

//        System.out.println(rspContent);

        JSONObject jsonRsp = new JSONObject(new JSONTokener(rspContent));
        String generatedChildRefId = jsonRsp.getJSONObject("data").getString("refId");
        result[0] = generatedChildRefId;

        // 2. Non-child or standard association.
        jsonString = new JSONStringer().object()
            .key("referenceType").value(RelationshipType.BIDIRECTIONAL)
            .key("label").value(BI_DI)
        .endObject()
        .toString();

//        System.out.println(jsonString);

        // Submit the JSON request.
        rsp = sendRequest(new PostRequest(RMA_CUSTOM_REFS_DEFINITIONS_URL,
                                 jsonString, APPLICATION_JSON), expectedStatus);

        rspContent = rsp.getContentAsString();
        assertTrue(rspContent.contains("success"));

//        System.out.println(rspContent);

        jsonRsp = new JSONObject(new JSONTokener(rspContent));
        String generatedBidiRefId = jsonRsp.getJSONObject("data").getString("refId");
        result[1] = generatedBidiRefId;

        // Now assert that both have appeared in the data dictionary.
        AspectDefinition customAssocsAspect = dictionaryService.getAspect(ASPECT_CUSTOM_ASSOCIATIONS);
        assertNotNull("Missing customAssocs aspect", customAssocsAspect);

        QName newRefQname = adminService.getQNameForClientId(generatedChildRefId);
        Map<QName, AssociationDefinition> associations = customAssocsAspect.getAssociations();
		assertTrue("Custom child assoc not returned by dataDictionary.", associations.containsKey(newRefQname));

        newRefQname = adminService.getQNameForClientId(generatedBidiRefId);
        assertTrue("Custom std assoc not returned by dataDictionary.", customAssocsAspect.getAssociations().containsKey(newRefQname));

        return result;
	}

    public void testPutCustomPropertyDefinition() throws Exception
    {
        // POST to create a property definition with a known propId
        final String propertyLabel = "Original label åçîéøü";
        String propId = postCustomPropertyDefinition(propertyLabel, null);

        // PUT an updated label.
        final String updatedLabel = "Updated label πø^¨¥†®";
        String jsonString = new JSONStringer().object()
            .key("label").value(updatedLabel)
        .endObject()
        .toString();

        String propDefnUrl = "/api/rma/admin/custompropertydefinitions/" + propId;
        Response rsp = sendRequest(new PutRequest(propDefnUrl,
                                 jsonString, APPLICATION_JSON), 200);

        // GET from the URL again to ensure it's valid
        rsp = sendRequest(new GetRequest(propDefnUrl), 200);
        String rspContent = rsp.getContentAsString();

//         System.out.println(rspContent);

        // PUT an updated constraint ref.
        final String updatedConstraint = "rmc:tlList";
        jsonString = new JSONStringer().object()
            .key("constraintRef").value(updatedConstraint)
        .endObject()
        .toString();

        propDefnUrl = "/api/rma/admin/custompropertydefinitions/" + propId;
        rsp = sendRequest(new PutRequest(propDefnUrl,
                                 jsonString, APPLICATION_JSON), 200);

        rspContent = rsp.getContentAsString();

        JSONObject jsonRsp = new JSONObject(new JSONTokener(rspContent));
        String urlOfNewPropDef = jsonRsp.getString("url");
        assertNotNull("urlOfNewPropDef was null.", urlOfNewPropDef);

        // GET from the URL again to ensure it's valid
        rsp = sendRequest(new GetRequest(propDefnUrl), 200);
        rspContent = rsp.getContentAsString();

//        System.out.println(rspContent);

        jsonRsp = new JSONObject(new JSONTokener(rspContent));
        JSONObject dataObject = jsonRsp.getJSONObject("data");
        assertNotNull("JSON data object was null", dataObject);
        JSONObject customPropsObject = dataObject.getJSONObject("customProperties");
        assertNotNull("JSON customProperties object was null", customPropsObject);
        assertEquals("Wrong customProperties length.", 1, customPropsObject.length());

        Object keyToSoleProp = customPropsObject.keys().next();

        JSONObject newPropObject = customPropsObject.getJSONObject((String)keyToSoleProp);
        assertEquals("Wrong property label.", updatedLabel, newPropObject.getString("label"));
        JSONArray constraintRefsArray = newPropObject.getJSONArray("constraintRefs");
        assertEquals("ConstraintRefsArray wrong length.", 1, constraintRefsArray.length());
        String retrievedUpdatedTitle = constraintRefsArray.getJSONObject(0).getString("name");
        assertEquals("Constraints had wrong name.", "rmc:tlList", retrievedUpdatedTitle);

        // PUT again to remove all constraints
        jsonString = new JSONStringer().object()
            .key("constraintRef").value(null)
        .endObject()
        .toString();

        rsp = sendRequest(new PutRequest(propDefnUrl,
                                 jsonString, APPLICATION_JSON), 200);

        rspContent = rsp.getContentAsString();
//        System.out.println(rspContent);

        jsonRsp = new JSONObject(new JSONTokener(rspContent));

        // GET from the URL again to ensure it's valid
        rsp = sendRequest(new GetRequest(propDefnUrl), 200);
        rspContent = rsp.getContentAsString();

//         System.out.println(rspContent);

        jsonRsp = new JSONObject(new JSONTokener(rspContent));
        dataObject = jsonRsp.getJSONObject("data");
        assertNotNull("JSON data object was null", dataObject);
        customPropsObject = dataObject.getJSONObject("customProperties");
        assertNotNull("JSON customProperties object was null", customPropsObject);
        assertEquals("Wrong customProperties length.", 1, customPropsObject.length());

        keyToSoleProp = customPropsObject.keys().next();

        newPropObject = customPropsObject.getJSONObject((String)keyToSoleProp);
        assertEquals("Wrong property label.", updatedLabel, newPropObject.getString("label"));
        constraintRefsArray = newPropObject.getJSONArray("constraintRefs");
        assertEquals("ConstraintRefsArray wrong length.", 0, constraintRefsArray.length());

        // Finally PUT a constraint on a PropertyDefn that has been cleared of constraints.
        // This was raised as an issue
        final String readdedConstraint = "rmc:tlList";
        jsonString = new JSONStringer().object()
            .key("constraintRef").value(readdedConstraint)
        .endObject()
        .toString();

        propDefnUrl = "/api/rma/admin/custompropertydefinitions/" + propId;
        rsp = sendRequest(new PutRequest(propDefnUrl,
                                 jsonString, APPLICATION_JSON), 200);

        rspContent = rsp.getContentAsString();

        jsonRsp = new JSONObject(new JSONTokener(rspContent));
//        System.out.println("PUTting a constraint back again.");
//        System.out.println(rspContent);

        // And GET from the URL again
        rsp = sendRequest(new GetRequest(propDefnUrl), 200);
        rspContent = rsp.getContentAsString();

//        System.out.println(rspContent);

        jsonRsp = new JSONObject(new JSONTokener(rspContent));
        dataObject = jsonRsp.getJSONObject("data");
        assertNotNull("JSON data object was null", dataObject);
        customPropsObject = dataObject.getJSONObject("customProperties");
        assertNotNull("JSON customProperties object was null", customPropsObject);
        assertEquals("Wrong customProperties length.", 1, customPropsObject.length());

        keyToSoleProp = customPropsObject.keys().next();

        newPropObject = customPropsObject.getJSONObject((String)keyToSoleProp);
        assertEquals("Wrong property label.", updatedLabel, newPropObject.getString("label"));
        constraintRefsArray = newPropObject.getJSONArray("constraintRefs");
        assertEquals("ConstraintRefsArray wrong length.", 1, constraintRefsArray.length());
        String readdedUpdatedTitle = constraintRefsArray.getJSONObject(0).getString("name");
        assertEquals("Constraints had wrong name.", "rmc:tlList", readdedUpdatedTitle);
    }

    public void testGetCustomReferences() throws IOException, JSONException
    {
        // Ensure that there is at least one custom reference.
        postCustomReferenceDefinitions();

        // GET all custom reference definitions
        final int expectedStatus = 200;
        Response rsp = sendRequest(new GetRequest(RMA_CUSTOM_REFS_DEFINITIONS_URL), expectedStatus);

        JSONObject jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));

        JSONObject dataObj = (JSONObject)jsonRsp.get("data");
        assertNotNull("JSON 'data' object was null", dataObj);

        JSONArray customRefsObj = (JSONArray)dataObj.get("customReferences");
        assertNotNull("JSON 'customReferences' object was null", customRefsObj);

        assertTrue("There should be at least two custom references. Found " + customRefsObj, customRefsObj.length() >= 2);

        // GET a specific custom reference definition.
        // Here, we're using one of the built-in references
        // qname = rmc:versions
        rsp = sendRequest(new GetRequest(RMA_CUSTOM_REFS_DEFINITIONS_URL + "/" + CUSTOM_REF_VERSIONS.getLocalName()), expectedStatus);

        jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));

        dataObj = (JSONObject)jsonRsp.get("data");
        assertNotNull("JSON 'data' object was null", dataObj);

        customRefsObj = (JSONArray)dataObj.get("customReferences");
        assertNotNull("JSON 'customProperties' object was null", customRefsObj);

        assertTrue("There should be exactly 1 custom references. Found " + customRefsObj.length(), customRefsObj.length() == 1);
    }

    public void testGetDodCustomTypes() throws IOException, JSONException
    {
        final int expectedStatus = 200;
        Response rsp = sendRequest(new GetRequest("/api/rma/admin/dodcustomtypes"), expectedStatus);

        String rspContent = rsp.getContentAsString();
        JSONObject jsonRsp = new JSONObject(new JSONTokener(rspContent));

        // System.out.println(rspContent);

        JSONObject dataObj = (JSONObject)jsonRsp.get("data");
        assertNotNull("JSON 'data' object was null", dataObj);

        JSONArray customTypesObj = (JSONArray)dataObj.get("dodCustomTypes");
        assertNotNull("JSON 'dodCustomTypes' object was null", customTypesObj);

        assertEquals("Wrong DOD custom types count.", 4, customTypesObj.length());
    }

    public void testGetPostAndRemoveCustomReferenceInstances() throws Exception
    {
    	NodeRef testRecord1 = utils.createRecord(recordFolder, "testRecord1" + System.currentTimeMillis(), "The from recørd");
        NodeRef testRecord2 = utils.createRecord(recordFolder, "testRecord2" + System.currentTimeMillis(), "The to récord");

        String node1Url = testRecord1.toString().replace("://", "/");
        String refInstancesRecord1Url = MessageFormat.format(REF_INSTANCES_URL_FORMAT, node1Url);

        // Create reference types.
        String[] generatedRefIds = postCustomReferenceDefinitions();

        // Add a standard ref
        String jsonString = new JSONStringer().object()
            .key("toNode").value(testRecord2.toString())
            .key("refId").value(generatedRefIds[1])
        .endObject()
        .toString();

        Response rsp = sendRequest(new PostRequest(refInstancesRecord1Url,
	                             jsonString, APPLICATION_JSON), 200);

	    // Add a child ref
	    jsonString = new JSONStringer().object()
	    .key("toNode").value(testRecord2.toString())
	    .key("refId").value(generatedRefIds[0])
	    .endObject()
	    .toString();

//	    System.out.println(jsonString);

	    rsp = sendRequest(new PostRequest(refInstancesRecord1Url,
	    		jsonString, APPLICATION_JSON), 200);

//	    System.out.println(rsp.getContentAsString());

        // Now retrieve the applied references from the REST API
	    // 1. references on the 'from' record.
        rsp = sendRequest(new GetRequest(refInstancesRecord1Url), 200);

        String contentAsString = rsp.getContentAsString();
//        System.out.println(contentAsString);

        JSONObject jsonRsp = new JSONObject(new JSONTokener(contentAsString));

        JSONObject dataObj = (JSONObject)jsonRsp.get("data");
        assertNotNull("JSON 'data' object was null", dataObj);

        JSONArray customRefsFromArray = (JSONArray)dataObj.get("customReferencesFrom");
        assertNotNull("JSON 'customReferencesFrom' object was null", customRefsFromArray);

        int customRefsCount = customRefsFromArray.length();
        assertTrue("There should be at least one custom reference. Found " + customRefsFromArray, customRefsCount > 0);

        JSONArray customRefsToArray = (JSONArray)dataObj.get("customReferencesTo");
        assertNotNull("JSON 'customReferencesTo' object was null", customRefsToArray);
        assertEquals("customReferencesTo wrong length.", 0, customRefsToArray.length());

        // 2. Back-references on the 'to' record
        String node2Url = testRecord2.toString().replace("://", "/");
        String refInstancesRecord2Url = MessageFormat.format(REF_INSTANCES_URL_FORMAT, node2Url);

        rsp = sendRequest(new GetRequest(refInstancesRecord2Url), 200);

        contentAsString = rsp.getContentAsString();

        jsonRsp = new JSONObject(new JSONTokener(contentAsString));

        dataObj = (JSONObject)jsonRsp.get("data");
        assertNotNull("JSON 'data' object was null", dataObj);

        customRefsToArray = (JSONArray)dataObj.get("customReferencesTo");
        assertNotNull("JSON 'customReferencesTo' object was null", customRefsToArray);

        customRefsCount = customRefsToArray.length();
        assertTrue("There should be at least one custom reference. Found " + customRefsToArray, customRefsCount > 0);

        customRefsFromArray = (JSONArray)dataObj.get("customReferencesFrom");
        assertNotNull("JSON 'customReferencesFrom' object was null", customRefsFromArray);
        assertEquals("customReferencesFrom wrong length.", 0, customRefsFromArray.length());



        // Now to delete a reference instance of each type
        String protocol = testRecord2.getStoreRef().getProtocol();
        String identifier = testRecord2.getStoreRef().getIdentifier();
        String recId = testRecord2.getId();
        final String queryFormat = "?st={0}&si={1}&id={2}";
        String urlQueryString = MessageFormat.format(queryFormat, protocol, identifier, recId);

        rsp = sendRequest(new DeleteRequest(refInstancesRecord1Url + "/" + generatedRefIds[1] + urlQueryString), 200);
        assertTrue(rsp.getContentAsString().contains("success"));

        rsp = sendRequest(new DeleteRequest(refInstancesRecord1Url + "/"
        		+ generatedRefIds[0]
        		+ urlQueryString), 200);
        assertTrue(rsp.getContentAsString().contains("success"));

        // Get the reference instances back and confirm they've been removed.
        rsp = sendRequest(new GetRequest(refInstancesRecord1Url), 200);

        jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));

        dataObj = (JSONObject)jsonRsp.get("data");
        assertNotNull("JSON 'data' object was null", dataObj);

        customRefsFromArray = (JSONArray)dataObj.get("customReferencesFrom");
        assertNotNull("JSON 'customReferences' object was null", customRefsFromArray);
        assertTrue("customRefsArray was unexpectedly not empty.", customRefsFromArray.length() == 0);
    }

    public void testMob1630ShouldNotBeAbleToCreateTwoSupersedesReferencesOnOneRecordPair() throws Exception
    {
        // Create 2 test records.
        NodeRef testRecord1 = utils.createRecord(recordFolder, "testRecord1" + System.currentTimeMillis(), "The from recørd");
        NodeRef testRecord2 = utils.createRecord(recordFolder, "testRecord2" + System.currentTimeMillis(), "The to récord");

        String node1Url = testRecord1.toString().replace("://", "/");
        String node2Url = testRecord2.toString().replace("://", "/");
        String refInstancesRecord1Url = MessageFormat.format(REF_INSTANCES_URL_FORMAT, node1Url);
        String refInstancesRecord2Url = MessageFormat.format(REF_INSTANCES_URL_FORMAT, node2Url);

        {// Sanity check. There should be no references defined on these new records.
            Response rsp = sendRequest(new GetRequest(refInstancesRecord1Url), 200);

            String rspContent = rsp.getContentAsString();
            JSONObject jsonRsp = new JSONObject(new JSONTokener(rspContent));
            JSONObject dataObj = jsonRsp.getJSONObject("data");
            JSONArray refsFrom = dataObj.getJSONArray("customReferencesFrom");
            JSONArray refsTo = dataObj.getJSONArray("customReferencesTo");
            assertEquals("Incorrect from-refs count.", 0, refsFrom.length());
            assertEquals("Incorrect to-refs count.", 0, refsTo.length());
        }

        // Add a supersedes ref instance between them
        final String supersedesRefLocalName = CUSTOM_REF_SUPERSEDES.getLocalName();
        String jsonString = new JSONStringer().object()
            .key("toNode").value(testRecord2.toString())
            .key("refId").value(supersedesRefLocalName)
            .endObject()
        .toString();

        Response rsp = sendRequest(new PostRequest(refInstancesRecord1Url,
                jsonString, APPLICATION_JSON), 200);

        // The bug is that we can apply two such references which should not be allowed
        rsp = sendRequest(new PostRequest(refInstancesRecord1Url,
                jsonString, APPLICATION_JSON), 500);

        {// Retrieve reference instances on this pair of records.
            // The first record
            rsp = sendRequest(new GetRequest(refInstancesRecord1Url), 200);

            String rspContent = rsp.getContentAsString();
            JSONObject jsonRsp = new JSONObject(new JSONTokener(rspContent));
            JSONObject dataObj = jsonRsp.getJSONObject("data");
            JSONArray refsFrom = dataObj.getJSONArray("customReferencesFrom");
            JSONArray refsTo = dataObj.getJSONArray("customReferencesTo");
            assertEquals("Incorrect from-refs count.", 1, refsFrom.length());
            assertEquals("Incorrect to-refs count.", 0, refsTo.length());

            // The second record - the back-reference
            rsp = sendRequest(new GetRequest(refInstancesRecord2Url), 200);

            rspContent = rsp.getContentAsString();
            jsonRsp = new JSONObject(new JSONTokener(rspContent));
            dataObj = jsonRsp.getJSONObject("data");
            refsFrom = dataObj.getJSONArray("customReferencesFrom");
            refsTo = dataObj.getJSONArray("customReferencesTo");
            assertEquals("Incorrect from-refs count.", 0, refsFrom.length());
            assertEquals("Incorrect to-refs count.", 1, refsTo.length());
        }

        // Delete the reference instance
        String protocol = testRecord2.getStoreRef().getProtocol();
        String identifier = testRecord2.getStoreRef().getIdentifier();
        String recId = testRecord2.getId();
        final String queryFormat = "?st={0}&si={1}&id={2}";
        String urlQueryString = MessageFormat.format(queryFormat, protocol, identifier, recId);

        rsp = sendRequest(new DeleteRequest(refInstancesRecord1Url + "/" + supersedesRefLocalName + urlQueryString), 200);
        assertTrue(rsp.getContentAsString().contains("success"));

        {// Retrieve reference instances on this pair of records.
            // The first record
            rsp = sendRequest(new GetRequest(refInstancesRecord1Url), 200);

            String rspContent = rsp.getContentAsString();
            JSONObject jsonRsp = new JSONObject(new JSONTokener(rspContent));
            JSONObject dataObj = jsonRsp.getJSONObject("data");
            JSONArray refsFrom = dataObj.getJSONArray("customReferencesFrom");
            JSONArray refsTo = dataObj.getJSONArray("customReferencesTo");
            assertEquals("Incorrect from-refs count.", 0, refsFrom.length());
            assertEquals("Incorrect to-refs count.", 0, refsTo.length());

            // The second record - the back-reference
            rsp = sendRequest(new GetRequest(refInstancesRecord2Url), 200);

            rspContent = rsp.getContentAsString();
            jsonRsp = new JSONObject(new JSONTokener(rspContent));
            dataObj = jsonRsp.getJSONObject("data");
            refsFrom = dataObj.getJSONArray("customReferencesFrom");
            refsTo = dataObj.getJSONArray("customReferencesTo");
            assertEquals("Incorrect from-refs count.", 0, refsFrom.length());
            assertEquals("Incorrect to-refs count.", 0, refsTo.length());
        }
    }

    public void testPostCustomPropertyDefinition() throws Exception
    {
        long currentTimeMillis = System.currentTimeMillis();

        // Create one with no propId - it'll get generated.
        postCustomPropertyDefinition("customProperty" + currentTimeMillis, null);

        // Create another with an explicit propId.
        postCustomPropertyDefinition("customProperty" + currentTimeMillis, "prop" + currentTimeMillis);
    }

    /**
     * Creates a new property definition using a POST call.
     * GETs the resultant property definition.
     *
     * @param propertyLabel the label to use
     * @param propId the propId to use - null to have one generated.
     * @return the propId of the new property definition
     */
    private String postCustomPropertyDefinition(String propertyLabel, String propId) throws JSONException,
            IOException, UnsupportedEncodingException
    {
        String jsonString;
        if (propId == null)
        {
            jsonString = new JSONStringer().object()
                .key("label").value(propertyLabel)
                .key("description").value("Dynamically defined test property")
                .key("mandatory").value(false)
                .key("dataType").value("d:text")
                .key("element").value("record")
                .key("constraintRef").value("rmc:smList")
                // Note no propId
            .endObject()
            .toString();
        }
        else
        {
            jsonString = new JSONStringer().object()
            .key("label").value(propertyLabel)
            .key("description").value("Dynamically defined test property")
            .key("mandatory").value(false)
            .key("dataType").value("d:text")
            .key("element").value("record")
            .key("constraintRef").value("rmc:smList")
            .key("propId").value(propId)
        .endObject()
        .toString();
        }

        // Submit the JSON request.
        final int expectedStatus = 200;
        Response rsp = sendRequest(new PostRequest("/api/rma/admin/custompropertydefinitions?element=record",
                                 jsonString, APPLICATION_JSON), expectedStatus);

        String rspContent = rsp.getContentAsString();

//        System.out.println(rspContent);

        JSONObject jsonRsp = new JSONObject(new JSONTokener(rspContent));
        String urlOfNewPropDef = jsonRsp.getString("url");
        String newPropId = jsonRsp.getString("propId");

        assertNotNull("urlOfNewPropDef was null.", urlOfNewPropDef);

        // GET from the URL we're given to ensure it's valid
        rsp = sendRequest(new GetRequest(urlOfNewPropDef), 200);
        rspContent = rsp.getContentAsString();

//        System.out.println(rspContent);

        jsonRsp = new JSONObject(new JSONTokener(rspContent));
        JSONObject dataObject = jsonRsp.getJSONObject("data");
        assertNotNull("JSON data object was null", dataObject);
        JSONObject customPropsObject = dataObject.getJSONObject("customProperties");
        assertNotNull("JSON customProperties object was null", customPropsObject);
        assertEquals("Wrong customProperties length.", 1, customPropsObject.length());

        Object keyToSoleProp = customPropsObject.keys().next();

//        System.out.println("New property defn: " + keyToSoleProp);

        JSONObject newPropObject = customPropsObject.getJSONObject((String)keyToSoleProp);
        assertEquals("Wrong property label.", propertyLabel, newPropObject.getString("label"));

        return newPropId;
    }

    public void testPutCustomReferenceDefinition() throws Exception
    {
        String[] generatedRefIds = postCustomReferenceDefinitions();
        final String pcRefId = generatedRefIds[0];
        final String bidiRefId = generatedRefIds[1];

        // GET the custom refs in order to retrieve the label/source/target
        String refDefnUrl = "/api/rma/admin/customreferencedefinitions/" + bidiRefId;
        Response rsp = sendRequest(new GetRequest(refDefnUrl), 200);

        String rspContent = rsp.getContentAsString();
//        System.out.println(rspContent);
        JSONObject jsonRsp = new JSONObject(new JSONTokener(rspContent));

        refDefnUrl = "/api/rma/admin/customreferencedefinitions/" + pcRefId;
        rsp = sendRequest(new GetRequest(refDefnUrl), 200);

        rspContent = rsp.getContentAsString();
//        System.out.println(rspContent);
        jsonRsp = new JSONObject(new JSONTokener(rspContent));

        // Update the bidirectional reference.
        final String updatedBiDiLabel = "Updated label üøéîçå";
        String jsonString = new JSONStringer().object()
            .key("label").value(updatedBiDiLabel)
        .endObject()
        .toString();

        refDefnUrl = "/api/rma/admin/customreferencedefinitions/" + bidiRefId;
        rsp = sendRequest(new PutRequest(refDefnUrl,
                                 jsonString, APPLICATION_JSON), 200);

        rspContent = rsp.getContentAsString();
//        System.out.println(rspContent);

        jsonRsp = new JSONObject(new JSONTokener(rspContent));
        String urlOfNewRefDef = jsonRsp.getString("url");
        assertNotNull("urlOfNewRefDef was null.", urlOfNewRefDef);

        // GET the bidi reference to ensure it's valid
        rsp = sendRequest(new GetRequest(refDefnUrl), 200);
        rspContent = rsp.getContentAsString();

//        System.out.println(rspContent);

        jsonRsp = new JSONObject(new JSONTokener(rspContent));
        JSONObject dataObject = jsonRsp.getJSONObject("data");
        assertNotNull("JSON data object was null", dataObject);
        JSONArray customRefsObject = dataObject.getJSONArray("customReferences");
        assertNotNull("JSON customReferences object was null", customRefsObject);
        assertEquals("Wrong customReferences length.", 1, customRefsObject.length());

        JSONObject newRefObject = customRefsObject.getJSONObject(0);
        assertEquals("Wrong property label.", updatedBiDiLabel, newRefObject.getString("label"));

        // Update the parent/child reference.
        final String updatedPcSource = "Updated source ∆Ωç√∫";
        final String updatedPcTarget = "Updated target ∆Ωç√∫";
        jsonString = new JSONStringer().object()
            .key("source").value(updatedPcSource)
            .key("target").value(updatedPcTarget)
        .endObject()
        .toString();

        refDefnUrl = "/api/rma/admin/customreferencedefinitions/" + pcRefId;
        rsp = sendRequest(new PutRequest(refDefnUrl,
                                 jsonString, APPLICATION_JSON), 200);

        rspContent = rsp.getContentAsString();
//        System.out.println(rspContent);

        jsonRsp = new JSONObject(new JSONTokener(rspContent));
        urlOfNewRefDef = jsonRsp.getString("url");
        assertNotNull("urlOfNewRefDef was null.", urlOfNewRefDef);

        // GET the parent/child reference to ensure it's valid
        refDefnUrl = "/api/rma/admin/customreferencedefinitions/" + pcRefId;

        rsp = sendRequest(new GetRequest(refDefnUrl), 200);
        rspContent = rsp.getContentAsString();

//        System.out.println(rspContent);

        jsonRsp = new JSONObject(new JSONTokener(rspContent));
        dataObject = jsonRsp.getJSONObject("data");
        assertNotNull("JSON data object was null", dataObject);
        customRefsObject = dataObject.getJSONArray("customReferences");
        assertNotNull("JSON customReferences object was null", customRefsObject);
        assertEquals("Wrong customReferences length.", 1, customRefsObject.length());

        newRefObject = customRefsObject.getJSONObject(0);
        assertEquals("Wrong reference source.", updatedPcSource, newRefObject.getString("source"));
        assertEquals("Wrong reference target.", updatedPcTarget, newRefObject.getString("target"));
    }

    public void testGetCustomProperties() throws Exception
    {
        getCustomProperties();
    }

    private String getCustomProperties() throws Exception, IOException,
            UnsupportedEncodingException, JSONException
    {
        // Ensure that there is at least one custom property.
        this.testPostCustomPropertyDefinition();

        final int expectedStatus = 200;
        Response rsp = sendRequest(new GetRequest("/api/rma/admin/custompropertydefinitions?element=record"), expectedStatus);

        String contentAsString = rsp.getContentAsString();
//        System.out.println(contentAsString);
        JSONObject jsonRsp = new JSONObject(new JSONTokener(contentAsString));

        JSONObject dataObj = (JSONObject)jsonRsp.get("data");
        assertNotNull("JSON 'data' object was null", dataObj);

        JSONObject customPropsObj = (JSONObject)dataObj.get("customProperties");
        assertNotNull("JSON 'customProperties' object was null", customPropsObj);

        final int customPropsCount = customPropsObj.length();
        assertTrue("There should be at least one custom property. Found " + customPropsObj, customPropsCount > 0);

        return contentAsString;
    }

    public void testGetRecordMetaDataAspects() throws Exception
    {
        Response rsp = sendRequest(new GetRequest("/api/rma/recordmetadataaspects"), 200);
        String contentAsString = rsp.getContentAsString();
        System.out.println(contentAsString);
        JSONObject jsonRsp = new JSONObject(new JSONTokener(contentAsString));

        JSONObject dataObj = (JSONObject)jsonRsp.get("data");
        assertNotNull("JSON 'data' object was null", dataObj);

        JSONArray aspects = dataObj.getJSONArray("recordMetaDataAspects");
        assertNotNull(aspects);
        assertEquals(4, aspects.length());

        // TODO test the items themselves
    }

    public void testExport() throws Exception
    {
        String exportUrl = "/api/rma/admin/export";

        // define JSON POST body
        JSONObject jsonPostData = new JSONObject();
        JSONArray nodeRefs = new JSONArray();
        nodeRefs.put(recordFolder.toString());
        nodeRefs.put(recordFolder2.toString());
        jsonPostData.put("nodeRefs", nodeRefs);
        String jsonPostString = jsonPostData.toString();

        // make the export request
        Response rsp = sendRequest(new PostRequest(exportUrl, jsonPostString, APPLICATION_JSON), 200);
        assertEquals("application/acp", rsp.getContentType());
    }

    public void testExportInTransferFormat() throws Exception
    {
        String exportUrl = "/api/rma/admin/export";

        // define JSON POST body
        JSONObject jsonPostData = new JSONObject();
        JSONArray nodeRefs = new JSONArray();
        nodeRefs.put(recordFolder.toString());
        nodeRefs.put(recordFolder2.toString());
        jsonPostData.put("nodeRefs", nodeRefs);
        jsonPostData.put("transferFormat", true);
        String jsonPostString = jsonPostData.toString();

        // make the export request
        Response rsp = sendRequest(new PostRequest(exportUrl, jsonPostString, APPLICATION_JSON), 200);
        assertEquals("application/zip", rsp.getContentType());
    }

    public void testAudit() throws Exception
    {
        // call the list service to get audit events
        Response rsp = sendRequest(new GetRequest(GET_LIST_URL), 200);
        //System.out.println("GET : " + rsp.getContentAsString());
        assertEquals("application/json;charset=UTF-8", rsp.getContentType());

        // get response as JSON and check
        JSONObject jsonParsedObject = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        assertNotNull(jsonParsedObject);
        JSONObject data = jsonParsedObject.getJSONObject("data");
        JSONObject events = data.getJSONObject("auditEvents");
        JSONArray items = events.getJSONArray("items");
        assertEquals(auditService.getAuditEvents().size(), items.length());
        assertTrue(items.length() > 0);
        JSONObject item = items.getJSONObject(0);
        assertTrue(item.length() == 2);
        assertTrue(item.has("label"));
        assertTrue(item.has("value"));

        // get the full RM audit log and check response
        rsp = sendRequest(new GetRequest(RMA_AUDITLOG_URL), 200);
        assertEquals("application/json", rsp.getContentType());
        JSONObject jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));

        // get the full RM audit log as an HTML report and check response
        rsp = sendRequest(new GetRequest(RMA_AUDITLOG_URL + "?format=html"), 200);
        assertEquals("text/html", rsp.getContentType());

        // export the full RM audit log and check response
        rsp = sendRequest(new GetRequest(RMA_AUDITLOG_URL + "?export=true"), 200);
        assertEquals("application/json", rsp.getContentType());
        jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));

        // construct the URL
        String nodeUrl = recordCategory.toString().replace("://", "/");
        String auditUrl = MessageFormat.format(GET_NODE_AUDITLOG_URL_FORMAT, nodeUrl);

        // send request
        rsp = sendRequest(new GetRequest(auditUrl), 200);
        // check response
        assertEquals("application/json", rsp.getContentType());
        jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));

        // get the audit log with all restrictions in place
        String filteredAuditUrl = auditUrl + "?user=gavinc&size=5&from=2009-01-01&to=2009-12-31&event=Login";
        rsp = sendRequest(new GetRequest(filteredAuditUrl), 200);
        // check response
        assertEquals("application/json", rsp.getContentType());
        jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));

        // attempt to get the audit log with invalid restrictions in place
        filteredAuditUrl = auditUrl + "?user=fred&size=abc&from=2009&to=2010&property=wrong";
        rsp = sendRequest(new GetRequest(filteredAuditUrl), 200);
        assertEquals("application/json", rsp.getContentType());
        jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));

        checkAuditStatus(true);

        // start the RM audit log
        JSONObject jsonPostData = new JSONObject();
        jsonPostData.put("enabled", true);
        String jsonPostString = jsonPostData.toString();
        rsp = sendRequest(new PutRequest(RMA_AUDITLOG_URL, jsonPostString, APPLICATION_JSON), 200);

        checkAuditStatus(true);

        // check the response
        //System.out.println(rsp.getContentAsString());
        jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        JSONObject dataObj = (JSONObject)jsonRsp.get("data");
        assertNotNull("JSON 'data' object was null", dataObj);
        assertTrue(dataObj.getBoolean("enabled"));
        assertTrue(dataObj.has("started"));
        assertTrue(dataObj.has("stopped"));

        // stop the RM audit log
        jsonPostData = new JSONObject();
        jsonPostData.put("enabled", false);
        jsonPostString = jsonPostData.toString();
        rsp = sendRequest(new PutRequest(RMA_AUDITLOG_URL, jsonPostString, APPLICATION_JSON), 200);

        checkAuditStatus(false);

        // check the response
        //System.out.println(rsp.getContentAsString());
        jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        dataObj = (JSONObject)jsonRsp.get("data");
        assertNotNull("JSON 'data' object was null", dataObj);
        assertFalse(dataObj.getBoolean("enabled"));

        // clear the RM audit log
        rsp = sendRequest(new DeleteRequest(RMA_AUDITLOG_URL), 200);
        //System.out.println(rsp.getContentAsString());
        jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        dataObj = (JSONObject)jsonRsp.get("data");
        assertNotNull("JSON 'data' object was null", dataObj);
        assertFalse(dataObj.getBoolean("enabled"));
    }

    private void checkAuditStatus(boolean expected) throws Exception
    {
        Response rsp = sendRequest(new GetRequest(RMA_AUDITLOG_STATUS_URL), 200);
        JSONObject rspObj = new JSONObject(rsp.getContentAsString());
        JSONObject data = rspObj.getJSONObject("data");
        boolean enabled = data.getBoolean("enabled");
        assertEquals("Audit log status does not match expected status.", expected, enabled);

    }

    public void testFileAuditLogAsRecord() throws Exception
    {
        // Attempt to store audit log at non existent destination, make sure we get 404
        JSONObject jsonPostData = new JSONObject();
        jsonPostData.put("destination", "workspace://SpacesStore/09ca1e02-1c87-4a53-97e7-xxxxxxxxxxxx");
        String jsonPostString = jsonPostData.toString();
        Response rsp = sendRequest(new PostRequest(RMA_AUDITLOG_URL, jsonPostString, APPLICATION_JSON), 404);

        // Attempt to store audit log at wrong type of destination, make sure we get 400
        jsonPostData = new JSONObject();
        jsonPostData.put("destination", recordCategory.toString());
        jsonPostString = jsonPostData.toString();
        rsp = sendRequest(new PostRequest(RMA_AUDITLOG_URL, jsonPostString, APPLICATION_JSON), 400);


        // Store the full audit log as a record
        jsonPostData = new JSONObject();
        jsonPostData.put("destination", recordFolder2);
        jsonPostString = jsonPostData.toString();
        rsp = sendRequest(new PostRequest(RMA_AUDITLOG_URL, jsonPostString, APPLICATION_JSON), 200);

        // check the response
        System.out.println(rsp.getContentAsString());
        JSONObject jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        assertTrue(jsonRsp.has("success"));
        assertTrue(jsonRsp.getBoolean("success"));
        assertTrue(jsonRsp.has("record"));
        assertNotNull(jsonRsp.get("record"));
        assertTrue(nodeService.exists(new NodeRef(jsonRsp.getString("record"))));
        assertTrue(jsonRsp.has("recordName"));
        assertNotNull(jsonRsp.get("recordName"));
        assertTrue(jsonRsp.getString("recordName").startsWith("audit_"));

        // Store a filtered audit log as a record
        jsonPostData = new JSONObject();
        jsonPostData.put("destination", recordFolder2);
        jsonPostData.put("size", "50");
        jsonPostData.put("user", "gavinc");
        jsonPostData.put("event", "Update Metadata");
        jsonPostData.put("property", "{http://www.alfresco.org/model/content/1.0}modified");
        jsonPostString = jsonPostData.toString();
        rsp = sendRequest(new PostRequest(RMA_AUDITLOG_URL, jsonPostString, APPLICATION_JSON), 200);

        // check the response
        System.out.println(rsp.getContentAsString());
        jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        assertTrue(jsonRsp.has("success"));
        assertTrue(jsonRsp.getBoolean("success"));
        assertTrue(jsonRsp.has("record"));
        assertNotNull(jsonRsp.get("record"));
        assertTrue(nodeService.exists(new NodeRef(jsonRsp.getString("record"))));
        assertTrue(jsonRsp.has("recordName"));
        assertNotNull(jsonRsp.get("recordName"));
        assertTrue(jsonRsp.getString("recordName").startsWith("audit_"));
    }

    public void testPropertyLabelWithAccentedChars() throws Exception
    {
        final long number = System.currentTimeMillis();

        // Create a property with a simple name
        final String simplePropId = "simpleId" + number;
        postCustomPropertyDefinition("simple", simplePropId);

        // Create a property whose name has accented chars
        final String originalAccentedLabel = "øoê≈çœ";
        final String accentedPropId = "accentedId" + number;
        postCustomPropertyDefinition(originalAccentedLabel, accentedPropId);

        // We'll update the label on the simple-name property a few times.
        // This will cause the repeated read and write of the entire RM custom model xml file
        // This should also leave the accented-char property unchanged.
        putCustomPropDefinition("one", simplePropId);
        putCustomPropDefinition("two", simplePropId);
        putCustomPropDefinition("three", simplePropId);
        putCustomPropDefinition("four", simplePropId);
        putCustomPropDefinition("five", simplePropId);

        // Now get all the custom properties back.
        String rspContent = getCustomProperties();

        JSONObject rspObject = new JSONObject(new JSONTokener(rspContent));
        JSONObject dataObj = rspObject.getJSONObject("data");
        assertNotNull("jsonObject was null", dataObj);

        JSONObject customPropertiesObj = dataObj.getJSONObject("customProperties");
        assertNotNull("customPropertiesObj was null", customPropertiesObj);

        JSONObject accentedPropertyObj = customPropertiesObj.getJSONObject(RecordsManagementCustomModel.RM_CUSTOM_PREFIX
                + ":" + accentedPropId);
        assertNotNull("accentedPropertyObj was null", accentedPropertyObj);

        String labelObj = accentedPropertyObj.getString("label");
        assertEquals("labelObj was changed.", originalAccentedLabel, labelObj);
    }

    private void putCustomPropDefinition(String label, String id) throws JSONException, IOException,
            UnsupportedEncodingException
    {
        String jsonString = new JSONStringer().object()
            .key("label").value(label)
        .endObject()
        .toString();

        String propDefnUrl = "/api/rma/admin/custompropertydefinitions/" + id;
        Response rsp = sendRequest(new PutRequest(propDefnUrl,
                                 jsonString, APPLICATION_JSON), 200);

        String rspContent = rsp.getContentAsString();
//        System.out.println(rspContent);

        JSONObject jsonRsp = new JSONObject(new JSONTokener(rspContent));
        String urlOfNewPropDef = jsonRsp.getString("url");
        assertNotNull("urlOfNewPropDef was null.", urlOfNewPropDef);
    }
}
