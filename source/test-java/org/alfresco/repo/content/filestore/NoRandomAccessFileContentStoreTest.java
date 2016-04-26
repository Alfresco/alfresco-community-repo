package org.alfresco.repo.content.filestore;

import java.io.File;

import org.alfresco.repo.content.AbstractWritableContentStoreTest;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.TempFileProvider;
import org.junit.Before;
import org.junit.experimental.categories.Category;

/**
 * Tests the file-based store when random access is not allowed, i.e. has to be spoofed.
 * 
 * @see org.alfresco.repo.content.filestore.FileContentStore
 * 
 * @since 2.1
 * @author Derek Hulley
 */
@Category(OwnJVMTestsCategory.class)
public class NoRandomAccessFileContentStoreTest extends AbstractWritableContentStoreTest
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
        store.setAllowRandomAccess(false);
    }
    
    @Override
    protected ContentStore getStore()
    {
        return store;
    }
}
