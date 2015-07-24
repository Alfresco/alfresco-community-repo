/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript;

import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.PROP_CLASSIFICATION_AGENCY;
import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.PROP_CLASSIFIED_BY;
import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.PROP_CURRENT_CLASSIFICATION;
import static org.alfresco.util.GUID.generate;

import java.io.IOException;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * REST API tests for classification of a content and editing content
 *
 * @author Tuna Aksoy
 * @since 3.0.a
 */
public class ClassificationRestApiTest extends BaseRMWebScriptTestCase
{
    /** URL for the REST API */
    private static final String RM_CLASSIFY_ACTIONS_URL = "/api/node/%s/%s/%s/classify";

    /** Content type */
    private static final String APPLICATION_JSON = "application/json";

    /** Constant for response */
    private static final String RESPONSE_SUCCESS = "success";

    /** Constants for classification request body parameters */
    private static final String CLASSIFICATION_LEVEL_ID = "classificationLevelId";
    private static final String CLASSIFIED_BY = "classifiedBy";
    private static final String CLASSIFICATION_AGENCY = "classificationAgency";
    private static final String CLASSIFICATION_REASONS = "classificationReasons";
    private static final String ID = "id";
    private static final String CLASSIFICATION_LEVEL_ID1_VALUE = "level1";
    private static final String CLASSIFICATION_LEVEL_ID2_VALUE = "level2";
    private static final String CLASSIFIED_BY1 = generate();
    private static final String CLASSIFIED_BY2 = generate();
    private static final String CLASSIFICATION_AGENCY1 = generate();
    private static final String CLASSIFICATION_AGENCY2 = generate();
    private static final String CLASSIFICATION_REASON_1_ID_VALUE = "Test Reason 1";
    private static final String CLASSIFICATION_REASON_2_ID_VALUE = "Test Reason 1.2";
    private static final String CLASSIFICATION_REASON_3_ID_VALUE = "Test Reason 1.2(c)";

    /** Tests classifying a content and editing a classified content */
    @SuppressWarnings("unchecked")
    public void testClassifyEditContent() throws IOException, JSONException
    {
        // Create record to classify
        NodeRef recordCategory = filePlanService.createRecordCategory(filePlan, generate());
        NodeRef recordFolder = recordFolderService.createRecordFolder(recordCategory, generate());
        NodeRef record = utils.createRecord(recordFolder, generate());

        // Format the URL
        StoreRef storeRef = record.getStoreRef();
        String storeProtocol = storeRef.getProtocol();
        String storeId = storeRef.getIdentifier();
        String id = record.getId();
        String url = String.format(RM_CLASSIFY_ACTIONS_URL, storeProtocol, storeId, id);

        // Classify content
        JSONObject requestBody = new JSONObject();
        requestBody.put(CLASSIFICATION_LEVEL_ID, CLASSIFICATION_LEVEL_ID1_VALUE);
        requestBody.put(CLASSIFIED_BY, CLASSIFIED_BY1);
        requestBody.put(CLASSIFICATION_AGENCY, CLASSIFICATION_AGENCY1);
        JSONArray classificationReasons = new JSONArray();
        classificationReasons.put(new JSONObject().put(ID, CLASSIFICATION_REASON_1_ID_VALUE));
        classificationReasons.put(new JSONObject().put(ID, CLASSIFICATION_REASON_2_ID_VALUE));
        requestBody.put(CLASSIFICATION_REASONS, classificationReasons);
        Response response = sendRequest(new PostRequest(url, requestBody.toString(), APPLICATION_JSON), 200);
        String responseContent = response.getContentAsString();
        JSONObject responseAsJson = new JSONObject(responseContent);

        assertNotNull(responseAsJson);
        assertTrue(responseAsJson.getBoolean(RESPONSE_SUCCESS));
        assertEquals(CLASSIFICATION_LEVEL_ID1_VALUE, nodeService.getProperty(record, PROP_CURRENT_CLASSIFICATION));
        assertEquals(CLASSIFIED_BY1, nodeService.getProperty(record, PROP_CLASSIFIED_BY));
        assertEquals(CLASSIFICATION_AGENCY1, nodeService.getProperty(record, PROP_CLASSIFICATION_AGENCY));
        List<String> classificationReasonsList = (List<String>) nodeService.getProperty(record, ClassifiedContentModel.PROP_CLASSIFICATION_REASONS);
        assertNotNull(classificationReasonsList);
        assertEquals(2, classificationReasonsList.size());
        assertTrue(classificationReasonsList.contains(CLASSIFICATION_REASON_1_ID_VALUE));
        assertTrue(classificationReasonsList.contains(CLASSIFICATION_REASON_2_ID_VALUE));

        // Edit classified content
        requestBody = new JSONObject();
        requestBody.put(CLASSIFICATION_LEVEL_ID, CLASSIFICATION_LEVEL_ID2_VALUE);
        requestBody.put(CLASSIFIED_BY, CLASSIFIED_BY2);
        requestBody.put(CLASSIFICATION_AGENCY, CLASSIFICATION_AGENCY2);
        classificationReasons = new JSONArray();
        classificationReasons.put(new JSONObject().put(ID, CLASSIFICATION_REASON_3_ID_VALUE));
        requestBody.put(CLASSIFICATION_REASONS, classificationReasons);
        response = sendRequest(new PutRequest(url, requestBody.toString(), APPLICATION_JSON), 200);
        responseContent = response.getContentAsString();
        responseAsJson = new JSONObject(responseContent);

        assertNotNull(responseAsJson);
        assertTrue(responseAsJson.getBoolean(RESPONSE_SUCCESS));
        assertEquals(CLASSIFICATION_LEVEL_ID2_VALUE, nodeService.getProperty(record, PROP_CURRENT_CLASSIFICATION));
        assertEquals(CLASSIFIED_BY2, nodeService.getProperty(record, PROP_CLASSIFIED_BY));
        assertEquals(CLASSIFICATION_AGENCY2, nodeService.getProperty(record, PROP_CLASSIFICATION_AGENCY));
        List<String> editedClassificationReasonsList = (List<String>) nodeService.getProperty(record, ClassifiedContentModel.PROP_CLASSIFICATION_REASONS);
        assertNotNull(editedClassificationReasonsList);
        assertEquals(1, editedClassificationReasonsList.size());
        assertTrue(editedClassificationReasonsList.contains(CLASSIFICATION_REASON_3_ID_VALUE));
    }
}
