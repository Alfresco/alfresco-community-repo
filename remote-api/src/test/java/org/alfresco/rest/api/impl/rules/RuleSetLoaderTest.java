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

import static org.mockito.BDDMockito.given;

import java.util.List;

import junit.framework.TestCase;
import org.alfresco.rest.api.model.rules.RuleSet;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
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
    private static final NodeRef FOLDER_NODE = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, FOLDER_ID);
    private static final NodeRef RULE_SET_NODE = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, RULE_SET_ID);

    @InjectMocks
    private RuleSetLoader ruleSetLoader;
    @Mock
    private NodeService nodeServiceMock;
    @Mock
    private ChildAssociationRef ruleSetAssociationMock;

    @Before
    @Override
    public void setUp()
    {
        given(ruleSetAssociationMock.getParentRef()).willReturn(FOLDER_NODE);
        given(nodeServiceMock.getPrimaryParent(RULE_SET_NODE)).willReturn(ruleSetAssociationMock);
    }

    @Test
    public void testLoadRuleSet_noIncludes()
    {
        // Call the method under test.
        RuleSet actual = ruleSetLoader.loadRuleSet(RULE_SET_NODE, null);

        RuleSet expected = RuleSet.builder().id(RULE_SET_ID).create();
        assertEquals(expected, actual);
    }

    @Test
    public void testLoadRuleSet_includeOwningFolder()
    {
        // Call the method under test.
        RuleSet actual = ruleSetLoader.loadRuleSet(RULE_SET_NODE, List.of("owningFolder"));

        RuleSet expected = RuleSet.builder().id(RULE_SET_ID).owningFolder(FOLDER_NODE).create();
        assertEquals(expected, actual);
    }
}
