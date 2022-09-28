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
package org.alfresco.service.cmr.workflow;

import java.util.Optional;

/**
 * The goal of this class is to provide a support for workflow deployment failure. Since {@link WorkflowDeployment} is
 * part of the public API we don't want to change it.
 */
public final class FailedWorkflowDeployment
{
    private FailedWorkflowDeployment()
    {
        //no instantiation
    }

    public static WorkflowDeployment deploymentForbidden(String workflowName, String reason)
    {
        return new DeploymentFailure(workflowName, reason);
    }

    public static Optional<String> getFailure(WorkflowDeployment workflowDeployment)
    {
        if (!(workflowDeployment instanceof DeploymentFailure))
        {
            return Optional.empty();
        }

        return Optional.of(workflowDeployment.getProblems()[0]);
    }

    private static class DeploymentFailure extends WorkflowDeployment
    {
        private static final String UNDEFINED = "undefined";

        private DeploymentFailure(String workflowName, String problemDescription)
        {
            super(failedDefinition(workflowName), problemDescription);
        }

        private static WorkflowDefinition failedDefinition(String workflowName)
        {
            final String definitionName = workflowName == null ? UNDEFINED : workflowName;
            return new WorkflowDefinition(UNDEFINED, definitionName, UNDEFINED, UNDEFINED, UNDEFINED, null);
        }
    }
}
