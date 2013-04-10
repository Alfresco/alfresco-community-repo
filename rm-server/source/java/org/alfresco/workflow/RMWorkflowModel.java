/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
    public static String RM_WORKFLOW_URI = "http://www.alfresco.org/model/rmworkflow/1.0";

    // Namespace prefix
    public static String RM_WORKFLOW_PREFIX = "rmwf";

    // Mixed Assignees
    public static QName RM_MIXED_ASSIGNEES = QName.createQName(RM_WORKFLOW_URI, "mixedAssignees");

    // Requested Information
    public static QName RM_REQUESTED_INFORMATION = QName.createQName(RM_WORKFLOW_URI, "requestedInformation");
}
