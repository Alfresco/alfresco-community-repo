package org.alfresco.repo.content.cleanup;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple listener that overwrites files with zeros.
 * <p/>
 * Wire this into the {@link EagerContentStoreCleaner} as a listener and it will
 * ensure that files have their contents overwritten with zeros before deletion.
 * Note that this process does not affect the content lifecycyle in any way
 * i.e. content will still follow the same orphan path as before.
 * <p>
 * Clearly wiring this up with a {@link DeletedContentBackupCleanerListener} is
 * pointless as you will be making a copy of the before wiping it or end up
 * copying a file full of zero depending on the order of the listeners.
 * 
 * @author Derek Hulley
 * @since 4.0.1
 */
public class FileWipingContentCleanerListener implements ContentStoreCleanerListener
{
    private static Log logger = LogFactory.getLog(FileWipingContentCleanerListener.class);
    
    public FileWipingContentCleanerListener()
    {
    }

    public void beforeDelete(ContentStore sourceStore, String contentUrl) throws ContentIOException
    {
        // First check if the content is present at all
        ContentReader reader = sourceStore.getReader(contentUrl);
        if (reader != null && reader.exists())
        {
            // Call to implementation's shred
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "About to shread: \n" +
                        "   URL:    " + contentUrl + "\n" +
                        "   Source: " + sourceStore);
            }
            try
            {
                shred(reader);
            }
            catch (Throwable e)
            {
                logger.error(
                        "Content shredding failed: \n" +
                        "   URL:    " + contentUrl + "\n" +
                        "   Source: " + sourceStore + "\n" +
                        "   Reader: " + reader,
                        e);
            }
        }
        else
        {
            logger.error(
                    "Content no longer exists.  Unable to shred: \n" +
                    "   URL:    " + contentUrl + "\n" +
                    "   Source: " + sourceStore);
        }
    }
    
    /**
     * Override to perform shredding on disparate forms of readers.  This implementation will,
     * by default, identify more specific readers and make calls for those.
     * 
     * @param reader            the reader to the content needing shredding
     * @exception IOException   any IO error
     */
    protected void shred(ContentReader reader) throws IOException
    {
        if (reader instanceof FileContentReader)
        {
            FileContentReader fileReader = (FileContentReader) reader;
            File file = fileReader.getFile();
            shred(file);
        }
    }
    
    /**
     * Called by {@link #shred(ContentReader)} when the reader points to a physical file.
     * The default implementation simply overwrites the content with zeros.
     * 
     * @param file              the file to shred before deletion
     * @throws IOException      any IO error
     */
    protected void shred(File file) throws IOException
    {
        // Double check
        if (!file.exists() || !file.canWrite())
        {
            throw new ContentIOException("Unable to write to file: " + file);
        }
        long bytes = file.length();
        OutputStream fos = null;
        OutputStream bos = null;
        try
        {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            /*
             * There are many more efficient ways of writing bytes into the file.
             * However, it is likely that implementations will do a lot more than
             * just overwrite with zeros.
             */
            for (int i = 0; i < bytes; i++)
            {
                bos.write(0);
            }
        }
        finally
        {
            if (bos != null)
            {
                try {bos.close(); } catch (Throwable e) {}
            }
            if (fos != null)
            {
                fos.close();
            }
        }
    }
}
