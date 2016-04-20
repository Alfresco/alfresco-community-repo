package org.alfresco.filesys.alfresco;

import org.alfresco.jlan.server.core.DeviceContext;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.filesys.DiskInterface;

/**
 * @author dward
 */
public interface ExtendedDiskInterface extends DiskInterface
{
    /**
     * Register an independently created device context object for this instance of the shared device. Useful, e.g. when
     * context singleton configuration managed by a container.
     * 
     * @param ctx
     *            the device context
     * @exception DeviceContextException
     */
    public void registerContext(DeviceContext ctx) throws DeviceContextException;
    
}
