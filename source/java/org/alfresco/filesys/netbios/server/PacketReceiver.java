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
package org.alfresco.filesys.netbios.server;

import java.io.IOException;
import java.net.DatagramSocket;

/**
 * Interface for NetBIOS packet receivers.
 */
public interface PacketReceiver
{

    /**
     * Receive packets on the specified datagram socket.
     * 
     * @param sock java.net.DatagramSocket
     * @exception java.io.IOException The exception description.
     */
    void ReceivePacket(DatagramSocket sock) throws IOException;
}