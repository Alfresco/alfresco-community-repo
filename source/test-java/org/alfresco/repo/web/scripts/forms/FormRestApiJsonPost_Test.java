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
package org.alfresco.repo.web.scripts.forms;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.json.JSONException;
import org.json.JSONObject;

public class FormRestApiJsonPost_Test extends AbstractTestFormRestApi
{
    private static final String PROP_CM_DESCRIPTION = "prop_cm_description";
    private static final String PROP_MIMETYPE = "prop_mimetype";
    private static final String ASSOC_CM_REFERENCES = "assoc_cm_references";
    private static final String ASSOC_CM_REFERENCES_ADDED = "assoc_cm_references_added";
    private static final String ASSOC_CM_REFERENCES_REMOVED = "assoc_cm_references_removed";
    private static final String ASSOC_SYS_CHILDREN = "assoc_sys_children";
    private static final String ASSOC_SYS_CHILDREN_ADDED = "assoc_sys_children_added";
    private static final String ASSOC_SYS_CHILDREN_REMOVED = "assoc_sys_children_removed";

    public void testSimpleJsonPostRequest() throws IOException, JSONException
    {
        // Retrieve and store the original property value.
        Serializable originalDescription =
            nodeService.getProperty(referencingDocNodeRef, ContentModel.PROP_DESCRIPTION);
        assertEquals(TEST_FORM_DESCRIPTION, originalDescription);
        
        // get the original mimetype
        String originalMimetype = null;
        ContentData content = (ContentData)this.nodeService.getProperty(referencingDocNodeRef, ContentModel.PROP_CONTENT);
        if (content != null)
        {
            originalMimetype = content.getMimetype();
        }
        
        // Construct some JSON to represent a new value.
        JSONObject jsonPostData = new JSONObject();
        final String proposedNewDescription = "Modified Description";
        jsonPostData.put(PROP_CM_DESCRIPTION, proposedNewDescription);
        jsonPostData.put(PROP_MIMETYPE, MimetypeMap.MIMETYPE_HTML);
        
        // Submit the JSON request.
        String jsonPostString = jsonPostData.toString();
        sendRequest(new PostRequest(referencingNodeUpdateUrl, jsonPostString, APPLICATION_JSON), 200);

        // The nodeService should give us the modified property.
        Serializable modifiedDescription =
            nodeService.getProperty(referencingDocNodeRef, ContentModel.PROP_DESCRIPTION);
        assertEquals(proposedNewDescription, modifiedDescription);
        
        // get the modified mimetype
        String modifiedMimetype = null;
        content = (ContentData)this.nodeService.getProperty(referencingDocNodeRef, ContentModel.PROP_CONTENT);
        if (content != null)
        {
            modifiedMimetype = content.getMimetype();
        }
        assertEquals(MimetypeMap.MIMETYPE_HTML, modifiedMimetype);

        // The Rest API should also give us the modified property.
        /*
        Response response = sendRequest(new GetRequest(referencingNodeUpdateUrl), 200);
        JSONObject jsonGetResponse = new JSONObject(response.getContentAsString());
        JSONObject jsonDataObj = (JSONObject)jsonGetResponse.get("data");
        assertNotNull(jsonDataObj);

        JSONObject formData = (JSONObject)jsonDataObj.get("formData");
        assertNotNull(formData);
        String retrievedValue = (String)formData.get(PROP_CM_DESCRIPTION);
        assertEquals(modifiedDescription, retrievedValue);
        String retrievedMimetype = (String)formData.get(PROP_MIMETYPE);
        assertEquals(MimetypeMap.MIMETYPE_HTML, modifiedMimetype);*/
    }
    
