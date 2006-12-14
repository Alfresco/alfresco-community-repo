/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
