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
package org.alfresco.opencmis.search;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.dictionary.PropertyDefintionWrapper;
import org.alfresco.opencmis.dictionary.TypeDefinitionWrapper;
import org.alfresco.opencmis.search.CMISQueryOptions.CMISQueryMode;
import org.alfresco.repo.search.impl.parsers.CMISLexer;
import org.alfresco.repo.search.impl.parsers.CMISParser;
import org.alfresco.repo.search.impl.parsers.FTSParser;
import org.alfresco.repo.search.impl.parsers.FTSQueryException;
import org.alfresco.repo.search.impl.parsers.FTSQueryParser;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.ArgumentDefinition;
import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.Constraint.Occur;
import org.alfresco.repo.search.impl.querymodel.Function;
import org.alfresco.repo.search.impl.querymodel.FunctionArgument;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.JoinType;
import org.alfresco.repo.search.impl.querymodel.ListArgument;
import org.alfresco.repo.search.impl.querymodel.LiteralArgument;
import org.alfresco.repo.search.impl.querymodel.Order;
import org.alfresco.repo.search.impl.querymodel.Ordering;
import org.alfresco.repo.search.impl.querymodel.ParameterArgument;
import org.alfresco.repo.search.impl.querymodel.PredicateMode;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.repo.search.impl.querymodel.Query;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.search.impl.querymodel.QueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.QueryOptions.Connective;
import org.alfresco.repo.search.impl.querymodel.Selector;
import org.alfresco.repo.search.impl.querymodel.SelectorArgument;
import org.alfresco.repo.search.impl.querymodel.Source;
import org.alfresco.repo.search.impl.querymodel.impl.BaseComparison;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Child;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Descendant;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Equals;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Exists;
import org.alfresco.repo.search.impl.querymodel.impl.functions.GreaterThan;
import org.alfresco.repo.search.impl.querymodel.impl.functions.GreaterThanOrEquals;
import org.alfresco.repo.search.impl.querymodel.impl.functions.In;
import org.alfresco.repo.search.impl.querymodel.impl.functions.LessThan;
import org.alfresco.repo.search.impl.querymodel.impl.functions.LessThanOrEquals;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Like;
import org.alfresco.repo.search.impl.querymodel.impl.functions.NotEquals;
import org.alfresco.repo.search.impl.querymodel.impl.functions.PropertyAccessor;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Score;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.CachingDateFormat;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;

/**
 * @author andyh
 */
public class CMISQueryParser
{
    private enum EscapeMode
    {
        LITERAL, LIKE, CONTAINS;
    }

    private CMISQueryOptions options;

    private CMISDictionaryService cmisDictionaryService;

    private CapabilityJoin joinSupport;

    private BaseTypeId[] validScopes;

    private boolean hasScore = false;

    private boolean hasContains = false;

    public CMISQueryParser(CMISQueryOptions options, CMISDictionaryService cmisDictionaryService,
            CapabilityJoin joinSupport)
    {
        this.options = options;
        this.cmisDictionaryService = cmisDictionaryService;
        this.joinSupport = joinSupport;
        this.validScopes = (options.getQueryMode() == CMISQueryMode.CMS_STRICT) ? CmisFunctionEvaluationContext.STRICT_SCOPES
                : CmisFunctionEvaluationContext.ALFRESCO_SCOPES;
    }

    public Query parse(QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext)
    {

        CMISParser parser = null;
        try
        {
            CharStream cs = new ANTLRStringStream(options.getQuery());
            CMISLexer lexer = new CMISLexer(cs);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            parser = new CMISParser(tokens);
            parser.setStrict(options.getQueryMode() == CMISQueryMode.CMS_STRICT);
            CommonTree queryNode = (CommonTree) parser.query().getTree();

            CommonTree sourceNode = (CommonTree) queryNode.getFirstChildWithType(CMISParser.SOURCE);
            Source source = buildSource(sourceNode, joinSupport, factory);
            Map<String, Selector> selectors = source.getSelectors();
            ArrayList<Column> columns = buildColumns(queryNode, factory, selectors, options.getQuery());

            HashMap<String, Column> columnMap = new HashMap<String, Column>();
            for (Column column : columns)
            {
                if (columnMap.containsKey(column.getAlias()))
                {
                    throw new CmisInvalidArgumentException("Duplicate column alias for " + column.getAlias());
                } else
                {
                    columnMap.put(column.getAlias(), column);
                }
            }

            ArrayList<Ordering> orderings = buildOrderings(queryNode, factory, selectors, columns);

            Constraint constraint = null;
            CommonTree orNode = (CommonTree) queryNode.getFirstChildWithType(CMISParser.DISJUNCTION);
            if (orNode != null)
            {
                constraint = buildDisjunction(orNode, factory, functionEvaluationContext, selectors, columnMap);
            }

            Query query = factory.createQuery(columns, source, constraint, orderings);

            // TODO: validate query and use of ID, function arguments matching
            // up etc

            if (options.getQueryMode() == CMISQueryMode.CMS_STRICT)
            {
                if (hasScore && !hasContains)
                {
                    throw new CmisInvalidArgumentException("Function SCORE() used without matching CONTAINS() function");
                }
            }

            return query;
        } catch (RecognitionException e)
        {
            if (parser != null)
            {
                String[] tokenNames = parser.getTokenNames();
                String hdr = parser.getErrorHeader(e);
                String msg = parser.getErrorMessage(e, tokenNames);
                throw new CmisInvalidArgumentException(hdr + "\n" + msg, e);
            }
        }
        throw new CmisInvalidArgumentException("Failed to parse");
    }

