/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.rules;

import static org.alfresco.rest.actions.access.AccessRestrictionUtil.MAIL_ACTION;
import static org.alfresco.rest.actions.access.AccessRestrictionUtil.createMailParameters;
import static org.alfresco.utility.model.UserModel.getRandomUserModel;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestActionBodyExecTemplateModel;
import org.alfresco.rest.model.RestActionConstraintDataModel;
import org.alfresco.rest.model.RestActionConstraintModel;
import org.alfresco.rest.model.RestActionDefinitionModel;
import org.alfresco.rest.model.RestCompositeConditionDefinitionModel;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.model.RestParameterDefinitionModel;
import org.alfresco.rest.model.RestRuleExecutionModel;
import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.rest.model.RestSimpleConditionDefinitionModel;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.data.DataSite;
import org.alfresco.utility.data.DataUserAIS;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RulesTestsUtils implements InitializingBean
{
    static final String RULE_NAME_DEFAULT = "ruleName";
    static final String RULE_DESCRIPTION_DEFAULT = "rule description";
    static final boolean RULE_ENABLED_DEFAULT = true;
    static final boolean RULE_CASCADE_DEFAULT = true;
    static final boolean RULE_ASYNC_DEFAULT = true;
    static final boolean RULE_SHARED_DEFAULT = false;
    static final String RULE_SCRIPT_ID = "script";
    static final String RULE_SCRIPT_PARAM_ID = "script-ref";
    static final String RULE_ERROR_SCRIPT_LABEL = "Start Pooled Review and Approve Workflow";
    static final String INBOUND = "inbound";
    static final String UPDATE = "update";
    static final String OUTBOUND = "outbound";
    static final List<String> RULE_TRIGGERS_DEFAULT = List.of(INBOUND, UPDATE, OUTBOUND);
    static final boolean INVERTED = true;
    static final String AND = "and";
    static final String ID = "id";
    static final String IS_SHARED = "isShared";
    static final String AUDIO_ASPECT = "audio:audio";
    static final String LOCKABLE_ASPECT = "cm:lockable";

    @Autowired
    private RestWrapper restClient;
    @Autowired
    private DataUserAIS dataUser;
    @Autowired
    private DataSite dataSite;
    @Autowired
    private DataContent dataContent;

    private SiteModel site;
    private String reviewAndApproveWorkflowNode;

    private FolderModel copyDestinationFolder;

    private FolderModel checkOutDestinationFolder;

    /**
     * Initialise the util class.
     */
    @Override
    public void afterPropertiesSet()
    {
        UserModel admin = dataUser.getAdminUser();
        // Obtain the node ref for the review and approve workflow.
        RestActionDefinitionModel actionDef = restClient.authenticateUser(admin).withCoreAPI().usingActions().getActionDefinitionById(RULE_SCRIPT_ID);
        RestParameterDefinitionModel paramDef = actionDef.getParameterDefinitions().stream().filter(param -> param.getName().equals(RULE_SCRIPT_PARAM_ID)).findFirst().get();
        String constraintName = paramDef.getParameterConstraintName();
        RestActionConstraintModel constraintDef = restClient.authenticateUser(admin).withCoreAPI().usingActions().getActionConstraintByName(constraintName);
        RestActionConstraintDataModel reviewAndApprove = constraintDef.getConstraintValues().stream().filter(constraintValue -> constraintValue.getLabel().equals(RULE_ERROR_SCRIPT_LABEL)).findFirst().get();
        reviewAndApproveWorkflowNode = reviewAndApprove.getValue();

        // Create a couple of public folders to be used as action destinations.
        site = dataSite.usingUser(admin).createPublicRandomSite();
        copyDestinationFolder = dataContent.usingUser(admin).usingSite(site).createFolder();
        checkOutDestinationFolder = dataContent.usingUser(admin).usingSite(site).createFolder();
    }

    public RestRuleModel createRuleModelWithModifiedValues()
    {
        return createRuleModelWithModifiedValues(List.of(createAddAudioAspectAction()));
    }

    /**
     * Get the review and approve workflow node (throwing an exception if this utility class has not been initialised).
     *
     * @return The node ref of the script node.
     */
    public String getReviewAndApproveWorkflowNode()
    {
        return reviewAndApproveWorkflowNode;
    }

    public FolderModel getCopyDestinationFolder()
    {
        return copyDestinationFolder;
    }

    public FolderModel getCheckOutDestinationFolder()
    {
        return checkOutDestinationFolder;
    }

    /**
     * Create a rule model filled with custom constant values.
     *
     * @param actions - rule's actions.
     * @return The created rule model.
     */
    public RestRuleModel createRuleModelWithModifiedValues(List<RestActionBodyExecTemplateModel> actions)
    {
        RestRuleModel ruleModel = createRuleModel(RULE_NAME_DEFAULT, actions);
        ruleModel.setDescription(RULE_DESCRIPTION_DEFAULT);
        ruleModel.setIsEnabled(RULE_ENABLED_DEFAULT);
        ruleModel.setIsInheritable(RULE_CASCADE_DEFAULT);
        ruleModel.setIsAsynchronous(RULE_ASYNC_DEFAULT);
        ruleModel.setIsShared(RULE_SHARED_DEFAULT);
        ruleModel.setTriggers(RULE_TRIGGERS_DEFAULT);
        ruleModel.setErrorScript(getReviewAndApproveWorkflowNode());

        return ruleModel;
    }

    public RestRuleModel createRuleModelWithDefaultValues()
    {
        return createRuleModel(RULE_NAME_DEFAULT);
    }

    public RestRuleModel createRuleModel(String name)
    {
        return createRuleModel(name, List.of(createAddAudioAspectAction()));
    }

    /**
     * Create a rule model.
     *
     * @param name The name for the rule.
     * @param actions Rule's actions.
     * @return The created rule model.
     */
    public RestRuleModel createRuleModel(String name, List<RestActionBodyExecTemplateModel> actions)
    {
        RestRuleModel ruleModel = new RestRuleModel();
        ruleModel.setIsEnabled(true);
        ruleModel.setName(name);
        ruleModel.setActions(actions);
        return ruleModel;
    }

    /**
     * Create a rule's action model.
     *
     * @return The created action model.
     */
    public RestActionBodyExecTemplateModel createAddAudioAspectAction()
    {
        return createAddAspectAction(AUDIO_ASPECT);
    }

    public RestActionBodyExecTemplateModel createAddAspectAction(String aspect)
    {
        return createCustomActionModel("add-features", Map.of("aspect-name", aspect));
    }

    public RestActionBodyExecTemplateModel createCustomActionModel(String actionDefinitionId, Map<String, Serializable> params)
    {
        RestActionBodyExecTemplateModel restActionModel = new RestActionBodyExecTemplateModel();
        restActionModel.setActionDefinitionId(actionDefinitionId);
        restActionModel.setParams(params);
        return restActionModel;
    }

    public RestCompositeConditionDefinitionModel createEmptyConditionModel()
    {
        RestCompositeConditionDefinitionModel conditions = new RestCompositeConditionDefinitionModel();
        conditions.setInverted(!INVERTED);
        conditions.setBooleanMode(AND);
        return conditions;
    }

    public RestCompositeConditionDefinitionModel createVariousConditions()
    {
        return createCompositeCondition(List.of(
            createCompositeCondition(!INVERTED, List.of(
                createSimpleCondition("cm:created", "less_than", "2022-09-01T12:59:00.000+02:00"),
                createSimpleCondition("cm:creator", "ends", "ski"),
                createSimpleCondition("size", "greater_than", "90000000"),
                createSimpleCondition("mimetype", "equals", "video/3gpp"),
                createSimpleCondition("encoding", "equals", "utf-8"),
                createSimpleCondition("type", "equals", "cm:folder"),
                createSimpleCondition("tag", "equals", "uat")
            )),
            createCompositeCondition(INVERTED, List.of(
                createSimpleCondition("aspect", "equals", AUDIO_ASPECT),
                createSimpleCondition("cm:modelVersion", "begins", "1.")
            ))
        ));
    }

    public RestRuleModel createRuleWithVariousActions()
    {
        final Map<String, Serializable> copyParams =
                Map.of("destination-folder", copyDestinationFolder.getNodeRef(), "deep-copy", true);
        final RestActionBodyExecTemplateModel copyAction = createCustomActionModel("copy", copyParams);
        final Map<String, Serializable> checkOutParams =
                Map.of("destination-folder", checkOutDestinationFolder.getNodeRef(), "assoc-name", "cm:checkout",
                        "assoc-type", "cm:contains");
        final RestActionBodyExecTemplateModel checkOutAction = createCustomActionModel("check-out", checkOutParams);
        // The counter action takes no parameters, so check we can omit the "params" entry.
        final RestActionBodyExecTemplateModel counterAction = createCustomActionModel("counter", null);
        final RestRuleModel ruleModel = createRuleModelWithDefaultValues();
        ruleModel.setActions(Arrays.asList(copyAction, checkOutAction, counterAction));

        return ruleModel;
    }

    public RestRuleModel createRuleWithPrivateAction()
    {
        RestActionBodyExecTemplateModel mailAction = new RestActionBodyExecTemplateModel();
        mailAction.setActionDefinitionId(MAIL_ACTION);
        mailAction.setParams(createMailParameters(getRandomUserModel(), getRandomUserModel()));
        RestRuleModel ruleModel = createRuleModelWithDefaultValues();
        ruleModel.setActions(Arrays.asList(mailAction));
        return ruleModel;
    }

    public RestSimpleConditionDefinitionModel createSimpleCondition(String field, String comparator, String parameter)
    {
        RestSimpleConditionDefinitionModel simpleCondition = new RestSimpleConditionDefinitionModel();
        simpleCondition.setField(field);
        simpleCondition.setComparator(comparator);
        simpleCondition.setParameter(parameter);
        return simpleCondition;
    }

    public RestCompositeConditionDefinitionModel createCompositeCondition(List<RestCompositeConditionDefinitionModel> compositeConditions)
    {
        return createCompositeCondition(AND, !INVERTED, compositeConditions, null);
    }

    public RestCompositeConditionDefinitionModel createCompositeCondition(boolean inverted,
        List<RestSimpleConditionDefinitionModel> simpleConditions)
    {
        return createCompositeCondition(AND, inverted, null, simpleConditions);
    }

    public RestRuleExecutionModel createRuleExecutionRequest()
    {
        return createRuleExecutionRequest(false);
    }

    public RestRuleExecutionModel createRuleExecutionRequest(boolean eachSubFolderIncluded)
    {
        RestRuleExecutionModel ruleExecutionBody = new RestRuleExecutionModel();
        ruleExecutionBody.setIsEachSubFolderIncluded(eachSubFolderIncluded);

        return ruleExecutionBody;
    }

    private RestCompositeConditionDefinitionModel createCompositeCondition(String booleanMode, boolean inverted,
        List<RestCompositeConditionDefinitionModel> compositeConditions, List<RestSimpleConditionDefinitionModel> simpleConditions)
    {
        RestCompositeConditionDefinitionModel compositeCondition = new RestCompositeConditionDefinitionModel();
        compositeCondition.setBooleanMode(booleanMode);
        compositeCondition.setInverted(inverted);
        compositeCondition.setCompositeConditions(compositeConditions);
        compositeCondition.setSimpleConditions(simpleConditions);

        return compositeCondition;
    }

    public NodeAssertion assertThat(RestNodeModel node)
    {
        return new NodeAssertion(node);
    }

    public class NodeAssertion
    {
        private final RestNodeModel node;

        private NodeAssertion(RestNodeModel node)
        {
            this.node = node;
        }

        public NodeAssertion containsAspects(String ...expectedAspects)
        {
            Arrays.stream(expectedAspects).forEach(aspect -> node.assertThat().field("aspectNames").contains(aspect));
            return this;
        }

        public NodeAssertion notContainsAspects(String ...unexpectedAspects)
        {
            Arrays.stream(unexpectedAspects).forEach(aspect -> node.assertThat().field("aspectNames").notContains(aspect));
            return this;
        }
    }
}
