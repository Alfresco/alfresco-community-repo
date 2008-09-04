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
package org.alfresco.repo.search.impl.querymodel.impl.lucene;

import java.util.Collection;

import org.alfresco.repo.search.impl.querymodel.impl.BaseSelector;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

/**
 * @author andyh
 *
 */
public class LuceneSelector extends BaseSelector implements LuceneQueryBuilderComponent
{

    /**
     * @param alias
     * @param type
     */
    public LuceneSelector(QName type, String alias)
    {
        super(type, alias);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilderComponent#addComponent(org.apache.lucene.search.BooleanQuery, org.apache.lucene.search.BooleanQuery)
     */
    public BooleanQuery addComponent(BooleanQuery base, BooleanQuery current, DictionaryService dictionaryService)
    {
        Collection<QName> subclasses = dictionaryService.getSubTypes(getType(), true);
        BooleanQuery booleanQuery = new BooleanQuery();
        for (QName qname : subclasses)
        {
            TermQuery termQuery = new TermQuery(new Term("TYPE", qname.toString()));
            if (termQuery != null)
            {
                booleanQuery.add(termQuery, Occur.SHOULD);
            }
        }
        base.add(booleanQuery, Occur.MUST);
        return current;
    }

    
}
