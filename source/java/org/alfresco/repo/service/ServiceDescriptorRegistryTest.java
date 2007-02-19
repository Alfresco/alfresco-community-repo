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
package org.alfresco.repo.service;

import java.util.Collection;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.service.ServiceDescriptor;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ServiceDescriptorRegistryTest extends TestCase
{
    
    private ApplicationContext factory = null;
    
    private static String TEST_NAMESPACE = "http://www.alfresco.org/test/serviceregistrytest";
    private static QName invalidService = QName.createQName(TEST_NAMESPACE, "invalid");
    private static QName service1 = QName.createQName(TEST_NAMESPACE, "service1");
    private static QName service2 = QName.createQName(TEST_NAMESPACE, "service2");
    private static QName service3 = QName.createQName(TEST_NAMESPACE, "service3");

    
    public void setUp()
    {
        factory = new ClassPathXmlApplicationContext("org/alfresco/repo/service/testregistry.xml");
    }
    
    public void testDescriptor()
    {
        ServiceRegistry registry = (ServiceRegistry)factory.getBean("serviceRegistry");
        
        Collection services = registry.getServices();
        assertNotNull(services);
        assertEquals(3, services.size());
        
        assertTrue(registry.isServiceProvided(service1));
        assertFalse(registry.isServiceProvided(invalidService));

        ServiceDescriptor invalid = registry.getServiceDescriptor(invalidService);
        assertNull(invalid);
        ServiceDescriptor desc1 = registry.getServiceDescriptor(service1);
        assertNotNull(desc1);
        assertEquals(service1, desc1.getQualifiedName());
        assertEquals("Test Service 1", desc1.getDescription());
        assertEquals(TestServiceInterface.class, desc1.getInterface());
        ServiceDescriptor desc2 = registry.getServiceDescriptor(service2);
        assertNotNull(desc2);
        assertEquals(service2, desc2.getQualifiedName());
        assertEquals("Test Service 2", desc2.getDescription());
        assertEquals(TestServiceInterface.class, desc2.getInterface());
    }

    
    public void testService()
    {
        ServiceRegistry registry = (ServiceRegistry)factory.getBean("serviceRegistry");
        
        TestServiceInterface theService1 = (TestServiceInterface)registry.getService(service1);
        assertNotNull(service1);
        assertEquals("Test1:service1", theService1.test("service1"));
        TestServiceInterface theService2 = (TestServiceInterface)registry.getService(service2);
        assertNotNull(service2);
        assertEquals("Test2:service2", theService2.test("service2"));
    }
    

    public void testStores()
    {
        ServiceRegistry registry = (ServiceRegistry)factory.getBean("serviceRegistry");
        
        ServiceDescriptor desc3 = registry.getServiceDescriptor(service3);
        assertNotNull(desc3);
        StoreRedirector theService3 = (StoreRedirector)registry.getService(service3);
        assertNotNull(service3);
        
        Collection<String> descStores = desc3.getSupportedStoreProtocols();
        assertTrue(descStores.contains("Type1"));
        assertTrue(descStores.contains("Type2"));
        assertFalse(descStores.contains("Invalid"));
        
        Collection<String> serviceStores = theService3.getSupportedStoreProtocols();
        for (String store: descStores)
        {
            assertTrue(serviceStores.contains(store));
        }
    }
    
    
    public void testAppContext()
    {
        ApplicationContext appContext = new ClassPathXmlApplicationContext("alfresco/application-context.xml");

        ServiceRegistry registry = (ServiceRegistry)appContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        assertNotNull(registry);
        NodeService s1 = registry.getNodeService();
        assertNotNull(s1);
        CheckOutCheckInService s2 = registry.getCheckOutCheckInService();
        assertNotNull(s2);
        ContentService s3 = registry.getContentService();
        assertNotNull(s3);
        CopyService s4 = registry.getCopyService();
        assertNotNull(s4);
        DictionaryService s5 = registry.getDictionaryService();
        assertNotNull(s5);
        LockService s6 = registry.getLockService();
        assertNotNull(s6);
        MimetypeService s7 = registry.getMimetypeService();
        assertNotNull(s7);
        SearchService s8 = registry.getSearchService();
        assertNotNull(s8);
        TransactionService transactionService = registry.getTransactionService();
        UserTransaction s9 = transactionService.getUserTransaction();
        assertNotNull(s9);
        UserTransaction s10 = transactionService.getUserTransaction();
        assertNotNull(s10);
        assertFalse(s9.equals(s10));
        VersionService s11 = registry.getVersionService();
        assertNotNull(s11);
    }
    
    
    public interface TestServiceInterface
    {
        public String test(String arg);
    }

    public static abstract class Component implements TestServiceInterface
    {
        private String type;
        
        private Component(String type)
        {
            this.type = type;
        }
        
        public String test(String arg)
        {
            return type + ":" + arg;
        }
    }
    
    public static class Test1Component extends Component
    {
        private Test1Component()
        {
            super("Test1");
        }
    }

    public static class Test2Component extends Component
    {
        private Test2Component()
        {
            super("Test2");
        }
    }
    
}
