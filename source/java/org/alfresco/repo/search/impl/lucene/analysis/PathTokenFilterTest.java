/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.search.impl.lucene.analysis;

import java.io.IOException;
import java.io.StringReader;

import junit.framework.TestCase;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

public class PathTokenFilterTest extends TestCase
{

    public PathTokenFilterTest()
    {
        super();
    }

    public PathTokenFilterTest(String arg0)
    {
        super(arg0);
    }

    
    public void testFullPath() throws IOException
    {
        tokenise("{uri1}one", new String[]{"uri1", "one"});
        tokenise("/{uri1}one", new String[]{"uri1", "one"});
        tokenise("{uri1}one/{uri2}two/", new String[]{"uri1", "one", "uri2", "two"});
        tokenise("/{uri1}one/{uri2}two/", new String[]{"uri1", "one", "uri2", "two"});
        tokenise("{uri1}one/{uri2}two/{uri3}three", new String[]{"uri1", "one", "uri2", "two", "uri3", "three"});
        tokenise("/{uri1}one/{uri2}two/{uri3}three", new String[]{"uri1", "one", "uri2", "two", "uri3", "three"});
        try
        {
           tokenise("{uri1}one;{uri2}two/", new String[]{"uri1", "one", "uri2", "two"});
        }
        catch(IllegalStateException ise)
        {
            
        }
       
    }
    
    
    public void testPrefixPath() throws IOException
    {
        tokenise("uri1:one", new String[]{"uri1", "one"});
        tokenise("/uri1:one", new String[]{"uri1", "one"});
        tokenise("uri1:one/uri2:two/", new String[]{"uri1", "one", "uri2", "two"});
        tokenise("/uri1:one/uri2:two/", new String[]{"uri1", "one", "uri2", "two"});
        tokenise("uri1:one/uri2:two/uri3:three", new String[]{"uri1", "one", "uri2", "two", "uri3", "three"});
        tokenise("/uri1:one/uri2:two/uri3:three", new String[]{"uri1", "one", "uri2", "two", "uri3", "three"});
        try
        {
           tokenise("{uri1}one;{uri2}two/", new String[]{"uri1", "one", "uri2", "two"});
        }
        catch(IllegalStateException ise)
        {
            
        }
       
    }
    
    
    public void testMixedPath() throws IOException
    {
     
        tokenise("{uri1}one/uri2:two/", new String[]{"uri1", "one", "uri2", "two"});
        tokenise("/{uri1}one/uri2:two/", new String[]{"uri1", "one", "uri2", "two"});
        tokenise("uri1:one/{uri2}two/uri3:three", new String[]{"uri1", "one", "uri2", "two", "uri3", "three"});
        tokenise("/uri1:one/{uri2}two/uri3:three", new String[]{"uri1", "one", "uri2", "two", "uri3", "three"});
        try
        {
           tokenise("{uri1}one;{uri2}two/", new String[]{"uri1", "one", "uri2", "two"});
        }
        catch(IllegalStateException ise)
        {
            
        }
       
    }
    
    
    private void tokenise(String path, String[] tokens) throws IOException
    {
        StringReader reader = new StringReader(path);
        TokenStream ts = new PathTokenFilter(reader, PathTokenFilter.PATH_SEPARATOR,
                PathTokenFilter.SEPARATOR_TOKEN_TEXT, PathTokenFilter.NO_NS_TOKEN_TEXT,
                PathTokenFilter.NAMESPACE_START_DELIMITER, PathTokenFilter.NAMESPACE_END_DELIMITER, true);
       Token t;
       int i = 0;
       while( (t = ts.next()) != null)
       {
           if(t.type().equals(PathTokenFilter.TOKEN_TYPE_PATH_ELEMENT_NAMESPACE))
           {
               assert(i % 2 == 0);
               assertEquals(t.termText(), tokens[i++]);
           }
           else if(t.type().equals(PathTokenFilter.TOKEN_TYPE_PATH_ELEMENT_NAMESPACE_PREFIX))
           {
               assert(i % 2 == 0);
               assertEquals(t.termText(), tokens[i++]);
           }
           else if(t.type().equals(PathTokenFilter.TOKEN_TYPE_PATH_ELEMENT_NAME))
           {
               assert(i % 2 == 1);
               assertEquals(t.termText(), tokens[i++]);
           }
       }
       if(i != tokens.length)
       {
           fail("Invalid number of tokens, found "+i+" and expected "+tokens.length);
       }
    }
}
