/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.framework.tests.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.antlr.runtime.tree.CommonTree;
import org.junit.Test;

import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.framework.resource.parameters.where.InvalidQueryException;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper.WalkerCallbackAdapter;
import org.alfresco.rest.framework.tools.RecognizedParamsExtractor;

public class WhereTests implements RecognizedParamsExtractor
{

    @Test
    public void basicTest() throws IOException
    {

        Query theQuery = getWhereClause(" ( fred   > g ) ");
        CommonTree ast = theQuery.getTree();
        // check AST structure
        assertEquals(WhereClauseParser.GREATERTHAN, ast.getType());
        assertEquals(WhereClauseParser.PROPERTYNAME, ast.getChild(0).getType());
        assertEquals(WhereClauseParser.PROPERTYVALUE, ast.getChild(1).getType());
    }

    @Test
    public void existClauseTest()
    {
        Query theQuery = getWhereClause(null);
        assertNotNull(theQuery);
        assertTrue("Null passed in so nothing to theQuery.", theQuery.getTree() == null);

        try
        {
            theQuery = getWhereClause("fred");
            fail("Should throw an InvalidQueryException");
        }
        catch (InvalidQueryException error)
        {
            // this is correct
        }

        try
        {
            theQuery = getWhereClause("(noClosingBracket");
            fail("Should throw an InvalidQueryException");
        }
        catch (InvalidQueryException error)
        {
            // this is correct
        }

        try
        {
            theQuery = getWhereClause("noOpeningBracket)");
            fail("Should throw an InvalidQueryException");
        }
        catch (InvalidQueryException error)
        {
            // this is correct
        }

        try
        {
            theQuery = getWhereClause("(EXISTS(target.file))");
            fail("Should throw an InvalidQueryException");
        }
        catch (InvalidQueryException error)
        {
            // this is correct
        }

        theQuery = getWhereClause("(exists(/target/file))");
        assertExistsPropertyEquals("/target/file", theQuery, false);

        theQuery = getWhereClause("(EXISTS(b))");
        assertExistsPropertyEquals("b", theQuery, false);

        theQuery = getWhereClause("  ( EXISTS ( whitespace ) )  ");
        assertExistsPropertyEquals("whitespace", theQuery, false);

        theQuery = getWhereClause("(exists ( folder ))");
        assertExistsPropertyEquals("folder", theQuery, false);

        theQuery = getWhereClause("(NOT EXISTS(b))");
        assertExistsPropertyEquals("b", theQuery, true);

        theQuery = getWhereClause(" (NOT EXISTS(b))");
        assertExistsPropertyEquals("b", theQuery, true);

        theQuery = getWhereClause("(  NOT EXISTS(b))");
        assertExistsPropertyEquals("b", theQuery, true);

        theQuery = getWhereClause("  (  NOT EXISTS(b))");
        assertExistsPropertyEquals("b", theQuery, true);

        try
        {
            theQuery = getWhereClause("(exists  folder)");
            fail("Should throw an InvalidQueryException, 'folder' should have a bracket around it");
        }
        catch (InvalidQueryException error)
        {
            // this is correct
        }

        theQuery = getWhereClause("(EXISTS(/target/folder) AND NOT EXISTS(/target/site))");
        assertNotNull(theQuery);
        CommonTree tree = theQuery.getTree();
        assertNotNull(tree);
        QueryHelper.walk(theQuery, new WalkerCallbackAdapter() {
            int i = 0;

            @Override
            public void exists(String propertyName, boolean negated)
            {
                if (i == 0)
                {
                    assertTrue("/target/folder".equals(propertyName));
                }
                else
                {
                    assertTrue("/target/site".equals(propertyName));
                }
                i++;
            }

            @Override
            public void and()
            {
                // We don't need to do anything in this method. However, overriding the method indicates that AND is
                // supported. OR is not supported at the same time.
            }

        });

        try
        {
            theQuery = getWhereClause("(EXISTS(/target/folder)OR EXISTS(/target/site))");
            fail("Should throw an InvalidQueryException, the OR should have a space before it.");
        }
        catch (InvalidQueryException error)
        {
            // this is correct
        }

        theQuery = getWhereClause("(NOT EXISTS(/target/folder) OR EXISTS(/target/site))");
        QueryHelper.walk(theQuery, new WalkerCallbackAdapter() {
            @Override
            public void exists(String propertyName, boolean negated)
            {
                if (negated)
                {
                    assertTrue("/target/folder".equals(propertyName));
                }
                else
                {
                    assertTrue("/target/site".equals(propertyName));
                }
            }

            @Override
            public void or()
            {
                // We don't need to do anything in this method. However, overriding the method indicates that OR is
                // supported. AND is not supported at the same time.
            }

        });

        theQuery = getWhereClause("(EXISTS   (  /target/folder  )   OR   EXISTS(  /target/site  )  )");
        QueryHelper.walk(theQuery, new WalkerCallbackAdapter() {
            int i = 0;

            @Override
            public void exists(String propertyName, boolean negated)
            {
                if (i == 0)
                {
                    assertTrue("/target/folder".equals(propertyName));
                }
                else
                {
                    assertTrue("/target/site".equals(propertyName));
                }
                i++;
            }

            @Override
            public void or()
            {
                // We don't need to do anything in this method. However, overriding the method indicates that OR is
                // supported. AND is not supported at the same time.
            }

        });

        theQuery = getWhereClause("(EXISTS(target/file) AND EXISTS(target/folder) AND EXISTS(target/site))");
        QueryHelper.walk(theQuery, new WalkerCallbackAdapter() {
            int i = 0;

            @Override
            public void exists(String propertyName, boolean negated)
            {
                switch (i)
                {
                case 0:
                    assertTrue("target/file".equals(propertyName));
                    break;
                case 1:
                    assertTrue("target/folder".equals(propertyName));
                    break;
                case 2:
                    assertTrue("target/site".equals(propertyName));
                    break;
                default:
                    break;
                }
                i++;
            }

            @Override
            public void and()
            {
                // We don't need to do anything in this method. However, overriding the method indicates that AND is
                // supported. OR is not supported at the same time.
            }

        });
    }

