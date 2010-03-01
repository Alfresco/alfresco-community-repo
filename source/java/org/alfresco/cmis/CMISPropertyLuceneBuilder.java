/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.cmis;

import java.io.Serializable;
import java.util.Collection;

import org.alfresco.repo.search.impl.lucene.LuceneFunction;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.search.impl.querymodel.PredicateMode;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;

/**
 * CMIS Property Lucene Builder
 * 
 * @author andyh
 */
public interface CMISPropertyLuceneBuilder
{
    /**
     * @param lqp
     * @param value
     * @param mode
     * @param luceneFunction 
     * @return the query
     * @throws ParseException
     */
    public Query buildLuceneEquality(LuceneQueryParser lqp, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws ParseException;

    /**
     * @param lqp
     * @param not
     * @return the query
     * @throws ParseException
     */
    public Query buildLuceneExists(LuceneQueryParser lqp, Boolean not) throws ParseException;

    /**
     * @param lqp
     * @param value
     * @param mode
     * @param luceneFunction 
     * @return the query
     * @throws ParseException 
     */
    public Query buildLuceneGreaterThan(LuceneQueryParser lqp, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws ParseException;

    /**
     * @param lqp
     * @param value
     * @param mode
     * @param luceneFunction 
     * @return the query
     * @throws ParseException 
     */
    public Query buildLuceneGreaterThanOrEquals(LuceneQueryParser lqp, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws ParseException;

    /**
     * @param lqp
     * @param values
     * @param not
     * @param mode
     * @return the query
     * @throws ParseException 
     */
    public Query buildLuceneIn(LuceneQueryParser lqp, Collection<Serializable> values, Boolean not, PredicateMode mode) throws ParseException;

    /**
     * @param lqp
     * @param value
     * @param mode
     * @param luceneFunction 
     * @return the query
     * @throws ParseException 
     */
    public Query buildLuceneInequality(LuceneQueryParser lqp, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws ParseException;

    /**
     * @param lqp
     * @param value
     * @param mode
     * @param luceneFunction 
     * @return the query
     * @throws ParseException 
     */
    public Query buildLuceneLessThan(LuceneQueryParser lqp, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws ParseException;

    /**
     * @param lqp
     * @param value
     * @param mode
     * @param luceneFunction 
     * @return the query
     * @throws ParseException 
     */
    public Query buildLuceneLessThanOrEquals(LuceneQueryParser lqp, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws ParseException;

    /**
     * @param lqp
     * @param value
     * @param not
     * @return the query
     * @throws ParseException 
     */
    public Query buildLuceneLike(LuceneQueryParser lqp, Serializable value, Boolean not) throws ParseException;

    /**
     * @param lqp TODO
     * @return the sort field
     */
    public String getLuceneSortField(LuceneQueryParser lqp);
    
    /**
     * @return the field name
     * 
     */
    public String getLuceneFieldName();
}
