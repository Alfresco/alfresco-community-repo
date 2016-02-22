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
 * @since 5.1
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
