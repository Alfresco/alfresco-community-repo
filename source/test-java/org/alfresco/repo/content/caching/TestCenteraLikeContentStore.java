package org.alfresco.repo.content.caching;

import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.springframework.context.ApplicationContext;

/**
 * Test content store that behaves like Centera content store
 * 
 * @author pavel.yurkevich
 */
public class TestCenteraLikeContentStore extends FileContentStore
{

    public TestCenteraLikeContentStore(ApplicationContext context, String rootDirectoryStr)
    {
        super(context, rootDirectoryStr);
    }

    @Override
    public ContentWriter getWriterInternal(ContentReader existingContentReader, String newContentUrl)
    {
        FileContentWriter fileContentWriter = (FileContentWriter)super.getWriterInternal(existingContentReader, newContentUrl);
        
        return new TestCenteraLikeContentWriter(fileContentWriter.getFile(), fileContentWriter.getContentUrl(), existingContentReader);
    }
}
