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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.avm.util.AVMUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.alfresco.wcm.util.WCMUtil;
import org.alfresco.wcm.webproject.WebProjectService;


/**
 * Preview URI Service fundamental API.
 * <p>
 * This service API is designed to support the Preview URI API
 * 
 * @author janv
 * 
 * @since 3.2
 */
public class PreviewURIServiceImpl implements PreviewURIService
{
    private PreviewURIServiceRegistry previewURIProviderRegistry;
    private WebProjectService wpService;
    
    public void setPreviewURIServiceRegistry(PreviewURIServiceRegistry previewURIProviderRegistry)
    {
        this.previewURIProviderRegistry = previewURIProviderRegistry;
    }
    
    public void setWebProjectService(WebProjectService wpService)
    {
        this.wpService = wpService;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.preview.PreviewURIService#getPreviewURI(java.lang.String, java.lang.String)
     */
    public String getPreviewURI(String sbStoreId, String pathToAsset)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        
        String wpStoreId = WCMUtil.getWebProjectStoreId(sbStoreId);
        String authenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();
        
        String webApp = null;
        if (pathToAsset != null)
        {
            webApp = WCMUtil.getWebapp(AVMUtil.buildAVMPath(sbStoreId, pathToAsset));
        }
        
        PreviewContext prevCtx = new PreviewContext(wpStoreId, webApp, authenticatedUser);
        
        return getProvider(wpStoreId).getPreviewURI(sbStoreId, pathToAsset, prevCtx);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.preview.PreviewURIService#getPreviewURIs(java.lang.String, java.util.List)
     */
    public List<String> getPreviewURIs(String sbStoreId, List<String> pathsToAssets)
    {
        ParameterCheck.mandatoryString("sbStoreId", sbStoreId);
        ParameterCheck.mandatory("pathsToAssets", pathsToAssets);
        
        String wpStoreId = WCMUtil.getWebProjectStoreId(sbStoreId);
        String authenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();
        
        String webApp = WCMUtil.getCommonWebApp(sbStoreId, pathsToAssets);
        
        PreviewContext prevCtx = new PreviewContext(wpStoreId, webApp, authenticatedUser);
        
        return getProvider(wpStoreId).getPreviewURIs(sbStoreId, pathsToAssets, prevCtx);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.preview.PreviewURIService#getProviderNames()
     */
    public Set<String> getProviderNames()
    {
        Map<String, PreviewURIServiceProvider> previewProviders = previewURIProviderRegistry.getPreviewURIServiceProviders();
        return previewProviders.keySet();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.preview.PreviewURIService#getDefaultProviderName()
     */
    public String getDefaultProviderName()
    {
        // delegate
        return previewURIProviderRegistry.getDefaultProviderName();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.wcm.preview.PreviewURIService#getProviderName(java.lang.String)
     */
    public String getProviderName(String wpStoreId)
    {
        // delegate
        return wpService.getPreviewProvider(wpStoreId);
    }
    
    private PreviewURIServiceProvider getProvider(String wpStoreId)
    {
        PreviewURIServiceProvider previewProvider = previewURIProviderRegistry.getPreviewURIServiceProviders().get(getProviderName(wpStoreId));
        if (previewProvider == null)
        {
            previewProvider = previewURIProviderRegistry.getPreviewURIServiceProviders().get(previewURIProviderRegistry.getDefaultProviderName());
        }
        return previewProvider;
    }
}
