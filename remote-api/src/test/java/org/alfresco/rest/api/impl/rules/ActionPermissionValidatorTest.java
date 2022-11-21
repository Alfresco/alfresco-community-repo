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

import static org.alfresco.service.cmr.rule.RuleType.INBOUND;
import static org.alfresco.service.cmr.rule.RuleType.OUTBOUND;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.then;

import java.util.List;

import junit.framework.TestCase;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.action.CompositeActionImpl;
import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.repo.action.executer.CheckOutActionExecuter;
import org.alfresco.repo.action.executer.CopyActionExecuter;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.Rule;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@Experimental
@RunWith(MockitoJUnitRunner.class)
public class ActionPermissionValidatorTest extends TestCase
{
    private static final String DUMMY_NODE_ID = "dummy-node-id";
    @Mock
    private RuntimeActionService runtimeActionService;

    @InjectMocks
    private ActionPermissionValidator objectUnderTest;

    @Test
    public void testPositiveRulePermissionValidation()
    {
        //given
        final CompositeActionImpl compositeAction =
                new CompositeActionImpl(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, DUMMY_NODE_ID), "composite-id");
        final Action action1 = new ActionImpl(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, DUMMY_NODE_ID), "id-1",
                CopyActionExecuter.NAME);
        compositeAction.addAction(action1);
        final Action action2 = new ActionImpl(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, DUMMY_NODE_ID), "id-2",
                CheckOutActionExecuter.NAME);
        compositeAction.addAction(action2);
        final Rule inputRule = new Rule();
        inputRule.setAction(compositeAction);
        inputRule.setRuleTypes(List.of(INBOUND));

        //when
        final Rule validatedRule = objectUnderTest.validateRulePermissions(inputRule);

        then(runtimeActionService).should().verifyActionAccessRestrictions(action1);
        then(runtimeActionService).should().verifyActionAccessRestrictions(action2);
        then(runtimeActionService).shouldHaveNoMoreInteractions();

        ((CompositeActionImpl) validatedRule.getAction()).getActions()
                .forEach(action -> Assertions.assertThat(action.getParameterValue("actionContext")).isEqualTo("rule"));
    }

    @Test
    public void testNegativeRulePermissionValidation()
    {
        //given
        final CompositeActionImpl compositeAction =
                new CompositeActionImpl(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, DUMMY_NODE_ID), "composite-id");
        final Action action1 = new ActionImpl(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, DUMMY_NODE_ID), "id-1",
                CopyActionExecuter.NAME);
        compositeAction.addAction(action1);
        final Action action2 = new ActionImpl(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, DUMMY_NODE_ID), "id-2",
                CheckOutActionExecuter.NAME);
        compositeAction.addAction(action2);
        final Rule inputRule = new Rule();
        inputRule.setAction(compositeAction);
        inputRule.setRuleTypes(List.of(OUTBOUND));

        //when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(() -> objectUnderTest.validateRulePermissions(inputRule));

        then(runtimeActionService).should().verifyActionAccessRestrictions(action1);
        then(runtimeActionService).should().verifyActionAccessRestrictions(action2);
        then(runtimeActionService).shouldHaveNoMoreInteractions();
    }

}
