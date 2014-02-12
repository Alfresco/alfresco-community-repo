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
package org.alfresco.repo.web.scripts.solr;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.solr.AlfrescoModelDiff;
import org.alfresco.repo.solr.SOLRTrackingComponent;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Support for SOLR: Track Alfresco model changes
 *
 * @since 4.0
 */
public class AlfrescoModelsDiff extends DeclarativeWebScript
{
    protected static final Log logger = LogFactory.getLog(AlfrescoModelsDiff.class);

    private static final String MSG_IO_EXCEPTION = "IO exception parsing request ";

    private static final String MSG_JSON_EXCEPTION = "Unable to fetch model changes from ";

    private SOLRTrackingComponent solrTrackingComponent;
    
    public void setSolrTrackingComponent(SOLRTrackingComponent solrTrackingComponent)
    {
        this.solrTrackingComponent = solrTrackingComponent;
    }

    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        try
        {
            Map<String, Object> model = buildModel(req);
            if (logger.isDebugEnabled())
            {
                logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
            }
            return model;
        }
        catch (IOException e)
        {
            setExceptionResponse(req, status, MSG_IO_EXCEPTION, Status.STATUS_INTERNAL_SERVER_ERROR, e);
            return null;
        }
        catch (JSONException e)
        {
            setExceptionResponse(req, status, MSG_JSON_EXCEPTION, Status.STATUS_BAD_REQUEST, e);
            return null;
        }
    }

    private void setExceptionResponse(WebScriptRequest req, Status responseStatus, String responseMessage, int statusCode, Exception e)
    {
        String message = responseMessage + req;

        if (logger.isDebugEnabled())
        {
            logger.warn(message, e);
        }
        else
        {
            logger.warn(message);
        }

        responseStatus.setCode(statusCode, message);
        responseStatus.setException(e);
    }

    private Map<String, Object> buildModel(WebScriptRequest req) throws JSONException, IOException
    {
        Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);

        Content content = req.getContent();
        if(content == null)
        {
            throw new WebScriptException("Failed to convert request to String");
        }
        JSONObject o = new JSONObject(content.getContent());
        JSONArray jsonModels = o.getJSONArray("models");
        Map<QName, Long> models = new HashMap<QName, Long>(jsonModels.length());
        for(int i = 0; i < jsonModels.length(); i++)
        {
            JSONObject jsonModel = jsonModels.getJSONObject(i);
            models.put(QName.createQName(jsonModel.getString("name")), jsonModel.getLong("checksum"));
        }

        List<AlfrescoModelDiff> diffs = solrTrackingComponent.getModelDiffs(models);
        model.put("diffs", diffs);

        if (logger.isDebugEnabled())
        {
            logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
        }
        
        return model;
    }
}
