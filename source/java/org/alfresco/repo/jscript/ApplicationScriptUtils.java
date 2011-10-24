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
package org.alfresco.repo.jscript;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.app.PropertyDecorator;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.URLEncoder;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

/**
 * Utility functions specifically for external application use.
 * 
 * @author Mike Hatfield
 */

public final class ApplicationScriptUtils extends BaseScopableProcessorExtension
{
    private static Log logger = LogFactory.getLog(ApplicationScriptUtils.class);

    /** Repository Service Registry */
    private ServiceRegistry services;
    private NodeService nodeService = null;
    private Map<String, Object> decoratedProperties;
    private String[] userPermissions;

    private final static String CONTENT_DOWNLOAD_API_URL = "/api/node/content/{0}/{1}/{2}/{3}";

    /**
     * Set the service registry
     *
     * @param serviceRegistry  the service registry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.services = serviceRegistry;
        this.nodeService = services.getNodeService();
    }

    /**
     * Set the properties that require decorator beans
     *
     * @param decoratedProperties
     */
    public void setDecoratedProperties(Map<String, Object> decoratedProperties)
    {
        this.decoratedProperties = decoratedProperties;
    }

    /**
     * Define the list of user permissions to return in the JSON body
     *
     * @param userPermissions
     */
    public void setUserPermissions(String[] userPermissions)
    {
        this.userPermissions = userPermissions;
    }

    /**
     * Returns the JSON representation of a node. Long-form QNames are used in the
     * result.
     * 
     * @param node the node to convert to JSON representation.
     * @return The JSON representation of this node
     */
    public String toJSON(ScriptNode node)
    {
        return this.toJSON(node, false);
    }

    /**
     * Returns the JSON representation of this node.
     * 
     * @param node the node to convert to JSON representation.
     * @param useShortQNames if true short-form qnames will be returned, else long-form.
     * @return The JSON representation of this node
     */
    public String toJSON(ScriptNode node, boolean useShortQNames)
    {
        return this.toJSONObj(node, useShortQNames).toString();
    }

    /**
     * Returns a JSON object representing the node.
     *
     * @param node the node to convert to JSON representation.
     * @param useShortQNames if true short-form qnames will be returned, else long-form.
     * @return The JSON representation of this node
     */
    protected Object toJSONObj(ScriptNode node, boolean useShortQNames)
    {
        NodeRef nodeRef = node.getNodeRef();
        JSONObject json = new JSONObject();

        if (this.nodeService.exists(nodeRef))
        {
            if (this.services.getPublicServiceAccessService().hasAccess(ServiceRegistry.NODE_SERVICE.getLocalName(), "getProperties", nodeRef) == AccessStatus.ALLOWED)
            {
                try
                {
                    String typeString = useShortQNames ? this.getShortQName(node.getQNameType()) : node.getType();
                    boolean isLink = node.getIsLinkToContainer() || node.getIsLinkToDocument();

                    json.put("nodeRef", nodeRef.toString());
                    json.put("type", typeString);
                    json.put("isContainer", node.getIsContainer() || node.getIsLinkToContainer());
                    json.put("isLink", isLink);
                    json.put("isLocked", node.getIsLocked());

                    if (node.getIsDocument())
                    {
                        json.put("contentURL", this.getDownloadAPIUrl(node));
                        json.put("mimetype", node.getMimetype());
                        json.put("size", node.getSize());
                    }

                    // permissions
                    Map<String, Serializable> permissionsJSON = new LinkedHashMap<String, Serializable>(3);
                    if (node.hasPermission("ReadPermissions"))
                    {
                        permissionsJSON.put("roles", node.retrieveAllSetPermissions(false, true));
                    }
                    permissionsJSON.put("inherited", node.inheritsPermissions());
                    Map<String, Serializable> userPermissionJSON = new LinkedHashMap<String, Serializable>(this.userPermissions.length);
                    for (String userPermission : this.userPermissions)
                    {
                        userPermissionJSON.put(userPermission, node.hasPermission(userPermission));
                    }
                    permissionsJSON.put("user", (Serializable) userPermissionJSON);
                    json.put("permissions", permissionsJSON);

                    // add properties
                    Map<QName, Serializable> nodeProperties = this.nodeService.getProperties(nodeRef);
                    json.put("properties", this.parseToJSON(nodeRef, nodeProperties, useShortQNames));

                    // add aspects as an array
                    Set<QName> nodeAspects = this.nodeService.getAspects(nodeRef);
                    if (useShortQNames)
                    {
                        Set<String> nodeAspectsShortQNames = new LinkedHashSet<String>(nodeAspects.size());
                        for (QName nextLongQName : nodeAspects)
                        {
                            String nextShortQName = this.getShortQName(nextLongQName);
                            nodeAspectsShortQNames.add(nextShortQName);
                        }
                        json.put("aspects", nodeAspectsShortQNames);
                    }
                    else
                    {
                        json.put("aspects", nodeAspects);
                    }

                    // link to document or folder?
                    if (isLink)
                    {
                        NodeRef targetNodeRef = (NodeRef) nodeProperties.get(ContentModel.PROP_LINK_DESTINATION);
                        if (targetNodeRef != null)
                        {
                            json.put("linkedNode", this.toJSONObj(new ScriptNode(targetNodeRef, this.services, node.scope), useShortQNames));
                        }
                    }
                }
                catch (JSONException error)
                {
                    error.printStackTrace();
                }
            }
        }
       
        return json;
    }

