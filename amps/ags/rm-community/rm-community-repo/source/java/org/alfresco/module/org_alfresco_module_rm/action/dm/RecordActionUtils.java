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
package org.alfresco.module.org_alfresco_module_rm.action.dm;

import java.util.Arrays;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Utility class containing helper methods for record
 */

public class RecordActionUtils
{
    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordActionUtils.class);

    /** Private constructor to prevent instantiation. */
    private RecordActionUtils()
    {
    }

    static class Services
    {
        private NodeService nodeService;
        private FilePlanService filePlanService;
        private AuthenticationUtil authenticationUtil;

        Services(NodeService nodeService, FilePlanService filePlanService, AuthenticationUtil authenticationUtil)
        {
            this.nodeService = nodeService;
            this.filePlanService = filePlanService;
            this.authenticationUtil = authenticationUtil;
        }

        public NodeService getNodeService()
        {
            return nodeService;
        }

        public FilePlanService getFilePlanService()
        {
            return filePlanService;
        }

        public AuthenticationUtil getAuthenticationUtil()
        {
            return authenticationUtil;
        }
    }

    /**
     * Helper method to get the target record folder node reference from the action path parameter
     *
     * @param filePlan      The filePlan containing the path
     * @param pathParameter The path
     * @return The NodeRef of the resolved path
     */
    static NodeRef resolvePath(Services services, NodeRef filePlan, final String pathParameter, String actionName)
    {
        NodeRef destinationFolder;

        if (filePlan == null)
        {
            filePlan = getDefaultFilePlan(services.getAuthenticationUtil(), services.getFilePlanService(), actionName);
        }

        final String[] pathElementsArray = StringUtils.tokenizeToStringArray(pathParameter, "/", false, true);
        if (pathElementsArray.length > 0)
        {
            destinationFolder = resolvePath(services.getNodeService(), filePlan, Arrays.asList(pathElementsArray), actionName);

            // destination must be a record folder
            QName nodeType = services.getNodeService().getType(destinationFolder);
            if (!nodeType.equals(RecordsManagementModel.TYPE_RECORD_FOLDER))
            {
                throw new AlfrescoRuntimeException("Unable to execute " + actionName + " action, because the destination path is not a record folder.");
            }
        }
        else
        {
            throw new AlfrescoRuntimeException("Unable to execute " + actionName + " action, because the destination path could not be found.");
        }
        return destinationFolder;
    }

    /**
     * Helper method to recursively get the next path element node reference from the action path parameter
     *
     * @param parent       The parent of the path elements
     * @param pathElements The path elements still to be resolved
     * @return The NodeRef of the resolved path element
     */
    static NodeRef resolvePath(NodeService nodeService, NodeRef parent, List<String> pathElements, String actionName)
    {
        NodeRef nodeRef;
        String childName = pathElements.get(0);

        nodeRef = nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, childName);

        if (nodeRef == null)
        {
            throw new AlfrescoRuntimeException("Unable to execute " + actionName + " action, because the destination path could not be found.");
        }
        QName nodeType = nodeService.getType(nodeRef);
        if (nodeType.equals(RecordsManagementModel.TYPE_HOLD_CONTAINER) ||
                nodeType.equals(RecordsManagementModel.TYPE_TRANSFER_CONTAINER) ||
                nodeType.equals(RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER))
        {
            throw new AlfrescoRuntimeException("Unable to execute " + actionName + " action, because the destination path is invalid.");
        }
        if (pathElements.size() > 1)
        {
            nodeRef = resolvePath(nodeService, nodeRef, pathElements.subList(1, pathElements.size()), actionName);
        }
        return nodeRef;
    }

    /**
     * Helper method to get the default RM filePlan
     *
     * @return The NodeRef of the default RM filePlan
     */
    static NodeRef getDefaultFilePlan(AuthenticationUtil authenticationUtil, FilePlanService filePlanService, String actionName)
    {
        NodeRef filePlan = authenticationUtil.runAsSystem(() -> filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID));

        // if the file plan is still null, raise an exception
        if (filePlan == null)
        {
            final String logMessage =
                    String.format("Unable to execute %s action, because the fileplan path could not be determined. Make sure at least one file plan has been created.", actionName);
            LOGGER.debug(logMessage);
            throw new AlfrescoRuntimeException(logMessage);
        }
        return filePlan;
    }
}
