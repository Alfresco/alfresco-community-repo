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
    public static final int FlagNegotiateNTLM       = 0x00000200;
    public static final int FlagDomainSupplied      = 0x00001000;
    public static final int FlagWorkstationSupplied = 0x00002000;
    public static final int FlagLocalCall           = 0x00004000;
    public static final int FlagAlwaysSign          = 0x00008000;
    public static final int FlagTypeDomain          = 0x00010000;
    public static final int FlagTypeServer          = 0x00020000;
    public static final int FlagTypeShare           = 0x00040000;
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
