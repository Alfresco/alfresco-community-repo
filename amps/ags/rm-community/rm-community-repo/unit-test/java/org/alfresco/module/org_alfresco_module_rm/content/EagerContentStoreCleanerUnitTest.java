/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.content;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.content.cleanser.ContentCleanser;
import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Eager content store cleaner unit test.
 * 
 * @author Roy Wetherall
 * @since 2.4.a
 */
public class EagerContentStoreCleanerUnitTest extends BaseUnitTest
{
    @InjectMocks private EagerContentStoreCleaner eagerContentStoreCleaner = new EagerContentStoreCleaner()
    {
        /** dummy implementation */
        public boolean registerOrphanedContentUrl(String contentUrl, boolean force) {return true;}
    };
    
    @Mock private ContentCleanser mockedContentCleanser;
    
    /**
     * When content is registered for cleansing
     * Then the content URL is recorded for use later
     */
    @SuppressWarnings("unchecked")
    @Test
    public void registerContentURL()
    {
        String contentURL = AlfMock.generateText();
        Set<Object> mockedSet = mock(Set.class);
        when(mockedTransactionalResourceHelper.getSet(EagerContentStoreCleaner.KEY_POST_COMMIT_CLEANSING_URLS))
            .thenReturn(mockedSet);

        eagerContentStoreCleaner.registerOrphanedContentUrlForCleansing(contentURL);    
        
        verify(mockedSet).add(contentURL);
    }
    
    /**
     * Given that the content requires cleansing
     * When the content is deleted from the store
     * Then the content is cleansed first
     */
    @Test
    public void contentRequiresCleaning()
    {
        String contentURL = AlfMock.generateText();
        Set<Object> mockedSet = new HashSet<>(Arrays.asList(contentURL));
        when(mockedTransactionalResourceHelper.getSet(EagerContentStoreCleaner.KEY_POST_COMMIT_CLEANSING_URLS))
            .thenReturn(mockedSet);
        
        FileContentReader mockedReader = mock(FileContentReader.class);
        when(mockedReader.exists())
            .thenReturn(true);
        
        File mockedFile = mock(File.class);
        when(mockedReader.getFile())
            .thenReturn(mockedFile);
        
        ContentStore mockedContentStore = mock(ContentStore.class);
        when(mockedContentStore.getReader(contentURL))
            .thenReturn(mockedReader);
        
        eagerContentStoreCleaner.deleteFromStore(contentURL, mockedContentStore);
        
        verify(mockedContentCleanser).cleanse(mockedFile);        
    }
    
    /**
     * Given that the content does not require cleansing
     * When the content is deleted from the store 
     * Then the content is not cleansed
     */
    @Test
    public void contentDoesntRequireCleaning()
    {
        String contentURL = AlfMock.generateText();
        Set<Object> mockedSet = new HashSet<>(Arrays.asList(contentURL));
        when(mockedTransactionalResourceHelper.getSet(EagerContentStoreCleaner.KEY_POST_COMMIT_CLEANSING_URLS))
            .thenReturn(mockedSet);
        
        eagerContentStoreCleaner.deleteFromStore(AlfMock.generateText(), mock(ContentStore.class));
        
        verifyZeroInteractions(mockedContentCleanser);
    }
}
