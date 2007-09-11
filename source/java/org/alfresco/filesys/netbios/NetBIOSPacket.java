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
package org.alfresco.filesys.netbios;

import org.alfresco.filesys.util.DataPacker;
import org.alfresco.filesys.util.HexDump;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * NetBIOS Packet Class
 */
public class NetBIOSPacket
{
    private static final Log logger = LogFactory.getLog("org.alfresco.smb.protocol.netbios");

    // Minimum valid receive length

    public static final int MIN_RXLEN = 4;

    // NetBIOS opcodes

    public static final int NAME_QUERY = 0x00;
    public static final int NAME_REGISTER = 0x05;
    public static final int NAME_RELEASE = 0x06;
    public static final int WACK = 0x07;
    public static final int REFRESH = 0x08;
    public static final int NAME_REGISTER_MULTI = 0x0F;

    public static final int RESP_QUERY = 0x10;
    public static final int RESP_REGISTER = 0x15;
    public static final int RESP_RELEASE = 0x16;

    // NetBIOS opcode masks

    public static final int MASK_OPCODE = 0xF800;
    public static final int MASK_NMFLAGS = 0x07F0;
    public static final int MASK_RCODE = 0x000F;

    public static final int MASK_NOOPCODE = 0x07FF;
    public static final int MASK_NOFLAGS = 0xF80F;
    public static final int MASK_NORCODE = 0xFFF0;

    public static final int MASK_RESPONSE = 0x0010;

    // Flags bit values

    public static final int FLG_BROADCAST = 0x0001;
    public static final int FLG_RECURSION = 0x0008;
    public static final int FLG_RECURSDES = 0x0010;
    public static final int FLG_TRUNCATION = 0x0020;
    public static final int FLG_AUTHANSWER = 0x0040;

    // NetBIOS name lookup types

    public static final int NAME_TYPE_NB = 0x0020;
    public static final int NAME_TYPE_NBSTAT = 0x0021;

    // RFC NetBIOS encoded name length

    public static final int NAME_LEN = 32;

    // NetBIOS name classes

    public static final int NAME_CLASS_IN = 0x0001;

    // Bit shifts for opcode/flags values

    private static final int SHIFT_FLAGS = 4;
    private static final int SHIFT_OPCODE = 11;

    // Default NetBIOS buffer size to allocate

    public static final int DEFAULT_BUFSIZE = 1024;

    // NetBIOS packet offsets

    private static final int NB_TRANSID = 0;
    private static final int NB_OPCODE = 2;
    private static final int NB_QDCOUNT = 4;
    private static final int NB_ANCOUNT = 6;
    private static final int NB_NSCOUNT = 8;
    private static final int NB_ARCOUNT = 10;
    private static final int NB_DATA = 12;

    // NetBIOS name registration error reponse codes (RCODE field)

    public static final int FMT_ERR = 0x01;
    public static final int SRV_ERR = 0x02;
    public static final int IMP_ERR = 0x04;
    public static final int RFS_ERR = 0x05;
    public static final int ACT_ERR = 0x06;
    public static final int CFT_ERR = 0x07;

    //  Name flags
    
    public static final int NAME_PERM       = 0x0200;
    public static final int NAME_ACTIVE     = 0x0400;
    public static final int NAME_CONFLICT   = 0x0800;
    public static final int NAME_DEREG      = 0x1000;
    public static final int NAME_GROUP      = 0x8000;
    
    public static final int NAME_TYPE_BNODE = 0x0000;
    public static final int NAME_TYPE_PNODE = 0x2000;
    public static final int NAME_TYPE_MNODE = 0x4000;
    public static final int NAME_TYPE_RESVD = 0x6000;
    
    //  Adapter status name in encoded format
    
    private static final String AdapterStatusNBName = "CKAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    
    // NetBIOS packet buffer

    private byte[] m_nbbuf;

    // Actual used packet length

    private int m_datalen;

    /**
     * Default constructor
     */
    public NetBIOSPacket()
    {
        m_nbbuf = new byte[DEFAULT_BUFSIZE];
        m_datalen = NB_DATA;
    }

