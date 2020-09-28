/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.util.exec;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Unit test class for {@link ExecParameterTokenizer}.
 *
 * @author Neil Mc Erlean
 * @since 3.4.2
 */
public class ExecParameterTokenizerTest
{
    @Test public void tokenizeEmptyString()
    {
        final String str1 = "";
        final String str2 = "   \t   ";
        
        List<String> expectedTokens = Arrays.asList(new String[0]);
        
        ExecParameterTokenizer t = new ExecParameterTokenizer(str1);
        assertEquals("Wrong tokens", expectedTokens, t.getAllTokens());

        t = new ExecParameterTokenizer(str2);
        assertEquals("Wrong tokens", expectedTokens, t.getAllTokens());
    }

    @Test(expected=NullPointerException.class) public void tokenizeNullString()
    {
        final String str1 = null;
        
        List<String> expectedTokens = Arrays.asList(new String[0]);
        
        ExecParameterTokenizer t = new ExecParameterTokenizer(str1);
        assertEquals("Wrong tokens", expectedTokens, t.getAllTokens());
    }

    @Test public void tokenizeSimpleParameterString()
    {
        final String str1 = "-font Helvetica -pointsize 50";
        final String str2 = "   -font   Helvetica   -pointsize   50   ";
        
        List<String> expectedTokens = Arrays.asList(new String[] {"-font", "Helvetica", "-pointsize", "50"});
        
        ExecParameterTokenizer t = new ExecParameterTokenizer(str1);
        assertEquals("Wrong tokens", expectedTokens, t.getAllTokens());
        
        t = new ExecParameterTokenizer(str2);
        assertEquals("Wrong tokens", expectedTokens, t.getAllTokens());
    }

    @Test public void tokenizeParameterStringEntirelyQuoted()
    {
        final String str1 = "\"circle 100,100 150,150\"";
        final String str2 = "'circle 100,100 150,150'";
        
        List<String> expectedTokens = Arrays.asList(new String[] {"circle 100,100 150,150"});
        
        ExecParameterTokenizer t = new ExecParameterTokenizer(str1);
        assertEquals("Wrong tokens", expectedTokens, t.getAllTokens());

        t = new ExecParameterTokenizer(str2);
        assertEquals("Wrong tokens", expectedTokens, t.getAllTokens());
    }

    @Test(expected=IllegalArgumentException.class)
    public void tokenizeParameterStringWithUnclosedSingleQuote()
    {
        final String str = "-font Helvetica -pointsize 50 -draw 'circle";
        
        ExecParameterTokenizer t = new ExecParameterTokenizer(str);
        t.getAllTokens();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void tokenizeParameterStringWithUnclosedDoubleQuote()
    {
        final String str = "-font Helvetica -pointsize 50 -draw \"circle";
        
        ExecParameterTokenizer t = new ExecParameterTokenizer(str);
        t.getAllTokens();
    }

    @Test(expected=IllegalArgumentException.class)
    public void tokenizeParameterStringWithMalformedQuoteNesting()
    {
        final String str = "  \"foo 'bar baz\" hello'  ";
        
        ExecParameterTokenizer t = new ExecParameterTokenizer(str);
        t.getAllTokens();
    }

    @Test public void tokenizeParameterStringWithQuotedParam()
    {
        final String str1 = "-font Helvetica -pointsize 50 -draw \"circle 100,100 150,150\"";
        final String str2 = "-font Helvetica -pointsize 50 -draw 'circle 100,100 150,150'";
        
        List<String> expectedTokens = Arrays.asList(new String[] {"-font", "Helvetica", "-pointsize", "50",
                                                                  "-draw", "circle 100,100 150,150"});
        
        ExecParameterTokenizer t = new ExecParameterTokenizer(str1);
        assertEquals("Wrong tokens", expectedTokens, t.getAllTokens());

        t = new ExecParameterTokenizer(str2);
        assertEquals("Wrong tokens", expectedTokens, t.getAllTokens());
    }

    @Test public void tokenizeParameterStringWithQuotedParam_MixedQuotes()
    {
        final String str1 = "'Hello world' middle \"Goodbye world\"";
        final String str2 = "\"Hello world\" middle 'Goodbye world'";
        
        List<String> expectedTokens = Arrays.asList(new String[] {"Hello world", "middle", "Goodbye world"});
        
        ExecParameterTokenizer t = new ExecParameterTokenizer(str1);
        assertEquals("Wrong tokens", expectedTokens, t.getAllTokens());

        t = new ExecParameterTokenizer(str2);
        assertEquals("Wrong tokens", expectedTokens, t.getAllTokens());
    }

    @Test public void tokenizeParameterStringWithQuotedParamContainingQuotes()
    {
        final String str1 = "-font Helvetica -pointsize 50 -draw \"gravity south fill black text 0,12 'CopyRight'\"";
        final String str2 = "-font Helvetica -pointsize 50 -draw 'gravity south fill black text 0,12 \"CopyRight\"'";
        
        List<String> expectedTokens1 = Arrays.asList(new String[] {"-font", "Helvetica", "-pointsize", "50",
                                                                   "-draw", "gravity south fill black text 0,12 'CopyRight'"});

        List<String> expectedTokens2 = Arrays.asList(new String[] {"-font", "Helvetica", "-pointsize", "50",
                                                                   "-draw", "gravity south fill black text 0,12 \"CopyRight\""});

        ExecParameterTokenizer t = new ExecParameterTokenizer(str1);
        assertEquals("Wrong tokens", expectedTokens1, t.getAllTokens());

        t = new ExecParameterTokenizer(str2);
        assertEquals("Wrong tokens", expectedTokens2, t.getAllTokens());
    }
}
