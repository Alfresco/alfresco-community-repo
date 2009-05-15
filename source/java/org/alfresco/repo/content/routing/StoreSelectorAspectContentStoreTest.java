/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.content.routing;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServiceImpl;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.repo.content.routing.StoreSelectorAspectContentStore.StoreSelectorConstraint;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.TempFileProvider;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Tests {@link StoreSelectorAspectContentStore}
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class StoreSelectorAspectContentStoreTest extends TestCase
{
    private static final String STORE_ONE = "Store1";
    private static final String STORE_TWO = "Store2";
    private static final String STORE_THREE = "Store3";
    
    private static ConfigurableApplicationContext ctx =
        (ConfigurableApplicationContext) ApplicationContextHelper.getApplicationContext();
    
    private TransactionService transactionService;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    
    private Map<String, ContentStore> storesByName;
    private FileContentStore fileStore1;
    private FileContentStore fileStore2;
    private FileContentStore fileStore3;
    private StoreSelectorAspectContentStore store;
    private NodeRef contentNodeRef;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();
        fileFolderService = serviceRegistry.getFileFolderService();
        
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        fileStore1 = new FileContentStore(
                ctx,
                TempFileProvider.getSystemTempDir() + "/fileStore1");
        fileStore2 = new FileContentStore(
                ctx,
                TempFileProvider.getSystemTempDir() + "/fileStore2");
        fileStore3 = new FileContentStore(
                ctx,
                TempFileProvider.getSystemTempDir() + "/fileStore3");
        
        storesByName = new HashMap<String, ContentStore>(7);
        storesByName.put(STORE_ONE, fileStore1);
        storesByName.put(STORE_TWO, fileStore2);
        storesByName.put(STORE_THREE, fileStore3);
        
        store = (StoreSelectorAspectContentStore) ctx.getBean("storeSelectorContentStore");
        store.setStoresByName(storesByName);
        store.setDefaultStoreName(STORE_ONE);
        store.afterPropertiesSet();
        
        // Force the constraint to re-initialize
        StoreSelectorConstraint storeConstraint = (StoreSelectorConstraint) ctx.getBean("storeSelectorContentStore.constraint");
        storeConstraint.initialize();
        
        // Change the content service's default store
        ContentServiceImpl contentService = (ContentServiceImpl) ctx.getBean("contentService");
        contentService.setStore(store);
        
        // Create a content node
        RetryingTransactionCallback<NodeRef> makeNodeCallback = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                StoreRef storeRef = nodeService.createStore(
                        StoreRef.PROTOCOL_TEST,
                        getName() + "_" + System.currentTimeMillis());
                NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
                // Create a folder
                NodeRef folderNodeRef = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.ASSOC_CHILDREN,
                        ContentModel.TYPE_FOLDER).getChildRef();
                // Add some content
                return fileFolderService.create(
                        folderNodeRef,
                        getName() + ".txt",
                        ContentModel.TYPE_CONTENT).getNodeRef();
            }
        };
        contentNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(makeNodeCallback);
    }

    @Override
    public void tearDown() throws Exception
    {
        AuthenticationUtil.popAuthentication();
    }

    /**
     * Writes to the file
     * @return          Returns the new content URL
     */
    private String writeToFile()
    {
        RetryingTransactionCallback<String> writeContentCallback = new RetryingTransactionCallback<String>()
        {
            public String execute() throws Throwable
            {
                ContentWriter writer = fileFolderService.getWriter(contentNodeRef);
                writer.putContent("Some test content");
                return writer.getContentUrl();
            }
        };
        return transactionService.getRetryingTransactionHelper().doInTransaction(writeContentCallback);
    }
    
    /**
     * Set the name of the store that must hold the content
     * @param storeName         the name of the store
     */
    private void setStoreNameProperty(String storeName)
    {
        // The nodeService is transactional
        nodeService.setProperty(contentNodeRef, ContentModel.PROP_STORE_NAME, storeName);
    }
    
    /**
     * Ensure that a <tt>null</tt> <b>cm:storeName</b> property is acceptable.
     */
    public void testNullStoreNameProperty() throws Exception
    {
        try
        {
            setStoreNameProperty(null);
        }
        catch (Throwable e)
        {
            throw new Exception("Failed to set store name property to null", e);
        }
    }
    
    /**
     * Ensure that an invalid <b>cm:storeName</b> property is kicked out.
     */
    public void testInvalidStoreNameProperty() throws Exception
    {
        RetryingTransactionCallback<Object> setInvalidStoreNameCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                setStoreNameProperty("bogus");
                return null;
            }
        };
        try
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(setInvalidStoreNameCallback, false, true);
            setStoreNameProperty("bogus");
            fail("Expected integrity error for bogus store name");
        }
        catch (IntegrityException e)
        {
            // Expected
        }
    }
    
    /**
     * Check that the default store is used if the property is not set
     */
    public void testWriteWithoutAspect() throws Exception
    {
        String contentUrl = writeToFile();
        // The content should be in the default store
        assertTrue("Default store does not have content", fileStore1.exists(contentUrl));
        assertFalse("Mapped store should not have content", fileStore2.exists(contentUrl));
        assertFalse("Mapped store should not have content", fileStore3.exists(contentUrl));
    }
    
    public void testSimpleWritesWithAspect() throws Exception
    {
        for (Map.Entry<String, ContentStore> entry : storesByName.entrySet())
        {
            String storeName = entry.getKey();
            ContentStore store = entry.getValue();
            setStoreNameProperty(storeName);
            String contentUrl = writeToFile();
            assertTrue("Content not in store " + storeName, store.exists(contentUrl));
        }
    }
    
    public void testPropertyChange() throws Exception
    {
        setStoreNameProperty(STORE_ONE);
        String contentUrl = writeToFile();
        assertTrue("Store1 should have content", storesByName.get(STORE_ONE).exists(contentUrl));
        assertFalse("Store2 should NOT have content", storesByName.get(STORE_TWO).exists(contentUrl));
        // Change the property
        setStoreNameProperty(STORE_TWO);
        // It should have moved
        assertFalse("Store1 should NOT have content", storesByName.get(STORE_ONE).exists(contentUrl));
        assertTrue("Store2 should have content", storesByName.get(STORE_TWO).exists(contentUrl));
    }
}
