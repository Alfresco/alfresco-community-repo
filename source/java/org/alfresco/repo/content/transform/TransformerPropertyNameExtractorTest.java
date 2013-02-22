/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.content.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test class for some of the regex methods.
 * 
 * @author Alan Davis
 */
public class TransformerPropertyNameExtractorTest
{
    @Test
    public void textSplitExt()
    {
        String[][] values = new String[][]
        {
                {"AAA.BBB", "AAA", "BBB"},
                {"AA\\.BB.CC\\.DD\\.EE", "AA.BB", "CC.DD.EE"},
        };
        
        for (String[] args: values)
        {
            String input = args[0];
            String expectedSource = args[1];
            String expectedTarget = args[2];
            
            String[] sourceTarget = TransformerPropertyNameExtractor.splitExt(input);
            assertEquals("length", sourceTarget.length, 2);
            assertEquals("source", expectedSource, sourceTarget[0]);
            assertEquals("target", expectedTarget, sourceTarget[1]);
        }
    }
    
    @Test
    public void testPattern()
    {
        assertTrue( TransformerPropertyNameExtractor.pattern("ABC").matcher("ABC").matches());
        assertFalse(TransformerPropertyNameExtractor.pattern("ABC").matcher("x").matches());
        assertFalse(TransformerPropertyNameExtractor.pattern("ABC").matcher("ABCD").matches());
        assertFalse(TransformerPropertyNameExtractor.pattern("ABC").matcher("DABC").matches());
        
        assertTrue( TransformerPropertyNameExtractor.pattern("*B").matcher("B").matches());
        assertTrue( TransformerPropertyNameExtractor.pattern("*B").matcher("xxB").matches());
        assertFalse(TransformerPropertyNameExtractor.pattern("*B").matcher("xxBx").matches());
        assertFalse( TransformerPropertyNameExtractor.pattern("B*").matcher("").matches());
        
        assertTrue( TransformerPropertyNameExtractor.pattern("C*").matcher("C").matches());
        assertTrue( TransformerPropertyNameExtractor.pattern("C*").matcher("CxxB").matches());
        assertFalse(TransformerPropertyNameExtractor.pattern("C*").matcher("xxBx").matches());
        
        assertTrue(TransformerPropertyNameExtractor.pattern("D*E*F").matcher("DEF").matches());
        assertTrue(TransformerPropertyNameExtractor.pattern("D*E*F").matcher("DxxExxF").matches());
        assertTrue(TransformerPropertyNameExtractor.pattern("D*E*F").matcher("D*E*F").matches());
        
        assertTrue( TransformerPropertyNameExtractor.pattern("A+").matcher("A+").matches());
        assertFalse(TransformerPropertyNameExtractor.pattern("A+").matcher("AA").matches());
        assertFalse(TransformerPropertyNameExtractor.pattern("A+").matcher("AAA").matches());
        assertFalse(TransformerPropertyNameExtractor.pattern("A+").matcher("A+A").matches());
    }
}
