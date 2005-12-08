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
package org.alfresco.filesys.smb.dcerpc;

/**
 * DCE/RPC Writeable Interface
 * <p>
 * A class that implements the DCEWriteable interface can save itself to a DCE buffer.
 */
public interface DCEWriteable
{

    /**
     * Write the object state to DCE/RPC buffers.
     * <p>
     * If a list of objects is being written the strings will be written after the objects so the
     * second buffer will be specified.
     * <p>
     * If a single object is being written to the buffer the second buffer may be null or be the
     * same buffer as the main buffer.
     * 
     * @param buf DCEBuffer
     * @param strBuf DCEBuffer
     * @exception DCEBufferException
     */
    public void writeObject(DCEBuffer buf, DCEBuffer strBuf) throws DCEBufferException;
}
