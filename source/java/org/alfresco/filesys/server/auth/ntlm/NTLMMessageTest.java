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

import junit.framework.TestCase;

/**
 * NTLM Message Test Class
 * 
 * @author GKSpencer
 */
public class NTLMMessageTest extends TestCase
{
    /**
     * Test type 1 message packing/unpacking
     */
    public void testType1Message()
    {
        // Create a minimal type 1 message
        
        Type1NTLMMessage ntlmMsg = new Type1NTLMMessage();
        ntlmMsg.buildType1(0, null, null);
        
        assertEquals("Minimal type 1 message length wrong", 16, ntlmMsg.getLength());
        assertFalse("Minimal type 1 message domain supplied flag set", ntlmMsg.hasFlag(NTLM.FlagDomainSupplied));
        assertFalse("Minimal type 1 message workstation supplied flag set", ntlmMsg.hasFlag(NTLM.FlagWorkstationSupplied));
        assertFalse("Minimal type 1 has domain", ntlmMsg.hasDomain());
        assertNull("Minimal type 1 domain not null", ntlmMsg.getDomain());
        assertFalse("Minimal type 1 has workstation", ntlmMsg.hasWorkstation());
        assertNull("Minimal type 1 workstation not null", ntlmMsg.getWorkstation());
        
        // Use a buffer to build a type 1 message
        
        byte[] buf = new byte[256];
        ntlmMsg = new Type1NTLMMessage(buf, 128, 128);
        ntlmMsg.buildType1(0, null, null);
        
        assertEquals("Minimal type 1 message length wrong", 16, ntlmMsg.getLength());
        assertFalse("Minimal type 1 message domain supplied flag set", ntlmMsg.hasFlag(NTLM.FlagDomainSupplied));
        assertFalse("Minimal type 1 message workstation supplied flag set", ntlmMsg.hasFlag(NTLM.FlagWorkstationSupplied));
        assertFalse("Minimal type 1 has domain", ntlmMsg.hasDomain());
        assertNull("Minimal type 1 domain not null", ntlmMsg.getDomain());
        assertFalse("Minimal type 1 has workstation", ntlmMsg.hasWorkstation());
        assertNull("Minimal type 1 workstation not null", ntlmMsg.getWorkstation());
        
        // Test type 1 with domain name only
        
        String testDomain = "TESTDOMAIN";
        String testWks    = "TESTPC";
        
        ntlmMsg = new Type1NTLMMessage();
        ntlmMsg.buildType1(0, testDomain, null);
        
        assertTrue("Minimal type 1 message length wrong", ntlmMsg.getLength() > 16);
        assertTrue("Minimal type 1 message domain supplied flag not set", ntlmMsg.hasFlag(NTLM.FlagDomainSupplied));
        assertFalse("Minimal type 1 message workstation supplied flag set", ntlmMsg.hasFlag(NTLM.FlagWorkstationSupplied));
        assertTrue("Minimal type 1 no domain", ntlmMsg.hasDomain());
        assertEquals("Minimal type 1 domain not correct", testDomain, ntlmMsg.getDomain());
        assertFalse("Minimal type 1 has workstation", ntlmMsg.hasWorkstation());
        assertNull("Minimal type 1 workstation not null", ntlmMsg.getWorkstation());
        
        // Test type 1 with domain name only with buffer

        ntlmMsg = new Type1NTLMMessage(buf, 128, 128);
        ntlmMsg.buildType1(0, testDomain, null);

        assertTrue("Minimal type 1 message length wrong", ntlmMsg.getLength() > 16);
        assertTrue("Minimal type 1 message domain supplied flag not set", ntlmMsg.hasFlag(NTLM.FlagDomainSupplied));
        assertFalse("Minimal type 1 message workstation supplied flag set", ntlmMsg.hasFlag(NTLM.FlagWorkstationSupplied));
        assertTrue("Minimal type 1 no domain", ntlmMsg.hasDomain());
        assertEquals("Minimal type 1 domain not correct", testDomain, ntlmMsg.getDomain());
        assertFalse("Minimal type 1 has workstation", ntlmMsg.hasWorkstation());
        assertNull("Minimal type 1 workstation not null", ntlmMsg.getWorkstation());

        // Test type 1 with workstation name only
        
        ntlmMsg = new Type1NTLMMessage();
        ntlmMsg.buildType1(0, null, testWks);
        
        assertTrue("Minimal type 1 message length wrong", ntlmMsg.getLength() > 16);
        assertFalse("Minimal type 1 message domain supplied flag set", ntlmMsg.hasFlag(NTLM.FlagDomainSupplied));
        assertTrue("Minimal type 1 message workstation supplied flag not set", ntlmMsg.hasFlag(NTLM.FlagWorkstationSupplied));
        assertFalse("Minimal type 1 has domain", ntlmMsg.hasDomain());
        assertNull("Minimal type 1 domain not null", ntlmMsg.getDomain());
        assertTrue("Minimal type 1 no workstation", ntlmMsg.hasWorkstation());
        assertEquals("Minimal type 1 workstation not correct", testWks, ntlmMsg.getWorkstation());
        
        // Test type 1 with domain name only with buffer

        ntlmMsg = new Type1NTLMMessage(buf, 128, 128);
        ntlmMsg.buildType1(0, null, testWks);

        assertTrue("Minimal type 1 message length wrong", ntlmMsg.getLength() > 16);
        assertFalse("Minimal type 1 message domain supplied flag set", ntlmMsg.hasFlag(NTLM.FlagDomainSupplied));
        assertTrue("Minimal type 1 message workstation supplied flag not set", ntlmMsg.hasFlag(NTLM.FlagWorkstationSupplied));
        assertFalse("Minimal type 1 has domain", ntlmMsg.hasDomain());
        assertNull("Minimal type 1 domain not null", ntlmMsg.getDomain());
        assertTrue("Minimal type 1 no workstation", ntlmMsg.hasWorkstation());
        assertEquals("Minimal type 1 workstation not correct", testWks, ntlmMsg.getWorkstation());
    
        // Test type 1 with domain and workstation names
        
        ntlmMsg = new Type1NTLMMessage();
        ntlmMsg.buildType1(0, testDomain, testWks);
        
        assertTrue("Minimal type 1 message length wrong", ntlmMsg.getLength() > 16);
        assertTrue("Minimal type 1 message domain supplied flag not set", ntlmMsg.hasFlag(NTLM.FlagDomainSupplied));
        assertTrue("Minimal type 1 message workstation supplied flag not set", ntlmMsg.hasFlag(NTLM.FlagWorkstationSupplied));
        assertTrue("Minimal type 1 has domain", ntlmMsg.hasDomain());
        assertEquals("Minimal type 1 domain not correct", testDomain, ntlmMsg.getDomain());
        assertTrue("Minimal type 1 no workstation", ntlmMsg.hasWorkstation());
        assertEquals("Minimal type 1 workstation not correct", testWks, ntlmMsg.getWorkstation());
        
        // Test type 1 with domain and workstation names, with buffer

        ntlmMsg = new Type1NTLMMessage(buf, 128, 128);
        ntlmMsg.buildType1(0, testDomain, testWks);

        assertTrue("Minimal type 1 message length wrong", ntlmMsg.getLength() > 16);
        assertTrue("Minimal type 1 message domain supplied flag not set", ntlmMsg.hasFlag(NTLM.FlagDomainSupplied));
        assertTrue("Minimal type 1 message workstation supplied flag not set", ntlmMsg.hasFlag(NTLM.FlagWorkstationSupplied));
        assertTrue("Minimal type 1 has domain", ntlmMsg.hasDomain());
        assertEquals("Minimal type 1 domain not correct", testDomain, ntlmMsg.getDomain());
        assertTrue("Minimal type 1 no workstation", ntlmMsg.hasWorkstation());
        assertEquals("Minimal type 1 workstation not correct", testWks, ntlmMsg.getWorkstation());
    }
    
    /**
     * Test type 2 message packing/unpacking
     */
    public void testType2Message()
    {
       // No tests yet, only receive type 2 from the server 
    }
    
    /**
     * Test type 3 message packing/unpacking
     */
    public void testType3Message()
    {
        
    }
}
