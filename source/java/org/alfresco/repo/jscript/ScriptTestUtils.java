/*
 * Copyright (C) 2005 Alfresco, Inc.
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
        if (expected.equals(value) == false)
        {
            if (message == null)
            {
                message = "Expected value '" + expected + "' was '" + value + "'";
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
    
    public void fail(String message)
    {
        throw new AlfrescoRuntimeException(message);
    }
}
