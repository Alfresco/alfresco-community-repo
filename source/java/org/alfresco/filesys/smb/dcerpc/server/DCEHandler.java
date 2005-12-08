/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.filesys.smb.dcerpc.server;

import java.io.IOException;

import org.alfresco.filesys.smb.dcerpc.DCEBuffer;
import org.alfresco.filesys.smb.server.SMBSrvException;
import org.alfresco.filesys.smb.server.SMBSrvSession;

/**
 * DCE Request Handler Interface
 */
public interface DCEHandler
{

    /**
     * Process a DCE/RPC request
     * 
     * @param sess SMBSrvSession
     * @param inBuf DCEBuffer
     * @param pipeFile DCEPipeFile
     * @exception IOException
     * @exception SMBSrvException
     */
    public void processRequest(SMBSrvSession sess, DCEBuffer inBuf, DCEPipeFile pipeFile) throws IOException,
            SMBSrvException;
}
