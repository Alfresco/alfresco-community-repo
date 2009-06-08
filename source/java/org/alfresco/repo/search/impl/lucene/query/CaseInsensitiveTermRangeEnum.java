/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.search.impl.lucene.query;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FilteredTermEnum;

/**
 * A term enum that finds terms that lie with in some range ignoring case
 * 
 * @author andyh
 */
public class CaseInsensitiveTermRangeEnum extends FilteredTermEnum
{

    private boolean endEnum = false;

    String expandedFieldName;

    String lowerTermText;

    String upperTermText;

    boolean includeLower;

    boolean includeUpper;

    /**
     * @param reader
     *            the index reader
     * @param expandedFieldName -
     *            field
     * @param lowerTermText -
     *            upper range value
     * @param upperTermText -
     *            lower range value
     * @param includeLower -
     *            include the lower value
     * @param includeUpper -
     *            include the upper value
     * @throws IOException
     */
    public CaseInsensitiveTermRangeEnum(IndexReader reader, String expandedFieldName, String lowerTermText, String upperTermText, boolean includeLower, boolean includeUpper)
            throws IOException
    {
        super();
        this.expandedFieldName = expandedFieldName;
        this.lowerTermText = lowerTermText.toLowerCase();
        this.upperTermText = upperTermText.toLowerCase();
        this.includeLower = includeLower;
        this.includeUpper = includeUpper;

        setEnum(reader.terms(new Term(expandedFieldName, "")));
    }

    @Override
    public float difference()
    {
        return 1.0f;
    }

    @Override
    protected boolean endEnum()
    {
        return endEnum;
    }

    @Override
    protected boolean termCompare(Term term)
    {
        if (expandedFieldName.equals(term.field()))
        {
            String searchText = term.text().toLowerCase();
            return checkLower(searchText) && checkUpper(searchText);
        }
        endEnum = true;
        return false;
    }

    private boolean checkLower(String searchText)
    {
        if (includeLower)
        {
            return (lowerTermText.compareTo(searchText) <= 0);
        }
        else
        {
            return (lowerTermText.compareTo(searchText) < 0);
        }
    }

    private boolean checkUpper(String searchText)
    {
        if (includeUpper)
        {
            return (upperTermText.compareTo(searchText) >= 0);
        }
        else
        {
            return (upperTermText.compareTo(searchText) > 0);
        }
    }

}
