package org.alfresco.repo.content.caching;

import java.io.File;

import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;

/**
 * Test content writer that changes its content url after content was written (like centera content writer)
 * 
 * @author pavel.yurkevich
 */
public class TestCenteraLikeContentWriter extends FileContentWriter implements ContentStreamListener
{
    public static final String UNKNOWN_ID = FileContentStore.STORE_PROTOCOL + ContentStore.PROTOCOL_DELIMITER + "UNKNOWN_ID";
    
    private String originalContentUrl;
    
    public TestCenteraLikeContentWriter(File file, String url, ContentReader existingContentReader)
    {
        super(file, UNKNOWN_ID, existingContentReader);
        
        this.originalContentUrl = url;
        
        this.addListener(this);
    }

    @Override
    public void contentStreamClosed() throws ContentIOException
    {
        setContentUrl(originalContentUrl);
    }

}
