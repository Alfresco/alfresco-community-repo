/*
 * #%L
 * Alfresco Remote API
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
