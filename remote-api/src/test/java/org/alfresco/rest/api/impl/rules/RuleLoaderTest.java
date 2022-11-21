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
package org.alfresco.rest.api.impl.rules;

import static java.util.Collections.emptyList;

import static org.alfresco.rest.api.impl.rules.RuleLoader.IS_SHARED;
import static org.alfresco.rest.api.model.rules.RuleTrigger.OUTBOUND;
import static org.alfresco.rest.api.model.rules.RuleTrigger.UPDATE;
import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;

import org.alfresco.rest.api.model.mapper.RestModelMapper;
import org.alfresco.rest.api.model.rules.Rule;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Unit tests for {@link RuleLoader}. */
@RunWith(MockitoJUnitRunner.class)
public class RuleLoaderTest
{
    private static final String NODE_ID = "node-id";
    private static final NodeRef NODE_REF = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, NODE_ID);
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final boolean ENABLED = true;
    private static final boolean INHERITABLE = true;
    private static final boolean EXECUTE_ASYNCHRONOUSLY = false;
    private static final List<String> TRIGGERS = List.of("update", "outbound");
    private static final NodeRef RULE_SET_NODE = new NodeRef("rule://set/");

    @Mock
    private RuleService ruleServiceMock;
    @Mock
    private NodeValidator nodeValidatorMock;
    @Mock
    private RestModelMapper<Rule, org.alfresco.service.cmr.rule.Rule> ruleMapperMock;

    private org.alfresco.service.cmr.rule.Rule serviceRule = createServiceRule();

    @InjectMocks
    private RuleLoader ruleLoader;

    @Test
    public void testLoadRule()
    {
        final Rule restModelRule = getRestModelRule();
        given(ruleMapperMock.toRestModel(serviceRule)).willReturn(restModelRule);

        //when
        Rule rule = ruleLoader.loadRule(serviceRule, emptyList());

        Rule expected = Rule.builder().id(NODE_ID)
                            .name(TITLE)
                            .description(DESCRIPTION)
                            .isEnabled(ENABLED)
                            .isInheritable(INHERITABLE)
                            .isAsynchronous(EXECUTE_ASYNCHRONOUSLY)
                            .triggers(List.of(UPDATE, OUTBOUND)).create();
        assertThat(rule).isEqualTo(expected);
    }

    @Test
    public void testLoadRule_noExceptionWithNullInclude()
    {
        //when
        ruleLoader.loadRule(serviceRule, null);

        then(ruleMapperMock).should().toRestModel(serviceRule);
        then(ruleMapperMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testLoadRule_includeIsShared()
    {
        // Simulate the rule set being shared.
        final Rule restModelRule = getRestModelRule();
        given(ruleServiceMock.getRuleSetNode(NODE_REF)).willReturn(RULE_SET_NODE);
        given(nodeValidatorMock.isRuleSetNotNullAndShared(RULE_SET_NODE)).willReturn(true);
        given(ruleMapperMock.toRestModel(serviceRule)).willReturn(restModelRule);

        Rule rule = ruleLoader.loadRule(serviceRule, List.of(IS_SHARED));

        then(ruleMapperMock).should().toRestModel(serviceRule);
        then(ruleMapperMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().getRuleSetNode(NODE_REF);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        then(nodeValidatorMock).should().isRuleSetNotNullAndShared(RULE_SET_NODE);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();
        assertThat(rule).extracting("isShared").isEqualTo(true);
    }

    private org.alfresco.service.cmr.rule.Rule createServiceRule()
    {
        org.alfresco.service.cmr.rule.Rule rule = new org.alfresco.service.cmr.rule.Rule();
        rule.setNodeRef(NODE_REF);
        rule.setTitle(TITLE);
        rule.setDescription(DESCRIPTION);
        rule.setRuleDisabled(!ENABLED);
        rule.applyToChildren(INHERITABLE);
        rule.setExecuteAsynchronously(EXECUTE_ASYNCHRONOUSLY);
        rule.setRuleTypes(TRIGGERS);
        return rule;
    }

    private Rule getRestModelRule()
    {
        return Rule.builder().id(NODE_ID)
                .name(TITLE)
                .description(DESCRIPTION)
                .isEnabled(ENABLED)
                .isInheritable(INHERITABLE)
                .isAsynchronous(EXECUTE_ASYNCHRONOUSLY)
                .triggers(List.of(UPDATE, OUTBOUND)).create();
    }

}
