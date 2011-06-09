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
package org.alfresco.repo.node;

import java.io.Serializable;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Tests basic {@link NodeService} functionality
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class NodeServiceTest extends TestCase
{
    public static final String NAMESPACE = "http://www.alfresco.org/test/BaseNodeServiceTest";
    public static final String TEST_PREFIX = "test";
    public static final QName  TYPE_QNAME_TEST = QName.createQName(NAMESPACE, "multiprop");
    public static final QName  PROP_QNAME_NAME = QName.createQName(NAMESPACE, "name");
    public static final QName  ASSOC_QNAME_CHILDREN = QName.createQName(NAMESPACE, "child");
    
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    protected ServiceRegistry serviceRegistry;
    protected NodeService nodeService;
    private TransactionService txnService;
    
    /** populated during setup */
    protected NodeRef rootNodeRef;

    @Override
    protected void setUp() throws Exception
    {
        I18NUtil.setLocale(null);

        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        nodeService = serviceRegistry.getNodeService();
        txnService = serviceRegistry.getTransactionService();
        
        AuthenticationUtil.setRunAsUserSystem();
        
        // create a first store directly
        RetryingTransactionCallback<NodeRef> createStoreWork = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute()
            {
                StoreRef storeRef = nodeService.createStore(
                        StoreRef.PROTOCOL_WORKSPACE,
                        "Test_" + System.nanoTime());
                return nodeService.getRootNode(storeRef);
            }
        };
        rootNodeRef = txnService.getRetryingTransactionHelper().doInTransaction(createStoreWork);
    }
    
    /**
     * Clean up the test thread
     */
    @Override
    protected void tearDown()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
        I18NUtil.setLocale(null);
    }
    
    public void testSetUp() throws Exception
    {
        assertNotNull(rootNodeRef);
    }
    
    public void testLocaleSupport() throws Exception
    {
        // Ensure that the root node has the default locale
        Locale locale = (Locale) nodeService.getProperty(rootNodeRef, ContentModel.PROP_LOCALE);
        assertNotNull("Locale property must occur on every node", locale);
        assertEquals("Expected default locale on the root node", I18NUtil.getLocale(), locale);
        assertTrue("Every node must have sys:localized", nodeService.hasAspect(rootNodeRef, ContentModel.ASPECT_LOCALIZED));
        
        // Now switch to a specific locale and create a new node
        I18NUtil.setLocale(Locale.CANADA_FRENCH);
        
        // Create a node using an explicit locale
        NodeRef nodeRef1 = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, getName()),
                ContentModel.TYPE_CONTAINER,
                Collections.singletonMap(ContentModel.PROP_LOCALE, (Serializable)Locale.GERMAN)).getChildRef();
        assertTrue("Every node must have sys:localized", nodeService.hasAspect(nodeRef1, ContentModel.ASPECT_LOCALIZED));
        assertEquals(
                "Didn't set the explicit locale during create. ",
                Locale.GERMAN, nodeService.getProperty(nodeRef1, ContentModel.PROP_LOCALE));
        
        // Create a node using the thread's locale
        NodeRef nodeRef2 = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, getName()),
                ContentModel.TYPE_CONTAINER).getChildRef();
        assertTrue("Every node must have sys:localized", nodeService.hasAspect(nodeRef2, ContentModel.ASPECT_LOCALIZED));
        assertEquals(
                "Didn't set the locale during create. ",
                Locale.CANADA_FRENCH, nodeService.getProperty(nodeRef2, ContentModel.PROP_LOCALE));
        
        // Switch Locale and modify ml:text property
        I18NUtil.setLocale(Locale.CHINESE);
        nodeService.setProperty(nodeRef2, ContentModel.PROP_DESCRIPTION, "Chinese description");
        I18NUtil.setLocale(Locale.FRENCH);
        nodeService.setProperty(nodeRef2, ContentModel.PROP_DESCRIPTION, "French description");
        
        // Expect that we have MLText (if we are ML aware)
        boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);
        try
        {
            MLText checkDescription = (MLText) nodeService.getProperty(nodeRef2, ContentModel.PROP_DESCRIPTION);
            assertEquals("Chinese description", checkDescription.getValue(Locale.CHINESE));
            assertEquals("French description", checkDescription.getValue(Locale.FRENCH));
        }
        finally
        {
            MLPropertyInterceptor.setMLAware(wasMLAware);
        }
        // But the node locale must not have changed
        assertEquals(
                "Node modification should not affect node locale. ",
                Locale.CANADA_FRENCH, nodeService.getProperty(nodeRef2, ContentModel.PROP_LOCALE));
        
        // Now explicitly set the node's locale
        nodeService.setProperty(nodeRef2, ContentModel.PROP_LOCALE, Locale.ITALY);
        assertEquals(
                "Node locale must be settable. ",
                Locale.ITALY, nodeService.getProperty(nodeRef2, ContentModel.PROP_LOCALE));
        // But mltext must be unchanged
        assertEquals(
                "Canada-French must be closest to French. ",
                "French description", nodeService.getProperty(nodeRef2, ContentModel.PROP_DESCRIPTION));
        
        // Finally, ensure that setting Locale to 'null' is takes the node back to its original locale
        nodeService.setProperty(nodeRef2, ContentModel.PROP_LOCALE, null);
        assertEquals(
                "Node locale set to 'null' does nothing. ",
                Locale.ITALY, nodeService.getProperty(nodeRef2, ContentModel.PROP_LOCALE));
        nodeService.removeProperty(nodeRef2, ContentModel.PROP_LOCALE);
        assertEquals(
                "Node locale removal does nothing. ",
                Locale.ITALY, nodeService.getProperty(nodeRef2, ContentModel.PROP_LOCALE));
        
        // Mass-set the properties, changing the locale in the process
        Map<QName, Serializable> props = nodeService.getProperties(nodeRef2);
        props.put(ContentModel.PROP_LOCALE, Locale.GERMAN);
        nodeService.setProperties(nodeRef2, props);
        assertEquals(
                "Node locale not set in setProperties(). ",
                Locale.GERMAN, nodeService.getProperty(nodeRef2, ContentModel.PROP_LOCALE));
    }
}
