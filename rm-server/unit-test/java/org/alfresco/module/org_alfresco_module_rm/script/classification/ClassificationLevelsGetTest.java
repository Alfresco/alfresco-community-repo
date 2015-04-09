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

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevel;
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
 * Tests for the get classification levels API.
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public class ClassificationLevelsGetTest extends BaseWebScriptUnitTest
{
    /** Classpath location of ftl template for web script */
    private static final String WEBSCRIPT_TEMPLATE = WEBSCRIPT_ROOT_RM + "classification/classificationlevels.get.json.ftl";

    /** ClassificationLevelsGet webscript instance */
    private @Spy @InjectMocks ClassificationLevelsGet webScript;
    private @Mock ClassificationService mockClassificationService;

    private List<ClassificationLevel> classificationLevels;

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
     * Test the successful retrieval of two classification levels.
     */
    @Test
    public void getClassificationLevels() throws Exception
    {
        // Create test data.
        classificationLevels = Arrays.asList(
                new ClassificationLevel("id1", "labelKey1"),
                new ClassificationLevel("id2", "labelKey2"));

        // setup interactions
        doReturn(classificationLevels).when(mockClassificationService).getClassificationLevels();

        // execute web script
        JSONObject json = executeJSONWebScript(new HashMap<String, String>());
        assertNotNull(json);
        String actualJSONString = json.toString();

        // check the JSON result using Jackson to allow easy equality testing.
        ObjectMapper mapper = new ObjectMapper();
        String expectedJSONString = "{\"data\":{\"items\":[{\"displayLabel\":\"labelKey1\",\"id\":\"id1\"},{\"displayLabel\":\"labelKey2\",\"id\":\"id2\"}]}}";
        JsonNode expected = mapper.readTree(expectedJSONString);
        assertEquals(expected, mapper.readTree(actualJSONString));
    }
}
