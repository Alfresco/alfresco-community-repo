package org.alfresco.repo.audit;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.alfresco.service.Auditable;

/**
 * @author Andy Hind
 */
public class AuditableAnnotationTest extends TestCase
{

    public AuditableAnnotationTest()
    {
        super();
    }

    @SuppressWarnings("unchecked")
    public void testAnnotations() throws Exception, NoSuchMethodException
    {
        Class clazz = AnnotationTestInterface.class;
        
        Method method = clazz.getMethod("noArgs", new Class[]{});
        assertTrue(method.isAnnotationPresent(Auditable.class));
        Auditable auditable = method.getAnnotation(Auditable.class);
        assertEquals(auditable.parameters().length, 0);
        
        
        method = clazz.getMethod("getString", new Class[]{String.class, String.class});
        assertTrue(method.isAnnotationPresent(Auditable.class));
        auditable = method.getAnnotation(Auditable.class);
        assertEquals(auditable.parameters().length, 2);
        assertEquals(auditable.parameters()[0], "one");
        assertEquals(auditable.parameters()[1], "two");
       
        
        method = clazz.getMethod("getAnotherString", new Class[]{String.class});
        assertTrue(method.isAnnotationPresent(Auditable.class));
        auditable = method.getAnnotation(Auditable.class);
        assertEquals(auditable.parameters().length, 1);
        assertEquals(auditable.parameters()[0], "one");
    }
}
