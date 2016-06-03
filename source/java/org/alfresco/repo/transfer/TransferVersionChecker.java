package org.alfresco.repo.transfer;

import org.alfresco.service.cmr.transfer.TransferVersion;

/**
 * 
 * @author mrogers
 *
 */
public interface TransferVersionChecker
{
    /**
     * Checks whether transfer is compatible between the two versions
     * @param from the version from
     * @param to the version to
     * @return boolean true the versions are compatible
     */
    boolean checkTransferVersions(TransferVersion from, TransferVersion to);

}
