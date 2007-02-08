/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.filesys.server.auth.ntlm;

/**
 * NTLM Constants Class
 *  
 * @author GKSpencer
 */
public class NTLM
{
    // Signature
    
    public static final byte[] Signature = "NTLMSSP\u0000".getBytes();
    
    // Message types
    
    public static final int Type1 = 1;
    public static final int Type2 = 2;
    public static final int Type3 = 3;
    
    // NTLM flags
    
    public static final int FlagNegotiateUnicode    = 0x00000001;
    public static final int FlagNegotiateOEM        = 0x00000002;
    public static final int FlagRequestTarget       = 0x00000004;
    public static final int FlagNegotiateSign       = 0x00000010;
    public static final int FlagNegotiateSeal       = 0x00000020;
    public static final int FlagDatagramStyle       = 0x00000040;
    public static final int FlagLanManKey           = 0x00000080;
    public static final int FlagNegotiateNetware    = 0x00000100;
    public static final int FlagNegotiateNTLM       = 0x00000200;
    public static final int FlagDomainSupplied      = 0x00001000;
    public static final int FlagWorkstationSupplied = 0x00002000;
    public static final int FlagLocalCall           = 0x00004000;
    public static final int FlagAlwaysSign          = 0x00008000;
    public static final int FlagChallengeInit       = 0x00010000;
    public static final int FlagChallengeAccept     = 0x00020000;
    public static final int FlagChallengeNonNT      = 0x00040000;
    public static final int FlagNTLM2Key            = 0x00080000;
    public static final int FlagTargetInfo          = 0x00800000;
    public static final int Flag128Bit              = 0x20000000;
    public static final int FlagKeyExchange         = 0x40000000;
    public static final int Flag56Bit               = 0x80000000;

    // Target information types
    
    public static final int TargetServer            = 0x0001;
    public static final int TargetDomain            = 0x0002;
    public static final int TargetFullDNS           = 0x0003;
    public static final int TargetDNSDomain         = 0x0004;
}
