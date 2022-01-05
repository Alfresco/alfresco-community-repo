/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.workflow;

import org.alfresco.service.namespace.QName;

/**
 * Workflow Constants for Records Management
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public interface RMWorkflowModel
{
    // Namespace URI
    String RM_WORKFLOW_URI = "http://www.alfresco.org/model/rmworkflow/1.0";

    // Namespace prefix
    String RM_WORKFLOW_PREFIX = "rmwf";

    // Mixed Assignees
    QName RM_MIXED_ASSIGNEES = QName.createQName(RM_WORKFLOW_URI, "mixedAssignees");

    // Requested Information
    QName RM_REQUESTED_INFORMATION = QName.createQName(RM_WORKFLOW_URI, "requestedInformation");

    // Rule creator
    QName RM_RULE_CREATOR = QName.createQName(RM_WORKFLOW_URI, "ruleCreator");
}
