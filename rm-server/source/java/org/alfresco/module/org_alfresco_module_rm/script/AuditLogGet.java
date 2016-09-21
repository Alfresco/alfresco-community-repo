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

import org.alfresco.repo.web.scripts.content.ContentStreamer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    /** Content Streamer */
    protected ContentStreamer contentStreamer;

    /**
     * @param contentStreamer
     */
    public void setContentStreamer(ContentStreamer contentStreamer)
    {
        this.contentStreamer = contentStreamer;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        File auditTrail = null;

        try
        {
            // parse the parameters and get a file containing the audit trail
            auditTrail = this.rmAuditService.getAuditTrailFile(parseQueryParameters(req), parseReportFormat(req));

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
}