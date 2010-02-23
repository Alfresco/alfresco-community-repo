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
package org.alfresco.repo.search.impl.querymodel.impl.lucene.functions;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.ListArgument;
import org.alfresco.repo.search.impl.querymodel.LiteralArgument;
import org.alfresco.repo.search.impl.querymodel.PredicateMode;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.search.impl.querymodel.impl.functions.In;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilderComponent;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilderContext;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;

/**
 * @author andyh
 */
public class LuceneIn extends In implements LuceneQueryBuilderComponent
{
    /**
     * 
     */
    public LuceneIn()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilderComponent#addComponent(org.apache.lucene
     * .search.BooleanQuery, org.apache.lucene.search.BooleanQuery,
     * org.alfresco.service.cmr.dictionary.DictionaryService, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query addComponent(Set<String> selectors, Map<String, Argument> functionArgs, LuceneQueryBuilderContext luceneContext, FunctionEvaluationContext functionContext)
            throws ParseException
    {
        LuceneQueryParser lqp = luceneContext.getLuceneQueryParser();
        PropertyArgument propertyArgument = (PropertyArgument) functionArgs.get(ARG_PROPERTY);
        Argument inverseArgument = functionArgs.get(ARG_NOT);
        Boolean not = DefaultTypeConverter.INSTANCE.convert(Boolean.class, inverseArgument.getValue(functionContext));
        LiteralArgument modeArgument = (LiteralArgument) functionArgs.get(ARG_MODE);
        String modeString = DefaultTypeConverter.INSTANCE.convert(String.class, modeArgument.getValue(functionContext));
        PredicateMode mode = PredicateMode.valueOf(modeString);

        ListArgument listArgument = (ListArgument) functionArgs.get(ARG_LIST);
        Collection<Serializable> collection = (Collection<Serializable>) listArgument.getValue(functionContext);

        Query query = functionContext.buildLuceneIn(lqp, propertyArgument.getPropertyName(), collection, not, mode);

        if (query == null)
        {
            throw new QueryModelException("No query time mapping for property  " + propertyArgument.getPropertyName() + ", it should not be allowed in predicates");
        }

        return query;
    }
}
