/**
 * 
 */
package org.alfresco.repo.avm;

import java.util.List;

import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.remote.AVMSyncServiceTransport;
import org.alfresco.service.cmr.security.AuthenticationService;

/**
 * Server side implementation of the remote wrapper of AVMSyncService.
 * @author britt
 */
public class AVMSyncServiceTransportImpl implements AVMSyncServiceTransport 
{
    /**
     * Reference to the AVMSyncService instance.
     */
    private AVMSyncService fSyncService;
    
    /**
     * Reference to the AuthenticationService instance.
     */
    private AuthenticationService fAuthenticationService;
    
    /**
     * Default constructor.
     */
    public AVMSyncServiceTransportImpl()
    {
    }

    public void setAvmSyncService(AVMSyncService service)
    {
        fSyncService = service;
    }
    
    public void setAuthenticationService(AuthenticationService service)
    {
        fAuthenticationService = service;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avmsync.AVMSyncServiceTransport#compare(java.lang.String, int, java.lang.String, int, java.lang.String)
     */
    public List<AVMDifference> compare(String ticket, int srcVersion,
            String srcPath, int dstVersion, String dstPath) 
    {
        fAuthenticationService.validate(ticket);
        return fSyncService.compare(srcVersion, srcPath, dstVersion, dstPath);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avmsync.AVMSyncServiceTransport#flatten(java.lang.String, java.lang.String, java.lang.String)
     */
    public void flatten(String ticket, String layerPath, String underlyingPath) 
    {
        fAuthenticationService.validate(ticket);
        fSyncService.flatten(layerPath, underlyingPath);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avmsync.AVMSyncServiceTransport#resetLayer(java.lang.String, java.lang.String)
     */
    public void resetLayer(String ticket, String layerPath)
    {
        fAuthenticationService.validate(ticket);
        fSyncService.resetLayer(layerPath);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.avmsync.AVMSyncServiceTransport#update(java.lang.String, java.util.List, boolean, boolean, boolean, boolean, java.lang.String, java.lang.String)
     */
    public void update(String ticket, List<AVMDifference> diffList,
            boolean ignoreConflicts, boolean ignoreOlder,
            boolean overrideConflicts, boolean overrideOlder, String tag,
            String description) 
    {
        fAuthenticationService.validate(ticket);
        fSyncService.update(diffList, ignoreConflicts, ignoreOlder, overrideConflicts, overrideOlder, tag, description);
    }
}
