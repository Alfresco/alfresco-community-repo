/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.legacy.service;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.model.security.ModelAccessDeniedException;
import org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService;
import org.alfresco.module.org_alfresco_module_rm.model.security.ProtectedAspect;
import org.alfresco.module.org_alfresco_module_rm.model.security.ProtectedProperty;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.namespace.QName;

/**
 * Model security service test.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class ModelSecurityServiceImplTest extends BaseRMTestCase
{
    private static final QName CUSTOM_ASPECT = QName.createQName("http://www.alfresco.org/model/rmtest/1.0", "customAspect");
    private static final QName CUSTOM_PROTECTED_ASPECT = QName.createQName("http://www.alfresco.org/model/rmtest/1.0", "customProtectedAspect");
    
    private static final QName CUSTOM_PROPERTY = QName.createQName("http://www.alfresco.org/model/rmtest/1.0", "customProperty");
    private static final QName CUSTOM_PROTECTED_PROPERTY = QName.createQName("http://www.alfresco.org/model/rmtest/1.0", "customProtectedProperty");
    
    
    /** Model security service */
    private ModelSecurityService modelSecurityService;    
    
    private boolean enabled;
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#isUserTest()
     */
    @Override
    protected boolean isUserTest()
    {
        return true;
    }
    
    @Override
    protected boolean isFillingForAllUsers()
    {
        return true;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#initServices()
     */
    @Override
    protected void initServices()
    {
        super.initServices();        
        modelSecurityService = (ModelSecurityService)applicationContext.getBean("ModelSecurityService");
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#setupTestDataImpl()
     */
    @Override
    protected void setupTestDataImpl()
    {
        super.setupTestDataImpl();
        
        enabled = modelSecurityService.isEnabled();
        modelSecurityService.setEnabled(true);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#tearDownImpl()
     */
    @Override
    protected void tearDownImpl()
    {
        super.tearDownImpl();
        modelSecurityService.setEnabled(enabled);
    }
    
    /**
     * test - getProtectedAspects(), isProtectedAspect(), getProtectedAspect()
     */
    public void testProtectedAspects() throws Exception
    {
        doTestInTransaction(new VoidTest()
        {            
            @Override
            public void runImpl() throws Exception
            {
                Set<QName> protectedAspects = modelSecurityService.getProtectedAspects();
                
                assertNotNull(protectedAspects);
                assertFalse(protectedAspects.isEmpty());
                assertTrue(protectedAspects.contains(CUSTOM_PROTECTED_ASPECT));
                assertFalse(protectedAspects.contains(CUSTOM_ASPECT));
                
                assertTrue(modelSecurityService.isProtectedAspect(CUSTOM_PROTECTED_ASPECT));
                assertFalse(modelSecurityService.isProtectedAspect(CUSTOM_ASPECT));
                
                ProtectedAspect protectedAspect = modelSecurityService.getProtectedAspect(CUSTOM_ASPECT);
                assertNull(protectedAspect);
                protectedAspect = modelSecurityService.getProtectedAspect(CUSTOM_PROTECTED_ASPECT);
                assertNotNull(protectedAspect);
                assertNotNull(protectedAspect.getQName());
                assertNotNull(protectedAspect.getCapabilities());                
            }
        });   
        
        doTestInTransaction(new VoidTest()
        {            
            @Override
            public void runImpl() throws Exception
            {
                assertTrue(modelSecurityService.isProtectedAspect(CUSTOM_PROTECTED_ASPECT));
                assertFalse(modelSecurityService.canEditProtectedAspect(rmFolder, CUSTOM_PROTECTED_ASPECT));                
            }
        }, powerUserName);
        
        doTestInTransaction(new FailureTest
        (
            "Normal rm user should not have the permissions to add the protected aspect.",    
            ModelAccessDeniedException.class
        )
        {
            @Override
            public void run() throws Exception
            {
              // try and add the aspect    
              nodeService.addAspect(rmFolder, CUSTOM_PROTECTED_ASPECT, null);
            }           
        }, powerUserName);
        
        doTestInTransaction(new VoidTest()
        {            
            @Override
            public void runImpl() throws Exception
            {
                assertTrue(modelSecurityService.canEditProtectedAspect(rmFolder, CUSTOM_PROTECTED_ASPECT));
                
                // try and add the aspect
                nodeService.addAspect(rmFolder, CUSTOM_PROTECTED_ASPECT, null);
                assertTrue(nodeService.hasAspect(rmFolder, CUSTOM_PROTECTED_ASPECT));
            }
        }, ADMIN_USER);
                               
        // check protected aspect via removeAspect
        doTestInTransaction(new FailureTest
        (
            "Normal rm user should not have the permissions to remove the custom aspect.",    
            ModelAccessDeniedException.class
        )
        {
            @Override
            public void run() throws Exception
            {
                nodeService.removeAspect(rmFolder, CUSTOM_PROTECTED_ASPECT);
            }           
        }, powerUserName);
        
        doTestInTransaction(new VoidTest()
        {            
            @Override
            public void runImpl() throws Exception
            {
                nodeService.removeAspect(rmFolder, CUSTOM_PROTECTED_ASPECT);
                assertFalse(nodeService.hasAspect(rmFolder, CUSTOM_PROTECTED_ASPECT));
            }
        }, ADMIN_USER);
    }
    
    /**
     * test - getProtectedProperties(), isProtectedProperty(), getProtectedProperty()
     */
    public void testProtectedProperties() throws Exception
    {
        doTestInTransaction(new VoidTest()
        {
            public void runImpl()
            {
                Set<QName> protectedProperties = modelSecurityService.getProtectedProperties();
                
                assertNotNull(protectedProperties);
                assertFalse(protectedProperties.isEmpty());
                assertTrue(protectedProperties.contains(CUSTOM_PROTECTED_PROPERTY));
                assertFalse(protectedProperties.contains(CUSTOM_PROPERTY));  
                
                assertTrue(modelSecurityService.isProtectedProperty(CUSTOM_PROTECTED_PROPERTY));
                assertFalse(modelSecurityService.isProtectedProperty(CUSTOM_PROPERTY));
                
                ProtectedProperty protectedProperty = modelSecurityService.getProtectedProperty(CUSTOM_PROPERTY);
                assertNull(protectedProperty);
                protectedProperty = modelSecurityService.getProtectedProperty(CUSTOM_PROTECTED_PROPERTY);
                assertNotNull(protectedProperty);
                assertNotNull(protectedProperty.getQName());
                assertNotNull(protectedProperty.getCapabilities());                              
            }
        });  
        
        doTestInTransaction(new VoidTest()
        {            
            @Override
            public void runImpl() throws Exception
            {
                assertTrue(modelSecurityService.canEditProtectedProperty(rmFolder, CUSTOM_PROTECTED_PROPERTY));
            }
        }, ADMIN_USER);
        
        doTestInTransaction(new VoidTest()
        {            
            @Override
            public void runImpl() throws Exception
            {
                assertFalse(modelSecurityService.canEditProtectedProperty(rmFolder, CUSTOM_PROTECTED_PROPERTY));                
            }
        }, powerUserName);  
        
        doTestInTransaction(new VoidTest()
        {            
            @Override
            public void runImpl() throws Exception
            {
                assertTrue(modelSecurityService.isProtectedProperty(CUSTOM_PROTECTED_PROPERTY));
                assertFalse(modelSecurityService.canEditProtectedProperty(rmFolder, CUSTOM_PROTECTED_PROPERTY));                
            }
        }, powerUserName);
        
        doTestInTransaction(new FailureTest
        (
            "Should not have the permissions to edit protected property.",    
            ModelAccessDeniedException.class
        )
        {
            @Override
            public void run() throws Exception
            {
                nodeService.setProperty(rmFolder, CUSTOM_PROTECTED_PROPERTY, "hello");
            }           
        }, powerUserName);
        
        doTestInTransaction(new VoidTest()
        {            
            @Override
            public void runImpl() throws Exception
            {
                assertTrue(modelSecurityService.canEditProtectedProperty(rmFolder, CUSTOM_PROTECTED_PROPERTY));
                
                nodeService.setProperty(rmFolder, CUSTOM_PROTECTED_PROPERTY, "hello");
                assertEquals("hello", nodeService.getProperty(rmFolder, CUSTOM_PROTECTED_PROPERTY));
            }
        }, ADMIN_USER);                               
    }
}
