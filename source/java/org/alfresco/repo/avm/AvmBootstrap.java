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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.avm;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.AbstractLifecycleBean;
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
    private List<Issuer> issuers;

    private AVMLockingAwareService avmLockingAwareService;

    private AVMRepository avmRepository;

    private PermissionService permissionService;

    public AvmBootstrap()
    {
        issuers = new ArrayList<Issuer>(0);
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

    /**
     * Provide a list of {@link Issuer issuers} to bootstrap on context initialization.
     *
     * @see #onBootstrap(ApplicationEvent)
     */
    public void setIssuers(List<Issuer> issuers)
    {
        this.issuers = issuers;
    }

    /**
     * Initialize the issuers.
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        for (Issuer issuer : issuers)
        {
            issuer.initialize();
        }
        avmLockingAwareService.init();
        avmRepository.setPermissionService(permissionService);
    }

    /** NO-OP */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // Nothing
    }
}
