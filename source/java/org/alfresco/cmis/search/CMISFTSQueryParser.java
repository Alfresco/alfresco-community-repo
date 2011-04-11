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
package org.alfresco.cmis.search;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.search.impl.lucene.AnalysisMode;
import org.alfresco.repo.search.impl.parsers.CMIS_FTSLexer;
import org.alfresco.repo.search.impl.parsers.CMIS_FTSParser;
import org.alfresco.repo.search.impl.parsers.FTSQueryException;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.Function;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.LiteralArgument;
import org.alfresco.repo.search.impl.querymodel.QueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.Selector;
import org.alfresco.repo.search.impl.querymodel.Constraint.Occur;
import org.alfresco.repo.search.impl.querymodel.QueryOptions.Connective;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSPhrase;
import org.alfresco.repo.search.impl.querymodel.impl.functions.FTSTerm;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

public class CMISFTSQueryParser
{
    static public Constraint buildFTS(String ftsExpression, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext, Selector selector,
            Map<String, Column> columnMap, String defaultField)
    {
        // TODO: Decode sql escape for '' should do in CMIS layer

        // parse templates to trees ...

        CMIS_FTSParser parser = null;
        try
        {
            CharStream cs = new ANTLRStringStream(ftsExpression);
            CMIS_FTSLexer lexer = new CMIS_FTSLexer(cs);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            parser = new CMIS_FTSParser(tokens);
            CommonTree ftsNode = (CommonTree) parser.cmisFtsQuery().getTree();
            return buildFTSConnective(ftsNode, factory, functionEvaluationContext, selector, columnMap, defaultField);
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

    static private Constraint buildFTSConnective(CommonTree node, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, Map<String, Column> columnMap, String defaultField)
    {
        Connective connective;
        switch (node.getType())
        {
        case CMIS_FTSParser.DISJUNCTION:
            connective = Connective.OR;
            break;
        case CMIS_FTSParser.CONJUNCTION:
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
            case CMIS_FTSParser.DISJUNCTION:
            case CMIS_FTSParser.CONJUNCTION:
                constraint = buildFTSConnective(subNode, factory, functionEvaluationContext, selector, columnMap, defaultField);
                break;
            case CMIS_FTSParser.DEFAULT:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(testNode, factory, functionEvaluationContext, selector, columnMap, defaultField);
                constraint.setOccur(Occur.DEFAULT);
                break;
            case CMIS_FTSParser.EXCLUDE:
                testNode = (CommonTree) subNode.getChild(0);
                constraint = buildFTSTest(testNode, factory, functionEvaluationContext, selector, columnMap, defaultField);
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

    static private Constraint buildFTSTest(CommonTree argNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, Map<String, Column> columnMap, String defaultField)
    {
        CommonTree testNode = argNode;
        switch (testNode.getType())
        {
        case CMIS_FTSParser.DISJUNCTION:
        case CMIS_FTSParser.CONJUNCTION:
            return buildFTSConnective(testNode, factory, functionEvaluationContext, selector, columnMap, defaultField);
        case CMIS_FTSParser.TERM:
            return buildTerm(testNode, factory, functionEvaluationContext, selector, columnMap);
        case CMIS_FTSParser.PHRASE:
            return buildPhrase(testNode, factory, functionEvaluationContext, selector, columnMap);
        default:
            throw new FTSQueryException("Unsupported FTS option " + testNode.getText());
        }
    }

    static private Constraint buildPhrase(CommonTree testNode, QueryModelFactory factory,
            FunctionEvaluationContext functionEvaluationContext, Selector selector, Map<String, Column> columnMap)
    {
        String functionName = FTSPhrase.NAME;
        Function function = factory.getFunction(functionName);
        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
        LiteralArgument larg = factory.createLiteralArgument(FTSPhrase.ARG_PHRASE, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
        functionArguments.put(larg.getName(), larg);
        larg = factory.createLiteralArgument(FTSPhrase.ARG_TOKENISATION_MODE, DataTypeDefinition.ANY, AnalysisMode.DEFAULT);
        functionArguments.put(larg.getName(), larg);
        return factory.createFunctionalConstraint(function, functionArguments);
    }

    static private Constraint buildTerm(CommonTree testNode, QueryModelFactory factory, FunctionEvaluationContext functionEvaluationContext,
            Selector selector, Map<String, Column> columnMap)
    {
        String functionName = FTSTerm.NAME;
        Function function = factory.getFunction(functionName);
        Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
        LiteralArgument larg = factory.createLiteralArgument(FTSTerm.ARG_TERM, DataTypeDefinition.TEXT, getText(testNode.getChild(0)));
        functionArguments.put(larg.getName(), larg);
        larg = factory.createLiteralArgument(FTSTerm.ARG_TOKENISATION_MODE, DataTypeDefinition.ANY, AnalysisMode.DEFAULT);
        functionArguments.put(larg.getName(), larg);
        return factory.createFunctionalConstraint(function, functionArguments);
    }

 
    static class DisjunctionToken implements Token
    {

        public int getChannel()
        {
            return 0;
        }

        public int getCharPositionInLine()
        {
            return 0;
        }

        public CharStream getInputStream()
        {
            return null;
        }

        public int getLine()
        {
            return 0;
        }

        public String getText()
        {
            return null;
        }

        public int getTokenIndex()
        {
            return 0;
        }

        public int getType()
        {
            return CMIS_FTSParser.DISJUNCTION;
        }

        public void setChannel(int arg0)
        {
            

        }

        public void setCharPositionInLine(int arg0)
        {
          

        }

        public void setInputStream(CharStream arg0)
        {
        
        }

        public void setLine(int arg0)
        {
         

        }

        public void setText(String arg0)
        {
           

        }

        public void setTokenIndex(int arg0)
        {
          
        }

        public void setType(int arg0)
        {
           
        }

    }

    static class DefaultToken implements Token
    {

        public int getChannel()
        {
           
            return 0;
        }

        public int getCharPositionInLine()
        {
            return 0;
        }

        public CharStream getInputStream()
        {
            return null;
        }

        public int getLine()
        {
            return 0;
        }

        public String getText()
        {
            return null;
        }

        public int getTokenIndex()
        {
            return 0;
        }

        public int getType()
        {
            return CMIS_FTSParser.DEFAULT;
        }

        public void setChannel(int arg0)
        {
           
        }

        public void setCharPositionInLine(int arg0)
        {
           
        }

        public void setInputStream(CharStream arg0)
        {
           
        }

        public void setLine(int arg0)
        {
            
        }

        public void setText(String arg0)
        {
           
        }

        public void setTokenIndex(int arg0)
        {
            
        }

        public void setType(int arg0)
        {
           
        }

    }

    static private String getText(Tree node)
    {
        String text = node.getText();
        int index;
        switch (node.getType())
        {
        case CMIS_FTSParser.FTSWORD:
            index = text.indexOf('\\');
            if (index == -1)
            {
                return text;
            }
            else
            {
                return unescape(text);
            }
        case CMIS_FTSParser.FTSPHRASE:
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
        default:
            return text;
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
