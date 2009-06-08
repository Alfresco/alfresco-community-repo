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
import org.apache.lucene.search.MultiTermQuery;

/**
 * Find terms that match a range ignoring case
 * 
 * @author andyh
 */
public class CaseInsensitiveFieldRangeQuery extends MultiTermQuery
{
    /**
     * 
     */
    private static final long serialVersionUID = -5859977841901861122L;

    String expandedFieldName;

    String lowerTermText;

    String upperTermText;

    boolean includeLower;

    boolean includeUpper;

    /**
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
     */
    public CaseInsensitiveFieldRangeQuery(String expandedFieldName, String lowerTermText, String upperTermText, boolean includeLower, boolean includeUpper)
    {
        super(new Term(expandedFieldName, ""));
        this.expandedFieldName = expandedFieldName;
        this.lowerTermText = lowerTermText;
        this.upperTermText = upperTermText;
        this.includeLower = includeLower;
        this.includeUpper = includeUpper;
    }

    @Override
    protected FilteredTermEnum getEnum(IndexReader reader) throws IOException
    {
        return new CaseInsensitiveTermRangeEnum(reader, expandedFieldName, lowerTermText, upperTermText, includeLower, includeUpper);
    }

}
