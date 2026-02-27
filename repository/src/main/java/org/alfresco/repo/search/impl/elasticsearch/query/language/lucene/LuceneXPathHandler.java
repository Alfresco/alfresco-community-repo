/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.query.language.lucene;

import java.util.Stack;

import org.apache.lucene.index.Term;
import org.apache.lucene.queries.spans.SpanNearQuery;
import org.apache.lucene.queries.spans.SpanQuery;
import org.apache.lucene.queries.spans.SpanTermQuery;
import org.jaxen.saxpath.Axis;
import org.jaxen.saxpath.Operator;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.saxpath.XPathHandler;

import org.alfresco.util.ISO9075;

public class LuceneXPathHandler implements XPathHandler
{
    /**
     * Elasticsearch does not support span near queries with unlimited slop. Instead we support wildcard xpaths by using a hard coded maximum that is larger than we expect any path query to use.
     */
    public static final int MAX_PATH_LENGTH = 1000;
    private Stack<SpanNearQuery.Builder> closedSpans;
    private SpanNearQuery.Builder openSpanNear;

    private boolean isAbsolutePath = true;

    private String field;

    public LuceneXPathHandler(String field)
    {
        super();
        this.field = field;
    }

    @Override
    public void startXPath()
    {
        closedSpans = new Stack<>();
        openSpanNear = SpanNearQuery.newOrderedNearQuery(field);
    }