    /**
     * Create a NetBIOS packet with the specified buffer.
     * 
     * @param buf byte[]
     */
    public NetBIOSPacket(byte[] buf)
    {
        m_nbbuf = buf;
        m_datalen = NB_DATA;
    }

    /**
     * Create a NetBIOS packet with the specified buffer size.
     * 
     * @param siz int
     */
    public NetBIOSPacket(int siz)
    {
        m_nbbuf = new byte[siz];
        m_datalen = NB_DATA;
    }

    /**
     * Dump the packet structure to the console.
     * 
     * @param sessPkt True if this is a NetBIOS session packet, else false.
     */
    public void DumpPacket(boolean sessPkt)
    {

        // Display the transaction id

        logger.debug("NetBIOS Packet Dump :-");

        // Detrmine the packet type

        if (sessPkt == true)
        {

            switch (getPacketType())
            {

            // NetBIOS session request

            case RFCNetBIOSProtocol.SESSION_REQUEST:
                StringBuffer name = new StringBuffer();
                for (int i = 0; i < 32; i++)
                    name.append((char) m_nbbuf[39 + i]);
                logger.debug("Session request from " + NetBIOSSession.DecodeName(name.toString()));
                break;

            // NetBIOS message

            case RFCNetBIOSProtocol.SESSION_MESSAGE:
                break;
            }
        }
        else
        {

            // Display the packet type

            logger.debug(" Transaction Id : " + getTransactionId());

            String opCode = null;

            switch (getOpcode())
            {
            case NAME_QUERY:
                opCode = "QUERY";
                break;
            case RESP_QUERY:
                opCode = "QUERY (Response)";
                break;
            case NAME_REGISTER:
                opCode = "NAME REGISTER";
                break;
            case RESP_REGISTER:
                opCode = "NAME REGISTER (Response)";
                break;
            case NAME_RELEASE:
                opCode = "NAME RELEASE";
                break;
            case RESP_RELEASE:
                opCode = "NAME RELEASE (Response)";
                break;
            case WACK:
                opCode = "WACK";
                break;
            case REFRESH:
                opCode = "REFRESH";
                break;
            default:
                opCode = Integer.toHexString(getOpcode());
                break;
            }
            logger.debug(" Opcode : " + opCode);

            // Display the flags

            logger.debug(" Flags  : " + Integer.toHexString(getFlags()));

            // Display the name counts

            logger.debug(" QDCount : " + getQuestionCount());
            logger.debug(" ANCount : " + getAnswerCount());
            logger.debug(" NSCount : " + getNameServiceCount());
            logger.debug(" ARCount : " + getAdditionalCount());

            // Dump the question name, if there is one

            if (getQuestionCount() > 0)
            {

                // Get the encoded name string

                StringBuffer encName = new StringBuffer();
                for (int i = 1; i <= 32; i++)
                    encName.append((char) m_nbbuf[NB_DATA + i]);

                // Decode the name

                String name = NetBIOSSession.DecodeName(encName.toString());
                logger.debug(" QName : " + name + " <" + NetBIOSName.TypeAsString(name.charAt(15)) + ">");
            }
        }

        // Dump the raw data

        logger.debug("********** Raw NetBIOS Data Dump **********");
        HexDump.Dump(getBuffer(), getLength(), 0);
    }

    /**
     * Get the additional byte count.
     * 
     * @return int
     */
    public final int getAdditionalCount()
    {
        return DataPacker.getShort(m_nbbuf, NB_ARCOUNT);
    }

    /**
     * Get the answer name details
     * 
     * @return String
     */
    public final String getAnswerName()
    {

        // Pack the encoded name into the NetBIOS packet

        return NetBIOSSession.DecodeName(m_nbbuf, NB_DATA + 1);
    }

    /**
     * Get the answer count.
     * 
     * @return int
     */
    public final int getAnswerCount()
    {
        return DataPacker.getShort(m_nbbuf, NB_ANCOUNT);
    }

