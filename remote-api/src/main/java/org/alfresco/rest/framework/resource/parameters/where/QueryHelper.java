/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.rest.framework.resource.parameters.where;

import static org.alfresco.rest.antlr.WhereClauseParser.BETWEEN;
import static org.alfresco.rest.antlr.WhereClauseParser.EQUALS;
import static org.alfresco.rest.antlr.WhereClauseParser.EXISTS;
import static org.alfresco.rest.antlr.WhereClauseParser.IN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.alfresco.rest.antlr.WhereClauseParser;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides helper methods for handling a WHERE query.
 * 
 * @author Gethin James
 */
public abstract class QueryHelper
{
    /**
     * An interface used when walking a query tree.  Calls are made to methods when the particular clause is encountered.
     */
    public interface WalkerCallback
    {
        InvalidQueryException UNSUPPORTED = new InvalidQueryException("Unsupported Predicate");

        /**
         * Called any time an EXISTS clause is encountered.
         * @param propertyName Name of the property
         * @param negated returns true if "NOT EXISTS" was used
         */
        default void exists(String propertyName, boolean negated)
        {
            throw UNSUPPORTED;
        }

        /**
         * Called any time a BETWEEN clause is encountered.
         * @param propertyName Name of the property
         * @param firstValue String
         * @param secondValue String
         * @param negated returns true if "NOT BETWEEN" was used
         */
        default void between(String propertyName, String firstValue, String secondValue, boolean negated)
        {
            throw UNSUPPORTED;
        }

        /**
         * One of EQUALS LESSTHAN GREATERTHAN LESSTHANOREQUALS GREATERTHANOREQUALS;
         */
        default void comparison(int type, String propertyName, String propertyValue, boolean negated)
        {
            throw UNSUPPORTED;
        }

        /**
         * Called any time an IN clause is encountered.
         * @param property Name of the property
         * @param negated returns true if "NOT IN" was used
         * @param propertyValues the property values
         */
        default void in(String property, boolean negated, String... propertyValues)
        {
            throw UNSUPPORTED;
        }

        /**
         * Called any time a MATCHES clause is encountered.
         * @param property Name of the property
         * @param propertyValue String
         * @param negated returns true if "NOT MATCHES" was used
         */
        default void matches(String property, String propertyValue, boolean negated)
        {
            throw UNSUPPORTED;
        }

        /**
         * Called any time an AND is encountered.
         */
        default void and()
        {
            throw UNSUPPORTED;
        }
        /**
         * Called any time an OR is encountered.
         */
        default void or()
        {
            throw UNSUPPORTED;
        }

        default Collection<String> getProperty(String propertyName, int type, boolean negated)
        {
            throw UNSUPPORTED;
        }
    }

    /**
     * Default implementation.  Override the methods you are interested in. If you don't
     * override the methods then an InvalidQueryException will be thrown.
     */
    public static class WalkerCallbackAdapter implements WalkerCallback {}

    /**
     * Walks a query with a callback for each operation
     * @param query the query
     * @param callback a callback
     */
    public static void walk(Query query, WalkerCallback callback)
    {
        CommonTree tree = query.getTree();
        if (tree != null)
        {
            LinkedList<Tree> stack = new LinkedList<Tree>();
            stack.push(tree);
            callbackTree(tree, callback, false);
        }
    }

