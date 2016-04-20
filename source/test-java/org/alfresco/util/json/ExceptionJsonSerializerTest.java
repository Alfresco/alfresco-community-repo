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
