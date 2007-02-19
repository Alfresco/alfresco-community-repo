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
package org.alfresco.filesys.server.state;

import org.alfresco.filesys.locking.FileLock;
import org.alfresco.filesys.locking.FileLockList;
import org.alfresco.filesys.locking.LockConflictException;
import org.alfresco.filesys.locking.NotLockedException;
import org.alfresco.filesys.server.filesys.FileOpenParams;
import org.alfresco.filesys.server.filesys.FileStatus;
import org.alfresco.filesys.server.pseudo.PseudoFile;
import org.alfresco.filesys.server.pseudo.PseudoFileList;
import org.alfresco.filesys.smb.SharingMode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * File State Class
 * 
 * <p>Keeps track of file state across all sessions on the server, to keep track of file sharing modes,
 * file locks and also for synchronizing access to files/folders.
 * 
 * @author gkspencer
 */
public class FileState
{
    private static final Log logger = LogFactory.getLog(FileState.class);

    // File state constants

    public final static long NoTimeout      = -1L;
    public final static long DefTimeout     = 2 * 60000L;   // 2 minutes
    public final static long RenameTimeout  = 1 * 60000L;   // 1 minute
    
    // File status

    public enum FileStateStatus { NotExist, FileExists, FolderExists, Renamed };
    
    // File name/path

    private String m_path;

    // File state timeout, -1 indicates no timeout

    private long m_tmo;

    // File status, indicates if the file/folder exists and if it is a file or folder.

    private FileStateStatus m_fileStatus = FileStateStatus.NotExist;

    // Open file count

    private int m_openCount;

    // Sharing mode

    private int m_sharedAccess = SharingMode.READWRITE;

    // File lock list, allocated once there are active locks on this file

    private FileLockList m_lockList;
    
    // Node for this file
    
    private NodeRef m_nodeRef;
    
    // Link to the new file state when a file is renamed
    
    private FileState m_newNameState;
    
    // Pseudo file list
    
    private PseudoFileList m_pseudoFiles;
    
    /**
     * Class constructor
     * 
     * @param fname String
     * @param isdir boolean
     */
    public FileState(String fname, boolean isdir)
    {

        // Normalize the file path

        setPath(fname);
        setExpiryTime(System.currentTimeMillis() + DefTimeout);
        
        // Set the file/folder status
        
        setFileStatus( isdir ? FileStateStatus.FolderExists : FileStateStatus.FileExists);
    }

    /**
     * Return the file name/path
     * 
     * @return String
     */
    public final String getPath()
    {
        return m_path;
    }

    /**
     * Return the file status
     * 
     * @return FileStateStatus
     */
    public final FileStateStatus getFileStatus()
    {
        return m_fileStatus;
    }

    /**
     * Determine if the file/folder exists
     * 
     * @return boolen
     */
    public final boolean exists()
    {
        if ( m_fileStatus == FileStateStatus.FileExists ||
                m_fileStatus == FileStateStatus.FolderExists)
            return true;
        return false;
    }

    /**
     * Return the directory state
     * 
     * @return boolean
     */
    public final boolean isDirectory()
    {
        return m_fileStatus == FileStateStatus.FolderExists ? true : false;
    }

    /**
     * Determine if the associated node has been set
     * 
     * @return boolean
     */
    public final boolean hasNodeRef()
    {
        return m_nodeRef != null ? true : false;
    }
    
    /**
     * Return the associated node
     * 
     * @return NodeRef
     */
    public final NodeRef getNodeRef()
    {
        return m_nodeRef;
    }
    
    /**
     * Return the file open count
     * 
     * @return int
     */
    public final int getOpenCount()
    {
        return m_openCount;
    }

    /**
     * Return the shared access mode
     * 
     * @return int
     */
    public final int getSharedAccess()
    {
        return m_sharedAccess;
    }

    /**
     * Check if there are active locks on this file
     * 
     * @return boolean
     */
    public final boolean hasActiveLocks()
    {
        if (m_lockList != null && m_lockList.numberOfLocks() > 0)
            return true;
        return false;
    }

    /**
     * Check if this file state does not expire
     * 
     * @return boolean
     */
    public final boolean hasNoTimeout()
    {
        return m_tmo == NoTimeout ? true : false;
    }

