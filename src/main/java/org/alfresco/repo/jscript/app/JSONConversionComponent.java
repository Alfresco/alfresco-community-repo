/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.jscript.app;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PublicServiceAccessService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.URLEncoder;

/**
 * JSON Conversion Component
 * 
 * @author Roy Wetherall
 * @author Kevin Roast
 */
public class JSONConversionComponent
{
    /** Content download API URL template */
    private final static String CONTENT_DOWNLOAD_API_URL = "/slingshot/node/content/{0}/{1}/{2}/{3}";
    
    /** Logger */
    private static Log logger = LogFactory.getLog(JSONConversionComponent.class);
    
    /** Registered decorators */
    protected Map<QName, PropertyDecorator> propertyDecorators = new HashMap<QName, PropertyDecorator>(8);
    
    /** User permissions */
    protected String[] userPermissions;
    
    /** Thread local cache of namespace prefixes for long QName to short prefix name conversions */
    protected static ThreadLocal<Map<String, String>> namespacePrefixCache = new ThreadLocal<Map<String, String>>()
    {
        @Override
        protected Map<String, String> initialValue()
        {
            return new HashMap<String, String>(8);
        }
    };
    
    /** Services */
    protected NodeService nodeService;
    protected PublicServiceAccessService publicServiceAccessService;    
    protected NamespaceService namespaceService;    
    protected FileFolderService fileFolderService;    
    protected LockService lockService;    
    protected ContentService contentService;    
    protected PermissionService permissionService;
    protected MimetypeService mimetypeService;
    
    
    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param publicServiceAccessService    public service access service
     */
    public void setPublicServiceAccessService(PublicServiceAccessService publicServiceAccessService)
    {
        this.publicServiceAccessService = publicServiceAccessService;
    }
    
    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * @param fileFolderService file folder service
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
    
    /**
     * @param lockService   lock service
     */
    public void setLockService(LockService lockService)
    {
        this.lockService = lockService;
    }    
    
    /**
     * @param permissionService permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    /**
     * @param userPermissions   user permissions
     */
    public void setUserPermissions(String[] userPermissions)
    {
        this.userPermissions = userPermissions;
    }
    
    /**
     * @param contentService    content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @param mimetypeService    mimetype service
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }
    
    /**
     * Register a property decorator;
     * 
     * @param propertyDecorator PropertyDecorator
     */
    public void registerPropertyDecorator(PropertyDecorator propertyDecorator)
    {
        for (QName propertyName : propertyDecorator.getPropertyNames())
        {
            propertyDecorators.put(propertyName, propertyDecorator);
        }        
    }
    
    /**
     * Convert a node reference to a JSON string.  Selects the correct converter based on selection
     * implementation.
     */
    @SuppressWarnings("unchecked")
    public String toJSON(final NodeRef nodeRef, final boolean useShortQNames)
    {
        return toJSONObject(nodeRef, useShortQNames).toJSONString();
    }
    
    /**
     * Convert a node reference to a JSON object.  Selects the correct converter based on selection
     * implementation.
     */
    @SuppressWarnings("unchecked")
    public JSONObject toJSONObject(final NodeRef nodeRef, final boolean useShortQNames)
    {
        final JSONObject json = new JSONObject();
        
        if (this.nodeService.exists(nodeRef))
        {
            if (publicServiceAccessService.hasAccess(ServiceRegistry.NODE_SERVICE.getLocalName(), "getProperties", nodeRef) == AccessStatus.ALLOWED)
            {
                // init namespace prefix cache
                namespacePrefixCache.get().clear();
                
                // Get node info
                FileInfo nodeInfo = this.fileFolderService.getFileInfo(nodeRef);
                
                // Set root values
                setRootValues(nodeInfo, json, useShortQNames);                                       
                
                // add permissions
                json.put("permissions", permissionsToJSON(nodeRef));
                
                // add properties
                json.put("properties", propertiesToJSON(nodeRef, nodeInfo.getProperties(), useShortQNames));
                
                // add aspects
                json.put("aspects", apsectsToJSON(nodeRef, useShortQNames));
            }
        }    
        
        return json;
    }
    
