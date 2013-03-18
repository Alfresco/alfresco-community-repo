/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.tenant;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.content.ContentLimitProvider;
import org.alfresco.repo.content.ContentLimitProvider.NoLimitProvider;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.springframework.context.ApplicationContext;

/**
 * MT-aware File Content Store
 */
public class TenantRoutingFileContentStore extends AbstractTenantRoutingContentStore
{
    private ContentLimitProvider contentLimitProvider = new NoLimitProvider();
    
    /**
     * Sets a new {@link ContentLimitProvider} which will provide a maximum filesize for content.
     */
    public void setContentLimitProvider(ContentLimitProvider contentLimitProvider)
    {
        this.contentLimitProvider = contentLimitProvider;
    }
    
    protected ContentStore initContentStore(ApplicationContext ctx, String contentRoot)
    {
    	Map<String, Serializable> extendedEventParams = new HashMap<String, Serializable>();
    	if (!TenantService.DEFAULT_DOMAIN.equals(tenantService.getCurrentUserDomain()))
    	{
    	    extendedEventParams.put("Tenant", tenantService.getCurrentUserDomain());
    	}

        FileContentStore fileContentStore = new FileContentStore(ctx, new File(contentRoot), extendedEventParams);
        
        // Set the content filesize limiter if there is one.
        if (this.contentLimitProvider != null)
        {
            fileContentStore.setContentLimitProvider(contentLimitProvider);
        }
        
        return fileContentStore;
    }
}
