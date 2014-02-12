/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.tenant;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * Test checking the behaviour of the MT {@link ContentStore} routing 
 * 
 * @author Alfresco
 * @since 4.2.1
 */
public class AbstractTenantRoutingContentStoreTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private ContentService contentService;
    private ServiceRegistry serviceRegistry;
    private AbstractTenantRoutingContentStore fileContentStore;
    private boolean isNullEntry = false;

    @Override
    public void setUp() throws Exception
    {
        AuthenticationUtil.setRunAsUserSystem();

        serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        contentService = serviceRegistry.getContentService();
        fileContentStore = (AbstractTenantRoutingContentStore) ctx.getBean("fileContentStore");
    }

    public void testGetAllStores() throws Exception
    {
        final RetryingTransactionCallback<Object> txnWork = new RetryingTransactionCallback<Object>()
        {
            public Boolean execute() throws Exception
            {
                // The listener
                final TestAfterRollbackTxnListener listener = new TestAfterRollbackTxnListener();
                AlfrescoTransactionSupport.bindListener(listener);

                NodeRef content = createContent(serviceRegistry);

                ContentWriter writer = contentService.getWriter(content, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.putContent("The quick brown fox jumps over the lazy dog");

                throw new AlfrescoRuntimeException("Some error that causes RollBack. The transaction will be closed");
            }
        };

        try
        {
            serviceRegistry.getTransactionService().getRetryingTransactionHelper().doInTransaction(txnWork, false, false);
        }
        catch (AlfrescoRuntimeException e)
        {
            // Expected
        }

        assertFalse("getAllStores method returned the list with null entry", isNullEntry);
    }

    // helper methods and listener
    private void checkStores(List<ContentStore> stores)
    {
        // check that list is not empty
        assertEquals(false, stores.isEmpty());

        // check for null entry
        for (ContentStore store : stores)
        {
            if (store == null)
            {
                isNullEntry = true;
            }
        }
    }

    private NodeRef createContent(ServiceRegistry serviceRegistry) throws Exception
    {
        AuthenticationService authenticationService = serviceRegistry.getAuthenticationService();
        authenticationService.authenticate("admin", "admin".toCharArray());
        SearchService searchService = serviceRegistry.getSearchService();
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

        try
        {
            ResultSet resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home\"");
            NodeRef companyHome = resultSet.getNodeRef(0);

            serviceRegistry.getFileFolderService().create(companyHome, "Test", ContentModel.TYPE_FOLDER);
            resultSet.close();
        }
        catch (Exception e)
        {
        }

        ResultSet resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home/cm:Test\"");
        NodeRef node = resultSet.getNodeRef(0);
        resultSet.close();

        // assign name
        String name = "TestContent(" + Thread.currentThread().getName() + " " + System.currentTimeMillis() + ")";
        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
        contentProps.put(ContentModel.PROP_NAME, name);

        // create content node
        NodeService nodeService = serviceRegistry.getNodeService();
        ChildAssociationRef association = nodeService.createNode(node, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, name),
                ContentModel.TYPE_CONTENT, contentProps);
        NodeRef content = association.getChildRef();

        // add titled aspect (for Web Client display)
        Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>();
        titledProps.put(ContentModel.PROP_TITLE, name);
        titledProps.put(ContentModel.PROP_DESCRIPTION, name);
        nodeService.addAspect(content, ContentModel.ASPECT_TITLED, titledProps);

        return content;
    }

    private class TestAfterRollbackTxnListener extends TransactionListenerAdapter
    {
        @Override
        public void afterRollback()
        {
            checkStores(fileContentStore.getAllStores());
        }
    }
}
