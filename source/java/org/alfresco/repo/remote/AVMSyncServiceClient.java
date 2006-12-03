/**
 * 
 */
package org.alfresco.repo.remote;

import java.util.List;

import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.remote.AVMSyncServiceTransport;

/**
 * Client side wrapper around the RMI based AVMSyncServiceTransport.
 * @author britt
 */
public class AVMSyncServiceClient implements AVMSyncService 
{
    /**
     * The instance of AVMSyncServiceTransport.
     */
    private AVMSyncServiceTransport fTransport;
    
    /**
     * Default constructor.
     */
    public AVMSyncServiceClient()
    {
    }

    /**
     * Set the transport for the service.
     */
    public void setAvmSyncServiceTransport(AVMSyncServiceTransport transport)
    {
        fTransport = transport;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avmsync.AVMSyncService#compare(int, java.lang.String, int, java.lang.String)
     */
    public List<AVMDifference> compare(int srcVersion, String srcPath,
            int dstVersion, String dstPath) 
    {
        return fTransport.compare(ClientTicketHolder.GetTicket(), srcVersion, srcPath, dstVersion, dstPath);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avmsync.AVMSyncService#flatten(java.lang.String, java.lang.String)
     */
    public void flatten(String layerPath, String underlyingPath) 
    {
        fTransport.flatten(ClientTicketHolder.GetTicket(), layerPath, underlyingPath);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avmsync.AVMSyncService#resetLayer(java.lang.String)
     */
    public void resetLayer(String layerPath) 
    {
        fTransport.resetLayer(ClientTicketHolder.GetTicket(), layerPath);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avmsync.AVMSyncService#update(java.util.List, boolean, boolean, boolean, boolean, java.lang.String, java.lang.String)
     */
    public void update(List<AVMDifference> diffList, boolean ignoreConflicts,
            boolean ignoreOlder, boolean overrideConflicts,
            boolean overrideOlder, String tag, String description) 
    {
        fTransport.update(ClientTicketHolder.GetTicket(), diffList, ignoreConflicts, ignoreOlder, overrideConflicts, overrideOlder, tag, description);
    }
}
