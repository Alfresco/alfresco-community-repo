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
package org.alfresco.cmis.search.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.cmis.dictionary.CMISCardinality;
import org.alfresco.cmis.dictionary.CMISDictionaryService;
import org.alfresco.cmis.dictionary.CMISMapping;
import org.alfresco.cmis.dictionary.CMISPropertyDefinition;
import org.alfresco.cmis.dictionary.CMISScope;
import org.alfresco.cmis.dictionary.CMISTypeId;
import org.alfresco.cmis.search.CMISQueryException;
import org.alfresco.cmis.search.CMISQueryOptions;
import org.alfresco.cmis.search.JoinSupport;
import org.alfresco.repo.search.impl.parsers.CMISLexer;
import org.alfresco.repo.search.impl.parsers.CMISParser;
import org.alfresco.repo.search.impl.parsers.FTSLexer;
import org.alfresco.repo.search.impl.parsers.FTSParser;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.ArgumentDefinition;
import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.Function;
import org.alfresco.repo.search.impl.querymodel.JoinType;
import org.alfresco.repo.search.impl.querymodel.Order;
import org.alfresco.repo.search.impl.querymodel.Ordering;
import org.alfresco.repo.search.impl.querymodel.Query;
import org.alfresco.repo.search.impl.querymodel.QueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.Selector;
import org.alfresco.repo.search.impl.querymodel.Source;
import org.alfresco.repo.search.impl.querymodel.impl.BaseComparison;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Child;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Descendant;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Equals;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Exists;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSExactTerm;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSPhrase;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSTerm;
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
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;

/**
 * @author andyh
 */
public class CMISQueryParser
{
    private CMISQueryOptions options;

    private CMISDictionaryService cmisDictionaryService;

    private CMISMapping cmisMapping;

    private JoinSupport joinSupport;

    public CMISQueryParser(CMISQueryOptions options, CMISDictionaryService cmisDictionaryService, CMISMapping cmisMapping, JoinSupport joinSupport)
    {
        this.options = options;
        this.cmisDictionaryService = cmisDictionaryService;
        this.cmisMapping = cmisMapping;
        this.joinSupport = joinSupport;
    }

    public Query parse(QueryModelFactory factory)
    {
        CMISParser parser = null;
        try
        {
            CharStream cs = new ANTLRStringStream(options.getQuery());
            CMISLexer lexer = new CMISLexer(cs);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            parser = new CMISParser(tokens);
            CommonTree queryNode = (CommonTree) parser.query().getTree();

            CommonTree sourceNode = (CommonTree) queryNode.getFirstChildWithType(CMISParser.SOURCE);
            Source source = buildSource(sourceNode, joinSupport, factory);
            Map<String, Selector> selectors = source.getSelectors();
            ArrayList<Column> columns = buildColumns(queryNode, factory, selectors);
            
            HashSet<String> columnNames = new HashSet<String>();
            for(Column column : columns)
            {
                if(!columnNames.add(column.getAlias()))
                {
                    throw new CMISQueryException("Duplicate column alias for "+column.getAlias());
                }
            }

            ArrayList<Ordering> orderings = buildOrderings(queryNode, factory, selectors, columns);

            Constraint constraint = null;
            CommonTree orNode = (CommonTree) queryNode.getFirstChildWithType(CMISParser.DISJUNCTION);
            if (orNode != null)
            {
                constraint = buildDisjunction(orNode, factory, selectors, columns);
            }

            Query query = factory.createQuery(columns, source, constraint, orderings);

            // TODO: validate query and use of ID, function arguments matching up etc

            return query;
        }
        catch (RecognitionException e)
        {
            if (parser != null)
            {
                String[] tokenNames = parser.getTokenNames();
                String hdr = parser.getErrorHeader(e);
                String msg = parser.getErrorMessage(e, tokenNames);
                throw new CMISQueryException(hdr + "\n" + msg, e);
            }
        }
        throw new CMISQueryException("Failed to parse");
    }

