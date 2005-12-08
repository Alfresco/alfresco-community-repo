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

import java.io.Reader;

import org.apache.lucene.analysis.CharTokenizer;

/**
 * @author andyh
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class PathTokeniser extends CharTokenizer
{
    public PathTokeniser(Reader in)
    {
        super(in);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.lucene.analysis.CharTokenizer#isTokenChar(char)
     */
    protected boolean isTokenChar(char c)
    {
        return (c != '/') && !Character.isWhitespace(c);
    }

}