    /**
     * @param queryNode
     * @param factory
     * @param selectors
     * @param columns
     * @return
     */
    private Constraint buildDisjunction(CommonTree orNode, QueryModelFactory factory,
            FunctionEvaluationContext functionEvaluationContext, Map<String, Selector> selectors,
            HashMap<String, Column> columnMap)
    {
        List<Constraint> constraints = new ArrayList<Constraint>(orNode.getChildCount());
        for (int i = 0; i < orNode.getChildCount(); i++)
        {
            CommonTree andNode = (CommonTree) orNode.getChild(i);
            Constraint constraint = buildConjunction(andNode, factory, functionEvaluationContext, selectors, columnMap);
            constraints.add(constraint);
        }
        if (constraints.size() == 1)
        {
            return constraints.get(0);
        } else
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
    private Constraint buildConjunction(CommonTree andNode, QueryModelFactory factory,
            FunctionEvaluationContext functionEvaluationContext, Map<String, Selector> selectors,
            HashMap<String, Column> columnMap)
    {
        List<Constraint> constraints = new ArrayList<Constraint>(andNode.getChildCount());
        for (int i = 0; i < andNode.getChildCount(); i++)
        {
            CommonTree notNode = (CommonTree) andNode.getChild(i);
            Constraint constraint = buildNegation(notNode, factory, functionEvaluationContext, selectors, columnMap);
            constraints.add(constraint);
        }
        if (constraints.size() == 1)
        {
            return constraints.get(0);
        } else
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
    private Constraint buildNegation(CommonTree notNode, QueryModelFactory factory,
            FunctionEvaluationContext functionEvaluationContext, Map<String, Selector> selectors,
            HashMap<String, Column> columnMap)
    {
        if (notNode.getType() == CMISParser.NEGATION)
        {
            Constraint constraint = buildTest((CommonTree) notNode.getChild(0), factory, functionEvaluationContext,
                    selectors, columnMap);
            constraint.setOccur(Occur.EXCLUDE);
            return constraint;
        } else
        {
            return buildTest(notNode, factory, functionEvaluationContext, selectors, columnMap);
        }
    }

    /**
     * @param notNode
     * @param factory
     * @param selectors
     * @param columns
     * @return
     */
    private Constraint buildTest(CommonTree testNode, QueryModelFactory factory,
            FunctionEvaluationContext functionEvaluationContext, Map<String, Selector> selectors,
            HashMap<String, Column> columnMap)
    {
        if (testNode.getType() == CMISParser.DISJUNCTION)
        {
            return buildDisjunction(testNode, factory, functionEvaluationContext, selectors, columnMap);
        } else
        {
            return buildPredicate(testNode, factory, functionEvaluationContext, selectors, columnMap);
        }
    }

    /**
     * @param orNode
     * @param factory
     * @param selectors
     * @param columns
     * @return
     */
    private Constraint buildPredicate(CommonTree predicateNode, QueryModelFactory factory,
            FunctionEvaluationContext functionEvaluationContext, Map<String, Selector> selectors,
            Map<String, Column> columnMap)
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
            arg = getFunctionArgument(argNode, function.getArgumentDefinition(Child.ARG_PARENT), factory, selectors,
                    columnMap, false);
            functionArguments.put(arg.getName(), arg);
            if (predicateNode.getChildCount() > 1)
            {
                argNode = (CommonTree) predicateNode.getChild(1);
                arg = getFunctionArgument(argNode, function.getArgumentDefinition(Child.ARG_SELECTOR), factory,
                        selectors, columnMap, false);
                if (!arg.isQueryable())
                {
                    throw new CmisInvalidArgumentException("The property is not queryable: " + argNode.getText());
                }
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
                throw new CmisInvalidArgumentException("Unknown comparison function "
                        + predicateNode.getChild(2).getText());
            }
            functionArguments = new LinkedHashMap<String, Argument>();
            argNode = (CommonTree) predicateNode.getChild(0);
            arg = getFunctionArgument(argNode, function.getArgumentDefinition(BaseComparison.ARG_MODE), factory,
                    selectors, columnMap, false);
            functionArguments.put(arg.getName(), arg);
            argNode = (CommonTree) predicateNode.getChild(1);
            arg = getFunctionArgument(argNode, function.getArgumentDefinition(BaseComparison.ARG_LHS), factory,
                    selectors, columnMap, false);
            functionArguments.put(arg.getName(), arg);
            argNode = (CommonTree) predicateNode.getChild(3);
            arg = getFunctionArgument(argNode, function.getArgumentDefinition(BaseComparison.ARG_RHS), factory,
                    selectors, columnMap, false);
            functionArguments.put(arg.getName(), arg);
            checkPredicateConditionsForComparisons(function, functionArguments, functionEvaluationContext, columnMap);
            return factory.createFunctionalConstraint(function, functionArguments);
        case CMISParser.PRED_DESCENDANT:
            functionName = Descendant.NAME;
            function = factory.getFunction(functionName);
            argNode = (CommonTree) predicateNode.getChild(0);
            functionArguments = new LinkedHashMap<String, Argument>();
            arg = getFunctionArgument(argNode, function.getArgumentDefinition(Descendant.ARG_ANCESTOR), factory,
                    selectors, columnMap, false);
            functionArguments.put(arg.getName(), arg);
            if (predicateNode.getChildCount() > 1)
            {
                argNode = (CommonTree) predicateNode.getChild(1);
                arg = getFunctionArgument(argNode, function.getArgumentDefinition(Descendant.ARG_SELECTOR), factory,
                        selectors, columnMap, false);
                functionArguments.put(arg.getName(), arg);
            }
            return factory.createFunctionalConstraint(function, functionArguments);
        case CMISParser.PRED_EXISTS:
            functionName = Exists.NAME;
            function = factory.getFunction(functionName);
            argNode = (CommonTree) predicateNode.getChild(0);
            functionArguments = new LinkedHashMap<String, Argument>();
            arg = getFunctionArgument(argNode, function.getArgumentDefinition(Exists.ARG_PROPERTY), factory, selectors,
                    columnMap, false);
            functionArguments.put(arg.getName(), arg);
            arg = factory.createLiteralArgument(Exists.ARG_NOT, DataTypeDefinition.BOOLEAN,
                    (predicateNode.getChildCount() > 1));
            functionArguments.put(arg.getName(), arg);
            // Applies to both single valued and multi-valued properties - no
            // checks required
            return factory.createFunctionalConstraint(function, functionArguments);
        case CMISParser.PRED_FTS:
            if (options.getQueryMode() == CMISQueryMode.CMS_STRICT)
            {
                if (hasContains)
                {
                    throw new CmisInvalidArgumentException(
                            "Only one CONTAINS() function can be included in a single query statement.");
                }
            }
            String ftsExpression = predicateNode.getChild(0).getText();
            ftsExpression = ftsExpression.substring(1, ftsExpression.length() - 1);
            ftsExpression = unescape(ftsExpression, EscapeMode.CONTAINS);
            Selector selector;
            if (predicateNode.getChildCount() > 1)
            {
                String qualifier = predicateNode.getChild(1).getText();
                selector = selectors.get(qualifier);
                if (selector == null)
                {
                    throw new CmisInvalidArgumentException("No selector for " + qualifier);
                }
            } else
            {
                if (selectors.size() == 1)
                {
                    selector = selectors.get(selectors.keySet().iterator().next());
                } else
                {
                    throw new CmisInvalidArgumentException(
                            "A selector must be specified when there are two or more selectors");
                }
            }
            Connective defaultConnective;
            Connective defaultFieldConnective;
            if (options.getQueryMode() == CMISQueryMode.CMS_STRICT)
            {
                defaultConnective = Connective.AND;
                defaultFieldConnective = Connective.AND;
            } else
            {
                defaultConnective = options.getDefaultFTSConnective();
                defaultFieldConnective = options.getDefaultFTSFieldConnective();
            }
            FTSParser.Mode mode;
            if (options.getQueryMode() == CMISQueryMode.CMS_STRICT)
            {
                mode = FTSParser.Mode.CMIS;
            } else
            {
                if (defaultConnective == Connective.AND)
                {
                    mode = FTSParser.Mode.DEFAULT_CONJUNCTION;
                } else
                {
                    mode = FTSParser.Mode.DEFAULT_DISJUNCTION;
                }
            }
            Constraint ftsConstraint;
            if (options.getQueryMode() == CMISQueryMode.CMS_STRICT)
            {
                ftsConstraint = CMISFTSQueryParser.buildFTS(ftsExpression, factory, functionEvaluationContext,
                        selector, columnMap, options.getDefaultFieldName());
            } else
            {
                ftsConstraint = FTSQueryParser.buildFTS(ftsExpression, factory, functionEvaluationContext, selector,
                        columnMap, mode, defaultFieldConnective, null, options.getDefaultFieldName());
            }
            ftsConstraint.setBoost(1000.0f);
            hasContains = true;
            return ftsConstraint;
        case CMISParser.PRED_IN:
            functionName = In.NAME;
            function = factory.getFunction(functionName);
            functionArguments = new LinkedHashMap<String, Argument>();
            argNode = (CommonTree) predicateNode.getChild(0);
            arg = getFunctionArgument(argNode, function.getArgumentDefinition(In.ARG_MODE), factory, selectors,
                    columnMap, false);
            functionArguments.put(arg.getName(), arg);
            argNode = (CommonTree) predicateNode.getChild(1);
            arg = getFunctionArgument(argNode, function.getArgumentDefinition(In.ARG_PROPERTY), factory, selectors,
                    columnMap, false);
            functionArguments.put(arg.getName(), arg);
            argNode = (CommonTree) predicateNode.getChild(2);
            arg = getFunctionArgument(argNode, function.getArgumentDefinition(In.ARG_LIST), factory, selectors,
                    columnMap, false);
            functionArguments.put(arg.getName(), arg);
            arg = factory.createLiteralArgument(In.ARG_NOT, DataTypeDefinition.BOOLEAN,
                    (predicateNode.getChildCount() > 3));
            functionArguments.put(arg.getName(), arg);
            checkPredicateConditionsForIn(functionArguments, functionEvaluationContext, columnMap);
            return factory.createFunctionalConstraint(function, functionArguments);
        case CMISParser.PRED_LIKE:
            functionName = Like.NAME;
            function = factory.getFunction(functionName);
            functionArguments = new LinkedHashMap<String, Argument>();
            argNode = (CommonTree) predicateNode.getChild(0);
            arg = getFunctionArgument(argNode, function.getArgumentDefinition(Like.ARG_PROPERTY), factory, selectors,
                    columnMap, false);
            functionArguments.put(arg.getName(), arg);
            argNode = (CommonTree) predicateNode.getChild(1);
            arg = getFunctionArgument(argNode, function.getArgumentDefinition(Like.ARG_EXP), factory, selectors,
                    columnMap, true);
            functionArguments.put(arg.getName(), arg);
            arg = factory.createLiteralArgument(Like.ARG_NOT, DataTypeDefinition.BOOLEAN,
                    (predicateNode.getChildCount() > 2));
            functionArguments.put(arg.getName(), arg);
            checkPredicateConditionsForLike(functionArguments, functionEvaluationContext, columnMap);
            return factory.createFunctionalConstraint(function, functionArguments);
        default:
            return null;
        }
    }

