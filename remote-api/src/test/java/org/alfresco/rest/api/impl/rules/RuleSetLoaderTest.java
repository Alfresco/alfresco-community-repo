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

import static org.alfresco.rest.api.impl.rules.RuleSetLoader.INCLUSION_TYPE;
import static org.alfresco.rest.api.impl.rules.RuleSetLoader.INHERITED_BY;
import static org.alfresco.rest.api.impl.rules.RuleSetLoader.IS_INHERITED;
import static org.alfresco.rest.api.impl.rules.RuleSetLoader.IS_LINKED_TO;
import static org.alfresco.rest.api.impl.rules.RuleSetLoader.LINKED_TO_BY;
import static org.alfresco.rest.api.impl.rules.RuleSetLoader.OWNING_FOLDER;
import static org.alfresco.rest.api.model.rules.InclusionType.INHERITED;
import static org.alfresco.rest.api.model.rules.InclusionType.LINKED;
import static org.alfresco.rest.api.model.rules.InclusionType.OWNED;
import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.List;

import junit.framework.TestCase;
import org.alfresco.rest.api.model.rules.RuleSet;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Unit tests for {@link RuleSetLoader}. */
@Experimental
@RunWith (MockitoJUnitRunner.class)
public class RuleSetLoaderTest extends TestCase
{
    private static final String FOLDER_ID = "dummy-folder-id";
    private static final String RULE_SET_ID = "dummy-rule-set-id";
    private static final NodeRef FOLDER_NODE = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, FOLDER_ID);
    private static final NodeRef RULE_SET_NODE = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, RULE_SET_ID);
    private static final String LINKING_FOLDER_ID = "linking-folder";
    private static final NodeRef LINKING_FOLDER = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, LINKING_FOLDER_ID);
    private static final NodeRef INHERITING_FOLDER = new NodeRef("inheriting://folder/");

    @InjectMocks
    private RuleSetLoader ruleSetLoader;
    @Mock
    private NodeService nodeServiceMock;
    @Mock
    private RuleService ruleServiceMock;
    @Mock
    private ChildAssociationRef ruleSetAssociationMock;
    @Mock
    private ChildAssociationRef linkAssociationMock;

    @Before
    @Override
    public void setUp()
    {
        given(ruleSetAssociationMock.getParentRef()).willReturn(FOLDER_NODE);
        given(nodeServiceMock.getPrimaryParent(RULE_SET_NODE)).willReturn(ruleSetAssociationMock);

        given(linkAssociationMock.getParentRef()).willReturn(LINKING_FOLDER);
        given(nodeServiceMock.getParentAssocs(RULE_SET_NODE)).willReturn(List.of(ruleSetAssociationMock, linkAssociationMock));

        given(ruleServiceMock.getFoldersInheritingRuleSet(eq(RULE_SET_NODE), anyInt())).willReturn(List.of(INHERITING_FOLDER));
        given(ruleServiceMock.getFoldersLinkingToRuleSet(eq(RULE_SET_NODE), anyInt())).willReturn(List.of(LINKING_FOLDER));
    }

    @Test
    public void testLoadRuleSet_noIncludes()
    {
        // Call the method under test.
        RuleSet actual = ruleSetLoader.loadRuleSet(RULE_SET_NODE, FOLDER_NODE, null);

        RuleSet expected = RuleSet.builder().id(RULE_SET_ID).create();
        assertEquals(expected, actual);
    }

    @Test
    public void testLoadRuleSet_includeOwningFolder()
    {
        // Call the method under test.
        RuleSet actual = ruleSetLoader.loadRuleSet(RULE_SET_NODE, FOLDER_NODE, List.of(OWNING_FOLDER));

        RuleSet expected = RuleSet.builder().id(RULE_SET_ID).owningFolder(FOLDER_NODE).create();
        assertEquals(expected, actual);
    }

    @Test
    public void testLoadRuleSet_includeInclusionType()
    {
        // Call the method under test.
        RuleSet actual = ruleSetLoader.loadRuleSet(RULE_SET_NODE, FOLDER_NODE, List.of(INCLUSION_TYPE));

        RuleSet expected = RuleSet.builder().id(RULE_SET_ID).inclusionType(OWNED).create();
        assertEquals(expected, actual);
    }

    @Test
    public void testLoadRuleSet_linkedInclusionType()
    {
        // Call the method under test.
        RuleSet actual = ruleSetLoader.loadRuleSet(RULE_SET_NODE, LINKING_FOLDER, List.of(INCLUSION_TYPE));

        RuleSet expected = RuleSet.builder().id(RULE_SET_ID).inclusionType(LINKED).create();
        assertEquals(expected, actual);
    }

    @Test
    public void testLoadRuleSet_inheritedInclusionType()
    {
        // Call the method under test.
        RuleSet actual = ruleSetLoader.loadRuleSet(RULE_SET_NODE, INHERITING_FOLDER, List.of(INCLUSION_TYPE));

        RuleSet expected = RuleSet.builder().id(RULE_SET_ID).inclusionType(INHERITED).create();
        assertEquals(expected, actual);
    }

    @Test
    public void testLoadRuleSet_inheritedBy()
    {
        // Call the method under test.
        RuleSet actual = ruleSetLoader.loadRuleSet(RULE_SET_NODE, INHERITING_FOLDER, List.of(INHERITED_BY));

        RuleSet expected = RuleSet.builder().id(RULE_SET_ID).inheritedBy(List.of(INHERITING_FOLDER)).create();
        assertEquals(expected, actual);
    }

    @Test
    public void testLoadRuleSet_linkedToBy()
    {
        // Call the method under test.
        RuleSet actual = ruleSetLoader.loadRuleSet(RULE_SET_NODE, FOLDER_NODE, List.of(LINKED_TO_BY));

        RuleSet expected = RuleSet.builder().id(RULE_SET_ID).linkedToBy(List.of(LINKING_FOLDER)).create();
        assertEquals(expected, actual);
    }

    @Test
    public void testLoadRuleSet_isInherited()
    {
        // Call the method under test.
        RuleSet actual = ruleSetLoader.loadRuleSet(RULE_SET_NODE, FOLDER_NODE, List.of(IS_INHERITED));

        RuleSet expected = RuleSet.builder().id(RULE_SET_ID).isInherited(true).create();
        assertEquals(expected, actual);
    }

    @Test
    public void testLoadRuleSet_isLinkedTo()
    {
        // Call the method under test.
        RuleSet actual = ruleSetLoader.loadRuleSet(RULE_SET_NODE, FOLDER_NODE, List.of(IS_LINKED_TO));

        RuleSet expected = RuleSet.builder().id(RULE_SET_ID).isLinkedTo(true).create();
        assertEquals(expected, actual);
    }
}
