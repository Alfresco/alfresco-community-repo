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
package org.alfresco.repo.web.scripts.forms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.springframework.extensions.webscripts.json.JSONUtils;

public class FormRestApiGet_Test extends AbstractTestFormRestApi 
{
    protected JSONObject createItemJSON(NodeRef nodeRef) throws Exception
    {
        JSONObject jsonPostData = new JSONObject();
        
        jsonPostData.put("itemKind", "node");
        
        StringBuilder builder = new StringBuilder();
        builder.append(nodeRef.getStoreRef().getProtocol()).append("/").append(
                    nodeRef.getStoreRef().getIdentifier()).append("/").append(nodeRef.getId());
        jsonPostData.put("itemId", builder.toString());
        
        return jsonPostData;
    }
    
    public void testResponseContentType() throws Exception
    {
        JSONObject jsonPostData = createItemJSON(this.referencingDocNodeRef);
        String jsonPostString = jsonPostData.toString();
        Response rsp = sendRequest(new PostRequest(FORM_DEF_URL, 
                    jsonPostString, APPLICATION_JSON), 200);
        assertEquals("application/json;charset=UTF-8", rsp.getContentType());
    }

    public void testGetFormForNonExistentNode() throws Exception
    {
        // Create a NodeRef with all digits changed to an 'x' char - 
        // this should make for a non-existent node.
        String missingId = this.referencingDocNodeRef.getId().replaceAll("\\d", "x");
        NodeRef missingNodeRef = new NodeRef(this.referencingDocNodeRef.getStoreRef(), 
                    missingId);
        
        JSONObject jsonPostData = createItemJSON(missingNodeRef);
        String jsonPostString = jsonPostData.toString();
        Response rsp = sendRequest(new PostRequest(FORM_DEF_URL, 
                    jsonPostString, APPLICATION_JSON), 404);
        assertEquals("application/json;charset=UTF-8", rsp.getContentType());
    }

    public void testJsonContentParsesCorrectly() throws Exception
    {
        JSONObject jsonPostData = createItemJSON(this.referencingDocNodeRef);
        String jsonPostString = jsonPostData.toString();
        Response rsp = sendRequest(new PostRequest(FORM_DEF_URL, 
                    jsonPostString, APPLICATION_JSON), 200);
        String jsonResponseString = rsp.getContentAsString();
        
        Object jsonObject = new JSONUtils().toObject(jsonResponseString);
        assertNotNull("JSON object was null.", jsonObject);
    }

    public void testJsonUpperStructure() throws Exception
    {
        JSONObject jsonPostData = createItemJSON(this.referencingDocNodeRef);
        String jsonPostString = jsonPostData.toString();
        Response rsp = sendRequest(new PostRequest(FORM_DEF_URL, 
                    jsonPostString, APPLICATION_JSON), 200);
        String jsonResponseString = rsp.getContentAsString();
        
        JSONObject jsonParsedObject = new JSONObject(new JSONTokener(jsonResponseString));
        assertNotNull(jsonParsedObject);
        
        Object dataObj = jsonParsedObject.get("data");
        assertEquals(JSONObject.class, dataObj.getClass());
        JSONObject rootDataObject = (JSONObject)dataObj;

        assertEquals(5, rootDataObject.length());
        String item = (String)rootDataObject.get("item");
        String submissionUrl = (String)rootDataObject.get("submissionUrl");
        String type = (String)rootDataObject.get("type");
        JSONObject definitionObject = (JSONObject)rootDataObject.get("definition");
        JSONObject formDataObject = (JSONObject)rootDataObject.get("formData");
        
        assertNotNull(item);
        assertNotNull(submissionUrl);
        assertNotNull(type);
        assertNotNull(definitionObject);
        assertNotNull(formDataObject);
    }

    @SuppressWarnings("unchecked")
    public void testJsonFormData() throws Exception
    {
        JSONObject jsonPostData = createItemJSON(this.referencingDocNodeRef);
        String jsonPostString = jsonPostData.toString();
        Response rsp = sendRequest(new PostRequest(FORM_DEF_URL, 
                    jsonPostString, APPLICATION_JSON), 200);
        String jsonResponseString = rsp.getContentAsString();

        JSONObject jsonParsedObject = new JSONObject(new JSONTokener(jsonResponseString));
        assertNotNull(jsonParsedObject);
        
        JSONObject rootDataObject = (JSONObject)jsonParsedObject.get("data");
        
        JSONObject formDataObject = (JSONObject)rootDataObject.get("formData");
        List<String> keys = new ArrayList<String>();
        for (Iterator iter = formDataObject.keys(); iter.hasNext(); )
        {
            String nextFieldName = (String)iter.next();
            assertEquals("Did not expect to find a colon char in " + nextFieldName,
                        -1, nextFieldName.indexOf(':'));
            keys.add(nextFieldName);
        }
        // Threshold is a rather arbitrary number. I simply want to ensure that there
        // are *some* entries in the formData hash.
        final int threshold = 5;
        int actualKeyCount = keys.size();
        assertTrue("Expected more than " + threshold +
                " entries in formData. Actual: " + actualKeyCount, actualKeyCount > threshold);
    }
    
