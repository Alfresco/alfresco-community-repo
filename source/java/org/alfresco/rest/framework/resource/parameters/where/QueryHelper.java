package org.alfresco.rest.framework.resource.parameters.where;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.rest.antlr.WhereClauseParser;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.apache.commons.lang.StringUtils;

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
    public static interface WalkerCallback
    {
		/**
		 * Called any time an EXISTS clause is encountered.
		 * @param propertyName Name of the property
         * @param negated returns true if "NOT EXISTS" was used
		 */
        void exists(String propertyName, boolean negated);

		/**
		 * Called any time a BETWEEN clause is encountered.
		 * @param propertyName Name of the property
		 * @param firstValue
		 * @param secondValue
         * @param negated returns true if "NOT BETWEEN" was used
		 */
		void between(String propertyName, String firstValue, String secondValue, boolean negated);
		
		/**
		 * One of EQUALS LESSTHAN GREATERTHAN LESSTHANOREQUALS GREATERTHANOREQUALS;
		 */
		void comparison(int type, String propertyName, String propertyValue);
		
		/**
		 * Called any time an IN clause is encountered.
		 * @param property Name of the property
		 * @param negated returns true if "NOT IN" was used
		 * @param propertyValues the property values
		 */
		void in(String property, boolean negated, String... propertyValues);
		
		
		/**
		 * Called any time a MATCHES clause is encountered.
		 * @param propertyName Name of the property
		 * @param propertyValue
         * @param negated returns true if "NOT MATCHES" was used
		 */
		void matches(String property, String propertyValue, boolean negated);
		
		/**
		 * Called any time an AND is encountered.
		 */  
		void and();
		/**
		 * Called any time an OR is encountered.
		 */  
		void or();
    }
    
    /**
     * Default implementation.  Override the methods you are interested in. If you don't
     * override the methods then an InvalidQueryException will be thrown.
     */
    private static final String UNSUPPORTED_TEXT = "Unsupported Predicate";
    private static final InvalidQueryException UNSUPPORTED = new InvalidQueryException(UNSUPPORTED_TEXT);
    
    public static class WalkerCallbackAdapter implements WalkerCallback
    {
    	@Override
        public void exists(String propertyName, boolean negated) { throw UNSUPPORTED;}
    	@Override
		public void between(String propertyName, String firstValue, String secondValue, boolean negated) { throw UNSUPPORTED;}
    	@Override
		public void comparison(int type, String propertyName, String propertyValue) { throw UNSUPPORTED;}
    	@Override
		public void in(String propertyName, boolean negated, String... propertyValues) { throw UNSUPPORTED;}
    	@Override
		public void matches(String property, String value, boolean negated)  { throw UNSUPPORTED;}
    	@Override
		public void and() {throw UNSUPPORTED;}
    	@Override
		public void or() {throw UNSUPPORTED;}
    }
    
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
     * @param tree
     * @param callback
     */
    protected static void callbackTree(Tree tree, WalkerCallback callback, boolean negated)
    {
    	if (tree != null)
    	{
    		switch (tree.getType()) {
			case WhereClauseParser.EXISTS:
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
			case WhereClauseParser.IN:
				if (WhereClauseParser.PROPERTYNAME == tree.getChild(0).getType())
				{
					List<Tree> children = getChildren(tree);
					//Don't need the first item because its the property name
					String[] inVals = new String[children.size()-1];
					for (int i = 1; i < children.size(); i++) {
						inVals[i-1] = stripQuotes(children.get(i).getText());
					}
					callback.in(tree.getChild(0).getText(), negated, inVals);
					return;
				}
				break;
			case WhereClauseParser.BETWEEN:
				if (WhereClauseParser.PROPERTYNAME == tree.getChild(0).getType())
				{
					callback.between(tree.getChild(0).getText(), stripQuotes(tree.getChild(1).getText()), stripQuotes(tree.getChild(2).getText()), negated);
					return;
				}
				break;
			case WhereClauseParser.EQUALS: //fall through (comparison)
			case WhereClauseParser.LESSTHAN: //fall through (comparison)
			case WhereClauseParser.GREATERTHAN: //fall through (comparison)
			case WhereClauseParser.LESSTHANOREQUALS: //fall through (comparison)
			case WhereClauseParser.GREATERTHANOREQUALS:		
				if (WhereClauseParser.PROPERTYNAME == tree.getChild(0).getType() &&
					WhereClauseParser.PROPERTYVALUE == tree.getChild(1).getType())
				{
					callback.comparison(tree.getType(), tree.getChild(0).getText(), stripQuotes(tree.getChild(1).getText()));
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
				for (Tree child : children) {
					callbackTree(child, callback, negated);
				}
				return;				
			case WhereClauseParser.AND:
				callback.and();
				List<Tree> childrenOfAnd = getChildren(tree);
				for (Tree child : childrenOfAnd) {
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
     * @param tree
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
    
	/**
     * Strips off any leading or trailing single quotes.
     * @param toBeStripped
     * @return the String that has been stripped
     */
    private static final String SINGLE_QUOTE = "'";
    public static String stripQuotes(String toBeStripped)
    {
    	if (StringUtils.isNotEmpty(toBeStripped) && toBeStripped.startsWith(SINGLE_QUOTE) && toBeStripped.endsWith(SINGLE_QUOTE))
    	{
    	  return toBeStripped.substring(1,toBeStripped.length()-1);
    	}
    	return toBeStripped; //default to return the String unchanged.
    }
}
