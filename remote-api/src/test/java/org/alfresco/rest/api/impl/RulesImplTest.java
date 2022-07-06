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

package org.alfresco.rest.api.impl;

import junit.framework.TestCase;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Rule;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@Experimental
@RunWith(MockitoJUnitRunner.class)
public class RulesImplTest extends TestCase
{

    private static final String FOLDER_NODE_ID = "dummy-node-id";
    private static final String RULE_SET_ID = "dummy-rule-set-id";
    private static final NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, FOLDER_NODE_ID);

    @Mock
    private Nodes nodes;

    @Mock
    private RuleService ruleService;

    @InjectMocks
    private RulesImpl rules;

    @Before
    @Override
    public void setUp() throws Exception
    {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetRules()
    {
        final Paging paging = Paging.DEFAULT;
        given(nodes.validateNode(any(String.class))).willReturn(nodeRef);
        given(nodes.nodeMatches(any(), any(), any())).willReturn(true);

        // when
        final CollectionWithPagingInfo<Rule> rulesPage = rules.getRules(FOLDER_NODE_ID, RULE_SET_ID, paging);

        then(nodes).should().validateNode(eq(FOLDER_NODE_ID));
        then(nodes).should().nodeMatches(eq(nodeRef), any(), isNull());
        then(nodes).shouldHaveNoMoreInteractions();
        then(ruleService).should().getRules(eq(nodeRef));
        then(ruleService).shouldHaveNoMoreInteractions();
        assertThat(rulesPage)
                .isNotNull()
            .extracting(CollectionWithPagingInfo::getCollection)
                .isNotNull();
    }

    @Test
    public void testGetRulesForNonFolderNode()
    {
        final Paging paging = Paging.DEFAULT;
        given(nodes.validateNode(any(String.class))).willReturn(nodeRef);
        given(nodes.nodeMatches(any(), any(), any())).willReturn(false);

        // when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(
            () -> rules.getRules(FOLDER_NODE_ID, RULE_SET_ID, paging));

        then(nodes).should().validateNode(eq(FOLDER_NODE_ID));
        then(nodes).should().nodeMatches(eq(nodeRef), any(), isNull());
        then(nodes).shouldHaveNoMoreInteractions();
        then(ruleService).shouldHaveNoMoreInteractions();
    }
}