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
package org.alfresco.filesys.server;

import java.io.*;

/**
 * Packet Handler Interface
 * 
 * <p>Implemented by classes that read/write request packets to a network connection.
 * 
 * @author GKSpencer
 */
public interface PacketHandlerInterface {

  /**
   * Return the protocol name
   *
   * @return String
   */
  public String getProtocolName();
  
  /**
   * Return the number of bytes available for reading without blocking
   * 
   * @return int
   * @exception IOException
   */
  public int availableBytes()
  	throws IOException;
  
  /**
   * Read a packet of data
   * 
   * @param pkt byte[]
   * @param offset int
   * @param maxLen int
   * @return int
   * @exception IOException
   */
  public int readPacket(byte[] pkt, int offset, int maxLen)
  	throws IOException;
  
  /**
   * Write a packet of data
   * 
   * @param pkt byte[]
   * @param offset int
   * @param len int
   * @exception IOException
   */
  public void writePacket(byte[] pkt, int offset, int len)
  	throws IOException;
  
  /**
   * Close the packet handler
   */
  public void closePacketHandler();
}