    @Test
    public void inClauseTest()
    {
        Query theQuery = getWhereClause("( dueAt in (5,8) )");
        inChecks(theQuery, "dueAt", "5", "8");

        theQuery = getWhereClause("( fred/bloggs in (head,elbow) )");
        inChecks(theQuery, "fred/bloggs", "head", "elbow");

        theQuery = getWhereClause("( nextOne in (5,8,4) )");
        inChecks(theQuery, "nextOne", "5", "8", "4");

        theQuery = getWhereClause("( nextOne in (5,56,fred) )");
        inChecks(theQuery, "nextOne", "5", "56", "fred");

        theQuery = getWhereClause("( nextOne in (5,56,'fred&') )");
        inChecks(theQuery, "nextOne", "5", "56", "fred&");

        theQuery = getWhereClause("( nextOne in ('me , you',56,egg) )");
        inChecks(theQuery, "nextOne", "me , you", "56", "egg");

        theQuery = getWhereClause("( NOT nextOne in (5,56,fred, king, kong, 'fred\\'^') )");
        CommonTree tree = theQuery.getTree();
        assertNotNull(tree);
        QueryHelper.walk(theQuery, new WalkerCallbackAdapter() {
            @Override
            public void in(String property, boolean negated, String... propertyValues)
            {
                assertTrue("Property name should be " + "nextOne", "nextOne".equals(property));
                assertTrue(negated);
                String[] values = {"5", "56", "fred", "king", "kong", "fred\\'^"};
                for (int i = 0; i < values.length; i++)
                {
                    assertTrue("Value must match:" + values[i], values[i].equals(propertyValues[i]));
                }
            }

        });
    }

