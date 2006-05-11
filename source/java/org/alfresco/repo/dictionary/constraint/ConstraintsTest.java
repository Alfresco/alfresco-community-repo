/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.dictionary.constraint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.dictionary.DictionaryDAOTest;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.dictionary.DictionaryException;

/**
 * <b>This file must be saved using UTF-8.</b>
 * 
 * @see org.alfresco.service.cmr.dictionary.Constraint
 * @see org.alfresco.repo.dictionary.constraint.AbstractConstraint
 * @see org.alfresco.repo.dictionary.constraint.RegexConstraint
 * 
 * @author Derek Hulley
 */
@SuppressWarnings("unused")
public class ConstraintsTest extends TestCase
{
    @Override
    protected void setUp() throws Exception
    {
        // register resource bundles for messages
        I18NUtil.registerResourceBundle(DictionaryDAOTest.TEST_RESOURCE_MESSAGES);
    }
    
    /**
     * ensure that the default handling of checks on collections will work
     */ 
    public void testCollections() throws Exception
    {
        DummyConstraint constraint = new DummyConstraint();
        constraint.initialize();

        List<Object> dummyObjects = new ArrayList<Object>(3);
        dummyObjects.add("ABC");   // correct
        dummyObjects.add("DEF");   // correct
        dummyObjects.add(this);    // NO
        try
        {
            constraint.evaluate(dummyObjects);
            fail("Failed to detected constraint violation in collection");
        }
        catch (ConstraintException e)
        {
            // expected
            checkI18NofExceptionMessage(e);
        }
        // check that the two strings were properly dealt with
        assertEquals("String values not checked", 2, constraint.tested.size());
    }
    
    public void testNull() throws Exception
    {
        DummyConstraint constraint = new DummyConstraint();
        constraint.initialize();
        
        // a null always passes
        constraint.evaluate(null);
    }
    
    private void checkI18NofExceptionMessage(Throwable e)
    {
        String msg = e.getMessage();
        assertFalse("I18N of constraint message failed", msg.startsWith("d_dictionary.constraint"));
    }
    
    private void evaluate(Constraint constraint, Object value, boolean expectFailure) throws Exception
    {
        try
        {
            constraint.evaluate(value);
            if (expectFailure)
            {
                // it should have failed
                fail("Failure did not occur: \n" +
                        "   constraint: " + constraint + "\n" +
                        "   value: " + value);
            }
        }
        catch (ConstraintException e)
        {
            // check if we expect an error
            if (expectFailure)
            {
                // expected - check message I18N
                checkI18NofExceptionMessage(e);
            }
            else
            {
                // didn't expect it
                throw e;
            }
        }
    }
    
    public void testStringLengthConstraint() throws Exception
    {
        StringLengthConstraint constraint = new StringLengthConstraint();
        try
        {
            constraint.setMinLength(-1);
        }
        catch (DictionaryException e)
        {
            // expected
            checkI18NofExceptionMessage(e);
        }
        try
        {
            constraint.setMaxLength(-1);
        }
        catch (DictionaryException e)
        {
            // expected
            checkI18NofExceptionMessage(e);
        }
        constraint.setMinLength(3);
        constraint.setMaxLength(6);
        
        evaluate(constraint, "abc", false);
        evaluate(constraint, "abcdef", false);
        evaluate(constraint, Arrays.asList("abc", "abcdef"), false);
        evaluate(constraint, "ab", true);
        evaluate(constraint, "abcdefg", true);
        evaluate(constraint, Arrays.asList("abc", "abcdefg"), true);
    }
    