    /**
     * This test method attempts to add new associations between existing nodes.
     */
    public void testAddNewAssociationsToNode() throws Exception
    {
        List<NodeRef> associatedNodes;
        checkOriginalAssocsBeforeChanges();
        
        // Add three additional associations
        JSONObject jsonPostData = new JSONObject();
        String assocsToAdd = associatedDoc_C + "," + associatedDoc_D + "," + associatedDoc_E;
        jsonPostData.put(ASSOC_CM_REFERENCES_ADDED, assocsToAdd);
        String jsonPostString = jsonPostData.toString();

        sendRequest(new PostRequest(referencingNodeUpdateUrl, jsonPostString, APPLICATION_JSON), 200);

        // Check the now updated associations via the node service
        List<AssociationRef> modifiedAssocs = nodeService.getTargetAssocs(referencingDocNodeRef, RegexQNamePattern.MATCH_ALL);
        assertEquals(5, modifiedAssocs.size());

        // Extract the target nodeRefs to make them easier to examine
        associatedNodes = new ArrayList<NodeRef>(5);
        for (AssociationRef assocRef : modifiedAssocs)
        {
            associatedNodes.add(assocRef.getTargetRef());
        }

        assertTrue(associatedNodes.contains(associatedDoc_A));
        assertTrue(associatedNodes.contains(associatedDoc_B));
        assertTrue(associatedNodes.contains(associatedDoc_C));
        assertTrue(associatedNodes.contains(associatedDoc_D));
        assertTrue(associatedNodes.contains(associatedDoc_E));
        
        // The Rest API should also give us the modified assocs.
        /*Response response = sendRequest(new GetRequest(referencingNodeUpdateUrl), 200);
        String jsonRspString = response.getContentAsString();
        JSONObject jsonGetResponse = new JSONObject(jsonRspString);
        JSONObject jsonData = (JSONObject)jsonGetResponse.get("data");
        assertNotNull(jsonData);

        JSONObject jsonFormData = (JSONObject)jsonData.get("formData");
        assertNotNull(jsonFormData);
        
        String jsonAssocs = (String)jsonFormData.get(ASSOC_CM_REFERENCES);
        
        // We expect exactly 5 assocs on the test node
        assertEquals(5, jsonAssocs.split(",").length);
        for (AssociationRef assocRef : modifiedAssocs)
        {
            assertTrue(jsonAssocs.contains(assocRef.getTargetRef().toString()));
        }*/
    }

    /**
     * This test method attempts to remove an existing association between two existing
     * nodes.
     */
    public void testRemoveAssociationsFromNode() throws Exception
    {
        List<NodeRef> associatedNodes;
        checkOriginalAssocsBeforeChanges();

        // Remove an association
        JSONObject jsonPostData = new JSONObject();
        String assocsToRemove = associatedDoc_B.toString();
        jsonPostData.put(ASSOC_CM_REFERENCES_REMOVED, assocsToRemove);
        String jsonPostString = jsonPostData.toString();

        sendRequest(new PostRequest(referencingNodeUpdateUrl, jsonPostString, APPLICATION_JSON), 200);

        // Check the now updated associations via the node service
        List<AssociationRef> modifiedAssocs = nodeService.getTargetAssocs(referencingDocNodeRef, RegexQNamePattern.MATCH_ALL);
        assertEquals(1, modifiedAssocs.size());

        // Extract the target nodeRefs to make them easier to examine
        associatedNodes = new ArrayList<NodeRef>(5);
        for (AssociationRef assocRef : modifiedAssocs)
        {
            associatedNodes.add(assocRef.getTargetRef());
        }

        assertTrue(associatedNodes.contains(associatedDoc_A));
        
        // The Rest API should also give us the modified assocs.
        /*Response response = sendRequest(new GetRequest(referencingNodeUpdateUrl), 200);
        String jsonRspString = response.getContentAsString();
        JSONObject jsonGetResponse = new JSONObject(jsonRspString);
        JSONObject jsonData = (JSONObject)jsonGetResponse.get("data");
        assertNotNull(jsonData);

        JSONObject jsonFormData = (JSONObject)jsonData.get("formData");
        assertNotNull(jsonFormData);
        
        String jsonAssocs = (String)jsonFormData.get(ASSOC_CM_REFERENCES);
        
        // We expect exactly 1 assoc on the test node
        assertEquals(1, jsonAssocs.split(",").length);
        for (AssociationRef assocRef : modifiedAssocs)
        {
            assertTrue(jsonAssocs.contains(assocRef.getTargetRef().toString()));
        }*/
    }

