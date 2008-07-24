/*
 * Copyright (C) 2006-2008 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.filesys.repo;

/**
 * Buffered Write Class
 * 
 * <p>Contains the details and data for a buffered write.
 * 
 * @author gkspencer
 */
public class BufferedWrite {

	// Write details
	
	private long m_offset;
	private byte[] m_data;
	
	/**
	 * Class constructor
	 * 
	 * @param buf byte[]
	 * @param offset long
	 */
	public BufferedWrite( byte[] buf, long offset) {
		m_data   = buf;
		m_offset = offset;
	}
	
	/**
	 * Return the file offset
	 * 
	 * @return long
	 */
	public final long getOffset() {
		return m_offset;
	}
	
	/**
	 * Return the write data
	 * 
	 * @return byte[]
	 */
	public final byte[] getData() {
		return m_data;
	}
	
	/**
	 * Return the data length
	 * 
	 * @return int
	 */
	public final int getDataLength() {
		return m_data != null ? m_data.length : 0;
	}
	
	/**
	 * Return the buffered write details as a string
	 * 
	 * @return String
	 */
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("[Data len=");
		str.append(m_data.length);
		str.append(",Offset=");
		str.append(m_offset);
		str.append("]");
		
		return str.toString();
	}
}