    public void testListOfValuesConstraint() throws Exception
    {
        ListOfValuesConstraint constraint = new ListOfValuesConstraint();
        try
        {
            constraint.setAllowedValues(Collections.<String>emptyList());
        }
        catch (DictionaryException e)
        {
            // expected
            checkI18NofExceptionMessage(e);
        }
        List<String> allowedValues = Arrays.asList(new String[] {"abc", "def", "ghi"});
        constraint.setAllowedValues(allowedValues);
        
        evaluate(constraint, "def", false);
        evaluate(constraint, "DEF", true);
        evaluate(constraint, Arrays.asList("abc", "def"), false);
        evaluate(constraint, Arrays.asList("abc", "DEF"), true);
        
        // now make it case-insensitive
        constraint.setCaseSensitive(false);
        evaluate(constraint, "DEF", false);
        evaluate(constraint, Arrays.asList("abc", "DEF"), false);
    }
    
    public void testNumericRangeConstraint() throws Exception
    {
        NumericRangeConstraint constraint = new NumericRangeConstraint();
        constraint.initialize();
        
        // check that Double.MIN_VALUE and Double.MAX_VALUE are allowed by default
        constraint.evaluate(Double.MIN_VALUE);
        constraint.evaluate(Double.MAX_VALUE);
        
        // check that Double.NaN is not allowed by default
        evaluate(constraint, Double.NaN, true);
        
        // set some limits and check
        constraint.setMinValue(-5.0D);
        constraint.setMaxValue(+5.0D);
        constraint.initialize();
        
        evaluate(constraint, "-1.0", false);
        evaluate(constraint, "+1.0", false);
        evaluate(constraint, Arrays.asList(-1, 0, 1), false);
        evaluate(constraint, "abc", true);
        evaluate(constraint, 56.453E4, true);
        evaluate(constraint, Arrays.asList(-1, 6), true);
    }
    
    public void testRegexConstraint() throws Exception
    {
        RegexConstraint constraint = new RegexConstraint();
        constraint.setExpression("[A-Z]*");
        constraint.setRequiresMatch(true);
        constraint.initialize();
        
        // do some successful stuff
        evaluate(constraint, "ABC", false);
        evaluate(constraint, "DEF", false);
        
        // now some failures
        evaluate(constraint, "abc", true);
        
        // now a case of passing in an object that could be a string
        evaluate(constraint, DummyEnum.ABC, false);
        evaluate(constraint, DummyEnum.DEF, false);
        evaluate(constraint, DummyEnum.abc, true);
        
        // now switch the requiresMatch around
        constraint.setRequiresMatch(false);
        constraint.initialize();
        
        evaluate(constraint, DummyEnum.abc, false);
    }
    
    public void testRegexConstraintFilename() throws Exception
    {
        // we assume UTF-8
        String expression = new String(".*[\\\"\\*\\\\\\>\\<\\?\\/\\:\\|\\¬\\£\\%\\&\\+\\;]+.*".getBytes("UTF-8"));
        String invalidChars = new String("\"*\\><?/:|%&+;¬£".getBytes("UTF-8"));
        
        RegexConstraint constraint = new RegexConstraint();
        constraint.setExpression(expression);
        constraint.setRequiresMatch(false);
        constraint.initialize();
        
        // check that all the invalid chars cause failures
        for (int i = 0; i < invalidChars.length(); i++)
        {
            String invalidStr = invalidChars.substring(i, i+1);
            evaluate(constraint, invalidStr, true);
        }
        // check a bogus filename
        evaluate(constraint, "Bogus<>.txt", true);
        // ... and a valid one
        evaluate(constraint, "Company Home", false);
    }
    
    
    private enum DummyEnum
    {
        ABC,
        DEF,
        abc;
    }
    
    private class DummyConstraint extends AbstractConstraint
    {
        private List<Object> tested;
        
        public void initialize()
        {
            tested = new ArrayList<Object>(4);
        }

        /**
         * Fails on everything but String values, which pass.
         * Null values cause runtime exceptions and all other failures are by
         * DictionaryException.
         */
        @Override
        protected void evaluateSingleValue(Object value)
        {
            if (value == null)
            {
                throw new NullPointerException("Null value in dummy test");
            }
            else if (value instanceof String)
            {
                tested.add(value);
            }
            else
            {
                throw new ConstraintException("Non-String value");
            }
        }
    }
}
