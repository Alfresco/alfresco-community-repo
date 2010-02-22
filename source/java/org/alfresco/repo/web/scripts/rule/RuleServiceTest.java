/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.rule;

import java.text.MessageFormat;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Unit test to test rules Web Script API
 * 
 * @author unknown
 *
 */
public class RuleServiceTest extends BaseWebScriptTest
{

    private static final String URL_RULETYPES = "/api/ruletypes";
    private static final String URL_ACTIONDEFINITIONS = "/api/actiondefinitions";
    private static final String URL_ACTIONCONDITIONDEFINITIONS = "/api/actionconditiondefinitions";
    
    private static final String URL_ACTIONCONSTRAINTS = "/api/actionConstraints";
    private static final String URL_ACTIONCONSTRAINT = "/api/actionConstraints/{0}";
    
    private static final String URL_QUEUE_ACTION = "/api/actionQueue?async={0}";
    
    private static final String URL_RULES = "/api/node/{0}/{1}/{2}/ruleset/rules";
    private static final String URL_RULESET = "/api/node/{0}/{1}/{2}/ruleset";
    private static final String URL_RULE = "/api/node/{0}/{1}/{2}/ruleset/rules/{3}";
    
    private static final String TEST_FOLDER = "test_folder-" + System.currentTimeMillis();
    
    private static final String COMPANY_HOME_PATH = "/app:company_home";
    
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private NamespaceService namespaceService;
    private SearchService searchService;    
    private AuthenticationComponent authenticationComponent;
    private RuleService ruleService;
    
    private NodeRef testNodeRef;
    private NodeRef companyHome;
    
    @Override
    protected void setUp() throws Exception
    {     
        super.setUp();
        this.nodeService = (NodeService)getServer().getApplicationContext().getBean("NodeService");
        this.fileFolderService = (FileFolderService)getServer().getApplicationContext().getBean("FileFolderService");
        this.namespaceService = (NamespaceService)getServer().getApplicationContext().getBean("NamespaceService");
        this.searchService = (SearchService)getServer().getApplicationContext().getBean("SearchService");
        this.ruleService = (RuleService)getServer().getApplicationContext().getBean("RuleService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        createTestFolder();
        
        assertNotNull(testNodeRef);
        assertNotNull(companyHome);
    }
    
    private void createTestFolder()
    {
        NodeRef storeRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        
        NodeRef companyHomeNodeRef;

        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, COMPANY_HOME_PATH, null, namespaceService, false);
        
        if (nodeRefs.size() > 1)
        {
            throw new RuntimeException("Multiple possible roots for : \n" + "   root path: " + COMPANY_HOME_PATH + "\n" + "   results: " + nodeRefs);
        }
        else if (nodeRefs.size() == 0)
        {
            throw new RuntimeException("No root found for : \n" + "   root path: " + COMPANY_HOME_PATH);
        }
        else
        {
            companyHomeNodeRef = nodeRefs.get(0);
            companyHome = companyHomeNodeRef;
        }
        FileInfo fileInfo = fileFolderService.create(companyHomeNodeRef, TEST_FOLDER, ContentModel.TYPE_FOLDER);
        
        testNodeRef = fileInfo.getNodeRef();
        
        this.nodeService.addAspect(testNodeRef, RuleModel.ASPECT_RULES, null);
    } 
    
    private String formatRulesUrl(NodeRef nodeRef)
    {
        return MessageFormat.format(URL_RULES, nodeRef.getStoreRef().getProtocol(), nodeRef.getStoreRef().getIdentifier(), nodeRef.getId());
    }
    
    private String formatRulesetUrl(NodeRef nodeRef)
    {
        return MessageFormat.format(URL_RULESET, nodeRef.getStoreRef().getProtocol(), nodeRef.getStoreRef().getIdentifier(), nodeRef.getId());
    }
    
    private String formateRuleUrl(NodeRef nodeRef, String ruleId)
    {
        return MessageFormat.format(URL_RULE, nodeRef.getStoreRef().getProtocol(), nodeRef.getStoreRef().getIdentifier(), nodeRef.getId(), ruleId);
    }
    
