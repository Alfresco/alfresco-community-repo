/*
 * #%L
 * Alfresco Repository
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
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.action;

import static org.alfresco.repo.action.ActionExecutionContext.builder;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.alfresco.repo.action.ActionServiceImpl.ActionExecutionValidator;
import org.junit.Assert;
import org.junit.Test;

public class PrivateActionValidationTest
{
    @Test
    public void shouldFailOnNullContext()
    {
        final ActionExecutionValidator validator = givenActionExecutionValidator(Map.of(), Set.of());

        try
        {
            validator.isExposed(null);
        }
        catch (NullPointerException e)
        {
            assertNotNull(e);
            return;
        }
        fail("Expected NPE.");
    }

    @Test
    public void privateActionShouldNotBeExposedByDefault()
    {
        final ActionExecutionValidator validator = givenActionExecutionValidator(Map.of(), Set.of());

        Assert.assertFalse(validator.isExposed(getAEC("privateA")));
        Assert.assertFalse(validator.isExposed(getAEC("privateA", "test")));
    }

    @Test
    public void publicActionShouldBeExposedByDefault()
    {
        final ActionExecutionValidator validator = givenActionExecutionValidator(Map.of(), Set.of("publicA"));

        Assert.assertTrue(validator.isExposed(getAEC("publicA")));
        Assert.assertTrue(validator.isExposed(getAEC("publicA", "test")));
    }

    @Test
    public void privateActionShouldBeExposedByConfigurationBasedOnActionId()
    {
        final ActionExecutionValidator validator = givenActionExecutionValidator(Map.of(
                "org.alfresco.repo.action.privateA.exposed", "true"), Set.of());

        Assert.assertTrue(validator.isExposed(getAEC("privateA")));
        Assert.assertTrue(validator.isExposed(getAEC("privateA", "test")));
        Assert.assertTrue(validator.isExposed(getAEC("privateA", "test2")));

        Assert.assertFalse(validator.isExposed(getAEC("privateB")));
        Assert.assertFalse(validator.isExposed(getAEC("privateB", "test")));
        Assert.assertFalse(validator.isExposed(getAEC("privateB", "test2")));
    }

    @Test
    public void privateActionShouldBeExposedByConfigurationBasedOnActionIdAndExecutionSource()
    {
        final ActionExecutionValidator validator = givenActionExecutionValidator(Map.of(
                "org.alfresco.repo.action.test.privateA.exposed", "true"), Set.of());

        Assert.assertFalse(validator.isExposed(getAEC("privateA")));
        Assert.assertTrue(validator.isExposed(getAEC("privateA", "test")));
        Assert.assertFalse(validator.isExposed(getAEC("privateA", "test2")));

        Assert.assertFalse(validator.isExposed(getAEC("privateB")));
        Assert.assertFalse(validator.isExposed(getAEC("privateB", "test")));
        Assert.assertFalse(validator.isExposed(getAEC("privateB", "test2")));
    }

    @Test
    public void executionSourceConfigurationShouldTakePrecedenceOverGeneralConfigurationForPrivateAction()
    {
        final ActionExecutionValidator validator = givenActionExecutionValidator(Map.of(
                "org.alfresco.repo.action.test.privateA.exposed", "true",
                "org.alfresco.repo.action.privateA.exposed", "false"), Set.of());

        Assert.assertFalse(validator.isExposed(getAEC("privateA")));
        Assert.assertTrue(validator.isExposed(getAEC("privateA", "test")));
    }

    @Test
    public void publicActionShouldNotBeExposedByConfigurationBasedOnActionId()
    {
        final ActionExecutionValidator validator = givenActionExecutionValidator(Map.of(
                "org.alfresco.repo.action.publicA.exposed", "false"), Set.of("publicA"));

        Assert.assertFalse(validator.isExposed(getAEC("publicA")));
        Assert.assertFalse(validator.isExposed(getAEC("publicA", "test")));
    }

    @Test
    public void publicActionShouldNotBeExposedByConfigurationBasedOnActionIdAndExecutionSource()
    {
        final ActionExecutionValidator validator = givenActionExecutionValidator(Map.of(
                "org.alfresco.repo.action.test.publicA.exposed", "false"), Set.of("publicA"));

        Assert.assertTrue(validator.isExposed(getAEC("publicA")));
        Assert.assertFalse(validator.isExposed(getAEC("publicA", "test")));
    }

    @Test
    public void executionSourceConfigurationShouldTakePrecedenceOverGeneralConfigurationForPublicAction()
    {
        final ActionExecutionValidator validator = givenActionExecutionValidator(Map.of(
                "org.alfresco.repo.action.test.publicA.exposed", "false",
                "org.alfresco.repo.action.publicA.exposed", "true"), Set.of("publicA"));

        Assert.assertTrue(validator.isExposed(getAEC("publicA")));
        Assert.assertFalse(validator.isExposed(getAEC("publicA", "test")));
    }

    private ActionExecutionContext getAEC(String actionName) {
        return getAEC(actionName, null);
    }

    private ActionExecutionContext getAEC(String actionName, String executionSource) {
        Assert.assertNotNull("Action name can't be empty", actionName);
        ActionExecutionContext.Builder builder = builder(actionName);
        if (executionSource != null) {
            builder.withExecutionSource(executionSource);
        }
        return builder.build();
    }

    private ActionExecutionValidator givenActionExecutionValidator(Map<String, String> configuration, Set<String> publicActions)
    {
        Predicate<ActionExecutionContext> isPublic = aec -> publicActions.contains(aec.getActionId());
        return new ActionExecutionValidator(configuration::get, isPublic);
    }
}
