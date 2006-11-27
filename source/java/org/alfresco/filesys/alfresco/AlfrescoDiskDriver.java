/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
