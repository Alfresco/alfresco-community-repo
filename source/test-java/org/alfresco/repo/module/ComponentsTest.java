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
package org.alfresco.repo.module;

import java.util.Collection;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Tests various module components.
 * 
 * @see org.alfresco.repo.module.ImporterModuleComponent
 * @see org.alfresco.repo.module.ModuleComponent
 * 
 * @author Derek Hulley
 */
public class ComponentsTest extends TestCase
{
    private static ApplicationContext ctx = new ClassPathXmlApplicationContext("module/module-component-test-beans.xml");
    
    private ServiceRegistry serviceRegistry;
    private AuthenticationComponent authenticationComponent;
    private TransactionService transactionService;
    private NodeService nodeService;
    private UserTransaction txn;
    
    @Override
    protected void setUp() throws Exception
    {
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        authenticationComponent = (AuthenticationComponent) ctx.getBean("AuthenticationComponent");
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();
        
        // Run as system user
        authenticationComponent.setSystemUserAsCurrentUser();
        
        // Start a transaction
        txn = transactionService.getUserTransaction();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        // Clear authentication
        try
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        // Rollback the transaction
        try
        {
//            txn.rollback();
            txn.commit();
        }
        catch (Throwable e)
        {
            // Ignore
        }
    }

    /** Ensure that the test starts and stops properly */
    public void testSetup() throws Exception
    {
    }
    
    private NodeRef getLoadedCategoryRoot()
    {
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        
        CategoryService categoryService = serviceRegistry.getCategoryService();
        // Check if the categories exist
        Collection<ChildAssociationRef> assocRefs = categoryService.getRootCategories(
                storeRef,
                ContentModel.ASPECT_GEN_CLASSIFIABLE);
        // Find it
        for (ChildAssociationRef assocRef : assocRefs)
        {
            NodeRef nodeRef = assocRef.getChildRef();
            if (nodeRef.getId().equals("test:xyz-root"))
            {
                // Found it
                return nodeRef;
            }
        }
        return null;
    }
    
    public void testImporterModuleComponent() throws Exception
    {
        // Delete any pre-existing data
        NodeRef nodeRef = getLoadedCategoryRoot();
        if (nodeRef != null)
        {
            CategoryService categoryService = serviceRegistry.getCategoryService();
            categoryService.deleteCategory(nodeRef);
        }
        // Double check to make sure it is gone
        nodeRef = getLoadedCategoryRoot();
        assertNull("Category not deleted", nodeRef);
        
        ImporterModuleComponent component = (ImporterModuleComponent) ctx.getBean("module.test.importerComponent");
        // Execute it
        component.execute();
        
        // Now make sure the data exists
        nodeRef = getLoadedCategoryRoot();
        assertNotNull("Loaded category root not found", nodeRef);
    }
}