    private void checkPredicateConditionsForIn(Map<String, Argument> functionArguments,
            FunctionEvaluationContext functionEvaluationContext, Map<String, Column> columnMap)
    {
        if (options.getQueryMode() == CMISQueryMode.CMS_STRICT)
        {
            PropertyArgument propertyArgument = (PropertyArgument) functionArguments.get(In.ARG_PROPERTY);
            LiteralArgument modeArgument = (LiteralArgument) functionArguments.get(In.ARG_MODE);
            String modeString = DefaultTypeConverter.INSTANCE.convert(String.class,
                    modeArgument.getValue(functionEvaluationContext));
            PredicateMode mode = PredicateMode.valueOf(modeString);
            String propertyName = propertyArgument.getPropertyName();

            Column column = columnMap.get(propertyName);
            if (column != null)
            {
                // check for function type
                if (column.getFunction().getName().equals(PropertyAccessor.NAME))
                {
                    PropertyArgument arg = (PropertyArgument) column.getFunctionArguments().get(
                            PropertyAccessor.ARG_PROPERTY);
                    propertyName = arg.getPropertyName();
                } else
                {
                    throw new CmisInvalidArgumentException("Complex column reference not supoprted in LIKE "
                            + propertyName);
                }
            }

            boolean isMultiValued = functionEvaluationContext.isMultiValued(propertyName);

            switch (mode)
            {
            case ANY:
                if (isMultiValued)
                {
                    break;
                } else
                {
                    throw new QueryModelException("Predicate mode " + PredicateMode.ANY
                            + " is not supported for IN and single valued properties");
                }
            case SINGLE_VALUED_PROPERTY:
                if (isMultiValued)
                {
                    throw new QueryModelException("Predicate mode " + PredicateMode.SINGLE_VALUED_PROPERTY
                            + " is not supported for IN and multi-valued properties");
                } else
                {
                    break;
                }
            default:
                throw new QueryModelException("Unsupported predicate mode " + mode);
            }

            PropertyDefintionWrapper propDef = cmisDictionaryService.findPropertyByQueryName(propertyName);
            if (propDef.getPropertyDefinition().getPropertyType() == PropertyType.BOOLEAN)
            {
                throw new QueryModelException("In is not supported for properties of type Boolean");
            }
        }

    }

    private void checkPredicateConditionsForComparisons(Function function, Map<String, Argument> functionArguments,
            FunctionEvaluationContext functionEvaluationContext, Map<String, Column> columnMap)
    {
        if (options.getQueryMode() == CMISQueryMode.CMS_STRICT)
        {
            ((BaseComparison) function).setPropertyAndStaticArguments(functionArguments);
            String propertyName = ((BaseComparison) function).getPropertyName();
            LiteralArgument modeArgument = (LiteralArgument) functionArguments.get(BaseComparison.ARG_MODE);
            String modeString = DefaultTypeConverter.INSTANCE.convert(String.class,
                    modeArgument.getValue(functionEvaluationContext));
            PredicateMode mode = PredicateMode.valueOf(modeString);

            Column column = columnMap.get(propertyName);
            if (column != null)
            {
                // check for function type
                if (column.getFunction().getName().equals(PropertyAccessor.NAME))
                {
                    PropertyArgument arg = (PropertyArgument) column.getFunctionArguments().get(
                            PropertyAccessor.ARG_PROPERTY);
                    propertyName = arg.getPropertyName();
                } else
                {
                    throw new CmisInvalidArgumentException("Complex column reference not supoprted in LIKE "
                            + propertyName);
                }
            }

            boolean isMultiValued = functionEvaluationContext.isMultiValued(propertyName);

            switch (mode)
            {
            case ANY:
                if (isMultiValued)
                {
                    if (function.getName().equals(Equals.NAME))
                    {
                        break;
                    } else
                    {
                        throw new QueryModelException("Predicate mode " + PredicateMode.ANY + " is only supported for "
                                + Equals.NAME + " (and multi-valued properties).");
                    }
                } else
                {
                    throw new QueryModelException("Predicate mode " + PredicateMode.ANY + " is not supported for "
                            + function.getName() + " and single valued properties");
                }
            case SINGLE_VALUED_PROPERTY:
                if (isMultiValued)
                {
                    throw new QueryModelException("Predicate mode " + PredicateMode.SINGLE_VALUED_PROPERTY
                            + " is not supported for " + function.getName() + " and multi-valued properties");
                } else
                {
                    break;
                }
            default:
                throw new QueryModelException("Unsupported predicate mode " + mode);
            }

            // limit support for ID and Boolean

            PropertyDefintionWrapper propDef = cmisDictionaryService.findPropertyByQueryName(propertyName);
            if (propDef.getPropertyDefinition().getPropertyType() == PropertyType.ID)
            {
                if (function.getName().equals(Equals.NAME) || function.getName().equals(NotEquals.NAME))
                {
                    return;
                } else
                {
                    throw new QueryModelException("Comparison " + function.getName()
                            + " is not supported for properties of type ID");
                }
            } else if (propDef.getPropertyDefinition().getPropertyType() == PropertyType.BOOLEAN)
            {
                if (function.getName().equals(Equals.NAME))
                {
                    return;
                } else
                {
                    throw new QueryModelException("Comparison " + function.getName()
                            + " is not supported for properties of type Boolean");
                }
            }
        }

    }

