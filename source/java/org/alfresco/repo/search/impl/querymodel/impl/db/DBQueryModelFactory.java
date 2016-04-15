/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.search.impl.querymodel.impl.db;

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
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.search.impl.querymodel.QueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.Selector;
import org.alfresco.repo.search.impl.querymodel.SelectorArgument;
import org.alfresco.repo.search.impl.querymodel.Source;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBChild;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBDescendant;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBEquals;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBExists;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBFTSFuzzyTerm;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBFTSPhrase;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBFTSPrefixTerm;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBFTSProximity;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBFTSRange;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBFTSTerm;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBFTSWildTerm;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBGreaterThan;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBGreaterThanOrEquals;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBIn;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBLessThan;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBLessThanOrEquals;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBLike;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBLower;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBNotEquals;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBPropertyAccessor;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBScore;
import org.alfresco.repo.search.impl.querymodel.impl.db.functions.DBUpper;
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
import org.alfresco.service.namespace.QName;

/**
 * @author Andy
 *
 */
public class DBQueryModelFactory implements QueryModelFactory
{
    private HashMap<String, Function> functions = new HashMap<String, Function>();
    
    public DBQueryModelFactory()
    {
        functions.put(Equals.NAME, new DBEquals());
        functions.put(PropertyAccessor.NAME, new DBPropertyAccessor());
        functions.put(Score.NAME, new DBScore());
        functions.put(Upper.NAME, new DBUpper());
        functions.put(Lower.NAME, new DBLower());

        functions.put(NotEquals.NAME, new DBNotEquals());
        functions.put(LessThan.NAME, new DBLessThan());
        functions.put(LessThanOrEquals.NAME, new DBLessThanOrEquals());
        functions.put(GreaterThan.NAME, new DBGreaterThan());
        functions.put(GreaterThanOrEquals.NAME, new DBGreaterThanOrEquals());

        functions.put(In.NAME, new DBIn());
        functions.put(Like.NAME, new DBLike());
        functions.put(Exists.NAME, new DBExists());

        functions.put(Child.NAME, new DBChild());
        functions.put(Descendant.NAME, new DBDescendant());

        functions.put(FTSTerm.NAME, new DBFTSTerm());
        functions.put(FTSPhrase.NAME, new DBFTSPhrase());
        functions.put(FTSProximity.NAME, new DBFTSProximity());
        functions.put(FTSRange.NAME, new DBFTSRange());
        functions.put(FTSPrefixTerm.NAME, new DBFTSPrefixTerm());
        functions.put(FTSWildTerm.NAME, new DBFTSWildTerm());
        functions.put(FTSFuzzyTerm.NAME, new DBFTSFuzzyTerm());
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createQuery(java.util.List, org.alfresco.repo.search.impl.querymodel.Source, org.alfresco.repo.search.impl.querymodel.Constraint, java.util.List)
     */
    @Override
    public Query createQuery(List<Column> columns, Source source, Constraint constraint, List<Ordering> orderings)
    {
        return new DBQuery(columns, source, constraint, orderings);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createSelector(org.alfresco.service.namespace.QName, java.lang.String)
     */
    @Override
    public Selector createSelector(QName classQName, String alias)
    {
       return new DBSelector(classQName, alias);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createJoin(org.alfresco.repo.search.impl.querymodel.Source, org.alfresco.repo.search.impl.querymodel.Source, org.alfresco.repo.search.impl.querymodel.JoinType, org.alfresco.repo.search.impl.querymodel.Constraint)
     */
    @Override
    public Join createJoin(Source left, Source right, JoinType joinType, Constraint joinCondition)
    {
        return new DBJoin(left, right, joinType, joinCondition);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createConjunction(java.util.List)
     */
    @Override
    public Constraint createConjunction(List<Constraint> constraints)
    {
        return new DBConjunction(constraints);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createDisjunction(java.util.List)
     */
    @Override
    public Constraint createDisjunction(List<Constraint> constraints)
    {
        return new DBDisjunction(constraints);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createFunctionalConstraint(org.alfresco.repo.search.impl.querymodel.Function, java.util.Map)
     */
    @Override
    public Constraint createFunctionalConstraint(Function function, Map<String, Argument> functionArguments)
    {
        return new DBFunctionalConstraint(function, functionArguments);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createColumn(org.alfresco.repo.search.impl.querymodel.Function, java.util.Map, java.lang.String)
     */
    @Override
    public Column createColumn(Function function, Map<String, Argument> functionArguments, String alias)
    {
       return new DBColumn(function, functionArguments, alias);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createLiteralArgument(java.lang.String, org.alfresco.service.namespace.QName, java.io.Serializable)
     */
    @Override
    public LiteralArgument createLiteralArgument(String name, QName type, Serializable value)
    {
        return new DBLiteralArgument(name, type, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createOrdering(org.alfresco.repo.search.impl.querymodel.Column, org.alfresco.repo.search.impl.querymodel.Order)
     */
    @Override
    public Ordering createOrdering(Column column, Order order)
    {
        return new DBOrdering(column, order);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createParameterArgument(java.lang.String, java.lang.String)
     */
    @Override
    public ParameterArgument createParameterArgument(String name, String parameterName)
    {
        return new DBParameterArgument(name, parameterName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createPropertyArgument(java.lang.String, boolean, boolean, java.lang.String, java.lang.String)
     */
    @Override
    public PropertyArgument createPropertyArgument(String name, boolean queryable, boolean orderable, String selectorAlias, String propertyName)
    {
        return new DBPropertyArgument(name, queryable, orderable, selectorAlias, propertyName);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createSelectorArgument(java.lang.String, java.lang.String)
     */
    @Override
    public SelectorArgument createSelectorArgument(String name, String selectorAlias)
    {
        return new DBSelectorArgument(name, selectorAlias);
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
            try
            {
                return function.getClass().newInstance();
            }
            catch (InstantiationException e)
            {
                throw new QueryModelException("InstantiationException", e);
            }
            catch (IllegalAccessException e)
            {
                throw new QueryModelException("IllegalAccessException", e);
            }
        }
        else
        {
            // scan
            for (String key : functions.keySet())
            {
                if (key.equalsIgnoreCase(functionName))
                {
                    try
                    {
                        return functions.get(key).getClass().newInstance();
                    }
                    catch (InstantiationException e)
                    {
                        throw new QueryModelException("InstantiationException", e);
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new QueryModelException("IllegalAccessException", e);
                    }
                }
            }
           return null;
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createListArgument(java.lang.String, java.util.ArrayList)
     */
    @Override
    public ListArgument createListArgument(String name, ArrayList<Argument> arguments)
    {
        return new DBListArgument(name, arguments);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.QueryModelFactory#createFunctionArgument(java.lang.String, org.alfresco.repo.search.impl.querymodel.Function, java.util.Map)
     */
    @Override
    public FunctionArgument createFunctionArgument(String name, Function function, Map<String, Argument> functionArguments)
    {
        return new DBFunctionArgument(name, function, functionArguments);
    }

}
