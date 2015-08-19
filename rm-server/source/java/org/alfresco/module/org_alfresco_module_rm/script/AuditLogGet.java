/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.script;

import java.io.File;
import java.io.IOException;

import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditQueryParameters;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService.ReportFormat;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.repo.web.scripts.content.ContentStreamer;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Implementation for Java backed webscript to return audit
 * log of RM events, optionally scoped to an RM node.
 *
 * @author Gavin Cornwell
 */
public class AuditLogGet extends BaseAuditRetrievalWebScript
{
    /** Logger */
    private static Log logger = LogFactory.getLog(AuditLogGet.class);

    private static final String PARAM_EXPORT = "export";
    private static final String ACCESS_AUDIT_CAPABILITY = "AccessAudit";

    /** Content Streamer */
    protected ContentStreamer contentStreamer;
    
    /** Capability service */
    protected CapabilityService capabilityService;

    /** File plan service */
    protected FilePlanService filePlanService;

    /**
     * @param contentStreamer
     */
    public void setContentStreamer(ContentStreamer contentStreamer)
    {
        this.contentStreamer = contentStreamer;
    }
    
    /**
     * 
     * @param capabilityService Capability Service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }
    
    /**
     * 
     * @param capabilityService Capability Service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        File auditTrail = null;

        try
        {
            
            RecordsManagementAuditQueryParameters queryParams = parseQueryParameters(req);
            ReportFormat reportFormat = parseReportFormat(req);

            if( !userCanAccessAudit(queryParams) )
            {
                throw new WebScriptException(Status.STATUS_FORBIDDEN, "Access denied because the user does not have the Access Audit capability");
            }
            // parse the parameters and get a file containing the audit trail
            auditTrail = this.rmAuditService.getAuditTrailFile(queryParams, reportFormat);

            if (logger.isDebugEnabled())
            {
                logger.debug("Streaming audit trail from file: " + auditTrail.getAbsolutePath());
            }

            boolean attach = false;
            String attachFileName = null;
            String export = req.getParameter(PARAM_EXPORT);
            if (export != null && Boolean.parseBoolean(export))
            {
                attach = true;
                attachFileName = auditTrail.getName();

                if (logger.isDebugEnabled())
                {
                    logger.debug("Exporting audit trail using file name: " + attachFileName);
                }
            }

            // stream the file back to the client
            contentStreamer.streamContent(req, res, auditTrail, null, attach, attachFileName, null);
        }
        finally
        {
            if (auditTrail != null)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug(
                            "Audit results written to file: \n" +
                            "   File:      " + auditTrail + "\n" +
                            "   Parameter: " + parseQueryParameters(req));
                }
                else
                {
                    auditTrail.delete();
                }
            }
        }
    }

    private boolean userCanAccessAudit(RecordsManagementAuditQueryParameters queryParams) 
    {
        NodeRef targetNode = queryParams.getNodeRef();
        if( targetNode == null )
        {
            targetNode = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
        }
        return AccessStatus.ALLOWED.equals(
                capabilityService.getCapabilityAccessState(targetNode, ACCESS_AUDIT_CAPABILITY));
    }
}