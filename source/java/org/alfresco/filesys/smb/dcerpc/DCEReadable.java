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
 * DCE/RPC Readable Interface
 * <p>
 * A class that implements the DCEReadable interface can load itself from a DCE buffer.
 */
public interface DCEReadable
{

    /**
     * Read the object state from the DCE/RPC buffer
     * 
     * @param buf DCEBuffer
     * @exception DCEBufferException
     */
    public void readObject(DCEBuffer buf) throws DCEBufferException;

    /**
     * Read the strings for object from the DCE/RPC buffer
     * 
     * @param buf DCEBuffer
     * @exception DCEBufferException
     */
    public void readStrings(DCEBuffer buf) throws DCEBufferException;
}
