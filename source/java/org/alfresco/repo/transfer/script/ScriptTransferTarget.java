package org.alfresco.repo.transfer.script;

import org.alfresco.service.cmr.transfer.TransferTarget;

/**
 * Java Script wrapper for TransferTarget
 *
 * @author Mark Rogers
 */
public class ScriptTransferTarget
{
    TransferTarget internal;
    
    public ScriptTransferTarget(TransferTarget target)
    {
        internal = target;
    }
    public String getName()
    {
        return internal.getName();
    }
    public String getDescription()
    {
        return internal.getDescription();
    } 
    public String getEndpointHost()
    {
        return internal.getEndpointHost();
    }
    public String getEndpointProtocol()
    {
        return internal.getEndpointProtocol();
    }
    public int getEndpointPort()
    {
        return internal.getEndpointPort();
    }
}
