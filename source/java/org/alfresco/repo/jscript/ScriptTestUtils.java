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
package org.alfresco.repo.jscript;

import org.alfresco.error.AlfrescoRuntimeException;


/**
 * @author Roy Wetherall
 */
public class ScriptTestUtils extends BaseScopableProcessorExtension
{    
    public void assertEquals(Object expected, Object value)
    {
        assertEquals(expected, value, null);
    }
    
    public void assertEquals(Object expected, Object value, String message)
    {
        if (expected == null && value == null)
        {
           return;
        }
        
        if ((expected == null && value != null) || expected.equals(value) == false)
        {
            if (message == null)
            {
                message = "Expected value '" + expected + "' was '" + value + "'";
            }
            throw new AlfrescoRuntimeException(message);
        }
    }
    
    public void assertNotEquals(Object expected, Object value)
    {
        assertNotEquals(expected, value, null);
    }
    
    public void assertNotEquals(Object expected, Object value, String message)
    {
        if (expected.equals(value) == true)
        {
            if (message == null)
            {
                message = "Expected value '" + expected + "' should not match recieved value '" + value + "'";
            }
            throw new AlfrescoRuntimeException(message);
        }
    }
    
    public void assertNotNull(Object value)
    {
        assertNotNull(value, null);
    }
    
    public void assertNotNull(Object value, String message)
    {
        if (value == null)
        {
            if (message == null)
            {
                message = "Unexpected null value encountered.";
            }
            throw new AlfrescoRuntimeException(message);
        }
    }
    
    public void assertNull(Object value)
    {
        assertNull(value, null);
    }
            
    public void assertNull(Object value, String message)
    {
        if (value != null)
        {
            if (message == null)
            {
                message = "Unexpected non-null value encountered.";
            }
            throw new AlfrescoRuntimeException(message);
        }
    }
    
    public void assertTrue(boolean value)
    {
        assertTrue(value, null);
    }            
            
    public void assertTrue(boolean value, String message)
    {
        if (value == false)
        {
            if (message == null)
            {
                message = "Value is not True";
            }
            throw new AlfrescoRuntimeException(message);
        }
    }
    
    public void assertFalse(boolean value)
    {
        assertFalse(value, null);
    }
    
    public void assertFalse(boolean value, String message)
    {
        if (value == true)
        {
            if (message == null)
            {
                message = "Value is not False";
            }
            throw new AlfrescoRuntimeException(message);
        }
    }
    
    public void assertContains(String value, String subString)
    {
        assertContains(value, subString, null);
    }
    
    public void assertContains(String value, String subString, String message)
    {
        if ( !value.contains(subString))
        {
            throw new AlfrescoRuntimeException(message);
        }
    }
    
    public void fail(String message)
    {
        throw new AlfrescoRuntimeException(message);
    }
}
