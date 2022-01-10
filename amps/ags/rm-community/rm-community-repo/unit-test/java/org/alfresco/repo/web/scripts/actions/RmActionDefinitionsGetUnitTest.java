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
package org.alfresco.repo.web.scripts.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseWebScriptUnitTest;
import org.alfresco.repo.action.ActionDefinitionImpl;
import org.alfresco.repo.web.scripts.rule.RmActionDefinitionsGet;
import org.alfresco.repo.web.scripts.rule.WhitelistedDMActions;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.extensions.webscripts.AbstractWebScript;


/**
 * Unit test for {@link RmActionDefinitionsGet} that checks if the whitelisted actions for rm are available.
 */
public class RmActionDefinitionsGetUnitTest extends BaseWebScriptUnitTest
{
    @Mock
    private RecordsManagementActionService mockedRecordsManagementActionService;
    @Mock
    private ActionService mockedExtendedActionService;

    @InjectMocks
    private RmActionDefinitionsGet webScript;

    private List<String> whitelistedActions = WhitelistedDMActions.getActionsList();

    @Override
    protected AbstractWebScript getWebScript()
    {
        return webScript;
    }

    @Override
    protected String getWebScriptTemplate()
    {
        return "alfresco/templates/webscripts/org/alfresco/repository/rule/rm-actiondefinitions.get.json.ftl";
    }

    /**
     * Before test
     */
    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
        webScript.setRecordsManagementActionService(mockedRecordsManagementActionService);
        webScript.setExtendedActionService(mockedExtendedActionService);
    }

    /**
     * Given the extendedActionService contains all dm actions
     * When retrieving RM Action Definitions
     * Then the whitelisted dm actions are present in the response
     */
    @Test
    public void getRmActionDefinitionsWithWhitelistedDMActions() throws Exception
    {
        List<ActionDefinition> dmActionDefinitions = new ArrayList<>();

        for (String action : whitelistedActions)
        {
            dmActionDefinitions.add(new ActionDefinitionImpl(action));
        }

        when(mockedRecordsManagementActionService.getRecordsManagementActions()).thenReturn(Collections.emptyList());
        when(mockedExtendedActionService.getActionDefinitions()).thenReturn(dmActionDefinitions);

        String jsonResponse = executeWebScript(Collections.emptyMap());
        assertNotNull(jsonResponse);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, ArrayList<LinkedHashMap<String, Object>>> responseMap = mapper.readValue(jsonResponse, Map.class);
        ArrayList<LinkedHashMap<String, Object>> actionDefinitions = responseMap.get("data");

        assertEquals(WhitelistedDMActions.getActionsList().size(), actionDefinitions.size());

        for (LinkedHashMap<String, Object> actionDefinition : actionDefinitions)
        {
            assertTrue(whitelistedActions.contains(actionDefinition.get("name")));
        }
    }

    /**
     * Given the extendedActionService only contains non whitelisted actions
     * When retrieving RM Action Definitions
     * Then the response does not contain the non whitelisted actions
     */
    @Test
    public void getRmActionDefinitionsWithNonWhitelistedDMActions() throws Exception
    {
        String dmAction = "notWhitelisted";
        List<ActionDefinition> dmActionDefinitions = Arrays.asList(new ActionDefinitionImpl(dmAction));
        when(mockedRecordsManagementActionService.getRecordsManagementActions()).thenReturn(Collections.emptyList());
        when(mockedExtendedActionService.getActionDefinitions()).thenReturn(dmActionDefinitions);

        String jsonResponse = executeWebScript(Collections.emptyMap());
        assertNotNull(jsonResponse);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, ArrayList<LinkedHashMap<String, Object>>> responseMap = mapper.readValue(jsonResponse, Map.class);
        ArrayList<LinkedHashMap<String, Object>> actionDefinitions = responseMap.get("data");

        // Action definitions should only contain whitelisted DM actions and rm actions
        assertFalse(actionDefinitions.contains(dmAction));
    }
}