    /**
     * Get the answer name list
     * 
     * @return NetBIOSNameList
     */
    public final NetBIOSNameList getAnswerNameList()
    {

        // Check if there are any answer names

        int cnt = getAnswerCount();
        if (cnt == 0)
            return null;

        NetBIOSNameList nameList = new NetBIOSNameList();
        int pos = NB_DATA;

        while (cnt-- > 0)
        {

            // Get a NetBIOS name from the buffer

            int nameLen = NetBIOSName.decodeNetBIOSNameLength(m_nbbuf, pos);
            NetBIOSName name = NetBIOSName.decodeNetBIOSName(m_nbbuf, pos);

            // Skip the type, class and TTL

            pos += nameLen;
            pos += 8;

            // Get the count of data bytes

            int dataCnt = DataPacker.getShort(m_nbbuf, pos);
            pos += 2;

            while (dataCnt > 0)
            {

                // Get the flags, check if the name is a unique or group name

                int flags = DataPacker.getShort(m_nbbuf, pos);
                pos += 2;
                if ((flags & NAME_GROUP) != 0)
                    name.setGroup(true);

                // Get the IP address and add to the list of addresses for the current name

                byte[] ipaddr = new byte[4];
                for (int i = 0; i < 4; i++)
                    ipaddr[i] = m_nbbuf[pos++];

                name.addIPAddress(ipaddr);

                // Update the data count

                dataCnt -= 6;
            }

            // Add the name to the name list

            nameList.addName(name);
        }

        // Return the name list

        return nameList;
    }

    /**
     * Get the answer name list from an adapter status reply
     * 
     * @return NetBIOSNameList
     */
    public final NetBIOSNameList getAdapterStatusNameList()
    {

        // Check if there are any answer names

        int cnt = getAnswerCount();
        if (cnt == 0)
            return null;

        int pos = NB_DATA;

        // Skip the initial name

        int nameLen = (int) (m_nbbuf[pos++] & 0xFF);
        pos += nameLen;
        pos = DataPacker.wordAlign(pos);
        pos += 8;

        // Get the count of data bytes and name count

        int dataCnt = DataPacker.getShort(m_nbbuf, pos);
        if ( dataCnt < 16)
        	return null;
        
        pos += 2;

        int nameCnt = (int) (m_nbbuf[pos++] & 0xFF);
        NetBIOSNameList nameList = new NetBIOSNameList();

        while (nameCnt > 0 && dataCnt > 0)
        {

            // Get the NetBIOS name/type

            NetBIOSName nbName = new NetBIOSName(m_nbbuf, pos);
            pos += 16;

            // Get the name type flags, check if this is a unique or group name

            int typ = DataPacker.getShort(m_nbbuf, pos);
            pos += 2;

            if ((typ & NAME_GROUP) != 0)
                nbName.setGroup(true);

            // Add the name to the list

            nameList.addName(nbName);

            // Update the data count and name count

            dataCnt -= 18;
            nameCnt--;
        }

        // Return the name list

        return nameList;
    }

    /**
     * Return the NetBIOS buffer.
     * 
     * @return byte[]
     */
    public final byte[] getBuffer()
    {
        return m_nbbuf;
    }

    /**
     * Get the flags from the received NetBIOS packet.
     * 
     * @return int
     */
    public final int getFlags()
    {
        int flags = DataPacker.getShort(m_nbbuf, NB_QDCOUNT) & MASK_NMFLAGS;
        flags = flags >> SHIFT_FLAGS;
        return flags;
    }

    /**
     * Return the NetBIOS header flags value.
     * 
     * @return int
     */
    public final int getHeaderFlags()
    {
        return m_nbbuf[1] & 0x00FF;
    }

    /**
     * Return the NetBIOS header data length value.
     * 
     * @return int
     */
    public final int getHeaderLength()
    {
        return DataPacker.getIntelShort(m_nbbuf, 2) & 0xFFFF;
    }

    /**
     * Return the NetBIOS header message type.
     * 
     * @return int
     */
    public final int getHeaderType()
    {
        return m_nbbuf[0] & 0x00FF;
    }

    /**
     * Return the received packet length.
     * 
     * @return int
     */
    public final int getLength()
    {
        return m_datalen;
    }

    /**
     * Return the name service count.
     * 
     * @return int
     */
    public final int getNameServiceCount()
    {
        return DataPacker.getShort(m_nbbuf, NB_NSCOUNT);
    }

