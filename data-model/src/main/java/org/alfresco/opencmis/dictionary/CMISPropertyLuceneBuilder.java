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
package org.alfresco.opencmis.dictionary;

import java.io.Serializable;
import java.util.Collection;

import org.alfresco.repo.search.adaptor.lucene.LuceneFunction;
import org.alfresco.repo.search.adaptor.lucene.LuceneQueryParserAdaptor;
import org.alfresco.repo.search.impl.querymodel.PredicateMode;


/**
 * Encapsulate the building of lucene queries for property predicates
 */
public interface CMISPropertyLuceneBuilder
{
    /**
     * @param value Serializable
     * @param mode PredicateMode
     * @param luceneFunction LuceneFunction
     * @return the query - may be null if no query is required
     * @throws E
     */
    public <Q, S, E extends Throwable> Q buildLuceneEquality(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E;

    /**
     * @param not Boolean
     * @return the query - may be null if no query is required
     * @throws E
     */
    public <Q, S, E extends Throwable> Q buildLuceneExists(LuceneQueryParserAdaptor<Q, S, E> lqpa, Boolean not) throws E;

    /**
     * @param value Serializable
     * @param mode PredicateMode
     * @param luceneFunction LuceneFunction
     * @return the query - may be null if no query is required
     * @throws E
     */
    public <Q, S, E extends Throwable> Q buildLuceneGreaterThan(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E;

    /**
     * @param value Serializable
     * @param mode PredicateMode
     * @param luceneFunction LuceneFunction
     * @return the query - may be null if no query is required
     * @throws E
     */
    public <Q, S, E extends Throwable> Q buildLuceneGreaterThanOrEquals(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E;

    /**
     * @param not Boolean
     * @param mode PredicateMode
     * @return the query - may be null if no query is required
     * @throws E
     */
    public <Q, S, E extends Throwable> Q buildLuceneIn(LuceneQueryParserAdaptor<Q, S, E> lqpa, Collection<Serializable> values, Boolean not, PredicateMode mode) throws E;

    /**
     * @param value PredicateMode
     * @param mode PredicateMode
     * @param luceneFunction LuceneFunction
     * @return the query - may be null if no query is required
     * @throws E
     */
    public <Q, S, E extends Throwable> Q buildLuceneInequality(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E;

    /**
     * @param value Serializable
     * @param mode PredicateMode
     * @param luceneFunction LuceneFunction
     * @return the query - may be null if no query is required
     * @throws E
     */
    public <Q, S, E extends Throwable> Q buildLuceneLessThan(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E;

    /**
     * @param value Serializable
     * @param mode PredicateMode
     * @param luceneFunction LuceneFunction
     * @return the query - may be null if no query is required
     * @throws E
     */
    public <Q, S, E extends Throwable> Q buildLuceneLessThanOrEquals(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E;

    /**
     * @param value Serializable
     * @param not Boolean
     * @return the query - may be null if no query is required
     * @throws E
     */
    public <Q, S, E extends Throwable> Q buildLuceneLike(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, Boolean not) throws E;

    /**
     * @param lqpa TODO
     * @return the sort field
     * @throws E 
     */
    public <Q, S, E extends Throwable> String getLuceneSortField(LuceneQueryParserAdaptor<Q, S, E> lqpa) throws E;
    
    /**
     * @return the field name
     * 
     */
    public String getLuceneFieldName();
}
