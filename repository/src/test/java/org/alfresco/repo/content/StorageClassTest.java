/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.content;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


@Category(OwnJVMTestsCategory.class)
@RunWith(SpringRunner.class)
public class StorageClassTest extends BaseSpringTest
{
        private static final String DEFAULT_SC = "Default1";
        private static final String TEST_NAMESPACE = "TestNameSpace";
        private TransactionService transactionService;
        private AuthenticationComponent authenticationComponent;

        private NodeService nodeService;
        private NodeRef rootNode;
        @Spy
        ContentStore mockContentStore;
        ContentService contentService;
        ContentStore contentStore;

        @Before
        public void before() throws Exception
        {
                nodeService = (NodeService) applicationContext.getBean("NodeService");
                transactionService = (TransactionService) applicationContext.getBean("TransactionService");
                this.authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
                this.contentService = (ContentService) this.applicationContext.getBean("contentService");
                this.contentStore = (ContentStore) ReflectionTestUtils.getField(contentService, "store");

                FileContentStore fileContentStore = new FileContentStore(applicationContext, this.contentStore.getRootLocation());
                mockContentStore = spy(fileContentStore);

                when(mockContentStore.getSupportedStorageClasses()).thenReturn(Set.of(DEFAULT_SC, "Azure", "S3"));
                ReflectionTestUtils.setField(contentService, "store", mockContentStore);

                this.authenticationComponent.setSystemUserAsCurrentUser();

                StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "testStoreRef");
                if(!nodeService.exists(storeRef))
                {
                        storeRef = nodeService.createStore(storeRef.getProtocol(), storeRef.getIdentifier());
                }
                rootNode = nodeService.getRootNode(storeRef);
        }

        @Test
        public void testDefaultGetSupportedStorageClasses()
        {
                assertTrue("Current supported storage classes: " + contentService.getSupportedStorageClasses(), contentService.getSupportedStorageClasses().contains("Default1"));
        }

        @Test
        public void testGetSupportedStorageClasses()
        {
                assertTrue("Expected DEFAULT_SC ", contentService.getSupportedStorageClasses().contains(DEFAULT_SC));
        }

        @Test
        public void getDefaultStorageClassesTransition()
        {
                assertTrue("Expected DEFAULT_SC ", contentService.getStorageClassesTransitions().isEmpty());
        }

        @Test
        public void getStorageClassesTransitions()
        {
                var key1 = Set.of("Default");
                var key2 = Set.of("Warm");
                var value1 = Set.of(Set.of("Archive"));
                Map<Set<String>,Set<Set<String>>> map = new HashMap<>();
                map.put(key1, value1);
                map.put(key2, value1);

                when(mockContentStore.getStorageClassesTransitions()).thenReturn(map);
                ReflectionTestUtils.setField(contentService, "store",mockContentStore);

                assertTrue("Obtained" + contentService.getStorageClassesTransitions(), contentService.getStorageClassesTransitions().containsKey(key1));
                assertTrue("Obtained" + contentService.getStorageClassesTransitions(), contentService.getStorageClassesTransitions().containsValue(value1));
        }

        @Test
        public void findDefaultStorageClasses() throws SystemException, NotSupportedException
        {
                NodeRef contentNodeRef = createNode("testNode1" + GUID.generate(), "testContent1");

                assertTrue("Found default storage classes: ", contentService.findStorageClasses(contentNodeRef).isEmpty());
        }

        @Test
        public void findStorageClasses() throws NotSupportedException, SystemException
        {
                NodeRef contentNodeRef = createNode("testNode" + GUID.generate(), "testContent");
                String contentUrl = contentService.getReader(contentNodeRef, ContentModel.TYPE_CONTENT).getContentUrl();

                when(mockContentStore.findStorageClasses(contentUrl)).thenReturn(Set.of("Azure"));
                ReflectionTestUtils.setField(contentService, "store",mockContentStore);

                assertTrue("Found storage classes: " + contentService.findStorageClasses(contentNodeRef), contentService.findStorageClasses(contentNodeRef).contains("Azure"));
        }

        @Test
        public void findDefaultStorageClassesTransitions() throws SystemException, NotSupportedException
        {
                NodeRef contentNodeRef = createNode("testNode1" + GUID.generate(), "testContent2");

                ReflectionTestUtils.setField(contentService, "store", mockContentStore);
                assertTrue("Found default storage transition: ", contentService.findStorageClassesTransitions(contentNodeRef).isEmpty());
        }

        @Test
        public void findStorageClassesTransitions() throws NotSupportedException, SystemException
        {
                var key1 = Set.of("Default");
                var key2 = Set.of("Warm");
                var value1 = Set.of(Set.of("Archive"));
                Map<Set<String>,Set<Set<String>>> map = new HashMap<>();
                map.put(key1, value1);
                map.put(key2, value1);

                NodeRef contentNodeRef = createNode("testNode" + GUID.generate(), "testContent");
                String contentUrl = contentService.getReader(contentNodeRef, ContentModel.TYPE_CONTENT).getContentUrl();

                when(mockContentStore.findStorageClassesTransitions(contentUrl)).thenReturn(map);
                ReflectionTestUtils.setField(contentService, "store",mockContentStore);

                assertTrue("Obtained" + contentService.findStorageClassesTransitions(contentNodeRef), contentService.findStorageClassesTransitions(contentNodeRef).containsKey(key1));
                assertTrue("Obtained" + contentService.findStorageClassesTransitions(contentNodeRef), contentService.findStorageClassesTransitions(contentNodeRef).containsValue(value1));        }

        private NodeRef createNode(String name, String testContent) throws SystemException, NotSupportedException
        {
                // start the transaction
                RetryingTransactionHelper.RetryingTransactionCallback<NodeRef> makeContentCallback = new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                {
                        public NodeRef execute() throws Throwable
                        {
                                Map<QName, Serializable> nodeProperties = new HashMap<>();
                                nodeProperties.put(ContentModel.PROP_NAME, name);

                                ChildAssociationRef assocRef = nodeService
                                        .createNode(rootNode, ContentModel.ASSOC_CHILDREN, QName.createQName(TEST_NAMESPACE, GUID.generate()),
                                                ContentModel.TYPE_CONTENT, nodeProperties);
                                NodeRef contentNodeRef = assocRef.getChildRef();

                                // Add the content to the node
                                ContentWriter contentWriter = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                                contentWriter.putContent(testContent);
                                return contentNodeRef;
                        }
                };

                return transactionService.getRetryingTransactionHelper().doInTransaction(makeContentCallback);

        }

}
