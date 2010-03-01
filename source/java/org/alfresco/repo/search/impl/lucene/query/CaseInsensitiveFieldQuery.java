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
import org.apache.lucene.search.MultiTermQuery;

/**
 * Perform a case insensitive match against a field
 * 
 * @author andyh
 *
 */
public class CaseInsensitiveFieldQuery extends MultiTermQuery
{
    /**
     * 
     */
    private static final long serialVersionUID = -2570803495329346982L;

    /**
     * @param term - the term for the match
     */
    public CaseInsensitiveFieldQuery(Term term)
    {
        super(term);
    }
    
    @Override
    protected FilteredTermEnum getEnum(IndexReader reader) throws IOException
    {
        Term term = new Term(getTerm().field(), getTerm().text());
        return new CaseInsensitiveTermEnum(reader, term);
    }

}
