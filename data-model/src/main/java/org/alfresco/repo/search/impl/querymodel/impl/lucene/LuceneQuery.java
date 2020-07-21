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
package org.alfresco.repo.search.impl.querymodel.impl.lucene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.search.adaptor.lucene.LuceneQueryParserExpressionAdaptor;
import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.Order;
import org.alfresco.repo.search.impl.querymodel.Ordering;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.repo.search.impl.querymodel.Selector;
import org.alfresco.repo.search.impl.querymodel.Source;
import org.alfresco.repo.search.impl.querymodel.impl.BaseQuery;
import org.alfresco.repo.search.impl.querymodel.impl.SimpleConstraint;
import org.alfresco.repo.search.impl.querymodel.impl.functions.PropertyAccessor;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Score;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition.SortType;
import org.alfresco.util.Pair;

/**
 * @author andyh
 */
public class LuceneQuery<Q, S, E extends Throwable> extends BaseQuery implements LuceneQueryBuilder<Q, S, E>
{

    /**
     * @param source Source
     * @param constraint Constraint
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
    public Q buildQuery(Set<String> selectors, LuceneQueryBuilderContext<Q, S, E> luceneContext, FunctionEvaluationContext functionContext) throws E
    {
        LuceneQueryParserExpressionAdaptor<Q, E> expressionBuilder = luceneContext.getLuceneQueryParserAdaptor().getExpressionAdaptor();

        boolean must = false;
        boolean must_not = false;

        ArrayList<Pair<Constraint, Q>> queriestoConjoin = new ArrayList<>();
        
        if (selectors != null)
        {
            for (String selector : selectors)
            {
                Selector current = getSource().getSelector(selector);
                if (current instanceof LuceneQueryBuilderComponent)
                {
                    @SuppressWarnings("unchecked")
                    LuceneQueryBuilderComponent<Q, S, E> luceneQueryBuilderComponent = (LuceneQueryBuilderComponent<Q, S, E>) current;
                    Q selectorQuery = luceneQueryBuilderComponent.addComponent(selectors, null, luceneContext, functionContext);
                    queriestoConjoin.add(new Pair<Constraint, Q>(new SimpleConstraint(org.alfresco.repo.search.impl.querymodel.Constraint.Occur.MANDATORY), selectorQuery));
                    if (selectorQuery != null)
                    {
                        expressionBuilder.addRequired(selectorQuery);
                        must = true;
                    }
                }
                else
                {
                    throw new UnsupportedOperationException();
                }
            }
        }

        Constraint constraint = getConstraint();
        if (constraint != null)
        {
            if (constraint instanceof LuceneQueryBuilderComponent)
            {
                @SuppressWarnings("unchecked")
                LuceneQueryBuilderComponent<Q, S, E> luceneQueryBuilderComponent = (LuceneQueryBuilderComponent<Q, S, E>) constraint;
                Q constraintQuery = luceneQueryBuilderComponent.addComponent(selectors, null, luceneContext, functionContext);
                queriestoConjoin.add(new Pair<Constraint, Q>(constraint, constraintQuery));
                
                if (constraintQuery != null)
                {
                    switch (constraint.getOccur())
                    {
                    case DEFAULT:
                    case MANDATORY:
                        expressionBuilder.addRequired(constraintQuery, constraint.getBoost());
                        must = true;
                        break;
                    case OPTIONAL:
                        expressionBuilder.addOptional(constraintQuery, constraint.getBoost());
                        break;
                    case EXCLUDE:
                        expressionBuilder.addExcluded(constraintQuery, constraint.getBoost());
                        must_not = true;
                        break;
                    }
                }
            }
            else
            {
                throw new UnsupportedOperationException();
            }
        }

        if (!must && must_not)
        {
            expressionBuilder.addRequired(luceneContext.getLuceneQueryParserAdaptor().getMatchAllNodesQuery());
        }

        return expressionBuilder.getQuery();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilder#buildSort(java.lang.String,
     *      org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilderContext,
     *      org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext)
     */
    public S buildSort(Set<String> selectors, LuceneQueryBuilderContext<Q, S, E> luceneContext, FunctionEvaluationContext functionContext) throws E
    {
        if ((getOrderings() == null) || (getOrderings().size() == 0))
        {
            return null;
        }
        
        return luceneContext.getLuceneQueryParserAdaptor().buildSort(getOrderings(), functionContext);
    }
    
    public List<SortDefinition> buildSortDefinitions(Set<String> selectors, LuceneQueryBuilderContext<Q, S, E> luceneContext, FunctionEvaluationContext functionContext)
    {
        if ((getOrderings() == null) || (getOrderings().size() == 0))
        {
            return Collections.<SortDefinition>emptyList();
        }

        ArrayList<SortDefinition> definitions = new ArrayList<SortDefinition>(getOrderings().size());

        for (Ordering ordering : getOrderings())
        {
            if (ordering.getColumn().getFunction().getName().equals(PropertyAccessor.NAME))
            {
                PropertyArgument property = (PropertyArgument) ordering.getColumn().getFunctionArguments().get(PropertyAccessor.ARG_PROPERTY);

                if (property == null)
                {
                    throw new IllegalStateException();
                }

                String propertyName = property.getPropertyName();

                String fieldName = functionContext.getLuceneFieldName(propertyName);
                
                definitions.add(new SortDefinition(SortType.FIELD, fieldName, ordering.getOrder() == Order.ASCENDING));
            }
            else if (ordering.getColumn().getFunction().getName().equals(Score.NAME))
            {
                definitions.add(new SortDefinition(SortType.SCORE, null, ordering.getOrder() == Order.ASCENDING));
            }
        }

        return definitions;
    }
}
