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
package org.alfresco.repo.web.scripts.metadata;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.json.JSONWriter;

/**
 * This class is the webscript implementation for the url api/bulkmetadata
 * It returns basic metadata such as title, parent noderef, name, ... for each noderef that is passed in
 * 
 * @since 3.4
 */
public class BulkMetadataGet extends AbstractWebScript
{
    private ServiceRegistry services;
    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private PermissionService permissionService;

    private String getMimeType(ContentData contentProperty)
    {
        String mimetype = null;

        if(contentProperty != null)
        {
            mimetype = contentProperty.getMimetype();
        }

        return mimetype;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        JSONObject jsonIn;
        JSONArray nodeRefsArray;

        try
        {
            Content c = req.getContent();
            if (c == null)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Missing POST body.");
            }

            jsonIn = new JSONObject(c.getContent());

            nodeRefsArray = jsonIn.getJSONArray("nodeRefs");
            if(nodeRefsArray == null || nodeRefsArray.length() == 0)
            {
                throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Must provide node refs");   
            }

            JSONWriter jsonOut = new JSONWriter(res.getWriter());
    
            res.setContentType("application/json");
            res.setContentEncoding(Charset.defaultCharset().displayName());     // TODO: Should be settable on JSONWriter
            
            jsonOut.startObject();
            {
                jsonOut.startValue("nodes");
                {
                    jsonOut.startArray();
                    {
                        for(int i = 0; i < nodeRefsArray.length(); i++)
                        {
                            NodeRef nodeRef = new NodeRef(nodeRefsArray.getString(i));

                            if(nodeService.exists(nodeRef))
                            {
                                NodeRef parentNodeRef = null;
                                ChildAssociationRef childAssocRef = nodeService.getPrimaryParent(nodeRef);
                                if(childAssocRef != null)
                                {
                                    parentNodeRef = childAssocRef.getParentRef();
                                }
                                QName type = nodeService.getType(nodeRef);

                                String shortType = type.toPrefixString(services.getNamespaceService());
                                Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        
                                jsonOut.startObject();
                                {
                                    jsonOut.writeValue("nodeRef", nodeRef.toString());
                                    jsonOut.writeValue("parentNodeRef", parentNodeRef.toString());
                                    jsonOut.writeValue("type", type.toString());
                                    jsonOut.writeValue("shortType", shortType);
                                    TypeDefinition typeDef = dictionaryService.getType(type);
                                    jsonOut.writeValue("typeTitle", typeDef.getTitle(dictionaryService));

                                    jsonOut.writeValue("name", (String)properties.get(ContentModel.PROP_NAME));
                                    jsonOut.writeValue("title", (String)properties.get(ContentModel.PROP_TITLE));
                                    jsonOut.writeValue("mimeType", getMimeType((ContentData)properties.get(ContentModel.PROP_CONTENT)));
                                    jsonOut.writeValue("hasWritePermission", permissionService.hasPermission(nodeRef, PermissionService.WRITE) == AccessStatus.ALLOWED);
                                    jsonOut.writeValue("hasDeletePermission", permissionService.hasPermission(nodeRef, PermissionService.DELETE) == AccessStatus.ALLOWED);
                                }
                                jsonOut.endObject();
                            }
                            else
                            {
                                jsonOut.startObject();
                                {
                                    jsonOut.writeValue("nodeRef", nodeRef.toString());
                                    jsonOut.writeValue("error", "true");
                                    jsonOut.writeValue("errorCode", "invalidNodeRef");
                                    jsonOut.writeValue("errorText", I18NUtil.getMessage("msg.invalidNodeRef", nodeRef.toString()));
                                }
                                jsonOut.endObject();
                            }
                        }
                    }
                    jsonOut.endArray();
                }
                jsonOut.endValue();
            }
            jsonOut.endObject();
        }
        catch (JSONException jErr)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Unable to parse JSON POST body: " + jErr.getMessage());
        }

        res.getWriter().close();

        res.setStatus(Status.STATUS_OK);
    }
    
    /**
     * Set the service registry
     * 
     * @param services  the service registry
     */
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.services = services;
        this.nodeService = services.getNodeService();
        this.dictionaryService = services.getDictionaryService();
        this.permissionService = services.getPermissionService();
    }
        
}
