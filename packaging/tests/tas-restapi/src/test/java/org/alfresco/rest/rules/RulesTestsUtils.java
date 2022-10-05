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

import org.alfresco.rest.model.RestActionBodyExecTemplateModel;
import org.alfresco.rest.model.RestCompositeConditionDefinitionModel;
import org.alfresco.rest.model.RestRuleExecutionBodyModel;
import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.rest.model.RestSimpleConditionDefinitionModel;

public class RulesTestsUtils
{
    static final String RULE_NAME_DEFAULT = "ruleName";
    static final String RULE_DESCRIPTION_DEFAULT = "rule description";
    static final boolean RULE_ENABLED_DEFAULT = true;
    static final boolean RULE_CASCADE_DEFAULT = true;
    static final boolean RULE_ASYNC_DEFAULT = true;
    static final boolean RULE_SHARED_DEFAULT = false;
    static final String RULE_ERROR_SCRIPT_DEFAULT = "error-script";
    static final String INBOUND = "inbound";
    static final String UPDATE = "update";
    static final String OUTBOUND = "outbound";
    static final List<String> RULE_TRIGGERS_DEFAULT = List.of(INBOUND, UPDATE, OUTBOUND);
    static final boolean INVERTED = true;
    static final String AND = "and";
    static final String ID = "id";
    static final String IS_SHARED = "isShared";
    static final String AUDIO_ASPECT = "audio:audio";

    public static RestRuleModel createRuleModelWithModifiedValues()
    {
        return createRuleModelWithModifiedValues(List.of(createAddAudioAspectAction()));
    }

    /**
     * Create a rule model filled with custom constant values.
     *
     * @param actions - rule's actions.
     * @return The created rule model.
     */
    public static RestRuleModel createRuleModelWithModifiedValues(List<RestActionBodyExecTemplateModel> actions)
    {
        RestRuleModel ruleModel = createRuleModel(RULE_NAME_DEFAULT, actions);
        ruleModel.setDescription(RULE_DESCRIPTION_DEFAULT);
        ruleModel.setIsEnabled(RULE_ENABLED_DEFAULT);
        ruleModel.setIsInheritable(RULE_CASCADE_DEFAULT);
        ruleModel.setIsAsynchronous(RULE_ASYNC_DEFAULT);
        ruleModel.setIsShared(RULE_SHARED_DEFAULT);
        ruleModel.setTriggers(RULE_TRIGGERS_DEFAULT);
        ruleModel.setErrorScript(RULE_ERROR_SCRIPT_DEFAULT);

        return ruleModel;
    }

    public static RestRuleModel createRuleModelWithDefaultValues()
    {
        return createRuleModel(RULE_NAME_DEFAULT);
    }

    public static RestRuleModel createRuleModel(String name)
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
    public static RestRuleModel createRuleModel(String name, List<RestActionBodyExecTemplateModel> actions)
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
    public static RestActionBodyExecTemplateModel createAddAudioAspectAction()
    {
        return createCustomActionModel("add-features", Map.of("aspect-name", AUDIO_ASPECT));
    }

    public static RestActionBodyExecTemplateModel createCustomActionModel(String actionDefinitionId, Map<String, Serializable> params)
    {
        RestActionBodyExecTemplateModel restActionModel = new RestActionBodyExecTemplateModel();
        restActionModel.setActionDefinitionId(actionDefinitionId);
        restActionModel.setParams(params);
        return restActionModel;
    }

    public static RestCompositeConditionDefinitionModel createEmptyConditionModel()
    {
        RestCompositeConditionDefinitionModel conditions = new RestCompositeConditionDefinitionModel();
        conditions.setInverted(!INVERTED);
        conditions.setBooleanMode(AND);
        return conditions;
    }

    public static RestCompositeConditionDefinitionModel createVariousConditions()
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

    public static RestRuleModel createVariousActions()
    {
        final Map<String, Serializable> copyParams =
                Map.of("destination-folder", "dummy-folder-node", "deep-copy", true);
        final RestActionBodyExecTemplateModel copyAction = createCustomActionModel("copy", copyParams);
        final Map<String, Serializable> checkOutParams =
                Map.of("destination-folder", "fake-folder-node", "assoc-name", "cm:checkout", "assoc-type",
                        "cm:contains");
        final RestActionBodyExecTemplateModel checkOutAction = createCustomActionModel("check-out", checkOutParams);
        final Map<String, Serializable> scriptParams = Map.of("script-ref", "dummy-script-node-id");
        final RestActionBodyExecTemplateModel scriptAction = createCustomActionModel("script", scriptParams);
        final RestRuleModel ruleModel = createRuleModelWithDefaultValues();
        ruleModel.setActions(Arrays.asList(copyAction, checkOutAction, scriptAction));

        return ruleModel;
    }

    public static RestRuleModel createRuleWithPrivateAction()
    {
        RestActionBodyExecTemplateModel mailAction = new RestActionBodyExecTemplateModel();
        mailAction.setActionDefinitionId(MAIL_ACTION);
        mailAction.setParams(createMailParameters(getRandomUserModel(), getRandomUserModel()));
        RestRuleModel ruleModel = createRuleModelWithDefaultValues();
        ruleModel.setActions(Arrays.asList(mailAction));
        return ruleModel;
    }

    public static RestSimpleConditionDefinitionModel createSimpleCondition(String field, String comparator, String parameter)
    {
        RestSimpleConditionDefinitionModel simpleCondition = new RestSimpleConditionDefinitionModel();
        simpleCondition.setField(field);
        simpleCondition.setComparator(comparator);
        simpleCondition.setParameter(parameter);
        return simpleCondition;
    }

    public static RestCompositeConditionDefinitionModel createCompositeCondition(List<RestCompositeConditionDefinitionModel> compositeConditions)
    {
        return createCompositeCondition(AND, !INVERTED, compositeConditions, null);
    }

    public static RestCompositeConditionDefinitionModel createCompositeCondition(boolean inverted,
        List<RestSimpleConditionDefinitionModel> simpleConditions)
    {
        return createCompositeCondition(AND, inverted, null, simpleConditions);
    }

    public static RestRuleExecutionBodyModel createRuleExecutionRequest()
    {
        return createRuleExecutionRequest(false, false);
    }

    public static RestRuleExecutionBodyModel createRuleExecutionRequest(boolean eachSubFolderIncluded, boolean eachInheritedRuleExecuted)
    {
        RestRuleExecutionBodyModel ruleExecutionBody = new RestRuleExecutionBodyModel();
        ruleExecutionBody.setIsEachSubFolderIncluded(eachSubFolderIncluded);
        ruleExecutionBody.setIsEachInheritedRuleExecuted(eachInheritedRuleExecuted);

        return ruleExecutionBody;
    }

    private static RestCompositeConditionDefinitionModel createCompositeCondition(String booleanMode, boolean inverted,
        List<RestCompositeConditionDefinitionModel> compositeConditions, List<RestSimpleConditionDefinitionModel> simpleConditions)
    {
        RestCompositeConditionDefinitionModel compositeCondition = new RestCompositeConditionDefinitionModel();
        compositeCondition.setBooleanMode(booleanMode);
        compositeCondition.setInverted(inverted);
        compositeCondition.setCompositeConditions(compositeConditions);
        compositeCondition.setSimpleConditions(simpleConditions);

        return compositeCondition;
    }
}
