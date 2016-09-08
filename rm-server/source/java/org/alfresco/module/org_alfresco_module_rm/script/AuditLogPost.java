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
package org.alfresco.module.org_alfresco_module_rm.script;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Implementation for Java backed webscript to file an
 * audit log as a record.
 * 
 * @author Gavin Cornwell
 */
public class AuditLogPost extends BaseAuditRetrievalWebScript
{
    /** Logger */
    private static Log logger = LogFactory.getLog(AuditLogPost.class);
    
    protected static final String PARAM_DESTINATION = "destination";
    protected static final String RESPONSE_SUCCESS = "success";
    protected static final String RESPONSE_RECORD = "record";
    protected static final String RESPONSE_RECORD_NAME = "recordName";
    
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        try
        {
            // retrieve requested format
            String format = req.getFormat();
            
            // construct model for template
            Status status = new Status();
            Cache cache = new Cache(getDescription().getRequiredCache());
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("status", status);
            model.put("cache", cache);
            
            // extract the destination parameter, ensure it's present and it is
            // a record folder
            JSONObject json = new JSONObject(new JSONTokener(req.getContent().getContent()));
            if (!json.has(PARAM_DESTINATION))
            {
                status.setCode(HttpServletResponse.SC_BAD_REQUEST, 
                            "Mandatory '" + PARAM_DESTINATION + "' parameter has not been supplied");
                Map<String, Object> templateModel = createTemplateParameters(req, res, model);
                sendStatus(req, res, status, cache, format, templateModel);
                return;
            }
            
            String destinationParam = json.getString(PARAM_DESTINATION);
            NodeRef destination = new NodeRef(destinationParam);
            
            if (!this.nodeService.exists(destination))
            {
                status.setCode(HttpServletResponse.SC_NOT_FOUND, 
                            "Node " + destination.toString() + " does not exist");
                Map<String, Object> templateModel = createTemplateParameters(req, res, model);
                sendStatus(req, res, status, cache, format, templateModel);
                return;
            }
            
            // ensure the node is a filePlan object
            if (!RecordsManagementModel.TYPE_RECORD_FOLDER.equals(this.nodeService.getType(destination)))
            {
                status.setCode(HttpServletResponse.SC_BAD_REQUEST, 
                            "Node " + destination.toString() + " is not a record folder");
                Map<String, Object> templateModel = createTemplateParameters(req, res, model);
                sendStatus(req, res, status, cache, format, templateModel);
                return;
            }
            
            if (logger.isDebugEnabled())
                logger.debug("Filing audit trail as record in record folder: " + destination);
        
            // parse the other parameters and get a file containing the audit trail
            NodeRef record = this.rmAuditService.fileAuditTrailAsRecord(parseQueryParameters(req), 
                        destination, parseReportFormat(req));
            
            if (logger.isDebugEnabled())
                logger.debug("Filed audit trail as new record: " + record);
            
            // return success flag and record noderef as JSON
            JSONObject responseJSON = new JSONObject();
            responseJSON.put(RESPONSE_SUCCESS, (record != null));
            if (record != null)
            {
                responseJSON.put(RESPONSE_RECORD, record.toString());
                responseJSON.put(RESPONSE_RECORD_NAME, 
                            (String)nodeService.getProperty(record, ContentModel.PROP_NAME));
            }
            
            // setup response
            String jsonString = responseJSON.toString();
            res.setContentType(MimetypeMap.MIMETYPE_JSON);
            res.setContentEncoding("UTF-8");
            res.setHeader("Content-Length", Long.toString(jsonString.length()));
            
            // write the JSON response
            res.getWriter().write(jsonString);
        }
        catch (Throwable e)
        {
            throw createStatusException(e, req, res);
        }
    }
}