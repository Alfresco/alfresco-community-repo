package org.alfresco.repo.content.metadata;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentReader;

/**
 * An interface that allows separation between the metadata extractor registry and the third party subsystem owning the
 * open office connection.
 * 
 * @author dward
 */
public interface OpenOfficeMetadataWorker
{
    /**
     * @return Returns true if a connection to the Uno server could be established
     */
    public boolean isConnected();

    /**
     * @see AbstractMappingMetadataExtracter#extractRaw(ContentReader)
     */
    public Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable;
}