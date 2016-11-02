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
package org.alfresco.repo.content.filestore;

import java.io.File;
import java.io.IOException;

import org.alfresco.test_category.OwnJVMTestsCategory;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link FileContentStore} that uses {@link VolumeAwareContentUrlProvider} 
 * to route content from a store to a selection of filesystem volumes
 * @author Andreea Dragoi
 */
@Category(OwnJVMTestsCategory.class)
public class VolumeAwareFileContentStoreTest extends FileContentStoreTest{
    
    private static final String VOLUMES = "volumeA,volumeB,volumeC";
    
    @Before
    public void before() throws Exception
    {
        super.before();
        
        VolumeAwareContentUrlProvider volumeAwareContentUrlProvider = new VolumeAwareContentUrlProvider(VOLUMES);
        store.setFileContentUrlProvider(volumeAwareContentUrlProvider);
    }
    
    @Test
    public void testVolumeCreation() throws IOException
    {
        int volumesNumber = VOLUMES.split(",").length;
        // create several files
        for (int i = 0; i < volumesNumber * 5 ; i++)
        {
            store.createNewFile();
        }
        File root = new File(store.getRootLocation());
        String[] folders = root.list();
        // check if root folders contains configured volumes
        for (String file : folders)
        {
            assertTrue("Unknown volume", VOLUMES.contains(file));
        }
        assertTrue("Not all configured volumes were created", folders.length == volumesNumber);
    }
}
