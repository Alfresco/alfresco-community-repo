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
