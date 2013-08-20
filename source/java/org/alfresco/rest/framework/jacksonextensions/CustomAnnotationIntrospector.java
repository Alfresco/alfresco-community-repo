package org.alfresco.rest.framework.jacksonextensions;

import java.lang.reflect.Method;

import org.alfresco.rest.framework.core.ResourceInspector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.introspect.AnnotatedClass;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.map.introspect.VisibilityChecker;

/**
 * 
 * @author Gethin James
 *
 */
public class CustomAnnotationIntrospector extends JacksonAnnotationIntrospector {

    private static Log logger = LogFactory.getLog(CustomAnnotationIntrospector.class);
    
    @Override
    public VisibilityChecker<?> findAutoDetectVisibility(AnnotatedClass ac, VisibilityChecker<?> checker)
    {
        
        return checker.withFieldVisibility(Visibility.NONE)
        .withSetterVisibility(Visibility.PUBLIC_ONLY)
        .withGetterVisibility(Visibility.PUBLIC_ONLY)
        .withIsGetterVisibility(Visibility.PUBLIC_ONLY)
        ;
        
    }

    @Override
    public String findGettablePropertyName(AnnotatedMethod am)
    {
        Method uniqueIdMethod = ResourceInspector.findUniqueIdMethod(am.getDeclaringClass());
        if (uniqueIdMethod != null && uniqueIdMethod.equals(am.getMember()))
        {
            {
                String uniqueIdPropertyName = ResourceInspector.findUniqueIdName(uniqueIdMethod);
                if (logger.isDebugEnabled())
                {
                    logger.debug("Changing the name of property: "+am.getFullName()+" to "+uniqueIdPropertyName);
                }
                
                return uniqueIdPropertyName;
            }
        }
        return super.findGettablePropertyName(am);
    }
    
}