    /**
     * This test method attempts to add the same association twice. This attempt will
     * not succeed, but the test case is to confirm that there is no exception thrown
     * back across the REST API.
     */
    public void testAddAssocThatAlreadyExists() throws Exception
    {
        checkOriginalAssocsBeforeChanges();

        // Add an association
        JSONObject jsonPostData = new JSONObject();
        String assocsToAdd = associatedDoc_C.toString();
        jsonPostData.put(ASSOC_CM_REFERENCES_ADDED, assocsToAdd);
        String jsonPostString = jsonPostData.toString();

        sendRequest(new PostRequest(referencingNodeUpdateUrl, jsonPostString, APPLICATION_JSON), 200);

        // Try to add the same association again
        sendRequest(new PostRequest(referencingNodeUpdateUrl, jsonPostString, APPLICATION_JSON), 200);
    }
    
    /**
     * This test method attempts to remove an association that does not exist. This
     * attempt will not succeed, but the test case is to confirm that there is no
     * exception thrown back across the REST API.
     */
    public void testRemoveAssocThatDoesNotExist() throws Exception
    {
        checkOriginalAssocsBeforeChanges();

        // Remove a non-existent association
        JSONObject jsonPostData = new JSONObject();
        String assocsToRemove = associatedDoc_E.toString();
        jsonPostData.put(ASSOC_CM_REFERENCES_REMOVED, assocsToRemove);
        String jsonPostString = jsonPostData.toString();

        sendRequest(new PostRequest(referencingNodeUpdateUrl, jsonPostString, APPLICATION_JSON), 200);
    }

    /**
     * This test method attempts to add new associations between existing nodes.
     */
    public void testAddNewChildAssociationsToNode() throws Exception
    {
        List<NodeRef> associatedNodes;
        checkOriginalChildAssocsBeforeChanges();
        
        // Add three additional associations
        JSONObject jsonPostData = new JSONObject();
        String assocsToAdd = childDoc_C + "," + childDoc_D + "," + childDoc_E;
        jsonPostData.put(ASSOC_SYS_CHILDREN_ADDED, assocsToAdd);
        String jsonPostString = jsonPostData.toString();

        sendRequest(new PostRequest(containingNodeUpdateUrl.toString(), jsonPostString, APPLICATION_JSON), 200);

        // Check the now updated child associations via the node service
        List<ChildAssociationRef> modifiedAssocs = nodeService.getChildAssocs(containerNodeRef);
        assertEquals(5, modifiedAssocs.size());

        // Extract the target nodeRefs to make them easier to examine
        associatedNodes = new ArrayList<NodeRef>(5);
        for (ChildAssociationRef assocRef : modifiedAssocs)
        {
            associatedNodes.add(assocRef.getChildRef());
        }

        assertTrue(associatedNodes.contains(childDoc_A));
        assertTrue(associatedNodes.contains(childDoc_B));
        assertTrue(associatedNodes.contains(childDoc_C));
        assertTrue(associatedNodes.contains(childDoc_D));
        assertTrue(associatedNodes.contains(childDoc_E));
        
        // The Rest API should also give us the modified assocs.
        /*Response response = sendRequest(new GetRequest(containingNodeUpdateUrl), 200);
        String jsonRspString = response.getContentAsString();
        
        JSONObject jsonGetResponse = new JSONObject(jsonRspString);
        JSONObject jsonData = (JSONObject)jsonGetResponse.get("data");
        assertNotNull(jsonData);

        JSONObject jsonFormData = (JSONObject)jsonData.get("formData");
        assertNotNull(jsonFormData);
        
        String jsonAssocs = (String)jsonFormData.get(ASSOC_SYS_CHILDREN);
        
        // We expect exactly 5 assocs on the test node
        assertEquals(5, jsonAssocs.split(",").length);
        for (ChildAssociationRef assocRef : modifiedAssocs)
        {
            String childNodeRef = assocRef.getChildRef().toString();
            assertTrue(jsonAssocs.contains(childNodeRef));
            assertTrue(NodeRef.isNodeRef(childNodeRef));
        }*/
    }
    
