/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.audit.access;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.audit.AuditComponent;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.debug.NodeStoreInspector;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;

/**
 * Integration test for AccessAuditor.
 * 
 * @author Alan Davis
 */
public class AccessAuditorTest
{
    // Integration test environment
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    private static ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
    private static NodeService nodeService = serviceRegistry.getNodeService();
    private static TransactionService transactionService = serviceRegistry.getTransactionService(); 
    private static NamespaceService namespaceService = serviceRegistry.getNamespaceService();
    private static PolicyComponent policyComponent = (PolicyComponent) ctx.getBean("policyComponent");
    private static AuthenticationComponent authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
    
    // Integration test data store
    private static StoreRef storeRef;
    private static NodeRef homeFolder;
    private static NodeRef folder0;
    private static NodeRef folder1;
    private static NodeRef folder2;
    private static NodeRef folder3;
    private static NodeRef content0;
    private static NodeRef content1;
    private static NodeRef content2;
    private static NodeRef content3;
    
    // Test setup
    private static AccessAuditor auditor;
    private static Properties properties;
    private static NodeRef workingCopyNodeRef;
    private UserTransaction txn;

    // To check results
    private static List<Map<String, Serializable>> auditMapList = new ArrayList<Map<String, Serializable>>();
    
    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        AuthenticationUtil.setRunAsUserSystem();
        
        storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
        
        homeFolder = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "homeFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();

        folder0 = newFolder(homeFolder, "folder0");
        folder1 = newFolder(homeFolder, "folder1");
        folder2 = newFolder(homeFolder, "folder2");
        folder3 = newFolder(homeFolder, "folder3");
        
        content0 = newContent(folder0, "content0");
        content1 = newContent(folder1, "content1");
        content2 = newContent(folder2, "content2");
        content3 = newContent(folder3, "content3");

        System.out.println(NodeStoreInspector.dumpNodeStore(nodeService, storeRef));

