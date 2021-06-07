package org.alfresco.repo.content;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
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
import org.alfresco.util.TempFileProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.transaction.UserTransaction;
import java.io.File;
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

        //        protected FileContentStore store;
        private NodeService nodeService;
        private NodeRef rootNodeRef;
        @Spy
        ContentStore mockContentStore;
        @Spy
        ContentService contentService;
        @Spy
        ContentStore contentStore;
        private Map<QName, Serializable> nodeProperties = new HashMap<>();

        @Before
        public void before() throws Exception
        {
                nodeService = (NodeService) applicationContext.getBean("NodeService");
                transactionService = (TransactionService) applicationContext.getBean("TransactionService");
                this.authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
                this.contentService = (ContentService)this.applicationContext.getBean("contentService");
                this.contentStore = (ContentStore) ReflectionTestUtils.getField(contentService, "store");
                mockContentStore = spy(MockContentStore.class);

                // authenticate
                this.authenticationComponent.setSystemUserAsCurrentUser();

                // start the transaction
                UserTransaction txn = getUserTransaction();
                txn.begin();

                // create a store and get the root node
                StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "testStoreRef");
                if (!nodeService.exists(storeRef))
                {
                        storeRef = nodeService.createStore(storeRef.getProtocol(), storeRef.getIdentifier());
                }
                rootNodeRef = nodeService.getRootNode(storeRef);

                nodeProperties.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain", 0L, "UTF-8"));

        }

        @Test
        public void testDefaultGetSupportedStorageClasses()
        {
                ReflectionTestUtils.setField(contentService, "store",mockContentStore);
                assertTrue("Obtained" + contentService.getSupportedStorageClasses(), contentService.getSupportedStorageClasses().contains("default"));
        }

        @Test
        public void testGetSupportedStorageClasses()
        {
                when(mockContentStore.getSupportedStorageClasses()).thenReturn(Set.of(DEFAULT_SC, "Azure", "S3"));
                ReflectionTestUtils.setField(contentService, "store",mockContentStore);
                assertTrue("Expected DEFAULT_SC ", contentService.getSupportedStorageClasses().contains(DEFAULT_SC));
        }

        @Test
        public void getDefaultStorageClassesTransition()
        {
                ReflectionTestUtils.setField(contentService, "store",mockContentStore);
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
        public void findDefaultStorageClasses()

        {
                ChildAssociationRef assocRef = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName(TEST_NAMESPACE, GUID.generate()),
                        ContentModel.TYPE_CONTENT,
                        nodeProperties
                );
                NodeRef contentNodeRef = assocRef.getChildRef();

                // Add the content to the node
                ReflectionTestUtils.setField(contentService, "store",mockContentStore);
                ContentWriter contentWriter = this.contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                contentWriter.putContent("testContent");
                assertTrue(" ", contentService.findStorageClasses(contentNodeRef).isEmpty());
        }


        @Test
        public void findStorageClasses()

        {
                ChildAssociationRef assocRef = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName(TEST_NAMESPACE, GUID.generate()),
                        ContentModel.TYPE_CONTENT,
                        nodeProperties
                );
                NodeRef contentNodeRef = assocRef.getChildRef();

                // Add the content to the node
                ContentWriter contentWriter = this.contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
                contentWriter.putContent("testContent");

                String contentUrl = contentService.getReader(contentNodeRef, ContentModel.TYPE_CONTENT).getContentUrl();

                when(mockContentStore.findStorageClasses(contentUrl)).thenReturn(Set.of("Azure"));
                ReflectionTestUtils.setField(contentService, "store",mockContentStore);
                assertTrue(" ", contentService.findStorageClasses(contentNodeRef).isEmpty());
        }

        @Test
        public void findDefaultStorageClassesTransitions()
        {
                ReflectionTestUtils.setField(contentService, "store", mockContentStore);
                //                assertTrue("Expected DEFAULT_SC ", contentService.findStorageClassesTransitions(contentURL).isEmpty());
        }

        private UserTransaction getUserTransaction()
        {
                return (UserTransaction) transactionService.getUserTransaction();
        }

}
