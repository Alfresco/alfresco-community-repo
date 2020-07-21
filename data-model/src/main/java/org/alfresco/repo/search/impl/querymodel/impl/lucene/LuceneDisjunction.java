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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.search.adaptor.lucene.LuceneQueryParserExpressionAdaptor;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.impl.BaseDisjunction;
import org.alfresco.util.Pair;

/**
 * @author andyh
 */
public class LuceneDisjunction<Q, S, E extends Throwable> extends BaseDisjunction implements LuceneQueryBuilderComponent<Q, S, E>
{

    /**
     */
    public LuceneDisjunction(List<Constraint> constraints)
    {
        super(constraints);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilderComponent#addComponent(java.lang.String,
     *      java.util.Map, org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilderContext,
     *      org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext)
     */
    public Q addComponent(Set<String> selectors, Map<String, Argument> functionArgs, LuceneQueryBuilderContext<Q, S, E> luceneContext, FunctionEvaluationContext functionContext)
            throws E
    {
        LuceneQueryParserExpressionAdaptor<Q, E> expressionBuilder = luceneContext.getLuceneQueryParserAdaptor().getExpressionAdaptor();
        ArrayList<Pair<Constraint, Q>> queriestoDisjoin = new ArrayList<>();
        for (Constraint constraint : getConstraints())
        {
            if (constraint instanceof LuceneQueryBuilderComponent)
            {
                @SuppressWarnings("unchecked")
                LuceneQueryBuilderComponent<Q, S, E> luceneQueryBuilderComponent = (LuceneQueryBuilderComponent<Q, S, E>) constraint;
                Q constraintQuery = luceneQueryBuilderComponent.addComponent(selectors, functionArgs, luceneContext, functionContext);
                queriestoDisjoin.add(new Pair<Constraint, Q>(constraint, constraintQuery));
                if (constraintQuery != null)
                {
                    switch (constraint.getOccur())
                    {
                    case DEFAULT:
                    case MANDATORY:
                    case OPTIONAL:
                        expressionBuilder.addOptional(constraintQuery, constraint.getBoost());
                        break;
                    case EXCLUDE:
                        LuceneQueryParserExpressionAdaptor<Q, E> subExpressionBuilder = luceneContext.getLuceneQueryParserAdaptor().getExpressionAdaptor();
                        subExpressionBuilder.addRequired(luceneContext.getLuceneQueryParserAdaptor().getMatchAllNodesQuery());
                        subExpressionBuilder.addExcluded(constraintQuery);
                        expressionBuilder.addOptional(subExpressionBuilder.getQuery(),  constraint.getBoost());
                        break;
                    }
                }
            }
            else
            {
                throw new UnsupportedOperationException();
            }
        }
        return expressionBuilder.getQuery();

    }

}
