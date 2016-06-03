
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
