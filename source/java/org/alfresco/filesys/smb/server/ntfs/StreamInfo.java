/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.filesys.smb.server.ntfs;

/**
 * File Stream Information Class
 * <p>
 * Contains the details of a file stream.
 */
public class StreamInfo
{

    // Constants

    public static final String StreamSeparator = ":";

    // Set stream information flags

    public static final int SetStreamSize =     0x0001;
    public static final int SetAllocationSize = 0x0002;
    public static final int SetModifyDate =     0x0004;
    public static final int SetCreationDate =   0x0008;
    public static final int SetAccessDate =     0x0010;

    // File path and stream name

    private String m_path;
    private String m_name;

    // Parent file id and stream id

    private int m_fid;
    private int m_stid;

    // Stream size/allocation size

    private long m_size;
    private long m_allocSize;

    // Stream creation, modification and access date/times

    private long m_createDate;
    private long m_modifyDate;
    private long m_accessDate;

    // Set stream information setter flags

    private int m_setFlags;

    /**
     * Default constructor
     */
    public StreamInfo()
    {
    }

    /**
     * Constructor
     * 
     * @param path String
     */
    public StreamInfo(String path)
    {

        // Parse the path to split into path and stream components

        parsePath(path);
    }

    /**
     * Constructor
     * 
     * @param name String
     * @param fid int
     * @param stid int
     */
    public StreamInfo(String name, int fid, int stid)
    {
        m_name = name;
        m_fid = fid;
        m_stid = stid;
    }

    /**
     * Constructor
     * 
     * @param name String
     * @param fid int
     * @param stid int
     * @param size long
     * @param alloc long
     */
    public StreamInfo(String name, int fid, int stid, long size, long alloc)
    {
        m_name = name;
        m_fid = fid;
        m_stid = stid;
        m_size = size;
        m_allocSize = alloc;
    }

    /**
     * Return the file path
     * 
     * @return String
     */
    public final String getPath()
    {
        return m_path;
    }

    /**
     * Return the stream name
     * 
     * @return String
     */
    public final String getName()
    {
        return m_name;
    }

    /**
     * Return the stream file id
     * 
     * @return int
     */
    public final int getFileId()
    {
        return m_fid;
    }

    /**
     * Return the stream id
     * 
     * @return int
     */
    public final int getStreamId()
    {
        return m_stid;
    }

    /**
     * Return the streams last access date/time.
     * 
     * @return long
     */
    public long getAccessDateTime()
    {
        return m_accessDate;
    }

    /**
     * Return the stream creation date/time.
     * 
     * @return long
     */
    public long getCreationDateTime()
    {
        return m_createDate;
    }

    /**
     * Return the modification date/time
     * 
     * @return long
     */
    public final long getModifyDateTime()
    {
        return m_modifyDate;
    }

    /**
     * Return the stream size
     * 
     * @return long
     */
    public final long getSize()
    {
        return m_size;
    }

    /**
     * Return the stream allocation size
     * 
     * @return long
     */
    public final long getAllocationSize()
    {
        return m_allocSize;
    }

    /**
     * Determine if the last access date/time is available.
     * 
     * @return boolean
     */
    public boolean hasAccessDateTime()
    {
        return m_accessDate == 0L ? false : true;
    }

    /**
     * Determine if the creation date/time details are available.
     * 
     * @return boolean
     */
    public boolean hasCreationDateTime()
    {
        return m_createDate == 0L ? false : true;
    }

    /**
     * Determine if the modify date/time details are available.
     * 
     * @return boolean
     */
    public boolean hasModifyDateTime()
    {
        return m_modifyDate == 0L ? false : true;
    }

    /**
     * Determine if the specified set stream information flags is enabled
     * 
     * @param setFlag int
     * @return boolean
     */
    public final boolean hasSetFlag(int flag)
    {
        if ((m_setFlags & flag) != 0)
            return true;
        return false;
    }

    /**
     * Return the set stream information flags
     * 
     * @return int
     */
    public final int getSetStreamInformationFlags()
    {
        return m_setFlags;
    }

    /**
     * Set the path, if it contains the stream name the path will be split into file name and stream
     * name components.
     * 
     * @param path String
     */
    public final void setPath(String path)
    {
        parsePath(path);
    }

    /**
     * Set the stream name
     * 
     * @param name String
     */
    public final void setName(String name)
    {
        m_name = name;
    }

    /**
     * Set the streams last access date/time.
     * 
     * @param timesec long
     */
    public void setAccessDateTime(long timesec)
    {

        // Create the access date/time

        m_accessDate = timesec;
    }

    /**
     * Set the creation date/time for the stream.
     * 
     * @param timesec long
     */
    public void setCreationDateTime(long timesec)
    {

        // Set the creation date/time

        m_createDate = timesec;
    }

    /**
     * Set the modifucation date/time for the stream.
     * 
     * @param timesec long
     */
    public void setModifyDateTime(long timesec)
    {

        // Set the date/time

        m_modifyDate = timesec;
    }

    /**
     * Set the file id
     * 
     * @param id int
     */
    public final void setFileId(int id)
    {
        m_fid = id;
    }

    /**
     * Set the stream id
     * 
     * @param id int
     */
    public final void setStreamId(int id)
    {
        m_stid = id;
    }

    /**
     * Set the stream size
     * 
     * @param size long
     */
    public final void setSize(long size)
    {
        m_size = size;
    }

    /**
     * Set the stream allocation size
     * 
     * @param alloc long
     */
    public final void setAllocationSize(long alloc)
    {
        m_allocSize = alloc;
    }

    /**
     * Set the set stream information flags to indicated which values are to be set
     * 
     * @param setFlags int
     */
    public final void setStreamInformationFlags(int setFlags)
    {
        m_setFlags = setFlags;
    }

    /**
     * Parse a path to split into file name and stream name components
     * 
     * @param path String
     */
    protected final void parsePath(String path)
    {

        // Check if the file name contains a stream name

        int pos = path.indexOf(StreamSeparator);
        if (pos == -1)
        {
            m_path = path;
            return;
        }

        // Split the main file name and stream name

        m_path = path.substring(0, pos);
        m_name = path.substring(pos + 1);
    }

    /**
     * Return the stream information as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();

        str.append("[");
        str.append(getName());
        str.append(",");
        str.append(getFileId());
        str.append(":");
        str.append(getStreamId());
        str.append(",");
        str.append(getSize());
        str.append("/");
        str.append(getAllocationSize());
        str.append("]");

        return str.toString();
    }
}
