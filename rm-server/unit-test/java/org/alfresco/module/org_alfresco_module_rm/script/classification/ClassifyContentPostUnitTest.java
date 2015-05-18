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
package org.alfresco.module.org_alfresco_module_rm.script.classification;

import static com.google.common.collect.Sets.newHashSet;
import static org.alfresco.module.org_alfresco_module_rm.script.classification.ClassifyContentPost.CLASSIFICATION_AUTHORITY;
import static org.alfresco.module.org_alfresco_module_rm.script.classification.ClassifyContentPost.CLASSIFICATION_LEVEL_ID;
import static org.alfresco.module.org_alfresco_module_rm.script.classification.ClassifyContentPost.CLASSIFICATION_REASONS;
import static org.alfresco.util.WebScriptUtils.getStringValueFromJSONObject;
import static org.alfresco.util.WebScriptUtils.putValuetoJSONObject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevelManager;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationReasonManager;
import org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseWebScriptUnitTest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.extensions.webscripts.DeclarativeWebScript;

/**
 * Classify content REST API POST implementation unit test.
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public class ClassifyContentPostUnitTest extends BaseWebScriptUnitTest
{
    /** Classpath location of ftl template for web script */
    private static final String WEBSCRIPT_TEMPLATE = WEBSCRIPT_ROOT_RM + "classification/classifycontent.post.json.ftl";

    /** Constants */
    private static final String STORE_TYPE = "store_type";
    private static final String STORE_ID = "store_id";
    private static final String ID = "id";
    private static final String SUCCESS = "success";
    private static final String LEVEL_ID = "aLevelId";
    private static final String AUTHORITY = "anAuthority";
    private static final String REASON1_ID = "reason1Id";
    private static final String REASON2_ID = "reason2Id";

    /** ClassifyContentPost webscript instance */
    private @Spy @InjectMocks ClassifyContentPost webScript;

    /** Mocked content classification service */
    private @Mock ContentClassificationService mockedContentClassificationService;

    /** Mocked classification level manager */
    private @Mock ClassificationLevelManager mockedClassificationLevelManager;

    /** Mocked classification reason manager */
    private @Mock ClassificationReasonManager mockedClassificationReasonManager;

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseWebScriptUnitTest#getWebScript()
     */
    @Override
    protected DeclarativeWebScript getWebScript()
    {
        return webScript;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseWebScriptUnitTest#getWebScriptTemplate()
     */
    @Override
    protected String getWebScriptTemplate()
    {
        return WEBSCRIPT_TEMPLATE;
    }

    @Test
    public void testClassifyContent() throws Exception
    {
        // Setup web script parameters
        Map<String, String> parameters = buildParameters
        (
                STORE_TYPE,       record.getStoreRef().getProtocol(),
                STORE_ID,         record.getStoreRef().getIdentifier(),
                ID,               record.getId()
        );

        // Build JSON to send to server
        String content = buildContent();

        // Execute web script
        JSONObject json = executeJSONWebScript(parameters, content);
        assertNotNull(json);
        assertTrue(json.has(SUCCESS));
        assertEquals(getStringValueFromJSONObject(json, SUCCESS), Boolean.TRUE.toString());

        // Verify that the classify content method was called
        verify(mockedContentClassificationService, times(1)).classifyContent(LEVEL_ID, AUTHORITY, newHashSet(REASON1_ID, REASON2_ID), record);
    }

    /**
     * Helper method to build the request content
     *
     * @return The request content as {@link String}
     */
    private String buildContent()
    {
        JSONObject content = new JSONObject();
        putValuetoJSONObject(content, CLASSIFICATION_LEVEL_ID, LEVEL_ID);
        putValuetoJSONObject(content, CLASSIFICATION_AUTHORITY, AUTHORITY);

        JSONObject classificationReason1 = new JSONObject();
        putValuetoJSONObject(classificationReason1, ID, REASON1_ID);
        JSONObject classificationReason2 = new JSONObject();
        putValuetoJSONObject(classificationReason2, ID, REASON2_ID);

        JSONArray classificationReasons = new JSONArray();
        classificationReasons.put(classificationReason1);
        classificationReasons.put(classificationReason2);
        putValuetoJSONObject(content, CLASSIFICATION_REASONS, classificationReasons);

        return content.toString();
    }
}
