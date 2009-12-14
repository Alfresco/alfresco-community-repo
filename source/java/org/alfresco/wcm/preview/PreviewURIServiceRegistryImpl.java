/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.wcm.preview;

import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.extensions.surf.util.PropertyCheck;
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
