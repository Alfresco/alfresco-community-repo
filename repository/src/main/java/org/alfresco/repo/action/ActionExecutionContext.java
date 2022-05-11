/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2022 Alfresco Software Limited
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

import java.util.Objects;

/**
 * Instances of this class are responsible for holding an action id with additional data used to identify the action's
 * execution context like:
 * <ul>
 *     <li>REST API</li>
 *     <li>rules execution</li>
 *     <li>...</li>
 * </ul>
 */
public class ActionExecutionContext
{
    public static final String RULES_CONTEXT = "rules";

    private final String actionId;
    private final String executionSource;

    private ActionExecutionContext(String actionId, String executionSource)
    {
        this.actionId = actionId;
        this.executionSource = executionSource;
    }

    String getActionId()
    {
        return actionId;
    }

    String getExecutionSource()
    {
        return executionSource;
    }

    boolean isExecutionSourceKnown()
    {
        return Objects.nonNull(executionSource);
    }

    public static Builder builder(final String actionId)
    {
        Objects.requireNonNull(actionId);
        return new Builder(actionId);
    }

    public static class Builder
    {
        private final String actionId;
        private String executionSource;

        private Builder(String actionId)
        {
            this.actionId = actionId;
        }

        public ActionExecutionContext build()
        {
            return new ActionExecutionContext(actionId, executionSource);
        }

        public Builder withExecutionSource(final String executionSource)
        {
            Objects.requireNonNull(executionSource);
            this.executionSource = executionSource;
            return this;
        }
    }
}
