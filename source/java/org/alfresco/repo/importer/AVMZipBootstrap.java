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
package org.alfresco.repo.importer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.zip.ZipFile;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * A Bootstrap-time Importer which allows the loading
 *  of part of an AVM filesystem from a Zip file
 * 
 * @author Nick Burch
 */
public class AVMZipBootstrap extends AbstractLifecycleBean
{
    // Logger
    private static final Log logger = LogFactory.getLog(AVMZipBootstrap.class);
    
    private TransactionService transactionService;
    private RetryingTransactionHelper retryingTransactionHelper;
    private NodeService nodeService;
    private AuthenticationContext authenticationContext;
    private AVMService avmService;
    private AVMZipImporter avmZipImporter;
    
    private boolean allowWrite = true;
    private String location;
    private String avmRoot;
        
    /**
     * Set whether we write or not
     * 
     * @param write true (default) if the import must go ahead, otherwise no import will occur
     */
    public void setAllowWrite(boolean write)
    {
        this.allowWrite = write;
    }
    
    /**
     * Sets the Root of the AVM store to import to.
     */
    public void setAvmRoot(String avmRoot)
    {
        this.avmRoot = avmRoot;
    }

    /**
     * Sets the location of the zip file to import from
     */
    public void setLocation(String location)
    {
        this.location = location;
    }

    /**
     * Sets the AVM Importer to be used for importing to
     * 
     * @param avmZipImporter The AVM Importer Service
     */
    public void setAvmZipImporter(AVMZipImporter avmZipImporter)
    {
        this.avmZipImporter = avmZipImporter;
    }
    
    /**
     * Sets the AVM Service to be used for exporting from
     * 
     * @param avmService The AVM Service
     */
    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }
    
    /**
     * Sets the Transaction Service
     * 
     * @param userTransaction the transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Sets the retrying transaction helper specific to the importer bootstrap. This transaction helper's parameters are
     * tuned to the longer-running import transaction.
     * 
     * @param retryingTransactionHelper
     *            the retrying transaction helper
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    /**
     * Sets the node service
     * 
     * @param nodeService the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
   
    /**
     * Set the authentication component
     * 
     * @param authenticationContext
     */
    public void setAuthenticationContext(AuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    /**
     * Bootstrap the Repository
     */
    public void bootstrap()
    {
        PropertyCheck.mandatory(this, "avmZipImporter", avmZipImporter);
        PropertyCheck.mandatory(this, "retryingTransactionHelper", retryingTransactionHelper);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
       
        if (avmRoot == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("No AVM Root URL - bootstrap import ignored");
            }
            return;
        }
        
        if (location == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("No Location given to import from - bootstrap import ignored");
            }
            return;
        }
        
        try
        {
            // import the content - note: in MT case, this will run in System context of tenant domain
            RunAsWork<Object> importRunAs = new RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    RetryingTransactionCallback<Object> doImportCallback = new RetryingTransactionCallback<Object>()
                    {
                        public Object execute() throws Throwable
                        {   
                            doImport();
                            return null;
                        }
                    };
                    return retryingTransactionHelper.doInTransaction(doImportCallback, transactionService.isReadOnly(), false);
                }
            };
            AuthenticationUtil.runAs(importRunAs, authenticationContext.getSystemUserName());
        }
        catch(Throwable e)
        {
            throw new AlfrescoRuntimeException("Bootstrap failed", e);
        }
    }
    
    /**
     * Perform the actual import work.  This is just separated to allow for simpler TXN demarcation.
     */
    private void doImport() throws Throwable
    {
        if (!allowWrite)
        {
            // we're in read-only node
            logger.warn("Skipping import as read-only: " + avmRoot);
        }
        else
        {
            // Create the store if necessary
            String store = avmRoot.substring(0, avmRoot.indexOf(':'));
            if(avmService.getStore(store) == null)
            {
                avmService.createStore(store);
            }
            
            logger.debug("Bootstrapping AVM data from " + location + " to " + avmRoot);
            
            // Open the Zip
            ZipFile zip = new ZipFile(
                    ImporterBootstrap.getFile(location)
            );

            // Now do the import
            avmZipImporter.importNodes(zip, avmRoot);
        }
    }
    
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        bootstrap();
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // NOOP
    }
}