    @Test
    public void betweenClauseTest()
    {
        Query theQuery = getWhereClause("( dueAt between (5,8) )");
        betweenChecks(theQuery, "dueAt", "5", "8");

        theQuery = getWhereClause("( fred/bloggs between (head,elbow) )");
        betweenChecks(theQuery, "fred/bloggs", "head", "elbow");

        try
        {
            theQuery = getWhereClause("( nextOne between (5,8,4) )");
            fail("Should throw an InvalidQueryException, between can have only two values.");
        }
        catch (InvalidQueryException error)
        {
            // this is correct
        }

        try
        {
            theQuery = getWhereClause("( nextOne between 5,8 )");
            fail("Should throw an InvalidQueryException, Need brackets.");
        }
        catch (InvalidQueryException error)
        {
            // this is correct
        }

        theQuery = getWhereClause("(NOT dueAt between (5,8) AND nextOne between (green,blue))");
        QueryHelper.walk(theQuery, new WalkerCallbackAdapter() {
            @Override
            public void between(String property, String firstVal, String secondVal, boolean negated)
            {
                if (negated)
                {
                    assertTrue("Property name should be " + "dueAt", "dueAt".equals(property));
                    assertTrue("First value should be " + "5", "5".equals(firstVal));
                    assertTrue("Second value should be " + "8", "8".equals(secondVal));
                }
                else
                {
                    assertTrue("Property name should be " + "nextOne", "nextOne".equals(property));
                    assertTrue("First value should be " + "green", "green".equals(firstVal));
                    assertTrue("Second value should be " + "blue", "blue".equals(secondVal));
                }
            }

            @Override
            public void and()
            {
                // do nothing
            }

        });
    }

    @Test
    public void matchesClauseTest()
    {
        Query theQuery = getWhereClause("(fred matches(bob))");
        matchesChecks(theQuery, "fred", "bob");

        theQuery = getWhereClause("( king/kong/hair/shoulders/knees/toes matches ('fred%') )");
        matchesChecks(theQuery, "king/kong/hair/shoulders/knees/toes", "fred%");

        theQuery = getWhereClause("( niceone matches (bob) )");
        matchesChecks(theQuery, "niceone", "bob");

        try
        {
            theQuery = getWhereClause("( fred matches bob )");
            fail("Should throw an InvalidQueryException, Need brackets.");
        }
        catch (InvalidQueryException error)
        {
            // this is correct
        }

    }

    @Test
    public void comparisonClauseTest()
    {
        Query theQuery = getWhereClause("( dueAt > '12.04.345' )");
        int comparisonOperator = WhereClauseParser.GREATERTHAN;
        comparisonChecks(theQuery, comparisonOperator, "dueAt", "12.04.345");

        theQuery = getWhereClause("( dueAt >= '12.04.345' )");
        comparisonOperator = WhereClauseParser.GREATERTHANOREQUALS;
        comparisonChecks(theQuery, comparisonOperator, "dueAt", "12.04.345");

        theQuery = getWhereClause("( dueAt < '12.04.345' )");
        comparisonOperator = WhereClauseParser.LESSTHAN;
        comparisonChecks(theQuery, comparisonOperator, "dueAt", "12.04.345");

        theQuery = getWhereClause("( dueAt <= '12.04.345' )");
        comparisonOperator = WhereClauseParser.LESSTHANOREQUALS;
        comparisonChecks(theQuery, comparisonOperator, "dueAt", "12.04.345");

        try
        {
            theQuery = getWhereClause("( Fred/Bloggs = %$NICE&* )");
            fail("Should throw an InvalidQueryException, needs single quotes");
        }
        catch (InvalidQueryException error)
        {
            // this is correct
        }

        theQuery = getWhereClause("( Fred/Bloggs = '%$NICE&*' )");
        comparisonOperator = WhereClauseParser.EQUALS;
        comparisonChecks(theQuery, comparisonOperator, "Fred/Bloggs", "%$NICE&*");

        try
        {
            theQuery = getWhereClause("( Ken = (456) )");
            fail("Should throw an InvalidQueryException, needs single quotes no brackets");
        }
        catch (InvalidQueryException error)
        {
            // this is correct
        }

        theQuery = getWhereClause("( Ken = '456' )");
        comparisonOperator = WhereClauseParser.EQUALS;
        comparisonChecks(theQuery, comparisonOperator, "Ken", "456");

        theQuery = getWhereClause("( DogHouse = 'Cat\\\'s House' )");
        comparisonOperator = WhereClauseParser.EQUALS;
        comparisonChecks(theQuery, comparisonOperator, "DogHouse", "Cat\\\'s House");

        theQuery = getWhereClause("( KING_KONG >= 'Mighty Mouse' )");
        comparisonOperator = WhereClauseParser.GREATERTHANOREQUALS;
        comparisonChecks(theQuery, comparisonOperator, "KING_KONG", "Mighty Mouse");

        // dueAt > and < + also string "like"
    }