    @Override
    public void endXPath()
    {
        // no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    /**
     * Get the resulting query
     *
     * @return - the query
     */
    public SpanQuery getQuery()
    {
        if (closedSpans.isEmpty())
        {
            isAbsolutePath = true;
            return getFullPathQuery(openSpanNear);
        }

        SpanNearQuery.Builder last = closedSpans.pop();
        boolean singleSpanNearQuery = closedSpans.isEmpty();
        if (singleSpanNearQuery)
        {
            return getFullPathQuery(last);
        }
        else
        {
            SpanNearQuery.Builder beforeLast = closedSpans.pop();
            SpanNearQuery.Builder lastBlock = SpanNearQuery.newOrderedNearQuery(field);
            lastBlock.addClause(beforeLast.build());
            lastBlock.addClause(getFullPathQuery(last));
            lastBlock.setSlop(MAX_PATH_LENGTH);
            while (!closedSpans.isEmpty())
            {
                SpanNearQuery.Builder currentNearBlock = SpanNearQuery.newOrderedNearQuery(field);
                currentNearBlock.addClause(closedSpans.pop().build());
                currentNearBlock.addClause(lastBlock.build());
                currentNearBlock.setSlop(MAX_PATH_LENGTH);
                lastBlock = currentNearBlock;
            }
            return lastBlock.build();
        }
    }

    /**
     * This query matches an exact full path indexed for a node. At indexing time the analyzer adds a ^ to mark the start of a path and a $ to mark the end. Adding these symbols to the positional span query makes sure we just match only nodes with the exact full path. e.g. q = /a/b/c -> q = '^' near 'a' near 'b' near 'c' near '$'
     *
     * D1 path= /a/b/c/d -> ^/a/b/c/d/$ D2 path= /d/a/b/c -> ^/d/a/b/c/$ D3 path= /a/b/c -> ^/a/b/c/$
     *
     * Only D2 matches
     *
     * @param latestSequentialBlock
     *            latest sequence of adjacent / block
     * @return
     */
    private SpanQuery getFullPathQuery(SpanNearQuery.Builder latestSequentialBlock)
    {
        latestSequentialBlock.addClause(new SpanTermQuery(new Term(field, "$")));
        SpanNearQuery resultSpanQuery = latestSequentialBlock.build();
        return resultSpanQuery;
    }

    /**
     * An absolute location xPath starts with /
     */
    @Override
    public void startAbsoluteLocationPath()
    {
        if (!isAbsolutePath)
        {
            throw new IllegalStateException();
        }
        openSpanNear.addClause(new SpanTermQuery(new Term(field, "^")));
    }

    /**
     * A relative location xPath starts with //
     */
    @Override
    public void startRelativeLocationPath()
    {
        isAbsolutePath = false;
    }

    /**
     * Any time we meet a //, we close a near span e.g. /a/b//c Once we meet // we close the /a/b span
     *
     * @param axis
     */
    @Override
    public void startAllNodeStep(int axis)
    {
        closeSpanBlock();
    }

    /**
     * Any time an absolute XPath ends, we close a near span e.g. /a/b//c Once we meet the end we close the c span
     *
     */
    @Override
    public void endAbsoluteLocationPath()
    {
        closeSpanBlock();
    }

    /**
     * Any time a relative XPath ends, we close a near span e.g. /a/b//* Once we meet the end we close the * span adding a wildcard span term
     *
     */
    @Override
    public void endRelativeLocationPath()
    {
        closeSpanBlock();
    }

    private void closeSpanBlock()
    {
        if (openSpanNear.build().getClauses().length > 0)
        {
            openSpanNear.setSlop(0);
            closedSpans.push(openSpanNear);
            openSpanNear = SpanNearQuery.newOrderedNearQuery(field);
        }
        else
        {
            isAbsolutePath = false;
        }
    }

    /**
     * Any time we meet a namespace:localName in the path we reach this point: e.g. /namespace:a//namespace:b namespace:a calls this method descending a child axis namespace:b calls this method descending a child axis
     *
     * @param axis
     *            - the XPath axis we are descending into
     * @param nameSpace
     *            - the nameSpace of the current element
     * @param localName
     *            - the local name of the current element
     */
    @Override
    public void startNameStep(int axis, String nameSpace, String localName)
    {
        switch (axis)
        {
        case Axis.CHILD:
            SpanQuery namespaceQuery;
            SpanQuery localNameQuery;

            if (nameSpace.isEmpty())
            {
                namespaceQuery = new SpanTermQuery(new Term(field, "*"));
            }
            else
            {
                namespaceQuery = new SpanTermQuery(new Term(field, nameSpace));
            }

            if (localName.isEmpty())
            {
                localNameQuery = new SpanTermQuery(new Term(field, "*"));
            }
            else
            {
                localNameQuery = new SpanTermQuery(new Term(field, ISO9075.decode(localName)));
            }

            openSpanNear.addClause(namespaceQuery);
            openSpanNear.addClause(localNameQuery);
            break;
        default:
            throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
        }

    }

    @Override
    public void endNameStep()
    {
        // no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void endAllNodeStep()
    {
        // no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void endAdditiveExpr(int op)
    {
        switch (op)
        {
        case Operator.NO_OP:
            break;
        case Operator.ADD:
        case Operator.SUBTRACT:
            throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
        default:
            throw new UnsupportedOperationException("Unknown operation " + op);// no behaviour was implemented for this feature in the original implementation for Apache Solr
        }
    }

    @Override
    public void endAndExpr(boolean create)
    {
        if (create)
        {
            throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
        }
    }

    @Override
    public void endCommentNodeStep()
    {
        throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void endEqualityExpr(int op)
    {
        switch (op)
        {
        case Operator.NO_OP:
            break;
        case Operator.EQUALS:
        case Operator.NOT_EQUALS:
            throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
        default:
            throw new UnsupportedOperationException("Unknown operation " + op);// no behaviour was implemented for this feature in the original implementation for Apache Solr
        }
    }

    @Override
    public void endFilterExpr()
    {
        throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void endFunction()
    {
        throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void endMultiplicativeExpr(int op)
    {
        switch (op)
        {
        case Operator.NO_OP:
            break;
        case Operator.MULTIPLY:
        case Operator.DIV:
        case Operator.MOD:
            throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
        default:
            throw new UnsupportedOperationException("Unknown operation " + op);// no behaviour was implemented for this feature in the original implementation for Apache Solr
        }
    }

    @Override
    public void endOrExpr(boolean create)
    {
        if (create)
        {
            throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
        }
    }

    @Override
    public void endPathExpr()
    {
        // no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void endPredicate()
    {
        throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void endProcessingInstructionNodeStep()
    {
        throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void endRelationalExpr(int op)
    {
        switch (op)
        {
        case Operator.NO_OP:
            break;
        case Operator.GREATER_THAN:
        case Operator.GREATER_THAN_EQUALS:
        case Operator.LESS_THAN:
        case Operator.LESS_THAN_EQUALS:
            throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
        default:
            throw new UnsupportedOperationException("Unknown operation " + op);// no behaviour was implemented for this feature in the original implementation for Apache Solr
        }
    }

    @Override
    public void endTextNodeStep()
    {
        throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void endUnaryExpr(int op)
    {
        switch (op)
        {
        case Operator.NO_OP:
            break;
        case Operator.NEGATIVE:
            throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
        default:
            throw new UnsupportedOperationException("Unknown operation " + op);// no behaviour was implemented for this feature in the original implementation for Apache Solr
        }
    }

    @Override
    public void endUnionExpr(boolean create)
    {
        if (create)
        {
            throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
        }
    }

    @Override
    public void literal(String arg0) throws SAXPathException
    {
        throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void number(double arg0) throws SAXPathException
    {
        throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void number(int arg0) throws SAXPathException
    {
        throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void startAdditiveExpr()
    {
        // no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void startAndExpr()
    {
        // no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void startCommentNodeStep(int arg0)
    {
        throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void startEqualityExpr()
    {
        // no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void startFilterExpr()
    {
        throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void startFunction(String arg0, String arg1)
    {
        throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void startMultiplicativeExpr()
    {
        // no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void startOrExpr()
    {
        // no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void startPathExpr()
    {
        // no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void startPredicate()
    {
        throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void startProcessingInstructionNodeStep(int arg0, String arg1)
    {
        throw new UnsupportedOperationException();// no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void startRelationalExpr()
    {
        // no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void startTextNodeStep(int arg0)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startUnaryExpr()
    {
        // no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void startUnionExpr()
    {
        // no behaviour was implemented for this feature in the original implementation for Apache Solr
    }

    @Override
    public void variableReference(String uri, String localName)
    {
        // no behaviour was implemented for this feature in the original implementation for Apache Solr
    }
}