    /**
     * Processes a tree type and calls the corresponding callback method.
     * @param tree Tree
     * @param callback WalkerCallback
     * @param negated boolean
     */
    protected static void callbackTree(Tree tree, WalkerCallback callback, boolean negated)
    {
        if (tree != null)
        {
            switch (tree.getType()) {
            case EXISTS:
                if (WhereClauseParser.PROPERTYNAME == tree.getChild(0).getType())
                {
                    callback.exists(tree.getChild(0).getText(), negated);
                    return;
                }
                break;
            case WhereClauseParser.MATCHES:
                if (WhereClauseParser.PROPERTYNAME == tree.getChild(0).getType())
                {
                    callback.matches(tree.getChild(0).getText(), stripQuotes(tree.getChild(1).getText()), negated);
                    return;
                }
                break;
            case IN:
                if (WhereClauseParser.PROPERTYNAME == tree.getChild(0).getType())
                {
                    List<Tree> children = getChildren(tree);
                    //Don't need the first item because its the property name
                    String[] inVals = new String[children.size()-1];
                    for (int i = 1; i < children.size(); i++) 
                    {
                        inVals[i-1] = stripQuotes(children.get(i).getText());
                    }
                    callback.in(tree.getChild(0).getText(), negated, inVals);
                    return;
                }
                break;
            case BETWEEN:
                if (WhereClauseParser.PROPERTYNAME == tree.getChild(0).getType())
                {
                    callback.between(tree.getChild(0).getText(), stripQuotes(tree.getChild(1).getText()), stripQuotes(tree.getChild(2).getText()), negated);
                    return;
                }
                break;
            case EQUALS: //fall through (comparison)
            case WhereClauseParser.LESSTHAN: //fall through (comparison)
            case WhereClauseParser.GREATERTHAN: //fall through (comparison)
            case WhereClauseParser.LESSTHANOREQUALS: //fall through (comparison)
            case WhereClauseParser.GREATERTHANOREQUALS:		
                if (WhereClauseParser.PROPERTYNAME == tree.getChild(0).getType() &&
                    WhereClauseParser.PROPERTYVALUE == tree.getChild(1).getType())
                {
                    callback.comparison(tree.getType(), tree.getChild(0).getText(), stripQuotes(tree.getChild(1).getText()), negated);
                    return;
                }
                break;
            case WhereClauseParser.NEGATION:
                //Negate the next element
                callbackTree(tree.getChild(0), callback, true);
                return;
            case WhereClauseParser.OR:
                callback.or();
                List<Tree> children = getChildren(tree);
                for (Tree child : children) 
                {
                    callbackTree(child, callback, negated);
                }
                return;				
            case WhereClauseParser.AND:
                callback.and();
                List<Tree> childrenOfAnd = getChildren(tree);
                for (Tree child : childrenOfAnd) 
                {
                    callbackTree(child, callback, negated);
                }
                return;
            default:
            }
            callbackTree(tree.getChild(0), callback, negated);  //Callback on the next node
        }
    }

    /**
     * Finds the siblings of the current tree item (does not include the current item)
     * that are after it in the tree (but at the same level).
     * @param tree the current tree
     * @return siblings - all the elements at the same level in the tree
     */
    //    public static List<Tree> getYoungerSiblings(Tree tree) 
    //    {
    //    	Tree parent = tree.getParent();
    //    	
    //    	if (parent!=null && parent.getChildCount() > 0)
    //    	{
    //    		List<Tree> sibs = new ArrayList<Tree>(parent.getChildCount()-1);
    //    		boolean laterChildren = false;
    //    		for (int i = 0; i < parent.getChildCount(); i++) {
    //    			Tree child = parent.getChild(i);
    //    			if (tree.equals(child))
    //    			{
    //    				laterChildren = true;
    //    			}
    //    			else
    //    			{
    //    				if (laterChildren)	sibs.add(child);
    //    			}
    //			}
    //    		return sibs;
    //    	}
    //    	
    //
    //	}

    /**
     * Gets the children as a List
     * @param tree Tree
     * @return either emptyList or the children.
     */
    public static List<Tree> getChildren(Tree tree)
    {
        if (tree!=null && tree.getChildCount() > 0)
        {
            List<Tree> children = new ArrayList<Tree>(tree.getChildCount());
            for (int i = 0; i < tree.getChildCount(); i++) {
                Tree child = tree.getChild(i);
                children.add(child);
            }
            return children;
        }

        //Default
        return Collections.emptyList();
    }

    private static final String SINGLE_QUOTE = "'";

    /**
     * Strips off any leading or trailing single quotes.
     * @param toBeStripped String
     * @return the String that has been stripped
     */
    public static String stripQuotes(String toBeStripped)
    {
        if (StringUtils.isNotEmpty(toBeStripped) && toBeStripped.startsWith(SINGLE_QUOTE) && toBeStripped.endsWith(SINGLE_QUOTE))
        {
            return toBeStripped.substring(1,toBeStripped.length()-1);
        }
        return toBeStripped; //default to return the String unchanged.
    }

    public static QueryResolver.WalkerSpecifier resolve(final Query query)
    {
        return new QueryResolver.WalkerSpecifier(query);
    }

    /**
     * Helper class allowing WHERE query resolving using query walker. By default {@link BasicQueryWalker} is used, but different walker can be supplied.
     */
    public static abstract class QueryResolver<S extends QueryResolver<?>>
    {
        private final Query query;
        protected WalkerCallback queryWalker;
        protected Function<Collection<String>, BasicQueryWalker> orQueryWalkerSupplier;
        protected boolean clausesNegatable = true;
        protected boolean validateLeniently = false;
        protected abstract S self();

