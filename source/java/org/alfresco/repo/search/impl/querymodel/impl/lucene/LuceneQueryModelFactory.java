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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.Function;
import org.alfresco.repo.search.impl.querymodel.FunctionArgument;
import org.alfresco.repo.search.impl.querymodel.Join;
import org.alfresco.repo.search.impl.querymodel.JoinType;
import org.alfresco.repo.search.impl.querymodel.ListArgument;
import org.alfresco.repo.search.impl.querymodel.LiteralArgument;
import org.alfresco.repo.search.impl.querymodel.Order;
import org.alfresco.repo.search.impl.querymodel.Ordering;
import org.alfresco.repo.search.impl.querymodel.ParameterArgument;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.repo.search.impl.querymodel.Query;
import org.alfresco.repo.search.impl.querymodel.QueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.Selector;
import org.alfresco.repo.search.impl.querymodel.SelectorArgument;
import org.alfresco.repo.search.impl.querymodel.Source;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Child;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Descendant;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Equals;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Exists;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSFuzzyTerm;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSPhrase;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSPrefixTerm;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSProximity;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSRange;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSTerm;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSWildTerm;
import org.alfresco.repo.search.impl.querymodel.impl.functions.GreaterThan;
import org.alfresco.repo.search.impl.querymodel.impl.functions.GreaterThanOrEquals;
import org.alfresco.repo.search.impl.querymodel.impl.functions.In;
import org.alfresco.repo.search.impl.querymodel.impl.functions.LessThan;
import org.alfresco.repo.search.impl.querymodel.impl.functions.LessThanOrEquals;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Like;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Lower;
import org.alfresco.repo.search.impl.querymodel.impl.functions.NotEquals;
import org.alfresco.repo.search.impl.querymodel.impl.functions.PropertyAccessor;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Score;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Upper;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LuceneChild;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LuceneDescendant;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LuceneEquals;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LuceneExists;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LuceneFTSFuzzyTerm;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LuceneFTSPhrase;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LuceneFTSPrefixTerm;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LuceneFTSProximity;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LuceneFTSRange;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LuceneFTSTerm;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LuceneFTSWildTerm;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LuceneGreaterThan;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LuceneGreaterThanOrEquals;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LuceneIn;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LuceneLessThan;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LuceneLessThanOrEquals;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LuceneLike;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LuceneLower;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LuceneNotEquals;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LucenePropertyAccessor;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LuceneScore;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.functions.LuceneUpper;
import org.alfresco.service.namespace.QName;

/**
 * @author andyh
 */
public class LuceneQueryModelFactory implements QueryModelFactory
{
    private HashMap<String, Function> functions = new HashMap<String, Function>();

