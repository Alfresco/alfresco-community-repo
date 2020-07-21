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
package org.alfresco.repo.dictionary.constraint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.repo.dictionary.DictionaryDAOTest;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * <b>This file must be saved using UTF-8.</b>
 * 
 * @see org.alfresco.service.cmr.dictionary.Constraint
 * @see org.alfresco.repo.dictionary.constraint.AbstractConstraint
 * @see org.alfresco.repo.dictionary.constraint.RegexConstraint
 * @see org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint
 * 
 * @author Derek Hulley
 */
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
        
        assertEquals("DummyConstraint type should be 'org.alfresco.repo.dictionary.constraint.ConstraintsTest$DummyConstraint'", 
                    "org.alfresco.repo.dictionary.constraint.ConstraintsTest$DummyConstraint", 
                    constraint.getType());
        assertNotNull("DummyConstraint should not have empty parameters", constraint.getParameters());
        assertEquals("DummyConstraint should not have empty parameters", 0, constraint.getParameters().size());
        
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
        
        assertEquals("StringLengthConstraint type should be 'LENGTH'", 
                    "LENGTH", constraint.getType());
        assertNotNull("StringLengthConstraint should have parameters", constraint.getParameters());
        assertEquals("StringLengthConstraint should have 2 parameters", 2, constraint.getParameters().size());
        assertEquals("minLength should be 3", 3, 
                    constraint.getParameters().get("minLength"));
        assertEquals("maxLength should be 6", 6, 
                    constraint.getParameters().get("maxLength"));
        
        evaluate(constraint, "abc", false);
        evaluate(constraint, "abcdef", false);
        evaluate(constraint, Arrays.asList("abc", "abcdef"), false);
        evaluate(constraint, "ab", true);
        evaluate(constraint, "abcdefg", true);
        evaluate(constraint, Arrays.asList("abc", "abcdefg"), true);
    }
    
    @SuppressWarnings("unchecked")
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
        List<String> allowedValues = Arrays.asList(new String[] {"abc", "def", "ghi", " jkl "});
        constraint.setAllowedValues(allowedValues);
        
        assertEquals("ListOfValuesConstraint type should be 'LIST'", 
                    "LIST", constraint.getType());
        assertNotNull("ListOfValuesConstraint should have parameters", constraint.getParameters());
        assertEquals("ListOfValuesConstraint should have 3 parameters", 3, constraint.getParameters().size());
        assertEquals("caseSensitive should be true", Boolean.TRUE,
                    constraint.getParameters().get("caseSensitive"));
        List<String> allowedValuesParam = (List<String>)constraint.getParameters().get("allowedValues");
        assertEquals("Should be 4 allowable values", 4, allowedValuesParam.size());
        assertEquals("First allowable value should be 'abc'", "abc", allowedValuesParam.get(0));
        assertEquals("First allowable value should be 'def'", "def", allowedValuesParam.get(1));
        assertEquals("First allowable value should be 'ghi'", "ghi", allowedValuesParam.get(2));
        assertEquals("First allowable value should be ' jkl '", " jkl ", allowedValuesParam.get(3));
        Boolean sorted = (Boolean)constraint.getParameters().get("sorted");
        assertFalse("sorting should be false", sorted.booleanValue());
        
        evaluate(constraint, "def", false);
        evaluate(constraint, "DEF", true);
        evaluate(constraint, Arrays.asList("abc", "def"), false);
        evaluate(constraint, Arrays.asList("abc", "DEF"), true);
        
        // now make it case-insensitive
        constraint.setCaseSensitive(false);
        assertEquals("caseSensitive should be false", Boolean.FALSE,
                    constraint.getParameters().get("caseSensitive"));
        evaluate(constraint, "DEF", false);
        evaluate(constraint, Arrays.asList("abc", "DEF"), false);
        
        // Check leading and trailing spaces are respected
        evaluate(constraint, " jkl ", false);
        evaluate(constraint, "jkl", true);
        evaluate(constraint, " jkl", true);
        evaluate(constraint, "jkl ", true);
        evaluate(constraint, " jkl  ", true);
        evaluate(constraint, Arrays.asList(" jkl ", " JKL "), false);
        evaluate(constraint, Arrays.asList("  jkl  ", "  JKL  "), true);
    }
    
    public void testNumericRangeConstraint() throws Exception
    {
        NumericRangeConstraint constraint = new NumericRangeConstraint();
        constraint.initialize();
        
        assertEquals("NumericRangeConstraint type should be 'MINMAX'", 
                    "MINMAX", constraint.getType());
        assertNotNull("NumericRangeConstraint should have parameters", constraint.getParameters());
        assertEquals("NumericRangeConstraint should have 2 parameters", 2, constraint.getParameters().size());
        
        // check that Double.MIN_VALUE and Double.MAX_VALUE are allowed by default
        constraint.evaluate(Double.MIN_VALUE);
        constraint.evaluate(Double.MAX_VALUE);
        
        // check that Double.NaN is not allowed by default
        evaluate(constraint, Double.NaN, true);
        
        // set some limits and check
        constraint.setMinValue(-5.0D);
        constraint.setMaxValue(+5.0D);
        constraint.initialize();
        
        assertEquals("minValue should be -5", -5.0D, constraint.getParameters().get("minValue"));
        assertEquals("maxValue should be 5", 5.0D, constraint.getParameters().get("maxValue"));
        
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
        
        assertEquals("RegexConstraint type should be 'REGEX'", 
                    "REGEX", constraint.getType());
        assertNotNull("RegexConstraint should have parameters", constraint.getParameters());
        assertEquals("RegexConstraint should have 2 parameters", 2, constraint.getParameters().size());
        assertEquals("requiresMatch should be true", Boolean.TRUE,
                    constraint.getParameters().get("requiresMatch"));
        assertEquals("expression should be [A-Z]*", "[A-Z]*",
                    constraint.getParameters().get("expression"));
        
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
        assertEquals("requiresMatch should be false", Boolean.FALSE,
                    constraint.getParameters().get("requiresMatch"));
        
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

        @Override
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
