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
package org.alfresco.filesys.locking;

import java.util.Vector;

/**
 * File Lock List Class
 * <p>
 * Contains a list of the current locks on a file.
 */
public class FileLockList
{

    // List of file locks

    private Vector<FileLock> m_lockList;

    /**
     * Construct an empty file lock list.
     */
    public FileLockList()
    {
        m_lockList = new Vector<FileLock>();
    }

    /**
     * Add a lock to the list
     * 
     * @param lock Lock to be added to the list.
     */
    public final void addLock(FileLock lock)
    {
        m_lockList.add(lock);
    }

    /**
     * Remove a lock from the list
     * 
     * @param lock FileLock
     * @return FileLock
     */
    public final FileLock removeLock(FileLock lock)
    {
        return removeLock(lock.getOffset(), lock.getLength(), lock.getProcessId());
    }

    /**
     * Remove a lock from the list
     * 
     * @param long offset Starting offset of the lock
     * @param long len Locked section length
     * @param int pid Owner process id
     * @return FileLock
     */
    public final FileLock removeLock(long offset, long len, int pid)
    {

        // Check if there are any locks in the list

        if (numberOfLocks() == 0)
            return null;

        // Search for the required lock

        for (int i = 0; i < numberOfLocks(); i++)
        {

            // Get the current lock details

            FileLock curLock = getLockAt(i);
            if (curLock.getOffset() == offset && curLock.getLength() == len && curLock.getProcessId() == pid)
            {

                // Remove the lock from the list

                m_lockList.removeElementAt(i);
                return curLock;
            }
        }

        // Lock not found

        return null;
    }

    /**
     * Remove all locks from the list
     */
    public final void removeAllLocks()
    {
        m_lockList.removeAllElements();
    }

    /**
     * Return the specified lock details
     * 
     * @param int Lock index
     * @return FileLock
     */
    public final FileLock getLockAt(int idx)
    {
        if (idx < m_lockList.size())
            return m_lockList.elementAt(idx);
        return null;
    }

    /**
     * Check if the new lock should be allowed by comparing with the locks in the list.
     * 
     * @param lock FileLock
     * @return boolean true if the lock can be granted, else false.
     */
    public final boolean allowsLock(FileLock lock)
    {

        // If the list is empty we can allow the lock request

        if (numberOfLocks() == 0)
            return true;

        // Search for any overlapping locks

        for (int i = 0; i < numberOfLocks(); i++)
        {

            // Get the current lock details

            FileLock curLock = getLockAt(i);
            if (curLock.hasOverlap(lock))
                return false;
        }

        // The lock does not overlap with any existing locks

        return true;
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

        // If the list is empty we can allow the read request

        if (numberOfLocks() == 0)
            return true;

        // Search for a lock that prevents the read

        for (int i = 0; i < numberOfLocks(); i++)
        {

            // Get the current lock details

            FileLock curLock = getLockAt(i);

            // Check if the process owns the lock, if not then check if there is an overlap

            if (curLock.getProcessId() != pid)
            {

                // Check if the read overlaps with the locked area

                if (curLock.hasOverlap(offset, len) == true)
                    return false;
            }
        }

        // The lock does not overlap with any existing locks

        return true;
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

        // If the list is empty we can allow the read request

        if (numberOfLocks() == 0)
            return true;

        // Search for a lock that prevents the read

        for (int i = 0; i < numberOfLocks(); i++)
        {

            // Get the current lock details

            FileLock curLock = getLockAt(i);

            // Check if the process owns the lock, if not then check if there is an overlap

            if (curLock.getProcessId() != pid)
            {

                // Check if the read overlaps with the locked area

                if (curLock.hasOverlap(offset, len) == true)
                    return false;
            }
        }

        // The lock does not overlap with any existing locks

        return true;
    }

    /**
     * Return the count of locks in the list.
     * 
     * @return int Number of locks in the list.
     */
    public final int numberOfLocks()
    {
        return m_lockList.size();
    }
}