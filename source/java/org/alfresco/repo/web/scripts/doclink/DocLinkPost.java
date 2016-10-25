/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.web.scripts.doclink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;
import org.json.simple.JSONArray;
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
    private static final String PARAM_DESTINATION_NODE = "destinationNodeRef";
    private static final String PARAM_MULTIPLE_FILES = "multipleFiles";

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

        List<NodeRef> nodeRefs = new ArrayList<NodeRef>();
        if (json.containsKey(PARAM_MULTIPLE_FILES))
        {
            JSONArray multipleFiles = (JSONArray) json.get(PARAM_MULTIPLE_FILES);
            for (int i = 0; i < multipleFiles.size(); i++)
            {
                String nodeRefString = (String) multipleFiles.get(i);
                if (nodeRefString != null)
                {
                    try
                    {
                        NodeRef nodeRefToCreateLink = new NodeRef(nodeRefString);
                        nodeRefs.add(nodeRefToCreateLink);
                    }
                    catch (AlfrescoRuntimeException ex)
                    {
                        throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid Arguments: " + ex.getMessage());
                    }
                   
                }
            }
        }
        else
        {
            nodeRefs.add(sourceNodeRef);
        }

        // getSite for destination folder
        String siteName = siteService.getSiteShortName(destinationNodeRef);
        
        ArrayList<Object> linksResults = new ArrayList<Object>();
        Map<String, Object> linkResult = new HashMap<String, Object>();
        NodeRef linkNodeRef = null;
        int successCount = 0;
        int failureCount = 0;

        if (nodeRefs != null && nodeRefs.size() > 0)
        {
            for (NodeRef sourceNode : nodeRefs)
            {
                /* Create link */
                linkNodeRef = createLink(destinationNodeRef, sourceNode);

                if (linkNodeRef != null)
                {
                    String sourceName = (String) nodeService.getProperty(sourceNode, ContentModel.PROP_NAME);
                    if (siteName != null)
                    {
                        addActivityEntry(ActivityType.DOCLINK_CREATED, sourceName, sourceNode.toString(), siteName);
                    }

                    linkResult.put("nodeRef", linkNodeRef.toString());
                    linksResults.add(linkResult);
                    successCount++;
                }
            }
        }

        failureCount = nodeRefs.size() - successCount;
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("results", linksResults);
        model.put("successCount", successCount);
        model.put("failureCount", failureCount);
        model.put("overallSuccess", failureCount == 0);
        return model;
    }

    /**
     * Create link for sourceNodeRef in destinationNodeRef location
     * 
     * @param destinationNodeRef
     * @param sourceNodeRef
     * @return
     */
    private NodeRef createLink(NodeRef destinationNodeRef, NodeRef sourceNodeRef)
    {
        NodeRef linkNodeRef = null;
        try
        {
            linkNodeRef = documentLinkService.createDocumentLink(sourceNodeRef, destinationNodeRef);
        }
        catch (IllegalArgumentException ex)
        {
            if (ex.getMessage().contains("filelink") || ex.getMessage().contains("folderLink"))
            {
                return null;
            }
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid Arguments: " + ex.getMessage());
        }
        catch (AccessDeniedException e)
        {
            throw new WebScriptException(Status.STATUS_FORBIDDEN, "You don't have permission to perform this operation");
        }
        return linkNodeRef;
    }
}
