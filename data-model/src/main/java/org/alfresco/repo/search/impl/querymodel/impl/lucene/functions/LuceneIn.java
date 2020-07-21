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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.search.adaptor.lucene.LuceneQueryParserAdaptor;
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

/**
 * @author andyh
 */
public class LuceneIn<Q, S, E extends Throwable> extends In implements LuceneQueryBuilderComponent<Q, S, E>
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
    public Q addComponent(Set<String> selectors, Map<String, Argument> functionArgs, LuceneQueryBuilderContext<Q, S, E> luceneContext, FunctionEvaluationContext functionContext)
            throws E
    {
        LuceneQueryParserAdaptor<Q, S, E> lqpa = luceneContext.getLuceneQueryParserAdaptor();
        PropertyArgument propertyArgument = (PropertyArgument) functionArgs.get(ARG_PROPERTY);
        Argument inverseArgument = functionArgs.get(ARG_NOT);
        Boolean not = DefaultTypeConverter.INSTANCE.convert(Boolean.class, inverseArgument.getValue(functionContext));
        LiteralArgument modeArgument = (LiteralArgument) functionArgs.get(ARG_MODE);
        String modeString = DefaultTypeConverter.INSTANCE.convert(String.class, modeArgument.getValue(functionContext));
        PredicateMode mode = PredicateMode.valueOf(modeString);

        ListArgument listArgument = (ListArgument) functionArgs.get(ARG_LIST);
        Collection<Serializable> collection = (Collection<Serializable>) listArgument.getValue(functionContext);

        Q query = functionContext.buildLuceneIn(lqpa, propertyArgument.getPropertyName(), collection, not, mode);

        if (query == null)
        {
            throw new QueryModelException("No query time mapping for property  " + propertyArgument.getPropertyName() + ", it should not be allowed in predicates");
        }

        return query;
    }
}
