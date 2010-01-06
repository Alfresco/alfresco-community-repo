/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.rule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluator;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.executer.CompositeActionExecuter;
import org.alfresco.repo.action.executer.CopyActionExecuter;
import org.alfresco.repo.action.executer.MoveActionExecuter;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Test Case class with unit tests for Rule REST API
 * 
 * @author Glen Johnson at Alfresco dot com
 */
public class RuleServiceTest extends BaseWebScriptTest
{
    // member variables for service instances
    private AuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private NodeService nodeService;
    private PersonService personService;
    
    private NodeRef owningNodeRef1;
    private NodeRef testDestFolder1;
    private NodeRef testDestFolder2;
    
    // private constants
    private static final String RULES_USER = "Rules.User";
    private static final String RULES_USER_PASSWORD = "password";
    private static final String RULES_TEST_OWNING_FOLDER_1 = "rulesTestOwningFolder1";
    private static final String RULES_TEST_DEST_FOLDER_1 = "rulesTestDestinationFolder1";
    private static final String RULES_TEST_DEST_FOLDER_2 = "rulesTestDestinationFolder2";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        // get references to services
        this.authenticationService = (AuthenticationService) getServer().getApplicationContext().getBean(
                "AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent) getServer().getApplicationContext().getBean(
                "AuthenticationComponent");
        this.nodeService = (NodeService) getServer().getApplicationContext().getBean("NodeService");
        this.personService = (PersonService) getServer().getApplicationContext().getBean("PersonService");
        
        //
        // various setup operations which need to be run as system user
        //
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                createUserAndAssocPerson(RULES_USER);
                
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
        
        // Do tests as inviter user
        this.authenticationComponent.setCurrentUser(RULES_USER);
        
