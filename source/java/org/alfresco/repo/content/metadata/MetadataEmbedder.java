package org.alfresco.repo.content.metadata;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;    
import org.alfresco.repo.content.ContentWorker;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.namespace.QName;

/**
 * Interface for writing metadata properties back into the content file.
 *
 * @author Ray Gauss II
 *
 */
@AlfrescoPublicApi
public interface MetadataEmbedder extends ContentWorker {

    /**
     * Determines if the extracter works against the given mimetype.
     *
     * @param mimetype      the document mimetype
     * @return Returns      <tt>true</tt> if the mimetype is supported, otherwise <tt>false</tt>.
     */
    public boolean isEmbeddingSupported(String mimetype);
    
    /**
     * Embeds the given properties into the file specified by the given content writer.
     *      * <p>
     * The embedding viability can be determined by an up front call to
     * {@link #isEmbeddingSupported(String)}.
     * <p>
     * The source mimetype <b>must</b> be available on the
     * {@link org.alfresco.service.cmr.repository.ContentAccessor#getMimetype()} method
     * of the writer.
     * 
     * @param properties the model properties to embed
     * @param reader the reader for the original source content file
     * @param writer the writer for the content after metadata has been embedded
     * @throws ContentIOException
     */
    public void embed(Map<QName, Serializable> properties, ContentReader reader, ContentWriter writer) throws ContentIOException;


}