    /**
     * Check if the file can be opened depending on any current file opens and the sharing mode of
     * the first file open
     * 
     * @param params FileOpenParams
     * @return boolean
     */
    public final boolean allowsOpen(FileOpenParams params)
    {

        // If the file is not currently open then allow the file open

        if (getOpenCount() == 0)
            return true;

        // Check the shared access mode

        if (getSharedAccess() == SharingMode.READWRITE && params.getSharedAccess() == SharingMode.READWRITE)
            return true;
        else if ((getSharedAccess() & SharingMode.READ) != 0 && params.isReadOnlyAccess())
            return true;
        else if ((getSharedAccess() & SharingMode.WRITE) != 0 && params.isWriteOnlyAccess())
            return true;

        // Sharing violation, do not allow the file open

        return false;
    }

    /**
     * Increment the file open count
     * 
     * @return int
     */
    public final synchronized int incrementOpenCount()
    {
        return m_openCount++;
    }

    /**
     * Decrement the file open count
     * 
     * @return int
     */
    public final synchronized int decrementOpenCount()
    {

        // Debug

        if (m_openCount <= 0)
            logger.debug("@@@@@ File close name=" + getPath() + ", count=" + m_openCount + " <<ERROR>>");
        else
            m_openCount--;

        return m_openCount;
    }

    /**
     * Check if the file state has expired
     * 
     * @param curTime long
     * @return boolean
     */
    public final boolean hasExpired(long curTime)
    {
        if (m_tmo == NoTimeout)
            return false;
        if (curTime > m_tmo)
            return true;
        return false;
    }

    /**
     * Return the number of seconds left before the file state expires
     * 
     * @param curTime long
     * @return long
     */
    public final long getSecondsToExpire(long curTime)
    {
        if (m_tmo == NoTimeout)
            return -1;
        return (m_tmo - curTime) / 1000L;
    }

    /**
     * Determine if the file state has an associated rename state
     * 
     * @return boolean
     */
    public final boolean hasRenameState()
    {
        return m_newNameState != null ? true : false;
    }
    
    /**
     * Return the associated rename state
     * 
     * @return FileState
     */
    public final FileState getRenameState()
    {
        return m_newNameState;
    }

    /**
     * Determine if a folder has pseudo files associated with it
     * 
     * @return boolean
     */
    public final boolean hasPseudoFiles()
    {
        if ( m_pseudoFiles != null)
            return m_pseudoFiles.numberOfFiles() > 0;
        return false;
    }
    
    /**
     * Return the pseudo file list
     * 
     * @return PseudoFileList
     */
    public final PseudoFileList getPseudoFileList()
    {
        return m_pseudoFiles;
    }
    
    /**
     * Add a pseudo file to this folder
     * 
     * @param pfile PseudoFile
     */
    public final void addPseudoFile(PseudoFile pfile)
    {
        if ( m_pseudoFiles == null)
            m_pseudoFiles = new PseudoFileList();
        m_pseudoFiles.addFile( pfile);
    }
    
    /**
     * Set the file status
     * 
     * @param status FileStateStatus
     */
    public final void setFileStatus(FileStateStatus status)
    {
        m_fileStatus = status;
    }

    /**
     * Set the file status
     * 
     * @param fsts int
     */
    public final void setFileStatus(int fsts)
    {
        if ( fsts == FileStatus.FileExists)
            m_fileStatus = FileStateStatus.FileExists;
        else if ( fsts == FileStatus.DirectoryExists)
            m_fileStatus = FileStateStatus.FolderExists;
        else if ( fsts == FileStatus.NotExist)
            m_fileStatus = FileStateStatus.NotExist;
    }
    
    /**
     * Set the file state expiry time
     * 
     * @param expire long
     */
    public final void setExpiryTime(long expire)
    {
        m_tmo = expire;
    }

    /**
     * Set the node ref for the file/folder
     * 
     * @param nodeRef NodeRef
     */
    public final void setNodeRef(NodeRef nodeRef)
    {
        m_nodeRef = nodeRef;
    }

    /**
     * Set the associated file state when a file is renamed, this is the link to the new file state
     * 
     * @param fstate FileState
     */
    public final void setRenameState(FileState fstate)
    {
        m_newNameState = fstate;
    }
    
