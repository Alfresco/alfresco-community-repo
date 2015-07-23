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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevel;
import org.alfresco.module.org_alfresco_module_rm.classification.ClearanceLevel;
import org.alfresco.module.org_alfresco_module_rm.classification.SecurityClearanceService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseWebScriptUnitTest;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.extensions.webscripts.DeclarativeWebScript;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;

/**
 * Tests for the get clearance levels API.
 *
 * @author David Webster
 * @since 3.0.a
 */
public class ClearanceLevelsGetUnitTest extends BaseWebScriptUnitTest
{
    /** Classpath location of ftl template for web script */
    private static final String WEBSCRIPT_TEMPLATE = WEBSCRIPT_ROOT_RM + "classification/clearancelevels.get.json.ftl";

    /** ClearanceLevelsGet webscript instance */
    private @Spy @InjectMocks ClearanceLevelsGet webScript;
    private @Mock SecurityClearanceService mockSecurityClearanceService;

    private List<ClearanceLevel> clearanceLevels;

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
     * Test the successful retrieval of two clearance levels.
     */
    @Test
    public void getClearanceLevels() throws Exception
    {
        // Create test data.
        ClassificationLevel classificationLevelOne = new ClassificationLevel("id1", "classificationLabelKey1");
        ClassificationLevel classificationLevelTwo = new ClassificationLevel("id2", "classificationLabelKey2");
        clearanceLevels = Arrays.asList(
                new ClearanceLevel(classificationLevelOne, "labelKey1"),
                new ClearanceLevel(classificationLevelTwo, "labelKey2"));

        // setup interactions
        doReturn(clearanceLevels).when(mockSecurityClearanceService).getClearanceLevels();

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