        try
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            // ignore
        }

        // Mock up an auditComponent to see the results of our tests
        AuditComponent auditComponent = mock(AuditComponent.class);
        when(auditComponent.areAuditValuesRequired(anyString())).thenReturn(true);
        when(auditComponent.recordAuditValues(anyString(), anyMap())).thenAnswer(new Answer<Map<String, Serializable>>()
                {
                    public Map<String, Serializable> answer(InvocationOnMock invocation) throws Throwable
                    {
                        Object[] args = invocation.getArguments();
                        Map<String, Serializable> auditMap = (Map<String, Serializable>)args[1];
                        if ("/alfresco-access/transaction".equals(args[0]))
                        {
                            auditMapList.add(auditMap);
                        }
                        return auditMap;
                    }
                });

        // Create our own properties object for use by the auditor
        properties = new Properties();
        properties.put("audit.alfresco-access.sub-actions.enabled", "false");

        // Set properties
        auditor = new AccessAuditor();
        auditor.setTransactionService(transactionService);
        auditor.setNamespaceService(namespaceService);
        auditor.setNodeInfoFactory(new NodeInfoFactory(nodeService, namespaceService));
        auditor.setPolicyComponent(policyComponent);
        auditor.setProperties(properties);
        auditor.setAuditComponent(auditComponent);
        
        // Simulate spring call after properties set
        auditor.afterPropertiesSet();    
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
        AuthenticationUtil.setRunAsUserSystem();

        System.out.println(NodeStoreInspector.dumpNodeStore(nodeService, storeRef));
        
        nodeService.deleteStore(storeRef);
        
        try
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            // ignore
        }
        
        properties = null;
        auditor = null;
    }

    @Before
    public void setUp() throws Exception
    {
        // authenticate
        authenticationComponent.setSystemUserAsCurrentUser();
        
        // start the transaction
        txn = transactionService.getUserTransaction();
        txn.begin();
    }

    @After
    public void tearDown() throws Exception
    {
        try
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            // ignore
        }

        try
        {
            if (txn != null)
            {
                txn.rollback();
            }
        }
        catch (Throwable e)
        {
            // ignore
        }
        
        auditMapList.clear();
    }

    private static NodeRef newFolder(NodeRef parent, String name)
    {
        return serviceRegistry.getFileFolderService().create(
                parent,
                name,
                ContentModel.TYPE_FOLDER).getNodeRef();
    }

    private static NodeRef newContent(NodeRef parent, String name)
    {
        PropertyMap propertyMap0 = new PropertyMap();
        propertyMap0.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-16", Locale.ENGLISH));
        NodeRef content = nodeService.createNode(
                parent,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                ContentModel.TYPE_CONTENT,
                propertyMap0).getChildRef();
        ContentWriter writer = serviceRegistry.getContentService().getWriter(content, ContentModel.TYPE_CONTENT, true);
        writer.putContent("The cat sat on the mat.");
        
        return content;
    }

    private Map<String, Serializable> getVersionProperties()
    {
        Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
        versionProperties.put(Version.PROP_DESCRIPTION, "This is a test");
        return versionProperties;
    }

    private void assertContains(String expected, Serializable actual)
    {
        String actualString = (String)actual;
        if (actual == null || !actualString.contains(expected))
        {
            throw new ComparisonFailure("Expected not contained in actual.", expected, actualString);
        }
    }

    @Test
    public final void testOnCreateNodeAndOnUpdateProperties() throws Exception
    {
        newContent(homeFolder, "content4");

        txn.commit();
        txn = null;
        
        assertEquals(1, auditMapList.size());
        Map<String, Serializable> auditMap = auditMapList.get(0);
        assertEquals("CREATE", auditMap.get("action"));
        assertContains("createNode", auditMap.get("sub-actions"));
        assertContains("updateNodeProperties", auditMap.get("sub-actions"));
        assertEquals("/cm:homeFolder/cm:content4", auditMap.get("path"));
        assertEquals("cm:content", auditMap.get("type"));
    }

    @Test
    public final void testOnCopyComplete() throws Exception
    {
      serviceRegistry.getFileFolderService().copy(content2, folder1, null); // keep leaf name

      txn.commit();
      txn = null;
      
      // TODO do we record the parent or the full path? Do we need to?

      assertEquals(1, auditMapList.size());
      Map<String, Serializable> auditMap = auditMapList.get(0);
      assertEquals("COPY", auditMap.get("action"));
      assertContains("createNode", auditMap.get("sub-actions"));
      assertContains("updateNodeProperties", auditMap.get("sub-actions"));
      assertContains("addNodeAspect", auditMap.get("sub-actions"));
      assertContains("copyNode", auditMap.get("sub-actions"));
      assertEquals("/cm:homeFolder/cm:folder1/cm:content2", auditMap.get("path"));
      assertEquals("/cm:homeFolder/cm:folder2/cm:content2", auditMap.get("copy/from/path"));
      assertEquals("cm:content", auditMap.get("type"));
    }

    @Test
    public final void testOnCopyCompleteAndNewName() throws Exception
    {
      serviceRegistry.getFileFolderService().copy(content2, folder1, "newName1");

      txn.commit();
      txn = null;
      
      // TODO do we record the parent or the full path? Do we need to?

      assertEquals(1, auditMapList.size());
      Map<String, Serializable> auditMap = auditMapList.get(0);
      assertEquals("COPY", auditMap.get("action"));
      assertContains("createNode", auditMap.get("sub-actions"));
      assertContains("updateNodeProperties", auditMap.get("sub-actions"));
      assertContains("addNodeAspect", auditMap.get("sub-actions"));
      assertContains("copyNode", auditMap.get("sub-actions"));
      assertEquals("/cm:homeFolder/cm:folder1/cm:newName1", auditMap.get("path"));
      assertEquals("/cm:homeFolder/cm:folder2/cm:content2", auditMap.get("copy/from/path"));
      assertEquals("cm:content", auditMap.get("type"));
    }

    @Test
    public final void testOnMoveNode() throws Exception
    {
        serviceRegistry.getNodeService().moveNode(content3, folder1, ContentModel.ASSOC_CONTAINS,  null);  // keep leaf name

        txn.commit();
        txn = null;
        
        // TODO do we record the parent or the full path? Do we need to?
        
        assertEquals(1, auditMapList.size());
        Map<String, Serializable> auditMap = auditMapList.get(0);
        assertEquals("MOVE", auditMap.get("action"));
        assertContains("moveNode", auditMap.get("sub-actions"));
        assertEquals("/cm:homeFolder/cm:folder1/cm:content3", auditMap.get("path"));
        assertEquals("/cm:homeFolder/cm:folder3/cm:content3", auditMap.get("move/from/path"));
        assertEquals("cm:content", auditMap.get("type"));
    }

    @Test
    public final void testOnMoveNodeAndNewName() throws Exception
    {
        serviceRegistry.getNodeService().moveNode(content3, folder1, ContentModel.ASSOC_CONTAINS,  QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "newName2"));

        txn.commit();
        txn = null;
        
        assertEquals(1, auditMapList.size());
        Map<String, Serializable> auditMap = auditMapList.get(0);
        assertEquals("MOVE", auditMap.get("action"));
        assertContains("moveNode", auditMap.get("sub-actions"));
        assertEquals("/cm:homeFolder/cm:folder1/cm:newName2", auditMap.get("path"));
        assertEquals("/cm:homeFolder/cm:folder1/cm:content3", auditMap.get("move/from/path"));
        assertEquals("cm:content", auditMap.get("type"));
    }

    @Test
    public final void testBeforeDeleteNode() throws Exception
    {
        serviceRegistry.getNodeService().deleteNode(content0);

        txn.commit();
        txn = null;
        
        assertEquals(1, auditMapList.size());
        Map<String, Serializable> auditMap = auditMapList.get(0);
        assertEquals("DELETE", auditMap.get("action"));
        assertContains("deleteNode", auditMap.get("sub-actions"));
        assertEquals("/cm:homeFolder/cm:folder0/cm:content0", auditMap.get("path"));
        assertEquals("cm:content", auditMap.get("type"));
    }

    @Test
    public final void testOnAddAspect() throws Exception
    {
        serviceRegistry.getNodeService().addAspect(content1, ContentModel.ASPECT_AUTHOR, null);
        serviceRegistry.getNodeService().addAspect(content1, ContentModel.ASPECT_OWNABLE, null);

        txn.commit();
        txn = null;
        
        assertEquals(1, auditMapList.size());
        Map<String, Serializable> auditMap  = auditMapList.get(0);
        assertEquals("addNodeAspect", auditMap.get("action"));
        assertContains("addNodeAspect", auditMap.get("sub-actions"));
        assertEquals("/cm:homeFolder/cm:folder1/cm:content1", auditMap.get("path"));
        assertEquals(2, ((Set<?>)auditMap.get("aspects/add")).size());
        assertTrue("Individual author aspect missing", auditMap.containsKey("aspects/add/cm:author"));
        assertTrue("Individual ownable aspect missing", auditMap.containsKey("aspects/add/cm:ownable"));
        assertEquals("cm:content", auditMap.get("type"));
    }

    @Test
    public final void testOnRemoveAspect() throws Exception
    {
        serviceRegistry.getNodeService().removeAspect(content1, ContentModel.ASPECT_AUTHOR);

        txn.commit();
        txn = null;
        
        assertEquals(1, auditMapList.size());
        Map<String, Serializable> auditMap = auditMapList.get(0);
        assertEquals("deleteNodeAspect", auditMap.get("action"));
        assertContains("deleteNodeAspect", auditMap.get("sub-actions"));
        assertEquals("/cm:homeFolder/cm:folder1/cm:content1", auditMap.get("path"));
        assertEquals(1, ((Set<?>)auditMap.get("aspects/delete")).size());
        assertTrue("Individual author aspect missing", auditMap.containsKey("aspects/delete/cm:author"));
        assertEquals("cm:content", auditMap.get("type"));
    }

    @Test
    public final void testOnContentUpdate() throws Exception
    {
        ContentWriter writer = serviceRegistry.getContentService().getWriter(content1, ContentModel.TYPE_CONTENT, true);
        writer.putContent("The cow jumped over the moon.");

        txn.commit();
        txn = null;

        assertEquals(1, auditMapList.size());
        Map<String, Serializable> auditMap = auditMapList.get(0);
        assertEquals("UPDATE CONTENT", auditMap.get("action")); // TODO Should be UPDATE CONTENT
        assertContains("updateContent", auditMap.get("sub-actions"));
        assertContains("updateNodeProperties", auditMap.get("sub-actions"));
        assertEquals("/cm:homeFolder/cm:folder1/cm:content1", auditMap.get("path"));
        assertEquals("cm:content", auditMap.get("type"));
    }

    @Test
    public final void testOnContentRead() throws Exception
    {
        serviceRegistry.getContentService().getReader(content1, ContentModel.TYPE_CONTENT);

        txn.commit();
        txn = null;

        assertEquals(1, auditMapList.size());
        Map<String, Serializable> auditMap = auditMapList.get(0);
        assertEquals("READ", auditMap.get("action"));
        assertContains("readContent",  auditMap.get("sub-actions"));
        assertEquals("/cm:homeFolder/cm:folder1/cm:content1", auditMap.get("path"));
        assertEquals("cm:content", auditMap.get("type"));
    }

    @Test
    public final void testOnCreateVersion() throws Exception
    {
       Map<String, Serializable> versionProperties = getVersionProperties();
       serviceRegistry.getVersionService().createVersion(content1, versionProperties);

        txn.commit();
        txn = null;
        
        assertEquals(1, auditMapList.size());
        Map<String, Serializable> auditMap = auditMapList.get(0);
        assertEquals("CREATE VERSION", auditMap.get("action"));
        assertContains("updateNodeProperties", auditMap.get("sub-actions"));
        assertContains("createVersion", auditMap.get("sub-actions"));
        assertEquals("/cm:homeFolder/cm:folder1/cm:content1", auditMap.get("path"));
        assertTrue("cm:versionable should be a value with in the set", ((Set<?>)auditMap.get("aspects/add")).contains(ContentModel.ASPECT_VERSIONABLE));
        assertTrue("Individual versionable aspect should exist", auditMap.containsKey("aspects/add/cm:versionable"));
        assertEquals("cm:content", auditMap.get("type"));
    }

    @Test
    public final void testOnCheckOut() throws Exception
    {
        workingCopyNodeRef = serviceRegistry.getCheckOutCheckInService().checkout(content1);
        
        txn.commit();
        txn = null;

        assertEquals(2, auditMapList.size());
        boolean origIn0 = ((String)auditMapList.get(0).get("path")).endsWith("cm:content1");
        Map<String, Serializable> origMap = auditMapList.get(origIn0 ? 0 : 1);
        Map<String, Serializable> workMap = auditMapList.get(origIn0 ? 1 : 0);
        
        // original file
        assertEquals("addNodeAspect", origMap.get("action"));
        // createNode createContent readContent updateNodeProperties addNodeAspect copyNode checkOut createVersion
        assertContains("updateNodeProperties", origMap.get("sub-actions"));
        assertEquals("cm:content", origMap.get("type"));
        assertEquals("/cm:homeFolder/cm:folder1/cm:content1", origMap.get("path"));

        // working copy
        assertEquals("CHECK OUT", workMap.get("action"));
        assertContains("createNode", workMap.get("sub-actions"));
        assertContains("createContent", workMap.get("sub-actions"));
        assertContains("updateNodeProperties", workMap.get("sub-actions"));
        assertContains("addNodeAspect", workMap.get("sub-actions"));
        assertContains("copyNode", workMap.get("sub-actions"));
        assertContains("checkOut", workMap.get("sub-actions"));
        assertContains("createVersion", workMap.get("sub-actions"));
        assertTrue("Expected working copy", ((String)workMap.get("path")).endsWith("(Working Copy)") &&
                                            ((String)workMap.get("path")).startsWith("/cm:homeFolder/cm:folder1/"));
        assertEquals("cm:content", workMap.get("type"));
    }

    @Test
    public final void testOnCheckIn() throws Exception
    {
        Map<String, Serializable> checkinProperties = new HashMap<String, Serializable>();
        checkinProperties.put(Version.PROP_DESCRIPTION, null);
        checkinProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
        serviceRegistry.getCheckOutCheckInService().checkin(workingCopyNodeRef, checkinProperties);
        
        txn.commit();
        txn = null;

        assertEquals(2, auditMapList.size());
        boolean origIn0 = ((String)auditMapList.get(0).get("path")).endsWith("cm:content1");
        Map<String, Serializable> origMap = auditMapList.get(origIn0 ? 0 : 1);
        Map<String, Serializable> workMap = auditMapList.get(origIn0 ? 1 : 0);

        // working copy
        assertEquals("DELETE", workMap.get("action"));
        assertContains("deleteNode", workMap.get("sub-actions"));
        assertTrue("Expected working copy", ((String)workMap.get("path")).endsWith("(Working Copy)") &&
                                            ((String)workMap.get("path")).startsWith("/cm:homeFolder/cm:folder1/"));
        assertEquals("cm:content", workMap.get("type"));

        // original file
        assertEquals("CHECK IN", origMap.get("action"));
        assertContains("deleteNodeAspect", origMap.get("sub-actions"));     
        assertContains("addNodeAspect", origMap.get("sub-actions"));
        assertContains("copyNode", origMap.get("sub-actions"));
        assertContains("createVersion", origMap.get("sub-actions"));
        assertContains("updateNodeProperties", origMap.get("sub-actions"));
        assertContains("checkIn", origMap.get("sub-actions"));
        assertEquals("/cm:homeFolder/cm:folder1/cm:content1", origMap.get("path"));
        assertEquals("cm:content", origMap.get("type"));
    }

    @Test
    public final void testOnCancelCheckOut() throws Exception
    {
        workingCopyNodeRef = serviceRegistry.getCheckOutCheckInService().checkout(content1);
        txn.commit();
        txn = null;

        tearDown();
        setUp();
        
        serviceRegistry.getCheckOutCheckInService().cancelCheckout(workingCopyNodeRef);
        
        txn.commit();
        txn = null;

        assertEquals(2, auditMapList.size());
        boolean origIn0 = ((String)auditMapList.get(0).get("path")).endsWith("cm:content1");
        Map<String, Serializable> origMap = auditMapList.get(origIn0 ? 0 : 1);
        Map<String, Serializable> workMap = auditMapList.get(origIn0 ? 1 : 0);
        
        // working copy
        assertEquals("DELETE", workMap.get("action"));
        assertContains("deleteNode", workMap.get("sub-actions"));
        assertTrue("Expected working copy", ((String)workMap.get("path")).endsWith("(Working Copy)") &&
                                              ((String)workMap.get("path")).startsWith("/cm:homeFolder/cm:folder1/"));
        assertEquals("cm:content", workMap.get("type"));

        // original file
        assertEquals("CANCEL CHECK OUT", origMap.get("action"));
        assertContains("deleteNodeAspect", origMap.get("sub-actions"));
        assertContains("cancelCheckOut", origMap.get("sub-actions"));
        assertEquals("/cm:homeFolder/cm:folder1/cm:content1", origMap.get("path"));
        assertEquals("cm:content", origMap.get("type"));
    }
}
