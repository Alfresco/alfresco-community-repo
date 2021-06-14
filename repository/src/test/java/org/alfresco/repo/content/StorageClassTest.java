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
import org.junit.After;
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
@RunWith(SpringRunner.class) public class StorageClassTest extends BaseSpringTest
{
        private static final String DEFAULT_SC = "Default1";
        private static final String TEST_NAMESPACE = "TestNameSpace";
        private TransactionService transactionService;
        private AuthenticationComponent authenticationComponent;

        private NodeService nodeService;
        private NodeRef rootNode;
        @Spy ContentStore mockContentStore;
        ContentService contentService;
        ContentStore contentStore;

        @Before
        public void before()
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
                if (!nodeService.exists(storeRef))
                {
                        storeRef = nodeService.createStore(storeRef.getProtocol(), storeRef.getIdentifier());
                }
                rootNode = nodeService.getRootNode(storeRef);
        }

        @After
        public void afterTestMethod()
        {
                ReflectionTestUtils.setField(contentService, "store", contentStore);
        }

        @Test
        public void testDefaultGetSupportedStorageClasses()
        {
                ReflectionTestUtils.setField(contentService, "store", contentStore);
                assertTrue("Current supported storage classes: " + contentService.getSupportedStorageClasses(),
                        contentService.getSupportedStorageClasses().contains("default"));
        }

        @Test
        public void testGetSupportedStorageClasses()
        {
                assertTrue("Currently supported storage classes:" + contentService.getSupportedStorageClasses()  , contentService.getSupportedStorageClasses().contains(DEFAULT_SC));
        }

        @Test
        public void getDefaultStorageClassesTransition()
        {
                ReflectionTestUtils.setField(contentService, "store", contentStore);
                assertTrue("Expected DEFAULT_SC ", contentService.getStorageClassesTransitions().isEmpty());
        }

        @Test
        public void getStorageClassesTransitions()
        {
                Set<String> key1 = Set.of("Default");
                Set<String> key2 = Set.of("Warm");
                Set<Set<String>> value1 = Set.of(Set.of("Archive"));
                Map<Set<String>, Set<Set<String>>> map = new HashMap<>();
                map.put(key1, value1);
                map.put(key2, value1);

                when(mockContentStore.getStorageClassesTransitions()).thenReturn(map);
                ReflectionTestUtils.setField(contentService, "store", mockContentStore);

                assertTrue("Obtained" + contentService.getStorageClassesTransitions(), contentService.getStorageClassesTransitions().containsKey(key1));
                assertTrue("Obtained" + contentService.getStorageClassesTransitions(), contentService.getStorageClassesTransitions().containsValue(value1));
        }

        @Test
        public void findDefaultStorageClasses() throws SystemException, NotSupportedException
        {
                ReflectionTestUtils.setField(contentService, "store", contentStore);

                NodeRef contentNodeRef = createNode("testNode1" + GUID.generate(), "testContent1");

                assertTrue("Found default storage classes: " + contentService.findStorageClasses(contentNodeRef), contentService.findStorageClasses(contentNodeRef).isEmpty());
        }

        @Test
        public void findStorageClasses() throws NotSupportedException, SystemException
        {
                NodeRef contentNodeRef = createNode("testNode" + GUID.generate(), "testContent");
                String contentUrl = contentService.getReader(contentNodeRef, ContentModel.TYPE_CONTENT).getContentUrl();

                when(mockContentStore.findStorageClasses(contentUrl)).thenReturn(Set.of("Azure"));
                ReflectionTestUtils.setField(contentService, "store", mockContentStore);

                assertTrue("Found storage classes: " + contentService.findStorageClasses(contentNodeRef),
                        contentService.findStorageClasses(contentNodeRef).contains("Azure"));
        }

        @Test
        public void findDefaultStorageClassesTransitions() throws SystemException, NotSupportedException
        {
                ReflectionTestUtils.setField(contentService, "store", contentStore);
                NodeRef contentNodeRef = createNode("testNode1" + GUID.generate(), "testContent2");

                ReflectionTestUtils.setField(contentService, "store", contentStore);
                assertTrue("Found default storage transition: ", contentService.findStorageClassesTransitions(contentNodeRef).isEmpty());
        }

        @Test
        public void findStorageClassesTransitions() throws NotSupportedException, SystemException
        {
                Set<String> key1 = Set.of("Default");
                Set<String> key2 = Set.of("Warm");
                Set<Set<String>> value1 = Set.of(Set.of("Archive"));
                Map<Set<String>, Set<Set<String>>> map = new HashMap<>();
                map.put(key1, value1);
                map.put(key2, value1);

                NodeRef contentNodeRef = createNode("testNode" + GUID.generate(), "testContent");
                String contentUrl = contentService.getReader(contentNodeRef, ContentModel.TYPE_CONTENT).getContentUrl();

                when(mockContentStore.findStorageClassesTransitions(contentUrl)).thenReturn(map);
                ReflectionTestUtils.setField(contentService, "store", mockContentStore);

                assertTrue("Obtained" + contentService.findStorageClassesTransitions(contentNodeRef),
                        contentService.findStorageClassesTransitions(contentNodeRef).containsKey(key1));
                assertTrue("Obtained" + contentService.findStorageClassesTransitions(contentNodeRef),
                        contentService.findStorageClassesTransitions(contentNodeRef).containsValue(value1));
        }

        @Test
        public void checkUpdateStorageClasses() throws NotSupportedException, SystemException
        {

                final Set<String> storageClasses = Set.of("Azure");
                final Set<String> storageClasses2 = Set.of("S3");
                final Set<String> storageClasses3 = Set.of("test1", "test2");

                NodeRef contentNodeRef = createNode("testNode" + GUID.generate(), "testContent");
                String contentUrl = contentService.getReader(contentNodeRef, ContentModel.TYPE_CONTENT).getContentUrl();

                when(mockContentStore.isStorageClassesSupported(storageClasses)).thenReturn(true);
                ReflectionTestUtils.setField(contentService, "store", mockContentStore);

                mockContentStore.updateStorageClasses(contentUrl, storageClasses, null);
                assertTrue("Storage classes not supported: ", contentService.isStorageClassesSupported(storageClasses));
                assertFalse("Storage classes not supported: ", contentService.isStorageClassesSupported(storageClasses2));

                when(mockContentStore.isStorageClassesSupported(storageClasses3)).thenReturn(true);
                ReflectionTestUtils.setField(contentService, "store", mockContentStore);
                assertTrue("Storage classes not supported: ", contentService.isStorageClassesSupported(storageClasses3));
        }

        @Test
        public void checkUpdateStorageClassesNotSupported() throws NotSupportedException, SystemException
        {

                final Set<String> storageClasses = Set.of("test");

                NodeRef contentNodeRef = createNode("testNode" + GUID.generate(), "testContent");

                when(mockContentStore.isStorageClassesSupported(storageClasses)).thenReturn(false);
                ReflectionTestUtils.setField(contentService, "store", mockContentStore);

                try
                {
                        contentService.updateStorageClasses(contentNodeRef, storageClasses, null);
                        fail("The supplied storage classes are not supported");
                }
                catch (UnsupportedStorageClassException expectedException)
                {
                }
        }

        @Test
        public void checkUpdateStorageEmptyContent() throws NotSupportedException, SystemException
        {

                final Set<String> storageClasses = Set.of("Azure");

                NodeRef contentNodeRef = createNode("testNode" + GUID.generate(), "");
                String contentUrl = contentService.getReader(contentNodeRef, ContentModel.TYPE_CONTENT).getContentUrl();

                when(mockContentStore.isStorageClassesSupported(storageClasses)).thenReturn(true);
                ReflectionTestUtils.setField(contentService, "store", mockContentStore);

                mockContentStore.updateStorageClasses(contentUrl, storageClasses, null);
                assertTrue("Storage classes not supported: ", contentService.isStorageClassesSupported(storageClasses));

        }

        @Test
        public void checkUpdateStorageClassesWithoutContent() throws NotSupportedException, SystemException
        {

                final Set<String> storageClasses = Set.of("Azure");

                NodeRef contentNodeRef = createNode("testNode" + GUID.generate(), null);
                when(mockContentStore.isStorageClassesSupported(storageClasses)).thenReturn(true);
                ReflectionTestUtils.setField(contentService, "store", mockContentStore);

                try
                {
                        contentService.updateStorageClasses(contentNodeRef, storageClasses, null);
                        fail("The supplied nodeRef" + contentNodeRef + "has no content");
                }
                catch (IllegalArgumentException expectedException)
                {
                }
        }

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
                                if (testContent != null)
                                {
                                        ContentWriter contentWriter = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                                        contentWriter.putContent(testContent);
                                }
                                return contentNodeRef;
                        }
                };

                return transactionService.getRetryingTransactionHelper().doInTransaction(makeContentCallback);

        }

}