    private String formateActionConstraintUrl(String name)
    {
        return MessageFormat.format(URL_ACTIONCONSTRAINT, name);
    }
    
    private String formateQueueActionUrl(boolean async)
    {
        return MessageFormat.format(URL_QUEUE_ACTION, async);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        fileFolderService.delete(testNodeRef);        
        this.authenticationComponent.clearCurrentSecurityContext();
    }
    
    private JSONObject createRule() throws Exception
    {
        JSONObject jsonRule = buildTestRule();
        
        Response response = sendRequest(new PostRequest(formatRulesUrl(testNodeRef), jsonRule.toString(), "application/json"), 200);
        
        JSONObject result = new JSONObject(response.getContentAsString());        
        
        return result;
    }
    
    private JSONArray getNodeRules() throws Exception
    {
        Response response = sendRequest(new GetRequest(formatRulesUrl(testNodeRef)), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        
        assertNotNull(result);
        
        assertTrue(result.has("data"));
        
        JSONArray data = result.getJSONArray("data");
        
        return data;
    }
    
    private void checkRuleComplete(JSONObject result) throws Exception
    {
        assertNotNull("Response is null.", result);        
        
        // if id present in response -> rule was created
        assertTrue(result.has("id"));
        
        assertEquals(result.getString("title"), "test_rule");
        assertEquals(result.getString("description"), "this is description for test_rule");
        
        JSONArray ruleType = result.getJSONArray("ruleType");
        
        assertEquals(1, ruleType.length());        
        assertEquals("outbound", ruleType.getString(0));
        
        assertFalse(result.getBoolean("applyToChildren"));
        assertFalse(result.getBoolean("executeAsynchronously"));
        assertFalse(result.getBoolean("disabled"));
        assertTrue(result.has("url"));
        
        JSONObject jsonAction = result.getJSONObject("action");
        
        assertTrue(jsonAction.has("id"));
        
        assertEquals(jsonAction.getString("actionDefinitionName"), "composite-action");
        assertEquals(jsonAction.getString("description"), "this is description for composite-action");
        assertEquals(jsonAction.getString("title"), "test_title");
        
        assertTrue(jsonAction.has("parameterValues"));
        
        assertTrue(jsonAction.getBoolean("executeAsync"));        
        
        assertTrue(jsonAction.has("actions"));
        assertTrue(jsonAction.has("conditions"));
        assertTrue(jsonAction.has("compensatingAction"));
        assertTrue(jsonAction.has("url"));
    }
    
    private void checkRuleSummary(JSONObject result) throws Exception
    {
        assertNotNull("Response is null.", result);
        
        assertTrue(result.has("data"));
        
        JSONObject data = result.getJSONObject("data");
        
        // if id present in response -> rule was created
        assertTrue(data.has("id"));
        
        assertEquals(data.getString("title"), "test_rule");
        assertEquals(data.getString("description"), "this is description for test_rule");
        
        JSONArray ruleType = data.getJSONArray("ruleType");
        
        assertEquals(1, ruleType.length());        
        assertEquals("outbound", ruleType.getString(0));
        
        assertFalse(data.getBoolean("disabled"));
        assertTrue(data.has("url"));
        
    }
    
    private void checkUpdatedRule(JSONObject before, JSONObject after) throws JSONException
    {
        // check saving of basic feilds 
        assertEquals("It seams that 'id' is not correct", before.getString("id"), after.getString("id"));
        
        assertEquals("It seams that 'title' was not saved", before.getString("title"), after.getString("title"));
        
        assertEquals("It seams that 'description' was not saved", before.getString("description"), after.getString("description"));
        
        assertEquals("It seams that 'ruleType' was not saved", before.getJSONArray("ruleType").length(), after.getJSONArray("ruleType").length());
        
        assertEquals(before.getBoolean("applyToChildren"), after.getBoolean("applyToChildren"));
        assertEquals(before.getBoolean("executeAsynchronously"), after.getBoolean("executeAsynchronously"));
        assertEquals(before.getBoolean("disabled"), after.getBoolean("disabled"));
        
        // check saving of collections        
        JSONObject afterAction = after.getJSONObject("action");
        
        // we didn't change actions collection
        assertEquals(1, afterAction.getJSONArray("actions").length());
        
        // conditions should be empty (should not present in response), 
        assertFalse(afterAction.has("conditions"));        
        
        assertEquals(before.has("url"), after.has("url"));
    }
    
    private void checkRuleset(JSONObject result, String ruleId) throws Exception
    {
        assertNotNull("Response is null.", result);
        
        assertTrue(result.has("data"));
        
        JSONObject data = result.getJSONObject("data");
        
        assertTrue(data.has("rules"));
        
        assertEquals(1, data.getJSONArray("rules").length());
        
        JSONObject ruleSummary = data.getJSONArray("rules").getJSONObject(0);
        
        assertEquals(ruleId, ruleSummary.getString("id"));
        
        assertTrue(ruleSummary.has("title"));
        assertTrue(ruleSummary.has("ruleType"));
        assertTrue(ruleSummary.has("disabled"));
        assertTrue(ruleSummary.has("url"));
        
        assertFalse(data.has("inheritedRules"));
        
    }
    
    public void testGetRuleTypes() throws Exception
    {
        Response response = sendRequest(new GetRequest(URL_RULETYPES), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        
        assertNotNull(result);
        
        assertTrue(result.has("data"));
        
        JSONArray data = result.getJSONArray("data");
        
        for (int i = 0; i < data.length(); i++)
        {
            JSONObject ruleType = data.getJSONObject(i);
            
            assertTrue(ruleType.has("name"));
            assertTrue(ruleType.has("displayLabel"));
            assertTrue(ruleType.has("url"));
        }
    }
    
    public void testGetActionDefinitions() throws Exception
    {
        Response response = sendRequest(new GetRequest(URL_ACTIONDEFINITIONS), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        
        assertNotNull(result);
        
        assertTrue(result.has("data"));
        
        JSONArray data = result.getJSONArray("data");
        
        for (int i = 0; i < data.length(); i++)
        {
            JSONObject actionDefinition = data.getJSONObject(i);
            
            assertTrue(actionDefinition.has("name"));
            assertTrue(actionDefinition.has("displayLabel"));
            assertTrue(actionDefinition.has("description"));
            assertTrue(actionDefinition.has("adHocPropertiesAllowed"));
            assertTrue(actionDefinition.has("parameterDefinitions"));
            assertTrue(actionDefinition.has("applicableTypes"));            
        }
    }
    
    public void testGetActionConditionDefinitions() throws Exception
    {
        Response response = sendRequest(new GetRequest(URL_ACTIONCONDITIONDEFINITIONS), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        
        assertNotNull(result);
        
        assertTrue(result.has("data"));
        
        JSONArray data = result.getJSONArray("data");
        
        for (int i = 0; i < data.length(); i++)
        {
            JSONObject actionConditionDefinition = data.getJSONObject(i);
            
            assertTrue(actionConditionDefinition.has("name"));
            assertTrue(actionConditionDefinition.has("displayLabel"));
            assertTrue(actionConditionDefinition.has("description"));
            assertTrue(actionConditionDefinition.has("adHocPropertiesAllowed"));
            assertTrue(actionConditionDefinition.has("parameterDefinitions"));
        }
    }
    
    public void off_testGetActionConstraints() throws Exception
    {
        Response response = sendRequest(new GetRequest(URL_ACTIONCONSTRAINTS), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        
        assertNotNull(result);
        
        assertTrue(result.has("data"));
        
        JSONArray data = result.getJSONArray("data");    
        
        for (int i = 0; i < data.length(); i++)
        {
            JSONObject actionConstraint = data.getJSONObject(i);
            
            assertTrue(actionConstraint.has("name"));
            assertTrue(actionConstraint.has("values"));
            
            JSONArray values = actionConstraint.getJSONArray("values");
            
            for (int j = 0; j < values.length(); j++)
            {
                JSONObject value = values.getJSONObject(j);
                
                assertTrue(value.has("value"));
                assertTrue(value.has("displayLabel"));
            }
        }
    }
    
    public void off_testGetActionConstraint() throws Exception
    {
        Response response = sendRequest(new GetRequest(formateActionConstraintUrl("compare-operations")), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        
        assertNotNull(result);
        
        assertTrue(result.has("data"));      
        
        JSONObject data = result.getJSONObject("data");
        
        assertTrue(data.has("name"));
        assertTrue(data.has("values"));
        
        JSONArray values = data.getJSONArray("values");
        
        for (int i = 0; i < values.length(); i++)
        {
            JSONObject value = values.getJSONObject(i);
            
            assertTrue(value.has("value"));
            assertTrue(value.has("displayLabel"));
        }
    }
    
    public void testQueueAction() throws Exception
    {
        String url = formateQueueActionUrl(false);
            
        JSONObject copyAction = buildCopyAction(companyHome);
        
        copyAction.put("actionedUponNode", testNodeRef);
        
        // execute before response (should be successful)
        Response successResponse = sendRequest(new PostRequest(url, copyAction.toString(), "application/json"), 200);
        
        JSONObject successResult = new JSONObject(successResponse.getContentAsString());
        
        assertNotNull(successResult);
        
        assertTrue(successResult.has("data"));
        
        JSONObject successData = successResult.getJSONObject("data");
        
        assertTrue(successData.has("status"));
        assertEquals("success", successData.getString("status"));
        assertTrue(successData.has("actionedUponNode"));
        assertFalse(successData.has("exception"));
        assertTrue(successData.has("action"));
        
        // execute before response (should fail)
        Response failResponse = sendRequest(new PostRequest(url, copyAction.toString(), "application/json"), 200);
        
        JSONObject failResult = new JSONObject(failResponse.getContentAsString());
        
        assertNotNull(failResult);
        
        assertTrue(failResult.has("data"));
        
        JSONObject failData = failResult.getJSONObject("data");
        
        assertTrue(failData.has("status"));
        assertEquals("fail", failData.getString("status"));
        assertTrue(failData.has("actionedUponNode"));
        assertTrue(failData.has("exception"));
        JSONObject exception = failData.getJSONObject("exception");
        assertTrue(exception.has("message"));
        assertTrue(exception.has("stackTrace"));
        assertTrue(failData.has("action"));
        
        // execute after response (should fail but error should not present in response)
        String asyncUrl = formateQueueActionUrl(true);
        Response response = sendRequest(new PostRequest(asyncUrl, copyAction.toString(), "application/json"), 200);
        
        // wait while action executed
        Thread.sleep(1000);
        
        JSONObject result = new JSONObject(response.getContentAsString());
        
        assertNotNull(result);
        
        assertTrue(result.has("data"));
        
        JSONObject data = result.getJSONObject("data");
        
        assertTrue(data.has("status"));
        assertEquals("queued", data.getString("status"));
        assertTrue(data.has("actionedUponNode"));
        assertFalse(data.has("exception"));
        assertTrue(data.has("action"));
    }
    
    public void testCreateRule() throws Exception
    {
        JSONObject result = createRule();       
        
        checkRuleSummary(result);        
        
        List<Rule> rules = ruleService.getRules(testNodeRef);
        
        assertEquals(1, rules.size());
        
    }
        
    public void testGetRulesCollection() throws Exception
    {
        JSONArray data = getNodeRules();
        
        assertEquals(0, data.length());
        
        createRule();
        
        data = getNodeRules();
        
        assertEquals(1, data.length());
    } 
        
    public void testGetRuleset() throws Exception
    {
        JSONObject jsonRule = createRule();
        
        String ruleId = jsonRule.getJSONObject("data").getString("id");
        
        Response response = sendRequest(new GetRequest(formatRulesetUrl(testNodeRef)), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        
        checkRuleset(result, ruleId);
    }
    
    public void testGetRuleDetails() throws Exception    
    {
        JSONObject jsonRule = createRule();
        
        String ruleId = jsonRule.getJSONObject("data").getString("id");
        
        Response response = sendRequest(new GetRequest(formateRuleUrl(testNodeRef, ruleId)), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        
        checkRuleComplete(result);
    }
    
    public void testUpdateRule() throws Exception
    {
        JSONObject jsonRule = createRule();
        
        String ruleId = jsonRule.getJSONObject("data").getString("id");
        
        Response getResponse = sendRequest(new GetRequest(formateRuleUrl(testNodeRef, ruleId)), 200);
        
        JSONObject before = new JSONObject(getResponse.getContentAsString());
        
        // do some changes
        before.put("description", "this is modified description for test_rule");
        
        // do some changes for action object
        JSONObject beforeAction = before.getJSONObject("action");
        // no changes for actions list  
        beforeAction.remove("actions");
        // clear conditions
        beforeAction.put("conditions", new JSONArray());
        
        Response putResponse = sendRequest(new PutRequest(formateRuleUrl(testNodeRef, ruleId), before.toString(), "application/json"), 200);
        
        JSONObject after = new JSONObject(putResponse.getContentAsString());
        
        // sent and retrieved objects should be the same (except ids and urls)
        // this means that all changes was saved
        checkUpdatedRule(before, after);
    }
    
    public void testDeleteRule() throws Exception
    {
        JSONObject jsonRule = createRule();
        
        assertEquals(1, ruleService.getRules(testNodeRef).size());
        
        String ruleId = jsonRule.getJSONObject("data").getString("id");
        
        Response response = sendRequest(new DeleteRequest(formateRuleUrl(testNodeRef, ruleId)), 200);
        JSONObject result = new JSONObject(response.getContentAsString());
        
        assertNotNull(result);
        
        assertTrue(result.has("success"));
        
        boolean success = result.getBoolean("success");
        
        assertTrue(success);
        
        // no more rules present 
        assertEquals(0, ruleService.getRules(testNodeRef).size());
    }
    
    private JSONObject buildCopyAction(NodeRef destination) throws JSONException    
    {
        JSONObject result = new JSONObject();
        
        // add actionDefinitionName
        result.put("actionDefinitionName", "copy");
        
        // build parameterValues
        JSONObject parameterValues = new JSONObject();
        parameterValues.put("destination-folder", destination);
        parameterValues.put("assoc-name", QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "copy"));
        parameterValues.put("assoc-type", ContentModel.ASSOC_CONTAINS);
        
        // add parameterValues
        result.put("parameterValues", parameterValues);
        
        // add executeAsync
        result.put("executeAsync", false);
        
        return result;
    }
    
    private JSONObject buildTestRule() throws JSONException
    {
        JSONObject result = new JSONObject();
        
        result.put("title", "test_rule");
        result.put("description", "this is description for test_rule");
        
        JSONArray ruleType = new JSONArray();
        ruleType.put("outbound");
        
        result.put("ruleType", ruleType);
        
        result.put("applyToChildren", false);
        
        result.put("executeAsynchronously", false);
        
        result.put("disabled", false);
        
        result.put("action", buildTestAction("composite-action", true, true));
        
        return result;
    }
    
    private JSONObject buildTestAction(String actionName, boolean addActions, boolean addCompensatingAction) throws JSONException
    {
        JSONObject result = new JSONObject();
        
        result.put("actionDefinitionName", actionName);
        result.put("description", "this is description for " + actionName);
        result.put("title", "test_title");
        
        JSONObject parameterValues = new JSONObject();
        parameterValues.put("test_name", "test_value");
        
        result.put("parameterValues", parameterValues);
        
        result.put("executeAsync", addActions);
        
        if (addActions)
        {
            JSONArray actions = new JSONArray();
            
            actions.put(buildTestAction("counter", false, false));
            
            result.put("actions", actions);
        }
        
        JSONArray conditions = new JSONArray();
        
        conditions.put(buildTestCondition("no-condition"));
        
        result.put("conditions", conditions);
        
        if (addCompensatingAction)
        {
            result.put("compensatingAction", buildTestAction("executeScript", false, false));
        }
        
        return result;
    }
    
    private JSONObject buildTestCondition(String conditionName) throws JSONException    
    {
        JSONObject result = new JSONObject();
        
        result.put("conditionDefinitionName", conditionName);
        result.put("invertCondition", false);
        
        JSONObject parameterValues = new JSONObject();
        parameterValues.put("test_name", "test_value");
        
        result.put("parameterValues", parameterValues);
        
        return result;
    }
}
