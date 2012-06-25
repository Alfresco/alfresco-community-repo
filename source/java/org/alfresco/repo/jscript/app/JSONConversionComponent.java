/**
 * 
 */
package org.alfresco.repo.jscript.app;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.springframework.extensions.surf.util.URLEncoder;

/**
 * JSON Conversion Component
 * 
 * @author Roy Wetherall
 */
public class JSONConversionComponent
{
    /** Content download API URL template */
    private final static String CONTENT_DOWNLOAD_API_URL = "/api/node/content/{0}/{1}/{2}/{3}";
    
    /** Logger */
    private static Log logger = LogFactory.getLog(JSONConversionComponent.class);
    
    /** Registered decorators */
    protected Map<QName, PropertyDecorator> propertyDecorators = new HashMap<QName, PropertyDecorator>(3);

    /** User permissions */
    protected String[] userPermissions;
    
    /** Services */
    protected NodeService nodeService;
    protected PublicServiceAccessService publicServiceAccessService;    
    protected NamespaceService namespaceService;    
    protected FileFolderService fileFolderService;    
    protected LockService lockService;    
    protected ContentService contentService;    
    protected PermissionService permissionService;
    
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
     * Register a property decorator;
     * 
     * @param propertyDecorator
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
    public String toJSON(NodeRef nodeRef, boolean useShortQNames)
    {
        JSONObject json = new JSONObject();
    
        if (this.nodeService.exists(nodeRef) == true)
        {
            if (publicServiceAccessService.hasAccess(ServiceRegistry.NODE_SERVICE.getLocalName(), "getProperties", nodeRef) == AccessStatus.ALLOWED)
            {
                // Get node info
                FileInfo nodeInfo = fileFolderService.getFileInfo(nodeRef);

                // Set root values
                setRootValues(nodeInfo, json, useShortQNames);                                       

                // add permissions
                json.put("permissions", permissionsToJSON(nodeRef));

                // add properties
                json.put("properties", propertiesToJSON(nodeRef, useShortQNames));

                // add aspects
                json.put("aspects", apsectsToJSON(nodeRef, useShortQNames));
            }
        }    
       
        return json.toJSONString();
    }
    
    /**
     * 
     * @param nodeInfo
     * @param rootJSONObject
     * @param useShortQNames
     * @throws JSONException
     */
    @SuppressWarnings("unchecked")
    protected void setRootValues(FileInfo nodeInfo, JSONObject rootJSONObject, boolean useShortQNames)
    {
        NodeRef nodeRef = nodeInfo.getNodeRef();
        
        rootJSONObject.put("nodeRef", nodeInfo.getNodeRef().toString());
        rootJSONObject.put("type", nameToString(nodeInfo.getType(), useShortQNames));                   
        rootJSONObject.put("isContainer", nodeInfo.isFolder()); //node.getIsContainer() || node.getIsLinkToContainer());
        rootJSONObject.put("isLocked", isLocked(nodeInfo.getNodeRef()));
            
        rootJSONObject.put("isLink", nodeInfo.isLink());
        if (nodeInfo.isLink() == true)
        {
            NodeRef targetNodeRef = nodeInfo.getLinkNodeRef();
            if (targetNodeRef != null)
            {
                rootJSONObject.put("linkedNode", toJSON(targetNodeRef, useShortQNames));
            }
        }    
        
        // TODO should this be moved to the property output since we may have more than one content property
        //      or a non-standard content property 
        
        if (nodeInfo.isFolder() == false)
        {
            ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
            
            if (reader != null)
            {
                String contentURL = MessageFormat.format(
                        CONTENT_DOWNLOAD_API_URL, new Object[]{
                                nodeRef.getStoreRef().getProtocol(),
                                nodeRef.getStoreRef().getIdentifier(),
                                nodeRef.getId(),
                                URLEncoder.encode(nodeInfo.getName())});
                
                rootJSONObject.put("contentURL", contentURL);
                rootJSONObject.put("mimetype", reader.getMimetype());
                rootJSONObject.put("encoding", reader.getEncoding());
                rootJSONObject.put("size", reader.getSize());
            }
        }
    }
    
    /**
     * 
     * @param nodeRef
     * @return
     * @throws JSONException
     */
    @SuppressWarnings("unchecked")
    protected JSONObject permissionsToJSON(NodeRef nodeRef)
    {
        JSONObject permissionsJSON = new JSONObject();        
        if (AccessStatus.ALLOWED.equals(permissionService.hasPermission(nodeRef, PermissionService.READ_PERMISSIONS)) == true)
        {
            permissionsJSON.put("inherited", permissionService.getInheritParentPermissions(nodeRef));
            permissionsJSON.put("roles", allSetPermissionsToJSON(nodeRef));
            permissionsJSON.put("user", userPermissionsToJSON(nodeRef));
        }
        return permissionsJSON;
    }
    
    /**
     * 
     * @param nodeRef
     * @return
     */
    @SuppressWarnings("unchecked")
    protected JSONObject userPermissionsToJSON(NodeRef nodeRef)
    {        
        JSONObject userPermissionJSON = new JSONObject();
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
     * @param nodeRef
     * @param propertyName
     * @param key
     * @param value
     * @return the JSON value
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Object propertyToJSON(NodeRef nodeRef, QName propertyName, String key, Serializable value)
    {
    	if (value != null)
        {
            // Has a decorator has been registered for this property?
            if (propertyDecorators.containsKey(propertyName) == true)
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
                	List jsonList = new ArrayList(((List) value).size());
                	for (Object listItem : (List) value) {
						jsonList.add(propertyToJSON(nodeRef, propertyName, key, (Serializable) listItem));
					}
                	return jsonList;
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
     * @param nodeRef
     * @param useShortQNames
     * @return
     * @throws JSONException
     */
    @SuppressWarnings("unchecked")
    protected JSONObject propertiesToJSON(NodeRef nodeRef, boolean useShortQNames)
    {
        JSONObject propertiesJSON = new JSONObject();
        
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
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
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("Ignoring property '" + propertyName + "' as its namespace is not registered");
                }
            }            
        }
        
        return propertiesJSON;
    }
    
    /**
     * 
     * @param nodeRef
     * @param useShortQNames
     * @return
     * @throws JSONException
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
     * 
     * @param nodeRef
     * @return
     */
    @SuppressWarnings("unchecked")
    protected JSONArray allSetPermissionsToJSON(NodeRef nodeRef)
    {
        Set<AccessPermission> acls = permissionService.getAllSetPermissions(nodeRef);
        JSONArray permissions = new JSONArray();
        for (AccessPermission permission : acls)
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
     * 
     * @param qname
     * @param isShortName
     * @return
     */
    private String nameToString(QName qname, boolean isShortName)
    {
        String result = null;
        if (isShortName == true)
        {
            result = qname.toPrefixString(namespaceService);
        }
        else
        {
            result = qname.toString();
        }
        return result;
    }
    
    /**
     * 
     * @param nodeRef
     * @return
     */
    private boolean isLocked(NodeRef nodeRef)
    {
        boolean locked = false;
        
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE) == true)
        {
            LockStatus lockStatus = lockService.getLockStatus(nodeRef);
            if (lockStatus == LockStatus.LOCKED || lockStatus == LockStatus.LOCK_OWNER)
            {
                locked = true;
            }
        }
        
        return locked;
    }
}
