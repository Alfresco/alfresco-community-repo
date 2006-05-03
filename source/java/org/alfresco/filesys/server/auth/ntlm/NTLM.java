/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Alfresco Network License. You may obtain a
 * copy of the License at
 *
 *   http://www.alfrescosoftware.com/legal/
 *
 * Please view the license relevant to your network subscription.
 *
 * BY CLICKING THE "I UNDERSTAND AND ACCEPT" BOX, OR INSTALLING,  
 * READING OR USING ALFRESCO'S Network SOFTWARE (THE "SOFTWARE"),  
 * YOU ARE AGREEING ON BEHALF OF THE ENTITY LICENSING THE SOFTWARE    
 * ("COMPANY") THAT COMPANY WILL BE BOUND BY AND IS BECOMING A PARTY TO 
 * THIS ALFRESCO NETWORK AGREEMENT ("AGREEMENT") AND THAT YOU HAVE THE   
 * AUTHORITY TO BIND COMPANY. IF COMPANY DOES NOT AGREE TO ALL OF THE   
 * TERMS OF THIS AGREEMENT, DO NOT SELECT THE "I UNDERSTAND AND AGREE"   
 * BOX AND DO NOT INSTALL THE SOFTWARE OR VIEW THE SOURCE CODE. COMPANY   
 * HAS NOT BECOME A LICENSEE OF, AND IS NOT AUTHORIZED TO USE THE    
 * SOFTWARE UNLESS AND UNTIL IT HAS AGREED TO BE BOUND BY THESE LICENSE  
 * TERMS. THE "EFFECTIVE DATE" FOR THIS AGREEMENT SHALL BE THE DAY YOU  
 * CHECK THE "I UNDERSTAND AND ACCEPT" BOX.
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
