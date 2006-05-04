/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
package org.alfresco.filesys.server.auth.ntlm;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.filesys.util.HexDump;

/**
 * Type 2 NTLM Message Class
 * 
 * @author GKSpencer
 */
public class Type2NTLMMessage extends NTLMMessage
{
    // Minimal type 2 message length
    
    public static final int MinimalMessageLength = 32;
    
    // Type 2 field offsets
    
    public static final int OffsetTarget                = 12;
    public static final int OffsetFlags                 = 20;
    public static final int OffsetChallenge             = 24;
    public static final int OffsetContext               = 32;
    public static final int OffsetTargetInfo            = 40;   // optional
    
    /**
     * Default constructor
     */
    public Type2NTLMMessage()
    {
        super();
    }
    
    /**
     * Class constructor
     * 
     * @param buf byte[]
     */
    public Type2NTLMMessage(byte[] buf)
    {
        super(buf, 0, buf.length);
    }
    
    /**
     * Class constructor
     * 
     * @param buf byte[]
     * @param offset int
     * @param len int
     */
    public Type2NTLMMessage(byte[] buf, int offset, int len)
    {
        super(buf, offset, len);
    }

    /**
     * Return the flags value
     * 
     * @return int
     */
    public int getFlags()
    {
        return getIntValue(OffsetFlags);
    }
    
    /**
     * Check if the target name has been set
     * 
     * @return boolean
     */
    public final boolean hasTarget()
    {
        return hasFlag(NTLM.FlagRequestTarget);
    }
    
    /**
     * Return the target name
     * 
     * @return String
     */
    public final String getTarget()
    {
        return getStringValue(OffsetTarget, hasFlag(NTLM.FlagNegotiateUnicode));
    }
    
    /**
     * Return the challenge
     * 
     * @return byte[]
     */
    public final byte[] getChallenge()
    {
        return getRawBytes(OffsetChallenge, 8);
    }
    
    /**
     * Check if the optional context field is present
     * 
     * @return boolean
     */
    public final boolean hasContext()
    {
        return hasFlag(NTLM.FlagLocalCall);
    }
    
    /**
     * Return the context values
     * 
     * @return int[]
     */
    public final int[] getContext()
    {
        if ( hasContext() == false)
            return null;
        
        int[] ctx = new int[2];
        
        ctx[0] = getIntValue(OffsetContext);
        ctx[1] = getIntValue(OffsetContext + 4);
        
        return ctx;
    }
    
    /**
     * Check if target information is present
     * 
     * @return boolean
     */
    public final boolean hasTargetInformation()
    {
        return hasFlag(NTLM.FlagTargetInfo);
    }
    
    /**
     * Return the target information
     * 
     * @return List<TargetInfo>
     */
    public final List<TargetInfo> getTargetInformation()
    {
        if ( hasTargetInformation() == false)
            return null;
        
        // Get the target information block length and offset
        
        int tLen = getStringLength(OffsetTargetInfo);
        int tOff = getStringOffset(OffsetTargetInfo);
        
        List<TargetInfo> tList = new ArrayList<TargetInfo>();
        if ( tLen == 0)
            return tList;
        
        // Unpack the target information structures
        
        int typ = -1;
        int slen = -1;
        String name = null;
        
        while ( typ != 0)
        {
            // Unpack the details for the current target
            
            typ  = getShortValue(tOff);
            slen = getShortValue(tOff + 2);
            
            if ( slen > 0)
                name = getRawString(tOff + 4, slen/2, true);
            else
                name = null;
            
            // Add the details to the list
            
            if ( typ != 0)
                tList.add( new TargetInfo(typ, name));
            
            // Update the data offset
            
            tOff += slen + 4;
        }
        
        // Return the target list
        
        return tList;
    }
    
    /**
     * Build a type 2 message
     * 
     * @param flags int
     * @param target String
     * @param challenge byte[]
     * @param ctx byte[]
     * @param tList List<TargetInfo>
     */
    public final void buildType2(int flags, String target, byte[] challenge, int[] ctx, List<TargetInfo> tList)
    {
        // Initialize the header/flags
        
        initializeHeader(NTLM.Type2, flags);

        // Determine if strings are ASCII or Unicode
        
        boolean isUni = hasFlag(NTLM.FlagNegotiateUnicode);
        
        int strOff = OffsetTargetInfo;
        if ( tList != null)
            strOff += 8;
        
        // Pack the target name
        
        strOff = setStringValue(OffsetTarget, target, strOff, isUni);
        
        // Pack the challenge and context
        
        if ( challenge != null)
            setRawBytes(OffsetChallenge, challenge);
        else
            zeroBytes(OffsetChallenge, 8);
        
        if ( ctx != null)
            setRawInts(OffsetContext, ctx);
        else
            zeroBytes(OffsetContext, 8);
        
        // Pack the target information, if specified
        
        if ( tList != null)
        {
            // Clear the target information length and set the data offset
            
            setIntValue(OffsetTargetInfo, 0);
            setIntValue(OffsetTargetInfo+4, strOff);
            
            int startOff = strOff;
            
            // Pack the target information structures
            
            for ( TargetInfo tInfo : tList)
            {
                // Pack the target information structure
                
                setShortValue(strOff, tInfo.isType());
                
                int tLen = tInfo.getName().length();
                if ( isUni)
                    tLen *= 2;
                setShortValue(strOff+2, tLen);
                strOff = setRawString(strOff+4, tInfo.getName(), isUni);
            }
            
            // Add the list terminator
            
            zeroBytes(strOff, 4);
            strOff += 4;
            
            // Set the target information block length
            
            setShortValue(OffsetTargetInfo, strOff - startOff);
            setShortValue(OffsetTargetInfo+2, strOff - startOff);
        }
        
        // Set the message length
        
        setLength(strOff);
    }
    
    /**
     * Set the message flags
     * 
     * @param flags int
     */
    protected void setFlags(int flags)
    {
        setIntValue( OffsetFlags, flags);
    }
    
    /**
     * Return the type 2 message as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        
        str.append("[Type2:0x");
        str.append(Integer.toHexString(getFlags()));
        str.append(",Target:");
        str.append(getTarget());
        str.append(",Ch:");
        str.append(HexDump.hexString(getChallenge()));
        
        if ( hasTargetInformation())
        {
            List<TargetInfo> targets = getTargetInformation();
            
            str.append(",TargInf:");
            for ( TargetInfo target : targets)
            {
                str.append(target);
            }
        }
        str.append("]");
        
        return str.toString();
    }
}
