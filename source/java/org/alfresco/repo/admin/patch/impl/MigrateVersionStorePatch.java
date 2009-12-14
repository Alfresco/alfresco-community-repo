/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.admin.patch.impl;

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.version.VersionMigrator;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Migrate version store from workspace://lightWeightVersionStore to workspace://version2Store
 */
public class MigrateVersionStorePatch extends AbstractPatch
{
    private static Log logger = LogFactory.getLog(MigrateVersionStorePatch.class);
    
    private static final String MSG_SUCCESS = "patch.migrateVersionStore.result";
    
    private VersionMigrator versionMigrator;
    private TenantService tenantService;
    private ImporterBootstrap version2ImporterBootstrap;
    
    private int batchSize = 1;
    private boolean deleteImmediately = false;
    
    public void setVersionMigrator(VersionMigrator versionMigrator)
    {
        this.versionMigrator = versionMigrator;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setImporterBootstrap(ImporterBootstrap version2ImporterBootstrap)
    {
        this.version2ImporterBootstrap = version2ImporterBootstrap;
    }
    
    public void setBatchSize(int batchSize)
    {
        this.batchSize = batchSize;
    }
    
    public void setDeleteImmediately(boolean deleteImmediately)
    {
        this.deleteImmediately = deleteImmediately;
    }
    
    public void init()
    {
        if (batchSize < 1)
        {
            String errorMessage = "batchSize ("+batchSize+") cannot be less than 1";
            logger.error(errorMessage);
            throw new AlfrescoRuntimeException(errorMessage);
        }
        
        super.init();
    }
    
    @Override
    protected String applyInternal() throws Exception
    {
    	if (tenantService.isEnabled() && tenantService.isTenantUser())
    	{
    		// bootstrap new version store
            StoreRef bootstrapStoreRef = version2ImporterBootstrap.getStoreRef();
            bootstrapStoreRef = tenantService.getName(AuthenticationUtil.getRunAsUser(),  bootstrapStoreRef);
            version2ImporterBootstrap.setStoreUrl(bootstrapStoreRef.toString());
        
            version2ImporterBootstrap.bootstrap();
    	}
    	
        int vhCount = versionMigrator.migrateVersions(batchSize, deleteImmediately);

        // build the result message
        String msg = I18NUtil.getMessage(MSG_SUCCESS, vhCount);
        
        // done
        return msg;
    }
}
