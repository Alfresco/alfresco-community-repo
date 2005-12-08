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
package org.alfresco.filesys.server.filesys;

import org.alfresco.filesys.smb.PCShare;

/**
 * SMB disk information class.
 * <p>
 * The DiskInfo class contains the details of a remote disk share.
 */
public class DiskInfo
{

    // Node/share details

    protected String m_nodename;
    protected String m_share;

    // Total number of allocation units, available allocation units

    protected long m_totalunits;
    protected long m_freeunits;

    // Blocks per allocation unit and block size in bytes

    protected long m_blockperunit;
    protected long m_blocksize;

    /**
     * Construct a blank disk information object.
     */
    public DiskInfo()
    {
    }

    /**
     * Class constructor
     * 
     * @param shr PCShare
     * @param totunits int
     * @param blkunit int
     * @param blksiz int
     * @param freeunit int
     */
    public DiskInfo(PCShare shr, int totunits, int blkunit, int blksiz, int freeunit)
    {
        if (shr != null)
        {
            m_nodename = shr.getNodeName();
            m_share = shr.getShareName();
        }

        m_totalunits = (long) totunits;
        m_freeunits = (long) freeunit;

        m_blockperunit = (long) blkunit;
        m_blocksize = (long) blksiz;
    }

    /**
     * Class constructor
     * 
     * @param shr PCShare
     * @param totunits long
     * @param blkunit int
     * @param blksiz int
     * @param freeunit long
     */
    public DiskInfo(PCShare shr, long totunits, int blkunit, int blksiz, long freeunit)
    {
        if (shr != null)
        {
            m_nodename = shr.getNodeName();
            m_share = shr.getShareName();
        }

        m_totalunits = totunits;
        m_freeunits = freeunit;

        m_blockperunit = (long) blkunit;
        m_blocksize = (long) blksiz;
    }

    /**
     * Get the block size, in bytes.
     * 
     * @return Block size in bytes.
     */
    public final int getBlockSize()
    {
        return (int) m_blocksize;
    }

    /**
     * Get the number of blocks per allocation unit.
     * 
     * @return Number of blocks per allocation unit.
     */
    public final int getBlocksPerAllocationUnit()
    {
        return (int) m_blockperunit;
    }

    /**
     * Get the disk free space in kilobytes.
     * 
     * @return Remote disk free space in kilobytes.
     */
    public final long getDiskFreeSizeKb()
    {
        return (((m_freeunits * m_blockperunit) * m_blocksize) / 1024L);
    }

    /**
     * Get the disk free space in megabytes.
     * 
     * @return Remote disk free space in megabytes.
     */
    public final long getDiskFreeSizeMb()
    {
        return getDiskFreeSizeKb() / 1024L;
    }

    /**
     * Get the disk size in kilobytes.
     * 
     * @return Remote disk size in kilobytes.
     */
    public final long getDiskSizeKb()
    {
        return (((m_totalunits * m_blockperunit) * m_blocksize) / 1024L);
    }

    /**
     * Get the disk size in megabytes.
     * 
     * @return Remote disk size in megabytes.
     */
    public final long getDiskSizeMb()
    {
        return (getDiskSizeKb() / 1024L);
    }

    /**
     * Get the number of free units on this share.
     * 
     * @return Number of free units.
     */
    public final long getFreeUnits()
    {
        return m_freeunits;
    }

    /**
     * Return the unit size in bytes
     * 
     * @return long
     */
    public final long getUnitSize()
    {
        return m_blockperunit * m_blocksize;
    }

    /**
     * Get the node name.
     * 
     * @return Node name of the remote server.
     */
    public final String getNodeName()
    {
        return m_nodename;
    }

    /**
     * Get the share name.
     * 
     * @return Remote share name.
     */
    public final String getShareName()
    {
        return m_share;
    }

    /**
     * Get the total number of allocation units.
     * 
     * @return The total number of allocation units.
     */
    public final long getTotalUnits()
    {
        return m_totalunits;
    }

    /**
     * Return the disk information as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();

        str.append("[");
        str.append(getTotalUnits());
        str.append("/");
        str.append(getFreeUnits());
        str.append(",");
        str.append(getBlockSize());
        str.append("/");
        str.append(getBlocksPerAllocationUnit());

        str.append(",");
        str.append(getDiskSizeMb());
        str.append("Mb/");
        str.append(getDiskFreeSizeMb());
        str.append("Mb");

        str.append("]");

        return str.toString();
    }
}