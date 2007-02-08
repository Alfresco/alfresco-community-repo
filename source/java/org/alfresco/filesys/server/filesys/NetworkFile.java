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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.filesys.server.filesys;

import java.io.IOException;

import org.alfresco.filesys.locking.FileLock;
import org.alfresco.filesys.locking.FileLockList;

/**
 * <p>
 * The network file represents a file or directory on a filesystem. The server keeps track of the
 * open files on a per session basis.
 * <p>
 * This class may be extended as required by your own disk driver class.
 */
public abstract class NetworkFile
{

    // Granted file access types

    public static final int READONLY = 0;
    public static final int WRITEONLY = 1;
    public static final int READWRITE = 2;

    // File status flags

    public static final int IOPending = 0x0001;
    public static final int DeleteOnClose = 0x0002;

    // File identifier and parent directory identifier

    protected int m_fid;
    protected int m_dirId;

    // Unique file identifier

    protected long m_uniqueId;

    // File/directory name

    protected String m_name;

    // Stream name and id

    protected String m_streamName;
    protected int m_streamId;

    // Full name, relative to the share

    protected String m_fullName;

    // File attributes

    protected int m_attrib;

    // File size

    protected long m_fileSize;

    // File creation/modify/last access date/time

    protected long m_createDate;
    protected long m_modifyDate;
    protected long m_accessDate;

    // Granted file access type

    protected int m_grantedAccess;

    // Flag to indicate that the file has been closed

    protected boolean m_closed = true;

    // Count of write requests to the file, used to determine if the file size may have changed

    protected int m_writeCount;

    // List of locks on this file by this session. The lock object will almost certainly be
    // referenced elsewhere depending upon the LockManager implementation used. If locking support is not
    // enabled for the DiskInterface implementation the lock list will not be allocated.
    //
    // This lock list is used to release locks on the file if the session abnormally terminates or
    // closes the file without releasing all locks.

    private FileLockList m_lockList;

    // File status flags

    private int m_flags;

    /**
     * Create a network file object with the specified file identifier.
     * 
     * @param fid int
     */
    public NetworkFile(int fid)
    {
        m_fid = fid;
    }

    /**
     * Create a network file with the specified file and parent directory ids
     * 
     * @param fid int
     * @param did int
     */
    public NetworkFile(int fid, int did)
    {
        m_fid = fid;
        m_dirId = did;
    }

    /**
     * Create a network file with the specified file id, stream id and parent directory id
     * 
     * @param fid int
     * @param stid int
     * @param did int
     */
    public NetworkFile(int fid, int stid, int did)
    {
        m_fid = fid;
        m_streamId = stid;
        m_dirId = did;
    }

    /**
     * Create a network file object with the specified file/directory name.
     * 
     * @param name File name string.
     */
    public NetworkFile(String name)
    {
        m_name = name;
    }

    /**
     * Return the parent directory identifier
     * 
     * @return int
     */
    public final int getDirectoryId()
    {
        return m_dirId;
    }

    /**
     * Return the file attributes.
     * 
     * @return int
     */
    public final int getFileAttributes()
    {
        return m_attrib;
    }

    /**
     * Return the file identifier.
     * 
     * @return int
     */
    public final int getFileId()
    {
        return m_fid;
    }

    /**
     * Get the file size, in bytes.
     * 
     * @return long
     */
    public final long getFileSize()
    {
        return m_fileSize;
    }

    /**
     * Get the file size, in bytes.
     * 
     * @return int
     */
    public final int getFileSizeInt()
    {
        return (int) (m_fileSize & 0x0FFFFFFFFL);
    }

    /**
     * Return the full name, relative to the share.
     * 
     * @return java.lang.String
     */
    public final String getFullName()
    {
        return m_fullName;
    }

    /**
     * Return the full name including the stream name, relative to the share.
     * 
     * @return java.lang.String
     */
    public final String getFullNameStream()
    {
        if (isStream())
            return m_fullName + m_streamName;
        else
            return m_fullName;
    }

    /**
     * Return the granted file access mode.
     */
    public final int getGrantedAccess()
    {
        return m_grantedAccess;
    }

    /**
     * Return the file/directory name.
     * 
     * @return java.lang.String
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Return the stream id, zero indicates the main file stream
     * 
     * @return int
     */
    public final int getStreamId()
    {
        return m_streamId;
    }

    /**
     * Return the stream name, if this is a stream
     * 
     * @return String
     */
    public final String getStreamName()
    {
        return m_streamName;
    }

    /**
     * Return the unique file identifier
     * 
     * @return long
     */
    public final long getUniqueId()
    {
        return m_uniqueId;
    }

    /**
     * Determine if the file has been closed.
     * 
     * @return boolean
     */
    public final boolean isClosed()
    {
        return m_closed;
    }

    /**
     * Return the directory file attribute status.
     * 
     * @return true if the file is a directory, else false.
     */

    public final boolean isDirectory()
    {
        return (m_attrib & FileAttribute.Directory) != 0 ? true : false;
    }

