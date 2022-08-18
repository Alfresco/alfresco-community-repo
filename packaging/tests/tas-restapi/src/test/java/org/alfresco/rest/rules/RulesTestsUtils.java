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

import java.util.List;
import java.util.Map;

import org.alfresco.rest.model.RestActionBodyExecTemplateModel;
import org.alfresco.rest.model.RestCompositeConditionDefinitionModel;
import org.alfresco.rest.model.RestRuleModel;

public class RulesTestsUtils
{
    static final String RULE_NAME_DEFAULT = "ruleName";
    static final String RULE_DESCRIPTION_DEFAULT = "rule description";
    static final boolean RULE_ENABLED_DEFAULT = true;
    static final boolean RULE_CASCADE_DEFAULT = true;
    static final boolean RULE_ASYNC_DEFAULT = true;
    static final boolean RULE_SHARED_DEFAULT = false;
    static final String RULE_ERROR_SCRIPT_DEFAULT = "error-script";
    static final List<String> ruleTriggersDefault = List.of("inbound", "update", "outbound");

    /**
     * Create a rule model filled with default values.
     *
     * @return The created rule model.
     */
    public static RestRuleModel createRuleModelWithDefaultValues()
    {
        RestRuleModel ruleModel = createRuleModelWithDefaultName();
        ruleModel.setDescription(RULE_DESCRIPTION_DEFAULT);
        ruleModel.setEnabled(RULE_ENABLED_DEFAULT);
        ruleModel.setCascade(RULE_CASCADE_DEFAULT);
        ruleModel.setAsynchronous(RULE_ASYNC_DEFAULT);
        ruleModel.setIsShared(RULE_SHARED_DEFAULT);
        ruleModel.setTriggers(ruleTriggersDefault);
        ruleModel.setErrorScript(RULE_ERROR_SCRIPT_DEFAULT);

        return ruleModel;
    }

    public static RestRuleModel createRuleModelWithDefaultName()
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
        restActionModel.setActionDefinitionId("add-features");
        restActionModel.setParams(Map.of("aspect-name", "{http://www.alfresco.org/model/audio/1.0}audio", "actionContext", "rule"));
        return restActionModel;
    }

    public static RestCompositeConditionDefinitionModel createEmptyConditionModel()
    {
        RestCompositeConditionDefinitionModel conditions = new RestCompositeConditionDefinitionModel();
        conditions.setInverted(false);
        conditions.setBooleanMode("and");
        return conditions;
    }
}
