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

package org.alfresco.web.bean.wcm.preview;

import javax.faces.context.FacesContext;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.web.bean.repository.Repository;


/**
 * A PreviewURIService that constructs a virtualisation server URI.
 *
 * @author Peter Monks (peter.monks@alfresco.com)
 * 
 * @since 2.2.1
 * 
 * @deprecated see org.alfresco.wcm.preview.VirtualisationServerPreviewURIService
 */
public class VirtualisationServerPreviewURIService extends org.alfresco.wcm.preview.VirtualisationServerPreviewURIService implements PreviewURIService
{
    /**
     * @see org.alfresco.web.bean.wcm.preview.PreviewURIService#getPreviewURI(java.lang.String, java.lang.String)
     */
    public String getPreviewURI(final String sbStoreId, final String pathToAsset)
    {
        ServiceRegistry serviceRegistry = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
        
        this.setAvmService(serviceRegistry.getAVMService());
        this.setVirtServerRegistry(serviceRegistry.getVirtServerRegistry());
        
        return super.getPreviewURI(sbStoreId, pathToAsset, null);
    }

}