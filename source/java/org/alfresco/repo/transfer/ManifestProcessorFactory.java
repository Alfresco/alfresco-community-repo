
package org.alfresco.repo.transfer;

import java.util.List;

import org.alfresco.repo.transfer.manifest.TransferManifestProcessor;
import org.alfresco.repo.transfer.requisite.TransferRequsiteWriter;
import org.alfresco.service.cmr.transfer.TransferReceiver;

/**
 * @author brian
 *
 * This is a factory class for the processors of the transfer manifest file.
 */
public interface ManifestProcessorFactory
{
    /**
     * The requisite processor
     * @param receiver TransferReceiver
     * @param transferId String
     * @param out TransferRequsiteWriter
     * @return the requisite processor
     */
    TransferManifestProcessor getRequsiteProcessor(TransferReceiver receiver, String transferId, TransferRequsiteWriter out);
    
    /**
     * The commit processors
     * @param receiver TransferReceiver
     * @param transferId String
     * @return the requsite processor
     */
    List<TransferManifestProcessor> getCommitProcessors(TransferReceiver receiver, String transferId);
}
