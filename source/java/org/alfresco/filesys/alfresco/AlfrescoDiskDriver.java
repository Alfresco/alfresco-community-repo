/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.alfresco.filesys.alfresco;

import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.filesys.IOControlNotImplementedException;
import org.alfresco.filesys.server.filesys.IOCtlInterface;
import org.alfresco.filesys.server.filesys.NetworkFile;
import org.alfresco.filesys.server.filesys.TreeConnection;
import org.alfresco.filesys.server.state.FileStateReaper;
import org.alfresco.filesys.smb.SMBException;
import org.alfresco.filesys.smb.SMBStatus;
import org.alfresco.filesys.util.DataBuffer;
import org.alfresco.service.ServiceRegistry;

/**
 * Alfresco Disk Driver Base Class
 * 
 * <p>Provides common code to the Alfresco filesystem implementations.
 *
 * @author gkspencer
 */
public class AlfrescoDiskDriver implements IOCtlInterface {

    // Service registry for desktop actions
    
    private ServiceRegistry serviceRegistry;
    
    // File state reaper
    
    private FileStateReaper m_stateReaper;
	
    /**
     * Return the service registry
     * 
     * @return ServiceRegistry
     */
    public final ServiceRegistry getServiceRegistry()
    {
    	return this.serviceRegistry;
    }

    /**
     * Return the file state reaper
     * 
     * @return FileStateReaper
     */
    public final FileStateReaper getStateReaper()
    {
    	return m_stateReaper;
    }
    
    /**
     * Set the service registry
     * 
     * @param serviceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
    	this.serviceRegistry = serviceRegistry;
    }
    
    /**
     * Set the file state reaper
     * 
     * @param stateReaper FileStateReaper
     */
    public final void setStateReaper(FileStateReaper stateReaper)
    {
    	m_stateReaper = stateReaper;
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
        
        AlfrescoContext ctx = (AlfrescoContext) tree.getContext();
        if ( ctx.hasIOHandler())
            return ctx.getIOHandler().processIOControl( sess, tree, ctrlCode, fid, dataBuf, isFSCtrl, filter);
        else
            throw new IOControlNotImplementedException();
    }
}
