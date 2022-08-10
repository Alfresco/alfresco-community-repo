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
import org.alfresco.rest.model.RestRuleModel;

public class RulesTestsUtils
{
    static final String FIELD_ID = "id";
    static final String FIELD_NAME = "name";
    static final String FIELD_DESCRIPTION = "description";
    static final String FIELD_ENABLED = "enabled";
    static final String FIELD_CASCADE = "cascade";
    static final String FIELD_ASYNC = "asynchronous";
    static final String FIELD_SHARED = "shared";
    static final String FIELD_TRIGGERS = "triggers";
    static final String FIELD_ERROR_SCRIPT = "errorScript";
    static final String RULE_NAME_DEFAULT = "ruleName";
    static final String RULE_DESCRIPTION_DEFAULT = "rule description";
    static final boolean RULE_ENABLED_DEFAULT = true;
    static final boolean RULE_CASCADE_DEFAULT = true;
    static final boolean RULE_ASYNC_DEFAULT = true;
    static final boolean RULE_SHARED_DEFAULT = false;
    static final String RULE_ERROR_SCRIPT_DEFAULT = "error-script";
    static final List<String> ruleTriggersDefault = List.of("inbound", "update", "outbound");

    public static RestRuleModel createRuleModelWithDefaultValues()
    {
        RestRuleModel ruleModel = createRuleModel(RULE_NAME_DEFAULT);
        ruleModel.setDescription(RULE_DESCRIPTION_DEFAULT);
        ruleModel.setEnabled(RULE_ENABLED_DEFAULT);
        ruleModel.setCascade(RULE_CASCADE_DEFAULT);
        ruleModel.setAsynchronous(RULE_ASYNC_DEFAULT);
        ruleModel.setShared(RULE_SHARED_DEFAULT);
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

    public static RuleModelAssertion assertThat(RestRuleModel rule)
    {
        return new RuleModelAssertion(rule);
    }

    public static class RuleModelAssertion
    {
        private final RestRuleModel rule;

        private RuleModelAssertion(RestRuleModel rule)
        {
            this.rule = rule;
        }

        public FieldAssertion field(String field)
        {
            return new FieldAssertion(field, this);
        }

        public FieldsAssertion fields(String... fields)
        {
            return new FieldsAssertion(List.of(fields), this);
        }

        public static class FieldAssertion
        {
            private final String field;
            private final RuleModelAssertion parent;

            public FieldAssertion(String field, RuleModelAssertion parent)
            {
                this.field = field;
                this.parent = parent;
            }

            public RuleModelAssertion isNull()
            {
                parent.rule.assertThat().field(field).isNull();
                return parent;
            }

            public RuleModelAssertion isNotNull()
            {
                parent.rule.assertThat().field(field).isNotNull();
                return parent;
            }

            public RuleModelAssertion isEqualTo(Object expected)
            {
                parent.rule.assertThat().field(field).is(expected);
                return parent;
            }

            public RuleModelAssertion isEqualToDefaultValue()
            {
                switch (field)
                {
                case FIELD_NAME:
                    parent.rule.assertThat().field(field).is(RULE_NAME_DEFAULT);
                    break;
                case FIELD_DESCRIPTION:
                    parent.rule.assertThat().field(field).is(RULE_DESCRIPTION_DEFAULT);
                    break;
                case FIELD_ENABLED:
                    parent.rule.assertThat().field(field).is(RULE_ENABLED_DEFAULT);
                    break;
                case FIELD_CASCADE:
                    parent.rule.assertThat().field(field).is(RULE_CASCADE_DEFAULT);
                    break;
                case FIELD_ASYNC:
                    parent.rule.assertThat().field(field).is(RULE_ASYNC_DEFAULT);
                    break;
                case FIELD_SHARED:
                    parent.rule.assertThat().field(field).is(RULE_SHARED_DEFAULT);
                    break;
                case FIELD_TRIGGERS:
                    parent.rule.assertThat().field(field).is(ruleTriggersDefault);
                    break;
                case FIELD_ERROR_SCRIPT:
                    parent.rule.assertThat().field(field).is(RULE_ERROR_SCRIPT_DEFAULT);
                    break;
                default:
                    throw new UnsupportedOperationException("Field: " + field + " doesn't have specified default value!");
                }
                return parent;
            }
        }

        public static class FieldsAssertion
        {
            private final List<String> fields;
            private final RuleModelAssertion parent;

            private FieldsAssertion(List<String> fields, RuleModelAssertion parent)
            {
                this.fields = fields;
                this.parent = parent;
            }

            public RuleModelAssertion areEqualToDefaultValues()
            {
                if (fields.contains(FIELD_NAME))
                {
                    parent.rule.assertThat().field(FIELD_NAME).is(RULE_NAME_DEFAULT);
                    fields.remove(FIELD_NAME);
                }
                if (fields.contains(FIELD_DESCRIPTION))
                {
                    parent.rule.assertThat().field(FIELD_DESCRIPTION).is(RULE_DESCRIPTION_DEFAULT);
                    fields.remove(FIELD_DESCRIPTION);
                }
                if (fields.contains(FIELD_ENABLED))
                {
                    parent.rule.assertThat().field(FIELD_ENABLED).is(RULE_ENABLED_DEFAULT);
                    fields.remove(FIELD_ENABLED);
                }
                if (fields.contains(FIELD_CASCADE))
                {
                    parent.rule.assertThat().field(FIELD_CASCADE).is(RULE_CASCADE_DEFAULT);
                    fields.remove(FIELD_CASCADE);
                }
                if (fields.contains(FIELD_ASYNC))
                {
                    parent.rule.assertThat().field(FIELD_ASYNC).is(RULE_ASYNC_DEFAULT);
                    fields.remove(FIELD_ASYNC);
                }
                if (fields.contains(FIELD_SHARED))
                {
                    parent.rule.assertThat().field(FIELD_SHARED).is(RULE_SHARED_DEFAULT);
                    fields.remove(FIELD_SHARED);
                }
                if (fields.contains(FIELD_TRIGGERS))
                {
                    parent.rule.assertThat().field(FIELD_TRIGGERS).is(ruleTriggersDefault);
                    fields.remove(FIELD_TRIGGERS);
                }
                if (fields.contains(FIELD_ERROR_SCRIPT))
                {
                    parent.rule.assertThat().field(FIELD_ERROR_SCRIPT).is(RULE_ERROR_SCRIPT_DEFAULT);
                    fields.remove(FIELD_ERROR_SCRIPT);
                }
                if (!fields.isEmpty())
                {
                    throw new UnsupportedOperationException("Fields: " + fields + " don't have specified default values!");
                }
                return parent;
            }
        }
    }
}
