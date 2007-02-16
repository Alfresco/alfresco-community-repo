/*
 * Copyright (C) 2006 Alfresco, Inc.
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
package org.alfresco.service.cmr.repository;

import java.util.Locale;

import junit.framework.TestCase;

/**
 * @see org.alfresco.service.cmr.repository.MLText
 * 
 * @author Derek Hulley
 */
public class MLTextTest extends TestCase
{
    MLText mlText;
    
    @Override
    protected void setUp()
    {
        mlText = new MLText(Locale.CANADA_FRENCH, Locale.CANADA_FRENCH.toString());
        mlText.addValue(Locale.US, Locale.US.toString());
        mlText.addValue(Locale.UK, Locale.UK.toString());
        mlText.addValue(Locale.FRENCH, Locale.FRENCH.toString());
        mlText.addValue(Locale.CHINESE, Locale.CHINESE.toString());
    }

    public void testGetByLocale()
    {
        // check each value
        assertNull("Expected nothing for German", mlText.getValue(Locale.GERMAN));
        assertEquals(Locale.US.toString(), mlText.get(Locale.US));
        assertEquals(Locale.UK.toString(), mlText.get(Locale.UK));
        assertNull("Expected no value for Japanese", mlText.getValue(Locale.JAPANESE));
        assertNotNull("Expected an arbirary value for Japanese", mlText.getClosestValue(Locale.JAPANESE));
    }
}