        // create a folder (as the rule owning node) under the current user's home space 
        NodeRef personRef = this.personService.getPerson(RULES_USER);
        NodeRef userHomeRef = (NodeRef)nodeService.getProperty(personRef, ContentModel.PROP_HOMEFOLDER);
        this.owningNodeRef1 = this.nodeService.createNode(userHomeRef, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, RULES_TEST_OWNING_FOLDER_1),
                ContentModel.TYPE_FOLDER).getChildRef();
        
        // create other folders for testing purposes
        this.testDestFolder1 = this.nodeService.createNode(userHomeRef, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, RULES_TEST_DEST_FOLDER_1),
                ContentModel.TYPE_FOLDER).getChildRef();
        this.testDestFolder2 = this.nodeService.createNode(userHomeRef, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, RULES_TEST_DEST_FOLDER_2),
                ContentModel.TYPE_FOLDER).getChildRef();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();

        // delete the actionable node created in setUp()
        // TODO uncomment this when I am no longer comparing
        // insync with JSF Web Client
        //
        // this.nodeService.deleteNode(this.actionableNodeRef);
        
        //
        // run various teardown operations which need to be run as 'admin'
        //
        // TODO uncomment this when I am no longer comparing
        // insync with JSF Web Client
        //
        // RunAsWork<Object> runAsWork = new RunAsWork<Object>()
        // {
        //    public Object doWork() throws Exception
        //    {
        //        deleteUserAndAssocPerson(RULES_USER);
        //        
        //        return null;
        //    }
        // ;
        // AuthenticationUtil.runAs(runAsWork, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * Get a URL to the Rule Collection associated with the given rule owning
     * node
     * 
     * @param ruleOwningNodeRef
     *            the rule owning node reference
     * @return the URL to the rule collection associated with the given
     *         rule owning node
     */
    private String getRulesNodeBasedUrl(NodeRef ruleOwningNodeRef,
            Boolean includeInherited, String ruleTypeName)
    {
        boolean includeInheritedParamGiven = includeInherited != null;
        boolean ruleTypeNameParamGiven = (ruleTypeName != null) && (ruleTypeName.length() > 0);
        boolean parameterGiven = (includeInheritedParamGiven || ruleTypeNameParamGiven);

        String url = "/api/node/"
                + ruleOwningNodeRef.getStoreRef().getProtocol() + "/"
                + ruleOwningNodeRef.getStoreRef().getIdentifier() + "/"
                + ruleOwningNodeRef.getId() + "/rules";
        
        if (parameterGiven)
        {
            url += "?";
        }

        if (includeInheritedParamGiven)
        {
            url += "includeInherited=" + includeInherited.toString();
            
            if (ruleTypeNameParamGiven)
            {
                url += "&";
            }
        }

        if (ruleTypeNameParamGiven)
        {
            url += "ruleTypeName=" + ruleTypeName;
        }

        return url;
    }
    
    /**
     * Get a URL to the Rule Collection associated with the given rule owning
     * node (without adding any parameters)
     * 
     * @param ruleOwningNodeRef
     *            the rule owning node reference
     * @return the URL to the rule collection associated with the given
     *         rule owning node
     */
    private String getRulesNodeBasedUrl(NodeRef ruleOwningNodeRef)
    {
        return getRulesNodeBasedUrl(ruleOwningNodeRef, null, null);
    }
    
    private void createUserAndAssocPerson(String userName)
    {
        // if user with given user name doesn't already exist then create user
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            // create user
            this.authenticationService.createAuthentication(userName,
                    RULES_USER_PASSWORD.toCharArray());
        }

        // if person node with given user name doesn't already exist then create
        // person
        if (this.personService.personExists(userName) == false)
        {
            // create person properties
            PropertyMap personProps = new PropertyMap();
            personProps.put(ContentModel.PROP_USERNAME, userName);
            personProps.put(ContentModel.PROP_FIRSTNAME, "FirstName123");
            personProps.put(ContentModel.PROP_LASTNAME, "LastName123");
            personProps.put(ContentModel.PROP_EMAIL, "Test.RulesUser@alfresco.com");
            personProps.put(ContentModel.PROP_JOBTITLE, "JobTitle123");
            personProps.put(ContentModel.PROP_ORGANIZATION, "Organisation123");

            // create person node for user
            this.personService.createPerson(personProps);
        }
    }

    /**
     * Create and return a Condition JSON object
     * 
     * @param id ID of the condition
     * @param parameterValues An associative array (map) of parameter values for the condition.
     *          Pass in <code>null</code> if there are no parameter values associated with this condition.
     * @param conditionDefName The definition name for this condition.
     * @param invertCondition Indicates whether the condition result should be inverted/negated.
     * @param conditionUrl URL to the condition resource
     * 
     * @return the constructed condition
     */
    private JSONObject getConditionJsonObject(String id, JSONObject parameterValues,
            String conditionDefName, boolean invertCondition, String conditionUrl)
        throws Exception
    {
        // Construct condition JSON object
        JSONObject condition = new JSONObject();
        
        condition.put("id", id);
        condition.put("parameterValues", parameterValues);
        condition.put("conditionDefinitionName", conditionDefName);
        condition.put("invertCondition", invertCondition);
        condition.put("url", conditionUrl);
        
        return condition;
    }

    /**
     * Create and return an Action JSON object
     * 
     * @param id ID of the action
     * @param actionDefName The name of the action definition that relates to this action.
     * @param title The title of the action.
     * @param description The description of the action.
     * @param executeAsync Indicates whether to run the action asynchronously or not.
     * @param parameterValues Associative array (map) of parameter values associated with this action.
     *          Pass in <code>null</code> if there are no parameter values associated with the action.
     * @param conditions Associative array of Condition Details for the condition resources associated with this action.
     *          Pass in <code>null</code> if there are no conditions associated with the action.
     * @param actions Associative array of Action Details for the action resources associated with this action.
     *          Pass in <code>null</code> if this action is not a composite action (and thus no actions
     *          are associated with it).
     * @param compensatingAction Action Details for the compensating action.
     *          Pass in <code>null</code> if this action does not have a compensating action
     * @param  actionUrl URL to the action resource
     * 
     * @return the constructed action
     */
    private JSONObject getActionJsonObject (String id, String actionDefName, String title, String description,
            boolean executeAsync,  JSONObject parameterValues, JSONArray conditions,
            JSONArray actions, JSONObject compensatingAction, String actionUrl)
        throws Exception
    {
        // Construct action JSON object
        JSONObject action = new JSONObject();
        
        action.put("id", id);
        action.put("actionDefinitionName", actionDefName);
        action.put("title", title);
        action.put("description", description);
        action.put("executeAsync", executeAsync);
        action.put("parameterValues", parameterValues);
        action.put("conditions", conditions);
        action.put("actions", actions);
        action.put("compensatingAction", compensatingAction);
        action.put("url", actionUrl);
        
        return action;
    }
    
    /**
     * Create and return a Rule JSON object
     * 
     * @param owningNodeRef The owning (actionable) node reference to which this rule applies
     * @param ruleNodeRef The node that uniquely represents this rule
     * @param title The title of the rule
     * @param description The description of the rule
     * @param ruleTypes The rule types associated with this rule
     * @param action The action associated with this rule
     * @param executeAsync Indicates whether the rule should execute the action asynchronously or not
     * @param ruleDisabled Indicates whether or not this rule is marked as disabled or not
     * @param appliedToChildren Indicates whether the rule is applied to all the children of the associated actionable node
     * @param ruleUrl URL to the rule resource
     * 
     * @return the constructed rule
     */
    private JSONObject getRuleJsonObject (NodeRef owningNodeRef, NodeRef ruleNodeRef, String title,
            String description, List<String> ruleTypes, JSONObject action, boolean executeAsync,
            boolean ruleDisabled, boolean appliedToChildren, String ruleUrl)
        throws Exception
    {
        // Construct rule JSON object
        JSONObject rule = new JSONObject();
        
        if (owningNodeRef != null)
        {
            rule.put("owningNodeRef", owningNodeRef.toString());
        }
        if (ruleNodeRef != null)
        {
            rule.put("ruleNodeRef", ruleNodeRef.toString());
        }
        rule.put("title", title);
        rule.put("description", description);
        rule.put("ruleTypes", new JSONArray(ruleTypes));
        rule.put("action", action);
        rule.put("executeAsync", executeAsync);
        rule.put("ruleDisabled", ruleDisabled);
        rule.put("appliedToChildren", appliedToChildren);
        rule.put("url", ruleUrl);
        
        return rule;
    }
    
    /**
     * Creates a rule in the rule collection associated with the given rule owning node
     * 
     * @param ruleOwningNodeRef the rule owning node with which the rule collection is associated
     * @param ruleJson The rule JSON object to POST to the collection
     * 
     * @return the created rule returned as a JSON object 
     * @throws Exception
     */
    private JSONObject postRules(NodeRef ruleOwningNodeRef, JSONObject ruleJson) throws Exception
    {
        // Construct rule collection URL for POST rules
        String rulesURL = getRulesNodeBasedUrl(ruleOwningNodeRef);
        
        Response response = sendRequest(new PostRequest(rulesURL, ruleJson.toString(), "application/json"),
                Status.STATUS_OK); 
        JSONObject result = new JSONObject(response.getContentAsString());
        
        return result;
    }
    
    /**
     * Check that getting the rule collection resource associated with the given actionable node
     * returns with the expected HTTP status code
     *  
     * @param ruleOwningNodeRef the actionable node for which we want to retrieve rules associated with
     *          it i.e. the rules applied to it 
     * @param expectedStatus the HTTP status that we expect to be returned by this operation
     * @return the rules collection returned as an array of Rule Details encapsulated in a JSON object
     * @throws Exception
     */
    private JSONArray getRules(NodeRef ruleOwningNodeRef, Boolean includeInherited, String ruleTypeName, int expectedStatus)
            throws Exception
    {
        // Construct rule collection URL for GET rules
        String rulesUrl = getRulesNodeBasedUrl(ruleOwningNodeRef, includeInherited, ruleTypeName);

        Response response = sendRequest(new GetRequest(rulesUrl), expectedStatus);

        JSONArray result = new JSONArray(response.getContentAsString());

        return result;
    }

    public void testDeleteRules() throws Exception
    {
        //
        // First POST a Rule to the Rule Collection associated with rule owning NodeRef - owningNodeRef1
        //
        
        // create condition parameters
        JSONObject condCompMimeTypeParams = new JSONObject();
        condCompMimeTypeParams.put(ComparePropertyValueEvaluator.PARAM_VALUE, "image/png");
        
        // create conditions
        JSONObject conditionCompMimeType = getConditionJsonObject(null, condCompMimeTypeParams, "compare-mime-type", false, null);
        JSONArray conditions = new JSONArray();
        conditions.put(conditionCompMimeType);
        
        // create nested action parameters
        JSONObject actionCopyParamsJson = new JSONObject();
        actionCopyParamsJson.put(MoveActionExecuter.PARAM_ASSOC_QNAME,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "copy").toString());
        actionCopyParamsJson.put(MoveActionExecuter.PARAM_DESTINATION_FOLDER, this.testDestFolder1.toString());
        actionCopyParamsJson.put(MoveActionExecuter.PARAM_ASSOC_TYPE_QNAME, ContentModel.ASSOC_CONTAINS.toString());
        
        // create nested actions
        JSONObject actionCopyJson = getActionJsonObject(null, CopyActionExecuter.NAME, "CopyTitle", "CopyDesc", false,
                actionCopyParamsJson, null, null, null, null);
        JSONArray nestedActions = new JSONArray();
        nestedActions.put(actionCopyJson);
        
        // create rule's composite action
        JSONObject compoActionJson = getActionJsonObject(null, CompositeActionExecuter.NAME, "Rule1Action", "Rule1ActionDesc",
                false, null, conditions, nestedActions, null, null);
        
        // create rule to POST
        List<String> ruleTypes = new ArrayList<String>();
        ruleTypes.add(RuleType.UPDATE);
        JSONObject ruleJson = getRuleJsonObject(this.owningNodeRef1, null, "Rule1", "Rule1Desc", ruleTypes,
                compoActionJson, false, false, false, null);
        
        // POST rule JSON to rules collection resource associated with rule owning node ref owningNodeRef1
        postRules(this.owningNodeRef1, ruleJson);
        
        //
        // DELETE all Rules owned by NodeRef - owningNodeRef1 
        // and validate that owningNodeRef1 does not have any
        // rules left applied against it thereafter
        //
        
        // construct url to the Rule Collection resource that we wish to delete (associated with rule owning node 
        // NodeRef owningNodeRef1)
        String url = "/api/node/" + this.owningNodeRef1.getStoreRef().getProtocol() + "/"
        +  this.owningNodeRef1.getStoreRef().getIdentifier() + "/" + this.owningNodeRef1.getId() + "/rules";
        
        // DELETE all rules owned by NodeRef owningNodeRef1
        sendRequest(new DeleteRequest(url), Status.STATUS_OK);
        
        // make sure that there are no rules associated with
        // rule owning NodeRef owningNodeRef1 anymore
        Response responseGetRules = sendRequest(new GetRequest(url), Status.STATUS_OK);
        JSONArray getRulesJson = new JSONArray(responseGetRules.getContentAsString());
        
        assertTrue(getRulesJson.length() == 0);
    }
    
    public void testDeleteRule() throws Exception
    {
        //
        // First POST a Rule and get the identifying rule node ref
        // from the Rule Details returned
        //
        
        // create condition parameters
        JSONObject condCompMimeTypeParams = new JSONObject();
        condCompMimeTypeParams.put(ComparePropertyValueEvaluator.PARAM_VALUE, "image/png");
        
        // create conditions
        JSONObject conditionCompMimeType = getConditionJsonObject(null, condCompMimeTypeParams, "compare-mime-type", false, null);
        JSONArray conditions = new JSONArray();
        conditions.put(conditionCompMimeType);
        
        // create nested action parameters
        JSONObject actionCopyParamsJson = new JSONObject();
        actionCopyParamsJson.put(MoveActionExecuter.PARAM_ASSOC_QNAME,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "copy").toString());
        actionCopyParamsJson.put(MoveActionExecuter.PARAM_DESTINATION_FOLDER, this.testDestFolder1.toString());
        actionCopyParamsJson.put(MoveActionExecuter.PARAM_ASSOC_TYPE_QNAME, ContentModel.ASSOC_CONTAINS.toString());
        
        // create nested action
        JSONObject actionCopyJson = getActionJsonObject(null, CopyActionExecuter.NAME, "CopyTitle", "CopyDesc", false,
                actionCopyParamsJson, null, null, null, null);
        JSONArray nestedActions = new JSONArray();
        nestedActions.put(actionCopyJson);
        
        // create rule's composite action
        JSONObject compoActionJson = getActionJsonObject(null, CompositeActionExecuter.NAME, "Rule1Action", "Rule1ActionDesc",
                false, null, conditions, nestedActions, null, null);
        
        // create rule to POST
        List<String> ruleTypes = new ArrayList<String>();
        ruleTypes.add(RuleType.UPDATE);
        JSONObject ruleJson = getRuleJsonObject(this.owningNodeRef1, null, "Rule1", "Rule1Desc", ruleTypes,
                compoActionJson, false, false, false, null);
        
        // POST rule JSON to rules collection resource
        JSONObject resultPostRule = postRules(this.owningNodeRef1, ruleJson);
        
        String ruleNodeRefStr = resultPostRule.getString("ruleNodeRef");
        NodeRef ruleNodeRef = new NodeRef(ruleNodeRefStr);
        
        //
        // DELETE Rule with rule node ref of Rule just POSTed
        // and validate that the Rule has been deleted
        //
        
        // construct url to Rule resource we wish to delete
        String url = "/api/rules/" + ruleNodeRef.getStoreRef().getProtocol() + "/"
        +  ruleNodeRef.getStoreRef().getIdentifier() + "/" + ruleNodeRef.getId();

        // delete the rule just POSTed
        sendRequest(new DeleteRequest(url), Status.STATUS_OK);
        
        // make sure that the rule just deleted no longer exists
        sendRequest(new GetRequest(url), Status.STATUS_NOT_FOUND);
    }

    @SuppressWarnings("unchecked")
    public void testGetRule() throws Exception
    {
        //
        // First POST a Rule and get the identifying rule node ref
        // from the Rule Details returned
        //
        
        // create condition parameters
        JSONObject condCompMimeTypeParams = new JSONObject();
        condCompMimeTypeParams.put(ComparePropertyValueEvaluator.PARAM_VALUE, "image/png");
        
        // create conditions JSON array
        JSONObject conditionCompMimeType = getConditionJsonObject(null, condCompMimeTypeParams, "compare-mime-type", false, null);
        JSONArray conditions = new JSONArray();
        conditions.put(conditionCompMimeType);
        
        // create parameters for nested actions
        JSONObject actionCopyParamsJson = new JSONObject();
        actionCopyParamsJson.put(MoveActionExecuter.PARAM_ASSOC_QNAME,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "copy").toString());
        actionCopyParamsJson.put(MoveActionExecuter.PARAM_DESTINATION_FOLDER, this.testDestFolder1.toString());
        actionCopyParamsJson.put(MoveActionExecuter.PARAM_ASSOC_TYPE_QNAME, ContentModel.ASSOC_CONTAINS.toString());
        
        // create nested actions
        JSONObject actionCopyJson = getActionJsonObject(null, CopyActionExecuter.NAME, "CopyTitle", "CopyDesc", false,
                actionCopyParamsJson, null, null, null, null);
        JSONArray nestedActions = new JSONArray();
        nestedActions.put(actionCopyJson);
        
        // create rule's root action
        JSONObject ruleActionJson = getActionJsonObject(null, CompositeActionExecuter.NAME, "Rule1Action", "Rule1ActionDesc",
                false, null, conditions, nestedActions, null, null);
        
        // create rule JSON
        List<String> ruleTypes = new ArrayList<String>();
        ruleTypes.add(RuleType.UPDATE);
        JSONObject ruleJson = getRuleJsonObject(null, null, "Rule1", "Rule1Desc", ruleTypes,
                ruleActionJson, false, false, false, null);
        
        // POST rule JSON to rules collection resource
        JSONObject resultPostRule = postRules(this.owningNodeRef1, ruleJson);
        
        String ruleNodeRefStr = resultPostRule.getString("ruleNodeRef");
        NodeRef ruleNodeRef = new NodeRef(ruleNodeRefStr);
        
        //
        // GET Rule with rule node ref of Rule just POSTed
        // and validate that the Rule returned by GET Rule
        // is the same as that for the rule just POSTed
        //
        String getRuleRulesBasedUrl = "/api/rules/" + ruleNodeRef.getStoreRef().getProtocol() + "/"
        +  ruleNodeRef.getStoreRef().getIdentifier() + "/" + ruleNodeRef.getId();
        
        Response response = sendRequest(new GetRequest(getRuleRulesBasedUrl), Status.STATUS_OK);
        JSONObject resultGetRule = new JSONObject(response.getContentAsString());
        
        //
        // validate properties returned in GET Rule response
        //
        
        //
        // check rule properties
        //
        assertEquals(this.owningNodeRef1.toString(), resultGetRule.getString("owningNodeRef"));
        assertEquals(ruleNodeRefStr, resultGetRule.getString("ruleNodeRef"));
        assertEquals("Rule1", resultGetRule.getString("title"));
        assertEquals("Rule1Desc", resultGetRule.getString("description"));
        assertEquals(false, resultGetRule.getBoolean("executeAsync"));
        assertEquals(false, resultGetRule.getBoolean("ruleDisabled"));
        assertEquals(false, resultGetRule.getBoolean("appliedToChildren"));
        
        //
        // check rule types
        //
        JSONArray resultRuleTypes = resultGetRule.getJSONArray("ruleTypes");
        assertEquals(1, resultRuleTypes.length());
        assertEquals(RuleType.UPDATE, resultRuleTypes.getString(0));
        
        // retrieve ID for copy action
        JSONObject resultRuleAction = resultGetRule.getJSONObject("action");
        JSONObject resultNestedActions = resultRuleAction.getJSONObject("actions");
        Iterator<String> actionKeysIter = resultNestedActions.keys();
        String resultCopyActionID = null;
        while (actionKeysIter.hasNext())
        {
            String key = actionKeysIter.next();
            
            JSONObject resultAction = resultNestedActions.getJSONObject(key);
            String actionDefName = resultAction.getString("actionDefinitionName");
            
            if (actionDefName.equals(CopyActionExecuter.NAME))
            {
                resultCopyActionID = resultAction.getString("id");
                break;
            }
        }
        
        assertNotNull(resultCopyActionID);
        
        // retrieve copy action
        JSONObject resCopyAction = resultNestedActions.getJSONObject(resultCopyActionID);
        
        //
        // check copy action properties
        //
        assertEquals(CopyActionExecuter.NAME, resCopyAction.getString("actionDefinitionName"));
        assertEquals("CopyTitle", resCopyAction.getString("title"));
        assertEquals("CopyDesc" , resCopyAction.getString("description"));
        assertEquals(false, resCopyAction.getBoolean("executeAsync"));

        //
        // check copy action parameters
        //
        JSONObject resCopyActionParams = resCopyAction.getJSONObject("parameterValues");
        assertEquals(QName.createQName(
                NamespaceService.CONTENT_MODEL_1_0_URI, "copy").toString(),
                resCopyActionParams.getString(MoveActionExecuter.PARAM_ASSOC_QNAME));
        assertEquals(this.testDestFolder1.toString(), resCopyActionParams.getString(MoveActionExecuter.PARAM_DESTINATION_FOLDER));
        assertEquals(ContentModel.ASSOC_CONTAINS.toString(), resCopyActionParams.getString(MoveActionExecuter.PARAM_ASSOC_TYPE_QNAME)); 
        
        // retrieve ID for compare MIME-type condition
        JSONObject resultConditions = resultRuleAction.getJSONObject("conditions");
        Iterator<String> condKeysIter = resultConditions.keys();
        String resultCondCompMimeTypeID = null;
        while (condKeysIter.hasNext())
        {
            String key = condKeysIter.next();
            
            JSONObject resultCondition = resultConditions.getJSONObject(key);
            String condDefName = resultCondition.getString("conditionDefinitionName");
            
            if (condDefName.equals(CompareMimeTypeEvaluator.NAME))
            {
                resultCondCompMimeTypeID = resultCondition.getString("id");
                break;
            }
        }
        
        assertNotNull(resultCondCompMimeTypeID);
        
        //
        // retrieve compare MIME-type condition
        //
        JSONObject resCompMimeTypeCond = resultConditions.getJSONObject(resultCondCompMimeTypeID);
        
        //
        // check compare MIME type condition's properties
        //
        assertEquals(CompareMimeTypeEvaluator.NAME, resCompMimeTypeCond.getString("conditionDefinitionName"));
        assertEquals(false, resCompMimeTypeCond.getBoolean("invertCondition"));
        
        //
        // retrieve compare MIME-type condition's parameter JSON and check its value
        //
        JSONObject resCondCmpMimeTypeParams = resCompMimeTypeCond.getJSONObject("parameterValues");
        assertEquals("image/png", resCondCmpMimeTypeParams.getString(ComparePropertyValueEvaluator.PARAM_VALUE));
    }
    
    public void testGetRules() throws Exception
    {
        //
        // First POST a Rule
        //
        
        // create condition parameters
        JSONObject condCompMimeTypeParams = new JSONObject();
        condCompMimeTypeParams.put(ComparePropertyValueEvaluator.PARAM_VALUE, "image/png");
        
        // create conditions
        JSONObject conditionCompMimeType = getConditionJsonObject(null, condCompMimeTypeParams, "compare-mime-type", false, null);
        JSONArray conditions = new JSONArray();
        conditions.put(conditionCompMimeType);
        
        // create parameters for nested actions
        JSONObject actionCopyParamsJson = new JSONObject();
        actionCopyParamsJson.put(MoveActionExecuter.PARAM_ASSOC_QNAME,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "copy").toString());
        actionCopyParamsJson.put(MoveActionExecuter.PARAM_DESTINATION_FOLDER, this.testDestFolder1.toString());
        actionCopyParamsJson.put(MoveActionExecuter.PARAM_ASSOC_TYPE_QNAME, ContentModel.ASSOC_CONTAINS.toString());
        
        // create nested actions
        JSONObject actionCopyJson = getActionJsonObject(null, CopyActionExecuter.NAME, "CopyTitle", "CopyDesc", false,
                actionCopyParamsJson, null, null, null, null);
        JSONArray nestedActions = new JSONArray();
        nestedActions.put(actionCopyJson);
        
        // create rule's composite action
        JSONObject compoActionJson = getActionJsonObject(null, CompositeActionExecuter.NAME, "Rule1Action", "Rule1ActionDesc",
                false, null, conditions, nestedActions, null, null);
        
        // create rule to POST
        List<String> ruleTypes = new ArrayList<String>();
        ruleTypes.add(RuleType.UPDATE);
        JSONObject ruleJson = getRuleJsonObject(this.owningNodeRef1, null, "Rule1", "Rule1Desc", ruleTypes,
                compoActionJson, false, false, false, null);
        
        // POST rule JSON to rules collection resource
        postRules(this.owningNodeRef1, ruleJson);
        
        //
        // GET Rules and test to make sure that POSTed rule is in return Rules collection
        //
        
        JSONArray result = getRules(this.owningNodeRef1, null, null, Status.STATUS_OK);
        
        boolean postedRuleFound = false;
        for (int i=0; i < result.length(); i++)
        {
            JSONObject resultRuleJson = result.getJSONObject(i);
            String ruleTitle = resultRuleJson.getString("title");
            
            if (ruleTitle.equals("Rule1"))
            {
                postedRuleFound = true;
            }
        }
        
        assertTrue(postedRuleFound == true);
    }
    
    @SuppressWarnings("unchecked")
    public void testPutRule() throws Exception
    {
        // -------------------------
        // Create a Rule - POST Rule
        // -------------------------
        
        // create condition parameters
        JSONObject condCompMimeTypeParams = new JSONObject();
        condCompMimeTypeParams.put(ComparePropertyValueEvaluator.PARAM_VALUE, "image/png");
        
        //
        // create conditions JSON
        //
        JSONObject conditionCompMimeType = getConditionJsonObject(null, condCompMimeTypeParams, "compare-mime-type", false, null);
        JSONArray conditions = new JSONArray();
        conditions.put(conditionCompMimeType);
        
        //
        // create parameters for nested actions JSON
        //
        JSONObject actionCopyParamsJson = new JSONObject();
        actionCopyParamsJson.put(MoveActionExecuter.PARAM_ASSOC_QNAME,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "copy").toString());
        actionCopyParamsJson.put(MoveActionExecuter.PARAM_DESTINATION_FOLDER, this.testDestFolder1.toString());
        actionCopyParamsJson.put(MoveActionExecuter.PARAM_ASSOC_TYPE_QNAME, ContentModel.ASSOC_CONTAINS.toString());
        
        //
        // create nested actions JSON
        //
        JSONObject actionCopyJson = getActionJsonObject(null, CopyActionExecuter.NAME, "CopyTitle", "CopyDesc", false,
                actionCopyParamsJson, null, null, null, null);
        JSONArray nestedActions = new JSONArray();
        nestedActions.put(actionCopyJson);
        
        //
        // create rule's composite action JSON
        //
        JSONObject compoActionJson = getActionJsonObject(null, CompositeActionExecuter.NAME, "Rule1Action", "Rule1ActionDesc",
                false, null, conditions, nestedActions, null, null);
        
        //
        // create rule to POST JSON
        //
        List<String> ruleTypes = new ArrayList<String>();
        ruleTypes.add(RuleType.UPDATE);
        JSONObject ruleJson = getRuleJsonObject(this.owningNodeRef1, null, "Rule", "RuleDesc", ruleTypes,
                compoActionJson, false, false, false, null);
        
        //
        // POST rule JSON to rules collection resource JSON
        //
        JSONObject resultRuleJson = postRules(this.owningNodeRef1, ruleJson);
        
        // get the rule node ref from the rule details returned so that
        // we can retrieve the rule later
        String ruleNodeRefStr = resultRuleJson.getString("ruleNodeRef");
        NodeRef ruleNodeRef = new NodeRef(ruleNodeRefStr);
        
        // --------------------------
        // Update the Rule - PUT Rule
        // --------------------------
        
        // create parameter for compare MIME-type condition with updated parameter value
        JSONObject putCondCompMimeTypeParams = new JSONObject();
        putCondCompMimeTypeParams.put(ComparePropertyValueEvaluator.PARAM_VALUE, "image/jpeg");
        
        // retrieve ID for compare MIME-type condition
        JSONObject resultRuleAction = resultRuleJson.getJSONObject("action");
        JSONObject resultConditions = resultRuleAction.getJSONObject("conditions");
        Iterator<String> condKeysIter = resultConditions.keys();
        String resultCondCompMimeTypeID = null;
        while (condKeysIter.hasNext())
        {
            String key = condKeysIter.next();
            
            JSONObject resultCondition = resultConditions.getJSONObject(key);
            String condDefName = resultCondition.getString("conditionDefinitionName");
            
            if (condDefName.equals("compare-mime-type"))
            {
                resultCondCompMimeTypeID = resultCondition.getString("id");
                break;
            }
        }
        
        assertNotNull(resultCondCompMimeTypeID);
        
        // create compare MIME-type condition with updated condition parameters and updated properties
        JSONObject putConditionCompMimeType = new JSONObject();
        putConditionCompMimeType.put("id", resultCondCompMimeTypeID);
        putConditionCompMimeType.put("parameterValues", putCondCompMimeTypeParams);
        putConditionCompMimeType.put("conditionDefinitionName", "compare-mime-type");
        putConditionCompMimeType.put("invertCondition", true);
        
        // create conditions JSON array and add updated compare MIME-type condition
        JSONArray putConditions = new JSONArray();
        putConditions.put(putConditionCompMimeType);
         
        // create parameters for copy action JSON with updated parameter values
        JSONObject putCopyActionParams = new JSONObject();
        putCopyActionParams.put(MoveActionExecuter.PARAM_DESTINATION_FOLDER, this.testDestFolder2.toString());
        
        // retrieve ID for copy action
        JSONObject resultActions = resultRuleAction.getJSONObject("actions");
        Iterator<String> actionKeysIter = resultActions.keys();
        String resultCopyActionID = null;
        while (actionKeysIter.hasNext())
        {
            String key = actionKeysIter.next();
            
            JSONObject resultAction = resultActions.getJSONObject(key);
            String actionDefName = resultAction.getString("actionDefinitionName");
            
            if (actionDefName.equals(CopyActionExecuter.NAME))
            {
                resultCopyActionID = resultAction.getString("id");
                break;
            }
        }
        
        assertNotNull(resultCopyActionID);
        
        // create copy action with updated action parameters and updated properties
        JSONObject putCopyAction = new JSONObject();
        putCopyAction.put("id", resultCopyActionID);
        putCopyAction.put("actionDefinitionName", CopyActionExecuter.NAME);
        putCopyAction.put("title", "CopyTitleUpdated");
        putCopyAction.put("description", "CopyDescUpdated");
        putCopyAction.put("executeAsync", true);
        putCopyAction.put("parameterValues", putCopyActionParams);

        // create nested actions JSON array and add updated copy action
        JSONArray putNestedActions = new JSONArray();
        putNestedActions.put(putCopyAction);
         
        // create rule action JSON (root action) with updated properties, conditions and nested actions
        JSONObject putRuleAction = new JSONObject();
        putRuleAction.put("id", resultRuleAction.getString("id"));
        putRuleAction.put("actionDefinitionName", CompositeActionExecuter.NAME);
        putRuleAction.put("actions", putNestedActions);
        putRuleAction.put("conditions", putConditions);
                 
        // create updated rule types JSON array
        JSONArray putRuleTypes = new JSONArray();
        putRuleTypes.put(RuleType.INBOUND);

        // create rule JSON with updated composite action, updated rule types and updated rule properties
        JSONObject putRuleJson = new JSONObject();
        putRuleJson.put("title", "RuleTitleUpdated");
        putRuleJson.put("description", "RuleDescUpdated");
        putRuleJson.put("ruleTypes", putRuleTypes);
        putRuleJson.put("action", putRuleAction);
        putRuleJson.put("executeAsync", true);
        putRuleJson.put("ruleDisabled", true);                 
        
        // 
        // update rule resource with updated rule JSON with PUT Rule  
        // 
        
        String url = "/api/rules/" + ruleNodeRef.getStoreRef().getProtocol() + "/"
        +  ruleNodeRef.getStoreRef().getIdentifier() + "/" + ruleNodeRef.getId();
        
        sendRequest(new PutRequest(url, putRuleJson.toString(), "application/json"),
                Status.STATUS_OK);        

        // -----------------------------------------------
        // Get the updated Rule - GET Rule
        // and validate that the response thereof contains 
        // the updated field values
        // -----------------------------------------------
        
        //
        // get the updated rule
        //
        Response responseGetRule = sendRequest(new GetRequest(url), Status.STATUS_OK);
        JSONObject resGetRule = new JSONObject(responseGetRule.getContentAsString());
        
        //
        // check updated rule fields
        //
        assertEquals("RuleTitleUpdated", resGetRule.getString("title"));
        assertEquals("RuleDescUpdated", resGetRule.getString("description"));
        assertEquals(true, resGetRule.getBoolean("executeAsync"));
        assertEquals(true, resGetRule.getBoolean("ruleDisabled"));
        
        //
        // retrieve updated copy action
        //
        JSONObject resRuleAction = resGetRule.getJSONObject("action");
        JSONObject resNestedActions = resRuleAction.getJSONObject("actions");
        String copyActionId = putCopyAction.getString("id");
        JSONObject resCopyAction = resNestedActions.getJSONObject(copyActionId);
        
        //
        // check updated copy action fields
        //
        assertEquals(CopyActionExecuter.NAME, resCopyAction.getString("actionDefinitionName"));
        assertEquals("CopyTitleUpdated", resCopyAction.getString("title"));
        assertEquals("CopyDescUpdated" , resCopyAction.getString("description"));
        assertEquals(true, resCopyAction.getBoolean("executeAsync"));

        //
        // check updated copy action parameters
        //
        JSONObject resCopyActionParams = resCopyAction.getJSONObject("parameterValues");
        assertEquals(this.testDestFolder2.toString(), resCopyActionParams.getString(MoveActionExecuter.PARAM_DESTINATION_FOLDER));
        
        //
        // retrieve updated compare MIME-type condition
        //
        JSONObject resConditions = resRuleAction.getJSONObject("conditions");
        String compMimeTypeCondId = putConditionCompMimeType.getString("id");
        JSONObject resCompMimeTypeCond = resConditions.getJSONObject(compMimeTypeCondId);
        
        //
        // check updated compare MIME type condition's fields
        //
        assertEquals(true, resCompMimeTypeCond.getBoolean("invertCondition"));
        
        //
        // retrieve compare MIME-type condition's parameter JSON and check its value
        //
        JSONObject resCondCmpMimeTypeParams = resCompMimeTypeCond.getJSONObject("parameterValues");
        assertEquals("image/jpeg", resCondCmpMimeTypeParams.getString(ComparePropertyValueEvaluator.PARAM_VALUE));
    }
    
    public void testPostRules() throws Exception
    {
        // create condition parameters for compare MIME type condition
        JSONObject condCompMimeTypeParams = new JSONObject();
        condCompMimeTypeParams.put(ComparePropertyValueEvaluator.PARAM_VALUE, "image/png");
        
        // create compare MIME type condition
        JSONObject conditionCompMimeType = getConditionJsonObject(null, condCompMimeTypeParams, "compare-mime-type", false, null);
        JSONArray conditions = new JSONArray();
        conditions.put(conditionCompMimeType);
        
        // create action parameters for copy action
        JSONObject actionCopyParamsJson = new JSONObject();
        actionCopyParamsJson.put(MoveActionExecuter.PARAM_ASSOC_QNAME,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "copy").toString());
        actionCopyParamsJson.put(MoveActionExecuter.PARAM_DESTINATION_FOLDER, this.testDestFolder1.toString());
        actionCopyParamsJson.put(MoveActionExecuter.PARAM_ASSOC_TYPE_QNAME, ContentModel.ASSOC_CONTAINS.toString());
        
        // create copy action and add to nested actions
        JSONObject actionCopyJson = getActionJsonObject(null, CopyActionExecuter.NAME, "CopyActionTitle", "CopyActionDesc", false,
                actionCopyParamsJson, null, null, null, null);
        JSONArray nestedActions = new JSONArray();
        nestedActions.put(actionCopyJson);
        
        // create rule's composite action
        JSONObject compoActionJson = getActionJsonObject(null, CompositeActionExecuter.NAME, "Rule1Action", "Rule1ActionDesc",
                false, null, conditions, nestedActions, null, null);
        
        // create rule to POST
        List<String> ruleTypes = new ArrayList<String>();
        ruleTypes.add(RuleType.UPDATE);
        JSONObject ruleJson = getRuleJsonObject(this.owningNodeRef1, null, "Rule1", "Rule1Desc", ruleTypes,
                compoActionJson, false, false, false, null);
        
        // POST RuleJson to Rules Collection
        JSONObject resultRule = postRules(this.owningNodeRef1, ruleJson);
        
        //
        // validate rule result
        // 
        
        assertEquals(this.owningNodeRef1.toString(), resultRule.getString("owningNodeRef"));
        assertEquals("Rule1", resultRule.getString("title"));
        assertEquals("Rule1Desc", resultRule.getString("description"));
        
        // validate that rule types sent in rule JSON are the same as that returned
        // there should only be one, namely "update"
        JSONArray resultRuleTypes = resultRule.getJSONArray("ruleTypes");
        assertTrue(resultRuleTypes.length() == 1);
        assertEquals(RuleType.UPDATE, resultRuleTypes.getString(0));
        
        assertEquals(false, resultRule.getBoolean("executeAsync"));
        assertEquals(false, resultRule.getBoolean("ruleDisabled"));
        assertEquals(false, resultRule.getBoolean("appliedToChildren"));
        
        // validate rule's composite action from rule in result JSON
        JSONObject resultCompoAction = resultRule.getJSONObject("action");
        
        assertEquals(CompositeActionExecuter.NAME, resultCompoAction.getString("actionDefinitionName"));
        assertEquals("Rule1Action", resultCompoAction.getString("title"));
        assertEquals("Rule1ActionDesc", resultCompoAction.getString("description"));
        assertEquals(false, resultCompoAction.getBoolean("executeAsync"));
        
        // validate condition in rule's composite action from the result JSON
        // there should only be one, namely the compare MIME type condition
        JSONObject resultConditions = resultCompoAction.getJSONObject("conditions");
        String resultcondCompMimeTypeKey = resultConditions.names().getString(0);
        JSONObject resultcondCompMimeType = resultConditions.getJSONObject(resultcondCompMimeTypeKey);
        
        assertEquals("compare-mime-type", resultcondCompMimeType.getString("conditionDefinitionName"));
        assertEquals(false, resultcondCompMimeType.getBoolean("invertCondition"));
        
        // validate parameter values in compare MIME type condition
        JSONObject resultCondParamVals = resultcondCompMimeType.getJSONObject("parameterValues");
        assertEquals("image/png", resultCondParamVals.getString(ComparePropertyValueEvaluator.PARAM_VALUE));
        
        // validate nested action in rule's composite action from the result JSON
        // there should only be one nested action, namely the copy action
        JSONObject resultNestedActions = resultCompoAction.getJSONObject("actions");
        String resultActionCopyKey = resultNestedActions.names().getString(0);
        JSONObject resultCopyAction = resultNestedActions.getJSONObject(resultActionCopyKey);
        
        assertEquals(CopyActionExecuter.NAME, resultCopyAction.getString("actionDefinitionName"));
        assertEquals("CopyActionTitle", resultCopyAction.getString("title"));
        assertEquals("CopyActionDesc", resultCopyAction.getString("description"));
        assertEquals(false, resultCopyAction.getBoolean("executeAsync"));
        
        // validate parameter values in copy action
        JSONObject resultActionParamVals = resultCopyAction.getJSONObject("parameterValues");
        assertEquals(QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "copy").toString(),
                resultActionParamVals.getString(MoveActionExecuter.PARAM_ASSOC_QNAME));
        assertEquals(this.testDestFolder1.toString(),
                resultActionParamVals.getString(MoveActionExecuter.PARAM_DESTINATION_FOLDER));
        assertEquals(ContentModel.ASSOC_CONTAINS.toString(),
                resultActionParamVals.getString(MoveActionExecuter.PARAM_ASSOC_TYPE_QNAME));
    }
    
    public void testPostActionQueue() throws Exception
    {
        // construct ActionQueue resource URL
        String url = "/api/actionqueue";
        
        //
        // construct action JSON to put into action queue item
        // (which will, in turn, be posted to the action queue)
        //
        
        // create action parameters for copy action
        JSONObject actionCopyParamsJson = new JSONObject();
        actionCopyParamsJson.put(MoveActionExecuter.PARAM_ASSOC_QNAME,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "copy").toString());
        actionCopyParamsJson.put(MoveActionExecuter.PARAM_DESTINATION_FOLDER, this.testDestFolder1.toString());
        actionCopyParamsJson.put(MoveActionExecuter.PARAM_ASSOC_TYPE_QNAME, ContentModel.ASSOC_CONTAINS.toString());
        
        // create copy action
        JSONObject actionCopyJson = getActionJsonObject(null, CopyActionExecuter.NAME, "CopyActionTitle",
                "CopyActionDesc", false, actionCopyParamsJson, null, null, null, null);
        
        //
        // create action queue item json
        //
        
        JSONObject actionQueueItemJson = new JSONObject();
        actionQueueItemJson.put("action", actionCopyJson);
        actionQueueItemJson.put("nodeRef", this.testDestFolder1.toString());
        actionQueueItemJson.put("checkConditions", true);
        actionQueueItemJson.put("executeAsync", false);
        
        Response response = sendRequest(new PostRequest(url, actionQueueItemJson.toString(), "application/json"),
                Status.STATUS_OK); 
        JSONObject result = new JSONObject(response.getContentAsString());
        
        assertNotNull(result);
        
        assertEquals("COMPLETE", result.getString("status"));
    }
    
    public void testGetConditionDefs() throws Exception
    {
        // construct condition definition collection resource URL
        String url = "/api/rules/conditiondefs";
        
        Response response = sendRequest(new GetRequest(url),
                Status.STATUS_OK); 
        JSONArray result = new JSONArray(response.getContentAsString());
        
        assertTrue(result.length() > 0);    
    }
    
    public void testGetConditionDef() throws Exception
    {
        String conditionDefName = "compare-property-value";
        // construct condition definition resource URL
        String url = "/api/rules/conditiondefs/" + conditionDefName;
        
        Response response = sendRequest(new GetRequest(url),
                Status.STATUS_OK); 
        JSONObject result = new JSONObject(response.getContentAsString());
        
        assertNotNull(result);
        
        assertEquals("compare-property-value", result.getString("name"));
        
        assertEquals(false, result.getBoolean("adhocPropertiesAllowed"));
    }
    
    public void testGetActionDefs() throws Exception
    {
        // construct action definition collection resource URL
        String url = "/api/rules/actiondefs";
        
        Response response = sendRequest(new GetRequest(url),
                Status.STATUS_OK); 
        JSONArray result = new JSONArray(response.getContentAsString());
        
        assertTrue(result.length() > 0);        
    }
    
    public void testGetActionDef() throws Exception
    {
        String actionDefName = "transform";
        // construct action definition resource URL
        String url = "/api/rules/actiondefs/" + actionDefName;
        
        Response response = sendRequest(new GetRequest(url),
                Status.STATUS_OK); 
        JSONObject result = new JSONObject(response.getContentAsString());
        
        assertNotNull(result);
        
        // validate applicable types in returned action definition 
        boolean applicableTypeContentFound = false;
        JSONArray applicableTypes = result.getJSONArray("applicableTypes");
        for (int i=0; i < applicableTypes.length(); i++)
        {
            String applicableType = applicableTypes.getString(i);
            if (applicableType.equals("content"))
            {
                applicableTypeContentFound = true;
            }
        }
        assertTrue(applicableTypeContentFound == true);
        
        assertEquals("transform", result.getString("name"));
        
        assertEquals(false, result.getBoolean("adhocPropertiesAllowed"));
    }
}
