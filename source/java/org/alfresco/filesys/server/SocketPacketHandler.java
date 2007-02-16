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
import java.net.*;

/**
 * Java Socket Based Packet Handler Class
 * 
 * @author GKSpencer
 */
public abstract class SocketPacketHandler implements PacketHandlerInterface {

	//	Socket to read/write to/from

	private Socket m_socket;

	// Input/output streams for receiving/sending data

	private DataInputStream m_in;
	private DataOutputStream m_out;

	/**
	 * Class constructor
	 * 
	 * @param socket Socket
	 * @param protocol String
	 * @exception IOException
	 */
	protected SocketPacketHandler(Socket socket) throws IOException {
		m_socket = socket;

		//	Open the input/output streams

		m_in = new DataInputStream(m_socket.getInputStream());
		m_out = new DataOutputStream(m_socket.getOutputStream());
	}

	/**
	 * Return the protocol name
	 * 
	 * @return String
	 */
	public abstract String getProtocolName();

	/**
	 * Return the number of bytes available for reading without blocking
	 * 
	 * @return int
	 * @exception IOException
	 */
	public int availableBytes() throws IOException {
		if (m_in != null)
			return m_in.available();
		return 0;
	}

	/**
	 * Read a packet of data
	 * 
	 * @param pkt byte[]
	 * @param offset int
	 * @param maxLen int
	 * @return int
	 * @exception IOException
	 */
	public int readPacket(byte[] pkt, int offset, int maxLen) throws IOException {

		//	Read a packet of data

		if (m_in != null)
			return m_in.read(pkt, offset, maxLen);
		return 0;
	}

	/**
	 * Write a packet of data
	 * 
	 * @param pkt byte[]
	 * @param offset int
	 * @param len int
	 * @exception IOException
	 */
	public void writePacket(byte[] pkt, int offset, int len) throws IOException {

		//	Output the raw packet

		if (m_out != null) {

			synchronized (m_out) {
				m_out.write(pkt, offset, len);
			}
		}
	}

	/**
	 * Close the packet handler
	 */
	public void closePacketHandler() {

		//	Close the socket

		if (m_socket != null) {
			try {
				m_socket.close();
			} catch (Exception ex) {
			}
			m_socket = null;
		}

		//	Close the input stream

		if (m_in != null) {
			try {
				m_in.close();
			} catch (Exception ex) {
			}
			m_in = null;
		}

		//	Close the output stream

		if (m_out != null) {
			try {
				m_out.close();
			} catch (Exception ex) {
			}
			m_out = null;
		}
	}

	/**
	 * Return the socket
	 * 
	 * @return Socket
	 */
	protected final Socket getSocket() {
		return m_socket;
	}
}
