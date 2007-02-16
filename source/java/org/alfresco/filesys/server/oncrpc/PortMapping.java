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
package org.alfresco.filesys.server.oncrpc;

/**
 * Port Details Class
 * 
 * <p>Contains the details of an RPC service registered with the PortMapper service.
 * 
 * @author GKSpencer
 */
public class PortMapping {

	//	Program id and version

	private int m_programId;
	private int m_versionId;

	//	Protocol type (UDP or TCP)

	private int m_protocol;

	//	Port

	private int m_port;

	/**
	 * Class constructor
	 *
	 * @param progId int
	 * @param verId int
	 * @param protocol int
	 * @param port int
	 */
	public PortMapping(int progId, int verId, int protocol, int port)
	{
		m_programId = progId;
		m_versionId = verId;
		m_protocol = protocol;
		m_port = port;
	}

	/**
	 * Return the program id
	 * 
	 * @return int
	 */
	public final int getProgramId()
	{
		return m_programId;
	}

	/**
	 * Return the version id
	 * 
	 * @return int
	 */
	public final int getVersionId()
	{
		return m_versionId;
	}

	/**
	 * Return the protocol type
	 * 
	 * @return int
	 */
	public final int getProtocol()
	{
		return m_protocol;
	}

	/**
	 * Return the port number
	 * 
	 * @return int
	 */
	public final int getPort()
	{
		return m_port;
	}

	/**
	 * Return a hash code for the port mapping
	 * 
	 * @return int
	 */
	public int hashCode()
	{

		//	Create a hash code from the program id + version + protocol

		return generateHashCode(m_programId, m_versionId, m_protocol);
	}

	/**
	 * Generate a hash code for the specified program, version and protocol
	 * 
	 * @param progId int
	 * @param verId int
	 * @param proto int
	 * @return int
	 */
	public final static int generateHashCode(int progId, int verId, int proto)
	{

		//	Create a hash code from the program id + version + protocol

		return (progId << 16) + (verId << 8) + proto;
	}

	/**
	 * Return the port details as a string
	 * 
	 * @return String
	 */
	public String toString()
	{
		StringBuffer str = new StringBuffer(64);

		str.append("[");
		str.append(getProgramId());
		str.append(":");
		str.append(getVersionId());
		str.append(getProtocol() == Rpc.TCP ? ",TCP," : ",UDP,");
		str.append(getPort());
		str.append("]");

		return str.toString();
	}
}
