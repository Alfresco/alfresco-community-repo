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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.filesys.smb.dcerpc.server;

import org.alfresco.filesys.netbios.RFCNetBIOSProtocol;
import org.alfresco.filesys.smb.PacketType;
import org.alfresco.filesys.smb.dcerpc.DCECommand;
import org.alfresco.filesys.smb.dcerpc.DCEDataPacker;
import org.alfresco.filesys.smb.server.SMBTransPacket;
import org.alfresco.filesys.util.DataPacker;

/**
 * DCE/RPC Server Packet Class
 */
public class DCESrvPacket extends SMBTransPacket
{

    // DCE/RPC header offsets

    private static final int VERSIONMAJOR = 0;
    private static final int VERSIONMINOR = 1;
    private static final int PDUTYPE = 2;
    private static final int HEADERFLAGS = 3;
    private static final int PACKEDDATAREP = 4;
    private static final int FRAGMENTLEN = 8;
    private static final int AUTHLEN = 10;
    private static final int CALLID = 12;
    private static final int DCEDATA = 16;

    // DCE/RPC Request offsets

    private static final int ALLOCATIONHINT = 16;
    private static final int PRESENTIDENT = 20;
    private static final int OPERATIONID = 22;
    private static final int OPERATIONDATA = 24;

    // Header flags

    public static final int FLG_FIRSTFRAG = 0x01;
    public static final int FLG_LASTFRAG = 0x02;
    public static final int FLG_ONLYFRAG = 0x03;

    // DCE/RPC header constants

    private static final byte HDR_VERSIONMAJOR = 5;
    private static final byte HDR_VERSIONMINOR = 0;
    private static final int HDR_PACKEDDATAREP = 0x00000010;

    // Offset to DCE/RPC header

    private int m_offset;

    /**
     * Construct a DCE/RPC transaction packet
     * 
     * @param buf Buffer that contains the SMB transaction packet.
     */
    public DCESrvPacket(byte[] buf)
    {
        super(buf);
        // m_offset = getParameterOffset();
    }

    /**
     * Construct a DCE/RPC transaction packet
     * 
     * @param siz Size of packet to allocate.
     */
    public DCESrvPacket(int siz)
    {
        super(siz);

        // Set the multiplex id for this transaction

        setMultiplexId(getNextMultiplexId());
    }

    /**
     * Return the major version number
     * 
     * @return int
     */
    public final int getMajorVersion()
    {
        return (int) (getBuffer()[m_offset + VERSIONMAJOR] & 0xFF);
    }

    /**
     * Return the minor version number
     * 
     * @return int
     */
    public final int getMinorVersion()
    {
        return (int) (getBuffer()[m_offset + VERSIONMINOR] & 0xFF);
    }

    /**
     * Return the PDU packet type
     * 
     * @return int
     */
    public final int getPDUType()
    {
        return (int) (getBuffer()[m_offset + PDUTYPE] & 0xFF);
    }

    /**
     * Return the header flags
     * 
     * @return int
     */
    public final int getHeaderFlags()
    {
        return (int) (getBuffer()[m_offset + HEADERFLAGS] & 0xFF);
    }

    /**
     * Return the packed data representation
     * 
     * @return int
     */
    public final int getPackedDataRepresentation()
    {
        return DataPacker.getIntelInt(getBuffer(), m_offset + PACKEDDATAREP);
    }

    /**
     * Return the fragment length
     * 
     * @return int
     */
    public final int getFragmentLength()
    {
        return DataPacker.getIntelShort(getBuffer(), m_offset + FRAGMENTLEN);
    }

    /**
     * Set the fragment length
     * 
     * @param len int
     */
    public final void setFragmentLength(int len)
    {

        // Set the DCE header fragment length

        DataPacker.putIntelShort(len, getBuffer(), m_offset + FRAGMENTLEN);
    }

    /**
     * Return the authentication length
     * 
     * @return int
     */
    public final int getAuthenticationLength()
    {
        return DataPacker.getIntelShort(getBuffer(), m_offset + AUTHLEN);
    }

    /**
     * Return the call id
     * 
     * @return int
     */
    public final int getCallId()
    {
        return DataPacker.getIntelInt(getBuffer(), m_offset + CALLID);
    }

    /**
     * Determine if this is the first fragment
     * 
     * @return boolean
     */
    public final boolean isFirstFragment()
    {
        if ((getHeaderFlags() & FLG_FIRSTFRAG) != 0)
            return true;
        return false;
    }

    /**
     * Determine if this is the last fragment
     * 
     * @return boolean
     */
    public final boolean isLastFragment()
    {
        if ((getHeaderFlags() & FLG_LASTFRAG) != 0)
            return true;
        return false;
    }

    /**
     * Determine if this is the only fragment in the request
     * 
     * @return boolean
     */
    public final boolean isOnlyFragment()
    {
        if ((getHeaderFlags() & FLG_ONLYFRAG) == FLG_ONLYFRAG)
            return true;
        return false;
    }

