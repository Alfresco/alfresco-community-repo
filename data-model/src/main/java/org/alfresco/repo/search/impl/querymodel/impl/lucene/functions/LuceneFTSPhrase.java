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
package org.alfresco.repo.search.impl.querymodel.impl.lucene.functions;

import java.util.Map;
import java.util.Set;

import org.alfresco.repo.search.adaptor.AnalysisMode;
import org.alfresco.repo.search.adaptor.LuceneFunction;
import org.alfresco.repo.search.adaptor.QueryParserAdaptor;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSPhrase;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilderComponent;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.QueryBuilderContext;

/**
 * @author andyh
 *
 */
public class LuceneFTSPhrase<Q, S, E extends Throwable> extends FTSPhrase implements LuceneQueryBuilderComponent<Q, S, E>
{
    /**
     * 
     */
    public LuceneFTSPhrase()
    {
        super();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryBuilderComponent#addComponent(org.apache.lucene.search.BooleanQuery,
     *      org.apache.lucene.search.BooleanQuery, org.alfresco.service.cmr.dictionary.DictionaryService,
     *      java.lang.String)
     */
    public Q addComponent(Set<String> selectors, Map<String, Argument> functionArgs, QueryBuilderContext<Q, S, E> luceneContext, FunctionEvaluationContext functionContext)
            throws E
    {
        QueryParserAdaptor<Q, S, E> lqpa = luceneContext.getLuceneQueryParserAdaptor();
        Argument argument = functionArgs.get(ARG_PHRASE);
        String term = (String) argument.getValue(functionContext);

        Integer slop = Integer.valueOf(lqpa.getPhraseSlop());
        argument = functionArgs.get(ARG_SLOP);
        if(argument != null)
        { 
           slop = (Integer) argument.getValue(functionContext);
        }
        
        argument = functionArgs.get(ARG_TOKENISATION_MODE);
        AnalysisMode mode = (AnalysisMode) argument.getValue(functionContext);
        
        PropertyArgument propArg = (PropertyArgument) functionArgs.get(ARG_PROPERTY);
        Q query;
        if (propArg != null)
        {
            String prop = propArg.getPropertyName();
            query = lqpa.getFieldQuery(functionContext.getLuceneFieldName(prop), term, mode, slop, LuceneFunction.FIELD);
        }
        else
        {
            query = lqpa.getFieldQuery(lqpa.getField(), term, mode, slop, LuceneFunction.FIELD);
        }
        return query;
    }
}
