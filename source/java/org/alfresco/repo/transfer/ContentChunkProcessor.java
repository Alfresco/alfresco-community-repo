package org.alfresco.repo.transfer;

import java.util.Set;

import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.transfer.TransferException;

/**
 * 
 * @author Mark Rogers
 */
public interface ContentChunkProcessor
{
    /**
     * process this chunk of content data
     * @param data Set<ContentData>
     */
    public void processChunk(Set<ContentData> data) throws TransferException;

}
