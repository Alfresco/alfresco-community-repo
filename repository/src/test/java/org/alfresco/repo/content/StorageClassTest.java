package org.alfresco.repo.content;

import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.util.BaseSpringTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration({"classpath:alfresco/application-context.xml"})
public class StorageClassTest extends BaseSpringTest
{
        private static final String DEFAULT_SC = "Default1";
        @Spy
        ContentStore mockContentStore;
        @Spy
        ContentService contentService;
        @Spy
        ContentStore contentStore;


        @Before
        public void before() throws Exception
        {

                this.contentService = (ContentService)this.applicationContext.getBean("contentService");
                this.contentStore = (ContentStore) ReflectionTestUtils.getField(contentService, "store");
                mockContentStore = contentStore;
        }

        @Test
        public void testGetSupportedStorageClasses(){
                when(mockContentStore.getSupportedStorageClasses()).thenReturn(Set.of(DEFAULT_SC));
                ReflectionTestUtils.setField(contentService, "store",mockContentStore);


                assertEquals("Expected DEFAULT_SC ", contentService.getSupportedStorageClasses().contains("Default1"));
        }

//        @Test
//        public void testUploadFileVersionCreateWithOverwrite() throws Exception
//        {
//
//                MockContentStore contentStoreMock = mock(MockContentStore.class);
//                when(contentStoreMock.getSupportedStorageClasses()).thenReturn(Set.of(DEFAULT_SC));
//
//
//                String myFolderNodeId = getMyNodeId();
//
//                // create folder
//                String f1Id = createFolder(myFolderNodeId, "f1").getId();
//
//                contentService.getSupportedStorageClasses();
//
//
//
//                // upload text file - versioning is currently auto enabled on upload (create file via multi-part/form-data)
//
//                String textContentSuffix = "The quick brown fox jumps over the lazy dog ";
//                String contentName = "content-1-" + System.currentTimeMillis();
//                String content = textContentSuffix;
//
//                // create first version (ie. 1.0)
//                Document documentResp = createTextFile(f1Id, contentName, content, "UTF-8", null);
//                String docId = documentResp.getId();
//
}
