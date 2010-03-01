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
package org.alfresco.util;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.transaction.TransactionService;

/**
 * Base Alfresco test.
 * 
 * Creates a store and root node that can be used in the tests.
 * 
 * Runs all tests as the system user.
 * 
 * @author Roy Wetherall
 */
public abstract class BaseAlfrescoSpringTest extends BaseSpringTest
{
    /** The node service */
    protected NodeService nodeService;

    /** The content service */
    protected ContentService contentService;

    /** The authentication service */
    protected MutableAuthenticationService authenticationService;

    /** The store reference */
    protected StoreRef storeRef;

    /** The root node reference */
    protected NodeRef rootNodeRef;
    
    
    protected ActionService actionService;
    protected TransactionService transactionService;

    protected AuthenticationComponent authenticationComponent;

    /**
     * On setup in transaction override
     */
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();

        // Get a reference to the node service
        this.nodeService = (NodeService) this.applicationContext.getBean("nodeService");
        this.contentService = (ContentService) this.applicationContext.getBean("contentService");
        this.authenticationService = (MutableAuthenticationService) this.applicationContext.getBean("authenticationService");
        this.actionService = (ActionService)this.applicationContext.getBean("actionService");
        this.transactionService = (TransactionService)this.applicationContext.getBean("transactionComponent");

        // Authenticate as the system user
        authenticationComponent = (AuthenticationComponent) this.applicationContext
                .getBean("authenticationComponent");
        authenticationComponent.setSystemUserAsCurrentUser();
        
        // Create the store and get the root node
        this.storeRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.storeRef);

      

    }

    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        authenticationService.clearCurrentSecurityContext();
        super.onTearDownInTransaction();
    }

}
