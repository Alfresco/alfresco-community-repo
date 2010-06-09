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
package org.alfresco.repo.admin.patch.impl;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.version.VersionMigrator;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Migrate version store from workspace://lightWeightVersionStore to workspace://version2Store
 */
public class MigrateVersionStorePatch extends AbstractPatch
{
    private static Log logger = LogFactory.getLog(MigrateVersionStorePatch.class);
    
    private static final String MSG_DONE = "patch.migrateVersionStore.done";
    private static final String MSG_INCOMPLETE = "patch.migrateVersionStore.incomplete";
    
    private VersionMigrator versionMigrator;
    private TenantService tenantService;
    private ImporterBootstrap version2ImporterBootstrap;
    
    private int batchSize = 1;
    private int threadCount = 2;
    
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
    
    public void setThreadCount(int threadCount)
    {
        this.threadCount = threadCount;
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
        
        if (threadCount < 1)
        {
            String errorMessage = "threadCount ("+threadCount+") cannot be less than 1";
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
            bootstrapStoreRef = tenantService.getName(AuthenticationUtil.getRunAsUser(), bootstrapStoreRef);
            version2ImporterBootstrap.setStoreUrl(bootstrapStoreRef.toString());
            
            version2ImporterBootstrap.bootstrap();
        }
        
        if (AuthenticationUtil.getRunAsUser() == null)
        {
            logger.info("Set system user");
            AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
        }
        
        boolean completed = versionMigrator.migrateVersions(batchSize, threadCount, deleteImmediately);
        
        // return the result message
        if (completed)
        {
            return I18NUtil.getMessage(MSG_DONE);
        }
        else
        {
            return I18NUtil.getMessage(MSG_INCOMPLETE);
        }
    }
}
