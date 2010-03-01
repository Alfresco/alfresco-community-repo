/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.wcm.preview;

import java.text.MessageFormat;

import org.alfresco.config.JNDIConstants;
import org.alfresco.mbeans.VirtServerRegistry;
import org.alfresco.repo.avm.util.AVMUtil;
import org.alfresco.service.cmr.avm.AVMService;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.alfresco.wcm.util.WCMUtil;


/**
 * A PreviewURIService that constructs a virtualisation server URI.
 *
 * @author Peter Monks, janv
 * 
 * @since 3.2
 */
public class VirtualisationServerPreviewURIService extends AbstractPreviewURIServiceProvider
{
    // TODO - remove deprecated constants from JNDIConstants
    
    /**
     * Default virtualization server IP address 
     */
    public final static String DEFAULT_VSERVER_IP = "127-0-0-1.ip.alfrescodemo.net";
    
    /**
     * Default virtualization server port number
     */
    public final static int DEFAULT_VSERVER_PORT = 8180;
    
    /**
     * Virtualization server sandbox URL pattern
     */
    public final static String PREVIEW_SANDBOX_URL = "http://{0}.www--sandbox.{1}:{2}";
    
    /**
     * Virtualization server asset URL pattern
     */
    public final static String PREVIEW_ASSET_URL   = "http://{0}.www--sandbox.{1}:{2}{3}";
    
    
    private AVMService avmService;
    private VirtServerRegistry virtSvrRegistry;
    
    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }
    
    public void setVirtServerRegistry(VirtServerRegistry virtSvrRegistry)
    {
        this.virtSvrRegistry = virtSvrRegistry;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.preview.PreviewURIServiceProvider#getPreviewURI(java.lang.String, java.lang.String, org.alfresco.wcm.preview.PreviewContext)
     */
    public String getPreviewURI(String sbStoreId, String pathToAsset, PreviewContext ignored)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        if ((pathToAsset == null) || (pathToAsset.length() == 0))
        {
            return buildPreviewStoreUrl(sbStoreId, getVServerDomain(), getVServerPort(),  WCMUtil.lookupStoreDNS(avmService, sbStoreId));
        }
        
        // Sanity checking
        if (!pathToAsset.startsWith('/' + JNDIConstants.DIR_DEFAULT_WWW + '/' + JNDIConstants.DIR_DEFAULT_APPBASE))
        {
            throw new IllegalStateException("Invalid asset path in AVM node ref: " + sbStoreId + ":" + pathToAsset);
        }
        
        return buildPreviewAssetUrl(pathToAsset, getVServerDomain(), getVServerPort(), WCMUtil.lookupStoreDNS(avmService, sbStoreId));
    }
    
    private String buildPreviewStoreUrl(String sbStoreId, String domain, String port, String dns)
    {
        return MessageFormat.format(PREVIEW_SANDBOX_URL, dns, domain, port);
    }
    
    private String buildPreviewAssetUrl(String assetPath, String domain, String port, String dns)
    {
        ParameterCheck.mandatoryString("assetPath", assetPath);
        
       if (domain == null || port == null || dns == null)
       {
          throw new IllegalArgumentException("Domain, port and dns name are mandatory.");
       }
       
       if (assetPath.startsWith(JNDIConstants.DIR_DEFAULT_WWW_APPBASE))
       {
          assetPath = assetPath.substring((JNDIConstants.DIR_DEFAULT_WWW_APPBASE).length());
       }
       if (assetPath.startsWith(AVMUtil.AVM_PATH_SEPARATOR + WCMUtil.DIR_ROOT))
       {
          assetPath = assetPath.substring((AVMUtil.AVM_PATH_SEPARATOR + WCMUtil.DIR_ROOT).length());
       }
       
       assetPath = AVMUtil.addLeadingSlash(assetPath);
       
       return MessageFormat.format(PREVIEW_ASSET_URL, dns, domain, port, assetPath);
    }
    
    /**
     * @return VServer Port
     */
    private String getVServerPort()
    {
        Integer port = (virtSvrRegistry != null ? virtSvrRegistry.getVirtServerHttpPort() : null);
        if (port == null)
        {
            port = DEFAULT_VSERVER_PORT;
        }
        return port.toString();
    }
    
    /**
     * @return VServer Domain
     */
    private String getVServerDomain()
    {
        String domain = (virtSvrRegistry != null ? virtSvrRegistry.getVirtServerFQDN() : null);
        if (domain == null)
        {
            domain = DEFAULT_VSERVER_IP;
        }
        return domain;
    }
}
