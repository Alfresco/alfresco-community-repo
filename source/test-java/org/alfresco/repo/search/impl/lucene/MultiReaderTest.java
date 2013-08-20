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
package org.alfresco.repo.search.impl.lucene;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.TestCase;

public class MultiReaderTest extends TestCase
{

    public MultiReaderTest()
    {
        super();
    }

    public MultiReaderTest(String arg0)
    {
        super(arg0);
    }

    public void testMultiReader_single() throws IOException
    {
        String first = "my first string";
        String second = "another little string";

        StringReader one = new StringReader(first);
        StringReader two = new StringReader(second);

        Reader multiReader = new MultiReader(one, two);
        StringBuilder builder = new StringBuilder();
        int c;
        while ((c = multiReader.read()) != -1)
        {
            builder.append((char) c);
        }
        assertEquals(builder.toString(), first + second);

    }

    public void testMultiReader_bits() throws IOException
    {
        String first = "my first string";
        String second = "another little string";

        StringReader one = new StringReader(first);
        StringReader two = new StringReader(second);

        Reader multiReader = new MultiReader(one, two);
        StringBuilder builder = new StringBuilder();
        for (int chunk = 1; chunk < 100; chunk++)
        {
            char[] c = new char[chunk];
            int i = 0;
            while (i != -1)
            {
                i = multiReader.read(c);
                for (int j = 0; j < i; j++)
                {
                    builder.append(c[j]);
                }
            }
            assertEquals(builder.toString(), first + second);
        }
    }
    
    public void testSkip() throws IOException
    {
        String first = "my first string";
        String second = "another little string";

        StringReader one = new StringReader(first);
        StringReader two = new StringReader(second);

        Reader multiReader = new MultiReader(one, two);
        
        multiReader.skip(3);
        String all = first + second;
        assertEquals((char)multiReader.read(), all.charAt(3));
        
        multiReader.skip(15);
        assertEquals((char)multiReader.read(), all.charAt(3+15+1));
    }

}
