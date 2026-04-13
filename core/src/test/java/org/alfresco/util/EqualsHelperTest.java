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
package org.alfresco.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.util.EqualsHelper.MapValueComparison;

import junit.framework.TestCase;

/**
 * @see EqualsHelper
 * 
 * @author Derek Hulley
 * @since 3.1SP2
 */
public class EqualsHelperTest extends TestCase
{
    private File fileOne;
    private File fileTwo;
    
    @Override
    public void setUp() throws Exception
    {
        fileOne = TempFileProvider.createTempFile(getName(), "-one.txt");
        fileTwo = TempFileProvider.createTempFile(getName(), "-two.txt");
        
        OutputStream osOne = new FileOutputStream(fileOne);
        osOne.write("1234567890 - ONE".getBytes("UTF-8"));
        osOne.close();
        
        OutputStream osTwo = new FileOutputStream(fileTwo);
        osTwo.write("1234567890 - TWO".getBytes("UTF-8"));
        osTwo.close();
    }
    
    public void testStreamsNotEqual() throws Exception
    {
        InputStream isLeft = new FileInputStream(fileOne);
        InputStream isRight = new FileInputStream(fileTwo);
        boolean equal = EqualsHelper.binaryStreamEquals(isLeft, isRight);
        assertFalse("Should not be the same", equal);
    }
    
    public void testStreamsEqual() throws Exception
    {
        InputStream isLeft = new FileInputStream(fileOne);
        InputStream isRight = new FileInputStream(fileOne);
        boolean equal = EqualsHelper.binaryStreamEquals(isLeft, isRight);
        assertTrue("Should be the same", equal);
    }
    
    public void testMapComparison() throws Exception
    {
        Map<Integer, String> left = new HashMap<Integer, String>();
        Map<Integer, String> right = new HashMap<Integer, String>();
        // EQUAL
        left.put(0, "A");
        right.put(0, "A");
        // NOT_EQUAL
        left.put(1, "A");
        right.put(1, "B");
        // EQUAL
        left.put(2, null);
        right.put(2, null);
        // NOT_EQUAL
        left.put(3, null);
        right.put(3, "B");
        // NOT_EQUAL
        left.put(4, "A");
        right.put(4, null);
        // RIGHT_ONLY
        right.put(5, "B");
        // LEFT_ONLY
        left.put(6, "A");
        Map<Integer, MapValueComparison> diff = EqualsHelper.getMapComparison(left, right);
        assertEquals("'EQUAL' check failed", MapValueComparison.EQUAL, diff.get(0));
        assertEquals("'NOT_EQUAL' check failed", MapValueComparison.NOT_EQUAL, diff.get(1));
        assertEquals("'EQUAL' check failed", MapValueComparison.EQUAL, diff.get(2));
        assertEquals("'NOT_EQUAL' check failed", MapValueComparison.NOT_EQUAL, diff.get(3));
        assertEquals("'NOT_EQUAL' check failed", MapValueComparison.NOT_EQUAL, diff.get(4));
        assertEquals("'RIGHT_ONLY' check failed", MapValueComparison.RIGHT_ONLY, diff.get(5));
        assertEquals("'LEFT_ONLY' check failed", MapValueComparison.LEFT_ONLY, diff.get(6));
    }
}
