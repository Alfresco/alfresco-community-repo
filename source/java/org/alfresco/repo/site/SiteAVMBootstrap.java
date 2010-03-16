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
package org.alfresco.repo.site;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.alfresco.util.PropertyCheck;
import org.springframework.context.ApplicationEvent;

/**
 * This component is responsible for bootstraping the special Site AVM store.
 * 
 * @author Kevin Roast
 */
public class SiteAVMBootstrap extends AbstractLifecycleBean
{
    /** AVM store name to create */
    private String storeName;
    
    /** Root directory name to create */
    private String rootDir;
    
    /** The AVM Service to use */
    private AVMService avmService;
    
    /** The Permission Service to use */
    private PermissionService permissionService;
    
    private TransactionService transactionService;
    
    
    /**
     * @param rootDir the rootDir to set
     */
    public void setRootdir(String rootdir)
    {
        if (rootDir != null && rootDir.length() == 0)
        {
            rootDir = null;
        }
        this.rootDir = rootdir;
    }

    /**
     * @param storeName the storeName to set
     */
    public void setStorename(String storename)
    {
        this.storeName = storename;
    }
    
    /**
     * @param avmService the AVMService avmService to set
     */
    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }

    /**
     * @param permissionService the PermissionService to set
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // run as System on bootstrap
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork()
            {            
                bootstrap();
                return null;
            }                               
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Bootstrap the AVM store
     */
    public void bootstrap()
    {
        // ensure properties have been set
        PropertyCheck.mandatory(this, "avmService", avmService);
        PropertyCheck.mandatory(this, "permissionService", permissionService);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "storeName", storeName);
        PropertyCheck.mandatory(this, "rootDir", rootDir);
        
        // Avoid read-only mode errors
        if (transactionService.isReadOnly())
        {
            // Do nothing
            return;
        }
        
        if (this.avmService.getStore(storeName) == null)
        {
            // create the site store
            this.avmService.createStore(storeName);
            
            // apply the special marker property - so we know this is a "sitestore"
            // this will then be found by the FTP/CIFS virtual filesystem and expose the store
            this.avmService.setStoreProperty(storeName, QName.createQName(null, ".sitestore"),
                    new PropertyValue(DataTypeDefinition.TEXT, "true"));
            
            // create the root directory
            this.avmService.createDirectory(storeName + ":/", rootDir);
            
            // set default permissions on the new store
            StoreRef store = new StoreRef(StoreRef.PROTOCOL_AVM, storeName);
            this.permissionService.setPermission(store, PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);
            this.permissionService.setPermission(store, AuthenticationUtil.getGuestUserName(), PermissionService.READ, true);
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // nothing to do
    }
}