    /**
     * Return the hidden file attribute status.
     * 
     * @return true if the file is hidden, else false.
     */

    public final boolean isHidden()
    {
        return (m_attrib & FileAttribute.Hidden) != 0 ? true : false;
    }

    /**
     * Return the read-only file attribute status.
     * 
     * @return true if the file is read-only, else false.
     */

    public final boolean isReadOnly()
    {
        return (m_attrib & FileAttribute.ReadOnly) != 0 ? true : false;
    }

    /**
     * Return the system file attribute status.
     * 
     * @return true if the file is a system file, else false.
     */

    public final boolean isSystem()
    {
        return (m_attrib & FileAttribute.System) != 0 ? true : false;
    }

    /**
     * Return the archived attribute status
     * 
     * @return boolean
     */
    public final boolean isArchived()
    {
        return (m_attrib & FileAttribute.Archive) != 0 ? true : false;
    }

    /**
     * Check if this is a stream file
     * 
     * @return boolean
     */
    public final boolean isStream()
    {
        return m_streamName != null ? true : false;
    }

    /**
     * Check if there are active locks on this file by this session
     * 
     * @return boolean
     */
    public final boolean hasLocks()
    {
        if (m_lockList != null && m_lockList.numberOfLocks() > 0)
            return true;
        return false;
    }

    /**
     * Check for NT attributes
     * 
     * @param attr int
     * @return boolean
     */
    public final boolean hasNTAttribute(int attr)
    {
        return (m_attrib & attr) == attr ? true : false;
    }

    /**
     * Determine if the file access date/time is valid
     * 
     * @return boolean
     */
    public final boolean hasAccessDate()
    {
        return m_accessDate != 0L ? true : false;
    }

    /**
     * Return the file access date/time
     * 
     * @return long
     */
    public final long getAccessDate()
    {
        return m_accessDate;
    }

    /**
     * Determine if the file creation date/time is valid
     * 
     * @return boolean
     */
    public final boolean hasCreationDate()
    {
        return m_createDate != 0L ? true : false;
    }

    /**
     * Return the file creation date/time
     * 
     * @return long
     */
    public final long getCreationDate()
    {
        return m_createDate;
    }

    /**
     * Check if the delete on close flag has been set for this file
     * 
     * @return boolean
     */
    public final boolean hasDeleteOnClose()
    {
        return (m_flags & DeleteOnClose) != 0 ? true : false;
    }

    /**
     * Check if the file has an I/O request pending
     * 
     * @return boolean
     */
    public final boolean hasIOPending()
    {
        return (m_flags & IOPending) != 0 ? true : false;
    }

    /**
     * Determine if the file modification date/time is valid
     * 
     * @return boolean
     */
    public boolean hasModifyDate()
    {
        return m_modifyDate != 0L ? true : false;
    }

    /**
     * Return the file modify date/time
     * 
     * @return long
     */
    public final long getModifyDate()
    {
        return m_modifyDate;
    }

    /**
     * Get the write count for the file
     * 
     * @return int
     */
    public final int getWriteCount()
    {
        return m_writeCount;
    }

    /**
     * Increment the write count
     */
    public final void incrementWriteCount()
    {
        m_writeCount++;
    }

    /**
     * Set the file attributes, as specified by the SMBFileAttribute class.
     * 
     * @param attrib int
     */
    public final void setAttributes(int attrib)
    {
        m_attrib = attrib;
    }

    /**
     * Set, or clear, the delete on close flag
     * 
     * @param del boolean
     */
    public final void setDeleteOnClose(boolean del)
    {
        setStatusFlag(DeleteOnClose, del);
    }

    /**
     * Set the parent directory identifier
     * 
     * @param dirId int
     */
    public final void setDirectoryId(int dirId)
    {
        m_dirId = dirId;
    }

    /**
     * Set the file identifier.
     * 
     * @param fid int
     */
    public final void setFileId(int fid)
    {
        m_fid = fid;
    }

    /**
     * Set the file size.
     * 
     * @param siz long
     */
    public final void setFileSize(long siz)
    {
        m_fileSize = siz;
    }

    /**
     * Set the file size.
     * 
     * @param siz int
     */
    public final void setFileSize(int siz)
    {
        m_fileSize = (long) siz;
    }

    /**
     * Set the full file name, relative to the share.
     * 
     * @param name java.lang.String
     */
    public final void setFullName(String name)
    {
        m_fullName = name;
    }

    /**
     * Set the granted file access mode.
     * 
     * @param mode int
     */
    public final void setGrantedAccess(int mode)
    {
        m_grantedAccess = mode;
    }

    /**
     * Set the file name.
     * 
     * @param name String
     */
    public final void setName(String name)
    {
        m_name = name;
    }

    /**
     * set/clear the I/O pending flag
     * 
     * @param pending boolean
     */
    public final void setIOPending(boolean pending)
    {
        setStatusFlag(IOPending, pending);
    }

