/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
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
