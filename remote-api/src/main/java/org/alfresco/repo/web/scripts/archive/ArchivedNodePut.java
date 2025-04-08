/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.repo.web.scripts.archive;

import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.repo.node.archive.RestoreNodeReport;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This class is the controller for the archivednode.put webscript.
 * 
 * @author Neil Mc Erlean
 * @since 3.5
 */
public class ArchivedNodePut extends AbstractArchivedNodeWebScript
{
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        // Current user
        String userID = AuthenticationUtil.getFullyAuthenticatedUser();
        if (userID == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_UNAUTHORIZED, "Web Script ["
                    + req.getServiceMatch().getWebScript().getDescription()
                    + "] requires user authentication.");
        }

        NodeRef nodeRefToBeRestored = parseRequestForNodeRef(req);
        if (nodeRefToBeRestored == null)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "nodeRef not recognised. Could not restore.");
        }

        // check if the current user has the permission to restore the node
        validatePermission(nodeRefToBeRestored, userID);

        RestoreNodeReport report = nodeArchiveService.restoreArchivedNode(nodeRefToBeRestored);

        // Handling of some error scenarios
        if (report.getStatus().equals(RestoreNodeReport.RestoreStatus.FAILURE_INVALID_ARCHIVE_NODE))
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find archive node: " + nodeRefToBeRestored);
        }
        else if (report.getStatus().equals(RestoreNodeReport.RestoreStatus.FAILURE_PERMISSION))
        {
            throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, "Unable to restore archive node: " + nodeRefToBeRestored);
        }
        else if (report.getStatus().equals(RestoreNodeReport.RestoreStatus.FAILURE_DUPLICATE_CHILD_NODE_NAME))
        {
            throw new WebScriptException(HttpServletResponse.SC_CONFLICT, "Unable to restore archive node: " + nodeRefToBeRestored + ". Duplicate child node name");
        }
        else if (report.getStatus().equals(RestoreNodeReport.RestoreStatus.FAILURE_INVALID_PARENT) ||
                report.getStatus().equals(RestoreNodeReport.RestoreStatus.FAILURE_INTEGRITY) ||
                report.getStatus().equals(RestoreNodeReport.RestoreStatus.FAILURE_OTHER))
        {
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to restore archive node: " + nodeRefToBeRestored);
        }

        model.put("restoreNodeReport", report);
        return model;
    }
}
