/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */package org.alfresco.repo.importer.system;

import java.io.InputStream;
import java.util.List;

import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.view.ImporterException;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.AbstractLifecycleBean;
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
    private AuthenticationComponent authenticationComponent;
    private SystemExporterImporter systemImporter;
    
    private List<String> mustNotExistStoreUrls = null;
    private String bootstrapView = null;
    
    
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
     * @param authenticationComponent
     */
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    /**
     * Set the System Importer
     * 
     * @param systemImporter
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
     * @param bootstrapView
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
        authenticationComponent.setSystemUserAsCurrentUser();

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
            try {authenticationComponent.clearCurrentSecurityContext(); } catch (Exception ex) {}
            throw new AlfrescoRuntimeException("System Info Bootstrap failed", e);
        }
        finally
        {
            authenticationComponent.clearCurrentSecurityContext();
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
