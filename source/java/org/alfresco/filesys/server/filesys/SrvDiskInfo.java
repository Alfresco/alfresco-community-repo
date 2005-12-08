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
 * <p>
 * The class extends the client side version of the disk information class to allow values to be set
 * after construction by a disk interface implementation.
 * <p>
 * The class contains information about the total, free and used blocks on a disk device, and the
 * block size and blocks per allocation unit of the device.
 */
public class SrvDiskInfo extends DiskInfo
{

    /**
     * Create an empty disk information object.
     */
    public SrvDiskInfo()
    {
    }

    /**
     * Construct a disk information object.
     * 
     * @param totunits int
     * @param blkunit int
     * @param blksiz int
     * @param freeunit int
     */
    public SrvDiskInfo(int totunits, int blkunit, int blksiz, int freeunit)
    {
        super(null, (long) totunits, blkunit, blksiz, (long) freeunit);
    }

    /**
     * Construct a disk information object.
     * 
     * @param totunits long
     * @param blkunit long
     * @param blksiz long
     * @param freeunit long
     */
    public SrvDiskInfo(long totunits, long blkunit, long blksiz, long freeunit)
    {
        super(null, totunits, (int) blkunit, (int) blksiz, freeunit);
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
    protected SrvDiskInfo(PCShare shr, int totunits, int blkunit, int blksiz, int freeunit)
    {
        super(shr, totunits, blkunit, blksiz, freeunit);
    }

    /**
     * Set the block size, in bytes.
     * 
     * @param siz int
     */
    public final void setBlockSize(int siz)
    {
        m_blocksize = siz;
    }

    /**
     * Set the number of blocks per filesystem allocation unit.
     * 
     * @param blks int
     */
    public final void setBlocksPerAllocationUnit(int blks)
    {
        m_blockperunit = blks;
    }

    /**
     * Set the number of free units on this shared disk device.
     * 
     * @param units int
     */
    public final void setFreeUnits(int units)
    {
        m_freeunits = units;
    }

    /**
     * Set the total number of units on this shared disk device.
     * 
     * @param units int
     */
    public final void setTotalUnits(int units)
    {
        m_totalunits = units;
    }

    /**
     * Set the block size, in bytes.
     * 
     * @param siz long
     */
    public final void setBlockSize(long siz)
    {
        m_blocksize = siz;
    }

    /**
     * Set the number of blocks per filesystem allocation unit.
     * 
     * @param blks long
     */
    public final void setBlocksPerAllocationUnit(long blks)
    {
        m_blockperunit = blks;
    }

    /**
     * Set the number of free units on this shared disk device.
     * 
     * @param units long
     */
    public final void setFreeUnits(long units)
    {
        m_freeunits = units;
    }

    /**
     * Set the total number of units on this shared disk device.
     * 
     * @param units long
     */
    public final void setTotalUnits(long units)
    {
        m_totalunits = units;
    }

    /**
     * Set the node name.
     * 
     * @param name java.lang.String
     */
    protected final void setNodeName(String name)
    {
        m_nodename = name;
    }

    /**
     * Set the shared device name.
     * 
     * @param name java.lang.String
     */
    protected final void setShareName(String name)
    {
        m_share = name;
    }

    /**
     * Copy the disk information details
     * 
     * @param disk SrvDiskInfo
     */
    public final void copyFrom(SrvDiskInfo disk)
    {

        // Copy the details to this object

        setBlockSize(disk.getBlockSize());
        setBlocksPerAllocationUnit(disk.getBlocksPerAllocationUnit());

        setFreeUnits(disk.getFreeUnits());
        setTotalUnits(disk.getTotalUnits());
    }
}