    /**
     * Return the NetBIOS opcode.
     * 
     * @return int
     */
    public final int getOpcode()
    {
        int op = DataPacker.getShort(m_nbbuf, NB_OPCODE) & MASK_OPCODE;
        op = op >> SHIFT_OPCODE;
        return op;
    }

    /**
     * Return the NetBIOS packet type.
     * 
     * @return int
     */
    public final int getPacketType()
    {
        return (int) (m_nbbuf[0] & 0xFF);
    }

    /**
     * Return the question count.
     * 
     * @return int
     */
    public final int getQuestionCount()
    {
        return DataPacker.getShort(m_nbbuf, NB_QDCOUNT);
    }

    /**
     * Get the question name.
     */
    public final String getQuestionName()
    {

        // Pack the encoded name into the NetBIOS packet

        return NetBIOSSession.DecodeName(m_nbbuf, NB_DATA + 1);
    }

    /**
     * Get the question name length.
     */
    public final int getQuestionNameLength()
    {

        // Pack the encoded name into the NetBIOS packet

        return (int) m_nbbuf[NB_DATA] & 0xFF;
    }

    /**
     * Return the result code for the received packet.
     * 
     * @return int
     */
    public final int getResultCode()
    {
        int res = DataPacker.getShort(m_nbbuf, NB_OPCODE) & MASK_RCODE;
        return res;
    }

    /**
     * Return the NetBIOS transaction id.
     * 
     * @return int
     */
    public final int getTransactionId()
    {
        return DataPacker.getShort(m_nbbuf, NB_TRANSID);
    }

    /**
     * Determine if the received packet is a repsonse packet.
     * 
     * @return boolean
     */
    public final boolean isResponse()
    {
        if ((getOpcode() & MASK_RESPONSE) != 0)
            return true;
        return false;
    }

    /**
     * Set the additional byte count.
     * 
     * @param cnt int
     */
    public final void setAdditionalCount(int cnt)
    {
        DataPacker.putShort((short) cnt, m_nbbuf, NB_ARCOUNT);
    }

    /**
     * Set the answer byte count.
     * 
     * @param cnt int
     */
    public final void setAnswerCount(int cnt)
    {
        DataPacker.putShort((short) cnt, m_nbbuf, NB_ANCOUNT);
    }

    /**
     * Set the answer name.
     * 
     * @param name java.lang.String
     * @param qtyp int
     * @param qcls int
     */
    public final int setAnswerName(String name, char ntyp, int qtyp, int qcls)
    {

        // RFC encode the NetBIOS name string

        String encName = NetBIOSSession.ConvertName(name, ntyp);
        byte[] nameByts = encName.getBytes();

        // Pack the encoded name into the NetBIOS packet

        int pos = NB_DATA;
        m_nbbuf[pos++] = (byte) NAME_LEN;

        for (int i = 0; i < 32; i++)
            m_nbbuf[pos++] = nameByts[i];
        m_nbbuf[pos++] = 0x00;

        // Set the name type and class

        DataPacker.putShort((short) qtyp, m_nbbuf, pos);
        pos += 2;

        DataPacker.putShort((short) qcls, m_nbbuf, pos);
        pos += 2;

        // Set the packet length

        if (pos > m_datalen)
            setLength(pos);
        return pos;
    }

    /**
     * Set the flags.
     * 
     * @param flg int
     */
    public final void setFlags(int flg)
    {
        int val = DataPacker.getShort(m_nbbuf, NB_OPCODE) & MASK_NOFLAGS;
        val += (flg << SHIFT_FLAGS);
        DataPacker.putShort((short) val, m_nbbuf, NB_OPCODE);
    }

    /**
     * Set the NetBIOS packet header flags value.
     * 
     * @param flg int
     */
    public final void setHeaderFlags(int flg)
    {
        m_nbbuf[1] = (byte) (flg & 0x00FF);
    }

    /**
     * Set the NetBIOS packet data length in the packet header.
     * 
     * @param len int
     */
    public final void setHeaderLength(int len)
    {
        DataPacker.putIntelShort(len, m_nbbuf, 2);
    }

