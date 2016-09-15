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
package org.alfresco.repo.search.impl.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.search.adaptor.lucene.AnalysisMode;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.Constraint.Occur;
import org.alfresco.repo.search.impl.querymodel.Function;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.LiteralArgument;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.repo.search.impl.querymodel.QueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.QueryOptions.Connective;
import org.alfresco.repo.search.impl.querymodel.Selector;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSFuzzyTerm;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSPhrase;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSPrefixTerm;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSProximity;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSRange;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSTerm;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSWildTerm;
import org.alfresco.repo.search.impl.querymodel.impl.functions.PropertyAccessor;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.util.ISO9075;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

public class FTSQueryParser
{
	public static enum RerankPhase
	{
		SINGLE_PASS, SINGLE_PASS_WITH_AUTO_PHRASE, QUERY_PHASE, RERANK_PHASE;
	}
	
    private static TestNodeBuilder testNodeBuilder = new TestNodeBuilder();
    
    public static void setTestNodeBuilder(TestNodeBuilder tnb)
    {
        testNodeBuilder = tnb;
    }
    
    static final String KEY_REPLACELONESTAR = "KEY_REPLACELONESTAR";
    static final String VALUE_REPLACELONESTAR = "ISNODE:T";
    
    @SuppressWarnings("unused")
    static public Constraint buildFTS(String ftsExpression, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext, Selector selector,
            Map<String, Column> columnMap, FTSParser.Mode mode, Connective defaultFieldConnective, Map<String, String> templates, String defaultField, RerankPhase rerankPhase)
    {
        // TODO: Decode sql escape for '' should do in CMIS layer

        // parse templates to trees ...

        Map<String, CommonTree> templateTrees = new HashMap<String, CommonTree>();

        if (templates == null)
        {
            templates = new HashMap<String, String>();
        }
        templates.put(KEY_REPLACELONESTAR, VALUE_REPLACELONESTAR);
        
        for (String name : templates.keySet())
        {
            FTSParser parser = null;

            try
            {
                String templateDefinition = templates.get(name);
                CharStream cs = new ANTLRStringStream(templateDefinition);
                FTSLexer lexer = new FTSLexer(cs);
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                parser = new FTSParser(tokens);
                parser.setMode(mode);
                parser.setDefaultFieldConjunction(defaultFieldConnective == Connective.AND ? true : false);
                CommonTree ftsNode = (CommonTree) parser.ftsQuery().getTree();
                //Check for duplicate template of properties e.g. NAME, name and NaMe.
                if (templateTrees.containsKey(name.toLowerCase()))
                {
                    throw new FTSQueryException("Duplicate template of property: " + name);
                }

                templateTrees.put(name.toLowerCase(), ftsNode);
            }
            catch (RecognitionException e)
            {
                if (parser != null)
                {
                    String[] tokenNames = parser.getTokenNames();
                    String hdr = parser.getErrorHeader(e);
                    String msg = parser.getErrorMessage(e, tokenNames);
                    throw new FTSQueryException(hdr + "\n" + msg, e);
                 }
                 return null;
            }
        }

        FTSParser parser = null;
        try
        {
            CharStream cs = new ANTLRStringStream(ftsExpression);
            FTSLexer lexer = new FTSLexer(cs);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            parser = new FTSParser(tokens);
            parser.setMode(mode);
            parser.setDefaultFieldConjunction(defaultFieldConnective == Connective.AND ? true : false);
            CommonTree ftsNode = (CommonTree) parser.ftsQuery().getTree();
            // Rewrite for auto phrase
            if(rerankPhase != RerankPhase.RERANK_PHASE.SINGLE_PASS)
            {
                ftsNode = autoPhraseReWrite(ftsNode, defaultFieldConnective == Connective.AND ? true : false, rerankPhase);
            }
            return buildFTSConnective(null, ftsNode, factory, functionEvaluationContext, selector, columnMap, templateTrees, defaultField);
        }
        catch (RecognitionException e)
        {
            if (parser != null)
            {
                String[] tokenNames = parser.getTokenNames();
                String hdr = parser.getErrorHeader(e);
                String msg = parser.getErrorMessage(e, tokenNames);
                throw new FTSQueryException(hdr + "\n" + msg, e);
            }
            return null;
        }

    }

    private static CommonTree autoPhraseReWrite(CommonTree node, boolean defaultConjunction, RerankPhase rerankPhase) {
    	if(node == null)
    	{
    		return node;
    	}
    	CommonTree newNode = new CommonTree(node);
		if(isAutoPhrasable(node, defaultConjunction))
		{
			StringBuilder phrase = new StringBuilder();
			LinkedList<CommonTree> autoPhrased = new LinkedList<CommonTree>();
			for (Object current : node.getChildren())
			{
				CommonTree child = (CommonTree)current;
				if((child.getType() ==  FTSParser.MANDATORY) || (child.getType() ==  FTSParser.DEFAULT))
    			{
    				if(child.getChildCount() > 0)
    				{
    					CommonTree term = (CommonTree)child.getChild(0);
    					if((term.getType() == FTSParser.TERM) && (term.getChildCount() == 1))
    					{
    						String termText = getText((List<Tree>) term.getChildren());
    						if(phrase.length() > 0)
    						{
    							phrase.append(" ");
    						}
    						phrase.append(termText);
    						autoPhrased.add(child);
    					}
    					else
    					{
    						CommonTree newChild = autoPhraseReWrite(child, defaultConjunction, rerankPhase);
    						newNode.addChild(newChild);
    					}
    					
    				}
    			}
    			else
    			{
    				CommonTree newChild = autoPhraseReWrite(child, defaultConjunction, rerankPhase);
					newNode.addChild(newChild);
    			}
			}
			CommonTree disjunction = new CommonTree(new DisjunctionToken());
			newNode.addChild(disjunction);

			if(rerankPhase == RerankPhase.SINGLE_PASS_WITH_AUTO_PHRASE)
			{
				CommonTree termConjuctionLink = new CommonTree(new DefaultToken());
				disjunction.addChild(termConjuctionLink);
				CommonTree termConjunction = new CommonTree(new ConjunctionToken());
				termConjuctionLink.addChild(termConjunction);
				for(CommonTree phrased : autoPhrased)
				{
					CommonTree newPhrased = autoPhraseReWrite(phrased, defaultConjunction, rerankPhase);
					termConjunction.addChild(newPhrased);
				}
			}


			CommonTree phraseLink = new CommonTree(new DefaultToken());
			disjunction.addChild(phraseLink);
			CommonTree phraseTree = new CommonTree(new PhraseToken());
			phraseLink.addChild(phraseTree);
			phraseTree.addChild(new CommonTree(new WordToken(phrase.toString())));
			
		}
		else
		{
			if(node.getChildren() != null)
			{
				for (Object current : node.getChildren())
				{
					CommonTree child = (CommonTree) current;
					CommonTree newChild = autoPhraseReWrite(child, defaultConjunction, rerankPhase);
					newNode.addChild(newChild);
				}
			}
		}
		return newNode;
    }

