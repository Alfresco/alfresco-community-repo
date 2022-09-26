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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.rest.model.RestActionBodyExecTemplateModel;
import org.alfresco.rest.model.RestCompositeConditionDefinitionModel;
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

    /**
     * Create a rule model filled with default values.
     *
     * @return The created rule model.
     */
    public static RestRuleModel createRuleModelWithModifiedValues()
    {
        RestRuleModel ruleModel = createRuleModelWithDefaultValues();
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
        return createRuleModel(RULE_NAME_DEFAULT, List.of(createDefaultActionModel()));
    }

    public static RestRuleModel createRuleModel(String name)
    {
        return createRuleModel(name, List.of(createDefaultActionModel()));
    }

    /**
     * Create a rule model.
     *
     * @param name The name for the rule.
     * @param restActionModels Rule's actions.
     * @return The created rule model.
     */
    public static RestRuleModel createRuleModel(String name, List<RestActionBodyExecTemplateModel> restActionModels)
    {
        RestRuleModel ruleModel = new RestRuleModel();
        ruleModel.setName(name);
        ruleModel.setActions(restActionModels);
        return ruleModel;
    }

    /**
     * Create a rule's action model.
     *
     * @return The created action model.
     */
    public static RestActionBodyExecTemplateModel createDefaultActionModel()
    {
        RestActionBodyExecTemplateModel restActionModel = new RestActionBodyExecTemplateModel();
        restActionModel.setActionDefinitionId("set-property-value");
        restActionModel.setParams(Map.of("aspect-name", "cm:audio"));
        return restActionModel;
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
                createSimpleCondition("aspect", "equals", "audio:audio"),
                createSimpleCondition("cm:modelVersion", "begins", "1.")
            ))
        ));
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