    /**
     * Default lucene query model factory and functions
     */
    public LuceneQueryModelFactory()
    {
        functions.put(Equals.NAME, new LuceneEquals());
        functions.put(PropertyAccessor.NAME, new LucenePropertyAccessor());
        functions.put(Score.NAME, new LuceneScore());
        functions.put(Upper.NAME, new LuceneUpper());
        functions.put(Lower.NAME, new LuceneLower());

        functions.put(NotEquals.NAME, new LuceneNotEquals());
        functions.put(LessThan.NAME, new LuceneLessThan());
        functions.put(LessThanOrEquals.NAME, new LuceneLessThanOrEquals());
        functions.put(GreaterThan.NAME, new LuceneGreaterThan());
        functions.put(GreaterThanOrEquals.NAME, new LuceneGreaterThanOrEquals());

        functions.put(In.NAME, new LuceneIn());
        functions.put(Like.NAME, new LuceneLike());
        functions.put(Exists.NAME, new LuceneExists());

        functions.put(Child.NAME, new LuceneChild());
        functions.put(Descendant.NAME, new LuceneDescendant());

        functions.put(FTSTerm.NAME, new LuceneFTSTerm());
        functions.put(FTSPhrase.NAME, new LuceneFTSPhrase());
        functions.put(FTSProximity.NAME, new LuceneFTSProximity());
        functions.put(FTSRange.NAME, new LuceneFTSRange());
        functions.put(FTSPrefixTerm.NAME, new LuceneFTSPrefixTerm());
        functions.put(FTSWildTerm.NAME, new LuceneFTSWildTerm());
        functions.put(FTSFuzzyTerm.NAME, new LuceneFTSFuzzyTerm());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createColumn(org.alfresco.repo.search.impl.querymodel.Function,
     *      java.util.List, java.lang.String)
     */
    public Column createColumn(Function function, Map<String, Argument> functionArguments, String alias)
    {
        return new LuceneColumn(function, functionArguments, alias);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createConjunction(java.util.List)
     */
    public Constraint createConjunction(List<Constraint> constraints)
    {
        return new LuceneConjunction(constraints);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createDisjunction(java.util.List)
     */
    public Constraint createDisjunction(List<Constraint> constraints)
    {
        return new LuceneDisjunction(constraints);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createFunctionalConstraint(org.alfresco.repo.search.impl.querymodel.Function,
     *      java.util.List)
     */
    public Constraint createFunctionalConstraint(Function function, Map<String, Argument> functionArguments)
    {
        return new LuceneFunctionalConstraint(function, functionArguments);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createJoin(org.alfresco.repo.search.impl.querymodel.Source,
     *      org.alfresco.repo.search.impl.querymodel.Source, org.alfresco.repo.search.impl.querymodel.JoinType,
     *      org.alfresco.repo.search.impl.querymodel.Constraint)
     */
    public Join createJoin(Source left, Source right, JoinType joinType, Constraint joinCondition)
    {
        return new LuceneJoin(left, right, joinType, joinCondition);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createLiteralArgument(java.lang.String,
     *      org.alfresco.service.namespace.QName, java.io.Serializable)
     */
    public LiteralArgument createLiteralArgument(String name, QName type, Serializable value)
    {
        return new LuceneLiteralArgument(name, type, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createOrdering(org.alfresco.repo.search.impl.querymodel.DynamicArgument,
     *      org.alfresco.repo.search.impl.querymodel.Order)
     */
    public Ordering createOrdering(Column column, Order order)
    {
        return new LuceneOrdering(column, order);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createParameterArgument(java.lang.String,
     *      java.lang.String)
     */
    public ParameterArgument createParameterArgument(String name, String parameterName)
    {
        return new LuceneParameterArgument(name, parameterName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createPropertyArgument(java.lang.String,
     *      org.alfresco.service.namespace.QName)
     */
    public PropertyArgument createPropertyArgument(String name, boolean queryable, boolean orderable, String selector, String propertyName)
    {
        return new LucenePropertyArgument(name, queryable, orderable, selector, propertyName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createQuery(java.util.List,
     *      org.alfresco.repo.search.impl.querymodel.Source, org.alfresco.repo.search.impl.querymodel.Constraint,
     *      java.util.List)
     */
    public Query createQuery(List<Column> columns, Source source, Constraint constraint, List<Ordering> orderings)
    {
        return new LuceneQuery(columns, source, constraint, orderings);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createSelector(org.alfresco.service.namespace.QName,
     *      java.lang.String)
     */
    public Selector createSelector(QName classQName, String alias)
    {
        return new LuceneSelector(classQName, alias);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#getFunction(java.lang.String)
     */
    public Function getFunction(String functionName)
    {
        Function function = functions.get(functionName);
        if (function != null)
        {
            return function;
        }
        else
        {
            // scan
            for (String key : functions.keySet())
            {
                if (key.equalsIgnoreCase(functionName))
                {
                    return functions.get(key);
                }
            }
           return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createSelectorArgument(java.lang.String,
     *      java.lang.String)
     */
    public SelectorArgument createSelectorArgument(String name, String selectorAlias)
    {
        return new LuceneSelectorArgument(name, selectorAlias);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createListArgument(java.lang.String,
     *      java.util.ArrayList)
     */
    public ListArgument createListArgument(String name, ArrayList<Argument> arguments)
    {
        return new LuceneListArgument(name, arguments);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createFunctionArgument(java.lang.String,
     *      org.alfresco.repo.search.impl.querymodel.Function, java.util.List)
     */
    public FunctionArgument createFunctionArgument(String name, Function function, Map<String, Argument> functionArguments)
    {
        return new LuceneFunctionArgument(name, function, functionArguments);
    }

}
