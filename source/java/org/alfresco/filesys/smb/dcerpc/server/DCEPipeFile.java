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

import java.io.IOException;

import org.alfresco.filesys.server.filesys.NetworkFile;
import org.alfresco.filesys.smb.dcerpc.DCEBuffer;
import org.alfresco.filesys.smb.dcerpc.DCEPipeType;

/**
 * DCE/RPC Pipe File Class
 * <p>
 * Contains the details and state of a DCE/RPC special named pipe.
 */
public class DCEPipeFile extends NetworkFile
{

    // Maximum receive/transmit DCE fragment size

    private int m_maxRxFragSize;
    private int m_maxTxFragSize;

    // Named pipe state flags

    private int m_state;

    // DCE/RPC handler for this named pipe

    private DCEHandler m_handler;

    // Current DCE buffered data

    private DCEBuffer m_dceData;

    /**
     * Class constructor
     * 
     * @param id int
     */
    public DCEPipeFile(int id)
    {
        super(id);
        setName(DCEPipeType.getTypeAsString(id));

        // Set the DCE/RPC request handler for the pipe

        setRequestHandler(DCEPipeHandler.getHandlerForType(id));
    }

    /**
     * Return the maximum receive fragment size
     * 
     * @return int
     */
    public final int getMaxReceiveFragmentSize()
    {
        return m_maxRxFragSize;
    }

    /**
     * Return the maximum transmit fragment size
     * 
     * @return int
     */
    public final int getMaxTransmitFragmentSize()
    {
        return m_maxTxFragSize;
    }

    /**
     * Return the named pipe state
     * 
     * @return int
     */
    public final int getPipeState()
    {
        return m_state;
    }

    /**
     * Return the pipe type id
     * 
     * @return int
     */
    public final int getPipeId()
    {
        return getFileId();
    }

    /**
     * Determine if the pipe has a request handler
     * 
     * @return boolean
     */
    public final boolean hasRequestHandler()
    {
        return m_handler != null ? true : false;
    }

    /**
     * Return the pipes DCE/RPC handler
     * 
     * @return DCEHandler
     */
    public final DCEHandler getRequestHandler()
    {
        return m_handler;
    }

    /**
     * Determine if the pipe has any buffered data
     * 
     * @return boolean
     */
    public final boolean hasBufferedData()
    {
        return m_dceData != null ? true : false;
    }

    /**
     * Get the buffered data for the pipe
     * 
     * @return DCEBuffer
     */
    public final DCEBuffer getBufferedData()
    {
        return m_dceData;
    }

    /**
     * Set buffered data for the pipe
     * 
     * @param buf DCEBuffer
     */
    public final void setBufferedData(DCEBuffer buf)
    {
        m_dceData = buf;
    }

    /**
     * Set the maximum receive fragment size
     * 
     * @param siz int
     */
    public final void setMaxReceiveFragmentSize(int siz)
    {
        m_maxRxFragSize = siz;
    }

    /**
     * Set the maximum transmit fragment size
     * 
     * @param siz int
     */
    public final void setMaxTransmitFragmentSize(int siz)
    {
        m_maxTxFragSize = siz;
    }

    /**
     * Set the named pipe state flags
     * 
     * @param state int
     */
    public final void setPipeState(int state)
    {
        m_state = state;
    }

    /**
     * Set the pipes DCE/RPC handler
     * 
     * @param handler DCEHandler
     */
    public final void setRequestHandler(DCEHandler handler)
    {
        m_handler = handler;
    }

    /**
     * Dump the file details
     */
    public final void DumpFile()
    {
        System.out.println("** DCE/RPC Named Pipe: " + getName());
        System.out.println("  File ID : " + getFileId());
        System.out.println("  State   : 0x" + Integer.toHexString(getPipeState()));
        System.out.println("  Max Rx  : " + getMaxReceiveFragmentSize());
        System.out.println("  Max Tx  : " + getMaxTransmitFragmentSize());
        System.out.println("  Handler : " + getRequestHandler());
    }

    /**
     * @see NetworkFile#closeFile()
     */
    public void closeFile() throws IOException
    {
    }

    /**
     * @see NetworkFile#openFile(boolean)
     */
    public void openFile(boolean createFlag) throws IOException
    {
    }

    /**
     * @see NetworkFile#readFile(byte[], int, int, long)
     */
    public int readFile(byte[] buf, int len, int pos, long fileOff) throws IOException
    {
        return 0;
    }

    /**
     * Flush any buffered output to the file
     * 
     * @throws IOException
     */
    public void flushFile() throws IOException
    {
    }

    /**
     * @see NetworkFile#seekFile(long, int)
     */
    public long seekFile(long pos, int typ) throws IOException
    {
        return 0;
    }

    /**
     * @see NetworkFile#truncateFile(long)
     */
    public void truncateFile(long siz) throws IOException
    {
    }

    /**
     * @see NetworkFile#writeFile(byte[], int, int, long)
     */
    public void writeFile(byte[] buf, int len, int pos, long fileOff) throws IOException
    {
    }
}