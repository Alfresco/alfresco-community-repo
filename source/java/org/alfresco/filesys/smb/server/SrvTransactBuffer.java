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
package org.alfresco.filesys.smb.server;

import org.alfresco.filesys.smb.PacketType;
import org.alfresco.filesys.smb.TransactBuffer;
import org.alfresco.filesys.util.DataBuffer;
import org.alfresco.filesys.util.DataPacker;

/**
 * Transact Buffer Class
 * <p>
 * Contains the parameters and data for a transaction, transaction2 or NT transaction request.
 */
class SrvTransactBuffer extends TransactBuffer
{

    /**
     * Class constructor
     * 
     * @param slen int
     * @param plen int
     * @param dlen int
     */
    public SrvTransactBuffer(int slen, int plen, int dlen)
    {
        super(slen, plen, dlen);
    }

    /**
     * Class constructor
     * <p>
     * Construct a TransactBuffer using the maximum size settings from the specified transaction
     * buffer
     * 
     * @param tbuf SrvTransactBuffer
     */
    public SrvTransactBuffer(SrvTransactBuffer tbuf)
    {
        super(tbuf.getReturnSetupLimit(), tbuf.getReturnParameterLimit(), tbuf.getReturnDataLimit());

        // Save the return limits for this transaction buffer

        setReturnLimits(tbuf.getReturnSetupLimit(), tbuf.getReturnParameterLimit(), tbuf.getReturnDataLimit());

        // Set the transaction reply type

        setType(tbuf.isType());

        // Copy the tree id

        setTreeId(tbuf.getTreeId());
    }

    /**
     * Class constructor
     * 
     * @param ntpkt NTTransPacket
     */
    public SrvTransactBuffer(NTTransPacket ntpkt)
    {

        // Call the base constructor so that it does not allocate any buffers

        super(0, 0, 0);

        // Set the tree id

        setTreeId(ntpkt.getTreeId());

        // Set the setup block and size

        int slen = ntpkt.getSetupCount() * 2;
        if (slen > 0)
            m_setupBuf = new DataBuffer(ntpkt.getBuffer(), ntpkt.getSetupOffset(), slen);

        // Set the parameter block and size

        int plen = ntpkt.getTotalParameterCount();
        if (plen > 0)
            m_paramBuf = new DataBuffer(ntpkt.getBuffer(), ntpkt.getParameterBlockOffset(), plen);

        // Set the data block and size

        int dlen = ntpkt.getDataBlockCount();
        if (dlen > 0)
            m_dataBuf = new DataBuffer(ntpkt.getBuffer(), ntpkt.getDataBlockOffset(), dlen);

        // Set the transaction type and sub-function

        setType(ntpkt.getCommand());
        setFunction(ntpkt.getNTFunction());

        // Set the maximum parameter and data block lengths to be returned

        setReturnParameterLimit(ntpkt.getMaximumParameterReturn());
        setReturnDataLimit(ntpkt.getMaximumDataReturn());

        // Set the Unicode flag

        setUnicode(ntpkt.isUnicode());

        // Indicate that this is a not a multi-packet transaction

        m_multi = false;
    }

    /**
     * Class constructor
     * 
     * @param tpkt SMBSrvTransPacket
     */
    public SrvTransactBuffer(SMBSrvTransPacket tpkt)
    {

        // Call the base constructor so that it does not allocate any buffers

        super(0, 0, 0);

        // Set the tree id

        setTreeId(tpkt.getTreeId());

        // Set the setup block and size

        int slen = tpkt.getSetupCount() * 2;
        if (slen > 0)
            m_setupBuf = new DataBuffer(tpkt.getBuffer(), tpkt.getSetupOffset(), slen);

        // Set the parameter block and size

        int plen = tpkt.getTotalParameterCount();
        if (plen > 0)
            m_paramBuf = new DataBuffer(tpkt.getBuffer(), tpkt.getRxParameterBlock(), plen);

        // Set the data block and size

        int dlen = tpkt.getRxDataBlockLength();
        if (dlen > 0)
            m_dataBuf = new DataBuffer(tpkt.getBuffer(), tpkt.getRxDataBlock(), dlen);

        // Set the transaction type and sub-function

        setType(tpkt.getCommand());

        if (tpkt.getSetupCount() > 0)
            setFunction(tpkt.getSetupParameter(0));

        // Set the Unicode flag

        setUnicode(tpkt.isUnicode());

        // Get the transaction name, if used

        if (isType() == PacketType.Transaction)
        {

            // Unpack the transaction name string

            int pos = tpkt.getByteOffset();
            byte[] buf = tpkt.getBuffer();

            if (isUnicode())
                pos = DataPacker.wordAlign(pos);

            setName(DataPacker.getString(buf, pos, 64, isUnicode()));
        }
        else
            setName("");

        // Indicate that this is a not a multi-packet transaction

        m_multi = false;
    }
}
