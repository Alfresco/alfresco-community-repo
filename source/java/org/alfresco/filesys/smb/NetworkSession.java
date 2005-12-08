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
package org.alfresco.filesys.smb;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Network Session Interface
 * <p>
 * Base class for client network sessions.
 */
public interface NetworkSession
{

    /**
     * Return the protocol name
     * 
     * @return String
     */
    public String getProtocolName();

    /**
     * Open a connection to a remote host
     * 
     * @param toName Host name/address being called
     * @param fromName Local host name/address
     * @param toAddr Optional address of the remote host
     * @exception IOException
     * @exception UnknownHostException
     */
    public void Open(String toName, String fromName, String toAddr) throws IOException, UnknownHostException;

    /**
     * Determine if the session is connected to a remote host
     * 
     * @return boolean
     */
    public boolean isConnected();

    /**
     * Check if the network session has data available
     * 
     * @return boolean
     * @exception IOException
     */
    public boolean hasData() throws IOException;

    /**
     * Receive a data packet from the remote host.
     * 
     * @param buf Byte buffer to receive the data into.
     * @param tmo Receive timeout in milliseconds, or zero for no timeout
     * @return Length of the received data.
     * @exception java.io.IOException I/O error occurred.
     */
    public int Receive(byte[] buf, int tmo) throws IOException;

    /**
     * Send a data packet to the remote host.
     * 
     * @param data Byte array containing the data to be sent.
     * @param siz Length of the data to send.
     * @return true if the data was sent successfully, else false.
     * @exception java.io.IOException I/O error occurred.
     */
    public boolean Send(byte[] data, int siz) throws IOException;

    /**
     * Close the network session
     * 
     * @exception java.io.IOException I/O error occurred
     */
    public void Close() throws IOException;
}