    private void checkPredicateConditionsForLike(Map<String, Argument> functionArguments,
            FunctionEvaluationContext functionEvaluationContext, Map<String, Column> columnMap)
    {
        if (options.getQueryMode() == CMISQueryMode.CMS_STRICT)
        {
            PropertyArgument propertyArgument = (PropertyArgument) functionArguments.get(Like.ARG_PROPERTY);

            boolean isMultiValued = functionEvaluationContext.isMultiValued(propertyArgument.getPropertyName());

            if (isMultiValued)
            {
                throw new QueryModelException("Like is not supported for multi-valued properties");
            }

            String cmisPropertyName = propertyArgument.getPropertyName();

            Column column = columnMap.get(cmisPropertyName);
            if (column != null)
            {
                // check for function type
                if (column.getFunction().getName().equals(PropertyAccessor.NAME))
                {
                    PropertyArgument arg = (PropertyArgument) column.getFunctionArguments().get(
                            PropertyAccessor.ARG_PROPERTY);
                    cmisPropertyName = arg.getPropertyName();
                } else
                {
                    throw new CmisInvalidArgumentException("Complex column reference not supoprted in LIKE "
                            + cmisPropertyName);
                }
            }

            PropertyDefintionWrapper propDef = cmisDictionaryService.findPropertyByQueryName(cmisPropertyName);
            if (propDef.getPropertyDefinition().getPropertyType() != PropertyType.STRING)
            {
                throw new CmisInvalidArgumentException("LIKE is only supported against String types" + cmisPropertyName);
            }
        }
    }

