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

import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ISOLatin1AccentFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;


public class AlfrescoStandardAnalyser extends Analyzer
{
    private Set stopSet;

    /**
     * An array containing some common English words that are usually not useful for searching.
     */
    public static final String[] STOP_WORDS = StopAnalyzer.ENGLISH_STOP_WORDS;

    /** Builds an analyzer. */
    public AlfrescoStandardAnalyser()
    {
        this(STOP_WORDS);
    }

    /** Builds an analyzer with the given stop words. */
    public AlfrescoStandardAnalyser(String[] stopWords)
    {
        stopSet = StopFilter.makeStopSet(stopWords);
    }

    /**
     * Constructs a {@link StandardTokenizer} filtered by a {@link StandardFilter}, a {@link LowerCaseFilter} and a {@link StopFilter}.
     */
    public TokenStream tokenStream(String fieldName, Reader reader)
    {
        TokenStream result = new StandardTokenizer(reader);
        result = new AlfrescoStandardFilter(result);
        result = new LowerCaseFilter(result);
        result = new StopFilter(result, stopSet);
        result = new ISOLatin1AccentFilter(result);
        return result;
    }
}
