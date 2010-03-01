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
