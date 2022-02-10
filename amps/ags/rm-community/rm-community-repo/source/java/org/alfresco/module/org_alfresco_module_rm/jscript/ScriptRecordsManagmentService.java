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

package org.alfresco.module.org_alfresco_module_rm.jscript;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.notification.RecordsManagementNotificationHelper;
import org.alfresco.module.org_alfresco_module_rm.security.FilePlanPermissionService;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.scripts.ScriptException;

/**
 * Records management service
 *
 * @author Roy Wetherall
 */
public class ScriptRecordsManagmentService extends BaseScopableProcessorExtension
                                           implements RecordsManagementModel
{
    /** Records management service registry */
    private RecordsManagementServiceRegistry rmServices;

    /** Records management notification helper */
    private RecordsManagementNotificationHelper notificationHelper;

    /**
     * Set records management service registry
     *
     * @param rmServices    records management service registry
     */
    public void setRecordsManagementServiceRegistry(RecordsManagementServiceRegistry rmServices)
    {
        this.rmServices = rmServices;
    }

    /**
     * Sets the notification helper
     *
     * @param notificationHelper    notification helper
     */
    public void setNotificationHelper(RecordsManagementNotificationHelper notificationHelper)
    {
        this.notificationHelper = notificationHelper;
    }

    /**
     * Get records management node
     *
     * @param node                          script node
     * @return ScriptRecordsManagementNode  records management script node
     */
    public ScriptRecordsManagmentNode getRecordsManagementNode(ScriptNode node)
    {
        ScriptRecordsManagmentNode result = null;

        if (rmServices.getNodeService().hasAspect(node.getNodeRef(), ASPECT_FILE_PLAN_COMPONENT))
        {
            // TODO .. at this point determine what type of records management node is it and
            //         create the appropriate sub-type
            result = new ScriptRecordsManagmentNode(node.getNodeRef(), rmServices);
        }
        else
        {
            throw new ScriptException("Node is not a records management node type.");
        }

        return result;
    }

    /**
     * Set the RM permission
     *
     * @param node
     * @param permission
     * @param authority
     */
    public void setPermission(ScriptNode node, String permission, String authority)
    {
        FilePlanPermissionService filePlanPermissionService = rmServices.getFilePlanPermissionService();
        filePlanPermissionService.setPermission(node.getNodeRef(), authority, permission);
    }

    /**
     * Delete the RM permission
     *
     * @param node
     * @param permission
     * @param authority
     */
    public void deletePermission(ScriptNode node, String permission, String authority)
    {
        FilePlanPermissionService filePlanPermissionService = rmServices.getFilePlanPermissionService();
        filePlanPermissionService.deletePermission(node.getNodeRef(), authority, permission);
    }

    /**
     * Send superseded notification
     *
     * @param record    superseded record
     */
    public void sendSupersededNotification(ScriptNode record)
    {
        notificationHelper.recordSupersededEmailNotification(record.getNodeRef());
    }
}