    @Test
    public void stripLeadingTrailingQuotesTests()
    {
        assertTrue("".equals(QueryHelper.stripQuotes("")));
        assertTrue("g".equals(QueryHelper.stripQuotes("g")));
        assertTrue("  bob ".equals(QueryHelper.stripQuotes("  bob ")));
        assertTrue("  bob '".equals(QueryHelper.stripQuotes("  bob '")));
        assertTrue("  bob ".equals(QueryHelper.stripQuotes("'  bob '")));
        assertTrue("bob".equals(QueryHelper.stripQuotes("'bob'")));
    }

    @Test
    public void getChildrenTests()
    {
        Query theQuery = getWhereClause("(fred matches(bob))");
        assertNotNull(theQuery);
        CommonTree tree = theQuery.getTree();
        assertNotNull(tree);
        assertTrue(2 == QueryHelper.getChildren(tree).size());

        theQuery = getWhereClause("( dueAt between (5,8) )");
        assertNotNull(theQuery);
        tree = theQuery.getTree();
        assertNotNull(tree);
        assertTrue(3 == QueryHelper.getChildren(tree).size());

        theQuery = getWhereClause("(NOT EXISTS(b))");
        assertNotNull(theQuery);
        tree = theQuery.getTree();
        assertNotNull(tree);
        assertTrue(1 == QueryHelper.getChildren(tree).size());

        theQuery = getWhereClause("(EXISTS(/target/folder) AND EXISTS(/target/site))");
        assertNotNull(theQuery);
        tree = theQuery.getTree();
        assertNotNull(tree);
        assertTrue(2 == QueryHelper.getChildren(tree).size());
    }

    /**
     * Used by ComparisonClauseTest, validates the clause
     * 
     * @param theQuery
     *            Query
     * @param comparisonOperator
     *            One of EQUALS LESSTHAN GREATERTHAN LESSTHANOREQUALS GREATERTHANOREQUALS
     * @param propName
     *            String
     * @param propVal
     *            String
     */
    private void comparisonChecks(Query theQuery, final int comparisonOperator, final String propName, final String propVal)
    {
        assertNotNull(theQuery);
        CommonTree tree = theQuery.getTree();
        assertNotNull(tree);
        assertEquals(comparisonOperator, tree.getType());
        assertEquals(WhereClauseParser.PROPERTYNAME, tree.getChild(0).getType());
        assertTrue(propName.equals(tree.getChild(0).getText()));
        assertEquals(WhereClauseParser.PROPERTYVALUE, tree.getChild(1).getType());
        QueryHelper.walk(theQuery, new WalkerCallbackAdapter() {
            @Override
            public void comparison(int comparisonType, String propertyName, String propertyValue, boolean negated)
            {
                assertTrue("Property name should be " + propName, propName.equals(propertyName));
                assertTrue(comparisonOperator == comparisonType);
                assertTrue("Property value should be " + propVal, propVal.equals(propertyValue));
            }

        });
    }

