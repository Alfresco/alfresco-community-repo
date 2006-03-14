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
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.dictionary.DictionaryDAOTest;
import org.alfresco.service.cmr.dictionary.DictionaryException;

/**
 * @see org.alfresco.service.cmr.dictionary.Constraint
 * @see org.alfresco.repo.dictionary.constraint.AbstractConstraint
 * @see org.alfresco.repo.dictionary.constraint.RegexConstraint
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

        List<Object> dummyObjects = new ArrayList<Object>(3);
        dummyObjects.add("ABC");   // correct
        dummyObjects.add("DEF");   // correct
        dummyObjects.add(this);    // NO
        try
        {
            constraint.evaluate(dummyObjects);
            fail("Failed to detected constraint violation in collection");
        }
        catch (DictionaryException e)
        {
            // expected
        }
        // check that the two strings were properly dealt with
        assertEquals("String values not checked", 2, constraint.tested.size());
    }
    
    public void testRegexConstraint() throws Exception
    {
        RegexConstraint constraint = new RegexConstraint();
        constraint.setExpression("[A-Z]*");
        constraint.initialize();
        
        // do some successful stuff
        constraint.evaluate("ABC");
        constraint.evaluate("DEF");
        
        // now some failures
        try
        {
            constraint.evaluate("abc");
            fail("Regular expression evaluation should have failed: abc");
        }
        catch (DictionaryException e)
        {
            String msg = e.getMessage();
            assertFalse("I18N of constraint message failed", msg.startsWith("d_dictionary.constraint"));
        }
        
        // now a case of passing in an object that could be a string
        constraint.evaluate(DummyEnum.ABC);
        constraint.evaluate(DummyEnum.DEF);
        try
        {
            constraint.evaluate(DummyEnum.abc);
            fail("Regular expression evaluation should have failed for enum: " + DummyEnum.abc);
        }
        catch (DictionaryException e)
        {
        }
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
                throw new DictionaryException("Non-String value");
            }
        }
    }
}
