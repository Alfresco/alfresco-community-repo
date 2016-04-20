package org.alfresco.repo.content.filestore;

import java.io.File;

import org.alfresco.repo.content.AbstractReadOnlyContentStoreTest;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.TempFileProvider;
import org.junit.Before;
import org.junit.experimental.categories.Category;

/**
 * Tests the file-based store when in read-only mode.
 * 
 * @see org.alfresco.repo.content.filestore.FileContentStore
 * 
 * @since 2.1
 * @author Derek Hulley
 */
@Category(OwnJVMTestsCategory.class)
public class ReadOnlyFileContentStoreTest extends AbstractReadOnlyContentStoreTest
{
    private FileContentStore store;
    
    @Before
    public void before() throws Exception
    {
        // create a store that uses a subdirectory of the temp directory
        File tempDir = TempFileProvider.getTempDir();
        store = new FileContentStore(ctx,
                tempDir.getAbsolutePath() +
                File.separatorChar +
                getName());
        // disallow random access
        store.setReadOnly(true);
    }
    
    @Override
    protected ContentStore getStore()
    {
        return store;
    }
}
