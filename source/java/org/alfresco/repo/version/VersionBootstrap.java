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
 */
package org.alfresco.repo.version;

import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.transaction.TransactionService;

/**
 * Bootstrap Version Store
 * 
 * @author David Caruana
 */
public class VersionBootstrap
{
    private TransactionService transactionService;
    private NodeService nodeService;
    private AuthenticationComponent authenticationComponent;
    private PermissionService permissionService;
    
    
    /**
     * Sets the Transaction Service
     * 
     * @param userTransaction the user transaction
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Sets the Node Service
     * 
     * @param nodeService the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }
    
    

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * Bootstrap the Version Store
     */
    public void bootstrap()
    {
        UserTransaction userTransaction = transactionService.getUserTransaction();
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
        
        try 
        {
            userTransaction.begin();

            //  Ensure that the version store has been created
            if (this.nodeService.exists(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, VersionModel.STORE_ID)) == true)
            {
                userTransaction.rollback();
            }
            else
            {
                StoreRef vStore = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, VersionModel.STORE_ID);
                // TODO: For now there are no permissions on version access 
                permissionService.setPermission(nodeService.getRootNode(vStore), permissionService.getAllAuthorities(), permissionService.getAllPermission(), true);
                userTransaction.commit();
            }
        }
        catch(Throwable e)
        {
            // rollback the transaction
            try { if (userTransaction != null) {userTransaction.rollback();} } catch (Exception ex) {}
            try {authenticationComponent.clearCurrentSecurityContext(); } catch (Exception ex) {}
            throw new AlfrescoRuntimeException("Bootstrap failed", e);
        }    
        finally
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
    }
    
}
