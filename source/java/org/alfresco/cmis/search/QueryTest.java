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
package org.alfresco.cmis.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.cmis.dictionary.BaseCMISTest;
import org.alfresco.cmis.dictionary.CMISCardinality;
import org.alfresco.cmis.dictionary.CMISMapping;
import org.alfresco.cmis.dictionary.CMISPropertyDefinition;
import org.alfresco.cmis.dictionary.CMISScope;
import org.alfresco.cmis.dictionary.CMISTypeId;
import org.alfresco.repo.search.impl.parsers.CMISLexer;
import org.alfresco.repo.search.impl.parsers.CMISParser;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.ArgumentDefinition;
import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.Function;
import org.alfresco.repo.search.impl.querymodel.JoinType;
import org.alfresco.repo.search.impl.querymodel.QueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.Selector;
import org.alfresco.repo.search.impl.querymodel.Source;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Equals;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Lower;
import org.alfresco.repo.search.impl.querymodel.impl.functions.PropertyAccessor;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Score;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Upper;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneQueryModelFactory;
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
public class QueryTest extends BaseCMISTest
{
    public void testParse()
    {
        String input = "SELECT UPPER(1.0) AS WOOF FROM DOCUMENT_OBJECT_TYPE AS DOC LEFT OUTER JOIN FOLDER_OBJECT_TYPE AS FOLDER ON (DOC.NAME = FOLDER.NAME)";
        CMISParser parser = null;
        try
        {
            CharStream cs = new ANTLRStringStream(input);
            CMISLexer lexer = new CMISLexer(cs);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            parser = new CMISParser(tokens);
            CommonTree queryNode = (CommonTree) parser.query().getTree();
            assertTrue(queryNode.getType() == CMISParser.QUERY);

            QueryModelFactory factory = new LuceneQueryModelFactory();

            CommonTree sourceNode = (CommonTree) queryNode.getFirstChildWithType(CMISParser.SOURCE);
            Source s = buildSource(sourceNode, true, factory);
            Map<String, Selector> selectors = s.getSelectors();
            ArrayList<Column> columns = buildColumns(queryNode, factory, selectors);

            System.out.println(s);
            System.out.println(selectors);
            System.out.println(columns);
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

    }

    private ArrayList<Column> buildColumns(CommonTree queryNode, QueryModelFactory factory, Map<String, Selector> selectors)
    {
        ArrayList<Column> columns = new ArrayList<Column>();
        CommonTree starNode = (CommonTree) queryNode.getFirstChildWithType(CMISParser.ALL_COLUMNS);
        if (starNode != null)
        {
            for (Selector selector : selectors.values())
            {
                QName cmisType = CMISMapping.getCmisType(selector.getType());
                CMISTypeId typeId = null;
                if (CMISMapping.isValidCmisDocument(dictionaryService, cmisType))
                {
                    typeId = CMISMapping.getCmisTypeId(CMISScope.DOCUMENT, cmisType);
                }
                else if (CMISMapping.isValidCmisFolder(dictionaryService, cmisType))
                {
                    typeId = CMISMapping.getCmisTypeId(CMISScope.FOLDER, cmisType);
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
                        QName propertyQName = CMISMapping.getPropertyQName(dictionaryService, serviceRegistry.getNamespaceService(), definition.getPropertyName());
                        Argument arg = factory.createPropertyArgument(PropertyAccessor.ARG_PROPERTY, selector.getAlias(), propertyQName);
                        List<Argument> functionArguments = new ArrayList<Argument>(1);
                        functionArguments.add(arg);
                        Column column = factory.createColumn(function, functionArguments, selector.getAlias() + "." + definition.getPropertyName());
                        columns.add(column);
                    }
                }
            }
        }

        CommonTree columnsNode = (CommonTree) queryNode.getFirstChildWithType(CMISParser.COLUMNS);
        if (columnsNode != null)
        {
            CommonTree allColumnsNode = (CommonTree) columnsNode.getFirstChildWithType(CMISParser.ALL_COLUMNS);
            if (allColumnsNode != null)
            {
                String qualifier = allColumnsNode.getChild(0).getText();
                Selector selector = selectors.get(qualifier);
                if (selector == null)
                {
                    throw new CMISQueryException("No selector for " + qualifier + " in " + qualifier + ".*");
                }
                QName cmisType = CMISMapping.getCmisType(selector.getType());
                CMISTypeId typeId = null;
                if (CMISMapping.isValidCmisDocument(dictionaryService, cmisType))
                {
                    typeId = CMISMapping.getCmisTypeId(CMISScope.DOCUMENT, cmisType);
                }
                else if (CMISMapping.isValidCmisFolder(dictionaryService, cmisType))
                {
                    typeId = CMISMapping.getCmisTypeId(CMISScope.FOLDER, cmisType);
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
                        QName propertyQName = CMISMapping.getPropertyQName(dictionaryService, serviceRegistry.getNamespaceService(), definition.getPropertyName());
                        Argument arg = factory.createPropertyArgument(PropertyAccessor.ARG_PROPERTY, selector.getAlias(), propertyQName);
                        List<Argument> functionArguments = new ArrayList<Argument>(1);
                        functionArguments.add(arg);
                        Column column = factory.createColumn(function, functionArguments, selector.getAlias() + "." + definition.getPropertyName());
                        columns.add(column);
                    }
                }
            }

            CommonTree columnNode = (CommonTree) columnsNode.getFirstChildWithType(CMISParser.COLUMN);
            if (columnNode != null)
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
                    QName cmisType = CMISMapping.getCmisType(selector.getType());
                    CMISTypeId typeId = null;
                    if (CMISMapping.isValidCmisDocument(dictionaryService, cmisType))
                    {
                        typeId = CMISMapping.getCmisTypeId(CMISScope.DOCUMENT, cmisType);
                    }
                    else if (CMISMapping.isValidCmisFolder(dictionaryService, cmisType))
                    {
                        typeId = CMISMapping.getCmisTypeId(CMISScope.FOLDER, cmisType);
                    }
                    else
                    {
                        throw new CMISQueryException("Type unsupported in CMIS queries: " + selector.getAlias());
                    }
                    CMISPropertyDefinition definition = cmisDictionaryService.getPropertyDefinition(typeId, columnName);

                    if (definition == null)
                    {
                        throw new CMISQueryException("Invalid column for " + CMISMapping.getQueryName(namespaceService, typeId.getQName()) + "." + columnName);
                    }

                    Function function = factory.getFunction(PropertyAccessor.NAME);
                    QName propertyQName = CMISMapping.getPropertyQName(dictionaryService, serviceRegistry.getNamespaceService(), definition.getPropertyName());
                    Argument arg = factory.createPropertyArgument(PropertyAccessor.ARG_PROPERTY, selector.getAlias(), propertyQName);
                    List<Argument> functionArguments = new ArrayList<Argument>(1);
                    functionArguments.add(arg);

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
                    Set<ArgumentDefinition> definitions = function.getArgumentDefinitions();
                    List<Argument> functionArguments = new ArrayList<Argument>();

                    int childIndex = 1;
                    for (ArgumentDefinition definition : definitions)
                    {
                        if (functionNode.getChildCount() > childIndex)
                        {
                            CommonTree argNode = (CommonTree) functionNode.getChild(childIndex++);
                            if (argNode.getType() == CMISParser.COLUMN_REF)
                            {
                                Argument arg = buildColumnReference(definition.getName(), argNode, factory);
                                functionArguments.add(arg);
                            }
                            else if (argNode.getType() == CMISParser.ID)
                            {
                                Argument arg;
                                String id = argNode.getText();
                                if(selectors.containsKey(id))
                                {
                                    arg = factory.createSelectorArgument(definition.getName(), id);
                                }
                                else
                                {
                                    QName propertyQName = CMISMapping.getPropertyQName(dictionaryService, serviceRegistry.getNamespaceService(), id);
                                    arg = factory.createPropertyArgument(definition.getName(), "", propertyQName);
                                }
                                functionArguments.add(arg);
                            }
                            else if(argNode.getType() == CMISParser.PARAMETER)
                            {   
                                Argument arg = factory.createParameterArgument(definition.getName(), argNode.getText());
                                functionArguments.add(arg);
                            }
                            else if(argNode.getType() == CMISParser.NUMERIC_LITERAL)
                            {
                                CommonTree literalNode = (CommonTree) argNode.getChild(0);
                                if(literalNode.getType() == CMISParser.FLOATING_POINT_LITERAL)
                                {
                                    QName type = DataTypeDefinition.DOUBLE;
                                    Number value = Double.parseDouble(literalNode.getText());
                                    if(value.floatValue() == value.doubleValue())
                                    {
                                        type = DataTypeDefinition.FLOAT;
                                        value = Float.valueOf(value.floatValue());
                                    }
                                    Argument arg = factory.createLiteralArgument(definition.getName(), type, value);
                                    functionArguments.add(arg);
                                }
                                else if(literalNode.getType() == CMISParser.DECIMAL_INTEGER_LITERAL)
                                {
                                    QName type = DataTypeDefinition.LONG;
                                    Number value = Long.parseLong(literalNode.getText());
                                    if(value.intValue() == value.longValue())
                                    {
                                        type = DataTypeDefinition.INT;
                                        value = Integer.valueOf(value.intValue());
                                    }
                                    Argument arg = factory.createLiteralArgument(definition.getName(), type, value);
                                    functionArguments.add(arg);
                                }
                                else
                                {
                                    throw new CMISQueryException("Invalid numeric literal "+literalNode.getText());
                                }
                            }
                            else if(argNode.getType() == CMISParser.STRING_LITERAL)
                            {
                                Argument arg = factory.createLiteralArgument(definition.getName(), DataTypeDefinition.TEXT, argNode.getChild(0).getText());
                                functionArguments.add(arg);
                            }
                            else
                            {
                                throw new CMISQueryException("Invalid query argument "+argNode.getText());
                            }
                        }
                        else
                        {
                            if (definition.isMandatory())
                            {
                                //throw new CMISQueryException("Insufficient aruments for function " + ((CommonTree) functionNode.getChild(0)).getText() );
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

        return columns;
    }

    @SuppressWarnings("unchecked")
    private Source buildSource(CommonTree source, boolean supportJoins, QueryModelFactory factory)
    {
        if (source.getChildCount() == 1)
        {
            // single table reference
            CommonTree singleTableNode = (CommonTree) source.getChild(0);
            if (singleTableNode.getType() == CMISParser.TABLE)
            {
                if (!supportJoins)
                {
                    throw new UnsupportedOperationException("Joins are not supported");
                }
                CommonTree tableSourceNode = (CommonTree) singleTableNode.getFirstChildWithType(CMISParser.SOURCE);
                return buildSource(tableSourceNode, false, factory);

            }
            assertTrue(singleTableNode.getType() == CMISParser.TABLE_REF);
            String tableName = singleTableNode.getChild(0).getText();
            String alias = "";
            if (singleTableNode.getChildCount() > 1)
            {
                alias = singleTableNode.getChild(1).getText();
            }
            QName classQName = CMISMapping.getAlfrescoClassQNameFromCmisTableName(dictionaryService, serviceRegistry.getNamespaceService(), tableName);
            return factory.createSelector(classQName, alias);
        }
        else
        {
            if (!supportJoins)
            {
                throw new UnsupportedOperationException("Joins are not supported");
            }
            CommonTree singleTableNode = (CommonTree) source.getChild(0);
            assertTrue(singleTableNode.getType() == CMISParser.TABLE_REF);
            String tableName = singleTableNode.getChild(0).getText();
            String alias = "";
            if (singleTableNode.getChildCount() == 2)
            {
                alias = singleTableNode.getChild(1).getText();
            }
            QName classQName = CMISMapping.getAlfrescoClassQNameFromCmisTableName(dictionaryService, serviceRegistry.getNamespaceService(), tableName);
            Source lhs = factory.createSelector(classQName, alias);

            List<CommonTree> list = (List<CommonTree>) (source.getChildren());
            for (CommonTree joinNode : list)
            {
                if (joinNode.getType() == CMISParser.JOIN)
                {
                    CommonTree rhsSource = (CommonTree) joinNode.getFirstChildWithType(CMISParser.SOURCE);
                    Source rhs = buildSource(rhsSource, supportJoins, factory);

                    JoinType joinType = JoinType.INNER;
                    CommonTree joinTypeNode = (CommonTree) joinNode.getFirstChildWithType(CMISParser.LEFT);
                    if (joinTypeNode != null)
                    {
                        joinType = JoinType.LEFT;
                    }

                    Constraint joinCondition = null;
                    CommonTree joinConditionNode = (CommonTree) joinNode.getFirstChildWithType(CMISParser.ON);
                    if (joinConditionNode != null)
                    {
                        Argument arg1 = buildColumnReference(Equals.ARG_LHS, (CommonTree) joinConditionNode.getChild(0), factory);
                        String functionName = getFunctionName((CommonTree) joinConditionNode.getChild(1));
                        Argument arg2 = buildColumnReference(Equals.ARG_RHS, (CommonTree) joinConditionNode.getChild(2), factory);
                        Function function = factory.getFunction(functionName);
                        List<Argument> functionArguments = new ArrayList<Argument>(2);
                        functionArguments.add(arg1);
                        functionArguments.add(arg2);
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
        QName propertyQName = CMISMapping.getPropertyQName(dictionaryService, serviceRegistry.getNamespaceService(), cmisPropertyName);
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
