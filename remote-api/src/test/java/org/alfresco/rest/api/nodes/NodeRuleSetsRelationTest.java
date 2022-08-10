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

    @Mock
    private Rules rules;

    @Mock
    private Parameters parameters;

    @InjectMocks
    private NodeRuleSetLinksRelation nodeRuleSetLinksRelation;

    @Test
    public void shouldProperlyCreateLink()
    {
        RuleSetLink requestBody = new RuleSetLink();
        RuleSetLink expectedRuleSetLink = new RuleSetLink();
        List<RuleSetLink> expectedRuleResult = List.of(expectedRuleSetLink);


        when(rules.linkToRuleSet(FOLDER_NODE_ID, LINK_TO_NODE_ID)).thenReturn(expectedRuleSetLink);

        requestBody.setId(LINK_TO_NODE_ID);
        expectedRuleResult.get(0).setId("dummy-ruleset-id");

        List<RuleSetLink> actual = nodeRuleSetLinksRelation.create(FOLDER_NODE_ID,List.of(requestBody), parameters);
        Assert.assertEquals(expectedRuleResult.get(0).getId(), actual.get(0).getId());
    }


}
