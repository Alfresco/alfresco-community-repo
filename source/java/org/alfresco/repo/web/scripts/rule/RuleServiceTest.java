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
package org.alfresco.repo.web.scripts.rule;

import java.text.MessageFormat;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.rule.LinkRules;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterConstraint;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
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
 * @author Roy Wetherall
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
    private static final String URL_INHERITED_RULES = "/api/node/{0}/{1}/{2}/ruleset/inheritedrules";
    private static final String URL_RULESET = "/api/node/{0}/{1}/{2}/ruleset";
    private static final String URL_RULE = "/api/node/{0}/{1}/{2}/ruleset/rules/{3}";

    private static final String TEST_STORE_IDENTIFIER = "test_store-" + System.currentTimeMillis();
    private static final String TEST_FOLDER = "test_folder-" + System.currentTimeMillis();
    private static final String TEST_FOLDER_2 = "test_folder-2-" + System.currentTimeMillis();

    private TransactionService transactionService;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private AuthenticationComponent authenticationComponent;
    private RuleService ruleService;
    private ActionService actionService;

    private NodeRef testNodeRef;
    private NodeRef testNodeRef2;
    private NodeRef testWorkNodeRef;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        this.transactionService = (TransactionService) getServer().getApplicationContext().getBean("TransactionService");
        this.nodeService = (NodeService) getServer().getApplicationContext().getBean("NodeService");
        this.fileFolderService = (FileFolderService) getServer().getApplicationContext().getBean("FileFolderService");
        this.ruleService = (RuleService) getServer().getApplicationContext().getBean("RuleService");
        this.actionService = (ActionService) getServer().getApplicationContext().getBean("ActionService");
        this.authenticationComponent = (AuthenticationComponent) getServer().getApplicationContext().getBean("authenticationComponent");

        this.authenticationComponent.setSystemUserAsCurrentUser();

        createTestFolders();

        assertNotNull(testWorkNodeRef);
        assertNotNull(testNodeRef);
        assertNotNull(testNodeRef2);
    }

    private void createTestFolders()
    {
        StoreRef testStore = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, TEST_STORE_IDENTIFIER);

        if (!nodeService.exists(testStore))
        {
            testStore = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, TEST_STORE_IDENTIFIER);
        }

        NodeRef rootNodeRef = nodeService.getRootNode(testStore);

        testWorkNodeRef = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}testnode"), ContentModel.TYPE_FOLDER).getChildRef();

        testNodeRef = fileFolderService.create(testWorkNodeRef, TEST_FOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
        testNodeRef2 = fileFolderService.create(testWorkNodeRef, TEST_FOLDER_2, ContentModel.TYPE_FOLDER).getNodeRef();
    }

    private String formatRulesUrl(NodeRef nodeRef, boolean inherited)
    {
        if (inherited)
        {
            return MessageFormat.format(URL_INHERITED_RULES, nodeRef.getStoreRef().getProtocol(), nodeRef.getStoreRef().getIdentifier(), nodeRef.getId());
        }
        else
        {
            return MessageFormat.format(URL_RULES, nodeRef.getStoreRef().getProtocol(), nodeRef.getStoreRef().getIdentifier(), nodeRef.getId());
        }
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
        RetryingTransactionCallback<Void> deleteCallback = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                nodeService.deleteNode(testNodeRef2);
                nodeService.deleteNode(testNodeRef);
                nodeService.deleteNode(testWorkNodeRef);
                return null;
            }
        };
        this.transactionService.getRetryingTransactionHelper().doInTransaction(deleteCallback);
        this.authenticationComponent.clearCurrentSecurityContext();
    }

    private JSONObject createRule(NodeRef ruleOwnerNodeRef) throws Exception
    {
        return createRule(ruleOwnerNodeRef, "test_rule");
    }
    
    private JSONObject createRule(NodeRef ruleOwnerNodeRef, String title) throws Exception
    {
        JSONObject jsonRule = buildTestRule(title);

        Response response = sendRequest(new PostRequest(formatRulesUrl(ruleOwnerNodeRef, false), jsonRule.toString(), "application/json"), 200);

        JSONObject result = new JSONObject(response.getContentAsString());

        return result;
    }

    private JSONArray getNodeRules(NodeRef nodeRef, boolean inherited) throws Exception
    {
        Response response = sendRequest(new GetRequest(formatRulesUrl(nodeRef, inherited)), 200);
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

        assertTrue(result.getBoolean("applyToChildren"));
        assertFalse(result.getBoolean("executeAsynchronously"));
        assertFalse(result.getBoolean("disabled"));
        assertTrue(result.has("owningNode"));
        JSONObject owningNode = result.getJSONObject("owningNode");
        assertTrue(owningNode.has("nodeRef"));
        assertTrue(owningNode.has("name"));
        assertTrue(result.has("url"));

        JSONObject jsonAction = result.getJSONObject("action");

        assertTrue(jsonAction.has("id"));

        assertEquals(jsonAction.getString("actionDefinitionName"), "composite-action");
        assertEquals(jsonAction.getString("description"), "this is description for composite-action");
        assertEquals(jsonAction.getString("title"), "test_title");

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

    private void checkRuleset(JSONObject result, int rulesCount, String[] ruleIds, int inhRulesCount, String[] parentRuleIds,
                                boolean isLinkedFrom, boolean isLinkedTo) throws Exception
    {
        assertNotNull("Response is null.", result);

        assertTrue(result.has("data"));

        JSONObject data = result.getJSONObject("data");

        if (data.has("rules"))
        {
            JSONArray rulesArray = data.getJSONArray("rules");

            assertEquals(rulesCount, rulesArray.length());

            for (int i = 0; i < rulesArray.length(); i++)
            {
                JSONObject ruleSum = rulesArray.getJSONObject(i);
                assertTrue(ruleSum.has("id"));
                assertEquals(ruleIds[i], ruleSum.getString("id"));
                assertTrue(ruleSum.has("title"));
                assertTrue(ruleSum.has("ruleType"));
                assertTrue(ruleSum.has("disabled"));
                assertTrue(ruleSum.has("owningNode"));
                JSONObject owningNode = ruleSum.getJSONObject("owningNode");
                assertTrue(owningNode.has("nodeRef"));
                assertTrue(owningNode.has("name"));
                assertTrue(ruleSum.has("url"));
            }
        }

        if (data.has("inheritedRules"))
        {
            JSONArray inheritedRulesArray = data.getJSONArray("inheritedRules");

            assertEquals(inhRulesCount, inheritedRulesArray.length());

            for (int i = 0; i < inheritedRulesArray.length(); i++)
            {
                JSONObject ruleSum = inheritedRulesArray.getJSONObject(i);
                assertTrue(ruleSum.has("id"));
                assertEquals(parentRuleIds[i], ruleSum.getString("id"));
                assertTrue(ruleSum.has("title"));
                assertTrue(ruleSum.has("ruleType"));
                assertTrue(ruleSum.has("disabled"));
                assertTrue(ruleSum.has("owningNode"));
                JSONObject owningNode = ruleSum.getJSONObject("owningNode");
                assertTrue(owningNode.has("nodeRef"));
                assertTrue(owningNode.has("name"));
                assertTrue(ruleSum.has("url"));
            }
        }

        assertEquals(isLinkedTo, data.has("linkedToRuleSet"));
        
        assertEquals(isLinkedFrom, data.has("linkedFromRuleSets"));

        assertTrue(data.has("url"));
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
    
    public void testGetActionConstraints() throws Exception
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
    
    public void testGetActionConstraint() throws Exception
    {

        List<ParameterConstraint> constraints = actionService.getParameterConstraints();

        if (constraints.size() == 0)
        {
            return;
        }

        String name = constraints.get(0).getName();

        Response response = sendRequest(new GetRequest(formateActionConstraintUrl(name)), 200);
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

        JSONObject copyAction = buildCopyAction(testWorkNodeRef);

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
        sendRequest(new PostRequest(url, copyAction.toString(), "application/json"), 500);

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
        JSONObject result = createRule(testNodeRef);

        checkRuleSummary(result);

        List<Rule> rules = ruleService.getRules(testNodeRef);

        assertEquals(1, rules.size());

    }

    public void testGetRulesCollection() throws Exception
    {
        JSONArray data = getNodeRules(testNodeRef, false);

        assertEquals(0, data.length());

        createRule(testNodeRef);

        data = getNodeRules(testNodeRef, false);

        assertEquals(1, data.length());

        for (int i = 0; i < data.length(); i++)
        {
            JSONObject ruleSum = data.getJSONObject(i);
            assertTrue(ruleSum.has("id"));
            assertTrue(ruleSum.has("title"));
            assertTrue(ruleSum.has("ruleType"));
            assertTrue(ruleSum.has("disabled"));
            assertTrue(ruleSum.has("owningNode"));
            JSONObject owningNode = ruleSum.getJSONObject("owningNode");
            assertTrue(owningNode.has("nodeRef"));
            assertTrue(owningNode.has("name"));
            assertTrue(ruleSum.has("url"));
        }
    }

    public void testGetInheritedRulesCollection() throws Exception
    {
        JSONArray data = getNodeRules(testNodeRef, true);

        assertEquals(0, data.length());

        createRule(testWorkNodeRef);

        data = getNodeRules(testNodeRef, true);

        assertEquals(1, data.length());

        for (int i = 0; i < data.length(); i++)
        {
            JSONObject ruleSum = data.getJSONObject(i);
            assertTrue(ruleSum.has("id"));
            assertTrue(ruleSum.has("title"));
            assertTrue(ruleSum.has("ruleType"));
            assertTrue(ruleSum.has("disabled"));
            assertTrue(ruleSum.has("owningNode"));
            JSONObject owningNode = ruleSum.getJSONObject("owningNode");
            assertTrue(owningNode.has("nodeRef"));
            assertTrue(owningNode.has("name"));
            assertTrue(ruleSum.has("url"));
        }
    }

    public void testGetRuleset() throws Exception
    {
        JSONObject parentRule = createRule(testWorkNodeRef);
        String[] parentRuleIds = new String[] { parentRule.getJSONObject("data").getString("id") };

        JSONObject jsonRule = createRule(testNodeRef);
        String[] ruleIds = new String[] { jsonRule.getJSONObject("data").getString("id") };

        Action linkRulesAction = actionService.createAction(LinkRules.NAME);
        linkRulesAction.setParameterValue(LinkRules.PARAM_LINK_FROM_NODE, testNodeRef);
        actionService.executeAction(linkRulesAction, testNodeRef2);

        Response linkedFromResponse = sendRequest(new GetRequest(formatRulesetUrl(testNodeRef)), 200);
        JSONObject linkedFromResult = new JSONObject(linkedFromResponse.getContentAsString());
        
        checkRuleset(linkedFromResult, 1, ruleIds, 1, parentRuleIds, true, false);

        Response linkedToResponse = sendRequest(new GetRequest(formatRulesetUrl(testNodeRef2)), 200);
        JSONObject linkedToResult = new JSONObject(linkedToResponse.getContentAsString());
        
        checkRuleset(linkedToResult, 1, ruleIds, 1, parentRuleIds, false, true);
    }

    public void testGetRuleDetails() throws Exception
    {
        JSONObject jsonRule = createRule(testNodeRef);

        String ruleId = jsonRule.getJSONObject("data").getString("id");

        Response response = sendRequest(new GetRequest(formateRuleUrl(testNodeRef, ruleId)), 200);
        JSONObject result = new JSONObject(response.getContentAsString());

        checkRuleComplete(result);
    }

    public void testUpdateRule() throws Exception
    {
        JSONObject jsonRule = createRule(testNodeRef);

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
        JSONObject jsonRule = createRule(testNodeRef);

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
    
    @SuppressWarnings("unused")
    public void testRuleReorder() throws Exception
    {
        assertEquals(0, ruleService.getRules(testNodeRef).size());
        
        // Create 3 rules
        NodeRef rule1 = createRuleNodeRef(testNodeRef, "Rule 1");
        NodeRef rule2 = createRuleNodeRef(testNodeRef, "Rule 2");
        NodeRef rule3 = createRuleNodeRef(testNodeRef, "Rule 3");
        
        List<Rule> rules = ruleService.getRules(testNodeRef);
        assertEquals(3, rules.size());
        assertEquals("Rule 1", rules.get(0).getTitle());
        assertEquals("Rule 2", rules.get(1).getTitle());
        assertEquals("Rule 3", rules.get(2).getTitle());
        
        JSONObject action = new JSONObject();
        action.put("actionDefinitionName", "reorder-rules");
        action.put("actionedUponNode", testNodeRef.toString());
        
        JSONObject params = new JSONObject();
        JSONArray orderArray = new JSONArray();
        orderArray.put(rules.get(2).getNodeRef().toString());
        orderArray.put(rules.get(1).getNodeRef().toString());
        orderArray.put(rules.get(0).getNodeRef().toString());
        params.put("rules", orderArray);
        action.put("parameterValues", params);
        
        String url = formateQueueActionUrl(false);

        // execute before response (should be successful)
        Response successResponse = sendRequest(new PostRequest(url, action.toString(), "application/json"), 200);
        JSONObject successResult = new JSONObject(successResponse.getContentAsString());
        assertNotNull(successResult);
        assertTrue(successResult.has("data"));
        JSONObject successData = successResult.getJSONObject("data");
        assertTrue(successData.has("status"));
        assertEquals("success", successData.getString("status"));
        assertTrue(successData.has("actionedUponNode"));
        assertFalse(successData.has("exception"));
        assertTrue(successData.has("action"));
        
        rules = ruleService.getRules(testNodeRef);
        assertEquals(3, rules.size());
        assertEquals("Rule 3", rules.get(0).getTitle());
        assertEquals("Rule 2", rules.get(1).getTitle());
        assertEquals("Rule 1", rules.get(2).getTitle());
    }
    
    private NodeRef createRuleNodeRef(NodeRef folder, String title) throws Exception
    {
        JSONObject jsonRule = createRule(folder, title);
        String id = jsonRule.getJSONObject("data").getString("id");
        return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
    }

    private JSONObject buildCopyAction(NodeRef destination) throws JSONException
    {
        JSONObject result = new JSONObject();

        // add actionDefinitionName
        result.put("actionDefinitionName", "copy");

        // build parameterValues
        JSONObject parameterValues = new JSONObject();
        parameterValues.put("destination-folder", destination);
        parameterValues.put("assoc-name", "cm:copy");
        parameterValues.put("assoc-type", "cm:contains");

        // add parameterValues
        result.put("parameterValues", parameterValues);

        // add executeAsync
        result.put("executeAsync", false);

        return result;
    }

    private JSONObject buildTestRule(String title) throws JSONException
    {
        JSONObject result = new JSONObject();

        result.put("title", title);
        result.put("description", "this is description for test_rule");

        JSONArray ruleType = new JSONArray();
        ruleType.put("outbound");

        result.put("ruleType", ruleType);

        result.put("applyToChildren", true);

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

        //JSONObject parameterValues = new JSONObject();
        //parameterValues.put("test_name", "test_value");

        //result.put("parameterValues", parameterValues);

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
            result.put("compensatingAction", buildTestAction("script", false, false));
        }

        return result;
    }

    private JSONObject buildTestCondition(String conditionName) throws JSONException
    {
        JSONObject result = new JSONObject();

        result.put("conditionDefinitionName", conditionName);
        result.put("invertCondition", false);

        //JSONObject parameterValues = new JSONObject();
        //parameterValues.put("test_name", "test_value");

        //result.put("parameterValues", parameterValues);

        return result;
    }
}
