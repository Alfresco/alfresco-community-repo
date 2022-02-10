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

package org.alfresco.module.org_alfresco_module_rm.script;

import java.io.IOException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

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

    /** Constants */
    protected static final String PARAM_DESTINATION = "destination";
    protected static final String RESPONSE_SUCCESS = "success";
    protected static final String RESPONSE_RECORD = "record";
    protected static final String RESPONSE_RECORD_NAME = "recordName";

    /** Record folder service */
    private RecordFolderService recordFolderService;

    /** Disposition service */
    private DispositionService dispositionService;

    /**
     * Sets the record folder service
     *
     * @param recordFolderService Record folder service
     */
    public void setRecordFolderService(RecordFolderService recordFolderService)
    {
        this.recordFolderService = recordFolderService;
    }

    /**
     * Sets the disposition service
     *
     * @param dispositionService disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     * @see org.alfresco.repo.web.scripts.content.StreamContent#execute(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse)
     */
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        try
        {
            ParameterCheck.mandatory("req", req);
            ParameterCheck.mandatory("res", res);

            // build the json object from the request
            JSONObject json = getJSONObjectFromRequest(req);

            // extract the destination parameter, ensure it's present and it is a record folder and not closed or cut off
            NodeRef destination = getDestination(json);

            // file audit trail as a record
            NodeRef record = fileAuditTrail(destination, req);

            // create response
            String response = createResponse(record);

            // write the JSON response
            writeResponse(res, response);
        }
        catch (Exception ex)
        {
            JSONObject json = new JSONObject();
            putToJSONObject(json, "message", ex.getMessage());
            writeResponse(res, json.toString());
        }
    }

    /**
     * Helper method to write the response to the web script response
     *
     * @param res {@link WebScriptResponse} Web script response
     * @param reponse {@link String} Response to write
     * @throws IOException can throw an exception whilst writing the response
     */
    private void writeResponse(WebScriptResponse res, String response) throws IOException
    {
        // setup response
        res.setContentType(MimetypeMap.MIMETYPE_JSON);
        res.setContentEncoding("UTF-8");
        res.setHeader("Content-Length", Long.toString(response.length()));

        // write the JSON response
        res.getWriter().write(response);
    }

    /**
     * Helper method to create the response text from the record
     *
     * @param record {@link NodeRef} The audit trail as record
     * @return Response text as {@link String}
     */
    @SuppressWarnings("null")
    private String createResponse(NodeRef record)
    {
        JSONObject responseJSON = new JSONObject();
        boolean recordExists = record != null;

        putToJSONObject(responseJSON, RESPONSE_SUCCESS, recordExists);

        if (recordExists)
        {
            putToJSONObject(responseJSON, RESPONSE_RECORD, record.toString());
            putToJSONObject(responseJSON, RESPONSE_RECORD_NAME, (String) nodeService.getProperty(record, ContentModel.PROP_NAME));
        }

        return responseJSON.toString();
    }

    /**
     * Helper method to put a key and a value to a json object.
     * It handles the {@link JSONException} so that a try/catch
     * block is not need through out the code
     *
     * @param json The json object the key/value write to
     * @param key The key which will be written to the json object
     * @param value The value which will be written to the json object
     */
    private void putToJSONObject(JSONObject json, String key,  Object value)
    {
        try
        {
            json.put(key, value);
        }
        catch (JSONException error)
        {
            throw new AlfrescoRuntimeException("Error writing the value '" + value + "' of the key '" + key + "' to the json object.", error);
        }
    }

    /**
     * Helper method which will file the audit trail
     *
     * @param destination The destination where the audit trail will be filed
     * @param req {@link WebScriptRequest} from which additional parameters will be retrieved
     * @return The {@link NodeRef} of the record
     */
    private NodeRef fileAuditTrail(NodeRef destination, WebScriptRequest req)
    {

        NodeRef record = rmAuditService.fileAuditTrailAsRecord(parseQueryParameters(req), destination, parseReportFormat(req));

        if (logger.isDebugEnabled())
        {
            logger.debug("Filed audit trail as new record: '" + record + "'.");
        }

        return record;
    }

    /**
     * Helper method to create a json object from the request
     *
     * @param req {@link WebScriptRequest} from which the json object will be created
     * @return Returns a json object containing the request content
     * @throws IOException can throw an exception whilst getting the content from the request
     */
    private JSONObject getJSONObjectFromRequest(WebScriptRequest req) throws IOException
    {
        JSONObject json;

        try
        {
            json = new JSONObject(new JSONTokener(req.getContent().getContent()));
        }
        catch (JSONException error)
        {
            throw new AlfrescoRuntimeException("Error creating json object from request content.", error);
        }

        return json;
    }

    /**
     * Helper method to get the destination from the json object
     *
     * @param json The json object which was created from the request content
     * @return {@link NodeRef} The destination of the audit log
     */
    private NodeRef getDestination(JSONObject json)
    {
        if (!json.has(PARAM_DESTINATION))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Mandatory parameter '" + PARAM_DESTINATION + "' has not been supplied.");
        }

        String destinationParam;
        try
        {
            destinationParam = json.getString(PARAM_DESTINATION);
        }
        catch (JSONException error)
        {
            throw new AlfrescoRuntimeException("Error extracting 'destination' from parameter.", error);
        }

        if (StringUtils.isBlank(destinationParam))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Please select a record folder.");
        }

        NodeRef destination = new NodeRef(destinationParam);
        if (!nodeService.exists(destination))
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND,
                    "Selected node does not exist");
        }

        // ensure the node is a record folder
        if (!RecordsManagementModel.TYPE_RECORD_FOLDER.equals(nodeService.getType(destination)))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Selected node is not a record folder");
        }

        // ensure the record folder is not closed
        if (recordFolderService.isRecordFolderClosed(destination))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Cannot file into a closed folder.");
        }

        // ensure the record folder is not cut off
        if (dispositionService.isDisposableItemCutoff(destination))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Cannot file into a cut off folder.");
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Filing audit trail as record in record folder: '" + destination + "'.");
        }

        return destination;
    }
}
