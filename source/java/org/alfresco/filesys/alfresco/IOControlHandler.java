
package org.alfresco.filesys.alfresco;

import org.alfresco.filesys.repo.ContentContext;
import org.alfresco.filesys.repo.ContentDiskDriver;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.filesys.IOControlNotImplementedException;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.jlan.smb.SMBException;
import org.alfresco.jlan.util.DataBuffer;

/**
 * I/O Control Handler Interface
 * 
 * @author gkspencer
 */
public interface IOControlHandler
{
    /**
     * ProcessIOControl
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param ctrlCode int
     * @param fid int
     * @param dataBuf DataBuffer
     * @param isFSCtrl boolean
     * @param filter int
     * @param contentDriver Object
     * @param contentContext ContentContext
     * @return DataBuffer
     */
    public org.alfresco.jlan.util.DataBuffer processIOControl(SrvSession sess, TreeConnection tree, int ctrlCode, int fid, DataBuffer dataBuf,
            boolean isFSCtrl, int filter, Object contentDriver, ContentContext contentContext)
        throws IOControlNotImplementedException, SMBException;
    
}
