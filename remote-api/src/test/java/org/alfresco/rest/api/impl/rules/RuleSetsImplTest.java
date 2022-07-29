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

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;
import org.alfresco.rest.api.model.rules.RuleSet;
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

/** Unit tests for {@link RuleSetsImpl}. */
@Experimental
@RunWith (MockitoJUnitRunner.class)
public class RuleSetsImplTest extends TestCase
{
    private static final String FOLDER_ID = "dummy-folder-id";
    private static final String RULE_SET_ID = "dummy-rule-set-id";
    private static final NodeRef FOLDER_NODE = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, FOLDER_ID);
    private static final NodeRef RULE_SET_NODE = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, RULE_SET_ID);
    private static final Paging PAGING = Paging.DEFAULT;

    @InjectMocks
    private RuleSetsImpl ruleSets;
    @Mock
    private NodeValidator nodeValidatorMock;
    @Mock
    private RuleService ruleServiceMock;

    @Before
    @Override
    public void setUp()
    {
        MockitoAnnotations.openMocks(this);

        given(nodeValidatorMock.validateFolderNode(eq(FOLDER_ID), anyBoolean())).willReturn(FOLDER_NODE);
        //given(nodeValidatorMock.validateFolderNode(eq(RULE_SET_ID), anyBoolean())).willReturn(RULE_SET_NODE);
        given(nodeValidatorMock.validateRuleSetNode(RULE_SET_ID, FOLDER_NODE)).willReturn(RULE_SET_NODE);

        given(ruleServiceMock.getRuleSetNode(FOLDER_NODE)).willReturn(RULE_SET_NODE);
    }

    @Test
    public void testGetRuleSets()
    {
        // Call the method under test.
        CollectionWithPagingInfo<RuleSet> actual = ruleSets.getRuleSets(FOLDER_ID, null, PAGING);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_ID, false);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();

        then(ruleServiceMock).should().getRuleSetNode(FOLDER_NODE);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();

        Collection<RuleSet> expected = List.of(RuleSet.of(RULE_SET_ID));
        assertEquals(expected, actual.getCollection());
        assertEquals(PAGING, actual.getPaging());
    }

    @Test
    public void testGetZeroRuleSets()
    {
        // Simulate no rule sets for the folder.
        given(ruleServiceMock.getRuleSetNode(FOLDER_NODE)).willReturn(null);

        // Call the method under test.
        CollectionWithPagingInfo<RuleSet> actual = ruleSets.getRuleSets(FOLDER_ID, null, PAGING);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_ID, false);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();

        then(ruleServiceMock).should().getRuleSetNode(FOLDER_NODE);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();

        assertEquals(emptyList(), actual.getCollection());
        assertEquals(PAGING, actual.getPaging());
    }

    @Test
    public void testGetRuleSetById()
    {
        // Call the method under test.
        RuleSet actual = ruleSets.getRuleSetById(FOLDER_ID, RULE_SET_ID, null);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_ID, false);
        then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, FOLDER_NODE);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();

        assertEquals(RuleSet.of(RULE_SET_ID), actual);
    }
}