    /**
     * 
     * @param nodeInfo FileInfo
     * @param rootJSONObject JSONObject
     * @param useShortQNames boolean
     */
    @SuppressWarnings("unchecked")
    protected void setRootValues(final FileInfo nodeInfo, final JSONObject rootJSONObject, final boolean useShortQNames)
    {
        final NodeRef nodeRef = nodeInfo.getNodeRef();
        
        rootJSONObject.put("nodeRef", nodeInfo.getNodeRef().toString());
        rootJSONObject.put("type", nameToString(nodeInfo.getType(), useShortQNames));                   
        rootJSONObject.put("isContainer", nodeInfo.isFolder()); //node.getIsContainer() || node.getIsLinkToContainer());
        rootJSONObject.put("isLocked", isLocked(nodeInfo.getNodeRef()));
        
        rootJSONObject.put("isLink", nodeInfo.isLink());
        if (nodeInfo.isLink())
        {
            NodeRef targetNodeRef = nodeInfo.getLinkNodeRef();
            if (targetNodeRef != null)
            {
                rootJSONObject.put("linkedNode", toJSONObject(targetNodeRef, useShortQNames));
            }
        }    
        
        // TODO should this be moved to the property output since we may have more than one content property
        //      or a non-standard content property 
        
        if (nodeInfo.isFolder() == false)
        {
            final ContentData cdata = nodeInfo.getContentData();
            if (cdata != null)
            {
                String contentURL = MessageFormat.format(
                        CONTENT_DOWNLOAD_API_URL, new Object[]{
                                nodeRef.getStoreRef().getProtocol(),
                                nodeRef.getStoreRef().getIdentifier(),
                                nodeRef.getId(),
                                URLEncoder.encode(nodeInfo.getName())});
                
                rootJSONObject.put("contentURL", contentURL);
                rootJSONObject.put("mimetype", cdata.getMimetype());
                Map<String, String> mimetypeDescriptions;
                mimetypeDescriptions = mimetypeService.getDisplaysByMimetype();

                if (mimetypeDescriptions.containsKey(cdata.getMimetype()))
                {
                    rootJSONObject.put("mimetypeDisplayName", mimetypeDescriptions.get(cdata.getMimetype()));
                }
                rootJSONObject.put("encoding", cdata.getEncoding());
                rootJSONObject.put("size", cdata.getSize());
            }
        }
    }
    
    /**
     * Handles the work of converting node permissions to JSON.
     *  
     * @param nodeRef NodeRef
     * @return JSONObject
     */
    @SuppressWarnings("unchecked")
    protected JSONObject permissionsToJSON(final NodeRef nodeRef)
    {
        final JSONObject permissionsJSON = new JSONObject();        
        if (AccessStatus.ALLOWED.equals(permissionService.hasPermission(nodeRef, PermissionService.READ_PERMISSIONS)) == true)
        {
            permissionsJSON.put("inherited", permissionService.getInheritParentPermissions(nodeRef));
            permissionsJSON.put("roles", allSetPermissionsToJSON(nodeRef));
            permissionsJSON.put("user", userPermissionsToJSON(nodeRef));
        }
        return permissionsJSON;
    }
    
    /**
     * Handles the work of converting user permissions to JSON.
     * 
     * @param nodeRef NodeRef
     * @return JSONObject
     */
    @SuppressWarnings("unchecked")
    protected JSONObject userPermissionsToJSON(final NodeRef nodeRef)
    {        
        final JSONObject userPermissionJSON = new JSONObject();
        for (String userPermission : this.userPermissions)
        {
            boolean hasPermission = AccessStatus.ALLOWED.equals(permissionService.hasPermission(nodeRef, userPermission));
            userPermissionJSON.put(userPermission, hasPermission);
        }
        return userPermissionJSON;
    }
    
