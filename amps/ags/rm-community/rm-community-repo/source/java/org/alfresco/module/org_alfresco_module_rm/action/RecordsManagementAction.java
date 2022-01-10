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

package org.alfresco.module.org_alfresco_module_rm.action;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Record Management Action
 *
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public interface RecordsManagementAction
{
    /**
     * Get the name of the action
     *
     * @return  String  action name
     */
    String getName();

    /**
     * Get the label of the action
     *
     * @return  String  action label
     */
    String getLabel();

    /**
     * Get the description of the action
     *
     * @return  String  action description
     */
    String getDescription();

    /**
     * Indicates whether this is a disposition action or not
     *
     * @return  boolean     true if a disposition action, false otherwise
     */
    boolean isDispositionAction();

    /**
     * Execution of the action
     *
     * @param filePlanComponent     file plan component the action is executed upon
     * @param parameters            action parameters
     * @return The result of the executed action
     */
    RecordsManagementActionResult execute(NodeRef filePlanComponent, Map<String, Serializable> parameters);

    /**
     * Some admin-related rmActions execute against a target nodeRef which is not provided
     * by the calling code, but is instead an implementation detail of the action.
     *
     * @return the target nodeRef
     */
    NodeRef getImplicitTargetNodeRef();

    /**
     * Get the records management action definition.
     *
     * @return The records management action definition.
     * @since 2.1
     */
    RecordsManagementActionDefinition getRecordsManagementActionDefinition();

    /**
     * Indicates whether the action is public or not
     *
     * @return <code>true</code> if the action is public, <code>false</code> otherwise
     * @since 2.1
     */
    boolean isPublicAction();
}