    /**
     * Set the stream id
     * 
     * @param id int
     */
    public final void setStreamId(int id)
    {
        m_streamId = id;
    }

    /**
     * Set the stream name
     * 
     * @param name String
     */
    public final void setStreamName(String name)
    {
        m_streamName = name;
    }

    /**
     * Set the file closed state.
     * 
     * @param b boolean
     */
    public final synchronized void setClosed(boolean b)
    {
        m_closed = b;
    }

    /**
     * Set the file access date/time
     * 
     * @param dattim long
     */
    public final void setAccessDate(long dattim)
    {
        m_accessDate = dattim;
    }

    /**
     * Set the file creation date/time
     * 
     * @param dattim long
     */
    public final void setCreationDate(long dattim)
    {
        m_createDate = dattim;
    }

    /**
     * Set the file modification date/time
     * 
     * @param dattim long
     */
    public final void setModifyDate(long dattim)
    {
        m_modifyDate = dattim;
    }

    /**
     * Set/clear a file status flag
     * 
     * @param flag int
     * @param sts boolean
     */
    protected final synchronized void setStatusFlag(int flag, boolean sts)
    {
        boolean state = (m_flags & flag) != 0;
        if (sts == true && state == false)
            m_flags += flag;
        else if (sts == false && state == true)
            m_flags -= flag;
    }

    /**
     * Add a lock to the active lock list
     * 
     * @param lock FileLock
     */
    public final synchronized void addLock(FileLock lock)
    {

        // Check if the lock list has been allocated

        if (m_lockList == null)
            m_lockList = new FileLockList();

        // Add the lock

        m_lockList.addLock(lock);
    }

    /**
     * Remove a lock from the active lock list
     * 
     * @param lock FileLock
     */
    public final synchronized void removeLock(FileLock lock)
    {

        // Check if the lock list is allocated

        if (m_lockList == null)
            return;

        // Remove the lock

        m_lockList.removeLock(lock);
    }

    /**
     * Remove all locks from the lock list
     */
    public final synchronized void removeAllLocks()
    {

        // Check if the lock list is valid

        if (m_lockList != null)
            m_lockList.removeAllLocks();
    }

    /**
     * Return the count of active locks
     * 
     * @return int
     */
    public final int numberOfLocks()
    {

        // Check if the lock list is allocated

        if (m_lockList == null)
            return 0;
        return m_lockList.numberOfLocks();
    }

    /**
     * Get the details of an active lock from the list
     * 
     * @param idx int
     * @return FileLock
     */
    public final FileLock getLockAt(int idx)
    {

        // Check if the lock list is allocated and the index is valid

        if (m_lockList != null)
            return m_lockList.getLockAt(idx);

        // Invalid index or lock list not valid

        return null;
    }

    /**
     * Return the lock list
     * 
     * @return FileLockList
     */
    public final FileLockList getLockList()
    {
        return m_lockList;
    }

    /**
     * Set the unique file identifier
     * 
     * @param id long
     */
    protected final void setUniqueId(long id)
    {
        m_uniqueId = id;
    }

    /**
     * Set the unique id using the file and directory id
     * 
     * @param fid int
     * @param did int
     */
    protected final void setUniqueId(int fid, int did)
    {
        long ldid = (long) did;
        long lfid = (long) fid;
        m_uniqueId = (ldid << 32) + lfid;
    }

    /**
     * Set the unique id using the full path string
     * 
     * @param path String
     */
    protected final void setUniqueId(String path)
    {
        m_uniqueId = (long) path.toUpperCase().hashCode();
    }

    /**
     * Open the file
     * 
     * @param createFlag boolean
     * @exception IOException
     */
    public abstract void openFile(boolean createFlag) throws IOException;

    /**
     * Read from the file.
     * 
     * @param buf byte[]
     * @param len int
     * @param pos int
     * @param fileOff long
     * @return Length of data read.
     * @exception IOException
     */
    public abstract int readFile(byte[] buf, int len, int pos, long fileOff) throws java.io.IOException;

    /**
     * Write a block of data to the file.
     * 
     * @param buf byte[]
     * @param len int
     * @param pos int
     * @param fileOff long
     * @exception IOException
     */
    public abstract void writeFile(byte[] buf, int len, int pos, long fileOff) throws java.io.IOException;

    /**
     * Seek to the specified file position.
     * 
     * @param pos long
     * @param typ int
     * @return int
     * @exception IOException
     */
    public abstract long seekFile(long pos, int typ) throws IOException;

    /**
     * Flush any buffered output to the file
     * 
     * @throws IOException
     */
    public abstract void flushFile() throws IOException;

    /**
     * Truncate the file to the specified file size
     * 
     * @param siz long
     * @exception IOException
     */
    public abstract void truncateFile(long siz) throws IOException;

    /**
     * Close the database file
     */
    public abstract void closeFile() throws IOException;

    /**
     * Temporary method
     */
    public void close() throws IOException
    {
        closeFile();
    }
}