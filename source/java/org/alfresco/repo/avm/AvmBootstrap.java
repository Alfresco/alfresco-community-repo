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
package org.alfresco.repo.avm;

import java.util.List;

import org.alfresco.service.cmr.security.PermissionService;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.context.ApplicationEvent;

/**
 * This component ensures that the AVM system is properly bootstrapped
 * and that this is done in the correct order relative to other
 * bootstrap components.
 *
 * @see #setIssuers(List)
 * @see org.alfresco.repo.avm.Issuer
 *
 * @author Derek Hulley
 */
public class AvmBootstrap extends AbstractLifecycleBean
{
    private AVMLockingAwareService avmLockingAwareService;

    private AVMRepository avmRepository;

    private PermissionService permissionService;
    
    private AVMSyncServiceImpl avmSyncService;
    

    public AvmBootstrap()
    {
    }

    public void setAvmLockingAwareService(AVMLockingAwareService service)
    {
        avmLockingAwareService = service;
    }

    public void setAvmRepository(AVMRepository repository)
    {
        avmRepository = repository;
    }

    public void setPermissionService(PermissionService service)
    {
        permissionService = service;
    }
    
    public void setAvmSyncService(AVMSyncServiceImpl service)
    {
        avmSyncService = service;
    }

    /**
     * Initialize the issuers.
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        avmLockingAwareService.init();
        avmRepository.setPermissionService(permissionService);
        avmSyncService.setPermissionService(permissionService);
    }

    /** NO-OP */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // Nothing
    }
}
