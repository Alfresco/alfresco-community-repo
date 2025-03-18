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
package org.alfresco.repo.web.scripts.node;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * This class is the webscript for creating child folders, of either a Node or a Site Container.
 * 
 * @since 4.1
 */
public class NodeFolderPost extends DeclarativeWebScript
{
    private NodeService nodeService;
    private SiteService siteService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // Identify the Node they want to create a child of
        SiteInfo site = null;
        String container = null;
        NodeRef parentNodeRef = null;

        Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
        if (templateArgs.get("site") != null && templateArgs.get("container") != null)
        {
            // Site based request
            site = siteService.getSite(templateArgs.get("site"));
            if (site == null)
            {
                status.setCode(Status.STATUS_NOT_FOUND);
                status.setRedirect(true);
                return null;
            }

            // Check the container exists
            container = templateArgs.get("container");
            NodeRef containerNodeRef = siteService.getContainer(site.getShortName(), container);
            if (containerNodeRef == null)
            {
                status.setCode(Status.STATUS_NOT_FOUND);
                status.setRedirect(true);
                return null;
            }

            // Work out where to put it
            if (templateArgs.get("path") != null)
            {
                // Nibble our way along the / delimited path, starting from the container
                parentNodeRef = containerNodeRef;
                StringTokenizer st = new StringTokenizer(templateArgs.get("path"), "/");
                while (st.hasMoreTokens())
                {
                    String childName = st.nextToken();
                    parentNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, childName);
                    if (parentNodeRef == null)
                    {
                        status.setCode(Status.STATUS_NOT_FOUND);
                        status.setRedirect(true);
                        return null;
                    }
                }
            }
            else
            {
                // Direct child of the container
                parentNodeRef = containerNodeRef;
            }
        }
        else if (templateArgs.get("store_type") != null && templateArgs.get("store_id") != null
                && templateArgs.get("id") != null)
        {
            // NodeRef based creation
            parentNodeRef = new NodeRef(templateArgs.get("store_type"),
                    templateArgs.get("store_id"), templateArgs.get("id"));
            if (!nodeService.exists(parentNodeRef))
            {
                status.setCode(Status.STATUS_NOT_FOUND);
                status.setRedirect(true);
                return null;
            }
        }
        else
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "No parent details found");
        }

        // Process the JSON post details
        JSONObject json = null;
        JSONParser parser = new JSONParser();
        try
        {
            json = (JSONObject) parser.parse(req.getContent().getContent());
        }
        catch (IOException io)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + io.getMessage());
        }
        catch (ParseException pe)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + pe.getMessage());
        }

        // Fetch the name, title and description
        String name = (String) json.get("name");
        if (name == null)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Name is required");
        }

        String title = (String) json.get("title");
        if (title == null)
        {
            title = name;
        }
        String description = (String) json.get("description");

        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, name);
        props.put(ContentModel.PROP_TITLE, title);
        props.put(ContentModel.PROP_DESCRIPTION, description);

        // Verify the type is allowed
        QName type = ContentModel.TYPE_FOLDER;
        if (json.get("type") != null)
        {
            type = QName.createQName((String) json.get("type"), namespaceService);
            if (!dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER))
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Specified type is not a folder");
            }
        }

        // Have the node created
        NodeRef nodeRef = null;
        try
        {
            nodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(name), type, props).getChildRef();
        }
        catch (AccessDeniedException e)
        {
            throw new WebScriptException(Status.STATUS_FORBIDDEN, "You don't have permission to create the node");
        }

        // Report the details
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("nodeRef", nodeRef);
        model.put("site", site);
        model.put("container", container);
        model.put("parentNodeRef", parentNodeRef);
        return model;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
}