    /**
     * @param queryNode
     * @param factory
     * @param selectors
     * @return
     */
    private ArrayList<Ordering> buildOrderings(CommonTree queryNode, QueryModelFactory factory,
            Map<String, Selector> selectors, List<Column> columns)
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
                            if (column.getFunction().getName().equals(PropertyAccessor.NAME))
                            {
                                PropertyArgument arg = (PropertyArgument) column.getFunctionArguments().get(
                                        PropertyAccessor.ARG_PROPERTY);
                                String propertyName = arg.getPropertyName();
                                if (propertyName.equals(columnName))
                                {
                                    match = column;
                                    break;
                                }
                            }
                        }
                        // in strict mode the ordered column must be selected
                        if ((options.getQueryMode() == CMISQueryMode.CMS_STRICT) && (match == null))
                        {
                            throw new CmisInvalidArgumentException("Ordered column is not selected: " + qualifier + "."
                                    + columnName);
                        }
                        if (match == null)
                        {

                            Selector selector = selectors.get(qualifier);
                            if (selector == null)
                            {
                                if ((qualifier.equals("")) && (selectors.size() == 1))
                                {
                                    selector = selectors.get(selectors.keySet().iterator().next());
                                } else
                                {
                                    throw new CmisInvalidArgumentException("No selector for " + qualifier);
                                }
                            }

                            TypeDefinitionWrapper typeDef = cmisDictionaryService.findTypeForClass(selector.getType(),
                                    BaseTypeId.CMIS_DOCUMENT, BaseTypeId.CMIS_FOLDER);
                            if (typeDef == null)
                            {
                                throw new CmisInvalidArgumentException("Type unsupported in CMIS queries: "
                                        + selector.getAlias());
                            }
                            PropertyDefintionWrapper propDef = cmisDictionaryService
                                    .findPropertyByQueryName(columnName);
                            if (propDef == null)
                            {
                                throw new CmisInvalidArgumentException("Invalid column for "
                                        + typeDef.getTypeDefinition(false).getQueryName() + "." + columnName);
                            }

                            // Check column/property applies to selector/type

                            if (typeDef.getPropertyById(propDef.getPropertyId()) == null)
                            {
                                throw new CmisInvalidArgumentException("Invalid column for "
                                        + typeDef.getTypeDefinition(false).getQueryName() + "." + columnName);
                            }

                            // check there is a matching selector

                            if (options.getQueryMode() == CMISQueryMode.CMS_STRICT)
                            {
                                boolean found = false;
                                for (Column column : columns)
                                {
                                    if (column.getFunction().getName().equals(PropertyAccessor.NAME))
                                    {
                                        PropertyArgument pa = (PropertyArgument) column.getFunctionArguments().get(
                                                PropertyAccessor.ARG_PROPERTY);
                                        if (pa.getPropertyName().equals(propDef.getPropertyId()))
                                        {
                                            found = true;
                                            break;
                                        }
                                    }
                                }
                                if (!found)
                                {
                                    throw new CmisInvalidArgumentException("Ordered column is not selected: "
                                            + qualifier + "." + columnName);
                                }
                            }

                            Function function = factory.getFunction(PropertyAccessor.NAME);
                            Argument arg = factory.createPropertyArgument(PropertyAccessor.ARG_PROPERTY, propDef
                                    .getPropertyDefinition().isQueryable(), propDef.getPropertyDefinition()
                                    .isOrderable(), selector.getAlias(), propDef.getPropertyId());
                            Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
                            functionArguments.put(arg.getName(), arg);

                            String alias = (selector.getAlias().length() > 0) ? selector.getAlias() + "."
                                    + propDef.getPropertyId() : propDef.getPropertyId();

                            match = factory.createColumn(function, functionArguments, alias);
                        }

                        orderColumn = match;
                    } else
                    {
                        Selector selector = selectors.get(qualifier);
                        if (selector == null)
                        {
                            if ((qualifier.equals("")) && (selectors.size() == 1))
                            {
                                selector = selectors.get(selectors.keySet().iterator().next());
                            } else
                            {
                                throw new CmisInvalidArgumentException("No selector for " + qualifier);
                            }
                        }

                        TypeDefinitionWrapper typeDef = cmisDictionaryService.findTypeForClass(selector.getType(),
                                BaseTypeId.CMIS_DOCUMENT, BaseTypeId.CMIS_FOLDER);
                        if (typeDef == null)
                        {
                            throw new CmisInvalidArgumentException("Type unsupported in CMIS queries: "
                                    + selector.getAlias());
                        }
                        PropertyDefintionWrapper propDef = cmisDictionaryService.findPropertyByQueryName(columnName);
                        if (propDef == null)
                        {
                            throw new CmisInvalidArgumentException("Invalid column for "
                                    + typeDef.getTypeDefinition(false).getQueryName() + "." + columnName
                                    + " selector alias " + selector.getAlias());
                        }

                        // check there is a matching selector

                        if (options.getQueryMode() == CMISQueryMode.CMS_STRICT)
                        {
                            boolean found = false;
                            for (Column column : columns)
                            {
                                if (column.getFunction().getName().equals(PropertyAccessor.NAME))
                                {
                                    PropertyArgument pa = (PropertyArgument) column.getFunctionArguments().get(
                                            PropertyAccessor.ARG_PROPERTY);
                                    if (pa.getPropertyName().equals(propDef.getPropertyId()))
                                    {
                                        found = true;
                                        break;
                                    }
                                }
                            }
                            if (!found)
                            {
                                throw new CmisInvalidArgumentException("Ordered column is not selected: " + qualifier
                                        + "." + columnName);
                            }
                        }

                        Function function = factory.getFunction(PropertyAccessor.NAME);
                        Argument arg = factory.createPropertyArgument(PropertyAccessor.ARG_PROPERTY, propDef
                                .getPropertyDefinition().isQueryable(), propDef.getPropertyDefinition().isOrderable(),
                                selector.getAlias(), propDef.getPropertyId());
                        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
                        functionArguments.put(arg.getName(), arg);

                        String alias = (selector.getAlias().length() > 0) ? selector.getAlias() + "."
                                + propDef.getPropertyId() : propDef.getPropertyId();

                        orderColumn = factory.createColumn(function, functionArguments, alias);
                    }

                    if (!orderColumn.isOrderable() || !orderColumn.isQueryable())
                    {
                        throw new CmisInvalidArgumentException("Ordering is not support for " + orderColumn.getAlias());
                    }

                    Ordering ordering = factory.createOrdering(orderColumn, order);
                    orderings.add(ordering);

                }
            }
        }
        return orderings;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<Column> buildColumns(CommonTree queryNode, QueryModelFactory factory,
            Map<String, Selector> selectors, String query)
    {
        ArrayList<Column> columns = new ArrayList<Column>();
        CommonTree starNode = (CommonTree) queryNode.getFirstChildWithType(CMISParser.ALL_COLUMNS);
        if (starNode != null)
        {
            for (Selector selector : selectors.values())
            {
                TypeDefinitionWrapper typeDef = cmisDictionaryService.findTypeForClass(selector.getType(), validScopes);
                if (typeDef == null)
                {
                    throw new CmisInvalidArgumentException("Type unsupported in CMIS queries: " + selector.getAlias());
                }
                Collection<PropertyDefintionWrapper> propDefs = typeDef.getProperties();
                for (PropertyDefintionWrapper definition : propDefs)
                {
                    Function function = factory.getFunction(PropertyAccessor.NAME);
                    Argument arg = factory.createPropertyArgument(PropertyAccessor.ARG_PROPERTY, definition
                            .getPropertyDefinition().isQueryable(), definition.getPropertyDefinition().isOrderable(),
                            selector.getAlias(), definition.getPropertyId());
                    Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
                    functionArguments.put(arg.getName(), arg);
                    String alias = (selector.getAlias().length() > 0) ? selector.getAlias() + "."
                            + definition.getPropertyId() : definition.getPropertyId();
                    Column column = factory.createColumn(function, functionArguments, alias);
                    columns.add(column);
                }
            }
        }

        CommonTree columnsNode = (CommonTree) queryNode.getFirstChildWithType(CMISParser.COLUMNS);
        if (columnsNode != null)
        {
            for (CommonTree columnNode : (List<CommonTree>) columnsNode.getChildren())
            {
                if (columnNode.getType() == CMISParser.ALL_COLUMNS)
                {
                    String qualifier = columnNode.getChild(0).getText();
                    Selector selector = selectors.get(qualifier);
                    if (selector == null)
                    {
                        if ((qualifier.equals("")) && (selectors.size() == 1))
                        {
                            selector = selectors.get(selectors.keySet().iterator().next());
                        } else
                        {
                            throw new CmisInvalidArgumentException("No selector for " + qualifier + " in " + qualifier
                                    + ".*");
                        }
                    }

                    TypeDefinitionWrapper typeDef = cmisDictionaryService.findTypeForClass(selector.getType(),
                            validScopes);
                    if (typeDef == null)
                    {
                        throw new CmisInvalidArgumentException("Type unsupported in CMIS queries: "
                                + selector.getAlias());
                    }
                    Collection<PropertyDefintionWrapper> propDefs = typeDef.getProperties();
                    for (PropertyDefintionWrapper definition : propDefs)
                    {
                        Function function = factory.getFunction(PropertyAccessor.NAME);
                        Argument arg = factory.createPropertyArgument(PropertyAccessor.ARG_PROPERTY, definition
                                .getPropertyDefinition().isQueryable(), definition.getPropertyDefinition()
                                .isOrderable(), selector.getAlias(), definition.getPropertyId());
                        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
                        functionArguments.put(arg.getName(), arg);
                        String alias = (selector.getAlias().length() > 0) ? selector.getAlias() + "."
                                + definition.getPropertyId() : definition.getPropertyId();
                        Column column = factory.createColumn(function, functionArguments, alias);
                        columns.add(column);
                    }
                }

                if (columnNode.getType() == CMISParser.COLUMN)
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
                            if ((qualifier.equals("")) && (selectors.size() == 1))
                            {
                                selector = selectors.get(selectors.keySet().iterator().next());
                            } else
                            {
                                throw new CmisInvalidArgumentException("No selector for " + qualifier);
                            }
                        }

                        TypeDefinitionWrapper typeDef = cmisDictionaryService.findTypeForClass(selector.getType(),
                                validScopes);
                        if (typeDef == null)
                        {
                            throw new CmisInvalidArgumentException("Type unsupported in CMIS queries: "
                                    + selector.getAlias());
                        }
                        PropertyDefintionWrapper propDef = cmisDictionaryService.findPropertyByQueryName(columnName);
                        if (propDef == null)
                        {
                            throw new CmisInvalidArgumentException("Invalid column for "
                                    + typeDef.getTypeDefinition(false).getQueryName() + " " + columnName);
                        }

                        // Check column/property applies to selector/type

                        if (typeDef.getPropertyById(propDef.getPropertyId()) == null)
                        {
                            throw new CmisInvalidArgumentException("Invalid column for "
                                    + typeDef.getTypeDefinition(false).getQueryName() + "." + columnName);
                        }

                        Function function = factory.getFunction(PropertyAccessor.NAME);
                        Argument arg = factory.createPropertyArgument(PropertyAccessor.ARG_PROPERTY, propDef
                                .getPropertyDefinition().isQueryable(), propDef.getPropertyDefinition().isOrderable(),
                                selector.getAlias(), propDef.getPropertyId());
                        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
                        functionArguments.put(arg.getName(), arg);

                        String alias = (selector.getAlias().length() > 0) ? selector.getAlias() + "."
                                + propDef.getPropertyId() : propDef.getPropertyId();
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
                        CommonTree functionNameNode = (CommonTree) functionNode.getChild(0);
                        Function function = factory.getFunction(functionNameNode.getText());
                        if (function == null)
                        {
                            throw new CmisInvalidArgumentException("Unknown function: " + functionNameNode.getText());
                        }
                        Collection<ArgumentDefinition> definitions = function.getArgumentDefinitions().values();
                        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();

                        int childIndex = 2;
                        for (ArgumentDefinition definition : definitions)
                        {
                            if (functionNode.getChildCount() > childIndex + 1)
                            {
                                CommonTree argNode = (CommonTree) functionNode.getChild(childIndex++);
                                Argument arg = getFunctionArgument(argNode, definition, factory, selectors, null,
                                        function.getName().equals(Like.NAME));
                                functionArguments.put(arg.getName(), arg);
                            } else
                            {
                                if (definition.isMandatory())
                                {
                                    // throw new
                                    // CmisInvalidArgumentException("Insufficient
                                    // aruments
                                    // for function " +
                                    // ((CommonTree)
                                    // functionNode.getChild(0)).getText() );
                                    break;
                                } else
                                {
                                    // ok
                                }
                            }
                        }

                        CommonTree rparenNode = (CommonTree) functionNode.getChild(functionNode.getChildCount() - 1);

                        int start = getStringPosition(query, functionNode.getLine(),
                                functionNode.getCharPositionInLine());
                        int end = getStringPosition(query, rparenNode.getLine(), rparenNode.getCharPositionInLine());

                        if (function.getName().equals(Score.NAME))
                        {
                            hasScore = true;
                        }

                        String alias;
                        if (function.getName().equals(Score.NAME))
                        {
                            alias = "SEARCH_SCORE";
                            // check no args
                            if (functionNode.getChildCount() > 3)
                            {
                                throw new CmisInvalidArgumentException(
                                        "The function SCORE() is not allowed any arguments");
                            }
                        } else
                        {
                            alias = query.substring(start, end + 1);
                        }
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

    /**
     * @param query
     * @param line
     * @param charPositionInLine
     * @return
     */
    private int getStringPosition(String query, int line, int charPositionInLine)
    {
        StringTokenizer tokenizer = new StringTokenizer(query, "\n\r\f");
        String[] lines = new String[tokenizer.countTokens()];
        int i = 0;
        while (tokenizer.hasMoreElements())
        {
            lines[i++] = tokenizer.nextToken();
        }

        int position = 0;
        for (i = 0; i < line - 1; i++)
        {
            position += lines[i].length();
            position++;
        }
        return position + charPositionInLine;
    }

    private Argument getFunctionArgument(CommonTree argNode, ArgumentDefinition definition, QueryModelFactory factory,
            Map<String, Selector> selectors, Map<String, Column> columnMap, boolean inLike)
    {
        if (argNode.getType() == CMISParser.COLUMN_REF)
        {
            PropertyArgument arg = buildColumnReference(definition.getName(), argNode, factory, selectors, columnMap);
            if (!arg.isQueryable())
            {
                throw new CmisInvalidArgumentException("Column refers to unqueryable property " + arg.getPropertyName());
            }
            if (!selectors.containsKey(arg.getSelector()))
            {
                throw new CmisInvalidArgumentException("No table with alias " + arg.getSelector());
            }
            return arg;
        } else if (argNode.getType() == CMISParser.ID)
        {
            String id = argNode.getText();
            if (selectors.containsKey(id))
            {
                SelectorArgument arg = factory.createSelectorArgument(definition.getName(), id);
                if (!arg.isQueryable())
                {
                    throw new CmisInvalidArgumentException("Selector is not queryable " + arg.getSelector());
                }
                return arg;
            } else
            {
                PropertyDefintionWrapper propDef = cmisDictionaryService.findPropertyByQueryName(id);
                if (propDef == null || !propDef.getPropertyDefinition().isQueryable())
                {
                    throw new CmisInvalidArgumentException("Column refers to unqueryable property "
                            + definition.getName());
                }
                PropertyArgument arg = factory.createPropertyArgument(definition.getName(), propDef
                        .getPropertyDefinition().isQueryable(), propDef.getPropertyDefinition().isOrderable(), "",
                        propDef.getPropertyId());
                return arg;
            }
        } else if (argNode.getType() == CMISParser.PARAMETER)
        {
            ParameterArgument arg = factory.createParameterArgument(definition.getName(), argNode.getText());
            if (!arg.isQueryable())
            {
                throw new CmisInvalidArgumentException("Parameter is not queryable " + arg.getParameterName());
            }
            return arg;
        } else if (argNode.getType() == CMISParser.NUMERIC_LITERAL)
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
                LiteralArgument arg = factory.createLiteralArgument(definition.getName(), type, value);
                return arg;
            } else if (literalNode.getType() == CMISParser.DECIMAL_INTEGER_LITERAL)
            {
                QName type = DataTypeDefinition.LONG;
                Number value = Long.parseLong(literalNode.getText());
                if (value.intValue() == value.longValue())
                {
                    type = DataTypeDefinition.INT;
                    value = Integer.valueOf(value.intValue());
                }
                LiteralArgument arg = factory.createLiteralArgument(definition.getName(), type, value);
                return arg;
            } else
            {
                throw new CmisInvalidArgumentException("Invalid numeric literal " + literalNode.getText());
            }
        } else if (argNode.getType() == CMISParser.STRING_LITERAL)
        {
            String text = argNode.getChild(0).getText();
            text = text.substring(1, text.length() - 1);
            text = unescape(text, inLike ? EscapeMode.LIKE : EscapeMode.LITERAL);
            LiteralArgument arg = factory.createLiteralArgument(definition.getName(), DataTypeDefinition.TEXT, text);
            return arg;
        } else if (argNode.getType() == CMISParser.DATETIME_LITERAL)
        {
            String text = argNode.getChild(0).getText();
            text = text.substring(1, text.length() - 1);
            StringBuilder builder = new StringBuilder();
            if (text.endsWith("Z"))
            {
                builder.append(text.substring(0, text.length() - 1));
                builder.append("+0000");
            } else
            {
                if (text.charAt(text.length() - 3) != ':')
                {
                    throw new CmisInvalidArgumentException("Invalid datetime literal " + text);
                }
                // remove TZ colon ....
                builder.append(text.substring(0, text.length() - 3));
                builder.append(text.substring(text.length() - 2, text.length()));
            }
            text = builder.toString();

            SimpleDateFormat df = CachingDateFormat.getCmisSqlDatetimeFormat();
            Date date;
            try
            {
                date = df.parse(text);
            } catch (ParseException e)
            {
                throw new CmisInvalidArgumentException("Invalid datetime literal " + text);
            }
            // Convert back :-)
            String alfrescoDate = DefaultTypeConverter.INSTANCE.convert(String.class, date);
            LiteralArgument arg = factory.createLiteralArgument(definition.getName(), DataTypeDefinition.TEXT,
                    alfrescoDate);
            return arg;
        } else if (argNode.getType() == CMISParser.BOOLEAN_LITERAL)
        {
            String text = argNode.getChild(0).getText();
            if (text.equalsIgnoreCase("TRUE") || text.equalsIgnoreCase("FALSE"))
            {
                LiteralArgument arg = factory
                        .createLiteralArgument(definition.getName(), DataTypeDefinition.TEXT, text);
                return arg;
            } else
            {
                throw new CmisInvalidArgumentException("Invalid boolean literal " + text);
            }

        } else if (argNode.getType() == CMISParser.LIST)
        {
            ArrayList<Argument> arguments = new ArrayList<Argument>();
            for (int i = 0; i < argNode.getChildCount(); i++)
            {
                CommonTree arg = (CommonTree) argNode.getChild(i);
                arguments.add(getFunctionArgument(arg, definition, factory, selectors, columnMap, inLike));
            }
            ListArgument arg = factory.createListArgument(definition.getName(), arguments);
            if (!arg.isQueryable())
            {
                throw new CmisInvalidArgumentException("Not all members of the list are queryable");
            }
            return arg;
        } else if (argNode.getType() == CMISParser.ANY)
        {
            LiteralArgument arg = factory.createLiteralArgument(definition.getName(), DataTypeDefinition.TEXT,
                    argNode.getText());
            return arg;
        } else if (argNode.getType() == CMISParser.SINGLE_VALUED_PROPERTY)
        {
            LiteralArgument arg = factory.createLiteralArgument(definition.getName(), DataTypeDefinition.TEXT,
                    argNode.getText());
            return arg;
        } else if (argNode.getType() == CMISParser.NOT)
        {
            LiteralArgument arg = factory.createLiteralArgument(definition.getName(), DataTypeDefinition.TEXT,
                    argNode.getText());
            return arg;
        } else if (argNode.getType() == CMISParser.FUNCTION)
        {
            CommonTree functionNameNode = (CommonTree) argNode.getChild(0);
            Function function = factory.getFunction(functionNameNode.getText());
            if (function == null)
            {
                throw new CmisInvalidArgumentException("Unknown function: " + functionNameNode.getText());
            }
            Collection<ArgumentDefinition> definitions = function.getArgumentDefinitions().values();
            Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();

            int childIndex = 2;
            for (ArgumentDefinition currentDefinition : definitions)
            {
                if (argNode.getChildCount() > childIndex + 1)
                {
                    CommonTree currentArgNode = (CommonTree) argNode.getChild(childIndex++);
                    Argument arg = getFunctionArgument(currentArgNode, currentDefinition, factory, selectors,
                            columnMap, inLike);
                    functionArguments.put(arg.getName(), arg);
                } else
                {
                    if (definition.isMandatory())
                    {
                        // throw new CmisInvalidArgumentException("Insufficient
                        // aruments
                        // for function " + ((CommonTree)
                        // functionNode.getChild(0)).getText() );
                        break;
                    } else
                    {
                        // ok
                    }
                }
            }
            FunctionArgument arg = factory.createFunctionArgument(definition.getName(), function, functionArguments);
            if (!arg.isQueryable())
            {
                throw new CmisInvalidArgumentException("Not all function arguments refer to orderable arguments: "
                        + arg.getFunction().getName());
            }
            return arg;
        } else
        {
            throw new CmisInvalidArgumentException("Invalid function argument " + argNode.getText());
        }
    }

    @SuppressWarnings("unchecked")
    private Source buildSource(CommonTree source, CapabilityJoin joinSupport, QueryModelFactory factory)
    {
        if (source.getChildCount() == 1)
        {
            // single table reference
            CommonTree singleTableNode = (CommonTree) source.getChild(0);
            if (singleTableNode.getType() == CMISParser.TABLE)
            {
                if (joinSupport == CapabilityJoin.NONE)
                {
                    throw new UnsupportedOperationException("Joins are not supported");
                }
                CommonTree tableSourceNode = (CommonTree) singleTableNode.getFirstChildWithType(CMISParser.SOURCE);
                return buildSource(tableSourceNode, joinSupport, factory);

            }
            if (singleTableNode.getType() != CMISParser.TABLE_REF)
            {
                throw new CmisInvalidArgumentException("Expecting TABLE_REF token but found "
                        + singleTableNode.getText());
            }
            String tableName = singleTableNode.getChild(0).getText();
            String alias = "";
            if (singleTableNode.getChildCount() > 1)
            {
                alias = singleTableNode.getChild(1).getText();
            }

            TypeDefinitionWrapper typeDef = cmisDictionaryService.findTypeByQueryName(tableName);
            if (typeDef == null)
            {
                throw new CmisInvalidArgumentException("Type is unsupported in query: " + tableName);
            }
            if (typeDef.getBaseTypeId() != BaseTypeId.CMIS_POLICY)
            {
                if (!typeDef.getTypeDefinition(false).isQueryable())
                {
                    throw new CmisInvalidArgumentException("Type is not queryable " + tableName + " -> "
                            + typeDef.getTypeId());
                }
            }
            return factory.createSelector(typeDef.getAlfrescoClass(), alias);
        } else
        {
            if (joinSupport == CapabilityJoin.NONE)
            {
                throw new UnsupportedOperationException("Joins are not supported");
            }
            CommonTree singleTableNode = (CommonTree) source.getChild(0);
            if (singleTableNode.getType() != CMISParser.TABLE_REF)
            {
                throw new CmisInvalidArgumentException("Expecting TABLE_REF token but found "
                        + singleTableNode.getText());
            }
            String tableName = singleTableNode.getChild(0).getText();
            String alias = "";
            if (singleTableNode.getChildCount() == 2)
            {
                alias = singleTableNode.getChild(1).getText();
            }
            TypeDefinitionWrapper typeDef = cmisDictionaryService.findTypeByQueryName(tableName);
            if (typeDef == null)
            {
                throw new CmisInvalidArgumentException("Type is unsupported in query " + tableName);
            }
            if (typeDef.getBaseTypeId() != BaseTypeId.CMIS_POLICY)
            {
                if (!typeDef.getTypeDefinition(false).isQueryable())
                {
                    throw new CmisInvalidArgumentException("Type is not queryable " + tableName + " -> "
                            + typeDef.getTypeId());
                }
            }

            Source lhs = factory.createSelector(typeDef.getAlfrescoClass(), alias);

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

                    if ((joinType == JoinType.LEFT) && (joinSupport == CapabilityJoin.INNERONLY))
                    {
                        throw new UnsupportedOperationException("Outer joins are not supported");
                    }

                    Constraint joinCondition = null;
                    CommonTree joinConditionNode = (CommonTree) joinNode.getFirstChildWithType(CMISParser.ON);
                    if (joinConditionNode != null)
                    {
                        PropertyArgument arg1 = buildColumnReference(Equals.ARG_LHS,
                                (CommonTree) joinConditionNode.getChild(0), factory, null, null);
                        if (!lhs.getSelectors().containsKey(arg1.getSelector())
                                && !rhs.getSelectors().containsKey(arg1.getSelector()))
                        {
                            throw new CmisInvalidArgumentException("No table with alias " + arg1.getSelector());
                        }
                        CommonTree functionNameNode = (CommonTree) joinConditionNode.getChild(1);
                        if (functionNameNode.getType() != CMISParser.EQUALS)
                        {
                            throw new CmisInvalidArgumentException("Only Equi-join is supported "
                                    + functionNameNode.getText());
                        }
                        Function function = factory.getFunction(Equals.NAME);
                        if (function == null)
                        {
                            throw new CmisInvalidArgumentException("Unknown function: " + functionNameNode.getText());
                        }
                        PropertyArgument arg2 = buildColumnReference(Equals.ARG_RHS,
                                (CommonTree) joinConditionNode.getChild(2), factory, null, null);
                        if (!lhs.getSelectors().containsKey(arg2.getSelector())
                                && !rhs.getSelectors().containsKey(arg2.getSelector()))
                        {
                            throw new CmisInvalidArgumentException("No table with alias " + arg2.getSelector());
                        }
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

    public PropertyArgument buildColumnReference(String argumentName, CommonTree columnReferenceNode,
            QueryModelFactory factory, Map<String, Selector> selectors, Map<String, Column> columnMap)
    {
        String cmisPropertyName = columnReferenceNode.getChild(0).getText();
        String qualifier = "";
        if (columnReferenceNode.getChildCount() > 1)
        {
            qualifier = columnReferenceNode.getChild(1).getText();
        }

        if ((qualifier == "") && (columnMap != null))
        {
            Column column = columnMap.get(cmisPropertyName);
            if (column != null)
            {
                // check for function type
                if (column.getFunction().getName().equals(PropertyAccessor.NAME))
                {
                    PropertyArgument arg = (PropertyArgument) column.getFunctionArguments().get(
                            PropertyAccessor.ARG_PROPERTY);
                    cmisPropertyName = arg.getPropertyName();
                    qualifier = arg.getSelector();
                } else
                {
                    // TODO: should be able to return non property arguments
                    // The implementation should throw out what it can not
                    // support at build time.
                    throw new CmisInvalidArgumentException(
                            "Complex column reference unsupported (only direct column references are currently supported) "
                                    + cmisPropertyName);
                }
            }
        }

        PropertyDefintionWrapper propDef = cmisDictionaryService.findPropertyByQueryName(cmisPropertyName);
        if (propDef == null)
        {
            throw new CmisInvalidArgumentException("Unknown column/property " + cmisPropertyName);
        }

        if (selectors != null)
        {
            Selector selector = selectors.get(qualifier);
            if (selector == null)
            {
                if ((qualifier.equals("")) && (selectors.size() == 1))
                {
                    selector = selectors.get(selectors.keySet().iterator().next());
                } else
                {
                    throw new CmisInvalidArgumentException("No selector for " + qualifier);
                }
            }

            TypeDefinitionWrapper typeDef = cmisDictionaryService.findTypeForClass(selector.getType(), validScopes);
            if (typeDef == null)
            {
                throw new CmisInvalidArgumentException("Type unsupported in CMIS queries: " + selector.getAlias());
            }

            // Check column/property applies to selector/type

            if (typeDef.getPropertyById(propDef.getPropertyId()) == null)
            {
                throw new CmisInvalidArgumentException("Invalid column for "
                        + typeDef.getTypeDefinition(false).getQueryName() + "." + cmisPropertyName);
            }
        }

        if (options.getQueryMode() == CMISQueryMode.CMS_STRICT)
        {
            if (!propDef.getPropertyDefinition().isQueryable())
            {
                throw new CmisInvalidArgumentException("Column is not queryable " + qualifier + "." + cmisPropertyName);
            }
        }
        return factory.createPropertyArgument(argumentName, propDef.getPropertyDefinition().isQueryable(), propDef
                .getPropertyDefinition().isOrderable(), qualifier, propDef.getPropertyId());
    }

    private String unescape(String string, EscapeMode mode)
    {
        StringBuilder builder = new StringBuilder(string.length());

        boolean lastWasEscape = false;

        for (int i = 0; i < string.length(); i++)
        {
            char c = string.charAt(i);
            if (lastWasEscape)
            {

                // Need to keep escaping for like as we have the same escaping
                if (mode == EscapeMode.LIKE)
                {
                    // Like does its own escaping - so pass through \ % and _
                    if (c == '\'')
                    {
                        builder.append(c);
                    } else if (c == '%')
                    {
                        builder.append('\\');
                        builder.append(c);
                    } else if (c == '_')
                    {
                        builder.append('\\');
                        builder.append(c);
                    } else if (c == '\\')
                    {
                        builder.append('\\');
                        builder.append(c);
                    } else
                    {
                        throw new UnsupportedOperationException("Unsupported escape pattern in <" + string
                                + "> at position " + i);
                    }
                } else if (mode == EscapeMode.CONTAINS)
                {
                    if (options.getQueryMode() == CMISQueryMode.CMS_STRICT)
                    {
                        if (c == '\'')
                        {
                            builder.append(c);
                        } else if (c == '\\')
                        {
                            builder.append('\\');
                            builder.append(c);
                        }

                        else
                        {
                            throw new UnsupportedOperationException("Unsupported escape pattern in <" + string
                                    + "> at position " + i);
                        }
                    } else
                    {
                        if (c == '\'')
                        {
                            builder.append(c);
                        } else
                        {
                            builder.append(c);
                        }
                    }
                } else if (mode == EscapeMode.LITERAL)
                {
                    if (c == '\'')
                    {
                        builder.append(c);
                    } else if (c == '\\')
                    {
                        builder.append(c);
                    } else
                    {
                        throw new UnsupportedOperationException("Unsupported escape pattern in <" + string
                                + "> at position " + i);

                    }
                } else
                {
                    throw new UnsupportedOperationException("Unsupported escape pattern in <" + string
                            + "> at position " + i);

                }
                lastWasEscape = false;
            } else
            {
                if (c == '\\')
                {
                    lastWasEscape = true;
                } else
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
