
package org.alfresco.filesys.alfresco;

import org.alfresco.filesys.repo.ContentContext;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.core.DeviceContext;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.filesys.IOControlNotImplementedException;
import org.alfresco.jlan.server.filesys.IOCtlInterface;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.jlan.smb.SMBException;
import org.alfresco.jlan.smb.SMBStatus;
import org.alfresco.jlan.util.DataBuffer;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Alfresco Disk Driver Base Class
 * 
 * <p>Provides common code to the Alfresco filesystem implementations.
 *
 * @author gkspencer
 */
public abstract class AlfrescoDiskDriver implements IOCtlInterface, ExtendedDiskInterface {

    // Logging
    
    private static final Log logger = LogFactory.getLog(AlfrescoDiskDriver.class);
    
    // Service registry for desktop actions
    
    private ServiceRegistry m_serviceRegistry;
    
    //  Transaction service
    
    protected TransactionService m_transactionService;
    
    protected IOControlHandler ioControlHandler;
    
    public void setIoControlHandler(IOControlHandler ioControlHandler)
    {
        this.ioControlHandler = ioControlHandler;
    }

    public IOControlHandler getIoControlHandler()
    {
        return ioControlHandler;
    }
        
    /**
     * Return the service registry
     * 
     * @return ServiceRegistry
     */
    public final ServiceRegistry getServiceRegistry()
    {
        return m_serviceRegistry;
    }

    /**
     * Return the transaction service
     * 
     * @return TransactionService
     */
    public final TransactionService getTransactionService()
    {
        return m_transactionService;
    }
    
    /**
     * Set the service registry
     * 
     * @param serviceRegistry ServiceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        m_serviceRegistry = serviceRegistry;
    }
    
    /**
     * @param transactionService the transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        m_transactionService = transactionService;
    }

    /**
     * Process a filesystem I/O control request
     * 
     * @param sess Server session
     * @param tree Tree connection.
     * @param ctrlCode I/O control code
     * @param fid File id
     * @param dataBuf I/O control specific input data
     * @param isFSCtrl true if this is a filesystem control, or false for a device control
     * @param filter if bit0 is set indicates that the control applies to the share root handle
     * @return DataBuffer
     * @exception IOControlNotImplementedException
     * @exception SMBException
     */
    public DataBuffer processIOControl(SrvSession sess, TreeConnection tree, int ctrlCode, int fid, DataBuffer dataBuf,
            boolean isFSCtrl, int filter)
        throws IOControlNotImplementedException, SMBException
    {
        // Validate the file id
        
        NetworkFile netFile = tree.findFile(fid);
        if ( netFile == null || netFile.isDirectory() == false)
            throw new SMBException(SMBStatus.NTErr, SMBStatus.NTInvalidParameter);
        
        // Check if the I/O control handler is enabled
        
        if(tree.getContext() instanceof ContentContext)
        {
            ContentContext ctx = (ContentContext) tree.getContext();
        
            if(ioControlHandler != null)
            {
                return ioControlHandler.processIOControl(sess, tree, ctrlCode, fid, dataBuf, isFSCtrl, filter, this, ctx);
            }
            else
            {
                throw new IOControlNotImplementedException();
            }
        }
        return null;
    }
    
    /**
     * Registers a device context object for this instance
     * of the shared device. The same DeviceInterface implementation may be used for multiple
     * shares. In this base class, we initialize all desktop actions.
     * 
     * @param ctx the context
     * @exception DeviceContextException
     */
    public void registerContext(DeviceContext ctx) throws DeviceContextException
    {
        if (ctx instanceof AlfrescoContext)
        {
            // Enable a standalone state cache on the filesystem
            
            AlfrescoContext alfCtx = (AlfrescoContext) ctx;
            
            // Initialize the filesystem
            
            alfCtx.initialize(this);
        }
    }
}
