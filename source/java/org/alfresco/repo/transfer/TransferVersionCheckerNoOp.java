package org.alfresco.repo.transfer;

import org.alfresco.service.cmr.transfer.TransferVersion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This implementation of TransferVersionChecker simply allows transfer between all versions.
 */
public class TransferVersionCheckerNoOp implements TransferVersionChecker
{
    private static Log logger = LogFactory.getLog(TransferVersionCheckerNoOp.class);
    
    public boolean checkTransferVersions(TransferVersion from, TransferVersion to)
    {
        logger.debug("checkTransferVersions from:" + from + ", to:" + to);        
        return true;
    }

}