        public QueryResolver(Query query)
        {
            this.query = query;
        }

        /**
         * Get property expected values.
         * @param propertyName Property name.
         * @param clauseType Property comparison type.
         * @param negated Comparison type negation.
         * @return Map composed of all comparators and compared values.
         */
        public Collection<String> getProperty(final String propertyName, final int clauseType, final boolean negated)
        {
            processQuery(propertyName);
            return queryWalker.getProperty(propertyName, clauseType, negated);
        }

        protected void processQuery(final String... propertyNames)
        {
            if (queryWalker == null)
            {
                if (orQueryWalkerSupplier != null)
                {
                    queryWalker = orQueryWalkerSupplier.apply(Set.of(propertyNames));
                }
                else
                {
                    queryWalker = new BasicQueryWalker(propertyNames);
                }
            }
            if (queryWalker instanceof BasicQueryWalker)
            {
                ((BasicQueryWalker) queryWalker).setClausesNegatable(clausesNegatable);
                ((BasicQueryWalker) queryWalker).setValidateStrictly(!validateLeniently);
            }
            walk(query, queryWalker);
        }

        /**
         * Helper class providing methods related with default query walker {@link BasicQueryWalker}.
         */
        public static class DefaultWalkerOperations<R extends DefaultWalkerOperations<?>> extends QueryResolver<R>
        {
            public DefaultWalkerOperations(Query query)
            {
                super(query);
            }

            @SuppressWarnings("unchecked")
            @Override
            protected R self()
            {
                return (R) this;
            }

            /**
             * Specifies that query properties and comparison types should NOT be verified strictly.
             */
            public R leniently()
            {
                this.validateLeniently = true;
                return self();
            }

            /**
             * Specifies that clause types negations are not allowed in query.
             */
            public R withoutNegations()
            {
                this.clausesNegatable = false;
                return self();
            }

            /**
             * Get property with expected values.
             * @param propertyName Property name.
             * @return Map composed of all comparators and compared values.
             */
            public WhereProperty getProperty(final String propertyName)
            {
                processQuery(propertyName);
                return ((BasicQueryWalker) this.queryWalker).getProperty(propertyName);
            }

            /**
             * Get multiple properties with it's expected values.
             * @param propertyNames Property names.
             * @return List of maps composed of all comparators and compared values.
             */
            public List<WhereProperty> getProperties(final String... propertyNames)
            {
                processQuery(propertyNames);
                return ((BasicQueryWalker) this.queryWalker).getProperties(propertyNames);
            }

            /**
             * Get multiple properties with it's expected values.
             * @param propertyNames Property names.
             * @return Map composed of property names and maps composed of all comparators and compared values.
             */
            public Map<String, WhereProperty> getPropertiesAsMap(final String... propertyNames)
            {
                processQuery(propertyNames);
                return ((BasicQueryWalker) this.queryWalker).getPropertiesAsMap(propertyNames);
            }
        }

        /**
         * Helper class allowing to specify custom {@link WalkerCallback} implementation or {@link BasicQueryWalker} extension.
         */
        public static class WalkerSpecifier extends DefaultWalkerOperations<WalkerSpecifier>
        {
            public WalkerSpecifier(Query query)
            {
                super(query);
            }

            @Override
            protected WalkerSpecifier self()
            {
                return this;
            }

            /**
             * Specifies that OR operator instead of AND should be used while resolving the query.
             */
            public DefaultWalkerOperations<? extends DefaultWalkerOperations<?>> usingOrOperator()
            {
                this.orQueryWalkerSupplier = (propertyNames) -> new BasicQueryWalker(propertyNames)
                {
                    @Override
                    public void or() {/*Enable OR support, disable AND support*/}
                    @Override
                    public void and() {throw UNSUPPORTED;}
                };
                return this;
            }

            /**
             * Allows to specify custom {@link BasicQueryWalker} extension, which should be used to resolve the query.
             */
            public <T extends BasicQueryWalker> DefaultWalkerOperations<? extends DefaultWalkerOperations<?>> usingWalker(final T queryWalker)
            {
                this.queryWalker = queryWalker;
                return this;
            }

            /**
             * Allows to specify custom {@link WalkerCallback} implementation, which should be used to resolve the query.
             */
            public <T extends WalkerCallback> QueryResolver<? extends QueryResolver<?>> usingWalker(final T queryWalker)
            {
                this.queryWalker = queryWalker;
                return this;
            }
        }
    }
}
