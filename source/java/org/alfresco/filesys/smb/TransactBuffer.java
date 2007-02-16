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
package org.alfresco.filesys.smb;

import org.alfresco.filesys.util.DataBuffer;

/**
 * Transact Buffer Class
 * <p>
 * Contains the parameters and data for a transaction, transaction2 or NT transaction request.
 */
public class TransactBuffer
{

    // Default buffer sizes

    protected static final int DefaultSetupSize = 32;
    protected static final int DefaultDataSize = 8192;
    protected static final int DefaultParameterSize = 64;

    // Default maximum return buffer sizes

    protected static final int DefaultMaxSetupReturn = 16;
    protected static final int DefaultMaxParameterReturn = 256;
    protected static final int DefaultMaxDataReturn = 65000;

    // Tree id, connection that the transaction is for

    protected int m_treeId = -1;

    // Transaction packet type and sub-function

    protected int m_type;
    protected int m_func;

    // Transaction name, for Transaction2 only

    protected String m_name;

    // Setup parameters

    protected DataBuffer m_setupBuf;

    // Parameter block

    protected DataBuffer m_paramBuf;

    // Data block and read/write position

    protected DataBuffer m_dataBuf;

    // Flag to indicate if this is a multi-packet transaction

    protected boolean m_multi;

    // Unicode strings flag

    protected boolean m_unicode;

    // Maximum setup, parameter and data bytes to return

    protected int m_maxSetup = DefaultMaxSetupReturn;
    protected int m_maxParam = DefaultMaxParameterReturn;
    protected int m_maxData = DefaultMaxDataReturn;

    /**
     * Default constructor
     */
    public TransactBuffer()
    {
        m_setupBuf = new DataBuffer(DefaultSetupSize);
        m_paramBuf = new DataBuffer(DefaultParameterSize);
        m_dataBuf = new DataBuffer(DefaultDataSize);
    }

    /**
     * Class constructor
     * 
     * @param scnt int
     * @param pcnt int
     * @param dcnt int
     */
    public TransactBuffer(int scnt, int pcnt, int dcnt)
    {

        // Allocate the setup parameter buffer

        if (scnt > 0)
            m_setupBuf = new DataBuffer(scnt);

        // Allocate the paramater buffer

        if (pcnt > 0)
            m_paramBuf = new DataBuffer(pcnt);

        // Allocate the data buffer

        if (dcnt > 0)
            m_dataBuf = new DataBuffer(dcnt);

        // Multi-packet transaction

        m_multi = true;
    }

    /**
     * Class constructor
     * 
     * @param cmd int
     * @param scnt int
     * @param pcnt int
     * @param dcnt int
     */
    public TransactBuffer(int cmd, int scnt, int pcnt, int dcnt)
    {

        // Set the command

        setType(cmd);

        // Allocate the setup parameter buffer

        if (scnt > 0)
            m_setupBuf = new DataBuffer(scnt);

        // Allocate the paramater buffer

        if (pcnt > 0)
            m_paramBuf = new DataBuffer(pcnt);

        // Allocate the data buffer

        if (dcnt > 0)
            m_dataBuf = new DataBuffer(dcnt);

        // Multi-packet transaction

        m_multi = true;
    }

    /**
     * Class constructor
     * 
     * @param func int
     * @param name String
     * @param scnt int
     * @param pcnt int
     * @param dcnt int
     */
    public TransactBuffer(int func, String name, int scnt, int pcnt, int dcnt)
    {

        // Set the name, for Transaction2

        setName(name);

        // Allocate the setup parameter buffer

        if (scnt > 0)
            m_setupBuf = new DataBuffer(scnt);

        // Allocate the paramater buffer

        if (pcnt > 0)
            m_paramBuf = new DataBuffer(pcnt);

        // Allocate the data buffer

        if (dcnt > 0)
            m_dataBuf = new DataBuffer(dcnt);

        // Set the function code

        setFunction(func);

        // Multi-packet transaction

        m_multi = true;
    }

