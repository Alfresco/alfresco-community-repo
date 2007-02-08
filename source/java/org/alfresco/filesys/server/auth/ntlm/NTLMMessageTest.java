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
