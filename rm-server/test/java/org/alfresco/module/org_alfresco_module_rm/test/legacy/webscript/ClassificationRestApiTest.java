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

import static org.alfresco.module.org_alfresco_module_rm.classification.ClassificationSchemeService.Reclassification.DOWNGRADE;
import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.PROP_CLASSIFICATION_AGENCY;
import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.PROP_CLASSIFICATION_REASONS;
import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.PROP_CLASSIFIED_BY;
import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.PROP_CURRENT_CLASSIFICATION;
import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.PROP_DECLASSIFICATION_DATE;
import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.PROP_DECLASSIFICATION_EVENT;
import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.PROP_DECLASSIFICATION_EXEMPTIONS;
import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.PROP_DOWNGRADE_DATE;
import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.PROP_DOWNGRADE_EVENT;
import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.PROP_DOWNGRADE_INSTRUCTIONS;
import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.PROP_LAST_RECLASSIFICATION_ACTION;
import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.PROP_LAST_RECLASSIFY_AT;
import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.PROP_LAST_RECLASSIFY_BY;
import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.PROP_LAST_RECLASSIFY_REASON;
import static org.alfresco.module.org_alfresco_module_rm.script.classification.ClassifyContentBase.CLASSIFICATION_AGENCY;
import static org.alfresco.module.org_alfresco_module_rm.script.classification.ClassifyContentBase.CLASSIFICATION_LEVEL_ID;
import static org.alfresco.module.org_alfresco_module_rm.script.classification.ClassifyContentBase.CLASSIFICATION_REASONS;
import static org.alfresco.module.org_alfresco_module_rm.script.classification.ClassifyContentBase.CLASSIFIED_BY;
import static org.alfresco.module.org_alfresco_module_rm.script.classification.ClassifyContentBase.DECLASSIFICATION_DATE;
import static org.alfresco.module.org_alfresco_module_rm.script.classification.ClassifyContentBase.DECLASSIFICATION_EVENT;
import static org.alfresco.module.org_alfresco_module_rm.script.classification.ClassifyContentBase.DECLASSIFICATION_EXEMPTIONS;
import static org.alfresco.module.org_alfresco_module_rm.script.classification.ClassifyContentBase.DOWNGRADE_DATE;
import static org.alfresco.module.org_alfresco_module_rm.script.classification.ClassifyContentBase.DOWNGRADE_EVENT;
import static org.alfresco.module.org_alfresco_module_rm.script.classification.ClassifyContentBase.DOWNGRADE_INSTRUCTIONS;
import static org.alfresco.module.org_alfresco_module_rm.script.classification.ClassifyContentBase.LAST_RECLASSIFY_BY;
import static org.alfresco.module.org_alfresco_module_rm.script.classification.ClassifyContentBase.LAST_RECLASSIFY_REASON;
import static org.alfresco.util.GUID.generate;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;
import static org.apache.commons.lang3.time.DateUtils.parseDate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.lang3.time.DateFormatUtils;
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
    private static final String DOWNGRADE_DATE_VALUE = "2015-07-08";
    private static final String DOWNGRADE_EVENT_VALUE = generate();
    private static final String DOWNGRADE_INSTRUCTIONS_VALUE = generate();
    private static final String DECLASSIFICATION_DATE_VALUE = "2015-12-01";
    private static final String DECLASSIFICATION_EVENT_VALUE = generate();
    private static final String DECLASSIFICATION_EXEMPTION_1_ID_VALUE = "Test Category 1";
    private static final String DECLASSIFICATION_EXEMPTION_2_ID_VALUE = "Test Category 2";
    private static final String LAST_RECLASSIFY_BY_VALUE = generate();
    private static final String LAST_RECLASSIFY_REASON_VALUE = generate();

    /** Tests classifying a content and editing a classified content */
    @SuppressWarnings("unchecked")
    public void testClassifyEditContent() throws IOException, JSONException, InvalidNodeRefException, ParseException
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
        requestBody.put(DECLASSIFICATION_DATE, DECLASSIFICATION_DATE_VALUE);
        requestBody.put(DECLASSIFICATION_EVENT, DECLASSIFICATION_EVENT_VALUE);
        JSONArray declassificationExemptions = new JSONArray();
        declassificationExemptions.put(new JSONObject().put(ID, DECLASSIFICATION_EXEMPTION_1_ID_VALUE));
        declassificationExemptions.put(new JSONObject().put(ID, DECLASSIFICATION_EXEMPTION_2_ID_VALUE));
        requestBody.put(DECLASSIFICATION_EXEMPTIONS, declassificationExemptions);
        Response response = sendRequest(new PostRequest(url, requestBody.toString(), APPLICATION_JSON), 200);
        String responseContent = response.getContentAsString();
        JSONObject responseAsJson = new JSONObject(responseContent);

        assertNotNull(responseAsJson);
        assertTrue(responseAsJson.getBoolean(RESPONSE_SUCCESS));
        assertEquals(CLASSIFICATION_LEVEL_ID1_VALUE, nodeService.getProperty(record, PROP_CURRENT_CLASSIFICATION));
        assertEquals(CLASSIFIED_BY1, nodeService.getProperty(record, PROP_CLASSIFIED_BY));
        assertEquals(CLASSIFICATION_AGENCY1, nodeService.getProperty(record, PROP_CLASSIFICATION_AGENCY));
        List<String> classificationReasonsList = (List<String>) nodeService.getProperty(record, PROP_CLASSIFICATION_REASONS);
        assertNotNull(classificationReasonsList);
        assertEquals(2, classificationReasonsList.size());
        assertTrue(classificationReasonsList.contains(CLASSIFICATION_REASON_1_ID_VALUE));
        assertTrue(classificationReasonsList.contains(CLASSIFICATION_REASON_2_ID_VALUE));
        assertEquals(parseDate(DECLASSIFICATION_DATE_VALUE, ISO_DATE_FORMAT.getPattern()), nodeService.getProperty(record, PROP_DECLASSIFICATION_DATE));
        assertEquals(DECLASSIFICATION_EVENT_VALUE, nodeService.getProperty(record, PROP_DECLASSIFICATION_EVENT));
        List<String> declassificationExemptionsList = (List<String>) nodeService.getProperty(record, PROP_DECLASSIFICATION_EXEMPTIONS);
        assertNotNull(declassificationExemptionsList);
        assertEquals(2, declassificationExemptionsList.size());
        assertTrue(declassificationExemptionsList.contains(DECLASSIFICATION_EXEMPTION_1_ID_VALUE));
        assertTrue(declassificationExemptionsList.contains(DECLASSIFICATION_EXEMPTION_2_ID_VALUE));

        // Edit classified content
        requestBody = new JSONObject();
        requestBody.put(CLASSIFICATION_LEVEL_ID, CLASSIFICATION_LEVEL_ID2_VALUE);
        requestBody.put(CLASSIFIED_BY, CLASSIFIED_BY2);
        requestBody.put(CLASSIFICATION_AGENCY, CLASSIFICATION_AGENCY2);
        classificationReasons = new JSONArray();
        classificationReasons.put(new JSONObject().put(ID, CLASSIFICATION_REASON_3_ID_VALUE));
        requestBody.put(CLASSIFICATION_REASONS, classificationReasons);
        requestBody.put(LAST_RECLASSIFY_BY, LAST_RECLASSIFY_BY_VALUE);
        requestBody.put(LAST_RECLASSIFY_REASON, LAST_RECLASSIFY_REASON_VALUE);
        response = sendRequest(new PutRequest(url, requestBody.toString(), APPLICATION_JSON), 200);
        responseContent = response.getContentAsString();
        responseAsJson = new JSONObject(responseContent);

        assertNotNull(responseAsJson);
        assertTrue(responseAsJson.getBoolean(RESPONSE_SUCCESS));
        assertEquals(CLASSIFICATION_LEVEL_ID2_VALUE, nodeService.getProperty(record, PROP_CURRENT_CLASSIFICATION));
        assertEquals(CLASSIFIED_BY2, nodeService.getProperty(record, PROP_CLASSIFIED_BY));
        assertEquals(CLASSIFICATION_AGENCY2, nodeService.getProperty(record, PROP_CLASSIFICATION_AGENCY));
        List<String> editedClassificationReasonsList = (List<String>) nodeService.getProperty(record, PROP_CLASSIFICATION_REASONS);
        assertNotNull(editedClassificationReasonsList);
        assertEquals(1, editedClassificationReasonsList.size());
        assertTrue(editedClassificationReasonsList.contains(CLASSIFICATION_REASON_3_ID_VALUE));
        assertEquals(DOWNGRADE.toModelString(), nodeService.getProperty(record, PROP_LAST_RECLASSIFICATION_ACTION));
        String date1 = DateFormatUtils.format(new Date(), ISO_DATE_FORMAT.getPattern());
        String date2 = DateFormatUtils.format((Date) nodeService.getProperty(record, PROP_LAST_RECLASSIFY_AT), ISO_DATE_FORMAT.getPattern());
        assertEquals(date1, date2);
        assertEquals(LAST_RECLASSIFY_BY_VALUE, nodeService.getProperty(record, PROP_LAST_RECLASSIFY_BY));
        assertEquals(LAST_RECLASSIFY_REASON_VALUE, nodeService.getProperty(record, PROP_LAST_RECLASSIFY_REASON));
    }

    /** Test downgrade schedule */
    public void testDowngradeSchedule() throws JSONException, UnsupportedEncodingException, IOException, InvalidNodeRefException, ParseException
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

        // Set just a downgrade date
        requestBody.put(DOWNGRADE_DATE, DOWNGRADE_DATE_VALUE);
        // An exception is expected
        sendRequest(new PostRequest(url, requestBody.toString(), APPLICATION_JSON), 500);

        // Set just a downgrade event
        requestBody.remove(DOWNGRADE_DATE);
        requestBody.put(DOWNGRADE_EVENT, DOWNGRADE_EVENT_VALUE);
        // An exception is expected
        sendRequest(new PostRequest(url, requestBody.toString(), APPLICATION_JSON), 500);

        // Set a downgrade date and downgrade event
        requestBody.put(DOWNGRADE_DATE, DOWNGRADE_DATE_VALUE);
        // An exception is expected
        sendRequest(new PostRequest(url, requestBody.toString(), APPLICATION_JSON), 500);

        // Set a downgrade instructions
        requestBody.put(DOWNGRADE_INSTRUCTIONS, DOWNGRADE_INSTRUCTIONS_VALUE);
        Response response = sendRequest(new PostRequest(url, requestBody.toString(), APPLICATION_JSON), 200);

        String responseContent = response.getContentAsString();
        JSONObject responseAsJson = new JSONObject(responseContent);

        assertNotNull(responseAsJson);
        assertTrue(responseAsJson.getBoolean(RESPONSE_SUCCESS));
        assertEquals(parseDate(DOWNGRADE_DATE_VALUE, ISO_DATE_FORMAT.getPattern()), nodeService.getProperty(record, PROP_DOWNGRADE_DATE));
        assertEquals(DOWNGRADE_EVENT_VALUE, nodeService.getProperty(record, PROP_DOWNGRADE_EVENT));
        assertEquals(DOWNGRADE_INSTRUCTIONS_VALUE, nodeService.getProperty(record, PROP_DOWNGRADE_INSTRUCTIONS));
    }
}