    /**
     * Used by BetweenClauseTest, validates the clause
     * 
     * @param theQuery
     *            Query
     * @param propName
     *            String
     * @param firstValue
     *            String
     * @param secondValue
     *            String
     */
    private void betweenChecks(Query theQuery, final String propName, final String firstValue, final String secondValue)
    {
        assertNotNull(theQuery);
        CommonTree tree = theQuery.getTree();
        assertNotNull(tree);
        assertEquals(WhereClauseParser.BETWEEN, tree.getType());
        assertEquals(WhereClauseParser.PROPERTYNAME, tree.getChild(0).getType());
        assertTrue(propName.equals(tree.getChild(0).getText()));
        assertEquals(WhereClauseParser.PROPERTYVALUE, tree.getChild(1).getType());
        assertEquals(WhereClauseParser.PROPERTYVALUE, tree.getChild(2).getType());
        QueryHelper.walk(theQuery, new WalkerCallbackAdapter() {
            @Override
            public void between(String property, String firstVal, String secondVal, boolean negated)
            {
                assertTrue("Property name should be " + propName, propName.equals(property));
                assertTrue("First value should be " + firstValue, firstValue.equals(firstVal));
                assertTrue("Second value should be " + secondValue, secondValue.equals(secondVal));
            }

        });
    }

    /**
     * Used by the matchesClauseTest
     * 
     * @param theQuery
     *            Query
     * @param propName
     *            String
     * @param propVal
     *            String
     */
    private void matchesChecks(Query theQuery, final String propName, final String propVal)
    {
        assertNotNull(theQuery);
        CommonTree tree = theQuery.getTree();
        assertNotNull(tree);
        assertEquals(WhereClauseParser.PROPERTYNAME, tree.getChild(0).getType());
        assertTrue(propName.equals(tree.getChild(0).getText()));
        assertEquals(WhereClauseParser.PROPERTYVALUE, tree.getChild(1).getType());
        QueryHelper.walk(theQuery, new WalkerCallbackAdapter() {
            @Override
            public void matches(String propertyName, String propertyValue, boolean negated)
            {
                assertTrue("Property name should be " + propName, propName.equals(propertyName));
                assertTrue("Property value should be " + propVal, propVal.equals(propertyValue));
            }

        });
    }

    /**
     * Use by the inClauseTest
     * 
     * @param theQuery
     *            Query
     * @param propName
     *            String
     * @param values
     *            String...
     */
    private void inChecks(Query theQuery, final String propName, final String... values)
    {
        assertNotNull(theQuery);
        CommonTree tree = theQuery.getTree();
        assertNotNull(tree);
        assertEquals(WhereClauseParser.IN, tree.getType());
        assertEquals(WhereClauseParser.PROPERTYNAME, tree.getChild(0).getType());
        assertTrue(propName.equals(tree.getChild(0).getText()));
        QueryHelper.walk(theQuery, new WalkerCallbackAdapter() {
            @Override
            public void in(String property, boolean negated, String... propertyValues)
            {
                assertTrue("Property name should be " + propName, propName.equals(property));
                for (int i = 0; i < values.length; i++)
                {
                    assertTrue("Value must match:" + values[i], values[i].equals(propertyValues[i]));
                }
            }

        });
    }

    /**
     * Helper class for walking the query.
     */
    private void assertExistsPropertyEquals(final String property, Query theQuery, final boolean isNegated)
    {
        assertNotNull(theQuery);
        CommonTree tree = theQuery.getTree();
        assertNotNull(tree);
        QueryHelper.walk(theQuery, new WalkerCallbackAdapter() {

            @Override
            public void exists(String propertyName, boolean negated)
            {
                assertTrue(property.equals(propertyName));
                assertTrue(isNegated == negated);
            }

        });
    }

}
