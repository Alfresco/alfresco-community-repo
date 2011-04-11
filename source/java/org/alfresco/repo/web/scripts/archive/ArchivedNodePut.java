/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.archive;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.node.archive.RestoreNodeReport;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

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

        NodeRef nodeRefToBeRestored = parseRequestForNodeRef(req);
        if (nodeRefToBeRestored == null)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "nodeRef not recognised. Could not restore.");
        }
        
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
