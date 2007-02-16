/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.util;


import junit.framework.TestCase;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.context.ApplicationContext;

/**
 * Base Alfresco test.
 * 
 * Creates a store and root node that can be used in the tests.
 * 
 * @author Roy Wetherall
 */
public abstract class BaseAlfrescoTestCase extends TestCase
{
    /** the context to keep between tests */
    public static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    /** the service registry */
    protected ServiceRegistry serviceRegistry;
    
    /** The node service */
    protected NodeService nodeService;
    
    /** The content service */
    protected ContentService contentService;
    
    /** The authentication component */
    protected AuthenticationComponent authenticationComponent;
    
    /** The store reference */
    protected StoreRef storeRef;
    
    /** The root node reference */
    protected NodeRef rootNodeRef;
    
    
    protected ActionService actionService;
    protected TransactionService transactionService;
    
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        // get the service register
        this.serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        //Get a reference to the node service
        this.nodeService = serviceRegistry.getNodeService();
        this.contentService = serviceRegistry.getContentService();
        this.authenticationComponent = (AuthenticationComponent)ctx.getBean("authenticationComponent");
        this.actionService = (ActionService)ctx.getBean("actionService");
        this.transactionService = serviceRegistry.getTransactionService();
        
        // Authenticate as the system user - this must be done before we create the store
        authenticationComponent.setSystemUserAsCurrentUser();
        
        // Create the store and get the root node
        this.storeRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.storeRef);
        
       
    }
    
    
    @Override
    protected void tearDown() throws Exception
    {
        try
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            // Don't let this mask any previous exceptions
        }
        super.tearDown();
    }
    
}