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

import java.util.List;

import org.alfresco.repo.search.impl.lucene.ParseException;
import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.Negation;
import org.alfresco.repo.search.impl.querymodel.Order;
import org.alfresco.repo.search.impl.querymodel.Ordering;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.repo.search.impl.querymodel.Selector;
import org.alfresco.repo.search.impl.querymodel.Source;
import org.alfresco.repo.search.impl.querymodel.impl.BaseQuery;
import org.alfresco.repo.search.impl.querymodel.impl.functions.PropertyAccessor;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Score;
import org.alfresco.service.namespace.QName;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.BooleanClause.Occur;

/**
 * @author andyh
 */
public class LuceneQuery extends BaseQuery implements LuceneQueryBuilder
{

    /**
     * @param columns
     * @param source
     * @param constraint
     * @param orderings
     */
    public LuceneQuery(List<Column> columns, Source source, Constraint constraint, List<Ordering> orderings)
    {
        super(columns, source, constraint, orderings);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilder#buildQuery()
     */
    public Query buildQuery(String selectorName, LuceneQueryBuilderContext luceneContext, FunctionEvaluationContext functionContext) throws ParseException
    {

        BooleanQuery luceneQuery = new BooleanQuery();
        Selector selector = getSource().getSelector(selectorName);
        if (selector != null)
        {
            if (selector instanceof LuceneQueryBuilderComponent)
            {
                LuceneQueryBuilderComponent luceneQueryBuilderComponent = (LuceneQueryBuilderComponent) selector;
                Query selectorQuery = luceneQueryBuilderComponent.addComponent(selectorName, null, luceneContext, functionContext);
                if (selectorQuery != null)
                {
                    luceneQuery.add(selectorQuery, Occur.MUST);
                }
            }
            else
            {
                throw new UnsupportedOperationException();
            }
        }
        else
        {
            throw new UnsupportedOperationException();
        }

        Constraint constraint = getConstraint();
        if (constraint != null)
        {
            if (constraint instanceof LuceneQueryBuilderComponent)
            {
                LuceneQueryBuilderComponent luceneQueryBuilderComponent = (LuceneQueryBuilderComponent) constraint;
                Query constraintQuery = luceneQueryBuilderComponent.addComponent(selectorName, null, luceneContext, functionContext);
                if (constraintQuery != null)
                {
                    if (constraint instanceof Negation)
                    {
                        luceneQuery.add(constraintQuery, Occur.MUST_NOT);
                    }
                    else
                    {
                        luceneQuery.add(constraintQuery, Occur.MUST);
                    }
                }
                else
                {
                    throw new UnsupportedOperationException();
                }
            }
            else
            {
                throw new UnsupportedOperationException();
            }
        }

        return luceneQuery;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilder#buildSort(java.lang.String,
     *      org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilderContext,
     *      org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext)
     */
    public Sort buildSort(String selectorName, LuceneQueryBuilderContext luceneContext, FunctionEvaluationContext functionContext)
    {
        if ((getOrderings() == null) || (getOrderings().size() == 0))
        {
            return null;
        }

        int index = 0;
        SortField[] fields = new SortField[getOrderings().size()];

        for (Ordering ordering : getOrderings())
        {
            if (ordering.getColumn().getFunction().getName().equals(PropertyAccessor.NAME))
            {
                PropertyArgument property = (PropertyArgument) ordering.getColumn().getFunctionArguments().get(PropertyAccessor.ARG_PROPERTY);

                if (property == null)
                {
                    throw new IllegalStateException();
                }

                QName propertyQName = property.getPropertyName();

                String luceneField = functionContext.getLuceneSortField(propertyQName);

                if (luceneField != null)
                {
                    fields[index++] = new SortField(luceneField, (ordering.getOrder() == Order.DESCENDING));
                }
                else
                {
                    throw new IllegalStateException();
                }
            }
            else if(ordering.getColumn().getFunction().getName().equals(Score.NAME))
            {
                fields[index++] = new SortField(null, SortField.SCORE, !(ordering.getOrder() == Order.DESCENDING));
            }
            else
            {
                throw new IllegalStateException();
            }

        }

        return new Sort(fields);
    }

}
