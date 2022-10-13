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

package org.alfresco.rest.api.rules;

import static org.mockito.BDDMockito.then;

import junit.framework.TestCase;
import org.alfresco.rest.api.RuleSets;
import org.alfresco.rest.api.model.rules.RuleSetLink;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NodeRuleSetsRelationTest extends TestCase
{
    private static final String FOLDER_NODE_ID = "dummy-folder-node-id";
    private static final String LINK_TO_NODE_ID = "dummy-link-to-node-id";
    private static final String RULE_SET_NODE_ID = "dummy-rule-set-node-id";

    @Mock
    private RuleSets ruleSets;

    @Mock
    private Parameters parameters;

    @InjectMocks
    private NodeRuleSetLinksRelation nodeRuleSetLinksRelation;

    @Test
    public void shouldProperlyCreateLink()
    {
        RuleSetLink ruleSetLink = new RuleSetLink();
        List<RuleSetLink> ruleResult = List.of(ruleSetLink);

        RuleSetLink requestBody = new RuleSetLink();
        requestBody.setId(LINK_TO_NODE_ID);

        when(ruleSets.linkToRuleSet(FOLDER_NODE_ID, LINK_TO_NODE_ID)).thenReturn(ruleSetLink);

        List<RuleSetLink> actual = nodeRuleSetLinksRelation.create(FOLDER_NODE_ID,List.of(requestBody), parameters);
        Assert.assertEquals(ruleResult, actual);
    }

    @Test
    public void testUnlinkRuleSet()
    {
        //when
        ruleSets.unlinkRuleSet(FOLDER_NODE_ID,RULE_SET_NODE_ID);

        then(ruleSets).should().unlinkRuleSet(FOLDER_NODE_ID,RULE_SET_NODE_ID);
        then(ruleSets).shouldHaveNoMoreInteractions();
    }


}
