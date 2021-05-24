package org.alfresco.repo.version;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.alfresco.repo.content.ContentServiceImpl;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.service.cmr.repository.ContentService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests for the CachingContentStore class. Tests use mock backing store and cache.
 *
 * @author Lucian Tuca
 */
@RunWith(MockitoJUnitRunner.class)
public class ContentServiceImplWithMockedContentStoreTest
{
    @Mock
    private ContentStore store;

    private ContentService contentService;

    @Before
    public void setUp() throws Exception
    {
        contentService = new ContentServiceImpl();
        ReflectionTestUtils.setField(contentService, "store", store);
    }

    @Test
    public void testStoreIsCalledForIsStorageClassesSupported()
    {
        when(store.isStorageClassesSupported(emptySet())).thenReturn(true);
        assertTrue(contentService.isStorageClassesSupported(emptySet()));
        verify(store, times(1)).isStorageClassesSupported(emptySet());
    }

    @Test
    public void testStoreIsCalledForGetSupportedStorageClasses()
    {
        when(store.getSupportedStorageClasses()).thenReturn(emptySet());
        assertTrue(contentService.getSupportedStorageClasses().isEmpty());
        verify(store, times(1)).getSupportedStorageClasses();
    }

    @Test
    public void testStoreIsCalledForGetStorageClassesTransitions()
    {
        when(store.getStorageClassesTransitions()).thenReturn(emptyMap());
        assertTrue(contentService.getStorageClassesTransitions().isEmpty());
        verify(store, times(1)).getStorageClassesTransitions();
    }
}
