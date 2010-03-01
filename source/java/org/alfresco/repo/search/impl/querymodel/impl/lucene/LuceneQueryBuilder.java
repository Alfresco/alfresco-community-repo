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
package org.alfresco.repo.search.impl.querymodel.impl.lucene;

import java.util.Set;

import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;

/**
 * @author andyh
 */
public interface LuceneQueryBuilder
{
    /**
     * Build the matching lucene query
     * @param selectors
     * @param luceneContext
     * @param functionContext
     * @return - the query
     * @throws ParseException
     */
    public  Query buildQuery(Set<String> selectors,  LuceneQueryBuilderContext luceneContext, FunctionEvaluationContext functionContext) throws ParseException;

    /**
     * Build the matching lucene sort
     * @param selectors
     * @param luceneContext
     * @param functionContext
     * @return - the sort spec
     */
    public Sort buildSort(Set<String> selectors, LuceneQueryBuilderContext luceneContext, FunctionEvaluationContext functionContext);
}