    private static boolean isAutoPhrasable(CommonTree node, boolean defaultConjunction) {
    	if((node.getType() == FTSParser.CONJUNCTION) && (node.getChildCount() > 1))
    	{
    		int simpleTermCount = 0;
    		for (Object current : node.getChildren())
    		{
    			CommonTree child = (CommonTree) current;
    			if((child.getType() ==  FTSParser.MANDATORY) || (child.getType() ==  FTSParser.DEFAULT))
    			{
    				if(child.getChildCount() > 0)
    				{
    					CommonTree item = (CommonTree)child.getChild(0);
    					if((item.getType() == FTSParser.TERM) && (item.getChildCount() == 1))
    					{
    						simpleTermCount++;
    					}
    				}
    			}
    			else
    			{
    				return false;
    			}
    		}
    		return simpleTermCount > 1;
    	}

    	return false;

    }

	static private Constraint buildFTSConnective(CommonTree fieldReferenceNode, CommonTree node, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, Map<String, Column> columnMap, Map<String, CommonTree> templateTrees, String defaultField)
    {
        Connective connective;
        switch (node.getType())
        {
        case FTSParser.DISJUNCTION:
        case FTSParser.FIELD_DISJUNCTION:
            connective = Connective.OR;
            break;
        case FTSParser.CONJUNCTION:
        case FTSParser.FIELD_CONJUNCTION:
            connective = Connective.AND;
            break;
        default:
            throw new FTSQueryException("Invalid connective ..." + node.getText());
        }

        List<Constraint> constraints = new ArrayList<Constraint>(node.getChildCount());
        CommonTree testNode;
        for (int i = 0; i < node.getChildCount(); i++)
        {
            CommonTree subNode = (CommonTree) node.getChild(i);
            Constraint constraint;
            switch (subNode.getType())
            {
            case FTSParser.DISJUNCTION:
            case FTSParser.FIELD_DISJUNCTION:
            case FTSParser.CONJUNCTION:
            case FTSParser.FIELD_CONJUNCTION:
                constraint = buildFTSConnective(fieldReferenceNode, subNode, factory, functionEvaluationContext, selector, columnMap, templateTrees, defaultField);
                setBoost(constraint, subNode);
                break;
            case FTSParser.NEGATION:
            case FTSParser.FIELD_NEGATION:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap, templateTrees, defaultField);
                constraint.setOccur(Occur.EXCLUDE);
                setBoost(constraint, subNode);
                break;
            case FTSParser.DEFAULT:
            case FTSParser.FIELD_DEFAULT:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap, templateTrees, defaultField);
                constraint.setOccur(Occur.DEFAULT);
                setBoost(constraint, subNode);
                break;
            case FTSParser.MANDATORY:
            case FTSParser.FIELD_MANDATORY:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap, templateTrees, defaultField);
                constraint.setOccur(Occur.MANDATORY);
                setBoost(constraint, subNode);
                break;
            case FTSParser.OPTIONAL:
            case FTSParser.FIELD_OPTIONAL:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap, templateTrees, defaultField);
                constraint.setOccur(Occur.OPTIONAL);
                setBoost(constraint, subNode);
                break;
            case FTSParser.EXCLUDE:
            case FTSParser.FIELD_EXCLUDE:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap, templateTrees, defaultField);
                constraint.setOccur(Occur.EXCLUDE);
                setBoost(constraint, subNode);
                break;

            default:
                throw new FTSQueryException("Unsupported FTS option " + subNode.getText());
            }
            constraints.add(constraint);
        }
        if (constraints.size() == 1)
        {
            return constraints.get(0);
        }
        else
        {
            if (connective == Connective.OR)
            {
                return factory.createDisjunction(constraints);
            }
            else
            {
                return factory.createConjunction(constraints);
            }
        }
    }

    static private void setBoost(Constraint constraint, CommonTree subNode)
    {
        for (int i = 0, l = subNode.getChildCount(); i < l; i++)
        {
            CommonTree child = (CommonTree) subNode.getChild(i);
            if (child.getType() == FTSParser.BOOST)
            {
                String boostString = child.getChild(0).getText();
                float boost = Float.parseFloat(boostString);
                constraint.setBoost(boost);
                return;
            }
        }
    }

    protected static class TestNodeBuilder
    {
        protected CommonTree build(CommonTree fieldReferenceNode, CommonTree argNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, Map<String, Column> columnMap, Map<String, CommonTree> templateTrees, String defaultField)
        {
            CommonTree testNode = argNode;
            // Check for template replacement

            PropertyArgument parg = null;
            if (fieldReferenceNode != null)
            {
                parg = buildFieldReference("", fieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
            }
            else
            {
                CommonTree specifiedFieldReferenceNode = findFieldReference(testNode);
                if (specifiedFieldReferenceNode != null)
                {
                    parg = buildFieldReference(FTSRange.ARG_PROPERTY, specifiedFieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
                }
            }
            if (parg != null)
            {
                CommonTree template = templateTrees.get(parg.getPropertyName().toLowerCase());
                if (template != null)
                {
                    testNode = copyAndReplace(template, testNode);
                }
            }
            else
            {
                if(hasOptionalFieldReference(testNode))
                {
                    if (isStarWithNoField(testNode))
                    {
                        testNode = copyAndReplace(templateTrees.get(KEY_REPLACELONESTAR.toLowerCase()), testNode);
                    }
                    else
                    {
                        CommonTree template = templateTrees.get(defaultField.toLowerCase());
                        if (template != null)
                        {
                            testNode = copyAndReplace(template, testNode);
                        }
                    }
                }
            }
            
            return testNode;
        }
    }

    static private Constraint buildFTSTest(CommonTree fieldReferenceNode, CommonTree argNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, Map<String, Column> columnMap, Map<String, CommonTree> templateTrees, String defaultField)
    {
        CommonTree testNode = testNodeBuilder.build(fieldReferenceNode, argNode, factory, functionEvaluationContext, selector, columnMap, templateTrees, defaultField);

        Tree termNode;
        Float fuzzy = findFuzzy(testNode);
        switch (testNode.getType())
        {
        case FTSParser.DISJUNCTION:
        case FTSParser.FIELD_DISJUNCTION:
        case FTSParser.CONJUNCTION:
        case FTSParser.FIELD_CONJUNCTION:
            return buildFTSConnective(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap, templateTrees, defaultField);
        case FTSParser.TERM:
        case FTSParser.FG_TERM:
            termNode = testNode.getChild(0);
            if (fuzzy == null)
            {
                switch (termNode.getType())
                {
                case FTSParser.FTSPRE:
                    return buildPrefixTerm(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap);
                case FTSParser.FTSWILD:
                case FTSParser.STAR:
                case FTSParser.QUESTION_MARK:
                    return buildWildTerm(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap);
                default:
                    return buildTerm(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap);
                }
            }
            else
            {
                switch (termNode.getType())
                {
                case FTSParser.FTSPRE:
                case FTSParser.FTSWILD:
                case FTSParser.STAR:
                case FTSParser.QUESTION_MARK:
                    throw new FTSQueryException("Fuzzy queries are not supported with wild cards");
                default:
                    return buildFuzzyTerm(fuzzy, fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap);
                }
            }
        case FTSParser.EXACT_TERM:
        case FTSParser.FG_EXACT_TERM:
            termNode = testNode.getChild(0);
            if (fuzzy == null)
            {
                switch (termNode.getType())
                {
                case FTSParser.FTSPRE:
                    return buildExactPrefixTerm(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap);
                case FTSParser.FTSWILD:
                case FTSParser.STAR:
                case FTSParser.QUESTION_MARK:
                    return buildExactWildTerm(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap);
                default:
                    return buildExactTerm(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap);
                }
            }
            else
            {
                switch (termNode.getType())
                {
                case FTSParser.FTSPRE:
                case FTSParser.FTSWILD:
                case FTSParser.STAR:
                case FTSParser.QUESTION_MARK:
                    throw new FTSQueryException("Fuzzy queries are not supported with wild cards");
                default:
                    return buildFuzzyTerm(fuzzy, fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap);
                }
            }
        case FTSParser.PHRASE:
        case FTSParser.FG_PHRASE:
            return buildPhrase(fuzzy, fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap);
        case FTSParser.EXACT_PHRASE:
        case FTSParser.FG_EXACT_PHRASE:
            return buildExactPhrase(fuzzy, fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap);
        case FTSParser.SYNONYM:
        case FTSParser.FG_SYNONYM:
            termNode = testNode.getChild(0);
            if (fuzzy == null)
            {
                switch (termNode.getType())
                {
                case FTSParser.FTSPRE:
                    return buildPrefixTerm(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap);
                case FTSParser.FTSWILD:
                case FTSParser.STAR:
                case FTSParser.QUESTION_MARK:
                    return buildWildTerm(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap);
                default:
                    return buildExpandTerm(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap);
                }
            }
            else
            {
                switch (termNode.getType())
                {
                case FTSParser.FTSPRE:
                case FTSParser.FTSWILD:
                case FTSParser.STAR:
                case FTSParser.QUESTION_MARK:
                    throw new FTSQueryException("Fuzzy queries are not supported with wild cards");
                default:
                    return buildFuzzyTerm(fuzzy, fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap);
                }
            }
        case FTSParser.PROXIMITY:
        case FTSParser.FG_PROXIMITY:
            return buildProximity(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap);
        case FTSParser.RANGE:
        case FTSParser.FG_RANGE:
            return buildRange(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columnMap);
        case FTSParser.FIELD_GROUP:
            if (fieldReferenceNode != null)
            {
                throw new IllegalStateException("Already in field?");
            }
            CommonTree newFieldReferenceNode = (CommonTree) testNode.getChild(0);
            CommonTree fieldExperssion = (CommonTree) testNode.getChild(1);
            return buildFTSConnective(newFieldReferenceNode, fieldExperssion, factory, functionEvaluationContext, selector, columnMap, templateTrees, defaultField);
        default:
            throw new FTSQueryException("Unsupported FTS option " + testNode.getText());
        }
    }

    static private Constraint buildRange(CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, Map<String, Column> columnMap)
    {
        String functionName = FTSRange.NAME;
        Function function = factory.getFunction(functionName);
        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
        LiteralArgument larg = factory.createLiteralArgument(FTSRange.ARG_FROM_INC, DataTypeDefinition.BOOLEAN, testNode.getChild(0).getType() == FTSParser.INCLUSIVE);
        functionArguments.put(larg.getName(), larg);
        larg = factory.createLiteralArgument(FTSRange.ARG_FROM, DataTypeDefinition.TEXT, getText(testNode.getChild(1)));
        functionArguments.put(larg.getName(), larg);
        larg = factory.createLiteralArgument(FTSRange.ARG_TO, DataTypeDefinition.TEXT, getText(testNode.getChild(2)));
        functionArguments.put(larg.getName(), larg);
        larg = factory.createLiteralArgument(FTSRange.ARG_TO_INC, DataTypeDefinition.BOOLEAN, testNode.getChild(3).getType() == FTSParser.INCLUSIVE);
        functionArguments.put(larg.getName(), larg);
        if (fieldReferenceNode != null)
        {
            PropertyArgument parg = buildFieldReference(FTSRange.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
            functionArguments.put(parg.getName(), parg);
        }
        else
        {
            CommonTree specifiedFieldReferenceNode = findFieldReference(testNode);
            if (specifiedFieldReferenceNode != null)
            {
                PropertyArgument parg = buildFieldReference(FTSRange.ARG_PROPERTY, specifiedFieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
                functionArguments.put(parg.getName(), parg);
            }
        }
        return factory.createFunctionalConstraint(function, functionArguments);
    }

    static private Constraint buildProximity(CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, Map<String, Column> columnMap)
    {
        String functionName = FTSProximity.NAME;
        Function function = factory.getFunction(functionName);
        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
        LiteralArgument larg = factory.createLiteralArgument(FTSProximity.ARG_FIRST, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
        functionArguments.put(larg.getName(), larg);
        if (testNode.getChild(1).getChildCount() > 0)
        {
            larg = factory.createLiteralArgument(FTSProximity.ARG_SLOP, DataTypeDefinition.INT, getText(testNode.getChild(1).getChild(0)));
            functionArguments.put(larg.getName(), larg);
        }
        larg = factory.createLiteralArgument(FTSProximity.ARG_LAST, DataTypeDefinition.TEXT, getText(testNode.getChild(2)));
        functionArguments.put(larg.getName(), larg);
        if (fieldReferenceNode != null)
        {
            PropertyArgument parg = buildFieldReference(FTSProximity.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
            functionArguments.put(parg.getName(), parg);
        }
        else
        {
            CommonTree specifiedFieldReferenceNode = findFieldReference(testNode);
            if (specifiedFieldReferenceNode != null)
            {
                PropertyArgument parg = buildFieldReference(FTSProximity.ARG_PROPERTY, specifiedFieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
                functionArguments.put(parg.getName(), parg);
            }
        }
        return factory.createFunctionalConstraint(function, functionArguments);
    }

    static private Constraint buildExpandTerm(CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, Map<String, Column> columnMap)
    {
        String functionName = FTSTerm.NAME;
        Function function = factory.getFunction(functionName);
        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
        LiteralArgument larg = factory.createLiteralArgument(FTSTerm.ARG_TERM, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
        functionArguments.put(larg.getName(), larg);
        larg = factory.createLiteralArgument(FTSTerm.ARG_TOKENISATION_MODE, DataTypeDefinition.ANY, AnalysisMode.TOKENISE);
        functionArguments.put(larg.getName(), larg);
        if (fieldReferenceNode != null)
        {
            PropertyArgument parg = buildFieldReference(FTSTerm.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
            functionArguments.put(parg.getName(), parg);
        }
        else
        {
            CommonTree specifiedFieldReferenceNode = findFieldReference(testNode);
            if (specifiedFieldReferenceNode != null)
            {
                PropertyArgument parg = buildFieldReference(FTSTerm.ARG_PROPERTY, specifiedFieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
                functionArguments.put(parg.getName(), parg);
            }
        }
        return factory.createFunctionalConstraint(function, functionArguments);
    }

    static private Constraint buildPhrase(Float fuzzy, CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory,
            FunctionEvaluationContext functionEvaluationContext, Selector selector, Map<String, Column> columnMap)
    {
        String functionName = FTSPhrase.NAME;
        Function function = factory.getFunction(functionName);
        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
        LiteralArgument larg = factory.createLiteralArgument(FTSPhrase.ARG_PHRASE, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
        functionArguments.put(larg.getName(), larg);
        larg = factory.createLiteralArgument(FTSPhrase.ARG_TOKENISATION_MODE, DataTypeDefinition.ANY, AnalysisMode.DEFAULT);
        functionArguments.put(larg.getName(), larg);
        if (fuzzy != null)
        {
            larg = factory.createLiteralArgument(FTSPhrase.ARG_SLOP, DataTypeDefinition.INT, Integer.valueOf(fuzzy.intValue()));
            functionArguments.put(larg.getName(), larg);
        }
        if (fieldReferenceNode != null)
        {
            PropertyArgument parg = buildFieldReference(FTSPhrase.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
            functionArguments.put(parg.getName(), parg);
        }
        else
        {
            CommonTree specifiedFieldReferenceNode = findFieldReference(testNode);
            if (specifiedFieldReferenceNode != null)
            {
                PropertyArgument parg = buildFieldReference(FTSPhrase.ARG_PROPERTY, specifiedFieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
                functionArguments.put(parg.getName(), parg);
            }
        }
        return factory.createFunctionalConstraint(function, functionArguments);
    }
    
    static private Constraint buildExactPhrase(Float fuzzy, CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory,
            FunctionEvaluationContext functionEvaluationContext, Selector selector, Map<String, Column> columnMap)
    {
        String functionName = FTSPhrase.NAME;
        Function function = factory.getFunction(functionName);
        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
        LiteralArgument larg = factory.createLiteralArgument(FTSPhrase.ARG_PHRASE, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
        functionArguments.put(larg.getName(), larg);
        larg = factory.createLiteralArgument(FTSPhrase.ARG_TOKENISATION_MODE, DataTypeDefinition.ANY, AnalysisMode.IDENTIFIER);
        functionArguments.put(larg.getName(), larg);
        if (fuzzy != null)
        {
            larg = factory.createLiteralArgument(FTSPhrase.ARG_SLOP, DataTypeDefinition.INT, Integer.valueOf(fuzzy.intValue()));
            functionArguments.put(larg.getName(), larg);
        }
        if (fieldReferenceNode != null)
        {
            PropertyArgument parg = buildFieldReference(FTSPhrase.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
            functionArguments.put(parg.getName(), parg);
        }
        else
        {
            CommonTree specifiedFieldReferenceNode = findFieldReference(testNode);
            if (specifiedFieldReferenceNode != null)
            {
                PropertyArgument parg = buildFieldReference(FTSPhrase.ARG_PROPERTY, specifiedFieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
                functionArguments.put(parg.getName(), parg);
            }
        }
        return factory.createFunctionalConstraint(function, functionArguments);
    }

    @SuppressWarnings("unchecked")
    static private Constraint buildExactTerm(CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, Map<String, Column> columnMap)
    {
        String functionName = FTSTerm.NAME;
        Function function = factory.getFunction(functionName);
        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
        LiteralArgument larg = factory.createLiteralArgument(FTSTerm.ARG_TERM, DataTypeDefinition.TEXT, getText((List<Tree>) testNode.getChildren()));
        functionArguments.put(larg.getName(), larg);
        larg = factory.createLiteralArgument(FTSTerm.ARG_TOKENISATION_MODE, DataTypeDefinition.ANY, AnalysisMode.IDENTIFIER);
        functionArguments.put(larg.getName(), larg);
        if (fieldReferenceNode != null)
        {
            PropertyArgument parg = buildFieldReference(FTSTerm.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
            functionArguments.put(parg.getName(), parg);
        }
        else
        {
            CommonTree specifiedFieldReferenceNode = findFieldReference(testNode);
            if (specifiedFieldReferenceNode != null)
            {
                PropertyArgument parg = buildFieldReference(FTSTerm.ARG_PROPERTY, specifiedFieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
                functionArguments.put(parg.getName(), parg);
            }
        }
        return factory.createFunctionalConstraint(function, functionArguments);
    }

    @SuppressWarnings("unchecked")
    static private Constraint buildTerm(CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, Map<String, Column> columnMap)
    {
        String functionName = FTSTerm.NAME;
        Function function = factory.getFunction(functionName);
        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
        LiteralArgument larg = factory.createLiteralArgument(FTSTerm.ARG_TERM, DataTypeDefinition.TEXT, getText((List<Tree>) testNode.getChildren()));
        functionArguments.put(larg.getName(), larg);
        larg = factory.createLiteralArgument(FTSTerm.ARG_TOKENISATION_MODE, DataTypeDefinition.ANY, AnalysisMode.DEFAULT);
        functionArguments.put(larg.getName(), larg);
        if (fieldReferenceNode != null)
        {
            PropertyArgument parg = buildFieldReference(FTSTerm.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
            functionArguments.put(parg.getName(), parg);
        }
        else
        {
            CommonTree specifiedFieldReferenceNode = findFieldReference(testNode);
            if (specifiedFieldReferenceNode != null)
            {
                PropertyArgument parg = buildFieldReference(FTSTerm.ARG_PROPERTY, specifiedFieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
                functionArguments.put(parg.getName(), parg);
            }
        }
        return factory.createFunctionalConstraint(function, functionArguments);
    }

    static private CommonTree copyAndReplace(CommonTree source, CommonTree insert)
    {
        CommonTree newNode = new CommonTree(source);
        if (source.getChildCount() > 0)
        {
            for (Object current : source.getChildren())
            {
                CommonTree child = (CommonTree) current;
                if (child.getType() == FTSParser.TEMPLATE)
                {
                    if (child.getChildCount() > 1)
                    {
                        CommonTree disjunction = new CommonTree(new DisjunctionToken());
                        newNode.addChild(disjunction);
                        for (Object currentfieldReferenceNode : child.getChildren())
                        {
                            CommonTree fieldReferenceNode = (CommonTree) currentfieldReferenceNode;
                            CommonTree def = new CommonTree(new DefaultToken());
                            disjunction.addChild(def);
                            CommonTree newChild = insertTreeAndFixFieldRefs(insert, fieldReferenceNode);
                            def.addChild(newChild);
                        }

                    }
                    else
                    {
                        CommonTree fieldReferenceNode = findFieldReference(child);
                        CommonTree newChild = insertTreeAndFixFieldRefs(insert, fieldReferenceNode);
                        newNode.addChild(newChild);
                    }
                }
                else
                {
                    CommonTree newChild = copyAndReplace(child, insert);
                    newNode.addChild(newChild);
                }
            }
        }
        return newNode;
    }

    static class DisjunctionToken implements Token
    {

        public int getChannel()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getCharPositionInLine()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public CharStream getInputStream()
        {
            // TODO Auto-generated method stub
            return null;
        }

        public int getLine()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getText()
        {
            // TODO Auto-generated method stub
            return null;
        }

        public int getTokenIndex()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getType()
        {
            return FTSParser.DISJUNCTION;
        }

        public void setChannel(int arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setCharPositionInLine(int arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setInputStream(CharStream arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setLine(int arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setText(String arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setTokenIndex(int arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setType(int arg0)
        {
            // TODO Auto-generated method stub

        }

    }
    
    static class WordToken implements Token
    {
    	String text;
    	
    	WordToken(String text)
    	{
    		this.text = text;
    	}
    	
        public int getChannel()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getCharPositionInLine()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public CharStream getInputStream()
        {
            // TODO Auto-generated method stub
            return null;
        }

        public int getLine()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getText()
        {
            return text;
        }

        public int getTokenIndex()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getType()
        {
            return FTSParser.FTSWORD;
        }

        public void setChannel(int arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setCharPositionInLine(int arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setInputStream(CharStream arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setLine(int arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setText(String arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setTokenIndex(int arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setType(int arg0)
        {
            // TODO Auto-generated method stub

        }

    }
    
    static class PhraseToken implements Token
    {
        public int getChannel()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getCharPositionInLine()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public CharStream getInputStream()
        {
            // TODO Auto-generated method stub
            return null;
        }

        public int getLine()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getText()
        {
            return null;
        }

        public int getTokenIndex()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getType()
        {
            return FTSParser.PHRASE;
        }

        public void setChannel(int arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setCharPositionInLine(int arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setInputStream(CharStream arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setLine(int arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setText(String arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setTokenIndex(int arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setType(int arg0)
        {
            // TODO Auto-generated method stub

        }

    }
    
    static class ConjunctionToken implements Token
    {

        public int getChannel()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getCharPositionInLine()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public CharStream getInputStream()
        {
            // TODO Auto-generated method stub
            return null;
        }

        public int getLine()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getText()
        {
            // TODO Auto-generated method stub
            return null;
        }

        public int getTokenIndex()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getType()
        {
            return FTSParser.CONJUNCTION;
        }

        public void setChannel(int arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setCharPositionInLine(int arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setInputStream(CharStream arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setLine(int arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setText(String arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setTokenIndex(int arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setType(int arg0)
        {
            // TODO Auto-generated method stub

        }

    }

    static class DefaultToken implements Token
    {

        public int getChannel()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getCharPositionInLine()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public CharStream getInputStream()
        {
            // TODO Auto-generated method stub
            return null;
        }

        public int getLine()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getText()
        {
            // TODO Auto-generated method stub
            return null;
        }

        public int getTokenIndex()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getType()
        {
            return FTSParser.DEFAULT;
        }

        public void setChannel(int arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setCharPositionInLine(int arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setInputStream(CharStream arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setLine(int arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setText(String arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setTokenIndex(int arg0)
        {
            // TODO Auto-generated method stub

        }

        public void setType(int arg0)
        {
            // TODO Auto-generated method stub

        }

    }

    static private CommonTree copy(CommonTree source)
    {
        CommonTree newNode = new CommonTree(source);
        if (source.getChildCount() > 0)
        {
            for (Object current : source.getChildren())
            {
                CommonTree child = (CommonTree) current;
                CommonTree newChild = copy(child);
                newNode.addChild(newChild);
            }
        }
        return newNode;
    }

    private static CommonTree insertTreeAndFixFieldRefs(CommonTree source, CommonTree fieldReferenceNode)
    {
        boolean foundRef = false;
        CommonTree newNode = new CommonTree(source);
        if (source.getChildCount() > 0)
        {
            for (Object current : source.getChildren())
            {
                CommonTree child = (CommonTree) current;
                if (child.getType() == FTSParser.FIELD_REF)
                {
                    CommonTree newChild = copy(fieldReferenceNode);
                    newNode.addChild(newChild);
                    foundRef = true;
                }
                else
                {
                    CommonTree newChild = insertTreeAndFixFieldRefs(child, fieldReferenceNode);
                    newNode.addChild(newChild);
                }
            }
        }
        // if no ref found and it should have one we are creating one in place of the default
        if(!foundRef && hasOptionalFieldReference(source))
        {
            CommonTree newChild = copy(fieldReferenceNode);
            newNode.addChild(newChild);      
        }
        return newNode;
    }

    private static boolean hasOptionalFieldReference(CommonTree node)
    {
        switch (node.getType())
        {
        case FTSParser.TERM:
        case FTSParser.EXACT_TERM:
        case FTSParser.PHRASE:
        case FTSParser.EXACT_PHRASE:
        case FTSParser.SYNONYM:
        case FTSParser.PROXIMITY:
        case FTSParser.RANGE:
            return true;
        default:
            return false;
        }
    }
    
    private static boolean isStarWithNoField(CommonTree testNode)
    {
        if (testNode.getType() == FTSParser.TERM && testNode.getChildCount() == 1)
        {
            CommonTree child = (CommonTree) testNode.getChild(0);
            if (child.getType() == FTSParser.STAR)
            {
                return true;
            }
        }
        return false;
    }
    
    static private Constraint buildFuzzyTerm(Float fuzzy, CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory,
            FunctionEvaluationContext functionEvaluationContext, Selector selector, Map<String, Column> columnMap)
    {
        String functionName = FTSFuzzyTerm.NAME;
        Function function = factory.getFunction(functionName);
        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
        LiteralArgument larg = factory.createLiteralArgument(FTSFuzzyTerm.ARG_TERM, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
        functionArguments.put(larg.getName(), larg);
        larg = factory.createLiteralArgument(FTSFuzzyTerm.ARG_MIN_SIMILARITY, DataTypeDefinition.FLOAT, fuzzy);
        functionArguments.put(larg.getName(), larg);
        if (fieldReferenceNode != null)
        {
            PropertyArgument parg = buildFieldReference(FTSFuzzyTerm.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
            functionArguments.put(parg.getName(), parg);
        }
        else
        {
            CommonTree specifiedFieldReferenceNode = findFieldReference(testNode);
            if (specifiedFieldReferenceNode != null)
            {
                PropertyArgument parg = buildFieldReference(FTSFuzzyTerm.ARG_PROPERTY, specifiedFieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
                functionArguments.put(parg.getName(), parg);
            }
        }
        return factory.createFunctionalConstraint(function, functionArguments);
    }

    @SuppressWarnings("unchecked")
    static private Constraint buildWildTerm(CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, Map<String, Column> columnMap)
    {
        String functionName = FTSWildTerm.NAME;
        Function function = factory.getFunction(functionName);
        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
        LiteralArgument larg = factory.createLiteralArgument(FTSWildTerm.ARG_TERM, DataTypeDefinition.TEXT, getText((List<Tree>) testNode.getChildren()));
        functionArguments.put(larg.getName(), larg);
        larg = factory.createLiteralArgument(FTSPhrase.ARG_TOKENISATION_MODE, DataTypeDefinition.ANY, AnalysisMode.DEFAULT);
        functionArguments.put(larg.getName(), larg);
        if (fieldReferenceNode != null)
        {
            PropertyArgument parg = buildFieldReference(FTSWildTerm.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
            functionArguments.put(parg.getName(), parg);
        }
        else
        {
            CommonTree specifiedFieldReferenceNode = findFieldReference(testNode);
            if (specifiedFieldReferenceNode != null)
            {
                PropertyArgument parg = buildFieldReference(FTSWildTerm.ARG_PROPERTY, specifiedFieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
                functionArguments.put(parg.getName(), parg);
            }
        }
        return factory.createFunctionalConstraint(function, functionArguments);
    }
    
    @SuppressWarnings("unchecked")
    static private Constraint buildExactWildTerm(CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, Map<String, Column> columnMap)
    {
        String functionName = FTSWildTerm.NAME;
        Function function = factory.getFunction(functionName);
        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
        LiteralArgument larg = factory.createLiteralArgument(FTSWildTerm.ARG_TERM, DataTypeDefinition.TEXT, getText((List<Tree>) testNode.getChildren()));
        functionArguments.put(larg.getName(), larg);
        larg = factory.createLiteralArgument(FTSPhrase.ARG_TOKENISATION_MODE, DataTypeDefinition.ANY, AnalysisMode.IDENTIFIER);
        functionArguments.put(larg.getName(), larg);
        if (fieldReferenceNode != null)
        {
            PropertyArgument parg = buildFieldReference(FTSWildTerm.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
            functionArguments.put(parg.getName(), parg);
        }
        else
        {
            CommonTree specifiedFieldReferenceNode = findFieldReference(testNode);
            if (specifiedFieldReferenceNode != null)
            {
                PropertyArgument parg = buildFieldReference(FTSWildTerm.ARG_PROPERTY, specifiedFieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
                functionArguments.put(parg.getName(), parg);
            }
        }
        return factory.createFunctionalConstraint(function, functionArguments);
    }

    @SuppressWarnings("unchecked")
    static private Constraint buildPrefixTerm(CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, Map<String, Column> columnMap)
    {
        String functionName = FTSPrefixTerm.NAME;
        Function function = factory.getFunction(functionName);
        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
        LiteralArgument larg = factory.createLiteralArgument(FTSPrefixTerm.ARG_TERM, DataTypeDefinition.TEXT, getText((List<Tree>) testNode.getChildren()));
        functionArguments.put(larg.getName(), larg);
        larg = factory.createLiteralArgument(FTSPhrase.ARG_TOKENISATION_MODE, DataTypeDefinition.ANY, AnalysisMode.DEFAULT);
        functionArguments.put(larg.getName(), larg);
        if (fieldReferenceNode != null)
        {
            PropertyArgument parg = buildFieldReference(FTSPrefixTerm.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
            functionArguments.put(parg.getName(), parg);
        }
        else
        {
            CommonTree specifiedFieldReferenceNode = findFieldReference(testNode);
            if (specifiedFieldReferenceNode != null)
            {
                PropertyArgument parg = buildFieldReference(FTSPrefixTerm.ARG_PROPERTY, specifiedFieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
                functionArguments.put(parg.getName(), parg);
            }
        }
        return factory.createFunctionalConstraint(function, functionArguments);
    }
    
    @SuppressWarnings("unchecked")
    static private Constraint buildExactPrefixTerm(CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, Map<String, Column> columnMap)
    {
        String functionName = FTSPrefixTerm.NAME;
        Function function = factory.getFunction(functionName);
        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
        LiteralArgument larg = factory.createLiteralArgument(FTSPrefixTerm.ARG_TERM, DataTypeDefinition.TEXT, getText((List<Tree>) testNode.getChildren()));
        functionArguments.put(larg.getName(), larg);
        larg = factory.createLiteralArgument(FTSPhrase.ARG_TOKENISATION_MODE, DataTypeDefinition.ANY, AnalysisMode.IDENTIFIER);
        functionArguments.put(larg.getName(), larg);
        if (fieldReferenceNode != null)
        {
            PropertyArgument parg = buildFieldReference(FTSPrefixTerm.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
            functionArguments.put(parg.getName(), parg);
        }
        else
        {
            CommonTree specifiedFieldReferenceNode = findFieldReference(testNode);
            if (specifiedFieldReferenceNode != null)
            {
                PropertyArgument parg = buildFieldReference(FTSPrefixTerm.ARG_PROPERTY, specifiedFieldReferenceNode, factory, functionEvaluationContext, selector, columnMap);
                functionArguments.put(parg.getName(), parg);
            }
        }
        return factory.createFunctionalConstraint(function, functionArguments);
    }

    static private CommonTree findFieldReference(CommonTree node)
    {
        for (int i = 0, l = node.getChildCount(); i < l; i++)
        {
            CommonTree child = (CommonTree) node.getChild(i);
            if (child.getType() == FTSParser.FIELD_REF)
            {
                return child;
            }
        }
        return null;
    }

    static private Float findFuzzy(Tree node)
    {
        for (int i = 0, l = node.getChildCount(); i < l; i++)
        {
            CommonTree child = (CommonTree) node.getChild(i);
            if (child.getType() == FTSParser.FUZZY)
            {
                String fuzzyString = child.getChild(0).getText();
                float fuzzy = Float.parseFloat(fuzzyString);
                return Float.valueOf(fuzzy);
            }
        }
        return null;
    }

    static public PropertyArgument buildFieldReference(String argumentName, CommonTree fieldReferenceNode, QueryModelFactory factory,
            FunctionEvaluationContext functionEvaluationContext, Selector selector, Map<String, Column> columnMap)
    {
        if (fieldReferenceNode.getType() != FTSParser.FIELD_REF)
        {
            throw new FTSQueryException("Not column ref  ..." + fieldReferenceNode.getText());
        }
        String fieldName = getText(fieldReferenceNode.getChild(0));
        if (columnMap != null)
        {
            for (Column column : columnMap.values())
            {
                if (column.getAlias().equals(fieldName))
                {
                    // TODO: Check selector matches ...
                    PropertyArgument arg = (PropertyArgument) column.getFunctionArguments().get(PropertyAccessor.ARG_PROPERTY);
                    fieldName = arg.getPropertyName();
                    break;
                }
            }
        }

        // prepend prefixes and name spaces

        if (fieldReferenceNode.getChildCount() > 1)
        {
            CommonTree child = (CommonTree) fieldReferenceNode.getChild(1);
            if (child.getType() == FTSParser.PREFIX)
            {
                fieldName = getText(child.getChild(0)) + ":" + fieldName;
            }
            else if (child.getType() == FTSParser.NAME_SPACE)
            {
                fieldName = getText(child.getChild(0)) + fieldName;
            }
        }

        String alias = "";
        if (selector != null)
        {
            functionEvaluationContext.checkFieldApplies(selector, fieldName);
            alias = selector.getAlias();
        }

        return factory.createPropertyArgument(argumentName, functionEvaluationContext.isQueryable(fieldName), functionEvaluationContext.isOrderable(fieldName), alias, fieldName);
    }

    static private String getText(List<? extends Object> nodes)
    {
        StringBuilder builder = new StringBuilder();
        for(Object node : nodes)
        {            
            builder.append(getText((Tree)node, false));
        }
        return builder.toString();
    }
    
    static private String getText(Tree node)
    {
        return getText(node, true);
    }
    
    static private String getText(Tree node, boolean returnTextFromUnknownNodes)
    {
        String text = node.getText();
        int index;
        switch (node.getType())
        {
        case FTSParser.FTSWORD:
        case FTSParser.FTSPRE:
        case FTSParser.FTSWILD:
            index = text.indexOf('\\');
            if (index == -1)
            {
                return text;
            }
            else
            {
                return unescape(text);
            }
        case FTSParser.FTSPHRASE:
            String phrase = text.substring(1, text.length() - 1);
            index = phrase.indexOf('\\');
            if (index == -1)
            {
                return phrase;
            }
            else
            {
                return unescape(phrase);
            }
        case FTSParser.ID:
            index = text.indexOf('\\');
            if (index == -1)
            {
                return ISO9075.decode(text);
            }
            else
            {
                return ISO9075.decode(unescape(text));
            }
        case FTSParser.URI:
        case FTSParser.OR:
        case FTSParser.AND:
        case FTSParser.NOT:
        case FTSParser.TILDA:
        case FTSParser.PLUS:
        case FTSParser.MINUS:
        case FTSParser.COLON:
        case FTSParser.STAR:
        case FTSParser.DOTDOT:
        case FTSParser.DOT:
        case FTSParser.AMP:
        case FTSParser.EXCLAMATION:
        case FTSParser.BAR:
        case FTSParser.EQUALS:
        case FTSParser.QUESTION_MARK:
        case FTSParser.TO:
        case FTSParser.COMMA:
        case FTSParser.CARAT:
        case FTSParser.DOLLAR:
        case FTSParser.AT:
        case FTSParser.PERCENT:
        case FTSParser.DECIMAL_INTEGER_LITERAL:
        case FTSParser.FLOATING_POINT_LITERAL:
        case FTSParser.DATETIME:
            return text;
        default:
            if(returnTextFromUnknownNodes)
            {
                return text;
            }
            else
            {
                return "";
            }
        }
    }

    static private String unescape(String string)
    {
        StringBuilder builder = new StringBuilder(string.length());
        boolean lastWasEscape = false;

        for (int i = 0; i < string.length(); i++)
        {
            char c = string.charAt(i);
            if (lastWasEscape)
            {
                if (c == 'u')
                {
                    if((i+4) < string.length())
                    {
                        char encoded = (char)Integer.parseInt(string.substring(i+1, i+5), 16);
                        builder.append(encoded);
                        i += 4;
                    }
                    else
                    {
                        throw new UnsupportedOperationException(string);
                    }
                }
                else
                {
                    builder.append(c);
                }
                lastWasEscape = false;
            }
            else
            {
                if (c == '\\')
                {
                    lastWasEscape = true;
                }
                else
                {
                    builder.append(c);
                }
            }
        }
        if (lastWasEscape)
        {
            throw new FTSQueryException("Escape character at end of string " + string);
        }

        return builder.toString();
    }
}
