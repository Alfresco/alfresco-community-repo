package org.alfresco.repo.content;

import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.BaseSpringTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


@Category(OwnJVMTestsCategory.class)
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
                mockContentStore = spy(MockContentStore.class);

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
        public void getStorageClassesTransition()
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
}
