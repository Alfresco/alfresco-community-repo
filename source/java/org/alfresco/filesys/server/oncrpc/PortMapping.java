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
