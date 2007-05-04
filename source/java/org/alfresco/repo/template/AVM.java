/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.template;

import java.text.MessageFormat;
import java.util.Map;

import org.alfresco.config.JNDIConstants;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * AVM root object access for a template model.
 * 
 * @author Kevin Roast
 */
public class AVM extends BaseTemplateProcessorExtension
{
    private ServiceRegistry services;

    /**
     * Sets the service registry
     * 
     * @param services  the service registry
     */
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.services = services;
    }
    
    /**
     * Return an AVM store object for the specified store name
     * 
     * @param store         Store name to lookup
     * 
     * @return the AVM store object for the specified store or null if not found
     */
    public AVMTemplateStore lookupStore(String store)
    {
        AVMTemplateStore avmStore = null;
        AVMStoreDescriptor descriptor = this.services.getAVMService().getStore(store);
        if (descriptor != null)
        {
            avmStore = new AVMTemplateStore(this.services, getTemplateImageResolver(), descriptor);
        }
        return avmStore;
    }
    
    /**
     * Return the root node for a specified AVM store
     * 
     * @param store         Store name to find root node for
     * 
     * @return the AVM store root node for the specified store or null if not found.
     */
    public AVMTemplateNode lookupStoreRoot(String store)
    {
        AVMTemplateNode root = null;
        if (store != null && store.length() != 0)
        {
            AVMTemplateStore avmStore = lookupStore(store);
            if (avmStore != null)
            {
                root = avmStore.getLookupRoot();
            }
        }
        return root;
    }
    
    /**
     * Look a node by the absolute path. Path should include the store reference.
     * 
     * @param path          Absolute path to the node, including store reference.
     * 
     * @return the node if found, null otherwise.
     */
    public AVMTemplateNode lookupNode(String path)
    {
        AVMTemplateNode node = null;
        if (path != null && path.length() != 0)
        {
            AVMNodeDescriptor nodeDesc = this.services.getAVMService().lookup(-1, path);
            if (nodeDesc != null)
            {
                node = new AVMTemplateNode(path, -1, this.services, getTemplateImageResolver());
            }
        }
        return node;
    }
    
    /**
     * @param storeId   Store ID to build staging store name for
     * 
     * @return the Staging Store name for the given store ID
     */
    public String stagingStore(String storeId)
    {
        return storeId;
    }
    
    /**
     * @param storeId   Store ID to build sandbox store name for
     * @param username  Username of the sandbox user
     * 
     * @return the Sandbox Store name for the given store ID and username
     */
    public String userSandboxStore(String storeId, String username)
    {
        ParameterCheck.mandatoryString("Store ID", storeId);
        ParameterCheck.mandatoryString("Username", username);
        return storeId + "--" + username;
    }
    
    /**
     * @param storeId   Store ID to build preview URL for
     * 
     * @return the preview URL to the staging store for the specified store ID
     */
    public String websiteStagingUrl(String storeId)
    {
        return MessageFormat.format(JNDIConstants.PREVIEW_SANDBOX_URL,
                lookupStoreDNS(storeId), getVServerDomain(), getVServerPort());
    }
    
    /**
     * @param storeId   Store ID to build preview URL for
     * @param username  Username to build sandbox preview URL for
     * 
     * @return the preview URL to the user sandbox for the specified store ID and username
     */
    public String websiteUserSandboxUrl(String storeId, String username)
    {
        return websiteStagingUrl(userSandboxStore(storeId, username));
    }
    
    /**
     * @param store     Store ID of the asset
     * @param assetPath Store relative path to the asset
     * 
     * @return the preview URL to the specified store asset
     */
    public String assetUrl(String store, String assetPath)
    {
        ParameterCheck.mandatoryString("Store", store);
        ParameterCheck.mandatoryString("Asset Path", assetPath);
        if (assetPath.startsWith('/' + JNDIConstants.DIR_DEFAULT_WWW + 
                                 '/' + JNDIConstants.DIR_DEFAULT_APPBASE))
        {
            assetPath = assetPath.substring(('/' + JNDIConstants.DIR_DEFAULT_WWW + 
                                             '/' + JNDIConstants.DIR_DEFAULT_APPBASE).length());
        }
        if (assetPath.startsWith("/ROOT"))
        {
            assetPath = assetPath.substring(("/ROOT").length());
        }
        if (assetPath.length() == 0 || assetPath.charAt(0) != '/')
        {
            assetPath = '/' + assetPath;
        }
        return MessageFormat.format(JNDIConstants.PREVIEW_ASSET_URL,
                lookupStoreDNS(store), getVServerDomain(), getVServerPort(), assetPath);
    }

    /**
     * @param avmPath   Fully qualified AVM path of the asset
     * 
     * @return the preview URL to the specified asset
     */
    public String assetUrl(String avmPath)
    {
        ParameterCheck.mandatoryString("AVM Path", avmPath);
        String[] s = avmPath.split(":");
        if (s.length != 2)
        {
            throw new IllegalArgumentException("Expected exactly one ':' in " + avmPath);
        }
        return assetUrl(s[0], s[1]);
    }
    
    /**
     * @return VServer Port
     */
    private String getVServerPort()
    {
        Integer port = this.services.getVirtServerRegistry().getVirtServerHttpPort();
        if (port == null)
        {
            port = JNDIConstants.DEFAULT_VSERVER_PORT;
        }
        return port.toString();
    }

    /**
     * @return VServer Domain
     */
    private String getVServerDomain()
    {
        String domain = this.services.getVirtServerRegistry().getVirtServerFQDN();
        if (domain == null)
        {
            domain = JNDIConstants.DEFAULT_VSERVER_IP;
        }
        return domain;
    }
    
    /**
     * @return the path to the webapps folder in a standard web store.
     */
    public static String getWebappsFolderPath()
    {
        return '/' + JNDIConstants.DIR_DEFAULT_WWW +
               '/' + JNDIConstants.DIR_DEFAULT_APPBASE;
    }
    
    private String lookupStoreDNS(String store)
    {
        Map<QName, PropertyValue> props = 
            this.services.getAVMService().queryStorePropertyKey(store, QName.createQName(null, PROP_DNS + '%'));
        return (props.size() == 1
                ? props.keySet().iterator().next().getLocalName().substring(PROP_DNS.length()) : null);
    }
    
    private final static String PROP_DNS = ".dns.";
}
