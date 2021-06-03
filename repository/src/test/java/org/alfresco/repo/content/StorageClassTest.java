/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.util.BaseSpringTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class StorageClassTest extends BaseSpringTest
{
        private static final String DEFAULT_SC = "Default1";
        @Spy
        ContentStore mockContentStore;
        ContentService contentService;
        ContentStore contentStore;

        @Before
        public void before() throws Exception
        {
                MockitoAnnotations.initMocks(this);
                this.contentService = (ContentService)this.applicationContext.getBean("contentService");
                this.contentStore = (ContentStore) ReflectionTestUtils.getField(contentService, "store");
        }

        @Test
        public void testGetSupportedStorageClasses(){
                when(mockContentStore.getSupportedStorageClasses()).thenReturn(Set.of(DEFAULT_SC));
                ReflectionTestUtils.setField(contentService, "store",mockContentStore);


                assertTrue(contentService.getSupportedStorageClasses().contains("Default1"));
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
