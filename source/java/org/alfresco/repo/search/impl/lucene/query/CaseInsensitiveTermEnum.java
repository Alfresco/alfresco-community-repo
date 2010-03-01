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
package org.alfresco.repo.search.impl.lucene.query;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FilteredTermEnum;

/**
 * A term enum to find case insensitive matches - used for Upper and Lower
 * 
 * @author andyh
 */
public class CaseInsensitiveTermEnum extends FilteredTermEnum
{
    private String field = "";

    private boolean endEnum = false;

    private String text;

    /**
     * @param reader =
     *            the index reader
     * @param term -
     *            the term to match
     * @throws IOException
     */
    public CaseInsensitiveTermEnum(IndexReader reader, Term term) throws IOException
    {
        super();
        field = term.field();
        text = term.text();
        // position at the start - we could do slightly better
        setEnum(reader.terms(new Term(term.field(), "")));
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
        if (field.equals(term.field()))
        {
            String searchText = term.text();
            return searchText.equalsIgnoreCase(text);
        }
        endEnum = true;
        return false;
    }

}
