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
package org.alfresco.filesys.smb.server;

import java.io.IOException;

import org.alfresco.filesys.server.filesys.DiskDeviceContext;
import org.alfresco.filesys.server.filesys.DiskInterface;
import org.alfresco.filesys.server.filesys.DiskSizeInterface;
import org.alfresco.filesys.server.filesys.DiskVolumeInterface;
import org.alfresco.filesys.server.filesys.SrvDiskInfo;
import org.alfresco.filesys.server.filesys.TooManyConnectionsException;
import org.alfresco.filesys.server.filesys.VolumeInfo;
import org.alfresco.filesys.smb.PacketType;

/**
 * Protocol handler abstract base class.
 * <p>
 * The protocol handler class is the base of all SMB protocol/dialect handler classes.
 */
abstract class ProtocolHandler
{

    // Server session that this protocol handler is associated with.

    protected SMBSrvSession m_sess;

    /**
     * Create a protocol handler for the specified session.
     */
    protected ProtocolHandler()
    {
    }

    /**
     * Create a protocol handler for the specified session.
     * 
     * @param sess SMBSrvSession
     */
    protected ProtocolHandler(SMBSrvSession sess)
    {
        m_sess = sess;
    }

    /**
     * Return the protocol handler name.
     * 
     * @return java.lang.String
     */
    public abstract String getName();

    /**
     * Run the SMB protocol handler for this server session.
     * 
     * @exception java.io.IOException
     * @exception SMBSrvException
     */
    public abstract boolean runProtocol() throws IOException, SMBSrvException, TooManyConnectionsException;

    /**
     * Get the server session that this protocol handler is associated with.
     * 
     * @param sess SMBSrvSession
     */
    protected final SMBSrvSession getSession()
    {
        return m_sess;
    }

    /**
     * Set the server session that this protocol handler is associated with.
     * 
     * @param sess SMBSrvSession
     */
    protected final void setSession(SMBSrvSession sess)
    {
        m_sess = sess;
    }

    /**
     * Determine if the request is a chained (AndX) type command and there is a chained command in
     * this request.
     * 
     * @param pkt SMBSrvPacket
     * @return true if there is a chained request to be handled, else false.
     */
    protected final boolean hasChainedCommand(SMBSrvPacket pkt)
    {

        // Determine if the command code is an AndX command

        int cmd = pkt.getCommand();

        if (cmd == PacketType.SessionSetupAndX || cmd == PacketType.TreeConnectAndX || cmd == PacketType.OpenAndX
                || cmd == PacketType.WriteAndX || cmd == PacketType.ReadAndX || cmd == PacketType.LogoffAndX
                || cmd == PacketType.LockingAndX || cmd == PacketType.NTCreateAndX)
        {

            // Check if there is a chained command

            return pkt.hasAndXCommand();
        }

        // Not a chained type command

        return false;
    }

    /**
     * Get disk sizing information from the specified driver and context.
     * 
     * @param disk DiskInterface
     * @param ctx DiskDeviceContext
     * @return SrvDiskInfo
     * @exception IOException
     */
    protected final SrvDiskInfo getDiskInformation(DiskInterface disk, DiskDeviceContext ctx) throws IOException
    {

        // Get the static disk information from the context, if available

        SrvDiskInfo diskInfo = ctx.getDiskInformation();

        // If we did not get valid disk information from the device context check if the driver
        // implements the
        // disk sizing interface

        if (diskInfo == null)
            diskInfo = new SrvDiskInfo();

        // Check if the driver implements the dynamic sizing interface to get realtime disk size
        // information

        if (disk instanceof DiskSizeInterface)
        {

            // Get the dynamic disk sizing information

            DiskSizeInterface sizeInterface = (DiskSizeInterface) disk;
            sizeInterface.getDiskInformation(ctx, diskInfo);
        }

        // Return the disk information

        return diskInfo;
    }

    /**
     * Get disk volume information from the specified driver and context
     * 
     * @param disk DiskInterface
     * @param ctx DiskDeviceContext
     * @return VolumeInfo
     */
    protected final VolumeInfo getVolumeInformation(DiskInterface disk, DiskDeviceContext ctx)
    {

        // Get the static volume information from the context, if available

        VolumeInfo volInfo = ctx.getVolumeInformation();

        // If we did not get valid volume information from the device context check if the driver
        // implements the
        // disk volume interface

        if (disk instanceof DiskVolumeInterface)
        {

            // Get the dynamic disk volume information

            DiskVolumeInterface volInterface = (DiskVolumeInterface) disk;
            volInfo = volInterface.getVolumeInformation(ctx);
        }

        // If we still have not got valid volume information then create empty volume information

        if (volInfo == null)
            volInfo = new VolumeInfo("");

        // Return the volume information

        return volInfo;
    }
}