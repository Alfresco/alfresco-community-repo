/*
 * #%L
 * Alfresco Remote API
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api;

import org.alfresco.rest.api.impl.mapper.rules.RestRuleActionModelMapperTest;
import org.alfresco.rest.api.impl.mapper.rules.RestRuleCompositeConditionModelMapperTest;
import org.alfresco.rest.api.impl.mapper.rules.RestRuleModelMapperTest;
import org.alfresco.rest.api.impl.mapper.rules.RestRuleSimpleConditionModelMapperTest;
import org.alfresco.rest.api.impl.rules.ActionParameterConverterTest;
import org.alfresco.rest.api.impl.rules.ActionPermissionValidatorTest;
import org.alfresco.rest.api.impl.rules.NodeValidatorTest;
import org.alfresco.rest.api.impl.rules.RuleLoaderTest;
import org.alfresco.rest.api.impl.rules.RuleSetsImplTest;
import org.alfresco.rest.api.impl.rules.RulesImplTest;
import org.alfresco.rest.api.impl.validator.actions.ActionNodeParameterValidatorTest;
import org.alfresco.rest.api.impl.validator.actions.ActionParameterDefinitionValidatorTest;
import org.alfresco.rest.api.rules.NodeRuleSetsRelationTest;
import org.alfresco.rest.api.rules.NodeRulesRelationTest;
import org.alfresco.service.Experimental;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@Experimental
@RunWith(Suite.class)
@Suite.SuiteClasses({
    NodeRulesRelationTest.class,
    NodeRuleSetsRelationTest.class,
    RulesImplTest.class,
    RuleSetsImplTest.class,
    NodeValidatorTest.class,
    RuleLoaderTest.class,
    ActionParameterConverterTest.class,
    ActionPermissionValidatorTest.class,
    ActionParameterDefinitionValidatorTest.class,
    ActionNodeParameterValidatorTest.class,
    RestRuleSimpleConditionModelMapperTest.class,
    RestRuleCompositeConditionModelMapperTest.class,
    RestRuleActionModelMapperTest.class,
    RestRuleModelMapperTest.class
})
public class RulesUnitTests
{
}