    /**
     * Set the NetBIOS packet type in the packet header.
     * 
     * @param typ int
     */
    public final void setHeaderType(int typ)
    {
        m_nbbuf[0] = (byte) (typ & 0x00FF);
    }

    /**
     * Set the IP address.
     * 
     * @return int
     * @param off int
     * @param ipaddr byte[]
     */
    public final int setIPAddress(int off, byte[] ipaddr)
    {

        // Pack the IP address

        for (int i = 0; i < 4; i++)
            m_nbbuf[off + i] = ipaddr[i];

        // Set the packet length

        int pos = off + 4;
        if (pos > m_datalen)
            setLength(pos);

        // Return the new packet offset

        return pos;
    }

    /**
     * Set the packet data length.
     * 
     * @param len int
     */
    public final void setLength(int len)
    {
        m_datalen = len;
    }

    /**
     * Set the name registration flags.
     * 
     * @return int
     * @param off int
     * @param flg int
     */
    public final int setNameRegistrationFlags(int off, int flg)
    {

        // Set the name registration flags

        DataPacker.putShort((short) 0x0006, m_nbbuf, off);
        DataPacker.putShort((short) flg, m_nbbuf, off + 2);

        // Set the packet length

        int pos = off + 4;
        if (pos > m_datalen)
            setLength(pos);

        // Return the new packet offset

        return pos;
    }

    /**
     * Set the name service count.
     * 
     * @param cnt int
     */
    public final void setNameServiceCount(int cnt)
    {
        DataPacker.putShort((short) cnt, m_nbbuf, NB_NSCOUNT);
    }

    /**
     * Set the NetBIOS opcode.
     * 
     * @param op int
     */
    public final void setOpcode(int op)
    {
        int val = DataPacker.getShort(m_nbbuf, NB_OPCODE) & MASK_NOOPCODE;
        val = val + (op << SHIFT_OPCODE);
        DataPacker.putShort((short) val, m_nbbuf, NB_OPCODE);
    }

    /**
     * Set the question count.
     * 
     * @param cnt int
     */
    public final void setQuestionCount(int cnt)
    {
        DataPacker.putShort((short) cnt, m_nbbuf, NB_QDCOUNT);
    }

    /**
     * Set the question name.
     * 
     * @param name NetBIOSName
     * @param qtyp int
     * @param qcls int
     * @return int
     */
    public final int setQuestionName(NetBIOSName name, int qtyp, int qcls)
    {

        // Encode the NetBIOS name

        byte[] nameByts = name.encodeName();

        // Pack the encoded name into the NetBIOS packet

        int pos = NB_DATA;
        System.arraycopy(nameByts, 0, m_nbbuf, pos, nameByts.length);
        pos += nameByts.length;

        // Set the name type and class

        DataPacker.putShort((short) qtyp, m_nbbuf, pos);
        pos += 2;

        DataPacker.putShort((short) qcls, m_nbbuf, pos);
        pos += 2;

        // Set the packet length

        if (pos > m_datalen)
            setLength(pos);
        return pos;
    }

    /**
     * Set the question name.
     * 
     * @param name java.lang.String
     * @param qtyp int
     * @param qcls int
     */
    public final int setQuestionName(String name, char ntyp, int qtyp, int qcls)
    {

        // RFC encode the NetBIOS name string

        String encName = NetBIOSSession.ConvertName(name, ntyp);
        byte[] nameByts = encName.getBytes();

        // Pack the encoded name into the NetBIOS packet

        int pos = NB_DATA;
        m_nbbuf[pos++] = (byte) NAME_LEN;

        for (int i = 0; i < 32; i++)
            m_nbbuf[pos++] = nameByts[i];
        m_nbbuf[pos++] = 0x00;

        // Set the name type and class

        DataPacker.putShort((short) qtyp, m_nbbuf, pos);
        pos += 2;

        DataPacker.putShort((short) qcls, m_nbbuf, pos);
        pos += 2;

        // Set the packet length

        if (pos > m_datalen)
            setLength(pos);
        return pos;
    }

