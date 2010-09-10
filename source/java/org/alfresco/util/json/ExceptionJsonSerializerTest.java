/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.util.json;

import java.util.Arrays;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.json.JSONObject;


public class ExceptionJsonSerializerTest extends TestCase
{
    
    private ExceptionJsonSerializer serializer;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        serializer = new ExceptionJsonSerializer();
    }

    public void testIllegalArgumentException()
    {
        Exception expectedException = new IllegalArgumentException("This is the message");
        JSONObject obj = serializer.serialize(expectedException);
        Throwable actualException = serializer.deserialize(obj);
        assertEquals(expectedException.getClass(), actualException.getClass());
        assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    public void testAlfrescoRuntimeExceptionWithNoParams()
    {
        AlfrescoRuntimeException expectedException = new AlfrescoRuntimeException("message id");
        JSONObject obj = serializer.serialize(expectedException);
        Throwable actualException = serializer.deserialize(obj);
        assertEquals(expectedException.getClass(), actualException.getClass());
        assertEquals(expectedException.getMsgId(), ((AlfrescoRuntimeException)actualException).getMsgId());
        assertTrue(((AlfrescoRuntimeException)actualException).getMsgParams().length == 0);
    }

    public void testAlfrescoRuntimeExceptionWithParams()
    {
        AlfrescoRuntimeException expectedException = new AlfrescoRuntimeException("message id", 
                new Object[]{"one","two","three"});
        JSONObject obj = serializer.serialize(expectedException);
        Throwable actualException = serializer.deserialize(obj);
        assertEquals(expectedException.getClass(), actualException.getClass());
        assertEquals(expectedException.getMsgId(), ((AlfrescoRuntimeException)actualException).getMsgId());
        assertTrue(Arrays.deepEquals(expectedException.getMsgParams(), 
                ((AlfrescoRuntimeException)actualException).getMsgParams()));
    }

    public void testAccessDeniedException()
    {
        AccessDeniedException expectedException = new AccessDeniedException("message id");
        JSONObject obj = serializer.serialize(expectedException);
        Throwable actualException = serializer.deserialize(obj);
        assertEquals(expectedException.getClass(), actualException.getClass());
        assertEquals(expectedException.getMsgId(), ((AlfrescoRuntimeException)actualException).getMsgId());
        assertTrue(expectedException.getMsgParams() == null);
    }
}
