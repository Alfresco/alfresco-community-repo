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

package org.alfresco.module.org_alfresco_module_rm.script.hold;

import static org.alfresco.module.org_alfresco_module_rm.test.util.WebScriptExceptionMatcher.fileNotFound;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.WebScriptException;

/**
 * Holds ReST API GET implementation unit test.
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public class HoldsGetUnitTest extends BaseHoldWebScriptUnitTest
{
    /** classpath location of ftl template for web script */
    private static final String WEBSCRIPT_TEMPLATE = WEBSCRIPT_ROOT_RM + "holds.get.json.ftl";
       
    /** HoldsGet webscript instance */
    protected @Spy @InjectMocks HoldsGet webScript;    
    
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
    
    /**
     * Test the outcome of calling the web script with an invalid file plan
     */
    @Test
    public void invalidFilePlan() throws Exception
    {
        // setup web script parameters
        Map<String, String> parameters = buildParameters
        (
                "store_type",       filePlan.getStoreRef().getProtocol(),
                "store_id",         filePlan.getStoreRef().getIdentifier(),
                "id",               "imadethisup"
        );

        // setup expected exception
        exception.expect(WebScriptException.class);
        exception.expect(fileNotFound());
        
        // execute web script
        executeWebScript(parameters);
    }
    
    /**
     * Test the outcome of calling the web script with no file plan specified
     * and with no default file plan created.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void defaultFilePlanDoesNotExist() throws Exception
    {
        // setup expected exception
        exception.expect(WebScriptException.class);
        exception.expect(fileNotFound());
        
        // execute web script
        executeWebScript(Collections.EMPTY_MAP);       
    }
    
    /**
     * Test the successful retrieval of holds defined for a specified file
     * plan.
     */
    @Test
    public void getHoldsForFilePlan() throws Exception
    {
        // setup interactions
        doReturn(holds).when(mockedHoldService).getHolds(filePlan);
        
        // setup web script parameters
        Map<String, String> parameters = buildParameters
        (
                "store_type",       filePlan.getStoreRef().getProtocol(),
                "store_id",         filePlan.getStoreRef().getIdentifier(),
                "id",               filePlan.getId()
        );

        // execute web script
        JSONObject json = executeJSONWebScript(parameters);        
        assertNotNull(json);
        
        // check the JSON result
        testForBothHolds(json);
    }
    
    /**
     * Test the retrieval of holds for the default file plan.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void getHoldsForDefaultFilePlan() throws Exception
    {
        // setup interactions
        doReturn(holds).when(mockedHoldService).getHolds(filePlan);        
        doReturn(filePlan).when(mockedFilePlanService).getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
        
        // execute web script
        JSONObject json = executeJSONWebScript(Collections.EMPTY_MAP);        
        assertNotNull(json);
        
        // check the JSON result
        testForBothHolds(json);        
    }
    
    /**
     * Test the retrieval of holds that hold a specified node.
     */
    @Test
    public void getHoldsThatNodeRefIsHeldBy() throws Exception
    {
        // setup interactions
        doReturn(holds).when(mockedHoldService).heldBy(record, true);
        
        // setup web script parameters
        Map<String, String> parameters = buildParameters
        (
                "store_type",       filePlan.getStoreRef().getProtocol(),
                "store_id",         filePlan.getStoreRef().getIdentifier(),
                "id",               filePlan.getId(),
                "itemNodeRef",      record.toString()
        );
        
        // execute web script
        JSONObject json = executeJSONWebScript(parameters);        
        assertNotNull(json);
        
        // check the JSON result
        testForBothHolds(json);
        
    }
    
    /**
     * Test the retrieval of holds that a node is not held in.
     */
    @Test
    public void getHoldsThatNodeRefIsNotHeldBy() throws Exception
    {
        // setup interactions
        doReturn(holds).when(mockedHoldService).heldBy(record, false);
        
        // setup web script parameters
        Map<String, String> parameters = buildParameters
        (
                "store_type",       filePlan.getStoreRef().getProtocol(),
                "store_id",         filePlan.getStoreRef().getIdentifier(),
                "id",               filePlan.getId(),
                "itemNodeRef",      record.toString(),
                "includedInHold",   "false"
        );
        
        // execute web script
        JSONObject json = executeJSONWebScript(parameters);        
        assertNotNull(json);
        
        // check the JSON result
        testForBothHolds(json);        
    }

    /**
     * Test the retrieval of holds that hold active content.
     */
    @Test
    public void getHoldsThatActiveContentIsHeldBy() throws Exception
    {
        // setup interactions
        doReturn(holds).when(mockedHoldService).heldBy(dmNodeRef, true);

        // setup web script parameters
        Map<String, String> parameters = buildParameters
        (
                "store_type",       filePlan.getStoreRef().getProtocol(),
                "store_id",         filePlan.getStoreRef().getIdentifier(),
                "id",               filePlan.getId(),
                "itemNodeRef",      dmNodeRef.toString()
        );

        // execute web script
        JSONObject json = executeJSONWebScript(parameters);
        assertNotNull(json);

        // check the JSON result
        testForBothHolds(json);

    }

    /**
     * Test the retrieval of holds that do not hold active content.
     */
    @Test
    public void getHoldsThatActiveContentIsNotHeldBy() throws Exception
    {
        // setup interactions
        doReturn(holds).when(mockedHoldService).heldBy(dmNodeRef, false);

        // setup web script parameters
        Map<String, String> parameters = buildParameters
        (
                "store_type",       filePlan.getStoreRef().getProtocol(),
                "store_id",         filePlan.getStoreRef().getIdentifier(),
                "id",               filePlan.getId(),
                "itemNodeRef",      dmNodeRef.toString(),
                "includedInHold",   "false"
        );

        // execute web script
        JSONObject json = executeJSONWebScript(parameters);
        assertNotNull(json);

        // check the JSON result
        testForBothHolds(json);
    }
    
    public void getFileOnlyHolds() throws Exception
    {
        doReturn(AccessStatus.ALLOWED).when(mockedPermissionService).hasPermission(hold1NodeRef, RMPermissionModel.FILING);
        doReturn(AccessStatus.DENIED).when(mockedPermissionService).hasPermission(hold2NodeRef, RMPermissionModel.FILING);
                        
        // setup web script parameters
        Map<String, String> parameters = buildParameters
        (
                "store_type",       filePlan.getStoreRef().getProtocol(),
                "store_id",         filePlan.getStoreRef().getIdentifier(),
                "id",               filePlan.getId(),
                "itemNodeRef",      record.toString(),
                "includedInHold",   "false",
                "fileOnly",         "true"
        );
        
        // execute web script
        JSONObject json = executeJSONWebScript(parameters);        
        assertNotNull(json);
        
        // check the JSON result
        assertTrue(json.has("data"));
        assertTrue(json.getJSONObject("data").has("holds"));
        
        JSONArray jsonHolds = json.getJSONObject("data").getJSONArray("holds");
        assertNotNull(jsonHolds);
        assertEquals(1, jsonHolds.length());
        
        JSONObject hold1 = jsonHolds.getJSONObject(0);
        assertNotNull(hold1);
        assertEquals("hold1", hold1.getString("name"));
        assertEquals(hold1NodeRef.toString(), hold1.getString("nodeRef"));        
    }
    
    /**
     * Helper method to test JSON object for the presence of both test holds.
     * 
     * @param json          json result from web script
     */
    private void testForBothHolds(JSONObject json) throws Exception
    {
        // check the JSON result
        assertTrue(json.has("data"));
        assertTrue(json.getJSONObject("data").has("holds"));
        
        JSONArray jsonHolds = json.getJSONObject("data").getJSONArray("holds");
        assertNotNull(jsonHolds);
        assertEquals(2, jsonHolds.length());
        
        JSONObject hold1 = jsonHolds.getJSONObject(0);
        assertNotNull(hold1);
        assertEquals("hold1", hold1.getString("name"));
        assertEquals(hold1NodeRef.toString(), hold1.getString("nodeRef"));
        
        JSONObject hold2 = jsonHolds.getJSONObject(1);
        assertNotNull(hold2);
        assertEquals("hold2", hold2.getString("name"));
        assertEquals(hold2NodeRef.toString(), hold2.getString("nodeRef"));
    }
}