    /**
     * Pack the resource data into the packet.
     * 
     * @return int
     * @param off int
     * @param flg int
     * @param data byte[]
     * @param len int
     */
    public final int setResourceData(int off, int flg, byte[] data, int len)
    {

        // Set the resource data type

        DataPacker.putShort((short) flg, m_nbbuf, off);

        // Pack the data

        int pos = off + 2;
        for (int i = 0; i < len; i++)
            m_nbbuf[pos++] = data[i];

        // Set the packet length

        if (pos > m_datalen)
            setLength(pos);
        return pos;
    }

    /**
     * Set the resource data length in the NetBIOS packet.
     * 
     * @return int
     * @param off int
     * @param len int
     */
    public final int setResourceDataLength(int off, int len)
    {

        // Set the resource data length

        DataPacker.putShort((short) len, m_nbbuf, off);

        // Set the packet length

        int pos = off + 2;
        if (pos > m_datalen)
            setLength(pos);

        // Return the new packet offset

        return pos;
    }

    /**
     * Set the resource record.
     * 
     * @param pktoff Packet offset to pack the resource record.
     * @param offset Offset to name.
     * @param qtyp int
     * @param qcls int
     */
    public final int setResourceRecord(int pktoff, int rroff, int qtyp, int qcls)
    {

        // Pack the resource record details

        DataPacker.putShort((short) (0xC000 + rroff), m_nbbuf, pktoff);
        DataPacker.putShort((short) qtyp, m_nbbuf, pktoff + 2);
        DataPacker.putShort((short) qcls, m_nbbuf, pktoff + 4);

        // Set the packet length

        int pos = pktoff + 6;
        if (pos > m_datalen)
            setLength(pos);

        // Return the new packet offset

        return pos;
    }

    /**
     * Set the transaction id.
     * 
     * @param id int
     */
    public final void setTransactionId(int id)
    {
        DataPacker.putShort((short) id, m_nbbuf, NB_TRANSID);
    }

    /**
     * Set the time to live for the packet.
     * 
     * @return int
     * @param off int
     * @param ttl int
     */
    public final int setTTL(int off, int ttl)
    {

        // Set the time to live value for the packet

        DataPacker.putInt(ttl, m_nbbuf, off);

        // Set the packet length

        int pos = off + 4;
        if (pos > m_datalen)
            setLength(pos);

        // Return the new packet offset

        return pos;
    }

    /**
     * Return a packet type as a string
     * 
     * @param typ int
     * @return String
     */
    public final static String getTypeAsString(int typ)
    {

        // Return the NetBIOS packet type as a string

        String typStr = "";

        switch (typ)
        {
        case RFCNetBIOSProtocol.SESSION_ACK:
            typStr = "SessionAck";
            break;
        case RFCNetBIOSProtocol.SESSION_KEEPALIVE:
            typStr = "SessionKeepAlive";
            break;
        case RFCNetBIOSProtocol.SESSION_MESSAGE:
            typStr = "SessionMessage";
            break;
        case RFCNetBIOSProtocol.SESSION_REJECT:
            typStr = "SessionReject";
            break;
        case RFCNetBIOSProtocol.SESSION_REQUEST:
            typStr = "SessionRequest";
            break;
        case RFCNetBIOSProtocol.SESSION_RETARGET:
            typStr = "SessionRetarget";
            break;
        default:
            typStr = "Unknown 0x" + Integer.toHexString(typ);
            break;
        }

        // Return the packet type string

        return typStr;
    }

    /**
     * Build a name query response packet for the specified NetBIOS name
     * 
     * @param name NetBIOSName
     * @return int
     */
    public final int buildNameQueryResponse(NetBIOSName name)
    {

        // Fill in the header

        setOpcode(NetBIOSPacket.RESP_QUERY);
        setFlags(NetBIOSPacket.FLG_RECURSDES + NetBIOSPacket.FLG_AUTHANSWER);

        setQuestionCount(0);
        setAnswerCount(1);
        setAdditionalCount(0);
        setNameServiceCount(0);

        int pos = setAnswerName(name.getName(), name.getType(), 0x20, 0x01);
        pos = setTTL(pos, 10000);
        pos = setResourceDataLength(pos, name.numberOfAddresses() * 6);

        // Pack the IP address(es) for this name

        for (int i = 0; i < name.numberOfAddresses(); i++)
        {

            // Get the current IP address

            byte[] ipaddr = name.getIPAddress(i);

            // Pack the NetBIOS flags and IP address

            DataPacker.putShort((short) 0x00, m_nbbuf, pos);
            pos += 2;

            for (int j = 0; j < 4; j++)
                m_nbbuf[pos++] = ipaddr[j];
        }

        // Set the packet length, and return the length

        setLength(pos);
        return getLength();
    }

