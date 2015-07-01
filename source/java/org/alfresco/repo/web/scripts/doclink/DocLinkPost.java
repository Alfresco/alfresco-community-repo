/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.doclink;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the doclink.post webscript doclink.post is a
 * webscript for creating a link of a document within a target destination
 * 
 * @author Ana Bozianu
 * @since 5.1
 */
public class DocLinkPost extends AbstractDocLink
{
    private static String PARAM_DESTINATION_NODE = "destinationNodeRef";

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        NodeRef sourceNodeRef = null;
        NodeRef destinationNodeRef = null;

        /* Parse the template vars */
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        sourceNodeRef = parseNodeRefFromTemplateArgs(templateVars);

        /* Parse the JSON content */
        JSONObject json = null;
        String contentType = req.getContentType();
        if (contentType != null && contentType.indexOf(';') != -1)
        {
            contentType = contentType.substring(0, contentType.indexOf(';'));
        }
        if (MimetypeMap.MIMETYPE_JSON.equals(contentType))
        {
            try
            {
                json = (JSONObject) JSONValue.parseWithException(req.getContent().getContent());
            }
            catch (IOException io)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + io.getMessage());
            }
            catch (ParseException pe)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + pe.getMessage());
            }
        }
        else
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "invalid request content type");
        }

        /* Parse the destination NodeRef parameter */
        String destinationNodeParam = (String) json.get(PARAM_DESTINATION_NODE);
        ParameterCheck.mandatoryString("destinationNodeParam", destinationNodeParam);
        destinationNodeRef = new NodeRef(destinationNodeParam);

        /* Create link */
        NodeRef linkNodeRef = null;
        try
        {
            linkNodeRef = documentLinkService.createDocumentLink(sourceNodeRef, destinationNodeRef);
        }
        catch (IllegalArgumentException ex)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid Arguments: " + ex.getMessage());
        }
        catch (AccessDeniedException e)
        {
            throw new WebScriptException(Status.STATUS_FORBIDDEN, "You don't have permission to perform this operation");
        }

        /* Build response */
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("linkNodeRef", linkNodeRef.toString());
        return model;
    }
}
