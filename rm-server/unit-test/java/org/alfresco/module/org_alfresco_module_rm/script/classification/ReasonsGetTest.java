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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationReason;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseWebScriptUnitTest;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.extensions.webscripts.DeclarativeWebScript;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests for the get classification reasons API.
 * 
 * @author tpage
 */
public class ReasonsGetTest extends BaseWebScriptUnitTest
{
    /** Classpath location of ftl template for web script */
    private static final String WEBSCRIPT_TEMPLATE = WEBSCRIPT_ROOT_RM + "classification/reasons.get.json.ftl";

    /** ReasonsGet webscript instance */
    private @Spy @InjectMocks ReasonsGet webScript;
    private @Mock ClassificationService mockClassificationService;

    private List<ClassificationReason> reasonsList;

    /** {@inheritDoc} */
    @Override
    protected DeclarativeWebScript getWebScript()
    {
        return webScript;
    }

    /** {@inheritDoc} */
    @Override
    protected String getWebScriptTemplate()
    {
        return WEBSCRIPT_TEMPLATE;
    }

    /**
     * Test the successful retrieval of two classification reasons.
     */
    @Test
    public void getReasons() throws Exception
    {
        // Create test data.
        ClassificationReason reasonA = new ClassificationReason("idA", "labelA");
        ClassificationReason reasonB = new ClassificationReason("idB", "labelB");
        reasonsList = Arrays.asList(reasonA, reasonB);

        // setup interactions
        doReturn(reasonsList).when(mockClassificationService).getClassificationReasons();

        // setup web script parameters
        Map<String, String> parameters = new HashMap<String, String>();

        // execute web script
        JSONObject json = executeJSONWebScript(parameters);
        assertNotNull(json);
        String actualJSONString = json.toString();

        // check the JSON result using Jackson to allow easy equality testing.
        ObjectMapper mapper = new ObjectMapper();
        String expectedJSONString = "{\"data\":{\"items\":[{\"displayLabel\":\"labelA\",\"id\":\"idA\"},{\"displayLabel\":\"labelB\",\"id\":\"idB\"}]}}";
        JsonNode expected = mapper.readTree(expectedJSONString);
        assertEquals(expected, mapper.readTree(actualJSONString));
    }
}