    /**
     * Build an add name request packet for the specified NetBIOS name
     * 
     * @param name NetBIOSName
     * @param addrIdx int
     * @param tranId int
     * @return int
     */
    public final int buildAddNameRequest(NetBIOSName name, int addrIdx, int tranId)
    {

        // Initialize an add name NetBIOS packet

        setTransactionId(tranId);
        setOpcode(NetBIOSPacket.NAME_REGISTER);
        setFlags(NetBIOSPacket.FLG_BROADCAST + NetBIOSPacket.FLG_RECURSION);

        setQuestionCount(1);
        setAnswerCount(0);
        setNameServiceCount(0);
        setAdditionalCount(1);

        int pos = setQuestionName(name.getName(), name.getType(), 0x20, 0x01);
        pos = setResourceRecord(pos, 12, 0x20, 0x01);

        if (name.getTimeToLive() == 0)
            pos = setTTL(pos, NetBIOSName.DefaultTTL);
        else
            pos = setTTL(pos, name.getTimeToLive());

        short flg = 0;
        if (name.isGroupName())
            flg = (short) 0x8000;
        pos = setNameRegistrationFlags(pos, flg);
        pos = setIPAddress(pos, name.getIPAddress(addrIdx));

        // Return the packet length

        setLength(pos);
        return pos;
    }

    /**
     * Build a refresh name request packet for the specified NetBIOS name
     * 
     * @param name NetBIOSName
     * @param addrIdx int
     * @param tranId int
     * @return int
     */
    public final int buildRefreshNameRequest(NetBIOSName name, int addrIdx, int tranId)
    {

        // Initialize an add name NetBIOS packet

        setTransactionId(tranId);
        setOpcode(NetBIOSPacket.REFRESH);
        setFlags(NetBIOSPacket.FLG_BROADCAST + NetBIOSPacket.FLG_RECURSION);

        setQuestionCount(1);
        setAnswerCount(0);
        setNameServiceCount(0);
        setAdditionalCount(1);

        int pos = setQuestionName(name.getName(), name.getType(), 0x20, 0x01);
        pos = setResourceRecord(pos, 12, 0x20, 0x01);

        if (name.getTimeToLive() == 0)
            pos = setTTL(pos, NetBIOSName.DefaultTTL);
        else
            pos = setTTL(pos, name.getTimeToLive());

        short flg = 0;
        if (name.isGroupName())
            flg = (short) 0x8000;
        pos = setNameRegistrationFlags(pos, flg);
        pos = setIPAddress(pos, name.getIPAddress(addrIdx));

        // Return the packet length

        setLength(pos);
        return pos;
    }

    /**
     * Build a delete name request packet for the specified NetBIOS name
     * 
     * @param name NetBIOSName
     * @param addrIdx int
     * @param tranId int
     * @return int
     */
    public final int buildDeleteNameRequest(NetBIOSName name, int addrIdx, int tranId)
    {

        // Initialize a delete name NetBIOS packet

        setTransactionId(tranId);
        setOpcode(NetBIOSPacket.NAME_RELEASE);
        setFlags(NetBIOSPacket.FLG_BROADCAST + NetBIOSPacket.FLG_RECURSION);

        setQuestionCount(1);
        setAnswerCount(0);
        setNameServiceCount(0);
        setAdditionalCount(1);

        int pos = setQuestionName(name.getName(), name.getType(), 0x20, 0x01);
        pos = setResourceRecord(pos, 12, 0x20, 0x01);
        pos = setTTL(pos, 30000);

        short flg = 0;
        if (name.isGroupName())
            flg = (short) 0x8000;
        pos = setNameRegistrationFlags(pos, flg);
        pos = setIPAddress(pos, name.getIPAddress(addrIdx));

        // Return the packet length

        setLength(pos);
        return pos;
    }

