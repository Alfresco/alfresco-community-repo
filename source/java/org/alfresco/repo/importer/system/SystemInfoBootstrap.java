/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.importer.system;

import java.io.InputStream;
import java.util.List;

import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.view.ImporterException;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.context.ApplicationEvent;


/**
 * Repository System Information bootstrap
 * 
 * @author davidc
 */
public class SystemInfoBootstrap extends AbstractLifecycleBean
{
    // dependencies
    private TransactionService transactionService;
    private NodeService nodeService;
    private AuthenticationContext authenticationContext;
    private SystemExporterImporter systemImporter;
    
    private List<String> mustNotExistStoreUrls = null;
    private String bootstrapView = null;
    
    
    /**
     * Sets the Transaction Service
     * 
     * @param transactionService the transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService; 
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
     * @param authenticationContext AuthenticationContext
     */
    public void setAuthenticationContext(AuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    /**
     * Set the System Importer
     * 
     * @param systemImporter SystemExporterImporter
     */
    public void setSystemImporter(SystemExporterImporter systemImporter)
    {
        this.systemImporter = systemImporter;
    }

    /**
     * If any of the store urls exist, the bootstrap does not take place
     * 
     * @param storeUrls  the list of store urls to check
     */
    public void setMustNotExistStoreUrls(List<String> storeUrls)
    {
        this.mustNotExistStoreUrls = storeUrls;
    }
        
    /**
     * Set the bootstrap view containing the system information
     * 
     * @param bootstrapView String
     */
    public void setBootstrapView(String bootstrapView)
    {
        this.bootstrapView = bootstrapView;
    }

    /**
     * Bootstrap
     */
    public void bootstrap()
    {
        UserTransaction userTransaction = transactionService.getUserTransaction();
        authenticationContext.setSystemUserAsCurrentUser();

        try
        {
            userTransaction.begin();
        
            // check the repository exists, create if it doesn't
            if (performBootstrap())
            {
                InputStream viewStream = getClass().getClassLoader().getResourceAsStream(bootstrapView);
                if (viewStream == null)
                {
                    throw new ImporterException("Could not find system info file " + bootstrapView);
                }
                try
                {
                    systemImporter.importSystem(viewStream);
                }
                finally
                {
                    viewStream.close();
                }
            }
            userTransaction.commit();
        }
        catch(Throwable e)
        {
            // rollback the transaction
            try { if (userTransaction != null) {userTransaction.rollback();} } catch (Exception ex) {}
            try {authenticationContext.clearCurrentSecurityContext(); } catch (Exception ex) {}
            throw new AlfrescoRuntimeException("System Info Bootstrap failed", e);
        }
        finally
        {
            authenticationContext.clearCurrentSecurityContext();
        }
    }
    
    /**
     * Determine if bootstrap should take place
     * 
     * @return  true => yes, it should
     */
    private boolean performBootstrap()
    {
        if (bootstrapView == null || bootstrapView.length() == 0)
        {
            return false;
        }
        if (mustNotExistStoreUrls != null)
        {
            for (String storeUrl : mustNotExistStoreUrls)
            {
                StoreRef storeRef = new StoreRef(storeUrl);
                if (nodeService.exists(storeRef))
                {
                    return false;
                }
            }
        }
        return true;
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