    /**
     * Handles the work of converting values to JSON.
     * 
     * @param nodeRef NodeRef
     * @param propertyName QName
     * @param key String
     * @param value Serializable
     * @return the JSON value
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Object propertyToJSON(final NodeRef nodeRef, final QName propertyName, final String key, final Serializable value)
    {
    	if (value != null)
        {
            // Has a decorator has been registered for this property?
            if (propertyDecorators.containsKey(propertyName))
            {
                JSONAware jsonAware = propertyDecorators.get(propertyName).decorate(propertyName, nodeRef, value);
                if (jsonAware != null)
                {
                	return jsonAware;
                }
            }
            else
            {
                // Built-in data type processing
                if (value instanceof Date)
                {
                    JSONObject dateObj = new JSONObject();
                    dateObj.put("value", JSONObject.escape(value.toString()));
                    dateObj.put("iso8601", JSONObject.escape(ISO8601DateFormat.format((Date)value)));
                    return dateObj;
                }
                else if (value instanceof List)
                {
                	// Convert the List to a JSON list by recursively calling propertyToJSON
                	List<Object> jsonList = new ArrayList<Object>(((List<Serializable>) value).size());
                	for (Serializable listItem : (List<Serializable>) value)
                	{
                	    jsonList.add(propertyToJSON(nodeRef, propertyName, key, listItem));
                	}
                	return jsonList;
                }
                else if (value instanceof Double)
                {
                    return (Double.isInfinite((Double)value) || Double.isNaN((Double)value) ? null : value.toString());
                }
                else if (value instanceof Float)
                {
                    return (Float.isInfinite((Float)value) || Float.isNaN((Float)value) ? null : value.toString());
                }
                else
                {
                	return value.toString();
                }
            }
        }
    	return null;
    }
    
    /**
     * 
     * @param nodeRef NodeRef
     * @param useShortQNames boolean
     * @return JSONObject
     */
    @SuppressWarnings("unchecked")
    protected JSONObject propertiesToJSON(NodeRef nodeRef, Map<QName, Serializable> properties, boolean useShortQNames)
    {
        JSONObject propertiesJSON = new JSONObject();
        
        for (QName propertyName : properties.keySet())
        {
            try
            {
                String key = nameToString(propertyName, useShortQNames);
                Serializable value = properties.get(propertyName);
                
                propertiesJSON.put(key, propertyToJSON(nodeRef, propertyName, key, value));
            }
            catch (NamespaceException ne)
            {
                // ignore properties that do not have a registered namespace
                if (logger.isDebugEnabled())
                    logger.debug("Ignoring property '" + propertyName + "' as its namespace is not registered");
            }
        }
        
        return propertiesJSON;
    }
    
    /**
     * Handles the work of converting aspects to JSON.
     * 
     * @param nodeRef NodeRef
     * @param useShortQNames boolean
     * @return JSONArray
     */
    @SuppressWarnings("unchecked")
    protected JSONArray apsectsToJSON(NodeRef nodeRef, boolean useShortQNames)
    {
        JSONArray aspectsJSON = new JSONArray();
        
        Set<QName> aspects = this.nodeService.getAspects(nodeRef);
        for (QName aspect : aspects)
        {
            aspectsJSON.add(nameToString(aspect, useShortQNames));
        }
        
        return aspectsJSON;
    }
    
    /**
     * Handles the work of converting all set permissions to JSON.
     * 
     * @param nodeRef NodeRef
     * @return JSONArray
     */
    @SuppressWarnings("unchecked")
    protected JSONArray allSetPermissionsToJSON(NodeRef nodeRef)
    {
        Set<AccessPermission> acls = permissionService.getAllSetPermissions(nodeRef);
        JSONArray permissions = new JSONArray();

        List<AccessPermission> ordered = ScriptNode.getSortedACLs(acls);

        for (AccessPermission permission : ordered)
        {
            StringBuilder buf = new StringBuilder(64);
            buf.append(permission.getAccessStatus())
                .append(';')
                .append(permission.getAuthority())
                .append(';')
                .append(permission.getPermission())
                .append(';').append(permission.isSetDirectly() ? "DIRECT" : "INHERITED");                
            permissions.add(buf.toString());
        }
        return permissions;
    }
    
    /**
     * Convert a qname to a string - either full or short prefixed named.
     * 
     * @param qname QName
     * @param isShortName boolean
     * @return qname string.
     */
    private String nameToString(final QName qname, final boolean isShortName)
    {
        String result;
        if (isShortName)
        {
            final Map<String, String> cache = namespacePrefixCache.get();
            String prefix = cache.get(qname.getNamespaceURI());
            if (prefix == null)
            {
                // first request for this namespace prefix, get and cache result
                Collection<String> prefixes = this.namespaceService.getPrefixes(qname.getNamespaceURI());
                prefix = prefixes.size() != 0 ? prefixes.iterator().next() : "";
                cache.put(qname.getNamespaceURI(), prefix);
            }
            result = prefix + QName.NAMESPACE_PREFIX + qname.getLocalName();
        }
        else
        {
            result = qname.toString();
        }
        return result;
    }
    
    /**
     * Return true if the node is locked.
     * 
     * @param nodeRef NodeRef
     * @return boolean
     */
    private boolean isLocked(final NodeRef nodeRef)
    {
        boolean locked = false;
        
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE) == true)
        {
            locked = lockService.isLocked(nodeRef);
        }
        
        return locked;
    }
}
