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
package org.alfresco.filesys.server.auth.ntlm;

import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.alfresco.filesys.smb.NTTime;
import org.alfresco.filesys.util.DataPacker;
import org.alfresco.filesys.util.HexDump;

/**
 * NTLMv2 Blob Class
 * 
 * <P>Contains methods to pack/unpack and calculate the hash of an NTLMv2 blob.
 * 
 * @author gkspencer
 */
public class NTLMv2Blob
{
    //  Constants
    
    public static final int HMAC_LEN        = 16;
    public static final int CHALLENGE_LEN   = 8;
    
    //  Offsets
    
    public static final int OFFSET_HMAC         = 0;
    public static final int OFFSET_HEADER       = 16;
    public static final int OFFSET_RESERVED     = 20;
    public static final int OFFSET_TIMESTAMP    = 24;
    public static final int OFFSET_CHALLENGE    = 32;
    public static final int OFFSET_UNKNOWN      = 36;
    public static final int OFFSET_TARGETINFO   = 40;
    
    //  NTLMv2 blob
    
    private byte[] m_blob;
    private int m_offset;
    private int m_len;
    
    /**
     * Class constructor
     * 
     * @param buf byte[]
     */
    public NTLMv2Blob(byte[] buf)
    {
        m_blob = buf;
        m_offset = 0;
        m_len = m_blob.length;
    }
    
    /**
     * Class constructor
     * 
     * @param buf byte[]
     * @param offset int
     * @param len int
     */
    public NTLMv2Blob(byte[] buf, int offset, int len)
    {
        m_blob = buf;
        m_offset = offset;
        m_len = len;
    }
    
    /**
     * Return the buffer
     * 
     * @return byte[]
     */
    public final byte[] getBuffer()
    {
        return m_blob;
    }
    
    /**
     * Return the offset
     * 
     * @return int
     */
    public final int getOffset()
    {
        return m_offset;
    }

    /**
     * Return the blob length
     * 
     * @return int
     */
    public final int getLength()
    {
        return m_len;
    }
    
    /**
     * Return the HMAC from the buffer
     * 
     * @return byte[]
     */
    public final byte[] getHMAC()
    {
        byte[] hmac = new byte[HMAC_LEN];
        System.arraycopy(m_blob, m_offset, hmac, 0, HMAC_LEN);
        
        return hmac;
    }
    
    /**
     * Return the timestamp from the buffer, in NT 64bit time format
     * 
     * @return long
     */
    public final long getTimeStamp()
    {
        return DataPacker.getIntelLong(m_blob, m_offset + OFFSET_TIMESTAMP);
    }
    
    /**
     * Return the client challenge
     * 
     * @return byte[]
     */
    public final byte[] getClientChallenge()
    {
        byte[] challenge = new byte[CHALLENGE_LEN];
        System.arraycopy( m_blob, m_offset + OFFSET_CHALLENGE, challenge, 0, CHALLENGE_LEN);
        
        return challenge;
    }
    
    /**
     * Calculate the HMAC of the blob using the specified NTLMv2 hash and challenge
     * 
     * @param challenge byte[]
     * @param v2hash byte[]
     * @return byte[]
     * @exception Exception
     */
    public final byte[] calculateHMAC( byte[] challenge, byte[] v2hash)
        throws Exception
    {
        // Create a copy of the NTLMv2 blob with room for the challenge
        
        byte[] blob = new byte[(m_len - HMAC_LEN) + CHALLENGE_LEN];
        System.arraycopy( challenge, 0, blob, 0, CHALLENGE_LEN);
        System.arraycopy( m_blob, m_offset + OFFSET_HEADER, blob, CHALLENGE_LEN, m_len - HMAC_LEN);
        
        // Generate the HMAC of the blob using the v2 hash as the key
        
        Mac hmacMd5 = Mac.getInstance( "HMACMD5");
        SecretKeySpec blobKey = new SecretKeySpec( v2hash, 0, v2hash.length, "MD5");
        
        hmacMd5.init( blobKey);
        return hmacMd5.doFinal( blob);
    }
    
    /**
     * Dump the NTLMv2 blob details
     */
    public final void Dump()
    {
        System.out.println("NTLMv2 blob :");
        System.out.println("       HMAC : " + HexDump.hexString( getHMAC()));
        System.out.println("     Header : 0x" + Integer.toHexString(DataPacker.getIntelInt( m_blob, m_offset + OFFSET_HEADER)));
        System.out.println("  Timestamp : " + new Date(NTTime.toJavaDate( getTimeStamp())));
        System.out.println("  Challenge : " + HexDump.hexString( getClientChallenge()));
    }
}