    /**
     * Set the shared access mode, from the first file open
     * 
     * @param mode int
     */
    public final void setSharedAccess(int mode)
    {
        if (getOpenCount() == 0)
            m_sharedAccess = mode;
    }

    /**
     * Set the file path
     * 
     * @param path String
     */
    public final void setPath(String path)
    {

        // Split the path into directories and file name, only uppercase the directories to
        // normalize the path.

        m_path = normalizePath(path);
    }

    /**
     * Return the count of active locks on this file
     * 
     * @return int
     */
    public final int numberOfLocks()
    {
        if (m_lockList != null)
            return m_lockList.numberOfLocks();
        return 0;
    }

    /**
     * Add a lock to this file
     * 
     * @param lock FileLock
     * @exception LockConflictException
     */
    public final void addLock(FileLock lock) throws LockConflictException
    {

        // Check if the lock list has been allocated

        if (m_lockList == null)
        {

            synchronized (this)
            {

                // Allocate the lock list, check if the lock list has been allocated elsewhere
                // as we may have been waiting for the lock

                if (m_lockList == null)
                    m_lockList = new FileLockList();
            }
        }

        // Add the lock to the list, check if there are any lock conflicts

        synchronized (m_lockList)
        {

            // Check if the new lock overlaps with any existing locks

            if (m_lockList.allowsLock(lock))
            {

                // Add the new lock to the list

                m_lockList.addLock(lock);
            }
            else
                throw new LockConflictException();
        }
    }

    /**
     * Remove a lock on this file
     * 
     * @param lock FileLock
     * @exception NotLockedException
     */
    public final void removeLock(FileLock lock) throws NotLockedException
    {

        // Check if the lock list has been allocated

        if (m_lockList == null)
            throw new NotLockedException();

        // Remove the lock from the active list

        synchronized (m_lockList)
        {

            // Remove the lock, check if we found the matching lock

            if (m_lockList.removeLock(lock) == null)
                throw new NotLockedException();
        }
    }

    /**
     * Check if the file is readable for the specified section of the file and process id
     * 
     * @param offset long
     * @param len long
     * @param pid int
     * @return boolean
     */
    public final boolean canReadFile(long offset, long len, int pid)
    {

        // Check if the lock list is valid

        if (m_lockList == null)
            return true;

        // Check if the file section is readable by the specified process

        boolean readOK = false;

        synchronized (m_lockList)
        {

            // Check if the file section is readable

            readOK = m_lockList.canReadFile(offset, len, pid);
        }

        // Return the read status

        return readOK;
    }

    /**
     * Check if the file is writeable for the specified section of the file and process id
     * 
     * @param offset long
     * @param len long
     * @param pid int
     * @return boolean
     */
    public final boolean canWriteFile(long offset, long len, int pid)
    {

        // Check if the lock list is valid

        if (m_lockList == null)
            return true;

        // Check if the file section is writeable by the specified process

        boolean writeOK = false;

        synchronized (m_lockList)
        {

            // Check if the file section is writeable

            writeOK = m_lockList.canWriteFile(offset, len, pid);
        }

        // Return the write status

        return writeOK;
    }

    /**
     * Normalize the path to uppercase the directory names and keep the case of the file name.
     * 
     * @param path String
     * @return String
     */
    public final static String normalizePath(String path)
    {
    	return path.toUpperCase();
    }

    /**
     * Return the file state as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();

        str.append("[");
        str.append(getPath());
        str.append(",");
        str.append(getFileStatus());
        str.append(":Opn=");
        str.append(getOpenCount());

        str.append(",Expire=");
        str.append(getSecondsToExpire(System.currentTimeMillis()));

        str.append(",Locks=");
        str.append(numberOfLocks());

        str.append(",Ref=");
        if ( hasNodeRef())
            str.append(getNodeRef().getId());
        else
            str.append("Null");
        
        if ( isDirectory())
        {
            str.append(",Pseudo=");
            if ( hasPseudoFiles())
                str.append(getPseudoFileList().numberOfFiles());
            else
                str.append(0);
        }
        str.append("]");

        return str.toString();
    }
}
