/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing" */
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