    /**
     * Build a name quesy request packet for the specified NetBIOS name
     * 
     * @param name NetBIOSName
     * @param tranId int
     * @return int
     */
    public final int buildNameQueryRequest(NetBIOSName name, int tranId)
    {

        // Initialize a name query NetBIOS packet

        setTransactionId(tranId);
        setOpcode(NetBIOSPacket.NAME_QUERY);
        setFlags(NetBIOSSession.hasWINSServer() ? 0 : NetBIOSPacket.FLG_BROADCAST);
        setQuestionCount(1);
        return setQuestionName(name, NetBIOSPacket.NAME_TYPE_NB, NetBIOSPacket.NAME_CLASS_IN);
    }

    /**
     * Build a session setup request packet
     * 
     * @param fromName NetBIOSName
     * @param toName NetBIOSName
     * @return int
     */
    public final int buildSessionSetupRequest(NetBIOSName fromName, NetBIOSName toName)
    {

        // Initialize the session setup packet header

        m_nbbuf[0] = (byte) RFCNetBIOSProtocol.SESSION_REQUEST;
        m_nbbuf[1] = (byte) 0; // flags

        // Set the remote NetBIOS name

        int pos = 4;
        byte[] encToName = toName.encodeName();
        System.arraycopy(encToName, 0, m_nbbuf, pos, encToName.length);
        pos += encToName.length;

        // Set the local NetBIOS name

        byte[] encFromName = fromName.encodeName();
        System.arraycopy(encFromName, 0, m_nbbuf, pos, encFromName.length);
        pos += encFromName.length;

        // Set the packet length

        setLength(pos);

        // Set the length in the session request header

        DataPacker.putShort((short) (pos - RFCNetBIOSProtocol.HEADER_LEN), m_nbbuf, 2);

        // Return the packet length

        return pos;
    }

    /**
     * Build an adapter status response
     * 
     * @param nameList NetBIOSNameList
     * @param int nodeType
     * @return int
     */
    public final int buildAdapterStatusResponse(NetBIOSNameList nameList, int nodeType) {

      //  Fill in the header
      
      setOpcode(NetBIOSPacket.RESP_QUERY);
      setFlags(NetBIOSPacket.FLG_RECURSDES + NetBIOSPacket.FLG_AUTHANSWER);

      setQuestionCount(0);
      setAnswerCount(1);
      setAdditionalCount(0);
      setNameServiceCount(0);

      //  Pack the encoded adapter status name into the NetBIOS packet

      int pos = NB_DATA;
      m_nbbuf [ pos++] = ( byte) NAME_LEN;

      pos = DataPacker.putString( AdapterStatusNBName, m_nbbuf, pos, true);

      //  Set the name type and class

      DataPacker.putShort (( short) 0x21, m_nbbuf, pos);
      pos += 2;

      DataPacker.putShort (( short) 0x01, m_nbbuf, pos);
      pos += 2;

      pos = setTTL(pos, 10000);
      pos = setResourceDataLength(pos, (nameList.numberOfNames() * 18) + 42);
      
      //  Pack the names
      
      m_nbbuf[pos++] = (byte) nameList.numberOfNames();
      
      for ( int i = 0; i < nameList.numberOfNames(); i++) {
        
        //  Get the current name
        
        NetBIOSName nbName = nameList.getName( i);
        
        //  Pack the NetBIOS name and flags
        
        System.arraycopy( nbName.getNetBIOSName(), 0, m_nbbuf, pos, NetBIOSName.NameLength);
        pos += NetBIOSName.NameLength;
        
        int flags = nodeType + NAME_ACTIVE;
        if ( nbName.isGroupName())
          flags += NAME_GROUP;
        
        DataPacker.putShort(( short) flags, m_nbbuf, pos); 
        pos += 2;
      }

      //  Zero out the statistics, MAC address area
      
      for ( int i = 0; i < 42; i++)
        m_nbbuf[pos++] = (byte) 0;
      
      //  Set the packet length, and return the length
      
      setLength(pos);
      return getLength();
    }
}