    /**
     * Class constructor
     * 
     * @param func int
     * @param scnt int
     * @param pcnt int
     * @param dbuf byte[]
     * @param doff int
     * @param dlen int
     */
    public TransactBuffer(int func, int scnt, int pcnt, byte[] dbuf, int doff, int dlen)
    {

        // Allocate the setup parameter buffer

        if (scnt > 0)
            m_setupBuf = new DataBuffer(scnt);

        // Allocate the paramater buffer

        if (pcnt > 0)
            m_paramBuf = new DataBuffer(pcnt);

        // Allocate the data buffer

        if (dbuf != null)
            m_dataBuf = new DataBuffer(dbuf, doff, dlen);

        // Set the function code

        setFunction(func);

        // Multi-packet transaction

        m_multi = true;
    }

    /**
     * Determine if the tree id has been set
     * 
     * @return boolean
     */
    public final boolean hasTreeId()
    {
        return m_treeId != -1 ? true : false;
    }

    /**
     * Return the tree id
     * 
     * @return int
     */
    public final int getTreeId()
    {
        return m_treeId;
    }

    /**
     * Return the transaction type (from SBMSrvPacketType, either Transaction, Transaction2 or
     * NTTransact)
     * 
     * @return int
     */
    public final int isType()
    {
        return m_type;
    }

    /**
     * Return the transaction function
     * 
     * @return int
     */
    public final int getFunction()
    {
        return m_func;
    }

    /**
     * Determine if the transaction has a name
     * 
     * @return boolean
     */
    public final boolean hasName()
    {
        return m_name != null ? true : false;
    }

    /**
     * Return the transaction name
     * 
     * @return String
     */
    public final String getName()
    {
        return m_name;
    }

    /**
     * Determine if this is a multi-packet transaction
     * 
     * @return boolean
     */
    public final boolean isMultiPacket()
    {
        return m_multi;
    }

    /**
     * Determine if the client is using Unicode strings
     * 
     * @return boolean
     */
    public final boolean isUnicode()
    {
        return m_unicode;
    }

    /**
     * Determine if the transaction buffer has setup data
     * 
     * @return boolean
     */
    public final boolean hasSetupBuffer()
    {
        return m_setupBuf != null ? true : false;
    }

    /**
     * Return the setup parameter buffer
     * 
     * @return DataBuffer
     */
    public final DataBuffer getSetupBuffer()
    {
        return m_setupBuf;
    }

    /**
     * Determine if the transaction buffer has parameter data
     * 
     * @return boolean
     */
    public final boolean hasParameterBuffer()
    {
        return m_paramBuf != null ? true : false;
    }

    /**
     * Return the parameter buffer
     * 
     * @return DataBuffer
     */
    public final DataBuffer getParameterBuffer()
    {
        return m_paramBuf;
    }

    /**
     * Determine if the transaction buffer has a data block
     * 
     * @return boolean
     */
    public final boolean hasDataBuffer()
    {
        return m_dataBuf != null ? true : false;
    }

    /**
     * Return the data buffer
     * 
     * @return DataBuffer
     */
    public final DataBuffer getDataBuffer()
    {
        return m_dataBuf;
    }

    /**
     * Return the setup return data limit
     * 
     * @return int
     */
    public final int getReturnSetupLimit()
    {
        return m_maxSetup;
    }

    /**
     * Return the parameter return data limit
     * 
     * @return int
     */
    public final int getReturnParameterLimit()
    {
        return m_maxParam;
    }

    /**
     * Return the data return data limit
     * 
     * @return int
     */
    public final int getReturnDataLimit()
    {
        return m_maxData;
    }

    /**
     * Set the tree id
     * 
     * @param tid int
     */
    public final void setTreeId(int tid)
    {
        m_treeId = tid;
    }