    @SuppressWarnings("unchecked")
    public void testJsonDefinitionFields() throws Exception
    {
        JSONObject jsonPostData = createItemJSON(this.referencingDocNodeRef);
        String jsonPostString = jsonPostData.toString();
        Response rsp = sendRequest(new PostRequest(FORM_DEF_URL, 
                    jsonPostString, APPLICATION_JSON), 200);
        String jsonResponseString = rsp.getContentAsString();
        
        JSONObject jsonParsedObject = new JSONObject(new JSONTokener(jsonResponseString));
        assertNotNull(jsonParsedObject);
        
        JSONObject rootDataObject = (JSONObject)jsonParsedObject.get("data");
        
        JSONObject definitionObject = (JSONObject)rootDataObject.get("definition");
        
        JSONArray fieldsArray = (JSONArray)definitionObject.get("fields");
        
        for (int i = 0; i < fieldsArray.length(); i++)
        {
            Object nextObj = fieldsArray.get(i);
            
            JSONObject nextJsonObject = (JSONObject)nextObj;
            List<String> fieldKeys = new ArrayList<String>();
            for (Iterator iter2 = nextJsonObject.keys(); iter2.hasNext(); )
            {
                fieldKeys.add((String)iter2.next());
            }
            for (String s : fieldKeys)
            {
                if (s.equals("mandatory") || s.equals("protectedField"))
                {
                    assertEquals("JSON booleans should be actual booleans.", java.lang.Boolean.class, nextJsonObject.get(s).getClass());
                }
            }
        }
    }
    
    public void testJsonSelectedFields() throws Exception
    {
        JSONObject jsonPostData = createItemJSON(this.referencingDocNodeRef);
        JSONArray jsonFields = new JSONArray();
        jsonFields.put("cm:name");
        jsonFields.put("cm:title");
        jsonFields.put("cm:publisher");
        jsonPostData.put("fields", jsonFields);
        
        // Submit the JSON request.
        String jsonPostString = jsonPostData.toString();        
        Response rsp = sendRequest(new PostRequest(FORM_DEF_URL, jsonPostString,
                APPLICATION_JSON), 200);
        
        String jsonResponseString = rsp.getContentAsString();
        JSONObject jsonParsedObject = new JSONObject(new JSONTokener(jsonResponseString));
        assertNotNull(jsonParsedObject);
        
        JSONObject rootDataObject = (JSONObject)jsonParsedObject.get("data");
        JSONObject definitionObject = (JSONObject)rootDataObject.get("definition");
        JSONArray fieldsArray = (JSONArray)definitionObject.get("fields");
        assertEquals("Expected 2 fields", 2, fieldsArray.length());
        
        // get the name and title definitions
        JSONObject nameField = (JSONObject)fieldsArray.get(0);
        JSONObject titleField = (JSONObject)fieldsArray.get(1);
        String nameFieldDataKey = nameField.getString("dataKeyName");
        String titleFieldDataKey = titleField.getString("dataKeyName");
        
        // get the data and check it
        JSONObject formDataObject = (JSONObject)rootDataObject.get("formData");
        assertNotNull("Expected to find cm:name data", formDataObject.get(nameFieldDataKey));
        assertNotNull("Expected to find cm:title data", formDataObject.get(titleFieldDataKey));
        assertEquals(TEST_FORM_TITLE, formDataObject.get("prop_cm_title"));
    }
    
    public void testJsonForcedFields() throws Exception
    {
        JSONObject jsonPostData = createItemJSON(this.referencingDocNodeRef);
        
        JSONArray jsonFields = new JSONArray();
        jsonFields.put("cm:name");
        jsonFields.put("cm:title");
        jsonFields.put("cm:publisher");
        jsonFields.put("cm:wrong");
        jsonPostData.put("fields", jsonFields);
        
        JSONArray jsonForcedFields = new JSONArray();
        jsonForcedFields.put("cm:publisher");
        jsonForcedFields.put("cm:wrong");
        jsonPostData.put("force", jsonForcedFields);
        
        // Submit the JSON request.
        String jsonPostString = jsonPostData.toString();
        Response rsp = sendRequest(new PostRequest(FORM_DEF_URL, jsonPostString,
                APPLICATION_JSON), 200);
        
        String jsonResponseString = rsp.getContentAsString();
        JSONObject jsonParsedObject = new JSONObject(new JSONTokener(jsonResponseString));
        assertNotNull(jsonParsedObject);
        
        JSONObject rootDataObject = (JSONObject)jsonParsedObject.get("data");
        JSONObject definitionObject = (JSONObject)rootDataObject.get("definition");
        JSONArray fieldsArray = (JSONArray)definitionObject.get("fields");
        assertEquals("Expected 3 fields", 3, fieldsArray.length());
        
        // get the name and title definitions
        JSONObject nameField = (JSONObject)fieldsArray.get(0);
        JSONObject titleField = (JSONObject)fieldsArray.get(1);
        String nameFieldDataKey = nameField.getString("dataKeyName");
        String titleFieldDataKey = titleField.getString("dataKeyName");
        
        // get the data and check it
        JSONObject formDataObject = (JSONObject)rootDataObject.get("formData");
        assertNotNull("Expected to find cm:name data", formDataObject.get(nameFieldDataKey));
        assertNotNull("Expected to find cm:title data", formDataObject.get(titleFieldDataKey));
        assertEquals(TEST_FORM_TITLE, formDataObject.get("prop_cm_title"));
    }
}
