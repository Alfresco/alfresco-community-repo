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
package org.alfresco.repo.search.impl.parsers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.Function;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.LiteralArgument;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.repo.search.impl.querymodel.QueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.Selector;
import org.alfresco.repo.search.impl.querymodel.Constraint.Occur;
import org.alfresco.repo.search.impl.querymodel.QueryOptions.Connective;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSExactTerm;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSExpandTerm;
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
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.apache.lucene.search.function.FieldScoreQuery;

public class FTSQueryParser
{
    public Constraint buildFTS(String ftsExpression, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext, Selector selector, ArrayList<Column> columns,
            Connective defaultConnective, Connective defaultFieldConnective)
    {
        // TODO: Decode sql escape for '' should do in CMIS layer
        FTSParser parser = null;
        try
        {
            CharStream cs = new ANTLRStringStream(ftsExpression);
            FTSLexer lexer = new FTSLexer(cs);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            parser = new FTSParser(tokens);
            parser.setDefaultConjunction(defaultConnective == Connective.AND ? true : false);
            parser.setDefaultFieldConjunction(defaultFieldConnective == Connective.AND ? true : false);
            CommonTree ftsNode = (CommonTree) parser.ftsQuery().getTree();
            if (ftsNode.getType() == FTSParser.CONJUNCTION)
            {
                return buildFTSConjunction(null, ftsNode, factory, functionEvaluationContext, selector, columns);
            }
            else
            {
                return buildFTSDisjunction(null, ftsNode, factory, functionEvaluationContext, selector, columns);
            }
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

    private Constraint buildFTSDisjunction(CommonTree fieldReferenceNode, CommonTree orNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, ArrayList<Column> columns)
    {
        if ((orNode.getType() != FTSParser.DISJUNCTION) && (orNode.getType() != FTSParser.FIELD_DISJUNCTION))
        {
            throw new FTSQueryException("Not disjunction " + orNode.getText());
        }
        List<Constraint> constraints = new ArrayList<Constraint>(orNode.getChildCount());
        CommonTree testNode;
        for (int i = 0; i < orNode.getChildCount(); i++)
        {
            CommonTree subNode = (CommonTree) orNode.getChild(i);
            Constraint constraint;
            switch (subNode.getType())
            {
            case FTSParser.DISJUNCTION:
            case FTSParser.FIELD_DISJUNCTION:
                constraint = buildFTSDisjunction(fieldReferenceNode, subNode, factory, functionEvaluationContext, selector, columns);
                break;
            case FTSParser.CONJUNCTION:
            case FTSParser.FIELD_CONJUNCTION:
                constraint = buildFTSConjunction(fieldReferenceNode, subNode, factory, functionEvaluationContext, selector, columns);
                break;
            case FTSParser.NEGATION:
            case FTSParser.FIELD_NEGATION:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.EXCLUDE);
                break;
            case FTSParser.DEFAULT:
            case FTSParser.FIELD_DEFAULT:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.DEFAULT);
                break;
            case FTSParser.MANDATORY:
            case FTSParser.FIELD_MANDATORY:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.MANDATORY);
                break;
            case FTSParser.OPTIONAL:
            case FTSParser.FIELD_OPTIONAL:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.OPTIONAL);
                break;
            case FTSParser.EXCLUDE:
            case FTSParser.FIELD_EXCLUDE:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.EXCLUDE);
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
            return factory.createDisjunction(constraints);
        }
    }

    private Constraint buildFTSConjunction(CommonTree fieldReferenceNode, CommonTree andNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, ArrayList<Column> columns)
    {
        if ((andNode.getType() != FTSParser.CONJUNCTION) && (andNode.getType() != FTSParser.FIELD_CONJUNCTION))
        {
            throw new FTSQueryException("Not conjunction ..." + andNode.getText());
        }
        List<Constraint> constraints = new ArrayList<Constraint>(andNode.getChildCount());
        CommonTree testNode;
        for (int i = 0; i < andNode.getChildCount(); i++)
        {
            CommonTree subNode = (CommonTree) andNode.getChild(i);
            Constraint constraint;
            switch (subNode.getType())
            {
            case FTSParser.DISJUNCTION:
            case FTSParser.FIELD_DISJUNCTION:
                constraint = buildFTSDisjunction(fieldReferenceNode, subNode, factory, functionEvaluationContext, selector, columns);
                setBoost(constraint, subNode);
                break;
            case FTSParser.CONJUNCTION:
            case FTSParser.FIELD_CONJUNCTION:
                constraint = buildFTSConjunction(fieldReferenceNode, subNode, factory, functionEvaluationContext, selector, columns);
                setBoost(constraint, subNode);
                break;
            case FTSParser.NEGATION:
            case FTSParser.FIELD_NEGATION:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.EXCLUDE);
                setBoost(constraint, subNode);
                break;
            case FTSParser.DEFAULT:
            case FTSParser.FIELD_DEFAULT:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.DEFAULT);
                setBoost(constraint, subNode);
                break;
            case FTSParser.MANDATORY:
            case FTSParser.FIELD_MANDATORY:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.MANDATORY);
                setBoost(constraint, subNode);
                break;
            case FTSParser.OPTIONAL:
            case FTSParser.FIELD_OPTIONAL:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.OPTIONAL);
                setBoost(constraint, subNode);
                break;
            case FTSParser.EXCLUDE:
            case FTSParser.FIELD_EXCLUDE:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
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
            return factory.createConjunction(constraints);
        }
    }

    private void setBoost(Constraint constraint, CommonTree subNode)
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

    private Constraint buildFTSTest(CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, ArrayList<Column> columns)
    {
        Tree termNode;
        Float fuzzy = findFuzzy(testNode);
        switch (testNode.getType())
        {
        case FTSParser.DISJUNCTION:
        case FTSParser.FIELD_DISJUNCTION:
            return buildFTSDisjunction(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
        case FTSParser.CONJUNCTION:
        case FTSParser.FIELD_CONJUNCTION:
            return buildFTSConjunction(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
        case FTSParser.TERM:
        case FTSParser.FG_TERM:
            termNode = testNode.getChild(0);
            if (fuzzy == null)
            {
                switch (termNode.getType())
                {
                case FTSParser.FTSPRE:
                    return buildPrefixTerm(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
                case FTSParser.FTSWILD:
                    return buildWildTerm(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
                default:
                    return buildTerm(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
                }
            }
            else
            {
                switch (termNode.getType())
                {
                case FTSParser.FTSPRE:
                case FTSParser.FTSWILD:
                    throw new FTSQueryException("Fuzzy queries are not supported with wild cards");
                default:
                    return buildFuzzyTerm(fuzzy, fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
                }
            }
        case FTSParser.EXACT_TERM:
        case FTSParser.FG_EXACT_TERM:
            termNode = testNode.getChild(0);
            switch (termNode.getType())
            {
            case FTSParser.FTSPRE:
                return buildPrefixTerm(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
            case FTSParser.FTSWILD:
                return buildWildTerm(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
            default:
                return buildExactTerm(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
            }
        case FTSParser.PHRASE:
        case FTSParser.FG_PHRASE:
            return buildPhrase(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
        case FTSParser.SYNONYM:
        case FTSParser.FG_SYNONYM:
            termNode = testNode.getChild(0);
            switch (termNode.getType())
            {
            case FTSParser.FTSPRE:
                return buildPrefixTerm(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
            case FTSParser.FTSWILD:
                return buildWildTerm(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
            default:
                return buildExpandTerm(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
            }
        case FTSParser.PROXIMITY:
        case FTSParser.FG_PROXIMITY:
            return buildProximity(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
        case FTSParser.RANGE:
        case FTSParser.FG_RANGE:
            return buildRange(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
        case FTSParser.FIELD_GROUP:
            if (fieldReferenceNode != null)
            {
                throw new IllegalStateException("Already in field?");
            }
            CommonTree newFieldReferenceNode = (CommonTree) testNode.getChild(0);
            CommonTree fieldExperssion = (CommonTree) testNode.getChild(1);
            if (fieldExperssion.getType() == FTSParser.FIELD_CONJUNCTION)
            {
                return buildFTSConjunction(newFieldReferenceNode, fieldExperssion, factory, functionEvaluationContext, selector, columns);
            }
            else
            {
                return buildFTSDisjunction(newFieldReferenceNode, fieldExperssion, factory, functionEvaluationContext, selector, columns);
            }
        default:
            throw new FTSQueryException("Unsupported FTS option " + testNode.getText());
        }
    }

    private Constraint buildRange(CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, ArrayList<Column> columns)
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
            PropertyArgument parg = buildFieldReference(FTSRange.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columns);
            functionArguments.put(parg.getName(), parg);
        }
        else
        {
            CommonTree specifiedFieldReferenceNode = findFieldReference(testNode);
            if (specifiedFieldReferenceNode != null)
            {
                PropertyArgument parg = buildFieldReference(FTSRange.ARG_PROPERTY, specifiedFieldReferenceNode, factory, functionEvaluationContext, selector, columns);
                functionArguments.put(parg.getName(), parg);
            }
        }
        return factory.createFunctionalConstraint(function, functionArguments);
    }

    private Constraint buildProximity(CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, ArrayList<Column> columns)
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
            PropertyArgument parg = buildFieldReference(FTSProximity.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columns);
            functionArguments.put(parg.getName(), parg);
        }
        return factory.createFunctionalConstraint(function, functionArguments);
    }

    private Constraint buildExpandTerm(CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, ArrayList<Column> columns)
    {
        String functionName = FTSExpandTerm.NAME;
        Function function = factory.getFunction(functionName);
        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
        LiteralArgument larg = factory.createLiteralArgument(FTSExpandTerm.ARG_TERM, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
        functionArguments.put(larg.getName(), larg);
        if (fieldReferenceNode != null)
        {
            PropertyArgument parg = buildFieldReference(FTSExpandTerm.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columns);
            functionArguments.put(parg.getName(), parg);
        }
        else
        {
            CommonTree specifiedFieldReferenceNode = findFieldReference(testNode);
            if (specifiedFieldReferenceNode != null)
            {
                PropertyArgument parg = buildFieldReference(FTSExpandTerm.ARG_PROPERTY, specifiedFieldReferenceNode, factory, functionEvaluationContext, selector, columns);
                functionArguments.put(parg.getName(), parg);
            }
        }
        return factory.createFunctionalConstraint(function, functionArguments);
    }

    private Constraint buildPhrase(CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, ArrayList<Column> columns)
    {
        String functionName = FTSPhrase.NAME;
        Function function = factory.getFunction(functionName);
        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
        LiteralArgument larg = factory.createLiteralArgument(FTSPhrase.ARG_PHRASE, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
        functionArguments.put(larg.getName(), larg);
        if (fieldReferenceNode != null)
        {
            PropertyArgument parg = buildFieldReference(FTSPhrase.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columns);
            functionArguments.put(parg.getName(), parg);
        }
        else
        {
            CommonTree specifiedFieldReferenceNode = findFieldReference(testNode);
            if (specifiedFieldReferenceNode != null)
            {
                PropertyArgument parg = buildFieldReference(FTSPhrase.ARG_PROPERTY, specifiedFieldReferenceNode, factory, functionEvaluationContext, selector, columns);
                functionArguments.put(parg.getName(), parg);
            }
        }
        return factory.createFunctionalConstraint(function, functionArguments);
    }

    private Constraint buildExactTerm(CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, ArrayList<Column> columns)
    {
        String functionName = FTSExactTerm.NAME;
        Function function = factory.getFunction(functionName);
        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
        LiteralArgument larg = factory.createLiteralArgument(FTSExactTerm.ARG_TERM, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
        functionArguments.put(larg.getName(), larg);
        if (fieldReferenceNode != null)
        {
            PropertyArgument parg = buildFieldReference(FTSExactTerm.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columns);
            functionArguments.put(parg.getName(), parg);
        }
        else
        {
            CommonTree specifiedFieldReferenceNode = findFieldReference(testNode);
            if (specifiedFieldReferenceNode != null)
            {
                PropertyArgument parg = buildFieldReference(FTSExactTerm.ARG_PROPERTY, specifiedFieldReferenceNode, factory, functionEvaluationContext, selector, columns);
                functionArguments.put(parg.getName(), parg);
            }
        }
        return factory.createFunctionalConstraint(function, functionArguments);
    }

    private Constraint buildTerm(CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, ArrayList<Column> columns)
    {
        String functionName = FTSTerm.NAME;
        Function function = factory.getFunction(functionName);
        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
        LiteralArgument larg = factory.createLiteralArgument(FTSTerm.ARG_TERM, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
        functionArguments.put(larg.getName(), larg);
        if (fieldReferenceNode != null)
        {
            PropertyArgument parg = buildFieldReference(FTSTerm.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columns);
            functionArguments.put(parg.getName(), parg);
        }
        else
        {
            CommonTree specifiedFieldReferenceNode = findFieldReference(testNode);
            if (specifiedFieldReferenceNode != null)
            {
                PropertyArgument parg = buildFieldReference(FTSTerm.ARG_PROPERTY, specifiedFieldReferenceNode, factory, functionEvaluationContext, selector, columns);
                functionArguments.put(parg.getName(), parg);
            }
        }
        return factory.createFunctionalConstraint(function, functionArguments);
    }
    
    private Constraint buildFuzzyTerm(Float fuzzy, CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, ArrayList<Column> columns)
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
            PropertyArgument parg = buildFieldReference(FTSFuzzyTerm.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columns);
            functionArguments.put(parg.getName(), parg);
        }
        else
        {
            CommonTree specifiedFieldReferenceNode = findFieldReference(testNode);
            if (specifiedFieldReferenceNode != null)
            {
                PropertyArgument parg = buildFieldReference(FTSFuzzyTerm.ARG_PROPERTY, specifiedFieldReferenceNode, factory, functionEvaluationContext, selector, columns);
                functionArguments.put(parg.getName(), parg);
            }
        }
        return factory.createFunctionalConstraint(function, functionArguments);
    }

    private Constraint buildWildTerm(CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, ArrayList<Column> columns)
    {
        String functionName = FTSWildTerm.NAME;
        Function function = factory.getFunction(functionName);
        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
        LiteralArgument larg = factory.createLiteralArgument(FTSWildTerm.ARG_TERM, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
        functionArguments.put(larg.getName(), larg);
        if (fieldReferenceNode != null)
        {
            PropertyArgument parg = buildFieldReference(FTSWildTerm.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columns);
            functionArguments.put(parg.getName(), parg);
        }
        else
        {
            CommonTree specifiedFieldReferenceNode = findFieldReference(testNode);
            if (specifiedFieldReferenceNode != null)
            {
                PropertyArgument parg = buildFieldReference(FTSWildTerm.ARG_PROPERTY, specifiedFieldReferenceNode, factory, functionEvaluationContext, selector, columns);
                functionArguments.put(parg.getName(), parg);
            }
        }
        return factory.createFunctionalConstraint(function, functionArguments);
    }

    private Constraint buildPrefixTerm(CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, ArrayList<Column> columns)
    {
        String functionName = FTSPrefixTerm.NAME;
        Function function = factory.getFunction(functionName);
        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
        LiteralArgument larg = factory.createLiteralArgument(FTSPrefixTerm.ARG_TERM, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
        functionArguments.put(larg.getName(), larg);
        if (fieldReferenceNode != null)
        {
            PropertyArgument parg = buildFieldReference(FTSPrefixTerm.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columns);
            functionArguments.put(parg.getName(), parg);
        }
        else
        {
            CommonTree specifiedFieldReferenceNode = findFieldReference(testNode);
            if (specifiedFieldReferenceNode != null)
            {
                PropertyArgument parg = buildFieldReference(FTSPrefixTerm.ARG_PROPERTY, specifiedFieldReferenceNode, factory, functionEvaluationContext, selector, columns);
                functionArguments.put(parg.getName(), parg);
            }
        }
        return factory.createFunctionalConstraint(function, functionArguments);
    }

    private CommonTree findFieldReference(CommonTree node)
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

    private Float findFuzzy(Tree node)
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

    public PropertyArgument buildFieldReference(String argumentName, CommonTree fieldReferenceNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, ArrayList<Column> columns)
    {
        if (fieldReferenceNode.getType() != FTSParser.FIELD_REF)
        {
            throw new FTSQueryException("Not column ref  ..." + fieldReferenceNode.getText());
        }
        String fieldName = getText(fieldReferenceNode.getChild(0));
        if (columns != null)
        {
            for (Column column : columns)
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
            alias = selector.getAlias();
        }

        return factory.createPropertyArgument(argumentName, functionEvaluationContext.isQueryable(fieldName), functionEvaluationContext.isOrderable(fieldName), alias, fieldName);
    }

    private String getText(Tree node)
    {
        String text = node.getText();
        int index;
        switch (node.getType())
        {
        case FTSParser.FTSWORD:
        case FTSParser.PHRASE:
            index = text.indexOf('\\');
            if (index == -1)
            {
                return text;
            }
            else
            {
                return unescape(text);
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
        default:
            return text;
        }
    }

    private String unescape(String string)
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
                    throw new UnsupportedOperationException(string);
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