    /**
     * Given a long-form QName, this method uses the namespace service to create a
     * short-form QName string.
     *
     * @param longQName
     * @return the short form of the QName string, e.g. "cm:content"
     */
    protected String getShortQName(QName longQName)
    {
        return longQName.toPrefixString(this.services.getNamespaceService());
    }

    /**
     * Converts a map of node properties to a format suitable for JSON output
     *
     * @param nodeRef
     * @param properties
     * @param useShortQNames
     * @return a decorated map of properties suitable for JSON output
     */
    protected Map<String, Serializable> parseToJSON(NodeRef nodeRef, Map<QName, Serializable> properties, boolean useShortQNames)
    {
        Map<String, Serializable> json = new LinkedHashMap<String, Serializable>(properties.size());

        for (QName nextLongQName : properties.keySet())
        {
            try
            {
                String shortQName = this.getShortQName(nextLongQName);
                String key = useShortQNames ? shortQName : nextLongQName.toString();
                Serializable value = properties.get(nextLongQName);

                if (value != null)
                {
                    // Has a decorator has been registered for this property?
                    if (this.decoratedProperties.containsKey(shortQName))
                    {
                        json.put(key, ((PropertyDecorator) this.decoratedProperties.get(shortQName)).decorate(nodeRef, shortQName, value));
                    }
                    else
                    {
                        // Built-in data type processing
                        if (value instanceof Date)
                        {
                            Map<String, Serializable> dateObj = new LinkedHashMap<String, Serializable>(1);
                            dateObj.put("value", value);
                            dateObj.put("iso8601", ISO8601DateFormat.format((Date)value));
                            json.put(key, (Serializable)dateObj);
                        }
                        else
                        {
                            json.put(key, value);
                        }
                    }
                }
                else
                {
                    json.put(key, null);
                }
            }
            catch (NamespaceException ne)
            {
                // ignore properties that do not have a registered namespace
                if (logger.isDebugEnabled())
                    logger.debug("Ignoring property '" + nextLongQName + "' as its namespace is not registered");
            }
        }

        return json;
    }

    /**
     * @param  node the node to construct the download URL for
     * @return For a content document, this method returns the URL to the /api/node/content
     *         API for the default content property
     *         <p>
     *         For a container node, this method returns an empty string
     *         </p>
     */
    public String getDownloadAPIUrl(ScriptNode node)
    {
        if (node.getIsDocument())
        {
           return MessageFormat.format(CONTENT_DOWNLOAD_API_URL, new Object[]{
                   node.nodeRef.getStoreRef().getProtocol(),
                   node.nodeRef.getStoreRef().getIdentifier(),
                   node.nodeRef.getId(),
                   URLEncoder.encode(node.getName())});
        }
        else
        {
            return "";
        }
    }

}
