/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.security.authentication;

import junit.framework.TestCase;

public class NameBasedUserNameGeneratorTest extends TestCase
{
	public void testGenerate()
	{
		NameBasedUserNameGenerator generator = new NameBasedUserNameGenerator();
		generator.setUserNameLength(10);
		generator.setNamePattern("%firstName%_%lastName%");
		
		String firstName = "Buffy";
		String lastName = "Summers";
		String emailAddress = "buffy@sunnydale.com";
		
		// should generate buffy_summers
		String userName = generator.generateUserName(firstName, lastName, emailAddress, 0);
		assertEquals("", (firstName + "_" + lastName).toLowerCase(), userName);
		
		// should generate something different from above since seed > 0
		userName = generator.generateUserName(firstName, lastName, emailAddress, 1);
		assertEquals("", (firstName + "_" + lastName).toLowerCase().substring(0,7), userName.substring(0,7));
		assertTrue("", !(firstName + "_" + lastName).toLowerCase().equals(userName));
		
		// should generate buffy_summers@sunnydale.com
		generator.setNamePattern("%emailAddress%");
		userName = generator.generateUserName(firstName, lastName, emailAddress, 0);
		assertEquals(emailAddress.toLowerCase(), userName);
		
		// should generate  buffy_s123
		userName = generator.generateUserName(firstName, lastName, emailAddress, 1);
		assertTrue("", !(emailAddress).toLowerCase().equals(userName));
		
		// should generate summers.buffy
		generator.setNamePattern("%lastName%.%firstName%");
		userName = generator.generateUserName(firstName, lastName, emailAddress, 0);
		assertEquals("", (lastName + "." + firstName).toLowerCase(), userName);
		
		// should generate bsummers
		generator.setNamePattern("%i%%lastName%");
		userName = generator.generateUserName(firstName, lastName, emailAddress, 0);
		assertEquals("", ("bsummers").toLowerCase(), userName);
		
	}

	public void testGenerateWhitespaceNames() throws Exception
    {
        NameBasedUserNameGenerator generator = new NameBasedUserNameGenerator();
        generator.setUserNameLength(10);
        generator.setNamePattern("%firstName%_%lastName%");
        String lastName = "bar";
        assertEquals("f_oo_bar", generator.generateUserName(" f oo ", lastName, "", 0));
        assertEquals("f_o_o_bar", generator.generateUserName("f o  o", lastName, "", 0));
        assertEquals("f_o_o_bar", generator.generateUserName("f\to\t o", lastName, "", 0));
        assertEquals("f_o_o_bar", generator.generateUserName("f\no \no", lastName, "", 0));
        assertEquals("f_o_o_bar", generator.generateUserName("f\ro\r\no", lastName, "", 0));
    }
	
	public void testAccentedCharsInNames() throws Exception
    {
        NameBasedUserNameGenerator generator = new NameBasedUserNameGenerator();
        generator.setUserNameLength(10);
        generator.setNamePattern("%firstName%_%lastName%");
        String firstName = "Çüéâäà çêëèï";
        String lastName = "îìÄÅÉæÆô öòû";
        
        String username = generator.generateUserName(firstName, lastName, "", 0);
        assertEquals("cueaaa_ceeei_iiaaeo_oou", username);
    }
}
