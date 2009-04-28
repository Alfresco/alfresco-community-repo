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
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSPhrase;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSProximity;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSTerm;
import org.alfresco.repo.search.impl.querymodel.impl.functions.PropertyAccessor;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.apache.lucene.search.function.FieldScoreQuery;

public class FTSQueryParser
{
    public Constraint buildFTS(String ftsExpression, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext, Selector selector, ArrayList<Column> columns, Connective defaultConnective, Connective defaultFieldConnective)
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
                return buildFTSConjunction(ftsNode, factory, functionEvaluationContext, selector, columns);
            }
            else
            {
                return buildFTSDisjunction(ftsNode, factory, functionEvaluationContext, selector, columns);
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

    private Constraint buildFTSDisjunction(CommonTree orNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext, Selector selector,
            ArrayList<Column> columns)
    {
        if (orNode.getType() != FTSParser.DISJUNCTION)
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
                constraint = buildFTSDisjunction(subNode, factory, functionEvaluationContext, selector, columns);
                break;
            case FTSParser.CONJUNCTION:
                constraint = buildFTSConjunction(subNode, factory, functionEvaluationContext, selector, columns);
                break;
            case FTSParser.NEGATION:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.EXCLUDE);
                break;
            case FTSParser.DEFAULT:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.DEFAULT);
                break;
            case FTSParser.MANDATORY:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.MANDATORY);
                break;
            case FTSParser.OPTIONAL:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.OPTIONAL);
                break;
            case FTSParser.EXCLUDE:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(testNode, factory, functionEvaluationContext, selector, columns);
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

    private Constraint buildFTSConjunction(CommonTree andNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext, Selector selector,
            ArrayList<Column> columns)
    {
        if (andNode.getType() != FTSParser.CONJUNCTION)
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
                constraint = buildFTSDisjunction(subNode, factory, functionEvaluationContext, selector, columns);
                break;
            case FTSParser.CONJUNCTION:
                constraint = buildFTSConjunction(subNode, factory, functionEvaluationContext, selector, columns);
                break;
            case FTSParser.NEGATION:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.EXCLUDE);
                break;
            case FTSParser.DEFAULT:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.DEFAULT);
                break;
            case FTSParser.MANDATORY:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.MANDATORY);
                break;
            case FTSParser.OPTIONAL:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.OPTIONAL);
                break;
            case FTSParser.EXCLUDE:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(testNode, factory, functionEvaluationContext, selector, columns);
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
            return factory.createConjunction(constraints);
        }
    }

    private Constraint buildFTSTest(CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext, Selector selector,
            ArrayList<Column> columns)
    {
        String functionName;
        Function function;
        Map<String, Argument> functionArguments;
        LiteralArgument larg;
        PropertyArgument parg;
        switch (testNode.getType())
        {
        case FTSParser.DISJUNCTION:
            return buildFTSDisjunction(testNode, factory, functionEvaluationContext, selector, columns);
        case FTSParser.CONJUNCTION:
            return buildFTSConjunction(testNode, factory, functionEvaluationContext, selector, columns);
        case FTSParser.TERM:
            functionName = FTSTerm.NAME;
            function = factory.getFunction(functionName);
            functionArguments = new LinkedHashMap<String, Argument>();
            larg = factory.createLiteralArgument(FTSTerm.ARG_TERM, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
            functionArguments.put(larg.getName(), larg);
            if (testNode.getChildCount() > 1)
            {
                parg = buildFieldReference(FTSTerm.ARG_PROPERTY, (CommonTree) testNode.getChild(1), factory, functionEvaluationContext, selector, columns);
                functionArguments.put(parg.getName(), parg);
            }
            return factory.createFunctionalConstraint(function, functionArguments);
        case FTSParser.EXACT_TERM:
            functionName = FTSExactTerm.NAME;
            function = factory.getFunction(functionName);
            functionArguments = new LinkedHashMap<String, Argument>();
            larg = factory.createLiteralArgument(FTSExactTerm.ARG_TERM, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
            functionArguments.put(larg.getName(), larg);
            if (testNode.getChildCount() > 1)
            {
                parg = buildFieldReference(FTSExactTerm.ARG_PROPERTY, (CommonTree) testNode.getChild(1), factory, functionEvaluationContext, selector, columns);
                functionArguments.put(parg.getName(), parg);
            }
            return factory.createFunctionalConstraint(function, functionArguments);
        case FTSParser.PHRASE:
            // TODO: transform "" to " to reverse escaping
            functionName = FTSPhrase.NAME;
            function = factory.getFunction(functionName);
            functionArguments = new LinkedHashMap<String, Argument>();
            larg = factory.createLiteralArgument(FTSPhrase.ARG_PHRASE, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
            functionArguments.put(larg.getName(), larg);
            if (testNode.getChildCount() > 1)
            {
                parg = buildFieldReference(FTSPhrase.ARG_PROPERTY, (CommonTree) testNode.getChild(1), factory, functionEvaluationContext, selector, columns);
                functionArguments.put(parg.getName(), parg);
            }
            return factory.createFunctionalConstraint(function, functionArguments);
        case FTSParser.SYNONYM:
            functionName = FTSExpandTerm.NAME;
            function = factory.getFunction(functionName);
            functionArguments = new LinkedHashMap<String, Argument>();
            larg = factory.createLiteralArgument(FTSExpandTerm.ARG_TERM, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
            functionArguments.put(larg.getName(), larg);
            if (testNode.getChildCount() > 1)
            {
                parg = buildFieldReference(FTSExpandTerm.ARG_PROPERTY, (CommonTree) testNode.getChild(1), factory, functionEvaluationContext, selector, columns);
                functionArguments.put(parg.getName(), parg);
            }
            return factory.createFunctionalConstraint(function, functionArguments);
        case FTSParser.PROXIMITY:
            functionName = FTSProximity.NAME;
            function = factory.getFunction(functionName);
            functionArguments = new LinkedHashMap<String, Argument>();
            larg = factory.createLiteralArgument(FTSProximity.ARG_FIRST, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
            functionArguments.put(larg.getName(), larg);
            if (testNode.getChild(1).getChildCount() > 0)
            {
                larg = factory.createLiteralArgument(FTSProximity.ARG_SLOP, DataTypeDefinition.INT, getText(testNode.getChild(1).getChild(0)));
                functionArguments.put(larg.getName(), larg);
            }
            larg = factory.createLiteralArgument(FTSProximity.ARG_LAST, DataTypeDefinition.TEXT, getText(testNode.getChild(2)));
            functionArguments.put(larg.getName(), larg);
            return factory.createFunctionalConstraint(function, functionArguments);
        case FTSParser.RANGE:
            throw new FTSQueryException("Unsupported FTS option " + testNode.getText());
        case FTSParser.FIELD_GROUP:
            CommonTree fieldReferenceNode = (CommonTree) testNode.getChild(0);
            CommonTree fieldExperssion = (CommonTree) testNode.getChild(1);
            if (fieldExperssion.getType() == FTSParser.FIELD_CONJUNCTION)
            {
                return buildFTSFieldConjunction(fieldReferenceNode, fieldExperssion, factory, functionEvaluationContext, selector, columns);
            }
            else
            {
                return buildFTSFieldDisjunction(fieldReferenceNode, fieldExperssion, factory, functionEvaluationContext, selector, columns);
            }
        default:
            throw new FTSQueryException("Unsupported FTS option " + testNode.getText());
        }
    }

    private Constraint buildFTSFieldDisjunction(CommonTree fieldReferenceNode, CommonTree orNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, ArrayList<Column> columns)
    {
        if (orNode.getType() != FTSParser.FIELD_DISJUNCTION)
        {
            throw new FTSQueryException("Not field disjunction " + orNode.getText());
        }
        List<Constraint> constraints = new ArrayList<Constraint>(orNode.getChildCount());
        CommonTree testNode;
        for (int i = 0; i < orNode.getChildCount(); i++)
        {
            CommonTree subNode = (CommonTree) orNode.getChild(i);
            Constraint constraint;
            switch (subNode.getType())
            {
            case FTSParser.FIELD_DISJUNCTION:
                constraint = buildFTSFieldDisjunction(fieldReferenceNode, subNode, factory, functionEvaluationContext, selector, columns);
                break;
            case FTSParser.FIELD_CONJUNCTION:
                constraint = buildFTSFieldConjunction(fieldReferenceNode, subNode, factory, functionEvaluationContext, selector, columns);
                break;
            case FTSParser.FIELD_NEGATION:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSFieldTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.EXCLUDE);
                break;
            case FTSParser.FIELD_DEFAULT:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSFieldTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.DEFAULT);
                break;
            case FTSParser.FIELD_MANDATORY:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSFieldTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.MANDATORY);
                break;
            case FTSParser.FIELD_OPTIONAL:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSFieldTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.OPTIONAL);
                break;
            case FTSParser.FIELD_EXCLUDE:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSFieldTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
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

    private Constraint buildFTSFieldConjunction(CommonTree fieldReferenceNode, CommonTree andNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, ArrayList<Column> columns)
    {
        if (andNode.getType() != FTSParser.FIELD_CONJUNCTION)
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
            case FTSParser.FIELD_DISJUNCTION:
                constraint = buildFTSFieldDisjunction(fieldReferenceNode, subNode, factory, functionEvaluationContext, selector, columns);
                break;
            case FTSParser.FIELD_CONJUNCTION:
                constraint = buildFTSFieldConjunction(fieldReferenceNode, subNode, factory, functionEvaluationContext, selector, columns);
                break;
            case FTSParser.FIELD_NEGATION:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSFieldTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.EXCLUDE);
                break;
            case FTSParser.FIELD_DEFAULT:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSFieldTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.DEFAULT);
                break;
            case FTSParser.FIELD_MANDATORY:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSFieldTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.MANDATORY);
                break;
            case FTSParser.FIELD_OPTIONAL:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSFieldTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
                constraint.setOccur(Occur.OPTIONAL);
                break;
            case FTSParser.FIELD_EXCLUDE:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSFieldTest(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
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
            return factory.createConjunction(constraints);
        }
    }

    private Constraint buildFTSFieldTest(CommonTree fieldReferenceNode, CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, ArrayList<Column> columns)
    {
        String functionName;
        Function function;
        Map<String, Argument> functionArguments;
        LiteralArgument larg;
        PropertyArgument parg;
        switch (testNode.getType())
        {
        case FTSParser.FIELD_DISJUNCTION:
            return buildFTSFieldDisjunction(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
        case FTSParser.FIELD_CONJUNCTION:
            return buildFTSFieldConjunction(fieldReferenceNode, testNode, factory, functionEvaluationContext, selector, columns);
        case FTSParser.FG_TERM:
            functionName = FTSTerm.NAME;
            function = factory.getFunction(functionName);
            functionArguments = new LinkedHashMap<String, Argument>();
            larg = factory.createLiteralArgument(FTSTerm.ARG_TERM, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
            functionArguments.put(larg.getName(), larg);
            parg = buildFieldReference(FTSTerm.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columns);
            functionArguments.put(parg.getName(), parg);
            return factory.createFunctionalConstraint(function, functionArguments);
        case FTSParser.FG_EXACT_TERM:
            functionName = FTSExactTerm.NAME;
            function = factory.getFunction(functionName);
            functionArguments = new LinkedHashMap<String, Argument>();
            larg = factory.createLiteralArgument(FTSExactTerm.ARG_TERM, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
            functionArguments.put(larg.getName(), larg);
            parg = buildFieldReference(FTSExactTerm.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columns);
            functionArguments.put(parg.getName(), parg);
            return factory.createFunctionalConstraint(function, functionArguments);
        case FTSParser.FG_PHRASE:
            // TODO: transform "" to " to reverse escaping
            functionName = FTSPhrase.NAME;
            function = factory.getFunction(functionName);
            functionArguments = new LinkedHashMap<String, Argument>();
            larg = factory.createLiteralArgument(FTSPhrase.ARG_PHRASE, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
            functionArguments.put(larg.getName(), larg);
            parg = buildFieldReference(FTSPhrase.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columns);
            functionArguments.put(parg.getName(), parg);
            return factory.createFunctionalConstraint(function, functionArguments);
        case FTSParser.FG_SYNONYM:
            functionName = FTSExpandTerm.NAME;
            function = factory.getFunction(functionName);
            functionArguments = new LinkedHashMap<String, Argument>();
            larg = factory.createLiteralArgument(FTSExpandTerm.ARG_TERM, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
            functionArguments.put(larg.getName(), larg);
            parg = buildFieldReference(FTSExpandTerm.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columns);
            functionArguments.put(parg.getName(), parg);
            return factory.createFunctionalConstraint(function, functionArguments);
        case FTSParser.FG_PROXIMITY:
            functionName = FTSProximity.NAME;
            function = factory.getFunction(functionName);
            functionArguments = new LinkedHashMap<String, Argument>();
            larg = factory.createLiteralArgument(FTSProximity.ARG_FIRST, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
            functionArguments.put(larg.getName(), larg);
            if (testNode.getChild(1).getChildCount() > 0)
            {
                larg = factory.createLiteralArgument(FTSProximity.ARG_SLOP, DataTypeDefinition.INT, getText(testNode.getChild(1).getChild(0)));
                functionArguments.put(larg.getName(), larg);
            }
            larg = factory.createLiteralArgument(FTSProximity.ARG_LAST, DataTypeDefinition.TEXT, getText(testNode.getChild(2)));
            functionArguments.put(larg.getName(), larg);
            parg = buildFieldReference(FTSProximity.ARG_PROPERTY, fieldReferenceNode, factory, functionEvaluationContext, selector, columns);
            functionArguments.put(parg.getName(), parg);
            return factory.createFunctionalConstraint(function, functionArguments);
        case FTSParser.FG_RANGE:
            throw new FTSQueryException("Unsupported FTS option " + testNode.getText());
        default:
            throw new FTSQueryException("Unsupported FTS option " + testNode.getText());
        }
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
        switch (node.getType())
        {
        case FTSParser.FTSWORD:
        case FTSParser.PHRASE:
        case FTSParser.ID:
            int index = text.indexOf('\\');
            if (index == -1)
            {
                return text;
            }
            else
            {
                return unescape(text);
            }
            // break;
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
