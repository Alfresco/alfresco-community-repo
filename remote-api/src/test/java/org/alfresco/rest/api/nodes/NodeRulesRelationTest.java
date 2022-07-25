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

package org.alfresco.rest.api.nodes;

import junit.framework.TestCase;
import org.alfresco.rest.api.Rules;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.tests.core.ParamsExtender;
import org.alfresco.service.Experimental;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

@Experimental
@RunWith(MockitoJUnitRunner.class)
public class NodeRulesRelationTest extends TestCase
{

    private static final String FOLDER_NODE_ID = "dummy-node-id";
    private static final String RULE_SET_ID = "dummy-rule-set-id";
    private static final String RULE_ID = "dummy-rule-id";

    @Mock
    private Rules rulesMock;

    @InjectMocks
    private NodeRulesRelation nodeRulesRelation;

    @Override
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testReadAll()
    {
        final Paging paging = Paging.DEFAULT;
        final Parameters parameters = ParamsExtender.valueOf(paging, FOLDER_NODE_ID, RULE_SET_ID, null);

        // when
        nodeRulesRelation.readAll(FOLDER_NODE_ID, parameters);

        then(rulesMock).should().getRules(eq(FOLDER_NODE_ID), eq(RULE_SET_ID), eq(paging));
        then(rulesMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testReadById()
    {
        final Parameters parameters = ParamsExtender.valueOf(null, FOLDER_NODE_ID, RULE_SET_ID, RULE_ID);

        // when
        nodeRulesRelation.readById(FOLDER_NODE_ID, RULE_SET_ID, parameters);

        then(rulesMock).should().getRuleById(eq(FOLDER_NODE_ID), eq(RULE_SET_ID), eq(RULE_ID));
        then(rulesMock).shouldHaveNoMoreInteractions();
    }
}