    /**
     * Get the offset to the DCE/RPC data within the SMB packet
     * 
     * @return int
     */
    public final int getDCEDataOffset()
    {

        // Determine the data offset from the DCE/RPC packet type

        int dataOff = -1;
        switch (getPDUType())
        {

        // Bind/bind acknowledge

        case DCECommand.BIND:
        case DCECommand.BINDACK:
            dataOff = m_offset + DCEDATA;
            break;

        // Request/response

        case DCECommand.REQUEST:
        case DCECommand.RESPONSE:
            dataOff = m_offset + OPERATIONDATA;
            break;
        }

        // Return the data offset

        return dataOff;
    }

    /**
     * Get the request allocation hint
     * 
     * @return int
     */
    public final int getAllocationHint()
    {
        return DataPacker.getIntelInt(getBuffer(), m_offset + ALLOCATIONHINT);
    }

    /**
     * Set the allocation hint
     * 
     * @param alloc int
     */
    public final void setAllocationHint(int alloc)
    {
        DataPacker.putIntelInt(alloc, getBuffer(), m_offset + ALLOCATIONHINT);
    }

    /**
     * Get the request presentation identifier
     * 
     * @return int
     */
    public final int getPresentationIdentifier()
    {
        return DataPacker.getIntelShort(getBuffer(), m_offset + PRESENTIDENT);
    }

    /**
     * Set the presentation identifier
     * 
     * @param ident int
     */
    public final void setPresentationIdentifier(int ident)
    {
        DataPacker.putIntelShort(ident, getBuffer(), m_offset + PRESENTIDENT);
    }

    /**
     * Get the request operation id
     * 
     * @return int
     */
    public final int getOperationId()
    {
        return DataPacker.getIntelShort(getBuffer(), m_offset + OPERATIONID);
    }

    /**
     * Initialize the DCE/RPC request. Set the SMB transaction parameter count so that the data
     * offset can be calculated.
     * 
     * @param handle int
     * @param typ byte
     * @param flags int
     * @param callId int
     */
    public final void initializeDCERequest(int handle, byte typ, int flags, int callId)
    {

        // Initialize the transaction

        InitializeTransact(16, null, 0, null, 0);

        // Set the parameter byte count/offset for this packet

        int bytPos = DCEDataPacker.longwordAlign(getByteOffset());

        setParameter(3, 0);
        setParameter(4, bytPos - RFCNetBIOSProtocol.HEADER_LEN);

        // Set the parameter displacement

        setParameter(5, 0);

        // Set the data byte count/offset for this packet

        setParameter(6, 0);
        setParameter(7, bytPos - RFCNetBIOSProtocol.HEADER_LEN);

        // Set the data displacement

        setParameter(8, 0);

        // Set up word count

        setParameter(9, 0);

        // Set the setup words

        setSetupParameter(0, PacketType.TransactNmPipe);
        setSetupParameter(1, handle);

        // Reset the DCE offset for a DCE reply

        m_offset = bytPos;

        // Build the DCE/RPC header

        byte[] buf = getBuffer();
        DataPacker.putZeros(buf, m_offset, 24);

        buf[m_offset + VERSIONMAJOR] = HDR_VERSIONMAJOR;
        buf[m_offset + VERSIONMINOR] = HDR_VERSIONMINOR;
        buf[m_offset + PDUTYPE] = typ;
        buf[m_offset + HEADERFLAGS] = (byte) (flags & 0xFF);
        DataPacker.putIntelInt(HDR_PACKEDDATAREP, buf, m_offset + PACKEDDATAREP);
        DataPacker.putIntelInt(0, buf, m_offset + AUTHLEN);
        DataPacker.putIntelInt(callId, buf, m_offset + CALLID);
    }

    /**
     * Initialize the DCE/RPC reply. Set the SMB transaction parameter count so that the data offset
     * can be calculated.
     */
    public final void initializeDCEReply()
    {

        // Set the total parameter words

        setParameterCount(10);

        // Set the total parameter/data bytes

        setParameter(0, 0);
        setParameter(1, 0);

        // Set the parameter byte count/offset for this packet

        int bytPos = DCEDataPacker.longwordAlign(getByteOffset());

        setParameter(3, 0);
        setParameter(4, bytPos - RFCNetBIOSProtocol.HEADER_LEN);

        // Set the parameter displacement

        setParameter(5, 0);

        // Set the data byte count/offset for this packet

        setParameter(6, 0);
        setParameter(7, bytPos - RFCNetBIOSProtocol.HEADER_LEN);

        // Set the data displacement

        setParameter(8, 0);

        // Set up word count

        setParameter(9, 0);
    }

    /**
     * Dump the DCE/RPC header details
     */
    public final void DumpHeader()
    {

        // Dump the PDU type

        System.out.println("** DCE/RPC Header - PDU Type = " + DCECommand.getCommandString(getPDUType()));
        System.out.println("  Version         : " + getMajorVersion() + "." + getMinorVersion());
        System.out.println("  Flags           : 0x" + getHeaderFlags());
        System.out.println("  Packed Data Rep : 0x" + getPackedDataRepresentation());
        System.out.println("  Fragment Length : " + getFragmentLength());
        System.out.println("  Auth Length     : " + getAuthenticationLength());
        System.out.println("  Call ID         : " + getCallId());
    }
}