    /**
     * Set the transaction type
     * 
     * @param typ int
     */
    public final void setType(int typ)
    {
        m_type = typ;
    }

    /**
     * Set the transaction function
     * 
     * @param func int
     */
    public final void setFunction(int func)
    {
        m_func = func;
    }

    /**
     * Set the transaction name, for Transactin2
     * 
     * @param name String
     */
    public final void setName(String name)
    {
        m_name = name;
    }

    /**
     * Set the Unicode strings flag
     * 
     * @param uni boolean
     */
    public final void setUnicode(boolean uni)
    {
        m_unicode = uni;
    }

    /**
     * Set the limit of returned setup bytes
     * 
     * @param limit int
     */
    public final void setReturnSetupLimit(int limit)
    {
        m_maxSetup = limit;
    }

    /**
     * Set the limit of returned parameter bytes
     * 
     * @param limit int
     */
    public final void setReturnParameterLimit(int limit)
    {
        m_maxParam = limit;
    }

    /**
     * Set the limit of returned data bytes
     * 
     * @param limit int
     */
    public final void setReturnDataLimit(int limit)
    {
        m_maxData = limit;
    }

    /**
     * Set the setup, parameter and data return data limits
     * 
     * @param slimit int
     * @param plimit int
     * @param dlimit int
     */
    public final void setReturnLimits(int slimit, int plimit, int dlimit)
    {
        setReturnSetupLimit(slimit);
        setReturnParameterLimit(plimit);
        setReturnDataLimit(dlimit);
    }

    /**
     * Set the end of buffer positions for the setup, parameter and data buffers ready for reading
     * the data.
     */
    public final void setEndOfBuffer()
    {

        // Set the end of the setup buffer

        if (m_setupBuf != null)
            m_setupBuf.setEndOfBuffer();

        // Set the end of the parameter buffer

        if (m_paramBuf != null)
            m_paramBuf.setEndOfBuffer();

        // Set the end of the data buffer

        if (m_dataBuf != null)
            m_dataBuf.setEndOfBuffer();
    }

    /**
     * Append setup data to the setup data buffer
     * 
     * @param buf byte[]
     * @param off int
     * @param len int
     */
    public final void appendSetup(byte[] buf, int off, int len)
    {
        m_setupBuf.appendData(buf, off, len);
    }

    /**
     * Append parameter data to the parameter data buffer
     * 
     * @param buf byte[]
     * @param off int
     * @param len int
     */
    public final void appendParameter(byte[] buf, int off, int len)
    {
        m_paramBuf.appendData(buf, off, len);
    }

    /**
     * Append data to the data buffer
     * 
     * @param buf byte[]
     * @param off int
     * @param len int
     */
    public final void appendData(byte[] buf, int off, int len)
    {
        m_dataBuf.appendData(buf, off, len);
    }

    /**
     * Return the transaction buffer details as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();

        str.append("[");

        switch (isType())
        {
        case PacketType.Transaction:
            str.append("Trans");
            break;
        case PacketType.Transaction2:
            str.append("Trans2(");
            str.append(getName());
            str.append(")");
            break;
        case PacketType.NTTransact:
            str.append("NTTrans");
            break;
        default:
            str.append("Unknown");
            break;
        }
        str.append("-0x");
        str.append(Integer.toHexString(getFunction()));

        str.append(": setup=");
        if (m_setupBuf != null)
            str.append(m_setupBuf);
        else
            str.append("none");

        str.append(",param=");
        if (m_paramBuf != null)
            str.append(m_paramBuf);
        else
            str.append("none");

        str.append(",data=");
        if (m_dataBuf != null)
            str.append(m_dataBuf);
        else
            str.append("none");
        str.append("]");

        str.append(",max=");
        str.append(getReturnSetupLimit());

        str.append("/");
        str.append(getReturnParameterLimit());

        str.append("/");
        str.append(getReturnDataLimit());

        return str.toString();
    }
}
