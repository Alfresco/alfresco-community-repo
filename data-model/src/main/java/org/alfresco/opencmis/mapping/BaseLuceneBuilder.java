/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.opencmis.mapping;

import java.io.Serializable;
import java.util.Collection;

import org.alfresco.opencmis.dictionary.CMISPropertyLuceneBuilder;
import org.alfresco.repo.search.adaptor.LuceneFunction;
import org.alfresco.repo.search.adaptor.QueryParserAdaptor;
import org.alfresco.repo.search.adaptor.QueryParserExpressionAdaptor;
import org.alfresco.repo.search.impl.querymodel.PredicateMode;

/**
 * Base class for all property lucene builders
 * 
 * @author davidc
 */
public class BaseLuceneBuilder implements CMISPropertyLuceneBuilder
{
    /**
     * Construct
     * 
     */
    protected BaseLuceneBuilder()
    {
    }

    @Override
    public <Q, S, E extends Throwable> Q buildLuceneEquality(QueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public <Q, S, E extends Throwable> Q buildLuceneExists(QueryParserAdaptor<Q, S, E> lqpa, Boolean not) throws E
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public <Q, S, E extends Throwable> Q buildLuceneGreaterThan(QueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public <Q, S, E extends Throwable> Q buildLuceneGreaterThanOrEquals(QueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public <Q, S, E extends Throwable> Q buildLuceneIn(QueryParserAdaptor<Q, S, E> lqpa, Collection<Serializable> values, Boolean not, PredicateMode mode) throws E
    {
        QueryParserExpressionAdaptor<Q, E> expressionAdaptor = lqpa.getExpressionAdaptor();
        for(Serializable value : values)
        {
            expressionAdaptor.addOptional(buildLuceneEquality(lqpa, value, mode, LuceneFunction.FIELD));
        }
        if(not)
        {
            return expressionAdaptor.getNegatedQuery();
        }
        else
        {
            return expressionAdaptor.getQuery();
        }
    }

    @Override
    public <Q, S, E extends Throwable> Q buildLuceneInequality(QueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        return lqpa.getNegatedQuery(buildLuceneEquality(lqpa, value, mode, luceneFunction));
    }

    @Override
    public <Q, S, E extends Throwable> Q buildLuceneLessThan(QueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public <Q, S, E extends Throwable> Q buildLuceneLessThanOrEquals(QueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public <Q, S, E extends Throwable> Q buildLuceneLike(QueryParserAdaptor<Q, S, E> lqpa, Serializable value, Boolean not) throws E
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLuceneFieldName()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public <Q, S, E extends Throwable> String getLuceneSortField(QueryParserAdaptor<Q, S, E> lqpa) throws E
    {
        throw new UnsupportedOperationException();
    }
}
