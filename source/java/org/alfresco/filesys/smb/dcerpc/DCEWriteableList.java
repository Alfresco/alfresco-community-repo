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
 * DCE/RPC Writeable List Interface
 * <p>
 * A class that implements the DCEWriteableList interface can write a list of DCEWriteable objects
 * to a DCE/RPC buffer.
 */
public interface DCEWriteableList
{

    /**
     * Write the object state to DCE/RPC buffers.
     * 
     * @param buf DCEBuffer
     * @exception DCEBufferException
     */
    public void writeObject(DCEBuffer buf) throws DCEBufferException;
}
