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

import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;


/**
 * Preview URI Service Provider Registry Implementation
 * 
 * @author janv
 * 
 * @since 3.2
 */
public class PreviewURIServiceRegistryImpl implements PreviewURIServiceRegistry, InitializingBean
{
    private static final String ERR_INVALID_DEFAULT_PREVIEW_URI_SERVICE = "preview.err.invalid_default_preview_uri_service";
    
    private Map<String, PreviewURIServiceProvider> previewURIServicesByName;
    private String defaultPreviewURIServiceName;
    
    public void setPreviewURIServiceProvidersByName(Map<String, PreviewURIServiceProvider> previewURIServicesByName)
    {
        this.previewURIServicesByName = previewURIServicesByName;
    }
    
    public Map<String, PreviewURIServiceProvider> getPreviewURIServiceProviders()
    {
        return this.previewURIServicesByName;
    }
    
    public void setDefaultProviderName(String defaultPreviewURIServiceName)
    {
        this.defaultPreviewURIServiceName = defaultPreviewURIServiceName;
    }
    
    public String getDefaultProviderName()
    {
        return this.defaultPreviewURIServiceName;
    }
    
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "previewURIServicesByName", previewURIServicesByName);
        PropertyCheck.mandatory(this, "defaultName", defaultPreviewURIServiceName);
        
        // Check that the default preview URI service provider name is valid
        if ((defaultPreviewURIServiceName.length() == 0) || (previewURIServicesByName.get(defaultPreviewURIServiceName) == null))
        {
            AlfrescoRuntimeException.create(ERR_INVALID_DEFAULT_PREVIEW_URI_SERVICE, defaultPreviewURIServiceName, previewURIServicesByName.keySet());
        }
    }
}