    /**
     * @param queryNode
     * @param factory
     * @param selectors
     * @param columns
     * @return
     */
    private Constraint buildDisjunction(CommonTree orNode, QueryModelFactory factory, Map<String, Selector> selectors, ArrayList<Column> columns)
    {
        List<Constraint> constraints = new ArrayList<Constraint>(orNode.getChildCount());
        for (int i = 0; i < orNode.getChildCount(); i++)
        {
            CommonTree andNode = (CommonTree) orNode.getChild(i);
            Constraint constraint = buildConjunction(andNode, factory, selectors, columns);
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

    /**
     * @param andNode
     * @param factory
     * @param selectors
     * @param columns
     * @return
     */
    private Constraint buildConjunction(CommonTree andNode, QueryModelFactory factory, Map<String, Selector> selectors, ArrayList<Column> columns)
    {
        List<Constraint> constraints = new ArrayList<Constraint>(andNode.getChildCount());
        for (int i = 0; i < andNode.getChildCount(); i++)
        {
            CommonTree notNode = (CommonTree) andNode.getChild(i);
            Constraint constraint = buildNegation(notNode, factory, selectors, columns);
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

    /**
     * @param notNode
     * @param factory
     * @param selectors
     * @param columns
     * @return
     */
    private Constraint buildNegation(CommonTree notNode, QueryModelFactory factory, Map<String, Selector> selectors, ArrayList<Column> columns)
    {
        if (notNode.getType() == CMISParser.NEGATION)
        {
            Constraint constraint = buildTest(notNode, factory, selectors, columns);
            return factory.createNegation(constraint);
        }
        else
        {
            return buildTest(notNode, factory, selectors, columns);
        }
    }

    /**
     * @param notNode
     * @param factory
     * @param selectors
     * @param columns
     * @return
     */
    private Constraint buildTest(CommonTree testNode, QueryModelFactory factory, Map<String, Selector> selectors, ArrayList<Column> columns)
    {
        if (testNode.getType() == CMISParser.DISJUNCTION)
        {
            return buildDisjunction(testNode, factory, selectors, columns);
        }
        else
        {
            return buildPredicate(testNode, factory, selectors, columns);
        }
    }

    /**
     * @param orNode
     * @param factory
     * @param selectors
     * @param columns
     * @return
     */
    private Constraint buildPredicate(CommonTree predicateNode, QueryModelFactory factory, Map<String, Selector> selectors, ArrayList<Column> columns)
    {
        String functionName;
        Function function;
        CommonTree argNode;
        Map<String, Argument> functionArguments;
        Argument arg;
        switch (predicateNode.getType())
        {
        case CMISParser.PRED_CHILD:
            functionName = Child.NAME;
            function = factory.getFunction(functionName);
            functionArguments = new LinkedHashMap<String, Argument>();
            argNode = (CommonTree) predicateNode.getChild(0);
            arg = getFunctionArgument(argNode, function.getArgumentDefinition(Child.ARG_PARENT), factory, selectors);
            functionArguments.put(arg.getName(), arg);
            if (predicateNode.getChildCount() > 1)
            {
                arg = getFunctionArgument(argNode, function.getArgumentDefinition(Child.ARG_SELECTOR), factory, selectors);
                functionArguments.put(arg.getName(), arg);
            }
            return factory.createFunctionalConstraint(function, functionArguments);
        case CMISParser.PRED_COMPARISON:
            switch (predicateNode.getChild(2).getType())
            {
            case CMISParser.EQUALS:
                functionName = Equals.NAME;
                function = factory.getFunction(functionName);
                break;
            case CMISParser.NOTEQUALS:
                functionName = NotEquals.NAME;
                function = factory.getFunction(functionName);
                break;
            case CMISParser.GREATERTHAN:
                functionName = GreaterThan.NAME;
                function = factory.getFunction(functionName);
                break;
            case CMISParser.GREATERTHANOREQUALS:
                functionName = GreaterThanOrEquals.NAME;
                function = factory.getFunction(functionName);
                break;
            case CMISParser.LESSTHAN:
                functionName = LessThan.NAME;
                function = factory.getFunction(functionName);
                break;
            case CMISParser.LESSTHANOREQUALS:
                functionName = LessThanOrEquals.NAME;
                function = factory.getFunction(functionName);
                break;
            default:
                throw new CMISQueryException("Unknown comparison function " + predicateNode.getChild(2).getText());
            }
            functionArguments = new LinkedHashMap<String, Argument>();
            argNode = (CommonTree) predicateNode.getChild(1);
            arg = getFunctionArgument(argNode, function.getArgumentDefinition(BaseComparison.ARG_LHS), factory, selectors);
            functionArguments.put(arg.getName(), arg);
            argNode = (CommonTree) predicateNode.getChild(3);
            arg = getFunctionArgument(argNode, function.getArgumentDefinition(BaseComparison.ARG_RHS), factory, selectors);
            functionArguments.put(arg.getName(), arg);
            return factory.createFunctionalConstraint(function, functionArguments);
        case CMISParser.PRED_DESCENDANT:
            functionName = Descendant.NAME;
            function = factory.getFunction(functionName);
            argNode = (CommonTree) predicateNode.getChild(0);
            functionArguments = new LinkedHashMap<String, Argument>();
            arg = getFunctionArgument(argNode, function.getArgumentDefinition(Child.ARG_PARENT), factory, selectors);
            functionArguments.put(arg.getName(), arg);
            if (predicateNode.getChildCount() > 1)
            {
                arg = getFunctionArgument(argNode, function.getArgumentDefinition(Child.ARG_SELECTOR), factory, selectors);
                functionArguments.put(arg.getName(), arg);
            }
            return factory.createFunctionalConstraint(function, functionArguments);
        case CMISParser.PRED_EXISTS:
            functionName = Exists.NAME;
            function = factory.getFunction(functionName);
            argNode = (CommonTree) predicateNode.getChild(0);
            functionArguments = new LinkedHashMap<String, Argument>();
            arg = getFunctionArgument(argNode, function.getArgumentDefinition(Exists.ARG_PROPERTY), factory, selectors);
            functionArguments.put(arg.getName(), arg);
            arg = factory.createLiteralArgument(Exists.ARG_NOT, DataTypeDefinition.BOOLEAN, (predicateNode.getChildCount() == 1));
            functionArguments.put(arg.getName(), arg);
            return factory.createFunctionalConstraint(function, functionArguments);
        case CMISParser.PRED_FTS:
            String ftsExpression = predicateNode.getChild(0).getText();
            return buildFTS(ftsExpression, factory, selectors, columns);
        case CMISParser.PRED_IN:
            functionName = In.NAME;
            function = factory.getFunction(functionName);
            functionArguments = new LinkedHashMap<String, Argument>();
            argNode = (CommonTree) predicateNode.getChild(0);
            arg = getFunctionArgument(argNode, function.getArgumentDefinition(In.ARG_PROPERTY), factory, selectors);
            functionArguments.put(arg.getName(), arg);
            argNode = (CommonTree) predicateNode.getChild(1);
            arg = getFunctionArgument(argNode, function.getArgumentDefinition(In.ARG_COLLECTION), factory, selectors);
            functionArguments.put(arg.getName(), arg);
            arg = factory.createLiteralArgument(In.ARG_NOT, DataTypeDefinition.BOOLEAN, (predicateNode.getChildCount() > 2));
            functionArguments.put(arg.getName(), arg);
            return factory.createFunctionalConstraint(function, functionArguments);
        case CMISParser.PRED_LIKE:
            functionName = Like.NAME;
            function = factory.getFunction(functionName);
            functionArguments = new LinkedHashMap<String, Argument>();
            argNode = (CommonTree) predicateNode.getChild(0);
            arg = getFunctionArgument(argNode, function.getArgumentDefinition(Like.ARG_PROPERTY), factory, selectors);
            functionArguments.put(arg.getName(), arg);
            argNode = (CommonTree) predicateNode.getChild(1);
            arg = getFunctionArgument(argNode, function.getArgumentDefinition(Like.ARG_EXP), factory, selectors);
            functionArguments.put(arg.getName(), arg);
            arg = factory.createLiteralArgument(Like.ARG_NOT, DataTypeDefinition.BOOLEAN, (predicateNode.getChildCount() > 2));
            functionArguments.put(arg.getName(), arg);
            return factory.createFunctionalConstraint(function, functionArguments);
        default:
            return null;
        }
    }

    private Constraint buildFTS(String ftsExpression, QueryModelFactory factory, Map<String, Selector> selectors, ArrayList<Column> columns)
    {
        // TODO: transform '' to ' to reverse encoding
        FTSParser parser = null;
        try
        {
            CharStream cs = new ANTLRStringStream(ftsExpression);
            FTSLexer lexer = new FTSLexer(cs);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            parser = new FTSParser(tokens);
            CommonTree ftsNode = (CommonTree) parser.fts().getTree();
            if (ftsNode.getType() == FTSParser.CONJUNCTION)
            {
                return buildFTSConjunction(ftsNode, factory, selectors, columns);
            }
            else
            {
                return buildFTSDisjunction(ftsNode, factory, selectors, columns);
            }
        }
        catch (RecognitionException e)
        {
            if (parser != null)
            {
                String[] tokenNames = parser.getTokenNames();
                String hdr = parser.getErrorHeader(e);
                String msg = parser.getErrorMessage(e, tokenNames);
                throw new CMISQueryException(hdr + "\n" + msg, e);
            }
            return null;
        }

    }

    private Constraint buildFTSDisjunction(CommonTree orNode, QueryModelFactory factory, Map<String, Selector> selectors, ArrayList<Column> columns)
    {
        List<Constraint> constraints = new ArrayList<Constraint>(orNode.getChildCount());
        for (int i = 0; i < orNode.getChildCount(); i++)
        {
            CommonTree andNode = (CommonTree) orNode.getChild(i);
            Constraint constraint = buildFTSConjunction(andNode, factory, selectors, columns);
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

    private Constraint buildFTSConjunction(CommonTree andNode, QueryModelFactory factory, Map<String, Selector> selectors, ArrayList<Column> columns)
    {
        List<Constraint> constraints = new ArrayList<Constraint>(andNode.getChildCount());
        for (int i = 0; i < andNode.getChildCount(); i++)
        {
            CommonTree notNode = (CommonTree) andNode.getChild(i);
            Constraint constraint = buildFTSNegation(notNode, factory, selectors, columns);
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

    private Constraint buildFTSNegation(CommonTree notNode, QueryModelFactory factory, Map<String, Selector> selectors, ArrayList<Column> columns)
    {
        if (notNode.getType() == FTSParser.NEGATION)
        {
            Constraint constraint = buildFTSTest(notNode, factory, selectors, columns);
            return factory.createNegation(constraint);
        }
        else
        {
            return buildFTSTest(notNode, factory, selectors, columns);
        }
    }

    private Constraint buildFTSTest(CommonTree testNode, QueryModelFactory factory, Map<String, Selector> selectors, ArrayList<Column> columns)
    {
        String functionName;
        Function function;
        Map<String, Argument> functionArguments;
        Argument arg;
        switch (testNode.getType())
        {
        case FTSParser.DISJUNCTION:
            return buildFTSDisjunction(testNode, factory, selectors, columns);
        case FTSParser.CONJUNCTION:
            return buildFTSConjunction(testNode, factory, selectors, columns);
        case FTSParser.TERM:
            functionName = FTSTerm.NAME;
            function = factory.getFunction(functionName);
            functionArguments = new LinkedHashMap<String, Argument>();
            arg = factory.createLiteralArgument(FTSTerm.ARG_TERM, DataTypeDefinition.TEXT, testNode.getChild(0).getText());
            functionArguments.put(arg.getName(), arg);
            if (testNode.getChildCount() > 1)
            {
                arg = buildColumnReference(FTSTerm.ARG_PROPERTY, (CommonTree) testNode.getChild(1), factory);
                functionArguments.put(arg.getName(), arg);
            }
            return factory.createFunctionalConstraint(function, functionArguments);
        case FTSParser.EXACT_TERM:
            functionName = FTSExactTerm.NAME;
            function = factory.getFunction(functionName);
            functionArguments = new LinkedHashMap<String, Argument>();
            arg = factory.createLiteralArgument(FTSExactTerm.ARG_TERM, DataTypeDefinition.TEXT, testNode.getChild(0).getText());
            functionArguments.put(arg.getName(), arg);
            if (testNode.getChildCount() > 1)
            {
                arg = buildColumnReference(FTSExactTerm.ARG_PROPERTY, (CommonTree) testNode.getChild(1), factory);
                functionArguments.put(arg.getName(), arg);
            }
            return factory.createFunctionalConstraint(function, functionArguments);
        case FTSParser.PHRASE:
            // TODO: transform "" to " to reverse escaping
            functionName = FTSPhrase.NAME;
            function = factory.getFunction(functionName);
            functionArguments = new LinkedHashMap<String, Argument>();
            arg = factory.createLiteralArgument(FTSPhrase.ARG_PHRASE, DataTypeDefinition.TEXT, testNode.getChild(0).getText());
            functionArguments.put(arg.getName(), arg);
            if (testNode.getChildCount() > 1)
            {
                arg = buildColumnReference(FTSPhrase.ARG_PROPERTY, (CommonTree) testNode.getChild(1), factory);
                functionArguments.put(arg.getName(), arg);
            }
            return factory.createFunctionalConstraint(function, functionArguments);
        case FTSParser.SYNONYM:
        case FTSParser.FG_PROXIMITY:
        case FTSParser.FG_RANGE:
        case FTSParser.FIELD_GROUP:
        case FTSParser.FIELD_CONJUNCTION:
        case FTSParser.FIELD_DISJUNCTION:
        default:
            throw new CMISQueryException("Unsupported FTS option " + testNode.getText());
        }
    }

    /**
     * @param queryNode
     * @param factory
     * @param selectors
     * @return
     */
    private ArrayList<Ordering> buildOrderings(CommonTree queryNode, QueryModelFactory factory, Map<String, Selector> selectors, List<Column> columns)
    {
        ArrayList<Ordering> orderings = new ArrayList<Ordering>();
        CommonTree orderNode = (CommonTree) queryNode.getFirstChildWithType(CMISParser.ORDER);
        if (orderNode != null)
        {
            for (int i = 0; i < orderNode.getChildCount(); i++)
            {
                CommonTree current = (CommonTree) orderNode.getChild(i);

                CommonTree columnRefNode = (CommonTree) current.getFirstChildWithType(CMISParser.COLUMN_REF);
                if (columnRefNode != null)
                {
                    String columnName = columnRefNode.getChild(0).getText();
                    String qualifier = "";
                    if (columnRefNode.getChildCount() > 1)
                    {
                        qualifier = columnRefNode.getChild(1).getText();
                    }

                    Order order = Order.ASCENDING;

                    if (current.getChild(1).getType() == CMISParser.DESC)
                    {
                        order = Order.DESCENDING;
                    }

                    Column orderColumn = null;

                    if (qualifier.length() == 0)
                    {
                        Column match = null;
                        for (Column column : columns)
                        {
                            if (column.getAlias().equals(columnName))
                            {
                                match = column;
                                break;
                            }
                        }
                        if (match == null)
                        {

                            Selector selector = selectors.get(qualifier);
                            if (selector == null)
                            {
                                throw new CMISQueryException("No selector for " + qualifier);
                            }
                            QName cmisType = cmisMapping.getCmisType(selector.getType());
                            CMISTypeId typeId = null;
                            if (cmisMapping.isValidCmisDocument(cmisType))
                            {
                                typeId = cmisMapping.getCmisTypeId(CMISScope.DOCUMENT, cmisType);
                            }
                            else if (cmisMapping.isValidCmisFolder(cmisType))
                            {
                                typeId = cmisMapping.getCmisTypeId(CMISScope.FOLDER, cmisType);
                            }
                            else
                            {
                                throw new CMISQueryException("Type unsupported in CMIS queries: " + selector.getAlias());
                            }
                            CMISPropertyDefinition definition = cmisDictionaryService.getPropertyDefinition(typeId, columnName);

                            if (definition == null)
                            {
                                throw new CMISQueryException("Invalid column for " + cmisMapping.getQueryName(typeId.getQName()) + "." + columnName);
                            }

                            Function function = factory.getFunction(PropertyAccessor.NAME);
                            QName propertyQName = cmisMapping.getPropertyQName(definition.getPropertyName());
                            Argument arg = factory.createPropertyArgument(PropertyAccessor.ARG_PROPERTY, selector.getAlias(), propertyQName);
                            Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
                            functionArguments.put(arg.getName(), arg);

                            String alias = (selector.getAlias().length() > 0) ? selector.getAlias() + "." + definition.getPropertyName() : definition.getPropertyName();

                            match = factory.createColumn(function, functionArguments, alias);
                        }
                        orderColumn = match;
                    }
                    else
                    {
                        Selector selector = selectors.get(qualifier);
                        if (selector == null)
                        {
                            throw new CMISQueryException("No selector for " + qualifier);
                        }
                        QName cmisType = cmisMapping.getCmisType(selector.getType());
                        CMISTypeId typeId = null;
                        if (cmisMapping.isValidCmisDocument(cmisType))
                        {
                            typeId = cmisMapping.getCmisTypeId(CMISScope.DOCUMENT, cmisType);
                        }
                        else if (cmisMapping.isValidCmisFolder(cmisType))
                        {
                            typeId = cmisMapping.getCmisTypeId(CMISScope.FOLDER, cmisType);
                        }
                        else
                        {
                            throw new CMISQueryException("Type unsupported in CMIS queries: " + selector.getAlias());
                        }
                        CMISPropertyDefinition definition = cmisDictionaryService.getPropertyDefinition(typeId, columnName);

                        if (definition == null)
                        {
                            throw new CMISQueryException("Invalid column for "
                                    + cmisMapping.getQueryName(typeId.getQName()) + "." + columnName + " selector alias " + selector.getAlias());
                        }

                        Function function = factory.getFunction(PropertyAccessor.NAME);
                        QName propertyQName = cmisMapping.getPropertyQName(definition.getPropertyName());
                        Argument arg = factory.createPropertyArgument(PropertyAccessor.ARG_PROPERTY, selector.getAlias(), propertyQName);
                        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
                        functionArguments.put(arg.getName(), arg);

                        String alias = (selector.getAlias().length() > 0) ? selector.getAlias() + "." + definition.getPropertyName() : definition.getPropertyName();

                        orderColumn = factory.createColumn(function, functionArguments, alias);
                    }

                    // TODO: check orderable - add to the column definition

                    Ordering ordering = factory.createOrdering(orderColumn, order);
                    orderings.add(ordering);

                }
            }
        }
        return orderings;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<Column> buildColumns(CommonTree queryNode, QueryModelFactory factory, Map<String, Selector> selectors)
    {
        ArrayList<Column> columns = new ArrayList<Column>();
        CommonTree starNode = (CommonTree) queryNode.getFirstChildWithType(CMISParser.ALL_COLUMNS);
        if (starNode != null)
        {
            for (Selector selector : selectors.values())
            {
                QName cmisType = cmisMapping.getCmisType(selector.getType());
                CMISTypeId typeId = null;
                if (cmisMapping.isValidCmisDocument(cmisType))
                {
                    typeId = cmisMapping.getCmisTypeId(CMISScope.DOCUMENT, cmisType);
                }
                else if (cmisMapping.isValidCmisFolder(cmisType))
                {
                    typeId = cmisMapping.getCmisTypeId(CMISScope.FOLDER, cmisType);
                }
                else
                {
                    throw new CMISQueryException("Type unsupported in CMIS queries: " + selector.getAlias());
                }
                Map<String, CMISPropertyDefinition> propDefs = cmisDictionaryService.getPropertyDefinitions(typeId);
                for (CMISPropertyDefinition definition : propDefs.values())
                {
                    if (definition.getCardinality() == CMISCardinality.SINGLE_VALUED)
                    {
                        Function function = factory.getFunction(PropertyAccessor.NAME);
                        QName propertyQName = cmisMapping.getPropertyQName(definition.getPropertyName());
                        Argument arg = factory.createPropertyArgument(PropertyAccessor.ARG_PROPERTY, selector.getAlias(), propertyQName);
                        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
                        functionArguments.put(arg.getName(), arg);
                        String alias = (selector.getAlias().length() > 0) ? selector.getAlias() + "." + definition.getPropertyName() : definition.getPropertyName();
                        Column column = factory.createColumn(function, functionArguments, alias);
                        columns.add(column);
                    }
                }
            }
        }

        CommonTree columnsNode = (CommonTree) queryNode.getFirstChildWithType(CMISParser.COLUMNS);
        if (columnsNode != null)
        {
            for (CommonTree columnNode : (List<CommonTree>)columnsNode.getChildren())
            {
                if(columnNode.getType() == CMISParser.ALL_COLUMNS)
                {
                    String qualifier = columnNode.getChild(0).getText();
                    Selector selector = selectors.get(qualifier);
                    if (selector == null)
                    {
                        throw new CMISQueryException("No selector for " + qualifier + " in " + qualifier + ".*");
                    }
                    QName cmisType = cmisMapping.getCmisType(selector.getType());
                    CMISTypeId typeId = null;
                    if (cmisMapping.isValidCmisDocument(cmisType))
                    {
                        typeId = cmisMapping.getCmisTypeId(CMISScope.DOCUMENT, cmisType);
                    }
                    else if (cmisMapping.isValidCmisFolder(cmisType))
                    {
                        typeId = cmisMapping.getCmisTypeId(CMISScope.FOLDER, cmisType);
                    }
                    else
                    {
                        throw new CMISQueryException("Type unsupported in CMIS queries: " + selector.getAlias());
                    }
                    Map<String, CMISPropertyDefinition> propDefs = cmisDictionaryService.getPropertyDefinitions(typeId);
                    for (CMISPropertyDefinition definition : propDefs.values())
                    {
                        if (definition.getCardinality() == CMISCardinality.SINGLE_VALUED)
                        {
                            Function function = factory.getFunction(PropertyAccessor.NAME);
                            QName propertyQName = cmisMapping.getPropertyQName(definition.getPropertyName());
                            Argument arg = factory.createPropertyArgument(PropertyAccessor.ARG_PROPERTY, selector.getAlias(), propertyQName);
                            Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
                            functionArguments.put(arg.getName(), arg);
                            String alias = (selector.getAlias().length() > 0) ? selector.getAlias() + "." + definition.getPropertyName() : definition.getPropertyName();
                            Column column = factory.createColumn(function, functionArguments, alias);
                            columns.add(column);
                        }
                    }
                }

                if(columnNode.getType() == CMISParser.COLUMN)
                {
                    CommonTree columnRefNode = (CommonTree) columnNode.getFirstChildWithType(CMISParser.COLUMN_REF);
                    if (columnRefNode != null)
                    {
                        String columnName = columnRefNode.getChild(0).getText();
                        String qualifier = "";
                        if (columnRefNode.getChildCount() > 1)
                        {
                            qualifier = columnRefNode.getChild(1).getText();
                        }
                        Selector selector = selectors.get(qualifier);
                        if (selector == null)
                        {
                            throw new CMISQueryException("No selector for " + qualifier);
                        }
                        QName cmisType = cmisMapping.getCmisType(selector.getType());
                        CMISTypeId typeId = null;
                        if (cmisMapping.isValidCmisDocument(cmisType))
                        {
                            typeId = cmisMapping.getCmisTypeId(CMISScope.DOCUMENT, cmisType);
                        }
                        else if (cmisMapping.isValidCmisFolder(cmisType))
                        {
                            typeId = cmisMapping.getCmisTypeId(CMISScope.FOLDER, cmisType);
                        }
                        else
                        {
                            throw new CMISQueryException("Type unsupported in CMIS queries: " + selector.getAlias());
                        }
                        CMISPropertyDefinition definition = cmisDictionaryService.getPropertyDefinition(typeId, columnName);

                        if (definition == null)
                        {
                            throw new CMISQueryException("Invalid column for " + cmisMapping.getQueryName(typeId.getQName()) + "." + columnName);
                        }

                        Function function = factory.getFunction(PropertyAccessor.NAME);
                        QName propertyQName = cmisMapping.getPropertyQName(definition.getPropertyName());
                        Argument arg = factory.createPropertyArgument(PropertyAccessor.ARG_PROPERTY, selector.getAlias(), propertyQName);
                        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
                        functionArguments.put(arg.getName(), arg);

                        String alias = (selector.getAlias().length() > 0) ? selector.getAlias() + "." + definition.getPropertyName() : definition.getPropertyName();
                        if (columnNode.getChildCount() > 1)
                        {
                            alias = columnNode.getChild(1).getText();
                        }

                        Column column = factory.createColumn(function, functionArguments, alias);
                        columns.add(column);

                    }

                    CommonTree functionNode = (CommonTree) columnNode.getFirstChildWithType(CMISParser.FUNCTION);
                    if (functionNode != null)
                    {
                        String functionName = getFunctionName((CommonTree) functionNode.getChild(0));
                        Function function = factory.getFunction(functionName);
                        Collection<ArgumentDefinition> definitions = function.getArgumentDefinitions().values();
                        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();

                        int childIndex = 1;
                        for (ArgumentDefinition definition : definitions)
                        {
                            if (functionNode.getChildCount() > childIndex)
                            {
                                CommonTree argNode = (CommonTree) functionNode.getChild(childIndex++);
                                Argument arg = getFunctionArgument(argNode, definition, factory, selectors);
                                functionArguments.put(arg.getName(), arg);
                            }
                            else
                            {
                                if (definition.isMandatory())
                                {
                                    // throw new CMISQueryException("Insufficient aruments for function " +
                                    // ((CommonTree)
                                    // functionNode.getChild(0)).getText() );
                                    break;
                                }
                                else
                                {
                                    // ok
                                }
                            }
                        }

                        String alias = function.getName();
                        if (columnNode.getChildCount() > 1)
                        {
                            alias = columnNode.getChild(1).getText();
                        }

                        Column column = factory.createColumn(function, functionArguments, alias);
                        columns.add(column);
                    }
                }
            }
        }

        return columns;
    }

    private Argument getFunctionArgument(CommonTree argNode, ArgumentDefinition definition, QueryModelFactory factory, Map<String, Selector> selectors)
    {
        if (argNode.getType() == CMISParser.COLUMN_REF)
        {
            Argument arg = buildColumnReference(definition.getName(), argNode, factory);
            return arg;
        }
        else if (argNode.getType() == CMISParser.ID)
        {
            Argument arg;
            String id = argNode.getText();
            if (selectors.containsKey(id))
            {
                arg = factory.createSelectorArgument(definition.getName(), id);
            }
            else
            {
                QName propertyQName = cmisMapping.getPropertyQName(id);
                arg = factory.createPropertyArgument(definition.getName(), "", propertyQName);
            }
            return arg;
        }
        else if (argNode.getType() == CMISParser.PARAMETER)
        {
            Argument arg = factory.createParameterArgument(definition.getName(), argNode.getText());
            return arg;
        }
        else if (argNode.getType() == CMISParser.NUMERIC_LITERAL)
        {
            CommonTree literalNode = (CommonTree) argNode.getChild(0);
            if (literalNode.getType() == CMISParser.FLOATING_POINT_LITERAL)
            {
                QName type = DataTypeDefinition.DOUBLE;
                Number value = Double.parseDouble(literalNode.getText());
                if (value.floatValue() == value.doubleValue())
                {
                    type = DataTypeDefinition.FLOAT;
                    value = Float.valueOf(value.floatValue());
                }
                Argument arg = factory.createLiteralArgument(definition.getName(), type, value);
                return arg;
            }
            else if (literalNode.getType() == CMISParser.DECIMAL_INTEGER_LITERAL)
            {
                QName type = DataTypeDefinition.LONG;
                Number value = Long.parseLong(literalNode.getText());
                if (value.intValue() == value.longValue())
                {
                    type = DataTypeDefinition.INT;
                    value = Integer.valueOf(value.intValue());
                }
                Argument arg = factory.createLiteralArgument(definition.getName(), type, value);
                return arg;
            }
            else
            {
                throw new CMISQueryException("Invalid numeric literal " + literalNode.getText());
            }
        }
        else if (argNode.getType() == CMISParser.STRING_LITERAL)
        {
            Argument arg = factory.createLiteralArgument(definition.getName(), DataTypeDefinition.TEXT, argNode.getChild(0).getText());
            return arg;
        }
        else if (argNode.getType() == CMISParser.LIST)
        {
            ArrayList<Argument> arguments = new ArrayList<Argument>();
            for (int i = 0; i < argNode.getChildCount(); i++)
            {
                CommonTree arg = (CommonTree) argNode.getChild(i);
                arguments.add(getFunctionArgument(arg, definition, factory, selectors));
            }
            Argument arg = factory.createListArgument(definition.getName(), arguments);
            return arg;
        }
        else if (argNode.getType() == CMISParser.ANY)
        {
            Argument arg = factory.createLiteralArgument(definition.getName(), DataTypeDefinition.TEXT, argNode.getText());
            return arg;
        }
        else if (argNode.getType() == CMISParser.NOT)
        {
            Argument arg = factory.createLiteralArgument(definition.getName(), DataTypeDefinition.TEXT, argNode.getText());
            return arg;
        }
        else if (argNode.getType() == CMISParser.FUNCTION)
        {
            String functionName = getFunctionName((CommonTree) argNode.getChild(0));
            Function function = factory.getFunction(functionName);
            Collection<ArgumentDefinition> definitions = function.getArgumentDefinitions().values();
            Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();

            int childIndex = 1;
            for (ArgumentDefinition currentDefinition : definitions)
            {
                if (argNode.getChildCount() > childIndex)
                {
                    CommonTree currentArgNode = (CommonTree) argNode.getChild(childIndex++);
                    Argument arg = getFunctionArgument(currentArgNode, currentDefinition, factory, selectors);
                    functionArguments.put(arg.getName(), arg);
                }
                else
                {
                    if (definition.isMandatory())
                    {
                        // throw new CMISQueryException("Insufficient aruments for function " + ((CommonTree)
                        // functionNode.getChild(0)).getText() );
                        break;
                    }
                    else
                    {
                        // ok
                    }
                }
            }
            Argument arg = factory.createFunctionArgument(definition.getName(), function, functionArguments);
            return arg;
        }
        else
        {
            throw new CMISQueryException("Invalid function argument " + argNode.getText());
        }
    }

    @SuppressWarnings("unchecked")
    private Source buildSource(CommonTree source, JoinSupport joinSupport, QueryModelFactory factory)
    {
        if (source.getChildCount() == 1)
        {
            // single table reference
            CommonTree singleTableNode = (CommonTree) source.getChild(0);
            if (singleTableNode.getType() == CMISParser.TABLE)
            {
                if (joinSupport == JoinSupport.NO_JOIN_SUPPORT)
                {
                    throw new UnsupportedOperationException("Joins are not supported");
                }
                CommonTree tableSourceNode = (CommonTree) singleTableNode.getFirstChildWithType(CMISParser.SOURCE);
                return buildSource(tableSourceNode, joinSupport, factory);

            }
            if (singleTableNode.getType() != CMISParser.TABLE_REF)
            {
                throw new CMISQueryException("Expecting TABLE_REF token but found " + singleTableNode.getText());
            }
            String tableName = singleTableNode.getChild(0).getText();
            String alias = "";
            if (singleTableNode.getChildCount() > 1)
            {
                alias = singleTableNode.getChild(1).getText();
            }
            QName classQName = cmisMapping.getAlfrescoClassQNameFromCmisTableName(tableName);
            return factory.createSelector(classQName, alias);
        }
        else
        {
            if (joinSupport == JoinSupport.NO_JOIN_SUPPORT)
            {
                throw new UnsupportedOperationException("Joins are not supported");
            }
            CommonTree singleTableNode = (CommonTree) source.getChild(0);
            if (singleTableNode.getType() != CMISParser.TABLE_REF)
            {
                throw new CMISQueryException("Expecting TABLE_REF token but found " + singleTableNode.getText());
            }
            String tableName = singleTableNode.getChild(0).getText();
            String alias = "";
            if (singleTableNode.getChildCount() == 2)
            {
                alias = singleTableNode.getChild(1).getText();
            }
            QName classQName = cmisMapping.getAlfrescoClassQNameFromCmisTableName(tableName);
            Source lhs = factory.createSelector(classQName, alias);

            List<CommonTree> list = (List<CommonTree>) (source.getChildren());
            for (CommonTree joinNode : list)
            {
                if (joinNode.getType() == CMISParser.JOIN)
                {
                    CommonTree rhsSource = (CommonTree) joinNode.getFirstChildWithType(CMISParser.SOURCE);
                    Source rhs = buildSource(rhsSource, joinSupport, factory);

                    JoinType joinType = JoinType.INNER;
                    CommonTree joinTypeNode = (CommonTree) joinNode.getFirstChildWithType(CMISParser.LEFT);
                    if (joinTypeNode != null)
                    {
                        joinType = JoinType.LEFT;
                    }

                    if ((joinType == JoinType.LEFT) && (joinSupport == JoinSupport.INNER_JOIN_SUPPORT))
                    {
                        throw new UnsupportedOperationException("Outer joins are not supported");
                    }

                    Constraint joinCondition = null;
                    CommonTree joinConditionNode = (CommonTree) joinNode.getFirstChildWithType(CMISParser.ON);
                    if (joinConditionNode != null)
                    {
                        Argument arg1 = buildColumnReference(Equals.ARG_LHS, (CommonTree) joinConditionNode.getChild(0), factory);
                        String functionName = getFunctionName((CommonTree) joinConditionNode.getChild(1));
                        Argument arg2 = buildColumnReference(Equals.ARG_RHS, (CommonTree) joinConditionNode.getChild(2), factory);
                        Function function = factory.getFunction(functionName);
                        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
                        functionArguments.put(arg1.getName(), arg1);
                        functionArguments.put(arg2.getName(), arg2);
                        joinCondition = factory.createFunctionalConstraint(function, functionArguments);
                    }

                    Source join = factory.createJoin(lhs, rhs, joinType, joinCondition);
                    lhs = join;
                }
            }

            return lhs;

        }
    }

    public Argument buildColumnReference(String argumentName, CommonTree columnReferenceNode, QueryModelFactory factory)
    {
        String cmisPropertyName = columnReferenceNode.getChild(0).getText();
        String qualifer = "";
        if (columnReferenceNode.getChildCount() > 1)
        {
            qualifer = columnReferenceNode.getChild(1).getText();
        }
        QName propertyQName = cmisMapping.getPropertyQName(cmisPropertyName);
        return factory.createPropertyArgument(argumentName, qualifer, propertyQName);
    }

    public String getFunctionName(CommonTree functionNameNode)
    {
        switch (functionNameNode.getType())
        {
        case CMISParser.EQUALS:
            return Equals.NAME;
        case CMISParser.UPPER:
            return Upper.NAME;
        case CMISParser.SCORE:
            return Score.NAME;
        case CMISParser.LOWER:
            return Lower.NAME;
        default:
            throw new CMISQueryException("Unknown function: " + functionNameNode.getText());
        }
    }
}
