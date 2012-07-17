/*
 * Copyright (C) 2005-2012
 Alfresco Software Limited.
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
package org.alfresco.util.test.junitrules;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.rules.ExternalResource;
import org.springframework.util.ReflectionUtils;

/**
 * A JUnit rule designed to help with the automatic revert of test objects with mocked fields.
 * This is intended to be used when writing test code and you wish to set a mock object on a spring singleton bean.
 * By mocking out fields on spring singletons beans, if you don't remember to revert them to their original values, then
 * any subsequent tests that expect to see 'proper' services plugged in, may fail when they are instead given a mock.
 * 
 * <p/>
 * Example usage:
 * <pre>
 * public class YourTestClass
 * {
 *     // Declare the rule.
 *     &#64;Rule public final TemporaryMockOverride mockOverride = new TemporaryMockOverride();
 *     
 *     &#64;Test public void aTestMethod()
 *     {
 *         // Get a singleton bean from the spring context
 *         FooService fooService = appContext.getBean("fooService", FooService.class);
 *         
 *         // Create a mocked service (this uses Mockito, but that's not required for this rule to work)
 *         BarService mockedBarService = mock(BarService.class);
 *         
 *         // Don't do this as you're just replacing the original barService: fooService.setBarService(mockedBarService);
 *         // Instead do this:
 *         mockOverride.setTemporaryField(fooService, barService, mockedBarService);
 *         
 *         // Go ahead and use the FooService in test code, whilst relying on a mocked BarService behind it.
 *         // After the rule has completed, the original BarService which spring injected into the FooService will be reset.
 *     }
 * }
 * </pre>
 * 
 * @author Neil Mc Erlean
 * @since Odin
 */
public class TemporaryMockOverride extends ExternalResource
{
    private static final Log log = LogFactory.getLog(TemporaryMockOverride.class);
    
    private List<FieldValueOverride> pristineFieldValues = new ArrayList<FieldValueOverride>();
    
    @Override protected void before() throws Throwable
    {
        // Intentionally empty
    }
    
    @Override protected void after()
    {
        // For all objects that have been tampered with, we'll revert them to their original state.
        for (int i = pristineFieldValues.size() - 1; i >= 0; i-- )
        {
            FieldValueOverride override = pristineFieldValues.get(i);
            
            if (log.isDebugEnabled())
            {
                log.debug("Reverting mocked field '" + override.fieldName + "' on object " + override.objectContainingField + " to original value '" + override.fieldPristineValue + "'");
            }
            
            // Hack into the Java field object
            Field f = ReflectionUtils.findField(override.objectContainingField.getClass(), override.fieldName);
            ReflectionUtils.makeAccessible(f);
            // and revert its value.
            ReflectionUtils.setField(f, override.objectContainingField, override.fieldPristineValue);
        }
    }
    
    public void setTemporaryField(Object objectContainingField, String fieldName, Object fieldValue)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Overriding field '" + fieldName + "' on object " + objectContainingField + " to new value '" + fieldValue + "'");
        }
        
        // Extract the pristine value of the field we're going to mock.
        Field f = ReflectionUtils.findField(objectContainingField.getClass(), fieldName);
        
        if (f == null)
        {
            final String msg = "Object of type '" + objectContainingField.getClass().getSimpleName() + "' has no field named '" + fieldName + "'";
            if (log.isDebugEnabled())
            {
                log.debug(msg);
            }
            throw new IllegalArgumentException(msg);
        }
        
        ReflectionUtils.makeAccessible(f);
        Object pristineValue = ReflectionUtils.getField(f, objectContainingField);
        
        // and add it to the list.
        pristineFieldValues.add(new FieldValueOverride(objectContainingField, fieldName, pristineValue));
        
        // and set it on the object
        ReflectionUtils.setField(f, objectContainingField, fieldValue);
    }
    
    private static class FieldValueOverride
    {
        public FieldValueOverride(Object objectContainingField, String fieldName, Object pristineValue)
        {
            this.objectContainingField = objectContainingField;
            this.fieldName = fieldName;
            this.fieldPristineValue = pristineValue;
        }
        
        public final Object objectContainingField;
        public final String fieldName;
        public final Object fieldPristineValue;
    }
}