    /**
     * This test method attempts to remove an existing child association between two
     * existing nodes.
     */
    public void testRemoveChildAssociationsFromNode() throws Exception
    {
        List<NodeRef> associatedNodes;
        checkOriginalChildAssocsBeforeChanges();

        // Remove an association
        JSONObject jsonPostData = new JSONObject();
        String assocsToRemove = childDoc_B.toString();
        jsonPostData.put(ASSOC_SYS_CHILDREN_REMOVED, assocsToRemove);
        String jsonPostString = jsonPostData.toString();

        sendRequest(new PostRequest(containingNodeUpdateUrl, jsonPostString, APPLICATION_JSON), 200);

        // Check the now updated child associations via the node service
        List<ChildAssociationRef> modifiedAssocs = nodeService.getChildAssocs(containerNodeRef);
        assertEquals(1, modifiedAssocs.size());

        // Extract the target nodeRefs to make them easier to examine
        associatedNodes = new ArrayList<NodeRef>(5);
        for (ChildAssociationRef assocRef : modifiedAssocs)
        {
            associatedNodes.add(assocRef.getChildRef());
        }

        assertTrue(associatedNodes.contains(childDoc_A));
        
        // The Rest API should also give us the modified assocs.
        /*Response response = sendRequest(new GetRequest(containingNodeUpdateUrl), 200);
        String jsonRspString = response.getContentAsString();
        JSONObject jsonGetResponse = new JSONObject(jsonRspString);
        JSONObject jsonData = (JSONObject)jsonGetResponse.get("data");
        assertNotNull(jsonData);

        JSONObject jsonFormData = (JSONObject)jsonData.get("formData");
        assertNotNull(jsonFormData);
        
        String jsonAssocs = (String)jsonFormData.get(ASSOC_SYS_CHILDREN);
        
        // We expect exactly 1 assoc on the test node
        assertEquals(1, jsonAssocs.split(",").length);
        for (ChildAssociationRef assocRef : modifiedAssocs)
        {
            assertTrue(jsonAssocs.contains(assocRef.getChildRef().toString()));
        }*/
    }

    /**
     * This test method attempts to add the same child association twice. This attempt
     * will not succeed, but the test case is to confirm that there is no exception thrown
     * back across the REST API.
     */
    public void testAddChildAssocThatAlreadyExists() throws Exception
    {
        checkOriginalChildAssocsBeforeChanges();

        // Add an association
        JSONObject jsonPostData = new JSONObject();
        String assocsToAdd = this.childDoc_C.toString();
        jsonPostData.put(ASSOC_SYS_CHILDREN_ADDED, assocsToAdd);
        String jsonPostString = jsonPostData.toString();

        sendRequest(new PostRequest(referencingNodeUpdateUrl, jsonPostString, APPLICATION_JSON), 200);

        // Try to add the same child association again
        sendRequest(new PostRequest(referencingNodeUpdateUrl, jsonPostString, APPLICATION_JSON), 200);
    }
    
    /**
     * This test method attempts to remove a child association that does not exist. This
     * attempt will not succeed, but the test case is to confirm that there is no
     * exception thrown back across the REST API.
     */
    public void testRemoveChildAssocThatDoesNotExist() throws Exception
    {
        checkOriginalChildAssocsBeforeChanges();

        // Remove a non-existent child association
        JSONObject jsonPostData = new JSONObject();
        String assocsToRemove = childDoc_E.toString();
        jsonPostData.put(ASSOC_SYS_CHILDREN_REMOVED, assocsToRemove);
        String jsonPostString = jsonPostData.toString();

        sendRequest(new PostRequest(referencingNodeUpdateUrl, jsonPostString, APPLICATION_JSON), 200);
    }


    private void checkOriginalAssocsBeforeChanges()
    {
        List<AssociationRef> originalAssocs = nodeService.getTargetAssocs(referencingDocNodeRef, RegexQNamePattern.MATCH_ALL);
        assertEquals(2, originalAssocs.size());

        List<NodeRef> associatedNodes = new ArrayList<NodeRef>(2);
        associatedNodes.add(originalAssocs.get(0).getTargetRef());
        associatedNodes.add(originalAssocs.get(1).getTargetRef());
        
        assertTrue(associatedNodes.contains(associatedDoc_A));
        assertTrue(associatedNodes.contains(associatedDoc_B));
    }

    private void checkOriginalChildAssocsBeforeChanges()
    {
        List<ChildAssociationRef> originalChildAssocs = nodeService.getChildAssocs(containerNodeRef);
        assertEquals(2, originalChildAssocs.size());

        List<NodeRef> associatedNodes = new ArrayList<NodeRef>(2);
        associatedNodes.add(originalChildAssocs.get(0).getChildRef());
        associatedNodes.add(originalChildAssocs.get(1).getChildRef());
        
        assertTrue(associatedNodes.contains(childDoc_A));
        assertTrue(associatedNodes.contains(childDoc_B));
    }
}
